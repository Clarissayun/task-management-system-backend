package com.clarissa.task_management_system_backend.service;

import com.clarissa.task_management_system_backend.model.Task;
import com.clarissa.task_management_system_backend.model.Project;
import com.clarissa.task_management_system_backend.model.TaskStatus;
import com.clarissa.task_management_system_backend.model.TaskPriority;
import com.clarissa.task_management_system_backend.dto.task.TaskRequest;
import com.clarissa.task_management_system_backend.dto.task.TaskResponse;
import com.clarissa.task_management_system_backend.exception.BadRequestException;
import com.clarissa.task_management_system_backend.exception.ResourceNotFoundException;
import com.clarissa.task_management_system_backend.repository.ProjectRepository;
import com.clarissa.task_management_system_backend.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.regex.Pattern;

@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MongoTemplate mongoTemplate;
    
    /**
     * Create a new task for a user
     */
    public TaskResponse createTask(String userId, String projectId, TaskRequest request) {
        String effectiveProjectId = normalizeProjectId(projectId != null ? projectId : request.getProjectId());

        LocalDateTime now = LocalDateTime.now();
        validateTaskDueDate(userId, effectiveProjectId, request.getDueDate(), now.toLocalDate());

        Task task = new Task();
        task.setUserId(userId);
        task.setProjectId(effectiveProjectId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority() != null ? request.getPriority() : TaskPriority.LOW);
        task.setStatus(TaskStatus.TODO); // Default status
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        
        Task savedTask = taskRepository.save(task);
        return convertToResponse(savedTask);
    }
    
    /**
     * Get tasks for a user. When projectId is provided, only project-scoped tasks are returned.
     * When projectId is absent, only standalone tasks are returned.
     */
    public List<TaskResponse> getTasksByUserId(String userId, String projectId) {
        List<Task> tasks;
        String effectiveProjectId = normalizeProjectId(projectId);

        if (effectiveProjectId != null) {
            ensureProjectOwnership(userId, effectiveProjectId);
            tasks = taskRepository.findByUserIdAndProjectId(userId, effectiveProjectId);
        } else {
            tasks = taskRepository.findByUserIdAndProjectIdIsNull(userId);
        }

        return tasks
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tasks by user ID, optional project ID, and status
     */
    public List<TaskResponse> getTasksByUserIdAndStatus(String userId, String projectId, TaskStatus status) {
        List<Task> tasks;
        String effectiveProjectId = normalizeProjectId(projectId);

        if (effectiveProjectId != null) {
            ensureProjectOwnership(userId, effectiveProjectId);
            tasks = taskRepository.findByUserIdAndProjectIdAndStatus(userId, effectiveProjectId, status);
        } else {
            tasks = taskRepository.findByUserIdAndProjectIdIsNullAndStatus(userId, status);
        }

        return tasks
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tasks by user ID, optional project ID, and priority
     */
    public List<TaskResponse> getTasksByUserIdAndPriority(String userId, String projectId, TaskPriority priority) {
        List<Task> tasks;
        String effectiveProjectId = normalizeProjectId(projectId);

        if (effectiveProjectId != null) {
            ensureProjectOwnership(userId, effectiveProjectId);
            tasks = taskRepository.findByUserIdAndProjectIdAndPriority(userId, effectiveProjectId, priority);
        } else {
            tasks = taskRepository.findByUserIdAndProjectIdIsNullAndPriority(userId, priority);
        }

        return tasks
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get a single task by ID
     */
    public TaskResponse getTaskById(String userId, String taskId) {
        Task task = findUserTaskOrThrow(userId, taskId);
        return convertToResponse(task);
    }
    
    /**
     * Update a task
     */
    public TaskResponse updateTask(String userId, String taskId, TaskRequest request) {
        Task task = findUserTaskOrThrow(userId, taskId);

        String requestedProjectId = normalizeProjectId(request.getProjectId());
        String effectiveProjectId = requestedProjectId != null ? requestedProjectId : normalizeProjectId(task.getProjectId());

        if (request.getProjectId() != null) {
            if (requestedProjectId != null) {
                task.setProjectId(requestedProjectId);
            } else {
                task.setProjectId(null);
            }
        }

        LocalDate taskCreationDate = task.getCreatedAt() != null
            ? task.getCreatedAt().toLocalDate()
            : LocalDate.now();
        validateTaskDueDate(userId, effectiveProjectId, request.getDueDate(), taskCreationDate);
        
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        task.setUpdatedAt(LocalDateTime.now());
        
        Task updatedTask = taskRepository.save(task);
        return convertToResponse(updatedTask);
    }
    
    /**
     * Update task status
     */
    public TaskResponse updateTaskStatus(String userId, String taskId, TaskStatus status) {
        Task task = findUserTaskOrThrow(userId, taskId);
        
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());
        
        Task updatedTask = taskRepository.save(task);
        return convertToResponse(updatedTask);
    }
    
    /**
     * Delete a task
     */
    public void deleteTask(String userId, String taskId) {
        Task task = findUserTaskOrThrow(userId, taskId);
        taskRepository.delete(task);
    }
    
    /**
     * Delete tasks for a user. When projectId is provided, only tasks in that project are deleted.
     * When projectId is absent, only standalone tasks are deleted.
     */
    public void deleteAllTasksByUserId(String userId, String projectId) {
        String effectiveProjectId = normalizeProjectId(projectId);
        if (effectiveProjectId != null) {
            ensureProjectOwnership(userId, effectiveProjectId);
            taskRepository.deleteByUserIdAndProjectId(userId, effectiveProjectId);
            return;
        }

        taskRepository.deleteByUserIdAndProjectIdIsNull(userId);
    }
    
    /**
     * Get paginated tasks for a user with optional filters.
     * Supports filtering by projectId, status, priority, and search text.
     */
    public Page<TaskResponse> searchTasks(
            String userId,
            String projectId,
            TaskStatus status,
            TaskPriority priority,
            String search,
            LocalDate dueDateFrom,
            LocalDate dueDateTo,
            Pageable pageable) {
        String effectiveProjectId = normalizeProjectId(projectId);
        if (effectiveProjectId != null) {
            ensureProjectOwnership(userId, effectiveProjectId);
        }

        validateDateRange(dueDateFrom, dueDateTo, "Task due date range is invalid");

        Query query = buildTaskSearchQuery(userId, effectiveProjectId, status, priority, search, dueDateFrom, dueDateTo);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Task.class);
        List<Task> tasks = mongoTemplate.find(query.with(pageable), Task.class);

        return new PageImpl<>(
                tasks.stream().map(this::convertToResponse).collect(Collectors.toList()),
                pageable,
                total
        );
    }
    
    /**
     * Convert Task entity to TaskResponse DTO
     */
    private TaskResponse convertToResponse(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getUserId(),
            task.getProjectId(),
            task.getTitle(),
            task.getDescription(),
            task.getDueDate(),
            task.getStatus(),
            task.getPriority(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }

    private Task findUserTaskOrThrow(String userId, String taskId) {
        return taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private void ensureProjectOwnership(String userId, String projectId) {
        projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private String normalizeProjectId(String projectId) {
        if (projectId == null) {
            return null;
        }

        String normalized = projectId.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void validateTaskDueDate(String userId, String projectId, LocalDate taskDueDate, LocalDate taskCreationDate) {
        if (taskDueDate == null) {
            throw new BadRequestException("Task due date is required");
        }

        if (taskCreationDate != null && taskDueDate.isBefore(taskCreationDate)) {
            throw new BadRequestException("Task due date cannot be before task creation date");
        }

        if (projectId == null) {
            return;
        }

        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        LocalDate startDate = project.getStartDate();
        LocalDate dueDate = project.getDueDate();

        if (dueDate != null && taskDueDate.isAfter(dueDate)) {
            throw new BadRequestException("Task due date cannot be after project due date");
        }

        if (startDate != null && taskDueDate.isBefore(startDate)) {
            throw new BadRequestException("Task due date cannot be before project start date");
        }

    }

    private Query buildTaskSearchQuery(
            String userId,
            String projectId,
            TaskStatus status,
            TaskPriority priority,
            String search,
            LocalDate dueDateFrom,
            LocalDate dueDateTo) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("userId").is(userId));

        if (projectId != null) {
            criteriaList.add(Criteria.where("projectId").is(projectId));
        }

        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        if (priority != null) {
            criteriaList.add(Criteria.where("priority").is(priority));
        }

        if (search != null && !search.isBlank()) {
            Pattern searchPattern = Pattern.compile(Pattern.quote(search.trim()), Pattern.CASE_INSENSITIVE);
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("title").regex(searchPattern),
                    Criteria.where("description").regex(searchPattern)
            ));
        }

        if (dueDateFrom != null || dueDateTo != null) {
            Criteria dueDateCriteria = Criteria.where("dueDate");
            if (dueDateFrom != null) {
                dueDateCriteria = dueDateCriteria.gte(dueDateFrom);
            }
            if (dueDateTo != null) {
                dueDateCriteria = dueDateCriteria.lte(dueDateTo);
            }
            criteriaList.add(dueDateCriteria);
        }

        return new Query().addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
    }

    private void validateDateRange(LocalDate from, LocalDate to, String errorMessage) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException(errorMessage);
        }
    }
}

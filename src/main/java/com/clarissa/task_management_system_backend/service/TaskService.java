package com.clarissa.task_management_system_backend.service;

import com.clarissa.task_management_system_backend.model.Task;
import com.clarissa.task_management_system_backend.model.TaskStatus;
import com.clarissa.task_management_system_backend.model.TaskPriority;
import com.clarissa.task_management_system_backend.dto.task.TaskRequest;
import com.clarissa.task_management_system_backend.dto.task.TaskResponse;
import com.clarissa.task_management_system_backend.exception.ResourceNotFoundException;
import com.clarissa.task_management_system_backend.repository.ProjectRepository;
import com.clarissa.task_management_system_backend.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;
    
    /**
     * Create a new task for a user
     */
    public TaskResponse createTask(String userId, String projectId, TaskRequest request) {
        String effectiveProjectId = normalizeProjectId(projectId != null ? projectId : request.getProjectId());
        if (effectiveProjectId != null) {
            ensureProjectOwnership(userId, effectiveProjectId);
        }

        Task task = new Task();
        task.setUserId(userId);
        task.setProjectId(effectiveProjectId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority() != null ? request.getPriority() : TaskPriority.LOW);
        task.setStatus(TaskStatus.TODO); // Default status
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
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
        if (request.getProjectId() != null) {
            if (requestedProjectId != null) {
                ensureProjectOwnership(userId, requestedProjectId);
                task.setProjectId(requestedProjectId);
            } else {
                task.setProjectId(null);
            }
        }
        
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
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
     * Convert Task entity to TaskResponse DTO
     */
    private TaskResponse convertToResponse(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getUserId(),
            task.getProjectId(),
            task.getTitle(),
            task.getDescription(),
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
}

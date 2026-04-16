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
    public TaskResponse createTask(String userId, TaskRequest request) {
        Task task = new Task();
        task.setUserId(userId);
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
     * Get all tasks for a user
     */
    public List<TaskResponse> getTasksByUserId(String userId) {
        return taskRepository.findByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tasks by user ID and status
     */
    public List<TaskResponse> getTasksByUserIdAndStatus(String userId, TaskStatus status) {
        return taskRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tasks by user ID and priority
     */
    public List<TaskResponse> getTasksByUserIdAndPriority(String userId, TaskPriority priority) {
        return taskRepository.findByUserIdAndPriority(userId, priority)
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
     * Delete all tasks for a user
     */
    public void deleteAllTasksByUserId(String userId) {
        taskRepository.deleteByUserId(userId);
    }

    /**
     * Create a new task within a project
     */
    public TaskResponse createTaskInProject(String userId, String projectId, TaskRequest request) {
        ensureProjectOwnership(userId, projectId);

        Task task = new Task();
        task.setUserId(userId);
        task.setProjectId(projectId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority() != null ? request.getPriority() : TaskPriority.LOW);
        task.setStatus(TaskStatus.TODO);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);
        return convertToResponse(savedTask);
    }

    /**
     * Get all tasks within a project
     */
    public List<TaskResponse> getTasksByProjectId(String userId, String projectId) {
        ensureProjectOwnership(userId, projectId);

        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by project ID and status
     */
    public List<TaskResponse> getTasksByProjectIdAndStatus(String userId, String projectId, TaskStatus status) {
        ensureProjectOwnership(userId, projectId);

        return taskRepository.findByProjectIdAndStatus(projectId, status)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by project ID and priority
     */
    public List<TaskResponse> getTasksByProjectIdAndPriority(String userId, String projectId, TaskPriority priority) {
        ensureProjectOwnership(userId, projectId);

        return taskRepository.findByProjectIdAndPriority(projectId, priority)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a task within a project
     */
    public TaskResponse getTaskInProject(String userId, String projectId, String taskId) {
        ensureProjectOwnership(userId, projectId);

        Task task = findProjectTaskOrThrow(userId, projectId, taskId);
        return convertToResponse(task);
    }

    /**
     * Update a task within a project
     */
    public TaskResponse updateTaskInProject(String userId, String projectId, String taskId, TaskRequest request) {
        ensureProjectOwnership(userId, projectId);

        Task task = findProjectTaskOrThrow(userId, projectId, taskId);

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
     * Update task status within a project
     */
    public TaskResponse updateTaskStatusInProject(String userId, String projectId, String taskId, TaskStatus status) {
        ensureProjectOwnership(userId, projectId);

        Task task = findProjectTaskOrThrow(userId, projectId, taskId);

        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);
        return convertToResponse(updatedTask);
    }

    /**
     * Delete a task within a project
     */
    public void deleteTaskInProject(String userId, String projectId, String taskId) {
        ensureProjectOwnership(userId, projectId);

        Task task = findProjectTaskOrThrow(userId, projectId, taskId);
        taskRepository.delete(task);
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
    
    private Task findProjectTaskOrThrow(String userId, String projectId, String taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .filter(task -> userId.equals(task.getUserId()))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private void ensureProjectOwnership(String userId, String projectId) {
        projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }
}

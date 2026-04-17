package com.clarissa.task_management_system_backend.controller;

import com.clarissa.task_management_system_backend.model.TaskStatus;
import com.clarissa.task_management_system_backend.model.TaskPriority;
import com.clarissa.task_management_system_backend.dto.task.TaskRequest;
import com.clarissa.task_management_system_backend.dto.task.TaskResponse;
import com.clarissa.task_management_system_backend.exception.BadRequestException;
import com.clarissa.task_management_system_backend.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    /**
     * Create a new task
     * POST /api/tasks?userId={userId}
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @RequestParam(required = false) String userId,
            Authentication authentication,
            @Valid @RequestBody TaskRequest request) {
        String effectiveUserId = resolveUserId(authentication, userId);
        TaskResponse response = taskService.createTask(effectiveUserId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get all tasks for a user
     * GET /api/tasks?userId={userId}
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasksByUserId(
            @RequestParam(required = false) String userId,
            Authentication authentication) {
        String effectiveUserId = resolveUserId(authentication, userId);
        List<TaskResponse> tasks = taskService.getTasksByUserId(effectiveUserId);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Get tasks by user and status
     * GET /api/tasks/status?userId={userId}&status={status}
     * Example: GET /api/tasks/status?userId=123&status=TODO
     */
    @GetMapping("/status")
    public ResponseEntity<List<TaskResponse>> getTasksByUserIdAndStatus(
            @RequestParam(required = false) String userId,
            Authentication authentication,
            @RequestParam TaskStatus status) {
        String effectiveUserId = resolveUserId(authentication, userId);
        List<TaskResponse> tasks = taskService.getTasksByUserIdAndStatus(effectiveUserId, status);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Get tasks by user and priority
     * GET /api/tasks/priority?userId={userId}&priority={priority}
     * Example: GET /api/tasks/priority?userId=123&priority=HIGH
     */
    @GetMapping("/priority")
    public ResponseEntity<List<TaskResponse>> getTasksByUserIdAndPriority(
            @RequestParam(required = false) String userId,
            Authentication authentication,
            @RequestParam TaskPriority priority) {
        String effectiveUserId = resolveUserId(authentication, userId);
        List<TaskResponse> tasks = taskService.getTasksByUserIdAndPriority(effectiveUserId, priority);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Get a single task by ID
     * GET /api/tasks/{taskId}
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable String taskId,
            Authentication authentication) {
        String userId = resolveUserId(authentication, null);
        TaskResponse task = taskService.getTaskById(userId, taskId);
        return ResponseEntity.ok(task);
    }
    
    /**
     * Update a task (title, description, priority)
     * PUT /api/tasks/{taskId}
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable String taskId,
            Authentication authentication,
            @Valid @RequestBody TaskRequest request) {
        String userId = resolveUserId(authentication, null);
        TaskResponse response = taskService.updateTask(userId, taskId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update task status only
     * PUT /api/tasks/{taskId}/status?status={status}
     * Example: PUT /api/tasks/123/status?status=IN_PROGRESS
     */
    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable String taskId,
            Authentication authentication,
            @RequestParam TaskStatus status) {
        String userId = resolveUserId(authentication, null);
        TaskResponse response = taskService.updateTaskStatus(userId, taskId, status);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a single task
     * DELETE /api/tasks/{taskId}
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable String taskId,
            Authentication authentication) {
        String userId = resolveUserId(authentication, null);
        taskService.deleteTask(userId, taskId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Delete all tasks for a user
     * DELETE /api/tasks?userId={userId}
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllTasksByUserId(
            @RequestParam(required = false) String userId,
            Authentication authentication) {
        String effectiveUserId = resolveUserId(authentication, userId);
        taskService.deleteAllTasksByUserId(effectiveUserId);
        return ResponseEntity.noContent().build();
    }

    private String resolveUserId(Authentication authentication, String requestedUserId) {
        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("Unable to resolve authenticated user");
        }

        String authenticatedUserId = authentication.getName();

        if (requestedUserId == null || requestedUserId.isBlank()) {
            return authenticatedUserId;
        }

        if (!authenticatedUserId.equals(requestedUserId)) {
            throw new AccessDeniedException("You can only access your own tasks");
        }

        return authenticatedUserId;
    }
}

package com.clarissa.task_management_system_backend.controller;

import com.clarissa.task_management_system_backend.model.TaskStatus;
import com.clarissa.task_management_system_backend.model.TaskPriority;
import com.clarissa.task_management_system_backend.dto.task.TaskRequest;
import com.clarissa.task_management_system_backend.dto.task.TaskResponse;
import com.clarissa.task_management_system_backend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
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
            @RequestParam String userId,
            @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get all tasks for a user
     * GET /api/tasks?userId={userId}
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasksByUserId(@RequestParam String userId) {
        List<TaskResponse> tasks = taskService.getTasksByUserId(userId);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Get tasks by user and status
     * GET /api/tasks/status?userId={userId}&status={status}
     * Example: GET /api/tasks/status?userId=123&status=TODO
     */
    @GetMapping("/status")
    public ResponseEntity<List<TaskResponse>> getTasksByUserIdAndStatus(
            @RequestParam String userId,
            @RequestParam TaskStatus status) {
        List<TaskResponse> tasks = taskService.getTasksByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Get tasks by user and priority
     * GET /api/tasks/priority?userId={userId}&priority={priority}
     * Example: GET /api/tasks/priority?userId=123&priority=HIGH
     */
    @GetMapping("/priority")
    public ResponseEntity<List<TaskResponse>> getTasksByUserIdAndPriority(
            @RequestParam String userId,
            @RequestParam TaskPriority priority) {
        List<TaskResponse> tasks = taskService.getTasksByUserIdAndPriority(userId, priority);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Get a single task by ID
     * GET /api/tasks/{taskId}
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable String taskId) {
        TaskResponse task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(task);
    }
    
    /**
     * Update a task (title, description, priority)
     * PUT /api/tasks/{taskId}
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable String taskId,
            @RequestBody TaskRequest request) {
        TaskResponse response = taskService.updateTask(taskId, request);
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
            @RequestParam TaskStatus status) {
        TaskResponse response = taskService.updateTaskStatus(taskId, status);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a single task
     * DELETE /api/tasks/{taskId}
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable String taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Delete all tasks for a user
     * DELETE /api/tasks?userId={userId}
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllTasksByUserId(@RequestParam String userId) {
        taskService.deleteAllTasksByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}

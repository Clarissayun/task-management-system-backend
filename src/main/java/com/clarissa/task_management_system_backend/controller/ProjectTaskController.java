package com.clarissa.task_management_system_backend.controller;

import com.clarissa.task_management_system_backend.dto.task.TaskRequest;
import com.clarissa.task_management_system_backend.dto.task.TaskResponse;
import com.clarissa.task_management_system_backend.exception.BadRequestException;
import com.clarissa.task_management_system_backend.model.TaskPriority;
import com.clarissa.task_management_system_backend.model.TaskStatus;
import com.clarissa.task_management_system_backend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@CrossOrigin(origins = "*")
public class ProjectTaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTaskInProject(
            @PathVariable String projectId,
            Authentication authentication,
            @RequestBody TaskRequest request) {
        String userId = resolveUserId(authentication);
        TaskResponse response = taskService.createTaskInProject(userId, projectId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasksByProjectId(
            @PathVariable String projectId,
            Authentication authentication) {
        String userId = resolveUserId(authentication);
        List<TaskResponse> tasks = taskService.getTasksByProjectId(userId, projectId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/status")
    public ResponseEntity<List<TaskResponse>> getTasksByProjectIdAndStatus(
            @PathVariable String projectId,
            Authentication authentication,
            @RequestParam TaskStatus status) {
        String userId = resolveUserId(authentication);
        List<TaskResponse> tasks = taskService.getTasksByProjectIdAndStatus(userId, projectId, status);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/priority")
    public ResponseEntity<List<TaskResponse>> getTasksByProjectIdAndPriority(
            @PathVariable String projectId,
            Authentication authentication,
            @RequestParam TaskPriority priority) {
        String userId = resolveUserId(authentication);
        List<TaskResponse> tasks = taskService.getTasksByProjectIdAndPriority(userId, projectId, priority);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskInProject(
            @PathVariable String projectId,
            @PathVariable String taskId,
            Authentication authentication) {
        String userId = resolveUserId(authentication);
        TaskResponse task = taskService.getTaskInProject(userId, projectId, taskId);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTaskInProject(
            @PathVariable String projectId,
            @PathVariable String taskId,
            Authentication authentication,
            @RequestBody TaskRequest request) {
        String userId = resolveUserId(authentication);
        TaskResponse response = taskService.updateTaskInProject(userId, projectId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateTaskStatusInProject(
            @PathVariable String projectId,
            @PathVariable String taskId,
            Authentication authentication,
            @RequestParam TaskStatus status) {
        String userId = resolveUserId(authentication);
        TaskResponse response = taskService.updateTaskStatusInProject(userId, projectId, taskId, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTaskInProject(
            @PathVariable String projectId,
            @PathVariable String taskId,
            Authentication authentication) {
        String userId = resolveUserId(authentication);
        taskService.deleteTaskInProject(userId, projectId, taskId);
        return ResponseEntity.noContent().build();
    }

    private String resolveUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("Unable to resolve authenticated user");
        }

        String authenticatedUserId = authentication.getName();
        if (authenticatedUserId.isBlank()) {
            throw new AccessDeniedException("Invalid authenticated user");
        }

        return authenticatedUserId;
    }
}

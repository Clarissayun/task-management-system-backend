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
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final int DEFAULT_UNPAGED_FALLBACK_SIZE = 100;
    private static final int MAX_PAGE_SIZE = 100;
    
    @Autowired
    private TaskService taskService;
    
    /**
     * Create a new task
     * POST /api/tasks?userId={userId}
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String projectId,
            Authentication authentication,
            @Valid @RequestBody TaskRequest request) {
        String effectiveUserId = resolveUserId(authentication, userId);
        TaskResponse response = taskService.createTask(effectiveUserId, projectId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Get paginated tasks with optional filters.
     * GET /api/tasks/paginated?userId={userId}&page=0&size=10&sort=createdAt,desc&projectId=&status=&priority=&search=
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<TaskResponse>> searchTasks(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate dueDateFrom,
            @RequestParam(required = false) LocalDate dueDateTo,
            Authentication authentication,
            Pageable pageable) {
        String effectiveUserId = resolveUserId(authentication, userId);
        Page<TaskResponse> tasks = taskService.searchTasks(
            effectiveUserId,
            projectId,
            status,
            priority,
            search,
            dueDateFrom,
            dueDateTo,
            sanitizePageable(pageable)
        );
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get tasks with optional filters (backward-compatible non-paginated endpoint).
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasksByUserId(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate dueDateFrom,
            @RequestParam(required = false) LocalDate dueDateTo,
            Authentication authentication) {
        String effectiveUserId = resolveUserId(authentication, userId);
        Page<TaskResponse> page = taskService.searchTasks(
            effectiveUserId,
            projectId,
            status,
            priority,
            search,
            dueDateFrom,
            dueDateTo,
            defaultListPageable()
        );
        return ResponseEntity.ok(page.getContent());
    }

    /**
     * Backward-compatible filtered status endpoint.
     */
    @GetMapping("/status")
    public ResponseEntity<List<TaskResponse>> getTasksByUserIdAndStatus(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String projectId,
            Authentication authentication,
            @RequestParam TaskStatus status,
            @RequestParam(required = false) LocalDate dueDateFrom,
            @RequestParam(required = false) LocalDate dueDateTo) {
        String effectiveUserId = resolveUserId(authentication, userId);
        Page<TaskResponse> page = taskService.searchTasks(
            effectiveUserId,
            projectId,
            status,
            null,
            null,
            dueDateFrom,
            dueDateTo,
            defaultListPageable()
        );
        return ResponseEntity.ok(page.getContent());
    }

    /**
     * Backward-compatible filtered priority endpoint.
     */
    @GetMapping("/priority")
    public ResponseEntity<List<TaskResponse>> getTasksByUserIdAndPriority(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String projectId,
            Authentication authentication,
            @RequestParam TaskPriority priority,
            @RequestParam(required = false) LocalDate dueDateFrom,
            @RequestParam(required = false) LocalDate dueDateTo) {
        String effectiveUserId = resolveUserId(authentication, userId);
        Page<TaskResponse> page = taskService.searchTasks(
            effectiveUserId,
            projectId,
            null,
            priority,
            null,
            dueDateFrom,
            dueDateTo,
            defaultListPageable()
        );
        return ResponseEntity.ok(page.getContent());
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
            @RequestParam(required = false) String projectId,
            Authentication authentication) {
        String effectiveUserId = resolveUserId(authentication, userId);
        taskService.deleteAllTasksByUserId(effectiveUserId, projectId);
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

    private Pageable sanitizePageable(Pageable pageable) {
        int safePage = Math.max(pageable.getPageNumber(), 0);
        int requestedSize = pageable.getPageSize() <= 0 ? DEFAULT_UNPAGED_FALLBACK_SIZE : pageable.getPageSize();
        int safeSize = Math.min(requestedSize, MAX_PAGE_SIZE);
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt");
        return PageRequest.of(safePage, safeSize, sort);
    }

    private Pageable defaultListPageable() {
        return PageRequest.of(0, DEFAULT_UNPAGED_FALLBACK_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}

package com.clarissa.task_management_system_backend.controller;

import com.clarissa.task_management_system_backend.dto.project.ProjectRequest;
import com.clarissa.task_management_system_backend.dto.project.ProjectResponse;
import com.clarissa.task_management_system_backend.model.ProjectStatus;
import com.clarissa.task_management_system_backend.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private static final int DEFAULT_UNPAGED_FALLBACK_SIZE = 100;
    private static final int MAX_PAGE_SIZE = 100;
    
    @Autowired
    private ProjectService projectService;
    
    /**
     * Create a new project
     */
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            Authentication auth,
            @Valid @RequestBody ProjectRequest request) {
        
        String userId = auth.getName();
        ProjectResponse response = projectService.createProject(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get paginated projects with optional filters.
     * GET /api/projects/paginated?page=0&size=10&sort=createdAt,desc&status=ACTIVE&search=design
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<ProjectResponse>> searchProjects(
            Authentication auth,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDateFrom,
            @RequestParam(required = false) LocalDate startDateTo,
            @RequestParam(required = false) LocalDate dueDateFrom,
            @RequestParam(required = false) LocalDate dueDateTo,
            Pageable pageable) {
        String userId = auth.getName();
        Page<ProjectResponse> projects = projectService.searchProjects(
            userId,
            status,
            search,
            startDateFrom,
            startDateTo,
            dueDateFrom,
            dueDateTo,
            sanitizePageable(pageable)
        );
        return ResponseEntity.ok(projects);
    }

    /**
     * Get all projects for the authenticated user with optional filters.
     */
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(
            Authentication auth,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDateFrom,
            @RequestParam(required = false) LocalDate startDateTo,
            @RequestParam(required = false) LocalDate dueDateFrom,
            @RequestParam(required = false) LocalDate dueDateTo) {
        String userId = auth.getName();
        Page<ProjectResponse> page = projectService.searchProjects(
            userId,
            status,
            search,
            startDateFrom,
            startDateTo,
            dueDateFrom,
            dueDateTo,
            defaultListPageable()
        );
        return ResponseEntity.ok(page.getContent());
    }

    /**
     * Backward-compatible status endpoint.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProjectResponse>> getProjectsByStatus(
            Authentication auth,
            @PathVariable ProjectStatus status) {
        String userId = auth.getName();
        Page<ProjectResponse> page = projectService.searchProjects(userId, status, null, null, null, null, null, defaultListPageable());
        return ResponseEntity.ok(page.getContent());
    }
    
    /**
     * Get a specific project
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            Authentication auth,
            @PathVariable String projectId) {
        
        String userId = auth.getName();
        ProjectResponse project = projectService.getProjectById(userId, projectId);
        return ResponseEntity.ok(project);
    }
    
    /**
     * Update a project
     */
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            Authentication auth,
            @PathVariable String projectId,
            @Valid @RequestBody ProjectRequest request) {
        
        String userId = auth.getName();
        ProjectResponse updated = projectService.updateProject(userId, projectId, request);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Delete a project
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            Authentication auth,
            @PathVariable String projectId) {
        
        String userId = auth.getName();
        projectService.deleteProject(userId, projectId);
        return ResponseEntity.noContent().build();
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

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
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:5173")
public class ProjectController {
    
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
     * Get all projects for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(Authentication auth) {
        String userId = auth.getName();
        List<ProjectResponse> projects = projectService.getProjectsByUserId(userId);
        return ResponseEntity.ok(projects);
    }
    
    /**
     * Get all projects by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProjectResponse>> getProjectsByStatus(
            Authentication auth,
            @PathVariable ProjectStatus status) {
        
        String userId = auth.getName();
        List<ProjectResponse> projects = projectService.getProjectsByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(projects);
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
}

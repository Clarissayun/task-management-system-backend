package com.clarissa.task_management_system_backend.service;

import com.clarissa.task_management_system_backend.model.Project;
import com.clarissa.task_management_system_backend.model.ProjectStatus;
import com.clarissa.task_management_system_backend.dto.project.ProjectRequest;
import com.clarissa.task_management_system_backend.dto.project.ProjectResponse;
import com.clarissa.task_management_system_backend.exception.ResourceNotFoundException;
import com.clarissa.task_management_system_backend.repository.ProjectRepository;
import com.clarissa.task_management_system_backend.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.regex.Pattern;

@Service
public class ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MongoTemplate mongoTemplate;
    
    /**
     * Create a new project for a user
     */
    public ProjectResponse createProject(String userId, ProjectRequest request) {
        Project project = new Project();
        project.setUserId(userId);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus() != null ? request.getStatus() : ProjectStatus.ACTIVE);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        
        Project savedProject = projectRepository.save(project);
        return convertToResponse(savedProject);
    }
    
    /**
     * Get all projects for a user
     */
    public List<ProjectResponse> getProjectsByUserId(String userId) {
        return projectRepository.findByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get projects by user ID and status
     */
    public List<ProjectResponse> getProjectsByUserIdAndStatus(String userId, ProjectStatus status) {
        return projectRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get a single project by ID
     */
    public ProjectResponse getProjectById(String userId, String projectId) {
        Project project = findUserProjectOrThrow(userId, projectId);
        return convertToResponse(project);
    }
    
    /**
     * Update a project
     */
    public ProjectResponse updateProject(String userId, String projectId, ProjectRequest request) {
        Project project = findUserProjectOrThrow(userId, projectId);
        
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        project.setUpdatedAt(LocalDateTime.now());
        
        Project updatedProject = projectRepository.save(project);
        return convertToResponse(updatedProject);
    }
    
    /**
     * Delete a project and all its tasks
     */
    public void deleteProject(String userId, String projectId) {
        Project project = findUserProjectOrThrow(userId, projectId);
        
        // Delete all tasks associated with the project
        taskRepository.deleteByProjectId(projectId);
        
        // Delete the project
        projectRepository.delete(project);
    }
    
    /**
     * Delete all projects for a user
     */
    public void deleteAllProjectsByUserId(String userId) {
        // Get all projects first to delete their tasks
        List<Project> projects = projectRepository.findByUserId(userId);
        for (Project project : projects) {
            taskRepository.deleteByProjectId(project.getId());
        }
        projectRepository.deleteByUserId(userId);
    }
    
    /**
     * Get paginated projects for a user with optional filters.
     * Supports filtering by status and search text.
     */
    public Page<ProjectResponse> searchProjects(String userId, ProjectStatus status, String search, Pageable pageable) {
        Query query = buildProjectSearchQuery(userId, status, search);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Project.class);
        List<Project> projects = mongoTemplate.find(query.with(pageable), Project.class);

        return new PageImpl<>(
                projects.stream().map(this::convertToResponse).collect(Collectors.toList()),
                pageable,
                total
        );
    }
    
    /**
     * Convert Project entity to ProjectResponse DTO
     */
    private ProjectResponse convertToResponse(Project project) {
        return new ProjectResponse(
            project.getId(),
            project.getName(),
            project.getDescription(),
            project.getStatus(),
            project.getCreatedAt(),
            project.getUpdatedAt()
        );
    }

    /**
     * Find project by ID and verify ownership
     */
    private Project findUserProjectOrThrow(String userId, String projectId) {
        return projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private Query buildProjectSearchQuery(String userId, ProjectStatus status, String search) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("userId").is(userId));

        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        if (search != null && !search.isBlank()) {
            Pattern searchPattern = Pattern.compile(Pattern.quote(search.trim()), Pattern.CASE_INSENSITIVE);
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("name").regex(searchPattern),
                    Criteria.where("description").regex(searchPattern)
            ));
        }

        return new Query().addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
    }
}

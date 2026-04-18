package com.clarissa.task_management_system_backend.repository;

import com.clarissa.task_management_system_backend.model.Project;
import com.clarissa.task_management_system_backend.model.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {
    // Paginated queries
    Page<Project> findByUserId(String userId, Pageable pageable);
    
    Page<Project> findByUserIdAndStatus(String userId, ProjectStatus status, Pageable pageable);
    
    // Non-paginated queries (for backward compatibility)
    List<Project> findByUserId(String userId);
    
    List<Project> findByUserIdAndStatus(String userId, ProjectStatus status);
    
    Optional<Project> findByIdAndUserId(String projectId, String userId);
    
    void deleteByUserId(String userId);
}

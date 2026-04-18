package com.clarissa.task_management_system_backend.repository;

import com.clarissa.task_management_system_backend.model.Task;
import com.clarissa.task_management_system_backend.model.TaskStatus;
import com.clarissa.task_management_system_backend.model.TaskPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    // Paginated queries
    Page<Task> findByUserId(String userId, Pageable pageable);
    
    Page<Task> findByUserIdAndProjectId(String userId, String projectId, Pageable pageable);
    
    Page<Task> findByUserIdAndProjectIdIsNull(String userId, Pageable pageable);
    
    Page<Task> findByUserIdAndStatus(String userId, TaskStatus status, Pageable pageable);
    
    Page<Task> findByUserIdAndProjectIdAndStatus(String userId, String projectId, TaskStatus status, Pageable pageable);
    
    Page<Task> findByUserIdAndProjectIdIsNullAndStatus(String userId, TaskStatus status, Pageable pageable);
    
    Page<Task> findByUserIdAndPriority(String userId, TaskPriority priority, Pageable pageable);
    
    Page<Task> findByUserIdAndProjectIdAndPriority(String userId, String projectId, TaskPriority priority, Pageable pageable);
    
    Page<Task> findByUserIdAndProjectIdIsNullAndPriority(String userId, TaskPriority priority, Pageable pageable);
    
    // Non-paginated queries (for backward compatibility)
    List<Task> findByUserId(String userId);

    List<Task> findByUserIdAndProjectId(String userId, String projectId);

    List<Task> findByUserIdAndProjectIdIsNull(String userId);
    
    List<Task> findByUserIdAndStatus(String userId, TaskStatus status);

    List<Task> findByUserIdAndProjectIdAndStatus(String userId, String projectId, TaskStatus status);

    List<Task> findByUserIdAndProjectIdIsNullAndStatus(String userId, TaskStatus status);
    
    List<Task> findByUserIdAndPriority(String userId, TaskPriority priority);

    List<Task> findByUserIdAndProjectIdAndPriority(String userId, String projectId, TaskPriority priority);

    List<Task> findByUserIdAndProjectIdIsNullAndPriority(String userId, TaskPriority priority);

    Optional<Task> findByIdAndUserId(String taskId, String userId);
    
    void deleteByProjectId(String projectId);

    void deleteByUserIdAndProjectId(String userId, String projectId);

    void deleteByUserIdAndProjectIdIsNull(String userId);
    
    void deleteByUserId(String userId);
}

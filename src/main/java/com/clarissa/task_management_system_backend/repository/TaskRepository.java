package com.clarissa.task_management_system_backend.repository;

import com.clarissa.task_management_system_backend.model.Task;
import com.clarissa.task_management_system_backend.model.TaskStatus;
import com.clarissa.task_management_system_backend.model.TaskPriority;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    
    List<Task> findByUserId(String userId);
    
    List<Task> findByUserIdAndStatus(String userId, TaskStatus status);
    
    List<Task> findByUserIdAndPriority(String userId, TaskPriority priority);

    Optional<Task> findByIdAndUserId(String taskId, String userId);
    
    void deleteByUserId(String userId);
}

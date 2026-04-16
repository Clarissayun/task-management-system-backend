package com.clarissa.task_management_system_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    
    @Id
    private String id;
    private String userId; // Reference to the user who owns the task
    private String projectId; // Reference to the project
    private String title;
    private String description;
    private TaskStatus status = TaskStatus.TODO; // Default status
    private TaskPriority priority = TaskPriority.LOW; // Default priority
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}

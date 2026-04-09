package com.clarissa.task_management_system_backend.dto.task;

import com.clarissa.task_management_system_backend.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    
    private String id;
    private String userId;
    private String title;
    private String description;
    private TaskStatus status;
    private String priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

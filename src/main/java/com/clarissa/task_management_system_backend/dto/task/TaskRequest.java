package com.clarissa.task_management_system_backend.dto.task;

import com.clarissa.task_management_system_backend.model.TaskPriority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    
    private String title;
    private String description;
    private TaskPriority priority;
}

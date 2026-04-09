package com.clarissa.task_management_system_backend.dto.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    
    private String title;
    private String description;
    private String priority;
}

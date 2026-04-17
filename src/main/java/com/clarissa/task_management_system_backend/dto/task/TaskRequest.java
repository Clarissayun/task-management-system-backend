package com.clarissa.task_management_system_backend.dto.task;

import com.clarissa.task_management_system_backend.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    @NotBlank(message = "Task title is required")
    @Size(max = 120, message = "Task title must be at most 120 characters")
    private String title;

    @Size(max = 2000, message = "Task description must be at most 2000 characters")
    private String description;

    private TaskPriority priority;
}

package com.clarissa.task_management_system_backend.dto.project;

import com.clarissa.task_management_system_backend.model.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {
    
    @NotBlank(message = "Project name is required")
    private String name;
    
    private String description;
    
    private ProjectStatus status;

    @NotNull(message = "Project start date is required")
    private LocalDate startDate;

    @NotNull(message = "Project due date is required")
    private LocalDate dueDate;
}

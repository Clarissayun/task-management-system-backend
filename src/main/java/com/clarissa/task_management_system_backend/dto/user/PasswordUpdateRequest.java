package com.clarissa.task_management_system_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordUpdateRequest {
    
    private String oldPassword;
    private String newPassword;
}

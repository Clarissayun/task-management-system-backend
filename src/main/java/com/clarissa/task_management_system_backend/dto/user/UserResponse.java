package com.clarissa.task_management_system_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private String id;
    private String username;
    private String email;
    private String avatar; // Base64 encoded avatar image
    private LocalDateTime createdAt;
}

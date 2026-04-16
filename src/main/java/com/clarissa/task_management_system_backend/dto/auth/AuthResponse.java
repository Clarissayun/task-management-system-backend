package com.clarissa.task_management_system_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String message;
    private String userId;
    private String username;
    private String email;
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn;
    private Long refreshTokenExpiresIn;

    public AuthResponse(String message, String userId, String username, String email) {
        this.message = message;
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}

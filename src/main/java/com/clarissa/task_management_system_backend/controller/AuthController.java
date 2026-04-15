package com.clarissa.task_management_system_backend.controller;

import com.clarissa.task_management_system_backend.dto.auth.RegisterRequest;
import com.clarissa.task_management_system_backend.dto.auth.LoginRequest;
import com.clarissa.task_management_system_backend.dto.auth.AuthResponse;
import com.clarissa.task_management_system_backend.dto.user.UserResponse;
import com.clarissa.task_management_system_backend.dto.user.UserUpdateRequest;
import com.clarissa.task_management_system_backend.dto.user.PasswordUpdateRequest;
import com.clarissa.task_management_system_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Register a new user
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Login user
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user profile by ID
     * GET /api/auth/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String userId) {
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user profile by username
     * GET /api/auth/user/username/{username}
     */
    @GetMapping("/user/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        UserResponse response = userService.getUserByUsername(username);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update user information
     * PUT /api/auth/user/{userId}
     * Only updates provided fields (username and/or email)
     */
    @PutMapping("/user/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update user password
     * POST /api/auth/update-password/{userId}
     * Requires old password verification
     */
    @PostMapping("/update-password/{userId}")
    public ResponseEntity<AuthResponse> updatePassword(
            @PathVariable String userId,
            @Valid @RequestBody PasswordUpdateRequest request) {
        AuthResponse response = userService.updatePassword(userId, request);
        return ResponseEntity.ok(response);
    }
}

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
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Login user
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
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
            @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Change user password
     * POST /api/auth/change-password/{userId}
     * Requires old password verification
     */
    @PostMapping("/change-password/{userId}")
    public ResponseEntity<AuthResponse> changePassword(
            @PathVariable String userId,
            @RequestBody PasswordUpdateRequest request) {
        AuthResponse response = userService.changePassword(userId, request);
        return ResponseEntity.ok(response);
    }
}

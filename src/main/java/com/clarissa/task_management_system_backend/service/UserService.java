package com.clarissa.task_management_system_backend.service;

import com.clarissa.task_management_system_backend.model.User;
import com.clarissa.task_management_system_backend.dto.auth.RegisterRequest;
import com.clarissa.task_management_system_backend.dto.auth.LoginRequest;
import com.clarissa.task_management_system_backend.dto.auth.AuthResponse;
import com.clarissa.task_management_system_backend.dto.user.UserResponse;
import com.clarissa.task_management_system_backend.dto.user.UserUpdateRequest;
import com.clarissa.task_management_system_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Register a new user
     */
    public AuthResponse register(RegisterRequest request) {
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // TODO: Hash password with BCrypt
        user.setCreatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        return new AuthResponse(
            "User registered successfully",
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail()
        );
    }
    
    /**
     * Login user with username and password
     */
    public AuthResponse login(LoginRequest request) {
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if password matches (plaintext for now, TODO: use BCrypt later)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        return new AuthResponse(
            "Login successful",
            user.getId(),
            user.getUsername(),
            user.getEmail()
        );
    }
    
    /**
     * Get user by ID
     */
    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return convertToResponse(user);
    }
    
    /**
     * Get user by username
     */
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return convertToResponse(user);
    }
    
    /**
     * Update user information
     */
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if new username already exists (and not same as current user)
        if (!user.getUsername().equals(request.getUsername()) && 
            userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        
        // Check if new email already exists (and not same as current user)
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        
        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }
    
    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse convertToResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getCreatedAt()
        );
    }
}

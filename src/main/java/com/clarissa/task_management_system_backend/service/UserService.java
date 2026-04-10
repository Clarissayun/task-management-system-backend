package com.clarissa.task_management_system_backend.service;

import com.clarissa.task_management_system_backend.model.User;
import com.clarissa.task_management_system_backend.dto.auth.RegisterRequest;
import com.clarissa.task_management_system_backend.dto.auth.LoginRequest;
import com.clarissa.task_management_system_backend.dto.auth.AuthResponse;
import com.clarissa.task_management_system_backend.dto.user.UserResponse;
import com.clarissa.task_management_system_backend.dto.user.UserUpdateRequest;
import com.clarissa.task_management_system_backend.dto.user.PasswordUpdateRequest;
import com.clarissa.task_management_system_backend.exception.ResourceNotFoundException;
import com.clarissa.task_management_system_backend.exception.BadRequestException;
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
            throw new BadRequestException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if password matches (plaintext for now, TODO: use BCrypt comparison later)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new BadRequestException("Invalid password");
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return convertToResponse(user);
    }
    
    /**
     * Get user by username
     */
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return convertToResponse(user);
    }
    
    /**
     * Update user information (partial update allowed)
     * Only updates fields that are provided (not null)
     */
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Update username only if provided
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            if (!user.getUsername().equals(request.getUsername()) && 
                userRepository.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username already taken");
            }
            user.setUsername(request.getUsername());
        }
        
        // Update email only if provided
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!user.getEmail().equals(request.getEmail()) && 
                userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }
        
        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }
    
    /**
     * Update user password
     * Must provide old password for verification
     */
    public AuthResponse updatePassword(String userId, PasswordUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify old password (plaintext for now, TODO: use BCrypt comparison later)
        if (!user.getPassword().equals(request.getOldPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }
        
        // Check if new password is different from old
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("New password must be different from old password");
        }
        
        user.setPassword(request.getNewPassword());
        userRepository.save(user);
        
        return new AuthResponse(
            "Password updated successfully",
            user.getId(),
            user.getUsername(),
            user.getEmail()
        );
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

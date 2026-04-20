package com.clarissa.task_management_system_backend.service;

import com.clarissa.task_management_system_backend.model.User;
import com.clarissa.task_management_system_backend.dto.auth.RegisterRequest;
import com.clarissa.task_management_system_backend.dto.auth.LoginRequest;
import com.clarissa.task_management_system_backend.dto.auth.AuthResponse;
import com.clarissa.task_management_system_backend.config.JwtTokenProvider;
import com.clarissa.task_management_system_backend.dto.user.UserResponse;
import com.clarissa.task_management_system_backend.dto.user.UserUpdateRequest;
import com.clarissa.task_management_system_backend.dto.user.PasswordUpdateRequest;
import com.clarissa.task_management_system_backend.exception.ResourceNotFoundException;
import com.clarissa.task_management_system_backend.exception.BadRequestException;
import com.clarissa.task_management_system_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PendingRegistrationService pendingRegistrationService;
    
    /**
     * Register a new user
     */
    public AuthResponse register(RegisterRequest request) {
        String normalizedUsername = normalizeUsername(request.getUsername());
        String normalizedEmail = normalizeEmail(request.getEmail());

        validateRegistrationRequest(request);
        
        // Create new user
        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        return buildAuthResponseWithTokens("User registered successfully", savedUser);
    }

    public AuthResponse registerWithEncodedPassword(String username, String email, String encodedPassword) {
        String normalizedUsername = normalizeUsername(username);
        String normalizedEmail = normalizeEmail(email);

        validateRegistrationAvailability(normalizedUsername, normalizedEmail);

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPassword(encodedPassword);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return buildAuthResponseWithTokens("User registered successfully", savedUser);
    }

    public void validateRegistrationAvailability(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }

        pendingRegistrationService.validateNoConflictingReservation(username, email);
    }

    public void validateRegistrationRequest(RegisterRequest request) {
        String normalizedUsername = normalizeUsername(request.getUsername());
        String normalizedEmail = normalizeEmail(request.getEmail());

        validatePasswordPolicy(request.getPassword(), normalizedUsername, normalizedEmail);

        validateRegistrationAvailability(normalizedUsername, normalizedEmail);
    }
    
    /**
     * Login user with username and password
     */
    public AuthResponse login(LoginRequest request) {
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!isPasswordMatch(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid password");
        }

        migratePasswordIfNeeded(user, request.getPassword());
        
        return buildAuthResponseWithTokens("Login successful", user);
    }

    /**
     * Refresh access token using refresh token.
     */
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        String userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return buildAuthResponseWithTokens("Token refreshed successfully", user);
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
            String normalizedUsername = normalizeUsername(request.getUsername());

            if (!user.getUsername().equals(normalizedUsername) && 
                userRepository.existsByUsername(normalizedUsername)) {
                throw new BadRequestException("Username already taken");
            }
            user.setUsername(normalizedUsername);
        }
        
        // Update email only if provided
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            String normalizedEmail = normalizeEmail(request.getEmail());

            if (!user.getEmail().equals(normalizedEmail) && 
                userRepository.existsByEmail(normalizedEmail)) {
                throw new BadRequestException("Email already in use");
            }
            user.setEmail(normalizedEmail);
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
        
        if (!isPasswordMatch(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }
        
        // Check if new password is different from old
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("New password must be different from old password");
        }

        validatePasswordPolicy(request.getNewPassword(), user.getUsername(), user.getEmail());
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
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

    private boolean isPasswordMatch(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        if (isBcryptHash(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        return storedPassword.equals(rawPassword);
    }

    private boolean isBcryptHash(String password) {
        return password != null && password.matches("^\\$2[aby]?\\$\\d{2}\\$.*");
    }

    private void migratePasswordIfNeeded(User user, String rawPassword) {
        if (user.getPassword() != null && !isBcryptHash(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
        }
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private void validatePasswordPolicy(String password, String username, String email) {
        if (password == null) {
            throw new BadRequestException("Password is required");
        }

        if (password.length() < 12) {
            throw new BadRequestException("Password must be at least 12 characters long");
        }

        if (password.length() > 128) {
            throw new BadRequestException("Password must not exceed 128 characters");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new BadRequestException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new BadRequestException("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new BadRequestException("Password must contain at least one number");
        }

        if (!password.matches(".*[^A-Za-z0-9].*")) {
            throw new BadRequestException("Password must contain at least one special character");
        }

        String loweredPassword = password.toLowerCase(Locale.ROOT);

        if (username != null && !username.isBlank() && loweredPassword.contains(username.trim().toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("Password must not contain the username");
        }

        if (email != null && !email.isBlank()) {
            String loweredEmail = email.trim().toLowerCase(Locale.ROOT);
            String emailLocalPart = loweredEmail.contains("@")
                    ? loweredEmail.substring(0, loweredEmail.indexOf('@'))
                    : loweredEmail;

            if (loweredPassword.contains(loweredEmail) || loweredPassword.contains(emailLocalPart)) {
                throw new BadRequestException("Password must not contain the email address");
            }
        }

        Set<String> commonPasswords = Set.of(
                "password",
                "password123",
                "123456",
                "12345678",
                "qwerty",
                "qwerty123",
                "admin",
                "letmein",
                "welcome",
                "abc123"
        );

        if (commonPasswords.contains(loweredPassword)) {
            throw new BadRequestException("Password is too common. Choose a stronger password");
        }
    }

    private AuthResponse buildAuthResponseWithTokens(String message, User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return new AuthResponse(
                message,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                "Bearer",
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpirationMs(),
                jwtTokenProvider.getRefreshTokenExpirationMs()
        );
    }
}

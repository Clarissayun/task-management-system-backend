package com.clarissa.task_management_system_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    private String id;

    @Indexed(unique = true) // Ensure username is unique
    private String username;
    
    @Indexed(unique = true) // Ensure email is unique
    private String email;
    
    private String password;
    private String avatar; // Base64 encoded avatar image
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}

package com.clarissa.task_management_system_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "otp_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpToken {

    @Id
    private String id;

    @Indexed
    private String email;

    @Indexed
    private OtpPurpose purpose;

    private String username;
    private String passwordHash;

    private String otpHash;
    private int attempts;
    private boolean used;
    private LocalDateTime createdAt;
    @Indexed(name = "otp_expires_at_ttl_idx", expireAfterSeconds = 0)
    private LocalDateTime expiresAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime updatedAt;
}

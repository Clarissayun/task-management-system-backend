package com.clarissa.task_management_system_backend.repository;

import com.clarissa.task_management_system_backend.model.OtpToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends MongoRepository<OtpToken, String> {

    Optional<OtpToken> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    List<OtpToken> findByEmailAndUsedFalse(String email);

    List<OtpToken> findByEmailAndExpiresAtBefore(String email, LocalDateTime expiresAt);
}

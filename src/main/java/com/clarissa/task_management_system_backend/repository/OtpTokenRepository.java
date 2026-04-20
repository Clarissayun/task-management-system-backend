package com.clarissa.task_management_system_backend.repository;

import com.clarissa.task_management_system_backend.model.OtpToken;
import com.clarissa.task_management_system_backend.model.OtpPurpose;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends MongoRepository<OtpToken, String> {

    Optional<OtpToken> findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(String email, OtpPurpose purpose);

    List<OtpToken> findByEmailAndPurposeAndUsedFalse(String email, OtpPurpose purpose);
}

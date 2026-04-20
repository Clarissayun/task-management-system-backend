package com.clarissa.task_management_system_backend.repository;

import com.clarissa.task_management_system_backend.model.PendingRegistration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PendingRegistrationRepository extends MongoRepository<PendingRegistration, String> {

    Optional<PendingRegistration> findByEmailAndExpiresAtAfter(String email, LocalDateTime now);

    Optional<PendingRegistration> findByUsernameAndExpiresAtAfter(String username, LocalDateTime now);

    Optional<PendingRegistration> findByEmailAndUsernameAndExpiresAtAfter(String email, String username, LocalDateTime now);

    void deleteByEmail(String email);
}

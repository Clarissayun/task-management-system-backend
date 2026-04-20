package com.clarissa.task_management_system_backend.service;

import com.clarissa.task_management_system_backend.exception.BadRequestException;
import com.clarissa.task_management_system_backend.model.PendingRegistration;
import com.clarissa.task_management_system_backend.repository.PendingRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PendingRegistrationService {

    private final PendingRegistrationRepository pendingRegistrationRepository;

    public void reserve(String username, String email, LocalDateTime expiresAt) {
        LocalDateTime now = LocalDateTime.now();

        var existingForEmail = pendingRegistrationRepository.findByEmailAndExpiresAtAfter(email, now);
        if (existingForEmail.isPresent()) {
            PendingRegistration reservation = existingForEmail.get();
            if (!username.equals(reservation.getUsername())) {
                throw new BadRequestException("Email already has a pending registration verification");
            }

            reservation.setExpiresAt(expiresAt);
            reservation.setUpdatedAt(now);
            pendingRegistrationRepository.save(reservation);
            return;
        }

        var existingForUsername = pendingRegistrationRepository.findByUsernameAndExpiresAtAfter(username, now);
        if (existingForUsername.isPresent() && !email.equals(existingForUsername.get().getEmail())) {
            throw new BadRequestException("Username already has a pending registration verification");
        }

        PendingRegistration pendingRegistration = new PendingRegistration();
        pendingRegistration.setEmail(email);
        pendingRegistration.setUsername(username);
        pendingRegistration.setCreatedAt(now);
        pendingRegistration.setExpiresAt(expiresAt);
        pendingRegistration.setUpdatedAt(now);

        try {
            pendingRegistrationRepository.save(pendingRegistration);
        } catch (DuplicateKeyException ex) {
            throw new BadRequestException("Email or username is currently reserved for pending registration");
        }
    }

    public void ensureActiveReservation(String username, String email) {
        boolean exists = pendingRegistrationRepository
                .findByEmailAndUsernameAndExpiresAtAfter(email, username, LocalDateTime.now())
                .isPresent();

        if (!exists) {
            throw new BadRequestException("Registration session expired. Please request a new OTP");
        }
    }

    public void validateNoConflictingReservation(String username, String email) {
        LocalDateTime now = LocalDateTime.now();

        pendingRegistrationRepository.findByEmailAndExpiresAtAfter(email, now).ifPresent(reservation -> {
            if (!username.equals(reservation.getUsername())) {
                throw new BadRequestException("Email already has a pending registration verification");
            }
        });

        pendingRegistrationRepository.findByUsernameAndExpiresAtAfter(username, now).ifPresent(reservation -> {
            if (!email.equals(reservation.getEmail())) {
                throw new BadRequestException("Username already has a pending registration verification");
            }
        });
    }

    public void releaseByEmail(String email) {
        pendingRegistrationRepository.deleteByEmail(email);
    }
}

package com.clarissa.task_management_system_backend.service;

import com.clarissa.task_management_system_backend.config.JwtTokenProvider;
import com.clarissa.task_management_system_backend.dto.auth.AuthResponse;
import com.clarissa.task_management_system_backend.dto.auth.OtpRequest;
import com.clarissa.task_management_system_backend.dto.auth.OtpVerifyRequest;
import com.clarissa.task_management_system_backend.dto.auth.RegisterRequest;
import com.clarissa.task_management_system_backend.exception.BadRequestException;
import com.clarissa.task_management_system_backend.exception.ResourceNotFoundException;
import com.clarissa.task_management_system_backend.model.OtpToken;
import com.clarissa.task_management_system_backend.model.OtpPurpose;
import com.clarissa.task_management_system_backend.model.User;
import com.clarissa.task_management_system_backend.repository.OtpTokenRepository;
import com.clarissa.task_management_system_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    private final OtpTokenRepository otpTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    private final SecureRandom secureRandom = new SecureRandom();

    public String requestOtp(OtpRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        invalidateActiveOtps(normalizedEmail, OtpPurpose.LOGIN, now);

        String otp = generateOtp();
        OtpToken otpToken = new OtpToken();
        otpToken.setEmail(normalizedEmail);
        otpToken.setPurpose(OtpPurpose.LOGIN);
        otpToken.setOtpHash(passwordEncoder.encode(otp));
        otpToken.setAttempts(0);
        otpToken.setUsed(false);
        otpToken.setCreatedAt(now);
        otpToken.setExpiresAt(now.plusMinutes(OTP_EXPIRY_MINUTES));
        otpToken.setUpdatedAt(now);

        otpTokenRepository.save(otpToken);
        emailService.sendOtpEmail(user.getEmail(), otp);

        return "OTP sent successfully to " + normalizedEmail + ". It will expire in " + OTP_EXPIRY_MINUTES + " minutes.";
    }

    public AuthResponse verifyOtp(OtpVerifyRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        OtpToken otpToken = otpTokenRepository.findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(normalizedEmail, OtpPurpose.LOGIN)
                .orElseThrow(() -> new BadRequestException("OTP is invalid or expired"));

        LocalDateTime now = LocalDateTime.now();
        if (otpToken.getExpiresAt() == null || otpToken.getExpiresAt().isBefore(now)) {
            otpToken.setUsed(true);
            otpToken.setUpdatedAt(now);
            otpTokenRepository.save(otpToken);
            throw new BadRequestException("OTP is invalid or expired");
        }

        if (otpToken.getAttempts() >= MAX_ATTEMPTS) {
            otpToken.setUsed(true);
            otpToken.setUpdatedAt(now);
            otpTokenRepository.save(otpToken);
            throw new BadRequestException("Too many invalid OTP attempts. Please request a new code.");
        }

        if (!passwordEncoder.matches(request.getOtp(), otpToken.getOtpHash())) {
            otpToken.setAttempts(otpToken.getAttempts() + 1);
            otpToken.setUpdatedAt(now);
            if (otpToken.getAttempts() >= MAX_ATTEMPTS) {
                otpToken.setUsed(true);
            }
            otpTokenRepository.save(otpToken);
            throw new BadRequestException("Invalid OTP");
        }

        otpToken.setUsed(true);
        otpToken.setVerifiedAt(now);
        otpToken.setUpdatedAt(now);
        otpTokenRepository.save(otpToken);

        return buildAuthResponseWithTokens("OTP verified successfully", user);
    }

    public String requestRegistrationOtp(RegisterRequest request) {
        userService.validateRegistrationRequest(request);

        String normalizedEmail = normalizeEmail(request.getEmail());
        LocalDateTime now = LocalDateTime.now();
        invalidateActiveOtps(normalizedEmail, OtpPurpose.REGISTRATION, now);

        String otp = generateOtp();
        OtpToken otpToken = new OtpToken();
        otpToken.setEmail(normalizedEmail);
        otpToken.setPurpose(OtpPurpose.REGISTRATION);
        otpToken.setUsername(normalizeUsername(request.getUsername()));
        otpToken.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        otpToken.setOtpHash(passwordEncoder.encode(otp));
        otpToken.setAttempts(0);
        otpToken.setUsed(false);
        otpToken.setCreatedAt(now);
        otpToken.setExpiresAt(now.plusMinutes(OTP_EXPIRY_MINUTES));
        otpToken.setUpdatedAt(now);

        otpTokenRepository.save(otpToken);
        emailService.sendOtpEmail(normalizedEmail, otp);

        return "OTP sent successfully to " + normalizedEmail + ". It will expire in " + OTP_EXPIRY_MINUTES + " minutes.";
    }

    public AuthResponse verifyRegistrationOtp(OtpVerifyRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        OtpToken otpToken = otpTokenRepository.findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(normalizedEmail, OtpPurpose.REGISTRATION)
                .orElseThrow(() -> new BadRequestException("OTP is invalid or expired"));

        LocalDateTime now = LocalDateTime.now();
        if (otpToken.getExpiresAt() == null || otpToken.getExpiresAt().isBefore(now)) {
            otpToken.setUsed(true);
            otpToken.setUpdatedAt(now);
            otpTokenRepository.save(otpToken);
            throw new BadRequestException("OTP is invalid or expired");
        }

        if (otpToken.getAttempts() >= MAX_ATTEMPTS) {
            otpToken.setUsed(true);
            otpToken.setUpdatedAt(now);
            otpTokenRepository.save(otpToken);
            throw new BadRequestException("Too many invalid OTP attempts. Please request a new code.");
        }

        if (!passwordEncoder.matches(request.getOtp(), otpToken.getOtpHash())) {
            otpToken.setAttempts(otpToken.getAttempts() + 1);
            otpToken.setUpdatedAt(now);
            if (otpToken.getAttempts() >= MAX_ATTEMPTS) {
                otpToken.setUsed(true);
            }
            otpTokenRepository.save(otpToken);
            throw new BadRequestException("Invalid OTP");
        }

        AuthResponse response = userService.registerWithEncodedPassword(
                otpToken.getUsername(),
                otpToken.getEmail(),
                otpToken.getPasswordHash()
        );

        otpToken.setUsed(true);
        otpToken.setVerifiedAt(now);
        otpToken.setUpdatedAt(now);
        otpTokenRepository.save(otpToken);

        return response;
    }

    private void invalidateActiveOtps(String email, OtpPurpose purpose, LocalDateTime now) {
        var activeOtps = otpTokenRepository.findByEmailAndPurposeAndUsedFalse(email, purpose);
        activeOtps.forEach(token -> {
            token.setUsed(true);
            token.setUpdatedAt(now);
        });
        otpTokenRepository.saveAll(activeOtps);
    }

    private String generateOtp() {
        int code = secureRandom.nextInt(1_000_000);
        return String.format(Locale.ROOT, "%06d", code);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new BadRequestException("Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim();
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

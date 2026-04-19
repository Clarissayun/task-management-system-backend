package com.clarissa.task_management_system_backend.controller;

import com.clarissa.task_management_system_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dev/email")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;

    @PostMapping("/test")
    public ResponseEntity<String> sendTestEmail(@RequestParam String email) {
        try {
            emailService.sendSimpleEmail(
                email,
                "Test Email from Task Management System",
                "This is a test email sent from Mailtrap. If you received this, everything is working correctly!"
            );
            return ResponseEntity.ok("Email sent successfully to " + email);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send email: " + e.getMessage());
        }
    }

    @PostMapping("/otp-test")
    public ResponseEntity<String> sendOtpTestEmail(@RequestParam String email) {
        try {
            String testOtp = "123456";
            emailService.sendOtpEmail(email, testOtp);
            return ResponseEntity.ok("OTP email sent successfully to " + email + " (OTP: " + testOtp + ")");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send OTP email: " + e.getMessage());
        }
    }

    @PostMapping("/welcome-test")
    public ResponseEntity<String> sendWelcomeTestEmail(@RequestParam String email, @RequestParam String name) {
        try {
            emailService.sendWelcomeEmail(email, name);
            return ResponseEntity.ok("Welcome email sent successfully to " + email);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send welcome email: " + e.getMessage());
        }
    }
}

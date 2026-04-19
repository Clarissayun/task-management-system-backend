package com.clarissa.task_management_system_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    /**
     * Send a simple text email
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send OTP email
     */
    public void sendOtpEmail(String to, String otp) {
        String subject = "Your OTP Code";
        String text = "Your OTP code is: " + otp + "\n\nThis code will expire in 10 minutes.\n\nIf you didn't request this, please ignore this email.";
        sendSimpleEmail(to, subject, text);
    }

    /**
     * Send welcome email
     */
    public void sendWelcomeEmail(String to, String userName) {
        String subject = "Welcome to Task Management System";
        String text = "Hello " + userName + ",\n\nWelcome to our Task Management System! We're excited to have you on board.\n\nIf you have any questions, feel free to reach out.\n\nBest regards,\nTask Management Team";
        sendSimpleEmail(to, subject, text);
    }
}

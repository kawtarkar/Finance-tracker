package com.financetracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${application.frontend-url}")
    private String frontendUrl;

    @Async
    public void sendVerificationEmail(String toEmail, String firstName, String token) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("verificationUrl",
                    frontendUrl + "/api/v1/auth/verify-email?token=" + token);

            String html = templateEngine.process("email/verification", context);
            sendHtmlEmail(toEmail, "Verify your email address", html);
            log.info("Verification email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String firstName, String token) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("resetUrl",
                    frontendUrl + "/reset-password?token=" + token);

            String html = templateEngine.process("email/password-reset", context);
            sendHtmlEmail(toEmail, "Reset your password", html);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }
}

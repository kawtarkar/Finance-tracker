package com.financetracker.controller;

import com.financetracker.dto.request.*;
import com.financetracker.dto.response.ApiResponse;
import com.financetracker.dto.response.AuthResponse;
import com.financetracker.dto.response.UserResponse;
import com.financetracker.model.User;
import com.financetracker.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── Public endpoints ──────────────────────────────────────────────────────

    @PostMapping("/register")
public ResponseEntity<ApiResponse> register(
        @Valid @RequestBody RegisterRequest request) {

    System.out.println("REGISTER ENDPOINT HIT");

    return ResponseEntity.ok(authService.register(request));
}

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse> resendVerification(@RequestParam String email) {
        return ResponseEntity.ok(authService.resendVerification(email));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    // ── Protected endpoints ───────────────────────────────────────────────────

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(authService.changePassword(request, currentUser));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(authService.logout(currentUser));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(UserResponse.builder()
                .id(currentUser.getId())
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .email(currentUser.getEmail())
                .role(currentUser.getRole())
                .enabled(currentUser.isEnabled())
                .build());
    }
}


package com.financetracker.service;

import com.financetracker.dto.request.*;
import com.financetracker.dto.response.ApiResponse;
import com.financetracker.dto.response.AuthResponse;
import com.financetracker.dto.response.UserResponse;
import com.financetracker.model.Token;
import com.financetracker.model.TokenType;
import com.financetracker.model.User;
import com.financetracker.exception.*;
import com.financetracker.repository.TokenRepository;
import com.financetracker.repository.UserRepository;
import com.financetracker.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtUtil               jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenService          tokenService;
    private final EmailService          emailService;
    private final TokenRepository       tokenRepository;

    @Transactional
    public ApiResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .build();
        userRepository.save(user);
        String verificationToken = tokenService.createEmailVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationToken);
        return ApiResponse.ok("Registration successful. Please check your email to verify your account.");
    }

    @Transactional
    public ApiResponse verifyEmail(String tokenValue) {
        Token token = tokenService.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));
        if (token.getTokenType() != TokenType.EMAIL_VERIFICATION)
            throw new InvalidTokenException("Invalid token type");
        if (token.isExpired())
            throw new TokenExpiredException("Verification token has expired. Please request a new one.");
        if (token.isConfirmed())
            return ApiResponse.ok("Email already verified. You can log in.");

        User user = token.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        tokenService.confirmToken(token);
        return ApiResponse.ok("Email verified successfully. You can now log in.");
    }

    @Transactional
    public ApiResponse resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user.isEnabled()) return ApiResponse.ok("Your email is already verified.");
        String token = tokenService.createEmailVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), token);
        return ApiResponse.ok("Verification email resent. Please check your inbox.");
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (DisabledException e) {
            throw new AccountNotVerifiedException("Please verify your email before logging in.");
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String accessToken  = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        tokenService.createRefreshToken(user, refreshToken);
        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        Token storedToken = tokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
        if (storedToken.getTokenType() != TokenType.REFRESH)
            throw new InvalidTokenException("Invalid token type");
        if (storedToken.isExpired()) {
            tokenService.deleteToken(storedToken);
            throw new TokenExpiredException("Refresh token expired. Please log in again.");
        }
        User user = storedToken.getUser();
        if (!jwtUtil.isTokenValid(request.getRefreshToken(), user))
            throw new InvalidTokenException("Refresh token is no longer valid");

        String newAccessToken  = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);
        tokenService.deleteToken(storedToken);
        tokenService.createRefreshToken(user, newRefreshToken);
        return buildAuthResponse(newAccessToken, newRefreshToken, user);
    }

    @Transactional
    public ApiResponse changePassword(ChangePasswordRequest request, User currentUser) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword()))
            throw new BadCredentialsException("Current password is incorrect");
        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new PasswordMismatchException("New password and confirm password do not match");

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
        return ApiResponse.ok("Password changed successfully.");
    }

    @Transactional
    public ApiResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = tokenService.createPasswordResetToken(user);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), token);
        });
        return ApiResponse.ok("If an account exists with that email, a reset link has been sent.");
    }

    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new PasswordMismatchException("Passwords do not match");

        Token token = tokenService.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));
        if (token.getTokenType() != TokenType.PASSWORD_RESET)
            throw new InvalidTokenException("Invalid token type");
        if (token.isExpired())
            throw new TokenExpiredException("Reset token has expired. Please request a new one.");

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        tokenService.deleteToken(token);
        return ApiResponse.ok("Password reset successfully. You can now log in.");
    }

    @Transactional
    public ApiResponse logout(User currentUser) {
        tokenRepository.deleteByUserAndTokenType(currentUser, TokenType.REFRESH);
        return ApiResponse.ok("Logged out successfully.");
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getJwtExpiration())
                .user(toUserResponse(user))
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .build();
    }
}


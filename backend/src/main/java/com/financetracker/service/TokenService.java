package com.financetracker.service;


import com.financetracker.model.Token;
import com.financetracker.model.TokenType;
import com.financetracker.model.User;
import com.financetracker.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    private static final int EMAIL_TOKEN_EXPIRY_HOURS  = 24;
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 30;

    @Transactional
    public String createEmailVerificationToken(User user) {
        // Invalidate old tokens first
        tokenRepository.deleteByUserAndTokenType(user, TokenType.EMAIL_VERIFICATION);

        Token token = Token.builder()
                .token(UUID.randomUUID().toString())
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().plusHours(EMAIL_TOKEN_EXPIRY_HOURS))
                .user(user)
                .build();

        return tokenRepository.save(token).getToken();
    }

    @Transactional
    public String createPasswordResetToken(User user) {
        tokenRepository.deleteByUserAndTokenType(user, TokenType.PASSWORD_RESET);

        Token token = Token.builder()
                .token(UUID.randomUUID().toString())
                .tokenType(TokenType.PASSWORD_RESET)
                .expiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES))
                .user(user)
                .build();

        return tokenRepository.save(token).getToken();
    }

    @Transactional
    public String createRefreshToken(User user, String jwtRefreshToken) {
        tokenRepository.deleteByUserAndTokenType(user, TokenType.REFRESH);

        Token token = Token.builder()
                .token(jwtRefreshToken)
                .tokenType(TokenType.REFRESH)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .user(user)
                .build();

        return tokenRepository.save(token).getToken();
    }

    public Optional<Token> findByToken(String token) {
        return tokenRepository.findByToken(token);
    }

    @Transactional
    public void confirmToken(Token token) {
        token.setConfirmedAt(LocalDateTime.now());
        tokenRepository.save(token);
    }

    @Transactional
    public void deleteToken(Token token) {
        tokenRepository.delete(token);
    }
}


package com.financetracker.repository;

import com.financetracker.model.Token;
import com.financetracker.model.TokenType;
import com.financetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;


public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByToken(String token);

    List<Token> findByUserAndTokenType(User user, TokenType tokenType);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.user = :user AND t.tokenType = :tokenType")
    void deleteByUserAndTokenType(User user, TokenType tokenType);
}

    


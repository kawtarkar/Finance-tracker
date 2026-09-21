package com.financetracker.exception;   
public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) { super(message); }
}

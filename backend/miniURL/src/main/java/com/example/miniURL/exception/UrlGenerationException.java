package com.example.miniURL.exception;

public class UrlGenerationException extends RuntimeException {
    public UrlGenerationException(String message) {
        super(message);
    }
    
    public UrlGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

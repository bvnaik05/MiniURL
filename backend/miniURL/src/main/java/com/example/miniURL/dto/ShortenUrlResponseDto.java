package com.example.miniURL.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Response DTO for URL shortening API
 * 
 * Returns both:
 * 1. shortCode: Just the code (e.g., "abc12345")
 * 2. shortUrl: Full URL for easy copying (e.g., "http://localhost:8080/abc12345")
 */
@Getter
@Setter
@Builder
public class ShortenUrlResponseDto {
    private String shortCode;   // e.g., "abc12345"
    private String shortUrl;    // e.g., "http://localhost:8080/abc12345"
}

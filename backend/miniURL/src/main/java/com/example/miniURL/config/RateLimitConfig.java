package com.example.miniURL.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiter using Token Bucket algorithm
 * 
 * How it works:
 * - Each user (IP) gets a "bucket" with tokens
 * - Each request consumes 1 token
 * - Tokens refill at a fixed rate
 * - If bucket is empty, request is blocked (429 Too Many Requests)
 * 
 * Benefits:
 * - Prevents abuse/DDOS
 * - Allows burst traffic (token bucket allows temporary spikes)
 * - Fair usage across all users
 */
@Component
public class RateLimitConfig {
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Rate limit for URL shortening: 20 requests per minute
     * 
     * Why 20/min?
     * - Generous for normal users (1 every 3 seconds)
     * - Blocks malicious automation
     * - Protects database write operations
     */
    public Bucket resolveShorteningBucket(String key) {
        return cache.computeIfAbsent(key, k -> createShorteningBucket());
    }

    /**
     * Rate limit for redirects: 100 requests per minute
     * 
     * Why 100/min?
     * - More lenient (read operations are cheaper)
     * - Allows normal browsing patterns
     * - Cached responses don't hit DB anyway
     */
    public Bucket resolveRedirectBucket(String key) {
        return cache.computeIfAbsent(key, k -> createRedirectBucket());
    }

    private Bucket createShorteningBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(20)
                .refillIntervally(20, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createRedirectBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(100)
                .refillIntervally(100, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}

package com.example.miniURL.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configures Caffeine cache for URL redirections
     * 
     * Performance Impact:
     * - Cache Hit: ~0.1ms (in-memory lookup)
     * - Cache Miss: ~50-100ms (database query)
     * - Expected hit rate: 80-90% for popular URLs
     * 
     * Configuration:
     * - Maximum 10,000 entries (uses ~10MB RAM)
     * - 1 hour expiration (balances freshness vs performance)
     * - LRU eviction (Least Recently Used)
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("urlCache");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(10_000)  // Store up to 10,000 most popular URLs
                .expireAfterWrite(1, TimeUnit.HOURS)  // Expire after 1 hour
                .recordStats();  // Enable metrics for monitoring
    }
}

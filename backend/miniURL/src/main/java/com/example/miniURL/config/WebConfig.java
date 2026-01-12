package com.example.miniURL.config;

import com.example.miniURL.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration to register interceptors and CORS settings
 * 
 * CORS (Cross-Origin Resource Sharing):
 * - Allows frontend (different domain/port) to call backend APIs
 * - Without CORS, browser blocks cross-origin requests (security feature)
 * - Required for: React apps, deployed frontends, any separate UI
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    /**
     * Configure CORS to allow frontend communication
     * 
     * Allows:
     * - localhost:3000 (React dev server)
     * - localhost:5173 (Vite dev server)
     * - Your production frontend domain
     * 
     * Methods allowed: GET, POST (for shortening and redirects)
     * Headers allowed: All (Content-Type, Authorization, etc.)
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply to all endpoints
                .allowedOriginPatterns("*") // Allow all origins including file://
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Cache preflight response for 1 hour
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Apply rate limiting to all endpoints
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**");
    }
}

package com.example.miniURL.interceptor;

import com.example.miniURL.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Intercepts HTTP requests to enforce rate limiting
 * 
 * Returns 429 Too Many Requests if limit exceeded
 * Adds rate limit headers to response for client awareness
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitConfig rateLimitConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        String clientIp = getClientIP(request);
        String path = request.getRequestURI();
        
        Bucket bucket;
        
        // Different limits for different endpoints
        if (path.equals("/shorten")) {
            bucket = rateLimitConfig.resolveShorteningBucket(clientIp);
        } else {
            bucket = rateLimitConfig.resolveRedirectBucket(clientIp);
        }
        
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            // Request allowed
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            // Rate limit exceeded
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.getWriter().write(String.format(
                "{\"error\": \"Too many requests\", \"message\": \"Rate limit exceeded. Please try again in %d seconds.\"}", 
                waitForRefill
            ));
            
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
            return false;
        }
    }

    /**
     * Extracts client IP address, handling proxies
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}

package com.example.miniURL.controller;

import com.example.miniURL.dto.ShortenUrlRequestDto;
import com.example.miniURL.dto.ShortenUrlResponseDto;
import com.example.miniURL.service.AnalyticsService;
import com.example.miniURL.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@RestController
@RequiredArgsConstructor
public class UrlController {
    private final UrlService urlService;
    private final AnalyticsService analyticsService;

    //idempotency: the ability of API to produce same result for same request
    @PostMapping("/shorten") // non-idempotent
    public ShortenUrlResponseDto shortenURL(@RequestBody ShortenUrlRequestDto requestDto){
        return urlService.shortenUrl(requestDto);
    }
    
    /**
     * Get analytics for a shortened URL
     * 
     * Endpoint: GET /analytics/{shortCode}
     * Example: GET /analytics/abc12345
     * 
     * Returns comprehensive analytics data:
     * - Total clicks
     * - Unique visitors
     * - Recent activity
     * - Geographic distribution
     * - Device/browser breakdown
     * - Traffic sources (referrers)
     * - Timeline data for charts
     * 
     * WHY THIS EXISTS:
     * Frontend analytics dashboard calls this to display stats
     * 
     * RESPONSE EXAMPLE:
     * {
     *   "totalClicks": 1247,
     *   "uniqueVisitors": 892,
     *   "recentClicks": [...],
     *   "clicksByCountry": [{label: "USA", value: 500}, ...],
     *   "clicksByDevice": [{label: "Mobile", value: 700}, ...],
     *   ...
     * }
     */
    @GetMapping("/analytics/{shortCode}")
    public ResponseEntity<?> getAnalytics(@PathVariable String shortCode) {
        var urlEntity = urlService.getUrlEntityByShortCode(shortCode);
        
        if (urlEntity == null) {
            return ResponseEntity.notFound().build();
        }
        
        var analytics = analyticsService.getAnalytics(urlEntity);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> getRedirectionUrl(
            @PathVariable String shortCode,
            HttpServletRequest request) {
        
        /**
         * ANALYTICS TRACKING:
         * Before redirecting, we capture information about this click
         * 
         * What we extract from the HTTP request:
         * 1. IP Address - Who clicked? (for geolocation)
         * 2. User-Agent - What browser/device? (for device detection)
         * 3. Referer - Where did they come from? (traffic sources)
         */
        
        // Get the original URL and URL entity
        var urlEntity = urlService.getUrlEntityByShortCode(shortCode);
        if (urlEntity == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Extract request information for analytics
        String ipAddress = extractClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");
        
        // Record the click asynchronously (don't slow down the redirect)
        try {
            analyticsService.recordClick(urlEntity, ipAddress, userAgent, referrer);
        } catch (Exception e) {
            // Log error but don't fail the redirect
            // Analytics failure shouldn't break the core functionality
        }
        
        // Redirect to original URL
        URI redirectUri = urlService.getRedirectionUri(shortCode);
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(redirectUri)
                .build();
    }
    
    /**
     * Extract client's real IP address
     * 
     * WHY THIS IS COMPLEX:
     * - Users might be behind proxies, load balancers, CDNs
     * - We need to check multiple headers to find the real IP
     * 
     * HEADER PRIORITY:
     * 1. X-Forwarded-For (most common, set by proxies)
     * 2. X-Real-IP (set by some proxies like nginx)
     * 3. request.getRemoteAddr() (fallback, might be proxy IP)
     * 
     * Example:
     * If user is behind CloudFlare CDN:
     * - X-Forwarded-For: "203.0.113.45, 198.51.100.1"
     * - We take the first IP: "203.0.113.45" (the real client)
     */
    private String extractClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For can contain multiple IPs: "client, proxy1, proxy2"
            // Take the first one (the real client IP)
            int commaIndex = ip.indexOf(',');
            if (commaIndex > 0) {
                ip = ip.substring(0, commaIndex).trim();
            }
            return ip;
        }
        
        // Try X-Real-IP header
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        
        // Fallback to remote address
        return request.getRemoteAddr();
    }
}

package com.example.miniURL.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * UrlAnalytics Entity - Tracks every click on shortened URLs
 * 
 * WHY THIS EXISTS:
 * - Records detailed information about each redirect/click
 * - Enables analytics dashboard to show traffic patterns
 * - Helps understand user behavior (where they're from, what device they use)
 * 
 * WHAT IT STORES:
 * - Click timestamp (when was the link clicked)
 * - IP address (where did the click come from)
 * - Geographic data (country, city from IP)
 * - Device info (browser, operating system, device type)
 * - Referrer (which website sent the user to this link)
 */
@Entity
@Table(name = "url_analytics", indexes = {
    @Index(name = "idx_url_id", columnList = "url_id"),
    @Index(name = "idx_clicked_at", columnList = "clickedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relationship to UrlEntity
     * Many analytics records belong to one URL
     * Using @ManyToOne because multiple clicks can happen for one short URL
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private UrlEntity url;

    /**
     * When was this link clicked?
     * Example: "2026-01-06 15:30:45"
     */
    @Column(nullable = false)
    private LocalDateTime clickedAt;

    /**
     * IP Address of the visitor
     * Example: "203.0.113.45"
     * Used for geolocation lookup
     */
    @Column(length = 45) // IPv6 can be up to 45 chars
    private String ipAddress;

    /**
     * Geographic Data
     * Country: "United States", "India", "United Kingdom"
     */
    @Column(length = 100)
    private String country;

    /**
     * City: "New York", "Mumbai", "London"
     */
    @Column(length = 100)
    private String city;

    /**
     * Device Information
     * Browser: "Chrome", "Firefox", "Safari", "Edge"
     */
    @Column(length = 50)
    private String browser;

    /**
     * Operating System: "Windows", "Mac OS", "Linux", "Android", "iOS"
     */
    @Column(length = 50)
    private String operatingSystem;

    /**
     * Device Type: "Computer", "Mobile", "Tablet"
     */
    @Column(length = 20)
    private String deviceType;

    /**
     * Referrer - Where did the user come from?
     * Examples:
     * - "https://twitter.com" (clicked from Twitter)
     * - "https://google.com" (found via Google search)
     * - "direct" (typed URL directly or no referrer)
     */
    @Column(length = 500)
    private String referrer;

    /**
     * Complete User-Agent string for detailed analysis if needed
     * Example: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36..."
     */
    @Column(length = 1000)
    private String userAgent;
}

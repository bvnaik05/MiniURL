package com.example.miniURL.repository;

import com.example.miniURL.entity.UrlAnalytics;
import com.example.miniURL.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AnalyticsRepository - Database operations for URL analytics
 * 
 * WHY THIS EXISTS:
 * - Provides methods to save analytics records
 * - Query analytics data for dashboard display
 * - Calculate statistics (total clicks, unique visitors, etc.)
 * 
 * WHAT IT DOES:
 * - Spring Data JPA automatically implements these methods
 * - We just define the method signatures
 * - Custom queries for complex analytics calculations
 */
@Repository
public interface AnalyticsRepository extends JpaRepository<UrlAnalytics, Long> {

    /**
     * Find all analytics records for a specific URL
     * Used to display complete click history
     * 
     * Example: Get all clicks for miniurl.com/abc123
     */
    List<UrlAnalytics> findByUrlOrderByClickedAtDesc(UrlEntity url);

    /**
     * Count total clicks for a specific URL
     * 
     * Example: How many times was miniurl.com/abc123 clicked?
     * Returns: 1,247 clicks
     */
    long countByUrl(UrlEntity url);

    /**
     * Count unique IP addresses (approximate unique visitors)
     * 
     * WHY: Same person might click multiple times from same IP
     * This gives us "unique visitors" metric
     * 
     * Example: 1,247 total clicks, but only 892 unique IPs
     */
    @Query("SELECT COUNT(DISTINCT a.ipAddress) FROM UrlAnalytics a WHERE a.url = :url")
    long countUniqueVisitorsByUrl(@Param("url") UrlEntity url);

    /**
     * Get clicks within a date range
     * Used for "Last 7 days" or "Last 30 days" analytics
     * 
     * Example: Show me all clicks in January 2026
     */
    @Query("SELECT a FROM UrlAnalytics a WHERE a.url = :url AND a.clickedAt BETWEEN :startDate AND :endDate ORDER BY a.clickedAt DESC")
    List<UrlAnalytics> findByUrlAndDateRange(
        @Param("url") UrlEntity url,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get most recent clicks (for "Recent Activity" section)
     * 
     * Example: Show me the last 10 clicks on this URL
     */
    List<UrlAnalytics> findTop10ByUrlOrderByClickedAtDesc(UrlEntity url);

    /**
     * Get click count by country
     * Returns data for geographic distribution chart
     * 
     * Example Result:
     * [
     *   ["USA", 500],
     *   ["India", 300],
     *   ["UK", 200]
     * ]
     */
    @Query("SELECT a.country, COUNT(a) FROM UrlAnalytics a WHERE a.url = :url GROUP BY a.country ORDER BY COUNT(a) DESC")
    List<Object[]> getClicksByCountry(@Param("url") UrlEntity url);

    /**
     * Get click count by device type
     * Shows Mobile vs Desktop vs Tablet usage
     * 
     * Example Result:
     * [
     *   ["Mobile", 700],
     *   ["Computer", 400],
     *   ["Tablet", 100]
     * ]
     */
    @Query("SELECT a.deviceType, COUNT(a) FROM UrlAnalytics a WHERE a.url = :url GROUP BY a.deviceType ORDER BY COUNT(a) DESC")
    List<Object[]> getClicksByDeviceType(@Param("url") UrlEntity url);

    /**
     * Get click count by browser
     * Shows Chrome vs Firefox vs Safari usage
     * 
     * Example Result:
     * [
     *   ["Chrome", 600],
     *   ["Safari", 300],
     *   ["Firefox", 200]
     * ]
     */
    @Query("SELECT a.browser, COUNT(a) FROM UrlAnalytics a WHERE a.url = :url GROUP BY a.browser ORDER BY COUNT(a) DESC")
    List<Object[]> getClicksByBrowser(@Param("url") UrlEntity url);

    /**
     * Get clicks grouped by date (for timeline chart)
     * Shows traffic trends over time
     * 
     * Example Result:
     * [
     *   ["2026-01-01", 45],
     *   ["2026-01-02", 67],
     *   ["2026-01-03", 89]
     * ]
     */
    @Query("SELECT DATE(a.clickedAt), COUNT(a) FROM UrlAnalytics a WHERE a.url = :url GROUP BY DATE(a.clickedAt) ORDER BY DATE(a.clickedAt)")
    List<Object[]> getClicksByDate(@Param("url") UrlEntity url);

    /**
     * Get top referrers (where traffic is coming from)
     * Shows which platforms drive the most traffic
     * 
     * Example Result:
     * [
     *   ["twitter.com", 300],
     *   ["facebook.com", 200],
     *   ["direct", 150]
     * ]
     */
    @Query("SELECT a.referrer, COUNT(a) FROM UrlAnalytics a WHERE a.url = :url AND a.referrer IS NOT NULL GROUP BY a.referrer ORDER BY COUNT(a) DESC")
    List<Object[]> getTopReferrers(@Param("url") UrlEntity url);
}

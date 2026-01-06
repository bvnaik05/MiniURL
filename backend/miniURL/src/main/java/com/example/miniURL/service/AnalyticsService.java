package com.example.miniURL.service;

import com.example.miniURL.entity.UrlAnalytics;
import com.example.miniURL.entity.UrlEntity;
import com.example.miniURL.repository.AnalyticsRepository;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import eu.bitwalker.useragentutils.DeviceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AnalyticsService - Handles click tracking and analytics data processing
 * 
 * WHY THIS EXISTS:
 * - Captures detailed information about each click
 * - Extracts user data (browser, device, location) from HTTP request
 * - Provides analytics statistics for dashboard
 * 
 * HOW IT WORKS:
 * 1. When someone clicks a short URL, we call recordClick()
 * 2. We parse the User-Agent to detect browser/device
 * 3. We extract IP address and look up location (optional, simplified for now)
 * 4. Save all this data to database
 * 5. Later, dashboard calls getAnalytics() to display stats
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    /**
     * Record a click event
     * Called every time someone is redirected via a short URL
     * 
     * @param url The UrlEntity that was clicked
     * @param ipAddress IP address of the visitor
     * @param userAgentString User-Agent header from HTTP request
     * @param referrer Referer header from HTTP request (where user came from)
     * 
     * EXAMPLE:
     * User clicks: miniurl.com/abc123
     * - ipAddress: "203.0.113.45"
     * - userAgentString: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0"
     * - referrer: "https://twitter.com"
     */
    @Transactional
    public void recordClick(UrlEntity url, String ipAddress, String userAgentString, String referrer) {
        try {
            // Parse User-Agent to extract device/browser info
            UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
            
            // Extract browser information
            Browser browser = userAgent.getBrowser();
            String browserName = browser != null ? browser.getName() : "Unknown";
            
            // Extract operating system
            OperatingSystem os = userAgent.getOperatingSystem();
            String osName = os != null ? os.getName() : "Unknown";
            
            // Extract device type (Mobile, Computer, Tablet)
            DeviceType deviceType = os != null ? os.getDeviceType() : null;
            String deviceTypeName = deviceType != null ? deviceType.getName() : "Unknown";
            
            // Geolocation (IP to Country/City)
            // NOTE: For production, you'd use MaxMind GeoIP2 database
            // For now, we'll use a simplified approach
            Map<String, String> geoData = getGeolocationFromIP(ipAddress);
            
            // Clean up referrer
            String cleanReferrer = cleanReferrer(referrer);
            
            // Build analytics record
            UrlAnalytics analytics = UrlAnalytics.builder()
                .url(url)
                .clickedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .country(geoData.get("country"))
                .city(geoData.get("city"))
                .browser(browserName)
                .operatingSystem(osName)
                .deviceType(deviceTypeName)
                .referrer(cleanReferrer)
                .userAgent(userAgentString)
                .build();
            
            // Save to database
            analyticsRepository.save(analytics);
            
            log.info("Recorded click for URL {} from IP {} using {} on {}", 
                url.getShortCode(), ipAddress, browserName, osName);
                
        } catch (Exception e) {
            // Don't fail the redirect if analytics fails
            // Just log the error and continue
            log.error("Error recording analytics: {}", e.getMessage());
        }
    }

    /**
     * Get comprehensive analytics for a URL
     * Returns all stats needed for the dashboard
     * 
     * @param url The UrlEntity to get analytics for
     * @return Map containing all analytics data
     * 
     * RETURNS:
     * {
     *   "totalClicks": 1247,
     *   "uniqueVisitors": 892,
     *   "recentClicks": [...],
     *   "clicksByCountry": [...],
     *   "clicksByDevice": [...],
     *   "clicksByBrowser": [...],
     *   "clicksByDate": [...],
     *   "topReferrers": [...]
     * }
     */
    public Map<String, Object> getAnalytics(UrlEntity url) {
        Map<String, Object> analytics = new HashMap<>();
        
        try {
            // Basic stats
            long totalClicks = analyticsRepository.countByUrl(url);
            long uniqueVisitors = analyticsRepository.countUniqueVisitorsByUrl(url);
            
            // Recent activity (last 10 clicks)
            List<UrlAnalytics> recentClicks = analyticsRepository.findTop10ByUrlOrderByClickedAtDesc(url);
            
            // Geographic distribution
            List<Object[]> clicksByCountry = analyticsRepository.getClicksByCountry(url);
            
            // Device breakdown
            List<Object[]> clicksByDevice = analyticsRepository.getClicksByDeviceType(url);
            
            // Browser breakdown
            List<Object[]> clicksByBrowser = analyticsRepository.getClicksByBrowser(url);
            
            // Timeline data
            List<Object[]> clicksByDate = analyticsRepository.getClicksByDate(url);
            
            // Traffic sources
            List<Object[]> topReferrers = analyticsRepository.getTopReferrers(url);
            
            // Build response
            analytics.put("totalClicks", totalClicks);
            analytics.put("uniqueVisitors", uniqueVisitors);
            analytics.put("recentClicks", formatRecentClicks(recentClicks));
            analytics.put("clicksByCountry", formatKeyValuePairs(clicksByCountry));
            analytics.put("clicksByDevice", formatKeyValuePairs(clicksByDevice));
            analytics.put("clicksByBrowser", formatKeyValuePairs(clicksByBrowser));
            analytics.put("clicksByDate", formatKeyValuePairs(clicksByDate));
            analytics.put("topReferrers", formatKeyValuePairs(topReferrers));
            
            log.info("Retrieved analytics for URL {}: {} total clicks, {} unique visitors", 
                url.getShortCode(), totalClicks, uniqueVisitors);
                
        } catch (Exception e) {
            log.error("Error retrieving analytics: {}", e.getMessage());
            analytics.put("error", "Failed to retrieve analytics");
        }
        
        return analytics;
    }

    /**
     * Get geolocation from IP address
     * 
     * PRODUCTION NOTE:
     * For a real app, you'd download MaxMind GeoLite2 database and use it
     * https://dev.maxmind.com/geoip/geolite2-free-geolocation-data
     * 
     * For this project, we'll use a simplified approach:
     * - Try to detect localhost/private IPs
     * - For real IPs, you could integrate a free API like ip-api.com
     * - Or use MaxMind GeoIP2 library with GeoLite2 database
     * 
     * SIMPLIFIED VERSION (for demo):
     * Returns "Unknown" for most IPs to avoid external API calls during development
     */
    private Map<String, String> getGeolocationFromIP(String ipAddress) {
        Map<String, String> geoData = new HashMap<>();
        
        // Check for localhost or private IPs
        if (ipAddress == null || 
            ipAddress.startsWith("127.") || 
            ipAddress.startsWith("192.168.") ||
            ipAddress.startsWith("10.") ||
            ipAddress.equals("0:0:0:0:0:0:0:1") ||
            ipAddress.equals("::1")) {
            
            geoData.put("country", "Local/Development");
            geoData.put("city", "Localhost");
            return geoData;
        }
        
        // For production, uncomment this to use MaxMind GeoIP2:
        /*
        try {
            File database = new File("path/to/GeoLite2-City.mmdb");
            DatabaseReader reader = new DatabaseReader.Builder(database).build();
            InetAddress ipAddressObj = InetAddress.getByName(ipAddress);
            CityResponse response = reader.city(ipAddressObj);
            
            geoData.put("country", response.getCountry().getName());
            geoData.put("city", response.getCity().getName());
        } catch (Exception e) {
            log.warn("Geolocation lookup failed for IP {}: {}", ipAddress, e.getMessage());
            geoData.put("country", "Unknown");
            geoData.put("city", "Unknown");
        }
        */
        
        // Simplified version for demo (no external API calls)
        geoData.put("country", "Unknown");
        geoData.put("city", "Unknown");
        
        return geoData;
    }

    /**
     * Clean up referrer URL
     * Extracts just the domain name from full URL
     * 
     * Example:
     * Input: "https://twitter.com/some/path?query=123"
     * Output: "twitter.com"
     */
    private String cleanReferrer(String referrer) {
        if (referrer == null || referrer.isEmpty()) {
            return "direct";
        }
        
        try {
            // Remove protocol (http://, https://)
            String cleaned = referrer.replaceFirst("^https?://", "");
            
            // Remove www.
            cleaned = cleaned.replaceFirst("^www\\.", "");
            
            // Take only domain (remove path and query)
            int slashIndex = cleaned.indexOf('/');
            if (slashIndex > 0) {
                cleaned = cleaned.substring(0, slashIndex);
            }
            
            return cleaned;
        } catch (Exception e) {
            return referrer;
        }
    }

    /**
     * Format recent clicks for frontend display
     */
    private List<Map<String, Object>> formatRecentClicks(List<UrlAnalytics> clicks) {
        List<Map<String, Object>> formatted = new ArrayList<>();
        
        for (UrlAnalytics click : clicks) {
            Map<String, Object> clickData = new HashMap<>();
            clickData.put("clickedAt", click.getClickedAt().toString());
            clickData.put("country", click.getCountry());
            clickData.put("city", click.getCity());
            clickData.put("browser", click.getBrowser());
            clickData.put("os", click.getOperatingSystem());
            clickData.put("deviceType", click.getDeviceType());
            clickData.put("referrer", click.getReferrer());
            formatted.add(clickData);
        }
        
        return formatted;
    }

    /**
     * Format key-value pairs for charts
     * Converts database results to frontend-friendly format
     */
    private List<Map<String, Object>> formatKeyValuePairs(List<Object[]> data) {
        List<Map<String, Object>> formatted = new ArrayList<>();
        
        for (Object[] row : data) {
            Map<String, Object> item = new HashMap<>();
            item.put("label", row[0] != null ? row[0].toString() : "Unknown");
            item.put("value", row[1]);
            formatted.add(item);
        }
        
        return formatted;
    }
}

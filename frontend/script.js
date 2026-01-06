// ===========================
// API CONFIGURATION
// ===========================
const API_BASE_URL = 'http://localhost:8080';

// ===========================
// DOM ELEMENTS
// ===========================
const urlInput = document.getElementById('urlInput');
const shortenBtn = document.getElementById('shortenBtn');
const loadingState = document.getElementById('loadingState');
const resultSection = document.getElementById('resultSection');
const errorState = document.getElementById('errorState');
const shortUrlDisplay = document.getElementById('shortUrlDisplay');
const copyBtn = document.getElementById('copyBtn');
const shortenAnotherBtn = document.getElementById('shortenAnotherBtn');
const tryAgainBtn = document.getElementById('tryAgainBtn');
const errorMessage = document.getElementById('errorMessage');

// Analytics DOM Elements
const viewAnalyticsBtn = document.getElementById('viewAnalyticsBtn');
const analyticsSection = document.getElementById('analyticsSection');
const closeAnalyticsBtn = document.getElementById('closeAnalyticsBtn');
const analyticsShortUrl = document.getElementById('analyticsShortUrl');
const analyticsLoading = document.getElementById('analyticsLoading');
const analyticsContent = document.getElementById('analyticsContent');

// ===========================
// STATE MANAGEMENT
// ===========================
let currentShortUrl = '';
let currentShortCode = '';

// ===========================
// UTILITY FUNCTIONS
// ===========================

/**
 * Validates URL format
 */
function isValidUrl(string) {
    try {
        const url = new URL(string);
        return url.protocol === 'http:' || url.protocol === 'https:';
    } catch (_) {
        return false;
    }
}

/**
 * Shows a specific state and hides others
 */
function showState(state) {
    loadingState.style.display = 'none';
    resultSection.style.display = 'none';
    errorState.style.display = 'none';

    if (state === 'loading') {
        loadingState.style.display = 'block';
    } else if (state === 'result') {
        resultSection.style.display = 'block';
    } else if (state === 'error') {
        errorState.style.display = 'block';
    }
}

/**
 * Shows error message
 */
function showError(message) {
    errorMessage.textContent = message;
    showState('error');
    shortenBtn.disabled = false;
}

/**
 * Resets the form to initial state
 */
function resetForm() {
    urlInput.value = '';
    currentShortUrl = '';
    showState(null);
    shortenBtn.disabled = false;
    
    // Reset copy button
    const copyText = copyBtn.querySelector('.copy-text');
    copyText.textContent = 'Copy';
    copyBtn.classList.remove('copied');
    
    // Focus input
    urlInput.focus();
}

// ===========================
// API FUNCTIONS
// ===========================

/**
 * Calls the backend API to shorten URL
 */
async function shortenUrl(longUrl) {
    const response = await fetch(`${API_BASE_URL}/shorten`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ url: longUrl })
    });

    if (!response.ok) {
        // Handle different error status codes
        if (response.status === 429) {
            throw new Error('Rate limit exceeded. Please wait a moment and try again.');
        } else if (response.status === 400) {
            const error = await response.json();
            throw new Error(error.message || 'Invalid URL provided.');
        } else if (response.status === 500) {
            throw new Error('Server error. Please try again later.');
        } else {
            throw new Error('Failed to shorten URL. Please try again.');
        }
    }

    return await response.json();
}

// ===========================
// EVENT HANDLERS
// ===========================

/**
 * Handles the shorten button click
 */
async function handleShortenUrl() {
    const longUrl = urlInput.value.trim();

    // Validation
    if (!longUrl) {
        urlInput.focus();
        urlInput.style.animation = 'shake 0.5s ease';
        setTimeout(() => {
            urlInput.style.animation = '';
        }, 500);
        return;
    }

    if (!isValidUrl(longUrl)) {
        showError('Please enter a valid URL (must start with http:// or https://)');
        return;
    }

    // Disable button and show loading
    shortenBtn.disabled = true;
    showState('loading');

    try {
        // Call API
        const data = await shortenUrl(longUrl);
        
        // Store result
        currentShortUrl = data.shortUrl;
        currentShortCode = data.shortCode; // Store short code for analytics
        
        // Display result
        shortUrlDisplay.value = currentShortUrl;
        showState('result');
        
        // Auto-select the URL for easy copying
        shortUrlDisplay.select();
        
    } catch (error) {
        console.error('Error shortening URL:', error);
        showError(error.message);
    }
}

/**
 * Handles copy to clipboard with one-click
 */
async function handleCopyUrl() {
    try {
        // Modern clipboard API
        await navigator.clipboard.writeText(currentShortUrl);
        
        // Visual feedback
        const copyText = copyBtn.querySelector('.copy-text');
        const originalText = copyText.textContent;
        
        copyText.textContent = 'Copied!';
        copyBtn.classList.add('copied');
        
        // Also select the text in input
        shortUrlDisplay.select();
        
        // Reset after 2 seconds
        setTimeout(() => {
            copyText.textContent = originalText;
            copyBtn.classList.remove('copied');
        }, 2000);
        
    } catch (err) {
        // Fallback for older browsers
        shortUrlDisplay.select();
        document.execCommand('copy');
        
        const copyText = copyBtn.querySelector('.copy-text');
        copyText.textContent = 'Copied!';
        copyBtn.classList.add('copied');
        
        setTimeout(() => {
            copyText.textContent = 'Copy';
            copyBtn.classList.remove('copied');
        }, 2000);
    }
}

/**
 * Handles "Shorten Another URL" button click
 */
function handleShortenAnother() {
    resetForm();
}

/**
 * Handles "Try Again" button click (on error)
 */
function handleTryAgain() {
    resetForm();
}

/**
 * Handles Enter key press in input field
 */
function handleKeyPress(event) {
    if (event.key === 'Enter' && !shortenBtn.disabled) {
        handleShortenUrl();
    }
}

// ===========================
// ANALYTICS FUNCTIONS
// ===========================

/**
 * Fetch analytics data from backend
 * 
 * WHAT THIS DOES:
 * Calls GET /analytics/{shortCode} to get all stats
 * Returns comprehensive analytics data for display
 */
async function fetchAnalytics(shortCode) {
    console.log('Fetching analytics for:', shortCode);
    const url = `${API_BASE_URL}/analytics/${shortCode}`;
    console.log('Analytics URL:', url);
    
    const response = await fetch(url);
    
    if (!response.ok) {
        throw new Error('Failed to fetch analytics data');
    }
    
    const data = await response.json();
    console.log('Analytics data received:', data);
    return data;
}

/**
 * Handle "View Analytics" button click
 * 
 * WHAT HAPPENS:
 * 1. Show loading spinner
 * 2. Fetch analytics from backend
 * 3. Display all charts and stats
 * 4. Show recent activity
 */
async function handleViewAnalytics() {
    if (!currentShortCode) {
        return;
    }
    
    // Show analytics section with loading state
    analyticsSection.style.display = 'block';
    analyticsLoading.style.display = 'block';
    analyticsContent.style.display = 'none';
    analyticsShortUrl.textContent = currentShortUrl;
    
    // Scroll to analytics section smoothly
    analyticsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
    
    try {
        // Fetch data
        const data = await fetchAnalytics(currentShortCode);
        
        // Check if there's any data
        if (data.totalClicks === 0) {
            analyticsLoading.innerHTML = `
                <div style="color: white; padding: 3rem; text-align: center;">
                    <svg width="64" height="64" viewBox="0 0 24 24" fill="none" style="margin: 0 auto 1rem; opacity: 0.5;">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                        <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    <h3 style="margin-bottom: 1rem;">No Analytics Data Yet</h3>
                    <p style="opacity: 0.8; margin-bottom: 1rem;">This URL hasn't been clicked yet.</p>
                    <p style="opacity: 0.7; font-size: 0.9rem;">
                        Click the short URL to generate analytics data:<br>
                        <code style="background: rgba(0,0,0,0.3); padding: 0.5rem 1rem; border-radius: 8px; display: inline-block; margin-top: 0.5rem;">${currentShortUrl}</code>
                    </p>
                </div>
            `;
            return;
        }
        
        // Display all analytics
        displayAnalytics(data);
        
        // Show content, hide loading
        analyticsLoading.style.display = 'none';
        analyticsContent.style.display = 'block';
        
    } catch (error) {
        console.error('Error fetching analytics:', error);
        analyticsLoading.innerHTML = `
            <div style="color: white; padding: 2rem;">
                <p>❌ Failed to load analytics</p>
                <p style="font-size: 0.9rem; opacity: 0.8;">${error.message}</p>
            </div>
        `;
    }
}

/**
 * Display all analytics data
 * 
 * WHAT THIS DOES:
 * Takes the analytics object from backend and renders:
 * - Stats cards (total clicks, unique visitors)
 * - Bar charts (country, device, browser, referrers)
 * - Recent activity list
 */
function displayAnalytics(data) {
    console.log('Displaying analytics with data:', data);
    
    // Update stats cards
    document.getElementById('totalClicks').textContent = formatNumber(data.totalClicks || 0);
    document.getElementById('uniqueVisitors').textContent = formatNumber(data.uniqueVisitors || 0);
    
    // Render charts
    renderBarChart('countryChart', data.clicksByCountry || []);
    renderBarChart('deviceChart', data.clicksByDevice || []);
    renderBarChart('browserChart', data.clicksByBrowser || []);
    renderBarChart('referrerChart', data.topReferrers || []);
    
    // Render recent activity
    renderRecentActivity(data.recentClicks || []);
}

/**
 * Render a bar chart
 * 
 * WHAT THIS DOES:
 * Creates horizontal bar chart from data
 * Each bar shows label (e.g., "USA") and value (e.g., 500 clicks)
 * Bar width is proportional to percentage of total
 * 
 * EXAMPLE DATA:
 * [
 *   {label: "USA", value: 500},
 *   {label: "India", value: 300},
 *   {label: "UK", value: 200}
 * ]
 */
function renderBarChart(elementId, data) {
    const container = document.getElementById(elementId);
    
    if (!data || data.length === 0) {
        container.innerHTML = '<div class="no-data">No data available</div>';
        return;
    }
    
    // Find max value for percentage calculation
    const maxValue = Math.max(...data.map(item => item.value));
    
    // Generate HTML for bars
    const html = data.slice(0, 5).map(item => {
        const percentage = (item.value / maxValue) * 100;
        return `
            <div class="chart-bar-container">
                <div class="chart-bar-label">
                    <span class="chart-bar-label-text">${item.label || 'Unknown'}</span>
                    <span class="chart-bar-label-value">${formatNumber(item.value)}</span>
                </div>
                <div class="chart-bar-track">
                    <div class="chart-bar-fill" style="width: ${percentage}%"></div>
                </div>
            </div>
        `;
    }).join('');
    
    container.innerHTML = html;
}

/**
 * Render recent activity list
 * 
 * WHAT THIS DOES:
 * Shows last 10 clicks with details:
 * - When (timestamp)
 * - Where from (country/city)
 * - Device/Browser
 * - Referrer
 * 
 * EXAMPLE ITEM:
 * {
 *   clickedAt: "2026-01-06T15:30:45",
 *   country: "USA",
 *   city: "New York",
 *   browser: "Chrome",
 *   os: "Windows",
 *   deviceType: "Computer",
 *   referrer: "twitter.com"
 * }
 */
function renderRecentActivity(clicks) {
    const container = document.getElementById('activityList');
    
    if (!clicks || clicks.length === 0) {
        container.innerHTML = '<div class="no-data">No recent clicks</div>';
        return;
    }
    
    const html = clicks.map(click => {
        const time = formatDateTime(click.clickedAt);
        const location = [click.city, click.country].filter(Boolean).join(', ') || 'Unknown';
        
        return `
            <div class="activity-item">
                <div class="activity-time">${time}</div>
                <div class="activity-details">
                    <span class="activity-tag">📍 ${location}</span>
                    <span class="activity-tag">💻 ${click.deviceType || 'Unknown'}</span>
                    <span class="activity-tag">🌐 ${click.browser || 'Unknown'}</span>
                    ${click.referrer && click.referrer !== 'direct' 
                        ? `<span class="activity-tag">🔗 ${click.referrer}</span>` 
                        : ''}
                </div>
            </div>
        `;
    }).join('');
    
    container.innerHTML = html;
}

/**
 * Handle close analytics button
 */
function handleCloseAnalytics() {
    analyticsSection.style.display = 'none';
}

/**
 * Format large numbers with commas
 * Example: 1247 → "1,247"
 */
function formatNumber(num) {
    return num.toLocaleString();
}

/**
 * Format datetime for display
 * Example: "2026-01-06T15:30:45" → "Jan 6, 2026 at 3:30 PM"
 */
function formatDateTime(dateString) {
    try {
        const date = new Date(dateString);
        return date.toLocaleString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
            hour: 'numeric',
            minute: '2-digit',
            hour12: true
        });
    } catch (e) {
        return dateString;
    }
}

// ===========================
// EVENT LISTENERS
// ===========================
shortenBtn.addEventListener('click', handleShortenUrl);
copyBtn.addEventListener('click', handleCopyUrl);
shortenAnotherBtn.addEventListener('click', handleShortenAnother);
tryAgainBtn.addEventListener('click', handleTryAgain);
urlInput.addEventListener('keypress', handleKeyPress);
viewAnalyticsBtn.addEventListener('click', handleViewAnalytics);
closeAnalyticsBtn.addEventListener('click', handleCloseAnalytics);

// Also allow clicking on the short URL input to copy
shortUrlDisplay.addEventListener('click', function() {
    this.select();
});

// ===========================
// INITIALIZATION
// ===========================
// Focus input on page load
window.addEventListener('load', () => {
    urlInput.focus();
});

// Handle paste event - auto-trim whitespace
urlInput.addEventListener('paste', (e) => {
    setTimeout(() => {
        urlInput.value = urlInput.value.trim();
    }, 10);
});

// ===========================
// BACKEND CONNECTION CHECK
// ===========================
// Check if backend is running (optional but helpful)
async function checkBackendHealth() {
    try {
        const response = await fetch(`${API_BASE_URL}/shorten`, {
            method: 'OPTIONS'
        });
        console.log('✅ Backend is reachable');
    } catch (error) {
        console.warn('⚠️ Backend might not be running. Make sure to start the Spring Boot application.');
    }
}

// Check on page load
checkBackendHealth();

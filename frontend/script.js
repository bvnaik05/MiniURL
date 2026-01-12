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

// ===========================
// STATE MANAGEMENT
// ===========================
let currentShortUrl = '';

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
// EVENT LISTENERS
// ===========================
shortenBtn.addEventListener('click', handleShortenUrl);
copyBtn.addEventListener('click', handleCopyUrl);
shortenAnotherBtn.addEventListener('click', handleShortenAnother);
tryAgainBtn.addEventListener('click', handleTryAgain);
urlInput.addEventListener('keypress', handleKeyPress);

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

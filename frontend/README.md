# 🎨 MiniURL Frontend

Modern, glassmorphism-themed URL shortener interface with one-click copy functionality.

## 🌟 Features

- ✨ **Glassmorphism Design** - Beautiful glass effect with blur backdrop
- 🟢 **Green & White Theme** - Soothing light green gradient background
- 📱 **Fully Responsive** - Works on all devices
- ⚡ **One-Click Copy** - Copy shortened URLs instantly
- 🎭 **Smooth Animations** - Delightful user experience
- 🚫 **No Sign-up Required** - Simple and straightforward

## 🚀 How to Run

### Option 1: Direct File Opening
1. Simply double-click `index.html`
2. Opens in your default browser
3. Make sure backend is running on `localhost:8080`

### Option 2: Local Server (Recommended)
```bash
# Using Python
cd frontend
python -m http.server 3000

# Using Node.js (http-server)
npx http-server frontend -p 3000

# Using VS Code Live Server
# Right-click index.html → Open with Live Server
```

Then visit: `http://localhost:3000`

## 🔗 Backend Connection

The frontend connects to backend at: `http://localhost:8080`

Make sure your Spring Boot backend is running:
```bash
cd backend/miniURL
.\mvnw spring-boot:run
```

## 🎯 Usage

1. **Paste** your long URL in the input field
2. Click **"Shorten"** button (or press Enter)
3. **Copy** the shortened URL with one click
4. **Share** or use it anywhere!

## 🎨 Design Specifications

### Colors:
- **Primary Green**: `#4ade80` (Light green)
- **Secondary Green**: `#22c55e` (Medium green)
- **Background Gradient**: `#d4fc79` → `#96e6a1` → `#4ade80`
- **Glass Effect**: `rgba(255, 255, 255, 0.25)` with backdrop blur

### Typography:
- **Font**: Poppins (Google Fonts)
- **Weights**: 300 (Light), 400 (Regular), 500 (Medium), 600 (SemiBold), 700 (Bold)

### Effects:
- **Glassmorphism**: backdrop-filter blur(20px)
- **Shadows**: Multiple layers for depth
- **Animations**: Fade in, slide up, bounce, float

## 📁 File Structure

```
frontend/
├── index.html      # Main HTML structure
├── styles.css      # All styles (glassmorphism, responsive)
└── script.js       # API integration & interactions
```

## 🌐 API Integration

### POST /shorten
```javascript
fetch('http://localhost:8080/shorten', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ url: 'https://example.com' })
})
```

**Response**:
```json
{
  "shortCode": "abc12345",
  "shortUrl": "http://localhost:8080/abc12345"
}
```

### GET /{shortCode}
Automatically redirects to original URL (301 redirect)

## ✨ Key Features Explained

### 1. One-Click Copy
```javascript
// Modern Clipboard API with fallback
await navigator.clipboard.writeText(url);
// Fallback: document.execCommand('copy')
```

### 2. Glass Effect
```css
background: rgba(255, 255, 255, 0.25);
backdrop-filter: blur(20px) saturate(180%);
border: 1px solid rgba(255, 255, 255, 0.3);
```

### 3. Animated Background
```css
/* Floating bubbles with ease-in-out animation */
animation: float 15s infinite ease-in-out;
```

### 4. Form Validation
- URL format validation (must start with http:// or https://)
- Empty input prevention
- Visual shake animation for invalid input

### 5. Error Handling
- Rate limit (429): "Please wait and try again"
- Invalid URL (400): "Invalid URL provided"
- Server error (500): "Server error, try later"
- Network error: "Failed to shorten URL"

## 📱 Responsive Breakpoints

- **Desktop**: 640px and above
- **Mobile**: Below 640px
  - Stacked button layout
  - Adjusted font sizes
  - Full-width components

## 🎭 Animations

- **Page Load**: Fade in & slide up
- **Logo**: Bounce & pulse
- **Bubbles**: Float & scale
- **Button Hover**: Lift with shadow
- **Success**: Checkmark animation
- **Error**: Shake animation
- **Copy**: Scale pulse

## 🔧 Customization

### Change Colors:
Edit `styles.css`:
```css
/* Primary color */
--green: #4ade80;

/* Background gradient */
background: linear-gradient(135deg, #d4fc79 0%, #96e6a1 50%, #4ade80 100%);
```

### Change API URL:
Edit `script.js`:
```javascript
const API_BASE_URL = 'https://your-production-api.com';
```

## 🚀 Deployment

### Option 1: Netlify (Recommended)
```bash
# Drag and drop 'frontend' folder to Netlify
# Or connect GitHub repo
```

### Option 2: Vercel
```bash
cd frontend
vercel --prod
```

### Option 3: GitHub Pages
```bash
# Push to GitHub
# Settings → Pages → Deploy from branch
```

### ⚠️ Important: Update API URL for production
```javascript
// In script.js
const API_BASE_URL = 'https://your-backend-api.com';
```

And update CORS in backend:
```java
// In WebConfig.java
.allowedOrigins("https://your-frontend-domain.com")
```

## 🎉 Result

You now have a beautiful, modern URL shortener with:
- ✅ Glassmorphism design
- ✅ Light green theme
- ✅ One-click copy
- ✅ Smooth animations
- ✅ Full responsiveness
- ✅ No sign-up needed

Enjoy your MiniURL! 🔗✨

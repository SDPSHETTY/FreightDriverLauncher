# Configuration Guide

This guide explains how to configure the real-world integrations in your Freight Driver Launcher.

## 1. Google Maps API Key Setup

The Navigation app uses Google Maps to show directions. You need a Google Maps API key.

### Steps to Get API Key:

1. **Go to Google Cloud Console**
   - Visit: https://console.cloud.google.com/

2. **Create a Project** (if you don't have one)
   - Click "Select a project" → "New Project"
   - Name it (e.g., "Freight Launcher")
   - Click "Create"

3. **Enable Maps SDK for Android**
   - In the console, go to "APIs & Services" → "Library"
   - Search for "Maps SDK for Android"
   - Click on it and click "Enable"

4. **Create API Key**
   - Go to "APIs & Services" → "Credentials"
   - Click "Create Credentials" → "API Key"
   - Copy the API key (it looks like: `AIzaSyXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxX`)

5. **Add API Key to Your App**
   - Open: `launcher/src/main/AndroidManifest.xml`
   - Find the line: `android:value="YOUR_API_KEY_HERE"`
   - Replace `YOUR_API_KEY_HERE` with your actual API key

   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="AIzaSyXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxX" />
   ```

6. **Restrict Your API Key** (Important for security)
   - In Google Cloud Console, click on your API key
   - Under "Application restrictions", select "Android apps"
   - Add your package name: `com.freight.launcher`
   - Get your SHA-1 fingerprint:
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
   - Add the SHA-1 fingerprint to the console
   - Save restrictions

### Configure Navigation Route:

Edit `common/src/main/java/com/freight/common/AppConfig.kt`:

```kotlin
object Navigation {
    // Starting location (origin)
    const val DEFAULT_ORIGIN_LAT = 41.8781  // Your origin latitude
    const val DEFAULT_ORIGIN_LNG = -87.6298 // Your origin longitude

    // Destination
    const val DEFAULT_DEST_LAT = 39.7392    // Your destination latitude
    const val DEFAULT_DEST_LNG = -104.9903  // Your destination longitude

    // Display names
    const val ORIGIN_NAME = "Your Starting Point"
    const val DEST_NAME = "Your Destination"

    // Default zoom level (higher = more zoomed in)
    const val DEFAULT_ZOOM = 5f
}
```

**Finding Coordinates:**
- Go to Google Maps (maps.google.com)
- Right-click on a location
- Click the coordinates at the top to copy them
- Format: First number is latitude, second is longitude

## 2. Dispatch Webapp URL Setup

The Dispatch app loads a web application in a WebView.

### Configure the URL:

Edit `common/src/main/java/com/freight/common/AppConfig.kt`:

```kotlin
const val DISPATCH_URL = "https://your-dispatch-system.com/driver-portal"
```

**Examples:**
- Production: `"https://dispatch.yourcompany.com/driver"`
- Staging: `"https://staging.dispatch.yourcompany.com"`
- Test: `"https://www.google.com"` (for testing)

### Requirements for Dispatch Webapp:

- Must be accessible over HTTPS (not HTTP)
- Should be mobile-responsive
- JavaScript must be enabled (already configured in the app)
- If using authentication, consider:
  - OAuth/SSO that works in WebView
  - Session cookies
  - Token-based auth

### Testing the WebView:

1. Set `DISPATCH_URL` to a simple URL first (e.g., "https://www.google.com")
2. Build and install the app
3. Tap the Dispatch tile
4. Verify the website loads correctly
5. Once working, update to your actual dispatch system URL

## 3. Rebuild and Install

After making configuration changes:

```bash
# Rebuild the app
./gradlew clean buildAllApks

# Install to device
./gradlew installAllApks

# Launch
./gradlew launchApp
```

## 4. Testing

### Test Navigation App:
1. Launch the launcher
2. Navigation should be in the main view
3. You should see:
   - Google Map with two markers (origin and destination)
   - A line connecting them
   - Route info card at top
   - If you see "API Key Required" warning, your API key isn't set up correctly

### Test Dispatch App:
1. Tap the Dispatch tile (red)
2. You should see:
   - Loading indicator
   - Then your dispatch website loads
   - If you see "Unable to Load" error, check:
     - Internet connection
     - URL is correct in AppConfig.kt
     - URL uses HTTPS (not HTTP)

## 5. Troubleshooting

### Google Maps shows blank screen:
- **Check API key**: Make sure it's in AndroidManifest.xml
- **Enable billing**: Google Maps requires a billing account (free tier available)
- **Check restrictions**: Make sure API key restrictions allow your app
- **Check logcat**: Look for Maps-related errors in Android Studio Logcat

### Dispatch WebView shows error:
- **Check URL**: Make sure it's accessible in a regular browser
- **Check internet permission**: Already added to manifest
- **Test with simple URL**: Try "https://www.google.com" first
- **Check SSL certificate**: Some corporate sites have self-signed certificates that WebView rejects

### App crashes after changes:
```bash
# Clean rebuild
./gradlew clean
./gradlew buildAllApks
adb uninstall com.freight.launcher
./gradlew installAllApks
```

## 6. Advanced Configuration

### Change App Colors:

Edit `launcher/src/main/java/com/freight/launcher/model/TileContentProvider.kt`:

```kotlin
TileInfo(
    id = "navigation",
    title = "Navigation",
    icon = "🧭",
    packageName = "com.freight.navigation",
    activityName = "com.freight.navigation.NavigationActivity",
    color = Color(0xFF2196F3) // Change this color
)
```

### Change Tile Layout Ratio:

Edit `launcher/src/main/java/com/freight/launcher/ui/TileGrid.kt`:

```kotlin
.weight(0.7f)  // Main tile (70%) - change to 0.8f for 80%
.weight(0.3f)  // Bottom tiles (30%) - change to 0.2f for 20%
```

### Add WebView Authentication:

Edit `app-dispatch/src/main/java/com/freight/dispatch/DispatchScreen.kt`:

Add headers or custom WebViewClient for authentication.

## 7. Production Checklist

Before deploying to drivers:

- [ ] Google Maps API key configured and restricted
- [ ] Dispatch URL points to production system
- [ ] Both apps tested on actual device
- [ ] Location permissions granted for Maps
- [ ] Internet connection verified
- [ ] Test with real dispatch credentials
- [ ] Test tile switching with both apps loaded
- [ ] Verify app performance on target devices
- [ ] Test in landscape orientation
- [ ] Test with poor internet connection

## Support

If you encounter issues:

1. Check Android Studio Logcat for errors
2. Test each app individually first
3. Verify configuration values in AppConfig.kt
4. Make sure all permissions are granted
5. Try on a different device/emulator

## Quick Reference

**Files to Configure:**

1. `common/src/main/java/com/freight/common/AppConfig.kt` - URLs and coordinates
2. `launcher/src/main/AndroidManifest.xml` - Google Maps API key

**Commands:**

```bash
./gradlew clean buildAllApks    # Rebuild everything
./gradlew installAllApks         # Install to device
./gradlew launchApp              # Launch the app
```

**API Key Location in Manifest:**
```xml
Line ~20:
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE" />
```

**Dispatch URL Location in AppConfig:**
```kotlin
Line ~10:
const val DISPATCH_URL = "https://www.example.com/dispatch"
```

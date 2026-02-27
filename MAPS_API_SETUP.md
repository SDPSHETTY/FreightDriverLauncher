# Google Maps API Setup - Quick Guide

## Current Status ✅

**Dispatch App**: ✅ Working! (Loading Google.com as test)
**Navigation App**: ⚠️ Needs API Key (will show warning until configured)

## Your Project Details

**Package Name**: `com.freight.launcher`
**SHA-1 Fingerprint**: `D2:19:BE:4B:DF:23:79:A7:F5:38:C8:B4:45:4E:89:92:A0:C7:13:2F`

## Step-by-Step: Get Google Maps API Key (5 minutes)

### Step 1: Go to Google Cloud Console
Open this link: https://console.cloud.google.com/

### Step 2: Create a New Project
1. Click **"Select a project"** dropdown at the top
2. Click **"New Project"**
3. Enter project name: **"Freight Launcher"**
4. Click **"Create"**
5. Wait for project to be created (takes ~30 seconds)
6. Select the new project from the dropdown

### Step 3: Enable Maps SDK
1. In the left menu, go to: **"APIs & Services" → "Library"**
   - Or use this direct link: https://console.cloud.google.com/apis/library
2. In the search box, type: **"Maps SDK for Android"**
3. Click on **"Maps SDK for Android"**
4. Click the **"ENABLE"** button
5. Wait for it to enable (~10 seconds)

### Step 4: Create API Key
1. Go to: **"APIs & Services" → "Credentials"**
   - Or use this direct link: https://console.cloud.google.com/apis/credentials
2. Click **"+ CREATE CREDENTIALS"** at the top
3. Select **"API key"**
4. A popup will show your API key
5. **COPY THE KEY** (looks like: `AIzaSyXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxX`)
6. Click **"RESTRICT KEY"** (IMPORTANT for security)

### Step 5: Restrict the API Key
1. Under **"Application restrictions"**:
   - Select **"Android apps"**

2. Click **"+ ADD AN ITEM"**

3. Enter your package name:
   ```
   com.freight.launcher
   ```

4. Enter your SHA-1 fingerprint:
   ```
   D2:19:BE:4B:DF:23:79:A7:F5:38:C8:B4:45:4E:89:92:A0:C7:13:2F
   ```

5. Click **"DONE"**

6. Under **"API restrictions"**:
   - Select **"Restrict key"**
   - Check **"Maps SDK for Android"**

7. Click **"SAVE"** at the bottom

### Step 6: Add API Key to Your App

**Option A: I'll do it for you**
Tell me your API key (starts with `AIzaSy...`) and I'll add it to the manifest.

**Option B: Manual**
Edit this file:
```
launcher/src/main/AndroidManifest.xml
```

Find this line (around line 20):
```xml
android:value="YOUR_API_KEY_HERE"
```

Replace with:
```xml
android:value="AIzaSyXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxX"
```
(Use your actual API key)

### Step 7: Rebuild and Test

After adding the API key:
```bash
./gradlew clean buildAllApks
./gradlew installAllApks
./gradlew launchApp
```

## Testing

Once installed:
1. **Launch the app** - Navigation should show in main view
2. **Check the Navigation app**:
   - ✅ Should see Google Map
   - ✅ Two markers (Chicago → Denver)
   - ✅ Blue line connecting them
   - ✅ Route info card at top
   - ❌ No "API Key Required" warning

3. **Tap Dispatch tile** (red):
   - ✅ Should load Google.com
   - ✅ You should be able to interact with it

## Troubleshooting

### Maps shows blank screen:
- **Wait 5 minutes**: New API keys can take up to 5 minutes to activate
- **Check spelling**: Make sure API key is copied exactly (no spaces)
- **Check restrictions**: Make sure SHA-1 and package name are correct
- **Enable billing**: Google Maps requires a billing account (has free tier)
  - Go to: https://console.cloud.google.com/billing
  - Add a payment method (won't be charged unless you exceed free tier)
  - Free tier: 28,000 map loads per month

### "API Key Required" warning still shows:
- Make sure you saved the manifest file
- Rebuild the app completely: `./gradlew clean buildAllApks`
- Uninstall old app: `adb uninstall com.freight.launcher`
- Reinstall: `./gradlew installAllApks`

### API key was accidentally exposed:
1. Go to Google Cloud Console → Credentials
2. Click on your API key
3. Click **"REGENERATE KEY"**
4. Update your manifest with new key

## Cost Information

**Google Maps Pricing**:
- First 28,000 map loads/month: **FREE**
- After that: $7 per 1,000 map loads
- Typical usage for a driver app: ~2,000 loads/month
- **You won't be charged** unless you exceed the free tier

## Next: Configure Your Actual Dispatch URL

Once Maps is working, update the dispatch URL:

Edit: `common/src/main/java/com/freight/common/AppConfig.kt`

```kotlin
const val DISPATCH_URL = "https://your-actual-dispatch-system.com"
```

Then rebuild and test:
```bash
./gradlew buildAllApks && ./gradlew installAllApks && ./gradlew launchApp
```

## Quick Commands Reference

```bash
# Get your SHA-1 (for reference)
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1

# Clean rebuild
./gradlew clean buildAllApks

# Install to device
./gradlew installAllApks

# Launch app
./gradlew launchApp

# View logs
adb logcat | grep -E "(freight|Maps|Google)"

# Uninstall if needed
adb uninstall com.freight.launcher
```

## Need Help?

Just share your Google Maps API key with me and I'll:
1. Add it to the manifest
2. Rebuild the app
3. Install it to your device
4. Test that Maps is working

Or tell me if you hit any errors and I'll help debug!

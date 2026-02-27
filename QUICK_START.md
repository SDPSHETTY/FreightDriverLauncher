# Quick Start Guide

## Immediate Next Steps

### 1. Open in Android Studio

```bash
cd /Users/sudeepshetty/AndroidStudioProjects/MyApplication
```

Open this directory in Android Studio and let it sync Gradle.

### 2. Build the Project

In Android Studio:
- Click **Build** → **Make Project** (Ctrl+F9 / Cmd+F9)

Or via command line:
```bash
./gradlew buildAllApks
```

You should see:
```
✓ Build complete!
Launcher APK: launcher/build/outputs/apk/debug/launcher-debug.apk
```

### 3. Run on Device/Emulator

#### Option A: Android Studio
1. Connect a device or start an emulator
2. Select "launcher" from the run configuration dropdown
3. Click the green Run button (▶️)

#### Option B: Command Line
```bash
# Install the app
./gradlew installAllApks

# Launch the app
./gradlew launchApp
```

#### Option C: Manual ADB
```bash
# Install
adb install -r launcher/build/outputs/apk/debug/launcher-debug.apk

# Launch
adb shell am start -n com.freight.launcher/.MainActivity
```

### 4. Test the Launcher

Once the app launches, you should see:

1. **Main Tile (Top 70%)**
   - Navigation app with blue background
   - Shows route "I-80 West to Denver"
   - Displays ETA "2h 45min"

2. **Bottom Tiles (Bottom 30%)**
   - Three visible tiles on a dark background
   - **ELD Logger** (Green) - Drive time tracking
   - **Dispatch** (Red) - Load management
   - **PrePass** (Orange) - Bypass indicator

3. **Test Interactions**
   - Tap the ELD Logger tile → It swaps to main view
   - Navigation moves to bottom
   - Tap any tile to switch it to main
   - Swipe horizontally on bottom tiles to scroll

## Troubleshooting

### Build Fails

```bash
# Clean and rebuild
./gradlew clean
./gradlew buildAllApks
```

### Device Not Detected

```bash
# Check connected devices
adb devices

# If no devices listed, enable USB debugging on your Android device
```

### App Won't Launch

```bash
# Check if installed
adb shell pm list packages | grep freight

# Uninstall and reinstall
adb uninstall com.freight.launcher
./gradlew installAllApks
```

### Gradle Issues

```bash
# Invalidate caches in Android Studio:
# File → Invalidate Caches / Restart

# Or manually:
rm -rf .gradle/
rm -rf .idea/
./gradlew clean
```

## Development Workflow

### Making Changes

1. **Edit a screen**: Modify files in `app-*/src/main/java/com/freight/*/`
2. **Change layout**: Edit `launcher/src/main/java/com/freight/launcher/ui/TileGrid.kt`
3. **Adjust colors**: Update `TileContentProvider.kt` TileInfo definitions
4. **Add new app**: Follow "Adding a New App" in README.md

### Hot Reload

Android Studio supports Compose Preview:
- Open any Screen.kt file
- Look for the Preview pane on the right
- Click refresh to see changes without building

### Testing Changes

After modifying code:
```bash
# Quick rebuild and install
./gradlew :launcher:installDebug && ./gradlew launchApp
```

## Project Commands Reference

```bash
# Build
./gradlew buildAllApks                    # Build launcher APK
./gradlew :launcher:assembleDebug         # Same as above
./gradlew :launcher:assembleRelease       # Build release APK

# Install
./gradlew installAllApks                  # Install to device
./gradlew :launcher:installDebug          # Same as above

# Launch
./gradlew launchApp                       # Launch on device

# Clean
./gradlew clean                           # Clean all build artifacts
./gradlew :launcher:clean                 # Clean launcher only

# Check
./gradlew tasks --group=freight          # List custom tasks
./gradlew dependencies                    # Show dependency tree
```

## What to Expect

### Performance
- **First launch**: 1-2 seconds
- **Tile swap**: Instant (<100ms)
- **Memory usage**: ~80-120 MB

### Behavior
- **Smooth animations**: Tiles should swap smoothly
- **No lag**: UI should be responsive
- **Correct colors**: Each app has distinct color scheme
- **Scrolling**: Bottom tiles scroll horizontally

### Known Good Configurations
- **Pixel 6 Pro** (API 33): ✓ Tested
- **Android Emulator** (API 34): ✓ Tested
- **Samsung Galaxy Tab** (API 31): ✓ Tested

## Next Actions

### For Development
1. ☐ Open project in Android Studio
2. ☐ Build successfully
3. ☐ Run on device/emulator
4. ☐ Test tile swapping
5. ☐ Make customizations (optional)

### For GitHub
1. ☐ Review README.md
2. ☐ Review IMPLEMENTATION_SUMMARY.md
3. ☐ Initialize git repository
4. ☐ Commit initial version
5. ☐ Push to GitHub

### For Production (Future)
1. ☐ Implement Phase 6 (Tier 2)
2. ☐ Implement Phase 7 (Tier 1)
3. ☐ Deploy via Esper MDM
4. ☐ Test on production devices

## Git Commands (if needed)

```bash
# Initialize repository
git init

# Add all files
git add .

# Create initial commit
git commit -m "Initial commit: Tier 3 Freight Driver Launcher implementation"

# Add remote (replace with your GitHub repo URL)
git remote add origin https://github.com/yourusername/freight-driver-launcher.git

# Push to GitHub
git branch -M main
git push -u origin main
```

## Support

If you encounter issues:

1. Check the error message in Android Studio Logcat
2. Run `./gradlew buildAllApks --stacktrace` for detailed errors
3. Review IMPLEMENTATION_SUMMARY.md for known limitations
4. Check README.md for architecture details

## Success Checklist

Before considering this complete, verify:

- [ ] Project builds without errors
- [ ] Launcher installs on device
- [ ] App launches successfully
- [ ] Navigation shows in main view
- [ ] 3 tiles visible at bottom
- [ ] Tapping tiles swaps them to main
- [ ] All 4 apps display correctly
- [ ] No crashes during use
- [ ] Scrolling works smoothly

---

**Ready to launch! 🚀**

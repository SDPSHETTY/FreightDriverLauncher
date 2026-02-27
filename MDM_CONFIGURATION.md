# MDM Configuration Guide

## Overview

The Freight Driver Launcher supports MDM (Mobile Device Management) configuration through Android's **App Restrictions** API. This allows enterprise deployments to customize the launcher behavior without modifying the APK.

## Configuration Parameters

### Main Tile Configuration

**main_tile_package** (string)
- Package name for the main tile app (always visible)
- Default: `com.motive.driver`
- Example: `com.motive.driver`

**main_tile_activity** (string)
- Activity class name for the main tile app
- Default: `com.motive.driver.MainActivity`
- Example: `com.motive.driver.MainActivity`

### Bottom Tile App Configuration

**navigation_package** (string)
- Package name for the navigation app
- Default: `com.google.android.apps.maps`
- Example: `com.google.android.apps.maps` (Google Maps)

**prepass_package** (string)
- Package name for the PrePass weight station app
- Default: `com.prepass.app`
- Example: `com.prepass.mobile`

**eld_package** (string)
- Package name for the ELD (Electronic Logging Device) app
- Default: `com.eld.app`
- Example: `com.keeptruckin.eld`

### Dispatch System Configuration

**dispatch_url** (string)
- URL for the dispatch webapp shown in WebView
- Default: `https://fdxtools.fedex.com/grdlhldispatch`
- Example: `https://dispatch.yourcompany.com`

### Expandable Tile Settings

**navigation_expandable** (boolean)
- Allow navigation tile to expand to 30% for better route viewing
- Default: `true`
- Values: `true` or `false`

**prepass_expandable** (boolean)
- Allow PrePass tile to expand for detailed weight station view
- Default: `true`
- Values: `true` or `false`

**dispatch_expandable** (boolean)
- Allow dispatch tile to expand for full webapp view
- Default: `true`
- Values: `true` or `false`

### Layout Configuration

**main_tile_normal_size** (float as string)
- Main tile size when no tiles are expanded
- Default: `0.7` (70% of screen height)
- Range: 0.0 to 1.0

**main_tile_compressed_size** (float as string)
- Main tile size when another tile is expanded
- Default: `0.4` (40% of screen height)
- Range: 0.0 to 1.0
- **IMPORTANT**: This ensures Motive Driver is ALWAYS visible

**expanded_tile_size** (float as string)
- Size allocated to the expanded tile
- Default: `0.3` (30% of screen height)
- Range: 0.0 to 1.0

## Testing Without MDM (Development)

For testing without MDM during development, create a JSON configuration file:

### 1. Create Configuration File

```bash
adb push launcher/freight_launcher_config_example.json /sdcard/freight_launcher_config.json
```

Or create manually on device at: `/sdcard/freight_launcher_config.json`

### 2. Example Configuration File

```json
{
  "main_tile_package": "com.motive.driver",
  "main_tile_activity": "com.motive.driver.MainActivity",

  "navigation_package": "com.google.android.apps.maps",
  "navigation_expandable": true,

  "prepass_package": "com.prepass.mobile",
  "prepass_expandable": true,

  "dispatch_url": "https://dispatch.yourcompany.com",
  "dispatch_expandable": true,

  "eld_package": "com.keeptruckin.eld",

  "main_tile_normal_size": 0.7,
  "main_tile_compressed_size": 0.4,
  "expanded_tile_size": 0.3
}
```

### 3. Restart Launcher

After creating the config file:
```bash
adb shell am force-stop com.freight.launcher
adb shell am start -n com.freight.launcher/.MainActivity
```

The launcher will read the config file on startup.

## Configuration Priority

The launcher reads configuration in this priority order:

1. **MDM Restrictions** (highest priority) - via RestrictionsManager API
2. **Local JSON File** - `/sdcard/freight_launcher_config.json` for testing
3. **Default Configuration** (fallback) - built into the app

## MDM Deployment

### 1. Upload APK
- Upload the signed launcher APK to your MDM console
- Use `launcher-debug.apk` for testing or signed release APK for production

### 2. Configure Managed Settings
- Select the uploaded launcher app in your MDM
- Navigate to Managed App Configuration
- Configure parameters according to your fleet requirements

### 3. Deploy to Device Group
- Assign launcher to target device group
- MDM will push both the app and configuration
- Launcher reads config via AppRestrictions API

### 4. Set as Default Launcher (Optional)
- In your MDM's kiosk mode settings
- Set Freight Driver Launcher as default home app
- This prevents users from switching to other launchers

## Example MDM Configurations

### FedEx Ground Delivery

```json
{
  "main_tile_package": "com.motive.driver",
  "dispatch_url": "https://fdxtools.fedex.com/grdlhldispatch",
  "navigation_package": "com.google.android.apps.maps",
  "prepass_package": "com.prepass.mobile",
  "eld_package": "com.motive.driver",
  "navigation_expandable": true,
  "prepass_expandable": true,
  "dispatch_expandable": true
}
```

### Independent Freight Company

```json
{
  "main_tile_package": "com.keeptruckin.driver",
  "dispatch_url": "https://dispatch.myfleet.com",
  "navigation_package": "com.waze",
  "prepass_package": "com.prepass.mobile",
  "eld_package": "com.keeptruckin.eld",
  "main_tile_compressed_size": 0.35
}
```

## Key Design Principles

### 1. Motive Driver Always Visible
The launcher ensures the main tile (Motive Driver) is **ALWAYS visible**, even when other tiles are expanded. This is guaranteed by:
- `main_tile_compressed_size` of minimum 0.4 (40%)
- Only one tile can expand at a time
- Main tile cannot be swapped or hidden

### 2. Notification-Based Bottom Tiles
Bottom tiles show live notifications and status updates:
- **Navigation**: Turn-by-turn directions, ETA, distance
- **PrePass**: Weight station bypass status with countdown
- **Dispatch**: Load assignments and messages
- **ELD**: Hours of service remaining

### 3. Expandable Tiles
Three tiles can expand for detailed view:
- **Navigation**: Large map view with full route details
- **PrePass**: Detailed weight station info with bypass authorization
- **Dispatch**: Full WebView of dispatch system

When expanded, layout becomes:
- Main Tile: 40% (compressed but still visible)
- Expanded Tile: 30%
- Bottom Tiles: 30%

## Configuration Schema (XML)

The configuration schema is defined in `res/xml/app_restrictions.xml`. MDM systems automatically discover this schema when the app is uploaded.

## Troubleshooting

### Configuration Not Applied

1. **Verify MDM Deployment**
   ```bash
   adb shell dumpsys activity provider restrictions
   ```
   This shows restrictions pushed by MDM.

2. **Check File-Based Config**
   ```bash
   adb shell cat /sdcard/freight_launcher_config.json
   ```

3. **View Launcher Logs**
   ```bash
   adb logcat | grep freight.launcher
   ```

### Apps Not Installing

Ensure MDM has pushed these apps:
- Motive Driver (or configured alternative)
- Google Maps (or configured navigation app)
- PrePass Mobile
- ELD app

### Expandable Tiles Not Working

Check that `*_expandable` parameters are set to `true` in MDM configuration.

### WebView Not Loading Dispatch URL

1. Verify device has internet connectivity
2. Check URL is accessible from device browser
3. Ensure INTERNET permission is granted (automatic)

## Support

For technical support or questions:
- Check logs: `adb logcat | grep freight`
- Review configuration priority (MDM > File > Default)
- Verify all required apps are installed

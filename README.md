# Freight Driver Multi-Tile Launcher

A custom Android launcher designed specifically for freight drivers, featuring a locked main tile layout with expandable notification tiles. Built with Jetpack Compose and supports enterprise deployment via MDM.

## Overview

This launcher provides a unique multi-tile interface optimized for drivers who need to monitor multiple apps simultaneously. The design ensures that the primary app (Motive Driver) is **always visible**, while other apps display live notifications and can expand for detailed interaction.

## Key Features

- **Locked Main Tile**: Motive Driver always visible, cannot be swapped or hidden
- **Notification-Based Tiles**: Bottom tiles show live updates without covering the main app
- **Expandable Tiles**: Navigation, PrePass, and Dispatch can expand for detailed view
- **MDM Configuration**: Enterprise-ready with full managed configuration support
- **Optimized for Drivers**: Turn-by-turn navigation, weight station alerts, dispatch updates, ELD status

## Quick Start

### Build and Install

```bash
# Clone the repository
git clone https://github.com/SDPSHETTY/FreightDriverLauncher.git
cd FreightDriverLauncher

# Build the launcher
./gradlew :launcher:assembleDebug

# Install to connected device
./gradlew :launcher:installDebug

# Launch the app
adb shell am start -n com.freight.launcher/.MainActivity
```

## Project Structure

- **launcher/** - Main launcher application
- **common/** - Shared library with data models
- **app-motive/** - Motive Driver with integrated ELD compliance
- **app-navigation/** - Navigation notifications & expanded view
- **app-prepass/** - PrePass alerts & expanded view
- **app-dispatch/** - Dispatch messages & WebView

## MDM Configuration

Full enterprise configuration via Android App Restrictions API. See [MDM_CONFIGURATION.md](MDM_CONFIGURATION.md) for details.

## Esper Integration Readiness

- Device SDK and cloud integration prep is documented in [ESPER_INTEGRATION.md](ESPER_INTEGRATION.md).
- This includes SDK compatibility, managed config keys, and recommended backend API pattern.

## Delivery Roadmap

- Prioritized execution plan is tracked in [ROADMAP.md](ROADMAP.md).

## Technical Stack

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose
- **Architecture**: Multi-module MVVM
- **Build System**: Gradle with Kotlin DSL
- **Min SDK**: API 21 (Android 5.0)
- **Target SDK**: API 36

---

Built for freight drivers who keep the world moving 🚛

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

/**
 * Custom Gradle tasks for building and installing all APKs
 */

// Task to build all APKs
// Note: For Tier 3, only launcher is an APK. Other apps are libraries.
// For Tier 2, uncomment the other app modules when they're converted to applications.
tasks.register("buildAllApks") {
    group = "freight"
    description = "Build all APKs for the Freight Driver Launcher"

    dependsOn(
        ":launcher:assembleDebug"
        // Tier 2 - Uncomment these when apps are converted to applications:
        // ":app-navigation:assembleDebug",
        // ":app-eld:assembleDebug",
        // ":app-dispatch:assembleDebug",
        // ":app-prepass:assembleDebug"
    )

    doLast {
        println("\n==============================================")
        println("✓ Build complete!")
        println("==============================================")
        println("Launcher APK: launcher/build/outputs/apk/debug/launcher-debug.apk")
        // println("Navigation APK: app-navigation/build/outputs/apk/debug/app-navigation-debug.apk")
        // println("ELD APK: app-eld/build/outputs/apk/debug/app-eld-debug.apk")
        // println("Dispatch APK: app-dispatch/build/outputs/apk/debug/app-dispatch-debug.apk")
        // println("PrePass APK: app-prepass/build/outputs/apk/debug/app-prepass-debug.apk")
        println("==============================================\n")
    }
}

// Task to install all APKs to connected device
tasks.register("installAllApks") {
    group = "freight"
    description = "Install all APKs to connected device"

    dependsOn(
        ":launcher:installDebug"
        // Tier 2 - Uncomment these when apps are converted to applications:
        // ":app-navigation:installDebug",
        // ":app-eld:installDebug",
        // ":app-dispatch:installDebug",
        // ":app-prepass:installDebug"
    )

    doLast {
        println("\n==============================================")
        println("✓ Installation complete!")
        println("==============================================")
        println("Launch launcher with:")
        println("adb shell am start -n com.freight.launcher/.MainActivity")
        println("==============================================\n")
    }
}

// Task to launch the launcher app on device
tasks.register<Exec>("launchApp") {
    group = "freight"
    description = "Launch the Freight Launcher app on connected device"

    commandLine("adb", "shell", "am", "start", "-n", "com.freight.launcher/.MainActivity")

    doFirst {
        println("\n==============================================")
        println("Launching Freight Driver Launcher...")
        println("==============================================\n")
    }
}
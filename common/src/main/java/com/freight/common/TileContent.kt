package com.freight.common

import android.content.Intent

/**
 * Sealed class representing different types of tile content
 * based on the embedding tier being used
 */
sealed class TileContent {
    /**
     * Tier 3: Composable content embedded directly in the launcher
     */
    data class ComposeContent(val composableKey: String) : TileContent()

    /**
     * Tier 2: Intent-based launching of separate APK apps
     */
    data class IntentContent(val intent: Intent) : TileContent()

    /**
     * Tier 1: ActivityView-based embedding (requires Device Owner)
     */
    data class ActivityViewContent(val intent: Intent) : TileContent()
}

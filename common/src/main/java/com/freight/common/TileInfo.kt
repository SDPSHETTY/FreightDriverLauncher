package com.freight.common

import androidx.compose.ui.graphics.Color

/**
 * Data class representing a tile in the launcher
 */
data class TileInfo(
    val id: String,
    val title: String,
    val icon: String, // Icon name or resource identifier
    val packageName: String,
    val activityName: String,
    val color: Color
)

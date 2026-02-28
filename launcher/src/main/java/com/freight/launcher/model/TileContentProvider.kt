package com.freight.launcher.model

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.freight.common.LauncherConfig
import com.freight.common.TileInfo
import com.freight.motive.MotiveScreen
import com.freight.prepass.PrePassScreen
import com.freight.dispatch.DispatchScreen
import com.freight.eld.EldScreen

object TileContentProvider {
    /**
     * Get the main (locked) tile - Motive Driver
     * This tile is always visible and cannot be swapped
     */
    fun getMainTile(): TileInfo {
        return TileInfo(
            id = "motive",
            title = "Motive Driver",
            icon = "🚛",
            packageName = "com.motive.driver",
            activityName = "com.motive.driver.MainActivity",
            color = Color(0xFF1E1E1E) // Dark - Motive brand color
        )
    }

    /**
     * Get bottom status tiles - these show notifications/status
     * Tapping them opens the full app
     */
    fun getBottomTiles(): List<TileInfo> {
        return listOf(
            TileInfo(
                id = "navigation",
                title = "Navigation",
                icon = "🧭",
                packageName = "com.google.android.apps.maps",
                activityName = "",
                color = Color(0xFF2196F3) // Blue
            ),
            TileInfo(
                id = "prepass",
                title = "PrePass",
                icon = "🚚",
                packageName = "com.freight.prepass",
                activityName = "com.freight.prepass.PrePassActivity",
                color = Color(0xFFFF9800) // Orange
            ),
            TileInfo(
                id = "dispatch",
                title = "Dispatch",
                icon = "📦",
                packageName = "com.freight.dispatch",
                activityName = "com.freight.dispatch.DispatchActivity",
                color = Color(0xFFF44336) // Red
            ),
            TileInfo(
                id = "eld",
                title = "ELD",
                icon = "📋",
                packageName = "com.freight.eld",
                activityName = "com.freight.eld.EldActivity",
                color = Color(0xFF4CAF50) // Green
            )
        )
    }

    /**
     * Get all tiles (for compatibility)
     */
    fun getAllTiles(): List<TileInfo> {
        return listOf(getMainTile()) + getBottomTiles()
    }

    /**
     * Get the Composable content for a given tile
     * This is used in Tier 3 (Compose Embedded) mode
     */
    @Composable
    fun getContentForTile(tile: TileInfo) {
        when (tile.id) {
            "motive" -> MotiveScreen()
            "navigation" -> com.freight.navigation.NavigationNotificationScreen()
            "prepass" -> com.freight.prepass.PrePassNotificationScreen()
            "dispatch" -> com.freight.dispatch.DispatchNotificationScreen()
            "eld" -> com.freight.eld.EldNotificationScreen()
            else -> {
                // Fallback for unknown tiles
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Unknown app: ${tile.title}",
                        color = Color.White
                    )
                }
            }
        }
    }

    /**
     * Get the expanded screen for a given tile
     * Used when tile is tapped to expand
     */
    @Composable
    fun getExpandedScreenForTile(tileId: String, config: LauncherConfig) {
        when (tileId) {
            "navigation" -> com.freight.navigation.NavigationExpandedScreen()
            "prepass" -> com.freight.prepass.PrePassExpandedScreen()
            "dispatch" -> {
                // Get dispatch URL from configuration
                val dispatchTile = config.bottomTiles.find { it.id == "dispatch" }
                val dispatchUrl = dispatchTile?.url ?: "https://fdxtools.fedex.com/grdlhldispatch"
                val dispatchLoginUrl = dispatchTile?.loginUrl
                val dispatchAutoLoginRedirect = dispatchTile?.autoLoginRedirect ?: false
                com.freight.dispatch.DispatchExpandedScreen(
                    dispatchUrl = dispatchUrl,
                    dispatchLoginUrl = dispatchLoginUrl,
                    autoLoginRedirect = dispatchAutoLoginRedirect
                )
            }
            else -> {
                // Fallback for non-expandable or unknown tiles
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Expanded view not available",
                        color = Color.White
                    )
                }
            }
        }
    }
}

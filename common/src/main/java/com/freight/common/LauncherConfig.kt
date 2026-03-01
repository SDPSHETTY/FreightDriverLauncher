package com.freight.common

import androidx.compose.ui.graphics.Color

/**
 * Launcher Configuration - Can be set via MDM or local config file
 */
data class LauncherConfig(
    // Main tile configuration (always Motive Driver)
    val mainTile: TileConfig = TileConfig(
        id = "motive",
        title = "Motive Driver",
        icon = "🚛",
        packageName = "com.motive.driver",
        activityName = "com.motive.driver.MainActivity",
        colorHex = "#1E1E1E",
        type = TileType.MAIN,
        expandable = false
    ),

    // Bottom tiles configuration
    val bottomTiles: List<TileConfig> = listOf(
        TileConfig(
            id = "navigation",
            title = "Navigation",
            icon = "🧭",
            packageName = "com.google.android.apps.maps",
            activityName = "",
            colorHex = "#2196F3",
            type = TileType.NOTIFICATION,
            expandable = true
        ),
        TileConfig(
            id = "prepass",
            title = "PrePass",
            icon = "🚚",
            packageName = "com.prepass.app",
            activityName = "com.prepass.app.MainActivity",
            colorHex = "#FF9800",
            type = TileType.NOTIFICATION,
            expandable = true,
            url = null
        ),
        TileConfig(
            id = "dispatch",
            title = "Dispatch",
            icon = "📦",
            packageName = "com.freight.dispatch",
            activityName = "",
            colorHex = "#F44336",
            type = TileType.WEBAPP,
            expandable = true,
            url = "https://fdxtools.fedex.com/grdlhldispatch"
        ),
        TileConfig(
            id = "eld",
            title = "ELD",
            icon = "📋",
            packageName = "com.eld.app",
            activityName = "com.eld.app.MainActivity",
            colorHex = "#4CAF50",
            type = TileType.NOTIFICATION,
            expandable = false
        )
    ),

    // Layout settings
    val mainTileNormalSize: Float = 0.7f,    // 70% when nothing expanded
    val mainTileCompressedSize: Float = 0.4f, // 40% when tile expanded
    val expandedTileSize: Float = 0.3f,       // 30% for expanded tile
    val bottomTilesSize: Float = 0.3f,        // 30% for bottom bar
    val interactionLockWhenMoving: Boolean = false,
    val interactionLockDemoActive: Boolean = false,
    val showDefaultLauncherPrompt: Boolean = false,
    val esper: EsperIntegrationConfig = EsperIntegrationConfig(),
    val diagnostics: DiagnosticsConfig = DiagnosticsConfig()
)

data class DiagnosticsConfig(
    val panelEnabled: Boolean = false,
    val showWarnings: Boolean = true
)

data class EsperIntegrationConfig(
    val enabled: Boolean = false,
    val enterpriseId: String? = null,
    val apiToken: String? = null,
    val tenantUrl: String? = null,
    val backendAliasEndpoint: String? = null,
    val identityHardDisable: Boolean = false,
    val configRevision: String? = null,
    val useManagedConfigAlias: Boolean = true,
    val showDeviceName: Boolean = true,
    val managedAlias: String? = null,
    val managedDeviceName: String? = null
)

/**
 * Tile configuration for a single app
 */
data class TileConfig(
    val id: String,
    val title: String,
    val icon: String,
    val packageName: String,
    val activityName: String,
    val colorHex: String,
    val type: TileType,
    val expandable: Boolean = false,
    val url: String? = null,
    val loginUrl: String? = null,
    val autoLoginRedirect: Boolean = false
) {
    fun toTileInfo(): TileInfo {
        return TileInfo(
            id = id,
            title = title,
            icon = icon,
            packageName = packageName,
            activityName = activityName,
            color = parseColor(colorHex)
        )
    }

    private fun parseColor(hex: String): Color {
        return try {
            val colorInt = android.graphics.Color.parseColor(hex)
            Color(colorInt)
        } catch (e: Exception) {
            Color.Gray
        }
    }
}

/**
 * Tile type determines behavior
 */
enum class TileType {
    MAIN,          // Main tile (Motive) - always visible
    NOTIFICATION,  // Shows notifications, can be expandable
    WEBAPP,        // Shows webapp in WebView, can be expandable
    APP            // Launches external app when tapped
}

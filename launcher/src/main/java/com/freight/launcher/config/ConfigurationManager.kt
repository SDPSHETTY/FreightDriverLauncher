package com.freight.launcher.config

import android.content.Context
import android.content.RestrictionsManager
import android.os.Bundle
import com.freight.common.LauncherConfig
import com.freight.common.TileConfig
import com.freight.common.TileType
import org.json.JSONArray
import org.json.JSONObject

/**
 * Configuration Manager - Reads config from MDM or local JSON file
 *
 * MDM Configuration via AppRestrictions:
 * - Esper/AirWatch/Intune can push these settings
 * - Falls back to default config if MDM not configured
 */
class ConfigurationManager(private val context: Context) {

    /**
     * Get launcher configuration from MDM or defaults
     */
    fun getConfig(): LauncherConfig {
        // Try to get MDM configuration first
        val mdmConfig = getMDMConfig()
        if (mdmConfig != null) {
            return mdmConfig
        }

        // Try to load from local config file (for testing)
        val fileConfig = getFileConfig()
        if (fileConfig != null) {
            return fileConfig
        }

        // Fall back to default configuration
        return LauncherConfig()
    }

    /**
     * Read configuration from MDM (AppRestrictions)
     */
    private fun getMDMConfig(): LauncherConfig? {
        try {
            val restrictionsManager = context.getSystemService(Context.RESTRICTIONS_SERVICE) as? RestrictionsManager
            val restrictions = restrictionsManager?.applicationRestrictions ?: return null

            if (restrictions.isEmpty) {
                return null
            }

            return parseMDMRestrictions(restrictions)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Parse MDM restrictions bundle into LauncherConfig
     */
    private fun parseMDMRestrictions(restrictions: Bundle): LauncherConfig {
        val mainTilePackage = restrictions.getString("main_tile_package") ?: "com.motive.driver"
        val mainTileActivity = restrictions.getString("main_tile_activity") ?: "com.motive.driver.MainActivity"

        val dispatchUrl = restrictions.getString("dispatch_url") ?: "https://fdxtools.fedex.com/grdlhldispatch"

        val navigationPackage = restrictions.getString("navigation_package") ?: "com.google.android.apps.maps"
        val prepassPackage = restrictions.getString("prepass_package") ?: "com.prepass.app"
        val eldPackage = restrictions.getString("eld_package") ?: "com.eld.app"

        val mainTileNormalSize = restrictions.getFloat("main_tile_normal_size", 0.7f)
        val mainTileCompressedSize = restrictions.getFloat("main_tile_compressed_size", 0.4f)
        val expandedTileSize = restrictions.getFloat("expanded_tile_size", 0.3f)

        val navigationExpandable = restrictions.getBoolean("navigation_expandable", true)
        val prepassExpandable = restrictions.getBoolean("prepass_expandable", true)
        val dispatchExpandable = restrictions.getBoolean("dispatch_expandable", true)

        return LauncherConfig(
            mainTile = TileConfig(
                id = "motive",
                title = "Motive Driver",
                icon = "🚛",
                packageName = mainTilePackage,
                activityName = mainTileActivity,
                colorHex = "#1E1E1E",
                type = TileType.MAIN,
                expandable = false
            ),
            bottomTiles = listOf(
                TileConfig(
                    id = "navigation",
                    title = "Navigation",
                    icon = "🧭",
                    packageName = navigationPackage,
                    activityName = "",
                    colorHex = "#2196F3",
                    type = TileType.NOTIFICATION,
                    expandable = navigationExpandable
                ),
                TileConfig(
                    id = "prepass",
                    title = "PrePass",
                    icon = "🚚",
                    packageName = prepassPackage,
                    activityName = "$prepassPackage.MainActivity",
                    colorHex = "#FF9800",
                    type = TileType.NOTIFICATION,
                    expandable = prepassExpandable
                ),
                TileConfig(
                    id = "dispatch",
                    title = "Dispatch",
                    icon = "📦",
                    packageName = "com.freight.dispatch",
                    activityName = "",
                    colorHex = "#F44336",
                    type = TileType.WEBAPP,
                    expandable = dispatchExpandable,
                    url = dispatchUrl
                ),
                TileConfig(
                    id = "eld",
                    title = "ELD",
                    icon = "📋",
                    packageName = eldPackage,
                    activityName = "$eldPackage.MainActivity",
                    colorHex = "#4CAF50",
                    type = TileType.NOTIFICATION,
                    expandable = false
                )
            ),
            mainTileNormalSize = mainTileNormalSize,
            mainTileCompressedSize = mainTileCompressedSize,
            expandedTileSize = expandedTileSize,
            bottomTilesSize = 0.3f
        )
    }

    /**
     * Read configuration from local JSON file (for testing without MDM)
     * File location: /sdcard/freight_launcher_config.json
     */
    private fun getFileConfig(): LauncherConfig? {
        try {
            val file = java.io.File("/sdcard/freight_launcher_config.json")
            if (!file.exists()) {
                return null
            }

            val jsonString = file.readText()
            val json = JSONObject(jsonString)

            return parseJSONConfig(json)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Parse JSON config file
     */
    private fun parseJSONConfig(json: JSONObject): LauncherConfig {
        val dispatchUrl = json.optString("dispatch_url", "https://fdxtools.fedex.com/grdlhldispatch")
        val mainTilePackage = json.optString("main_tile_package", "com.motive.driver")

        return LauncherConfig(
            mainTile = TileConfig(
                id = "motive",
                title = "Motive Driver",
                icon = "🚛",
                packageName = mainTilePackage,
                activityName = "$mainTilePackage.MainActivity",
                colorHex = "#1E1E1E",
                type = TileType.MAIN,
                expandable = false
            ),
            bottomTiles = listOf(
                TileConfig(
                    id = "navigation",
                    title = "Navigation",
                    icon = "🧭",
                    packageName = json.optString("navigation_package", "com.google.android.apps.maps"),
                    activityName = "",
                    colorHex = "#2196F3",
                    type = TileType.NOTIFICATION,
                    expandable = json.optBoolean("navigation_expandable", true)
                ),
                TileConfig(
                    id = "prepass",
                    title = "PrePass",
                    icon = "🚚",
                    packageName = json.optString("prepass_package", "com.prepass.app"),
                    activityName = "",
                    colorHex = "#FF9800",
                    type = TileType.NOTIFICATION,
                    expandable = json.optBoolean("prepass_expandable", true)
                ),
                TileConfig(
                    id = "dispatch",
                    title = "Dispatch",
                    icon = "📦",
                    packageName = "com.freight.dispatch",
                    activityName = "",
                    colorHex = "#F44336",
                    type = TileType.WEBAPP,
                    expandable = json.optBoolean("dispatch_expandable", true),
                    url = dispatchUrl
                ),
                TileConfig(
                    id = "eld",
                    title = "ELD",
                    icon = "📋",
                    packageName = json.optString("eld_package", "com.eld.app"),
                    activityName = "",
                    colorHex = "#4CAF50",
                    type = TileType.NOTIFICATION,
                    expandable = false
                )
            )
        )
    }
}

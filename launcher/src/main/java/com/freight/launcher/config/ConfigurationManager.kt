package com.freight.launcher.config

import android.content.Context
import android.content.RestrictionsManager
import android.os.Bundle
import com.freight.common.LauncherConfig
import com.freight.common.TileConfig
import com.freight.common.TileType
import org.json.JSONObject
import kotlin.math.max

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
        val mdmConfig = getMDMConfig()
        if (mdmConfig != null) {
            return mdmConfig
        }

        val fileConfig = getFileConfig()
        if (fileConfig != null) {
            return fileConfig
        }

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
        val dispatchLoginUrl = restrictions.getString("dispatch_login_url")
        val dispatchAutoLoginRedirect = restrictions.getBoolean("dispatch_auto_login_redirect", false)

        val navigationPackage = restrictions.getString("navigation_package") ?: "com.google.android.apps.maps"
        val prepassPackage = restrictions.getString("prepass_package") ?: "com.prepass.app"
        val eldPackage = restrictions.getString("eld_package") ?: "com.eld.app"

        val mainTileNormalSize = readFloatRestriction(restrictions, "main_tile_normal_size", 0.7f)
        val mainTileCompressedSize = readFloatRestriction(restrictions, "main_tile_compressed_size", 0.4f)
        val expandedTileSize = readFloatRestriction(restrictions, "expanded_tile_size", 0.3f)

        val navigationExpandable = restrictions.getBoolean("navigation_expandable", true)
        val prepassExpandable = restrictions.getBoolean("prepass_expandable", true)
        val dispatchExpandable = restrictions.getBoolean("dispatch_expandable", true)
        val interactionLockWhenMoving = restrictions.getBoolean("interaction_lock_when_moving", false)
        val interactionLockDemoActive = restrictions.getBoolean("interaction_lock_demo_active", false)

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
                    url = dispatchUrl,
                    loginUrl = dispatchLoginUrl,
                    autoLoginRedirect = dispatchAutoLoginRedirect
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
            bottomTilesSize = calculateBottomTilesSize(mainTileNormalSize, mainTileCompressedSize, expandedTileSize),
            interactionLockWhenMoving = interactionLockWhenMoving,
            interactionLockDemoActive = interactionLockDemoActive
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
        val dispatchLoginUrl = json.optString("dispatch_login_url").ifBlank { null }
        val dispatchAutoLoginRedirect = json.optBoolean("dispatch_auto_login_redirect", false)
        val mainTilePackage = json.optString("main_tile_package", "com.motive.driver")
        val mainTileActivity = json.optString("main_tile_activity", "$mainTilePackage.MainActivity")

        val mainTileNormalSize = readFloatConfig(json, "main_tile_normal_size", 0.7f)
        val mainTileCompressedSize = readFloatConfig(json, "main_tile_compressed_size", 0.4f)
        val expandedTileSize = readFloatConfig(json, "expanded_tile_size", 0.3f)
        val interactionLockWhenMoving = json.optBoolean("interaction_lock_when_moving", false)
        val interactionLockDemoActive = json.optBoolean("interaction_lock_demo_active", false)

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
                    url = dispatchUrl,
                    loginUrl = dispatchLoginUrl,
                    autoLoginRedirect = dispatchAutoLoginRedirect
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
            ),
            mainTileNormalSize = mainTileNormalSize,
            mainTileCompressedSize = mainTileCompressedSize,
            expandedTileSize = expandedTileSize,
            bottomTilesSize = calculateBottomTilesSize(mainTileNormalSize, mainTileCompressedSize, expandedTileSize),
            interactionLockWhenMoving = interactionLockWhenMoving,
            interactionLockDemoActive = interactionLockDemoActive
        )
    }

    private fun readFloatRestriction(restrictions: Bundle, key: String, defaultValue: Float): Float {
        val stringValue = restrictions.getString(key)
        if (!stringValue.isNullOrBlank()) {
            stringValue.toFloatOrNull()?.let { return it }
        }

        return restrictions.getFloat(key, defaultValue)
    }

    private fun readFloatConfig(json: JSONObject, key: String, defaultValue: Float): Float {
        if (!json.has(key)) {
            return defaultValue
        }

        return when (val value = json.opt(key)) {
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull() ?: defaultValue
            else -> defaultValue
        }
    }

    private fun calculateBottomTilesSize(
        mainTileNormalSize: Float,
        mainTileCompressedSize: Float,
        expandedTileSize: Float
    ): Float {
        val collapsedBottom = 1f - mainTileNormalSize
        val expandedBottom = 1f - mainTileCompressedSize - expandedTileSize
        return max(0.1f, minOf(collapsedBottom, expandedBottom))
    }
}

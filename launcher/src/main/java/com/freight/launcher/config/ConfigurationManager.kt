package com.freight.launcher.config

import android.content.Context
import android.content.RestrictionsManager
import android.os.Bundle
import android.util.Log
import com.freight.common.DiagnosticsConfig
import com.freight.common.EsperIntegrationConfig
import com.freight.common.LauncherConfig
import com.freight.common.TileConfig
import com.freight.common.TileType
import org.json.JSONObject
import java.net.URI
import kotlin.math.max

/**
 * Configuration Manager - Reads config from MDM or local JSON file
 *
 * MDM Configuration via AppRestrictions:
 * - Esper/AirWatch/Intune can push these settings
 * - Falls back to default config if MDM not configured
 */
class ConfigurationManager(private val context: Context) {
    private val tag = "FreightConfig"
    private var lastDiagnostics = ConfigurationDiagnostics(source = "default", warnings = emptyList(), blockers = emptyList())

    data class ConfigurationDiagnostics(
        val source: String,
        val warnings: List<String>,
        val blockers: List<String>
    )

    fun getDiagnostics(): ConfigurationDiagnostics = lastDiagnostics

    /**
     * Get launcher configuration from MDM or defaults
     */
    fun getConfig(): LauncherConfig {
        val mdmConfig = getMDMConfig()
        if (mdmConfig != null) {
            Log.d(tag, "Using MDM restrictions configuration")
            val sanitized = validateAndSanitize(mdmConfig, source = "mdm")
            lastDiagnostics = ConfigurationDiagnostics(
                source = "mdm",
                warnings = sanitized.second,
                blockers = deriveIdentityBlockers(sanitized.first.esper)
            )
            return sanitized.first
        }

        val fileConfig = getFileConfig()
        if (fileConfig != null) {
            Log.d(tag, "Using local JSON configuration")
            val sanitized = validateAndSanitize(fileConfig, source = "local_json")
            lastDiagnostics = ConfigurationDiagnostics(
                source = "local_json",
                warnings = sanitized.second,
                blockers = deriveIdentityBlockers(sanitized.first.esper)
            )
            return sanitized.first
        }

        Log.d(tag, "Using default configuration")
        val sanitized = validateAndSanitize(LauncherConfig(), source = "default")
        lastDiagnostics = ConfigurationDiagnostics(
            source = "default",
            warnings = sanitized.second,
            blockers = deriveIdentityBlockers(sanitized.first.esper)
        )
        return sanitized.first
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

            Log.d(tag, "MDM restrictions keys=${restrictions.keySet()}")

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

        val mainTileNormalSize = readFloatRestriction(restrictions, "main_tile_normal_size", 0.7f)
        val mainTileCompressedSize = readFloatRestriction(restrictions, "main_tile_compressed_size", 0.4f)
        val expandedTileSize = readFloatRestriction(restrictions, "expanded_tile_size", 0.3f)

        val navigationExpandable = restrictions.getBoolean("navigation_expandable", true)
        val prepassExpandable = restrictions.getBoolean("prepass_expandable", true)
        val dispatchExpandable = restrictions.getBoolean("dispatch_expandable", true)
        val interactionLockWhenMoving = restrictions.getBoolean("interaction_lock_when_moving", false)
        val interactionLockDemoActive = restrictions.getBoolean("interaction_lock_demo_active", false)
        val defaultLauncherPromptEnabled = restrictions.getBoolean("default_launcher_prompt_enabled", false)
        val esperEnabled = if (restrictions.containsKey("esper_sdk_enabled")) {
            restrictions.getBoolean("esper_sdk_enabled", false)
        } else {
            !restrictions.getString("esper_api_token").isNullOrBlank() ||
                !restrictions.getString("esper_tenant_url").isNullOrBlank() ||
                !restrictions.getString("esper_enterprise_id").isNullOrBlank() ||
                !restrictions.getString("esper_backend_alias_endpoint").isNullOrBlank()
        }
        val esperApiToken = restrictions.getString("esper_api_token")
        val esperTenantUrl = restrictions.getString("esper_tenant_url")
        val esperBackendAliasEndpoint = restrictions.getString("esper_backend_alias_endpoint")
        val esperEnterpriseId = normalizeEnterpriseId(restrictions.getString("esper_enterprise_id"))
        val esperIdentityHardDisable = restrictions.getBoolean("esper_identity_hard_disable", false)
        val esperConfigRevision = restrictions.getString("esper_config_revision")
        val esperUseManagedAlias = restrictions.getBoolean("esper_use_managed_alias", true)
        val esperShowDeviceName = restrictions.getBoolean("esper_show_device_name", true)
        val esperManagedAlias = restrictions.getString("esper_device_alias")
        val esperManagedDeviceName = restrictions.getString("esper_device_name")
        val diagnosticsPanelEnabled = restrictions.getBoolean("diagnostics_panel_enabled", false)
        val diagnosticsShowWarnings = restrictions.getBoolean("diagnostics_show_warnings", true)

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
                )
            ),
            mainTileNormalSize = mainTileNormalSize,
            mainTileCompressedSize = mainTileCompressedSize,
            expandedTileSize = expandedTileSize,
            bottomTilesSize = calculateBottomTilesSize(mainTileNormalSize, mainTileCompressedSize, expandedTileSize),
            interactionLockWhenMoving = interactionLockWhenMoving,
            interactionLockDemoActive = interactionLockDemoActive,
            showDefaultLauncherPrompt = defaultLauncherPromptEnabled,
            esper = EsperIntegrationConfig(
                enabled = esperEnabled,
                enterpriseId = esperEnterpriseId,
                apiToken = esperApiToken,
                tenantUrl = esperTenantUrl,
                backendAliasEndpoint = esperBackendAliasEndpoint,
                identityHardDisable = esperIdentityHardDisable,
                configRevision = esperConfigRevision,
                useManagedConfigAlias = esperUseManagedAlias,
                showDeviceName = esperShowDeviceName,
                managedAlias = esperManagedAlias,
                managedDeviceName = esperManagedDeviceName
            ),
            diagnostics = DiagnosticsConfig(
                panelEnabled = diagnosticsPanelEnabled,
                showWarnings = diagnosticsShowWarnings
            )
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
        val defaultLauncherPromptEnabled = json.optBoolean("default_launcher_prompt_enabled", false)
        val esperEnabled = if (json.has("esper_sdk_enabled")) {
            json.optBoolean("esper_sdk_enabled", false)
        } else {
            !json.optString("esper_api_token").isBlank() ||
                !json.optString("esper_tenant_url").isBlank() ||
                !json.optString("esper_enterprise_id").isBlank() ||
                !json.optString("esper_backend_alias_endpoint").isBlank()
        }
        val esperApiToken = json.optString("esper_api_token").ifBlank { null }
        val esperTenantUrl = json.optString("esper_tenant_url").ifBlank { null }
        val esperBackendAliasEndpoint = json.optString("esper_backend_alias_endpoint").ifBlank { null }
        val esperEnterpriseId = normalizeEnterpriseId(json.optString("esper_enterprise_id").ifBlank { null })
        val esperIdentityHardDisable = json.optBoolean("esper_identity_hard_disable", false)
        val esperConfigRevision = json.optString("esper_config_revision").ifBlank { null }
        val esperUseManagedAlias = json.optBoolean("esper_use_managed_alias", true)
        val esperShowDeviceName = json.optBoolean("esper_show_device_name", true)
        val esperManagedAlias = json.optString("esper_device_alias").ifBlank { null }
        val esperManagedDeviceName = json.optString("esper_device_name").ifBlank { null }
        val diagnosticsPanelEnabled = json.optBoolean("diagnostics_panel_enabled", false)
        val diagnosticsShowWarnings = json.optBoolean("diagnostics_show_warnings", true)

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
                )
            ),
            mainTileNormalSize = mainTileNormalSize,
            mainTileCompressedSize = mainTileCompressedSize,
            expandedTileSize = expandedTileSize,
            bottomTilesSize = calculateBottomTilesSize(mainTileNormalSize, mainTileCompressedSize, expandedTileSize),
            interactionLockWhenMoving = interactionLockWhenMoving,
            interactionLockDemoActive = interactionLockDemoActive,
            showDefaultLauncherPrompt = defaultLauncherPromptEnabled,
            esper = EsperIntegrationConfig(
                enabled = esperEnabled,
                enterpriseId = esperEnterpriseId,
                apiToken = esperApiToken,
                tenantUrl = esperTenantUrl,
                backendAliasEndpoint = esperBackendAliasEndpoint,
                identityHardDisable = esperIdentityHardDisable,
                configRevision = esperConfigRevision,
                useManagedConfigAlias = esperUseManagedAlias,
                showDeviceName = esperShowDeviceName,
                managedAlias = esperManagedAlias,
                managedDeviceName = esperManagedDeviceName
            ),
            diagnostics = DiagnosticsConfig(
                panelEnabled = diagnosticsPanelEnabled,
                showWarnings = diagnosticsShowWarnings
            )
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

    private fun validateAndSanitize(config: LauncherConfig, source: String): Pair<LauncherConfig, List<String>> {
        val warnings = mutableListOf<String>()

        var mainNormal = config.mainTileNormalSize.coerceIn(0.45f, 0.85f)
        if (mainNormal != config.mainTileNormalSize) {
            warnings += "main_tile_normal_size out of range; clamped to $mainNormal"
        }

        var mainCompressed = config.mainTileCompressedSize.coerceIn(0.25f, 0.75f)
        if (mainCompressed != config.mainTileCompressedSize) {
            warnings += "main_tile_compressed_size out of range; clamped to $mainCompressed"
        }

        var expandedSize = config.expandedTileSize.coerceIn(0.15f, 0.45f)
        if (expandedSize != config.expandedTileSize) {
            warnings += "expanded_tile_size out of range; clamped to $expandedSize"
        }

        if (mainCompressed + expandedSize > 0.9f) {
            val adjustedExpanded = (0.9f - mainCompressed).coerceAtLeast(0.15f)
            warnings += "main_tile_compressed_size + expanded_tile_size exceeds 0.9; adjusted expanded_tile_size to $adjustedExpanded"
            expandedSize = adjustedExpanded
        }

        val sanitizedBottomTiles = config.bottomTiles.map { tile ->
            if (tile.id == "dispatch") {
                val sanitizedDispatchUrl = sanitizeUrl(tile.url, defaultValue = "https://fdxtools.fedex.com/grdlhldispatch")
                if (sanitizedDispatchUrl != tile.url) {
                    warnings += "dispatch_url invalid; reverted to default"
                }

                val sanitizedLoginUrl = sanitizeUrl(tile.loginUrl, defaultValue = null)
                if (tile.loginUrl != null && sanitizedLoginUrl == null) {
                    warnings += "dispatch_login_url invalid; cleared"
                }

                tile.copy(url = sanitizedDispatchUrl, loginUrl = sanitizedLoginUrl)
            } else {
                tile
            }
        }

        val expandableCount = sanitizedBottomTiles.count { it.expandable }
        if (expandableCount == 0) {
            warnings += "All expandable tiles disabled; launcher will run in static mode"
        }

        var esperConfig = config.esper
        val identityVisibilityEnabled = esperConfig.useManagedConfigAlias || esperConfig.showDeviceName
        val hasToken = !esperConfig.apiToken.isNullOrBlank()

        if (esperConfig.identityHardDisable) {
            if (esperConfig.enabled) {
                warnings += "esper_identity_hard_disable=true; forcing Esper identity disabled"
            }
            esperConfig = esperConfig.copy(enabled = false)
        } else if (!esperConfig.enabled && identityVisibilityEnabled && hasToken) {
            warnings += "esper_sdk_enabled resolved to false while identity visibility+token are active; auto-enabled Esper identity"
            esperConfig = esperConfig.copy(enabled = true)
        }

        if (esperConfig.enabled && identityVisibilityEnabled && !hasToken) {
            warnings += "Esper SDK enabled but esper_api_token missing; SDK/cloud calls may fail"
        }

        val sanitizedBackendEndpoint = sanitizeUrl(esperConfig.backendAliasEndpoint, defaultValue = null)
        if (esperConfig.useManagedConfigAlias && esperConfig.backendAliasEndpoint != null && sanitizedBackendEndpoint == null) {
            warnings += "esper_backend_alias_endpoint invalid; cloud alias lookup disabled"
        }

        val sanitizedTenantUrl = sanitizeUrl(esperConfig.tenantUrl, defaultValue = null)
        if (esperConfig.useManagedConfigAlias && esperConfig.tenantUrl != null && sanitizedTenantUrl == null) {
            warnings += "esper_tenant_url invalid; direct API lookup disabled"
        }

        if (
            esperConfig.useManagedConfigAlias &&
            !esperConfig.apiToken.isNullOrBlank() &&
            sanitizedTenantUrl == null &&
            !esperConfig.enterpriseId.isNullOrBlank()
        ) {
            warnings += "esper_enterprise_id provided without valid tenant URL; direct API lookup disabled"
        }

        esperConfig = esperConfig.copy(
            tenantUrl = sanitizedTenantUrl,
            backendAliasEndpoint = sanitizedBackendEndpoint
        )

        val sanitized = config.copy(
            bottomTiles = sanitizedBottomTiles,
            mainTileNormalSize = mainNormal,
            mainTileCompressedSize = mainCompressed,
            expandedTileSize = expandedSize,
            bottomTilesSize = calculateBottomTilesSize(mainNormal, mainCompressed, expandedSize),
            esper = esperConfig
        )

        if (warnings.isEmpty()) {
            Log.i(tag, "config_valid source=$source")
        } else {
            warnings.forEach { warning ->
                Log.w(tag, "config_warning source=$source message=$warning")
            }
        }

        return sanitized to warnings
    }

    private fun deriveIdentityBlockers(esper: EsperIntegrationConfig): List<String> {
        val blockers = mutableListOf<String>()

        if (esper.identityHardDisable) {
            blockers += "hard-disabled"
            return blockers
        }

        if (!esper.enabled) {
            blockers += "sdk-disabled"
        }

        if (esper.apiToken.isNullOrBlank()) {
            blockers += "missing-token"
        }

        if (!esper.useManagedConfigAlias) {
            blockers += "alias-hidden"
        }

        if (!esper.showDeviceName) {
            blockers += "device-hidden"
        }

        return blockers
    }

    private fun sanitizeUrl(value: String?, defaultValue: String?): String? {
        val normalized = value?.trim().orEmpty()
        if (normalized.isBlank()) {
            return defaultValue
        }

        return runCatching {
            val uri = URI(normalized)
            val scheme = uri.scheme?.lowercase()
            if ((scheme == "https" || scheme == "http") && !uri.host.isNullOrBlank()) {
                normalized
            } else {
                defaultValue
            }
        }.getOrDefault(defaultValue)
    }

    private fun normalizeEnterpriseId(rawValue: String?): String? {
        val trimmed = rawValue?.trim().orEmpty()
        if (trimmed.isBlank()) {
            return null
        }

        val normalized = if (trimmed.startsWith("-") && trimmed.length == 37) {
            trimmed.drop(1)
        } else {
            trimmed
        }

        return normalized
    }

}

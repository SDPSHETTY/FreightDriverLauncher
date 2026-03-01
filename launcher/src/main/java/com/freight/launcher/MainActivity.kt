package com.freight.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.freight.common.LauncherConfig
import com.freight.launcher.config.ConfigurationManager
import com.freight.launcher.integration.esper.DeviceIdentityDisplay
import com.freight.launcher.integration.esper.DeviceIdentityResolver
import com.freight.launcher.model.TileContentProvider
import com.freight.launcher.telemetry.LauncherEventLogger
import com.freight.launcher.ui.LockedMainTileLauncher
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val tag = "FreightMain"
    private lateinit var configManager: ConfigurationManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var launcherConfigState by mutableStateOf(LauncherConfig())
    private var configDiagnosticsState by mutableStateOf(
        ConfigurationManager.ConfigurationDiagnostics(source = "default", warnings = emptyList(), blockers = emptyList())
    )
    private var isDefaultLauncherState by mutableStateOf(true)

    private val restrictionsChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED) {
                reloadConfiguration(reason = "restrictions_changed")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configManager = ConfigurationManager(this)
        reloadConfiguration(reason = "activity_created")
        isDefaultLauncherState = isDefaultLauncher()

        registerReceiver(
            restrictionsChangedReceiver,
            IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED)
        )

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val isDefaultLauncher = isDefaultLauncherState
                    val launcherConfig = launcherConfigState
                    val configDiagnostics = configDiagnosticsState

                    // Remember tiles based on configuration
                    val mainTile = remember(launcherConfig) { launcherConfig.mainTile.toTileInfo() }
                    val bottomTiles = remember(launcherConfig) {
                        launcherConfig.bottomTiles.map { it.toTileInfo() }
                    }
                    val expandableTileIds = remember(launcherConfig) {
                        launcherConfig.bottomTiles
                            .filter { it.expandable }
                            .map { it.id }
                            .toSet()
                    }
                    val isInteractionLocked = remember(launcherConfig) {
                        launcherConfig.interactionLockWhenMoving && launcherConfig.interactionLockDemoActive
                    }
                    val identityResolver = remember(launcherConfig) {
                        DeviceIdentityResolver(this@MainActivity, launcherConfig)
                    }
                    val diagnosticsLines = remember(launcherConfig, configDiagnostics) {
                        buildList {
                            add("config:${configDiagnostics.source}")
                            add("esper:${if (launcherConfig.esper.enabled) "on" else "off"}")
                            add("hard-disable:${if (launcherConfig.esper.identityHardDisable) "on" else "off"}")
                            add("alias:${if (launcherConfig.esper.useManagedConfigAlias) "on" else "off"}")
                            add("device:${if (launcherConfig.esper.showDeviceName) "on" else "off"}")
                            if (!launcherConfig.esper.apiToken.isNullOrBlank()) {
                                add("api-token:present")
                            }
                            if (!launcherConfig.esper.configRevision.isNullOrBlank()) {
                                add("rev:${launcherConfig.esper.configRevision}")
                            }
                            if (configDiagnostics.blockers.isNotEmpty()) {
                                add("block:${configDiagnostics.blockers.joinToString(",")}")
                            }
                            if (launcherConfig.diagnostics.showWarnings && configDiagnostics.warnings.isNotEmpty()) {
                                add("warn:${configDiagnostics.warnings.size}")
                            }
                        }
                    }
                    var mainTileIdentity by remember {
                        mutableStateOf(identityResolver.localDisplayIdentity())
                    }

                    LaunchedEffect(launcherConfig) {
                        mainTileIdentity = identityResolver.localDisplayIdentity()
                    }

                    LaunchedEffect(
                        launcherConfig.esper.enabled,
                        launcherConfig.esper.identityHardDisable,
                        launcherConfig.esper.useManagedConfigAlias,
                        launcherConfig.esper.showDeviceName,
                        launcherConfig.esper.configRevision,
                        launcherConfig.esper.apiToken,
                        launcherConfig.esper.enterpriseId,
                        launcherConfig.esper.tenantUrl,
                        launcherConfig.esper.backendAliasEndpoint
                    ) {
                        LauncherEventLogger.log(
                            event = "launcher_started",
                            attributes = mapOf(
                                "esper_enabled" to launcherConfig.esper.enabled,
                                "interaction_lock" to isInteractionLocked
                            )
                        )

                        val shouldResolveIdentity = !launcherConfig.esper.identityHardDisable &&
                            launcherConfig.esper.enabled &&
                            (launcherConfig.esper.useManagedConfigAlias || launcherConfig.esper.showDeviceName)

                        if (!shouldResolveIdentity) {
                            return@LaunchedEffect
                        }

                        while (true) {
                            identityResolver.resolveEsperIdentity { esperIdentity ->
                                if (esperIdentity != null && !esperIdentity.primary.isNullOrBlank()) {
                                    mainHandler.post {
                                        mainTileIdentity = mergeIdentity(mainTileIdentity, esperIdentity)
                                    }
                                }
                            }

                            delay(30000)
                        }
                    }

                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(15000)
                            reloadConfiguration(reason = "periodic_refresh")
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        LockedMainTileLauncher(
                            mainTile = mainTile,
                            bottomTiles = bottomTiles,
                            expandableTileIds = expandableTileIds,
                            deviceIdentityPrimary = mainTileIdentity.primary,
                            deviceIdentitySecondary = mainTileIdentity.secondary,
                            diagnosticsEnabled = launcherConfig.diagnostics.panelEnabled,
                            diagnosticsLines = diagnosticsLines,
                            isInteractionLocked = isInteractionLocked,
                            mainTileNormalSize = launcherConfig.mainTileNormalSize,
                            mainTileCompressedSize = launcherConfig.mainTileCompressedSize,
                            expandedTileSize = launcherConfig.expandedTileSize,
                            tileContentProvider = { tile ->
                                TileContentProvider.getContentForTile(tile)
                            },
                            expandableScreenProvider = { tileId ->
                                TileContentProvider.getExpandedScreenForTile(tileId, launcherConfig)
                            }
                        )

                        if (!isDefaultLauncher && launcherConfig.showDefaultLauncherPrompt) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 12.dp)
                                    .background(Color(0xCC000000))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Button(onClick = { openHomeSettings() }) {
                                    Text("Set Freight as default launcher")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        reloadConfiguration(reason = "activity_resumed")
        isDefaultLauncherState = isDefaultLauncher()
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(restrictionsChangedReceiver) }
        super.onDestroy()
    }

    private fun reloadConfiguration(reason: String) {
        val refreshedConfig = configManager.getConfig()
        val diagnostics = configManager.getDiagnostics()

        val configChanged = refreshedConfig != launcherConfigState
        val diagnosticsChanged = diagnostics != configDiagnosticsState
        if (!configChanged && !diagnosticsChanged) {
            return
        }

        mainHandler.post {
            launcherConfigState = refreshedConfig
            configDiagnosticsState = diagnostics
            LauncherEventLogger.log(
                event = "config_reloaded",
                attributes = mapOf(
                    "reason" to reason,
                    "source" to diagnostics.source,
                    "warnings" to diagnostics.warnings.size
                )
            )
            Log.i(tag, "Configuration reloaded reason=$reason source=${diagnostics.source}")
        }
    }

    private fun mergeIdentity(
        current: DeviceIdentityDisplay,
        incoming: DeviceIdentityDisplay
    ): DeviceIdentityDisplay {
        return DeviceIdentityDisplay(
            primary = incoming.primary ?: current.primary,
            secondary = incoming.secondary ?: current.secondary
        )
    }

    private fun isDefaultLauncher(): Boolean {
        return runCatching {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolved = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolved?.activityInfo?.packageName == packageName
        }.getOrDefault(false)
    }

    private fun openHomeSettings() {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { startActivity(intent) }
            .onFailure {
                startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
    }
}

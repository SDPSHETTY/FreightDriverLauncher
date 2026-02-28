package com.freight.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.freight.launcher.config.ConfigurationManager
import com.freight.launcher.model.TileContentProvider
import com.freight.launcher.ui.LockedMainTileLauncher

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize configuration manager
        val configManager = ConfigurationManager(this)
        val launcherConfig = configManager.getConfig()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Remember tiles based on configuration
                    val mainTile = remember { launcherConfig.mainTile.toTileInfo() }
                    val bottomTiles = remember {
                        launcherConfig.bottomTiles.map { it.toTileInfo() }
                    }
                    val expandableTileIds = remember {
                        launcherConfig.bottomTiles
                            .filter { it.expandable }
                            .map { it.id }
                            .toSet()
                    }
                    val isInteractionLocked = remember {
                        launcherConfig.interactionLockWhenMoving && launcherConfig.interactionLockDemoActive
                    }

                    LockedMainTileLauncher(
                        mainTile = mainTile,
                        bottomTiles = bottomTiles,
                        expandableTileIds = expandableTileIds,
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
                }
            }
        }
    }
}

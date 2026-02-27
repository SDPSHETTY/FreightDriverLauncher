package com.freight.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.freight.common.TileInfo

/**
 * Locked Main Tile Launcher with Multiple Expandable Tiles
 * - Motive Driver always visible (never covered) - MINIMUM 40%
 * - Navigation, PrePass, and Dispatch tiles can expand for detailed view
 * - Layout adjusts: Motive 40%, Expanded tile 30%, Other tiles 30%
 * - Only one tile can be expanded at a time
 */
@Composable
fun LockedMainTileLauncher(
    mainTile: TileInfo,
    bottomTiles: List<TileInfo>,
    tileContentProvider: @Composable (TileInfo) -> Unit,
    expandableScreenProvider: @Composable (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Track which tile is currently expanded (null = none expanded)
    var expandedTileId by remember { mutableStateOf<String?>(null) }

    // Get expandable tiles
    val expandableTileIds = setOf("navigation", "prepass", "dispatch")
    val expandedTile = bottomTiles.find { it.id == expandedTileId }
    val visibleBottomTiles = if (expandedTileId != null) {
        bottomTiles.filter { it.id != expandedTileId }
    } else {
        bottomTiles
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Main tile area - LOCKED, always shows Motive
        // Size: 70% normally, 40% when any tile expanded
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (expandedTileId != null) 0.4f else 0.7f)
        ) {
            tileContentProvider(mainTile)
        }

        // Expanded tile area (30% when expanded)
        if (expandedTileId != null && expandedTile != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
                    .clickable { expandedTileId = null } // Tap to collapse
            ) {
                // Show appropriate expanded view
                expandableScreenProvider(expandedTileId!!)
            }
        }

        // Bottom status tiles area (30%)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
                .background(Color(0xFF212121)) // Dark background for bottom bar
                .padding(vertical = 12.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(visibleBottomTiles) { tile ->
                    StatusTileCard(
                        tile = tile,
                        content = { tileContentProvider(tile) },
                        isExpandable = tile.id in expandableTileIds,
                        onTap = {
                            if (tile.id in expandableTileIds) {
                                expandedTileId = if (expandedTileId == tile.id) null else tile.id
                            }
                        }
                    )
                }
            }
        }
    }
}

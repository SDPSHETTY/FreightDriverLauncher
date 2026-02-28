package com.freight.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    expandableTileIds: Set<String>,
    isInteractionLocked: Boolean,
    mainTileNormalSize: Float,
    mainTileCompressedSize: Float,
    expandedTileSize: Float,
    tileContentProvider: @Composable (TileInfo) -> Unit,
    expandableScreenProvider: @Composable (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Track which tile is currently expanded (null = none expanded)
    var expandedTileId by remember { mutableStateOf<String?>(null) }

    val expandedTile = bottomTiles.find { it.id == expandedTileId }
    val visibleBottomTiles = if (expandedTileId != null) {
        bottomTiles.filter { it.id != expandedTileId }
    } else {
        bottomTiles
    }

    val collapsedMainWeight = mainTileNormalSize.coerceIn(0.2f, 0.9f)
    val expandedMainWeight = mainTileCompressedSize.coerceIn(0.2f, 0.8f)
    val expandedTileWeight = expandedTileSize.coerceIn(0.1f, 0.5f)
    val collapsedBottomWeight = (1f - collapsedMainWeight).coerceIn(0.1f, 0.8f)
    val expandedBottomWeight = (1f - expandedMainWeight - expandedTileWeight).coerceIn(0.1f, 0.6f)

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Main tile area - LOCKED, always shows Motive
        // Size: 70% normally, 40% when any tile expanded
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (expandedTileId != null) expandedMainWeight else collapsedMainWeight)
        ) {
            tileContentProvider(mainTile)
        }

        // Expanded tile area (30% when expanded)
        if (expandedTileId != null && expandedTile != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(expandedTileWeight)
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
                .weight(if (expandedTileId != null) expandedBottomWeight else collapsedBottomWeight)
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
                            if (!isInteractionLocked && tile.id in expandableTileIds) {
                                expandedTileId = if (expandedTileId == tile.id) null else tile.id
                            }
                        }
                    )
                }
            }

            if (isInteractionLocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 6.dp)
                        .background(Color(0xFFB71C1C).copy(alpha = 0.92f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Interaction locked while vehicle is moving",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

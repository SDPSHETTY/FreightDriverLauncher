package com.freight.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.freight.common.TileInfo

@Composable
fun TileGrid(
    mainTile: TileInfo?,
    bottomTiles: List<TileInfo>,
    onTileClick: (TileInfo) -> Unit,
    mainTileContent: @Composable (TileInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Main tile area (70% of screen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
        ) {
            MainTileView(
                tile = mainTile,
                content = mainTileContent
            )
        }

        // Bottom tiles area (30% of screen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
                .background(Color(0xFF212121)) // Dark background for bottom bar
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(bottomTiles) { tile ->
                    TileCard(
                        tile = tile,
                        onTileClick = onTileClick
                    )
                }
            }
        }
    }
}

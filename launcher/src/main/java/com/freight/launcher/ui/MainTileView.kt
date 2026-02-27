package com.freight.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freight.common.TileInfo

@Composable
fun MainTileView(
    tile: TileInfo?,
    content: @Composable (TileInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        if (tile != null) {
            // Render the content for the current tile
            content(tile)
        } else {
            // Show placeholder if no tile is selected
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No app selected",
                    fontSize = 24.sp,
                    color = Color.White
                )
            }
        }
    }
}

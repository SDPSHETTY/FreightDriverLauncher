package com.freight.launcher.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freight.common.TileInfo
import com.freight.launcher.viewmodel.LauncherViewModel

@Composable
fun LauncherScreen(
    tiles: List<TileInfo>,
    tileContentProvider: @Composable (TileInfo) -> Unit,
    viewModel: LauncherViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    // Initialize tiles when the screen first loads
    LaunchedEffect(tiles) {
        viewModel.initializeTiles(tiles)
    }

    // Collect state from ViewModel
    val mainTile by viewModel.mainTile.collectAsState()
    val bottomTiles by viewModel.bottomTiles.collectAsState()

    // Render the tile grid
    TileGrid(
        mainTile = mainTile,
        bottomTiles = bottomTiles,
        onTileClick = { tile ->
            viewModel.swapTileToMain(tile)
        },
        mainTileContent = { tile ->
            tileContentProvider(tile)
        },
        modifier = modifier
    )
}

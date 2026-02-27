package com.freight.launcher.viewmodel

import androidx.lifecycle.ViewModel
import com.freight.common.TileInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LauncherViewModel : ViewModel() {
    private val _mainTile = MutableStateFlow<TileInfo?>(null)
    val mainTile: StateFlow<TileInfo?> = _mainTile.asStateFlow()

    private val _bottomTiles = MutableStateFlow<List<TileInfo>>(emptyList())
    val bottomTiles: StateFlow<List<TileInfo>> = _bottomTiles.asStateFlow()

    private val _allTiles = MutableStateFlow<List<TileInfo>>(emptyList())

    /**
     * Initialize tiles with the provided list
     * First tile becomes the main tile, rest go to bottom
     */
    fun initializeTiles(tiles: List<TileInfo>) {
        if (tiles.isEmpty()) return

        _allTiles.value = tiles
        _mainTile.value = tiles.first()
        _bottomTiles.value = tiles.drop(1)
    }

    /**
     * Swap a tile from the bottom into the main position
     * The previous main tile goes into the bottom tiles
     */
    fun swapTileToMain(tile: TileInfo) {
        val currentMain = _mainTile.value ?: return

        // Only swap if the tile is different from current main
        if (tile.id == currentMain.id) return

        // Find the position of the clicked tile in bottom tiles
        val bottomTilesList = _bottomTiles.value.toMutableList()
        val clickedIndex = bottomTilesList.indexOfFirst { it.id == tile.id }

        if (clickedIndex != -1) {
            // Replace the clicked tile with the current main tile
            bottomTilesList[clickedIndex] = currentMain

            // Update state
            _mainTile.value = tile
            _bottomTiles.value = bottomTilesList
        }
    }
}

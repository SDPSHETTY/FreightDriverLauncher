package com.freight.common

/**
 * Interface that embedded apps should implement
 * to communicate with the launcher
 */
interface AppContract {
    /**
     * Called when the app should pause (moved to bottom tile)
     */
    fun onPause()

    /**
     * Called when the app should resume (moved to main tile)
     */
    fun onResume()

    /**
     * Get the current state of the app for preservation
     */
    fun getState(): Map<String, Any>

    /**
     * Restore the app state
     */
    fun restoreState(state: Map<String, Any>)
}

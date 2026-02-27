package com.freight.common

/**
 * Configuration for the freight apps
 * Modify these values to customize app behavior
 */
object AppConfig {
    /**
     * Dispatch webapp URL - FedEx Ground Linehaul Dispatch
     */
    const val DISPATCH_URL = "https://fdxtools.fedex.com/grdlhldispatch"

    /**
     * Navigation settings
     * Configure the default route for Google Maps
     */
    object Navigation {
        // Starting location (origin)
        const val DEFAULT_ORIGIN_LAT = 41.8781  // Chicago, IL
        const val DEFAULT_ORIGIN_LNG = -87.6298

        // Destination
        const val DEFAULT_DEST_LAT = 39.7392    // Denver, CO
        const val DEFAULT_DEST_LNG = -104.9903

        // Display names
        const val ORIGIN_NAME = "Chicago, IL"
        const val DEST_NAME = "Denver, CO"

        // Default zoom level (higher = more zoomed in)
        const val DEFAULT_ZOOM = 5f
    }
}

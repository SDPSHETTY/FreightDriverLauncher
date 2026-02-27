package com.freight.common

/**
 * Enum representing the three tiers of app embedding
 */
enum class EmbeddingMode {
    /**
     * Tier 3: Compose-Embedded Prototype
     * All apps are composables within the launcher
     */
    COMPOSE_EMBEDDED,

    /**
     * Tier 2: Intent-Based Multi-Tasking
     * Apps are separate APKs launched via Intents
     */
    INTENT_BASED,

    /**
     * Tier 1: ActivityView Production
     * Apps are embedded using ActivityView (requires Device Owner)
     */
    ACTIVITY_VIEW
}

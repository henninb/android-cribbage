package com.brianhenning.cribbage.model

/**
 * Card selection method
 */
enum class CardSelectionMode {
    TAP,        // Single tap to select
    LONG_PRESS, // Long press to select
    DRAG        // Drag to discard area
}

/**
 * Counting mode for game scoring
 */
enum class CountingMode {
    AUTOMATIC, // App calculates points automatically
    MANUAL     // Player inputs points manually
}

/**
 * Game settings data class
 */
data class GameSettings(
    val cardSelectionMode: CardSelectionMode = CardSelectionMode.TAP,
    val countingMode: CountingMode = CountingMode.AUTOMATIC
)

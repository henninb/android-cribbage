package com.brianhenning.cribbage.game.state

/**
 * Represents the different phases of a cribbage game.
 *
 * @property displayName Human-readable name for the phase
 * @property showTurnIndicator Whether to show whose turn it is during this phase
 * @property instructionHint Optional hint text to guide the player
 */
enum class GamePhase(
    val displayName: String,
    val showTurnIndicator: Boolean,
    val instructionHint: String? = null
) {
    SETUP("Game Setup", false, "Tap \"Start New Game\" to begin"),
    DEALING("Dealing Cards", false, "Tap \"Deal Cards\" to deal 6 cards to each player"),
    CRIB_SELECTION("Selecting for Crib", false, "Select 2 cards by tapping them, then tap the button to place them in the crib"),
    PEGGING("Pegging", true, "Tap a card to immediately play it"),
    HAND_COUNTING("Counting Hands", false, null),
    GAME_OVER("Game Over", false, "Tap \"Start New Game\" to play again")
}

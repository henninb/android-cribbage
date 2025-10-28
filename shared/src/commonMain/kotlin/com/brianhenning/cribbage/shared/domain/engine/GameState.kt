package com.brianhenning.cribbage.shared.domain.engine

import com.brianhenning.cribbage.shared.domain.logic.DetailedScoreBreakdown
import com.brianhenning.cribbage.shared.domain.model.Card

/**
 * Game phases enum
 */
enum class GamePhase {
    SETUP,
    CUT_FOR_DEALER,
    DEALING,
    CRIB_SELECTION,
    PEGGING,
    HAND_COUNTING,
    GAME_OVER
}

/**
 * Counting phases during hand scoring
 */
enum class CountingPhase {
    NONE,
    NON_DEALER,
    DEALER,
    CRIB,
    COMPLETED
}

/**
 * Complete game state - immutable data class
 */
data class GameState(
    // Game progression
    val gameStarted: Boolean = false,
    val currentPhase: GamePhase = GamePhase.SETUP,
    val gameOver: Boolean = false,

    // Scores
    val playerScore: Int = 0,
    val opponentScore: Int = 0,
    val gamesWon: Int = 0,
    val gamesLost: Int = 0,
    val skunksFor: Int = 0,
    val skunksAgainst: Int = 0,

    // Dealer
    val isPlayerDealer: Boolean = false,

    // Hands
    val playerHand: List<Card> = emptyList(),
    val opponentHand: List<Card> = emptyList(),
    val cribHand: List<Card> = emptyList(),
    val starterCard: Card? = null,
    val selectedCards: Set<Int> = emptySet(),

    // Cut cards (for dealer determination)
    val cutPlayerCard: Card? = null,
    val cutOpponentCard: Card? = null,
    val showCutForDealer: Boolean = false,

    // Pegging state
    val isPeggingPhase: Boolean = false,
    val isPlayerTurn: Boolean = false,
    val peggingCount: Int = 0,
    val peggingPile: List<Card> = emptyList(),
    val playerCardsPlayed: Set<Int> = emptySet(),
    val opponentCardsPlayed: Set<Int> = emptySet(),
    val consecutiveGoes: Int = 0,
    val lastPlayerWhoPlayed: String? = null,

    // Hand counting
    val isInHandCountingPhase: Boolean = false,
    val countingPhase: CountingPhase = CountingPhase.NONE,
    val handScores: HandScores = HandScores(),

    // UI state
    val gameStatus: String = "",
    val isOpponentActionInProgress: Boolean = false,

    // Pending reset (after 31 or Go)
    val pendingReset: PendingResetState? = null,

    // Winner modal
    val showWinnerModal: Boolean = false,
    val winnerModalData: WinnerModalData? = null
)

/**
 * Hand scores during counting phase
 */
data class HandScores(
    val nonDealerScore: Int = 0,
    val nonDealerBreakdown: DetailedScoreBreakdown? = null,
    val dealerScore: Int = 0,
    val dealerBreakdown: DetailedScoreBreakdown? = null,
    val cribScore: Int = 0,
    val cribBreakdown: DetailedScoreBreakdown? = null
)

/**
 * Pending reset state after 31 or Go
 */
data class PendingResetState(
    val pile: List<Card>,
    val finalCount: Int,
    val scoreAwarded: Int,
    val message: String
)

/**
 * Winner modal data
 */
data class WinnerModalData(
    val playerWon: Boolean,
    val playerScore: Int,
    val opponentScore: Int,
    val wasSkunk: Boolean,
    val gamesWon: Int,
    val gamesLost: Int,
    val skunksFor: Int,
    val skunksAgainst: Int
)

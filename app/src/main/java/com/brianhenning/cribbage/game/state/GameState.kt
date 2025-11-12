package com.brianhenning.cribbage.game.state

import com.brianhenning.cribbage.shared.domain.logic.PeggingRoundManager
import com.brianhenning.cribbage.shared.domain.logic.SubRoundReset
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.ui.composables.CountingPhase
import com.brianhenning.cribbage.ui.composables.GamePhase
import com.brianhenning.cribbage.ui.composables.HandScores
import com.brianhenning.cribbage.ui.composables.ScoreAnimationState

/**
 * Main game UI state - immutable data class representing all game state.
 * This is the single source of truth for the game's state.
 */
data class GameUiState(
    // Core game state
    val gameStarted: Boolean = false,
    val currentPhase: GamePhase = GamePhase.SETUP,
    val playerScore: Int = 0,
    val opponentScore: Int = 0,
    val isPlayerDealer: Boolean = false,
    val gameOver: Boolean = false,
    val gameStatus: String = "",

    // Card state
    val playerHand: List<Card> = emptyList(),
    val opponentHand: List<Card> = emptyList(),
    val cribHand: List<Card> = emptyList(),
    val drawDeck: List<Card> = emptyList(),
    val selectedCards: Set<Int> = emptySet(),
    val starterCard: Card? = null,

    // Dealer cut cards
    val cutPlayerCard: Card? = null,
    val cutOpponentCard: Card? = null,
    val showCutForDealer: Boolean = false,

    // Button states
    val dealButtonEnabled: Boolean = false,
    val selectCribButtonEnabled: Boolean = false,
    val playCardButtonEnabled: Boolean = false,
    val showHandCountingButton: Boolean = false,
    val showGoButton: Boolean = false,

    // Phase-specific state
    val peggingState: PeggingState? = null,
    val handCountingState: HandCountingState? = null,

    // Modals and overlays
    val showWinnerModal: Boolean = false,
    val winnerModalData: WinnerModalData? = null,
    val showCutCardDisplay: Boolean = false,

    // Animations
    val playerScoreAnimation: ScoreAnimationState? = null,
    val opponentScoreAnimation: ScoreAnimationState? = null,
    val show31Banner: Boolean = false,
    val previousPeggingCount: Int = 0,

    // Match statistics
    val matchStats: MatchStats = MatchStats(),

    // Debug
    val showDebugScoreDialog: Boolean = false
)

/**
 * Pegging phase state - all state specific to the pegging phase
 */
data class PeggingState(
    val isPeggingPhase: Boolean = false,
    val isPlayerTurn: Boolean = false,
    val peggingCount: Int = 0,
    val peggingPile: List<Card> = emptyList(),
    val peggingDisplayPile: List<Card> = emptyList(),
    val playerCardsPlayed: Set<Int> = emptySet(),
    val opponentCardsPlayed: Set<Int> = emptySet(),
    val consecutiveGoes: Int = 0,
    val lastPlayerWhoPlayed: String? = null,
    val pendingReset: PendingResetState? = null,
    val isOpponentActionInProgress: Boolean = false,
    val peggingManager: PeggingRoundManager? = null,
    val showPeggingCount: Boolean = false
)

/**
 * Hand counting phase state
 */
data class HandCountingState(
    val isInHandCountingPhase: Boolean = false,
    val countingPhase: CountingPhase = CountingPhase.NONE,
    val handScores: HandScores = HandScores(),
    val waitingForDialogDismissal: Boolean = false,
    val waitingForManualInput: Boolean = false
)

/**
 * Match statistics across multiple games
 */
data class MatchStats(
    val gamesWon: Int = 0,
    val gamesLost: Int = 0,
    val skunksFor: Int = 0,
    val skunksAgainst: Int = 0,
    val doubleSkunksFor: Int = 0,
    val doubleSkunksAgainst: Int = 0
)

/**
 * State for pending reset - shown to user before clearing pile
 */
data class PendingResetState(
    val pile: List<Card>,
    val finalCount: Int,
    val scoreAwarded: Int,
    val resetData: SubRoundReset
)

/**
 * Data for winner modal dialog
 */
data class WinnerModalData(
    val playerWon: Boolean,
    val playerScore: Int,
    val opponentScore: Int,
    val wasSkunk: Boolean,
    val gamesWon: Int,
    val gamesLost: Int,
    val skunksFor: Int,
    val skunksAgainst: Int,
    val doubleSkunksFor: Int = 0,
    val doubleSkunksAgainst: Int = 0
)

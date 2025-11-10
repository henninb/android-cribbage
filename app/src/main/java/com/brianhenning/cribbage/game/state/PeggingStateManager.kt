package com.brianhenning.cribbage.game.state

import com.brianhenning.cribbage.shared.domain.logic.OpponentAI
import com.brianhenning.cribbage.shared.domain.logic.PeggingPoints
import com.brianhenning.cribbage.shared.domain.logic.PeggingRoundManager
import com.brianhenning.cribbage.shared.domain.logic.PeggingScorer
import com.brianhenning.cribbage.shared.domain.logic.Player
import com.brianhenning.cribbage.shared.domain.logic.SubRoundReset
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.ui.composables.ScoreAnimationState

/**
 * State Manager for pegging phase operations.
 * Manages turn-based card play, scoring, Go handling, and sub-round resets.
 * Follows MVVM best practices - operates on immutable state and returns new state.
 *
 * This is a pure business logic class with no Android dependencies.
 * All methods are testable without the Android framework.
 */
class PeggingStateManager {

    /**
     * Result of starting the pegging phase
     */
    data class StartPeggingResult(
        val peggingState: PeggingState,
        val statusMessage: String,
        val hisHeelsPoints: Int = 0, // Points awarded for His Heels (Jack as starter)
        val hisHeelsToPlayer: Boolean = false
    )

    /**
     * Result of playing a card
     */
    data class PlayCardResult(
        val updatedPeggingState: PeggingState,
        val peggingPoints: PeggingPoints,
        val pointsAwarded: Int,
        val isForPlayer: Boolean,
        val statusMessage: String,
        val animation: ScoreAnimationState?,
        val pendingReset: PendingResetState?,
        val show31Banner: Boolean = false
    )

    /**
     * Result of handling a Go
     */
    data class GoResult(
        val updatedPeggingState: PeggingState,
        val goPointAwarded: Int,
        val goPointToPlayer: Boolean?,
        val statusMessage: String,
        val animation: ScoreAnimationState?,
        val pendingReset: PendingResetState?
    )

    /**
     * Result of checking legal moves
     */
    data class LegalMovesResult(
        val legalMoves: List<Pair<Int, Card>>, // List of (index, card) pairs
        val hasLegalMoves: Boolean
    )

    /**
     * Result of acknowledging a reset
     */
    data class AcknowledgeResetResult(
        val updatedPeggingState: PeggingState,
        val isPeggingComplete: Boolean,
        val statusMessage: String
    )

    /**
     * Starts the pegging phase.
     * Initializes PeggingRoundManager and checks for His Heels.
     *
     * @param isPlayerDealer Whether player is the dealer
     * @param starterCard The starter card (to check for His Heels - Jack)
     * @return StartPeggingResult with initial state
     */
    fun startPegging(
        isPlayerDealer: Boolean,
        starterCard: Card?
    ): StartPeggingResult {
        val nonDealerStarts = !isPlayerDealer
        val manager = PeggingRoundManager(
            startingPlayer = if (nonDealerStarts) Player.PLAYER else Player.OPPONENT
        )

        // Check for His Heels (Jack as starter - dealer gets 2 points)
        val hisHeelsPoints = if (starterCard?.rank == com.brianhenning.cribbage.shared.domain.model.Rank.JACK) {
            2
        } else {
            0
        }

        val peggingState = PeggingState(
            isPeggingPhase = true,
            isPlayerTurn = nonDealerStarts,
            peggingCount = 0,
            peggingPile = emptyList(),
            peggingDisplayPile = emptyList(),
            playerCardsPlayed = emptySet(),
            opponentCardsPlayed = emptySet(),
            consecutiveGoes = 0,
            lastPlayerWhoPlayed = null,
            pendingReset = null,
            isOpponentActionInProgress = !nonDealerStarts, // Set if opponent starts
            peggingManager = manager,
            showPeggingCount = true
        )

        return StartPeggingResult(
            peggingState = peggingState,
            statusMessage = "Pegging phase begins. ${if (nonDealerStarts) "Your turn" else "Opponent's turn"}",
            hisHeelsPoints = hisHeelsPoints,
            hisHeelsToPlayer = isPlayerDealer
        )
    }

    /**
     * Plays a card in the pegging phase.
     * Updates manager state, calculates points, and checks for resets.
     *
     * @param currentState Current pegging state
     * @param card Card being played
     * @param isPlayer Whether player is playing the card
     * @param cardIndex Index of the card in the hand (for tracking played cards)
     * @return PlayCardResult with updated state and scoring
     */
    fun playCard(
        currentState: PeggingState,
        card: Card,
        isPlayer: Boolean,
        cardIndex: Int
    ): PlayCardResult {
        val manager = currentState.peggingManager
            ?: throw IllegalStateException("PeggingRoundManager not initialized")

        // Save pile and count BEFORE calling onPlay (manager may reset immediately)
        val pileBeforePlay = manager.peggingPile.toList() + card
        val countBeforeReset = manager.peggingCount + card.getValue()

        // Play the card through the manager
        val outcome = manager.onPlay(card)

        // Calculate points scored
        val pts = PeggingScorer.pointsForPile(pileBeforePlay, countBeforeReset)
        val totalPoints = pts.fifteen + pts.thirtyOne + pts.pairPoints + pts.runPoints

        // Update played cards
        val updatedPlayedCards = if (isPlayer) {
            currentState.playerCardsPlayed + cardIndex
        } else {
            currentState.opponentCardsPlayed + cardIndex
        }

        // Update display pile
        val updatedDisplayPile = currentState.peggingDisplayPile + card

        // Build status message
        val statusMessage = buildPlayCardStatusMessage(card, isPlayer, pts)

        // Create animation if points awarded
        val animation = if (totalPoints > 0) {
            ScoreAnimationState(totalPoints, isPlayer)
        } else {
            null
        }

        // Handle reset if triggered
        val resetData = outcome.reset
        val pendingReset = if (resetData != null) {
            createPendingReset(currentState, resetData, countBeforeReset, updatedDisplayPile)
        } else {
            null
        }

        // Update pegging state with manager's state
        val updatedState = currentState.copy(
            isPlayerTurn = manager.isPlayerTurn == Player.PLAYER,
            peggingCount = manager.peggingCount,
            peggingPile = manager.peggingPile.toList(),
            peggingDisplayPile = updatedDisplayPile,
            playerCardsPlayed = if (isPlayer) updatedPlayedCards else currentState.playerCardsPlayed,
            opponentCardsPlayed = if (!isPlayer) updatedPlayedCards else currentState.opponentCardsPlayed,
            consecutiveGoes = manager.consecutiveGoes,
            lastPlayerWhoPlayed = if (isPlayer) "player" else "opponent",
            peggingManager = manager
        )

        return PlayCardResult(
            updatedPeggingState = updatedState,
            peggingPoints = pts,
            pointsAwarded = totalPoints,
            isForPlayer = isPlayer,
            statusMessage = statusMessage,
            animation = animation,
            pendingReset = pendingReset,
            show31Banner = countBeforeReset == 31
        )
    }

    /**
     * Handles a "Go" declaration by a player.
     * Updates manager state and awards go points if applicable.
     *
     * @param currentState Current pegging state
     * @param opponentHasLegalMove Whether the opponent has a legal move
     * @return GoResult with updated state
     */
    fun handleGo(
        currentState: PeggingState,
        opponentHasLegalMove: Boolean
    ): GoResult {
        val manager = currentState.peggingManager
            ?: throw IllegalStateException("PeggingRoundManager not initialized")

        val currentPlayerIsPlayer = manager.isPlayerTurn == Player.PLAYER

        // Call manager's onGo
        val resetData = manager.onGo(opponentHasLegalMove = opponentHasLegalMove)

        // Determine go point award
        var goPointAwarded = 0
        var goPointToPlayer: Boolean? = null
        var statusMessage = if (currentPlayerIsPlayer) "You say Go" else "Opponent says Go"

        if (resetData != null && !resetData.resetFor31) {
            when (resetData.goPointTo) {
                Player.PLAYER -> {
                    goPointAwarded = 1
                    goPointToPlayer = true
                    statusMessage = "Go point for You!"
                }
                Player.OPPONENT -> {
                    goPointAwarded = 1
                    goPointToPlayer = false
                    statusMessage = "Go point for Opponent!"
                }
                else -> {}
            }
        }

        val animation = if (goPointAwarded > 0 && goPointToPlayer != null) {
            ScoreAnimationState(goPointAwarded, goPointToPlayer)
        } else {
            null
        }

        val pendingReset = if (resetData != null) {
            createPendingReset(currentState, resetData, manager.peggingCount)
        } else {
            null
        }

        // Update state with manager's state
        val updatedState = currentState.copy(
            isPlayerTurn = manager.isPlayerTurn == Player.PLAYER,
            peggingCount = manager.peggingCount,
            peggingPile = manager.peggingPile.toList(),
            consecutiveGoes = manager.consecutiveGoes,
            lastPlayerWhoPlayed = when (manager.lastPlayerWhoPlayed) {
                Player.PLAYER -> "player"
                Player.OPPONENT -> "opponent"
                else -> currentState.lastPlayerWhoPlayed
            },
            peggingManager = manager
        )

        return GoResult(
            updatedPeggingState = updatedState,
            goPointAwarded = goPointAwarded,
            goPointToPlayer = goPointToPlayer,
            statusMessage = statusMessage,
            animation = animation,
            pendingReset = pendingReset
        )
    }

    /**
     * Gets legal moves for a player given the current count.
     *
     * @param hand Player's hand
     * @param cardsPlayed Set of indices already played
     * @param currentCount Current pegging count
     * @return LegalMovesResult with list of legal moves
     */
    fun getLegalMoves(
        hand: List<Card>,
        cardsPlayed: Set<Int>,
        currentCount: Int
    ): LegalMovesResult {
        val legalMoves = hand.filterIndexed { index, card ->
            !cardsPlayed.contains(index) && (currentCount + card.getValue() <= 31)
        }.mapIndexed { filterIndex, card ->
            val originalIndex = hand.indexOfFirst { it == card && !cardsPlayed.contains(hand.indexOf(it)) }
            Pair(originalIndex, card)
        }

        return LegalMovesResult(
            legalMoves = legalMoves,
            hasLegalMoves = legalMoves.isNotEmpty()
        )
    }

    /**
     * Acknowledges a pending reset and clears the display pile.
     * Checks if pegging phase is complete.
     *
     * @param currentState Current pegging state with pending reset
     * @param playerHand Player's hand
     * @param opponentHand Opponent's hand
     * @return AcknowledgeResetResult with updated state
     */
    fun acknowledgeReset(
        currentState: PeggingState,
        playerHand: List<Card>,
        opponentHand: List<Card>
    ): AcknowledgeResetResult {
        // Clear pending reset and display pile
        val clearedState = currentState.copy(
            pendingReset = null,
            peggingDisplayPile = emptyList()
        )

        // Check if pegging is complete (no legal moves for either player)
        val playerLegal = getLegalMoves(playerHand, currentState.playerCardsPlayed, currentState.peggingCount)
        val opponentLegal = getLegalMoves(opponentHand, currentState.opponentCardsPlayed, currentState.peggingCount)

        val isPeggingComplete = !playerLegal.hasLegalMoves && !opponentLegal.hasLegalMoves

        val statusMessage = if (isPeggingComplete) {
            "Pegging phase complete. Proceed to hand scoring."
        } else if (clearedState.isPlayerTurn) {
            "New sub-round begins. Your turn"
        } else {
            "New sub-round begins. Opponent's turn"
        }

        return AcknowledgeResetResult(
            updatedPeggingState = clearedState,
            isPeggingComplete = isPeggingComplete,
            statusMessage = statusMessage
        )
    }

    /**
     * Chooses a smart card for the opponent to play using OpponentAI.
     *
     * @param hand Opponent's hand
     * @param playedIndices Indices already played
     * @param currentCount Current pegging count
     * @param peggingPile Current pegging pile
     * @return Pair of (index, card) or null if no legal moves
     */
    fun chooseOpponentCard(
        hand: List<Card>,
        playedIndices: Set<Int>,
        currentCount: Int,
        peggingPile: List<Card>
    ): Pair<Int, Card>? {
        val opponentCardsRemaining = 4 - playedIndices.size
        return OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = peggingPile,
            opponentCardsRemaining = opponentCardsRemaining
        )
    }

    // Private helper methods

    private fun createPendingReset(
        currentState: PeggingState,
        reset: SubRoundReset,
        finalCount: Int,
        updatedDisplayPile: List<Card>? = null
    ): PendingResetState {
        val scoreAwarded = if (!reset.resetFor31) {
            when (reset.goPointTo) {
                Player.PLAYER, Player.OPPONENT -> 1
                else -> 0
            }
        } else {
            0
        }

        return PendingResetState(
            pile = (updatedDisplayPile ?: currentState.peggingDisplayPile).toList(),
            finalCount = if (reset.resetFor31) 31 else finalCount,
            scoreAwarded = scoreAwarded,
            resetData = reset
        )
    }

    private fun buildPlayCardStatusMessage(
        card: Card,
        isPlayer: Boolean,
        pts: PeggingPoints
    ): String {
        val playerName = if (isPlayer) "You" else "Opponent"
        var message = "Played ${card.getSymbol()} by $playerName"

        if (pts.fifteen > 0) message += "\nScored 2 for 15"
        if (pts.thirtyOne > 0) message += "\nScored 2 for 31"
        if (pts.pairPoints > 0) {
            val pairMsg = when (pts.sameRankCount) {
                2 -> "2 for a pair"
                3 -> "6 for three-of-a-kind"
                else -> "12 for four-of-a-kind"
            }
            message += "\nScored $pairMsg"
        }
        if (pts.runPoints > 0) message += "\nScored ${pts.runPoints} for a run"

        return message
    }
}

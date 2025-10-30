package com.brianhenning.cribbage.game.state

import com.brianhenning.cribbage.shared.domain.logic.OpponentAI
import com.brianhenning.cribbage.shared.domain.logic.PeggingRoundManager
import com.brianhenning.cribbage.shared.domain.logic.PeggingScorer
import com.brianhenning.cribbage.shared.domain.logic.Player
import com.brianhenning.cribbage.shared.domain.logic.SubRoundReset as SharedSubRoundReset
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank

/**
 * Manages all pegging phase logic and state transitions.
 *
 * Responsibilities:
 * - Turn management (player/opponent)
 * - Card play validation
 * - Pegging count tracking
 * - Go handling (automatic and manual)
 * - Sub-round reset detection
 * - PeggingRoundManager coordination
 * - Opponent AI card selection
 * - Score point calculations (delegates to PeggingScorer)
 */
class PeggingStateManager {

    private var manager: PeggingRoundManager? = null

    /**
     * Start the pegging phase.
     */
    fun startPegging(
        playerHand: List<Card>,
        opponentHand: List<Card>,
        isPlayerDealer: Boolean,
        starterCard: Card?
    ): PeggingStartResult {
        // Non-dealer plays first
        val playerStartsFirst = !isPlayerDealer
        manager = PeggingRoundManager(startingPlayer = if (playerStartsFirst) Player.PLAYER else Player.OPPONENT)

        // Check for "His Heels" (Jack as starter card)
        val hisHeelsPoints = if (starterCard?.rank == Rank.JACK) {
            HisHeelsPoints(
                points = 2,
                isForPlayer = isPlayerDealer
            )
        } else {
            null
        }

        val initialState = PeggingState(
            isPeggingPhase = true,
            isPlayerTurn = playerStartsFirst,
            peggingCount = 0,
            peggingPile = emptyList(),
            peggingDisplayPile = emptyList(),
            playerCardsPlayed = emptySet(),
            opponentCardsPlayed = emptySet(),
            consecutiveGoes = 0,
            lastPlayerWhoPlayed = null,
            pendingReset = null,
            isOpponentActionInProgress = !playerStartsFirst,
            previousPeggingCount = 0
        )

        return PeggingStartResult(
            state = initialState,
            hisHeelsPoints = hisHeelsPoints,
            statusMessage = if (playerStartsFirst) "Pegging phase begins. Your turn!" else "Pegging phase begins. Opponent's turn."
        )
    }

    /**
     * Play a card during the pegging phase.
     */
    fun playCard(
        currentState: PeggingState,
        cardIndex: Int,
        playerHand: List<Card>,
        isPlayer: Boolean
    ): PeggingResult {
        val mgr = manager ?: return PeggingResult.Error("Pegging manager not initialized")

        val hand = if (isPlayer) playerHand else playerHand // Note: opponent hand would be passed differently
        val cardsPlayed = if (isPlayer) currentState.playerCardsPlayed else currentState.opponentCardsPlayed

        if (cardIndex in cardsPlayed) {
            return PeggingResult.Error("Card already played")
        }

        val card = hand.getOrNull(cardIndex) ?: return PeggingResult.Error("Invalid card index")

        // Validate the play
        if (mgr.peggingCount + card.getValue() > 31) {
            return PeggingResult.Error("Card would exceed 31")
        }

        // Save pile and count BEFORE calling onPlay (manager resets immediately if hitting 31)
        val pileBeforePlay = mgr.peggingPile.toList() + card
        val countBeforeReset = mgr.peggingCount + card.getValue()

        // Execute the play
        val outcome = mgr.onPlay(card)

        // Sync manager state to our state
        val newState = syncManagerState(currentState, mgr).copy(
            playerCardsPlayed = if (isPlayer) currentState.playerCardsPlayed + cardIndex else currentState.playerCardsPlayed,
            opponentCardsPlayed = if (!isPlayer) currentState.opponentCardsPlayed + cardIndex else currentState.opponentCardsPlayed,
            peggingDisplayPile = currentState.peggingDisplayPile + card,
            previousPeggingCount = currentState.peggingCount
        )

        // Calculate points scored
        val points = PeggingScorer.pointsForPile(pileBeforePlay, countBeforeReset)

        // Handle reset if needed
        val reset = outcome.reset
        return if (reset != null) {
            handleReset(newState, reset, isPlayer)
        } else {
            // Check if next player can play
            val nextPlayerIsPlayer = (mgr.isPlayerTurn == Player.PLAYER)
            PeggingResult.Success(
                newState = newState,
                pointsScored = points,
                scoredBy = if (isPlayer) "Player" else "Opponent",
                statusMessage = if (isPlayer) "You played ${card.getSymbol()}" else "Opponent played ${card.getSymbol()}",
                nextAction = determineNextAction(newState, playerHand, nextPlayerIsPlayer)
            )
        }
    }

    /**
     * Handle "Go" button press (player cannot play).
     */
    fun handleGo(
        currentState: PeggingState,
        playerHand: List<Card>,
        opponentHand: List<Card>
    ): PeggingResult {
        val mgr = manager ?: return PeggingResult.Error("Pegging manager not initialized")

        val currentPlayerIsPlayer = (mgr.isPlayerTurn == Player.PLAYER)

        // Check if opponent has legal moves
        val opponentLegalMoves = if (currentPlayerIsPlayer) {
            opponentHand.filterIndexed { index, card ->
                !currentState.opponentCardsPlayed.contains(index) && (mgr.peggingCount + card.getValue() <= 31)
            }
        } else {
            playerHand.filterIndexed { index, card ->
                !currentState.playerCardsPlayed.contains(index) && (mgr.peggingCount + card.getValue() <= 31)
            }
        }

        val reset = mgr.onGo(opponentHasLegalMove = opponentLegalMoves.isNotEmpty())

        val newState = syncManagerState(currentState, mgr)

        return if (reset != null) {
            handleReset(newState, reset, currentPlayerIsPlayer)
        } else {
            val nextPlayerIsPlayer = (mgr.isPlayerTurn == Player.PLAYER)
            PeggingResult.Success(
                newState = newState.copy(isOpponentActionInProgress = !nextPlayerIsPlayer),
                pointsScored = null,
                scoredBy = null,
                statusMessage = if (currentPlayerIsPlayer) "You say Go" else "Opponent says Go",
                nextAction = determineNextAction(newState, playerHand, nextPlayerIsPlayer)
            )
        }
    }

    /**
     * Acknowledge a pending reset and continue pegging.
     */
    fun acknowledgeReset(
        currentState: PeggingState
    ): PeggingResult {
        val pendingReset = currentState.pendingReset ?: return PeggingResult.Error("No pending reset")

        val newState = currentState.copy(
            pendingReset = null,
            peggingDisplayPile = emptyList()
        )

        val nextPlayerIsPlayer = pendingReset.resetData.nextPlayerIsPlayer

        return PeggingResult.Success(
            newState = newState.copy(isOpponentActionInProgress = !nextPlayerIsPlayer),
            pointsScored = null,
            scoredBy = null,
            statusMessage = "Reset acknowledged. " + if (nextPlayerIsPlayer) "Your turn!" else "Opponent's turn.",
            nextAction = if (nextPlayerIsPlayer) NextAction.PlayerTurn else NextAction.OpponentTurn
        )
    }

    /**
     * Sync the internal manager state to our PeggingState.
     */
    private fun syncManagerState(currentState: PeggingState, mgr: PeggingRoundManager): PeggingState {
        return currentState.copy(
            isPlayerTurn = mgr.isPlayerTurn == Player.PLAYER,
            peggingCount = mgr.peggingCount,
            peggingPile = mgr.peggingPile.toList(),
            consecutiveGoes = mgr.consecutiveGoes,
            lastPlayerWhoPlayed = when (mgr.lastPlayerWhoPlayed) {
                Player.PLAYER -> "Player"
                Player.OPPONENT -> "Opponent"
                null -> null
            }
        )
    }

    /**
     * Handle a sub-round reset.
     */
    private fun handleReset(state: PeggingState, reset: SharedSubRoundReset, currentPlayerIsPlayer: Boolean): PeggingResult {
        val goPointAwarded = reset.goPointTo != null
        val goPointTo = reset.goPointTo

        val scoreAwarded = if (reset.resetFor31) 2 else if (goPointAwarded) 1 else 0
        val scoredBy = when (goPointTo) {
            Player.PLAYER -> "Player"
            Player.OPPONENT -> "Opponent"
            null -> null
        }

        // Determine next player after reset
        val mgr = manager!!
        val nextPlayerIsPlayer = (mgr.isPlayerTurn == Player.PLAYER)

        // Check if pegging is complete
        val peggingComplete = (state.playerCardsPlayed.size >= 4 && state.opponentCardsPlayed.size >= 4)

        if (peggingComplete) {
            return PeggingResult.PeggingComplete(
                finalState = state.copy(isPeggingPhase = false, pendingReset = null),
                finalPoints = if (scoreAwarded > 0 && goPointTo != null) {
                    FinalPeggingPoints(points = scoreAwarded, isForPlayer = goPointTo == Player.PLAYER)
                } else null
            )
        }

        val resetData = com.brianhenning.cribbage.game.state.SubRoundReset(
            reason = if (reset.resetFor31) "Count reached 31" else "Both players said Go",
            playerCardsRemaining = 4 - state.playerCardsPlayed.size,
            opponentCardsRemaining = 4 - state.opponentCardsPlayed.size,
            nextPlayerIsPlayer = nextPlayerIsPlayer
        )

        val pendingReset = PendingResetState(
            pile = state.peggingPile,
            finalCount = state.peggingCount,
            scoreAwarded = scoreAwarded,
            resetData = resetData
        )

        val newState = state.copy(
            pendingReset = pendingReset,
            peggingCount = 0,
            peggingPile = emptyList()
        )

        return PeggingResult.ResetPending(
            newState = newState,
            resetData = pendingReset,
            statusMessage = if (reset.resetFor31) "31! " else "Go! " +
                    if (scoreAwarded > 0 && scoredBy != null) "$scoredBy scores $scoreAwarded" else "No points"
        )
    }

    /**
     * Determine the next action based on current state.
     */
    private fun determineNextAction(state: PeggingState, playerHand: List<Card>, nextPlayerIsPlayer: Boolean): NextAction {
        if (nextPlayerIsPlayer) {
            val legalMoves = playerHand.filterIndexed { index, card ->
                !state.playerCardsPlayed.contains(index) && (state.peggingCount + card.getValue() <= 31)
            }
            return if (legalMoves.isEmpty()) {
                if (state.playerCardsPlayed.size < 4) {
                    NextAction.PlayerMustGo
                } else {
                    NextAction.PlayerAutoGo
                }
            } else {
                NextAction.PlayerTurn
            }
        } else {
            return NextAction.OpponentTurn
        }
    }

    /**
     * Check if pegging is complete (all cards played).
     */
    fun checkPeggingComplete(state: PeggingState): Boolean {
        return state.playerCardsPlayed.size >= 4 && state.opponentCardsPlayed.size >= 4
    }
}

/**
 * Result of starting the pegging phase.
 */
data class PeggingStartResult(
    val state: PeggingState,
    val hisHeelsPoints: HisHeelsPoints?,
    val statusMessage: String
)

/**
 * Points awarded for "His Heels" (Jack as starter card).
 */
data class HisHeelsPoints(
    val points: Int,
    val isForPlayer: Boolean
)

/**
 * Points awarded at the end of pegging.
 */
data class FinalPeggingPoints(
    val points: Int,
    val isForPlayer: Boolean
)

/**
 * Next action after a pegging move.
 */
enum class NextAction {
    PlayerTurn,
    PlayerMustGo,
    PlayerAutoGo,
    OpponentTurn
}

/**
 * Results from pegging operations.
 */
sealed class PeggingResult {
    data class Success(
        val newState: PeggingState,
        val pointsScored: com.brianhenning.cribbage.shared.domain.logic.PeggingPoints?,
        val scoredBy: String?,
        val statusMessage: String,
        val nextAction: NextAction
    ) : PeggingResult()

    data class ResetPending(
        val newState: PeggingState,
        val resetData: PendingResetState,
        val statusMessage: String
    ) : PeggingResult()

    data class PeggingComplete(
        val finalState: PeggingState,
        val finalPoints: FinalPeggingPoints?
    ) : PeggingResult()

    data class Error(val message: String) : PeggingResult()
}

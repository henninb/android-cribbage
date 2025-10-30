package com.brianhenning.cribbage.game.state

import com.brianhenning.cribbage.shared.domain.logic.CribbageScorer
import com.brianhenning.cribbage.shared.domain.logic.DetailedScoreBreakdown
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.ui.composables.CountingPhase
import com.brianhenning.cribbage.ui.composables.HandScores

/**
 * Manages the hand counting phase with sequential dialog displays.
 *
 * Responsibilities:
 * - Hand counting sequence coordination (non-dealer → dealer → crib)
 * - Dialog state management
 * - Score calculation (delegates to CribbageScorer)
 * - Waiting for user acknowledgment between dialogs
 * - Round completion detection
 */
class HandCountingStateManager(
    private val scoreManager: ScoreManager
) {

    /**
     * Start the hand counting phase.
     * Returns the first dialog to show (non-dealer hand).
     */
    suspend fun startHandCounting(
        playerHand: List<Card>,
        opponentHand: List<Card>,
        cribHand: List<Card>,
        starterCard: Card,
        isPlayerDealer: Boolean
    ): HandCountingResult {
        // Determine which hand is non-dealer vs dealer
        val (nonDealerHand, dealerHand) = if (isPlayerDealer) {
            Pair(opponentHand, playerHand)
        } else {
            Pair(playerHand, opponentHand)
        }

        // Count non-dealer hand
        val nonDealerBreakdown = CribbageScorer.scoreHandWithBreakdown(nonDealerHand, starterCard)

        val initialState = HandCountingState(
            isInHandCountingPhase = true,
            countingPhase = CountingPhase.NON_DEALER,
            handScores = HandScores(
                nonDealerScore = nonDealerBreakdown.totalScore,
                nonDealerBreakdown = nonDealerBreakdown
            ),
            waitingForDialogDismissal = true
        )

        return HandCountingResult.ShowDialog(
            state = initialState,
            phase = CountingPhase.NON_DEALER,
            breakdown = nonDealerBreakdown,
            handToDisplay = nonDealerHand,
            pointsAwarded = nonDealerBreakdown.totalScore,
            isForPlayer = !isPlayerDealer
        )
    }

    /**
     * Dismiss the current counting dialog and advance to the next phase.
     */
    suspend fun dismissDialog(
        currentState: HandCountingState,
        playerHand: List<Card>,
        opponentHand: List<Card>,
        cribHand: List<Card>,
        starterCard: Card,
        isPlayerDealer: Boolean
    ): HandCountingResult {
        return when (currentState.countingPhase) {
            CountingPhase.NON_DEALER -> {
                // Move to dealer hand counting
                val (_, dealerHand) = if (isPlayerDealer) {
                    Pair(opponentHand, playerHand)
                } else {
                    Pair(playerHand, opponentHand)
                }

                val dealerBreakdown = CribbageScorer.scoreHandWithBreakdown(dealerHand, starterCard)

                val newState = currentState.copy(
                    countingPhase = CountingPhase.DEALER,
                    handScores = currentState.handScores.copy(
                        dealerScore = dealerBreakdown.totalScore,
                        dealerBreakdown = dealerBreakdown
                    ),
                    waitingForDialogDismissal = true
                )

                HandCountingResult.ShowDialog(
                    state = newState,
                    phase = CountingPhase.DEALER,
                    breakdown = dealerBreakdown,
                    handToDisplay = dealerHand,
                    pointsAwarded = dealerBreakdown.totalScore,
                    isForPlayer = isPlayerDealer
                )
            }

            CountingPhase.DEALER -> {
                // Move to crib counting
                val cribBreakdown = CribbageScorer.scoreHandWithBreakdown(cribHand, starterCard, isCrib = true)

                val newState = currentState.copy(
                    countingPhase = CountingPhase.CRIB,
                    handScores = currentState.handScores.copy(
                        cribScore = cribBreakdown.totalScore,
                        cribBreakdown = cribBreakdown
                    ),
                    waitingForDialogDismissal = true
                )

                HandCountingResult.ShowDialog(
                    state = newState,
                    phase = CountingPhase.CRIB,
                    breakdown = cribBreakdown,
                    handToDisplay = cribHand,
                    pointsAwarded = cribBreakdown.totalScore,
                    isForPlayer = isPlayerDealer
                )
            }

            CountingPhase.CRIB -> {
                // Hand counting complete
                val newState = currentState.copy(
                    countingPhase = CountingPhase.COMPLETED,
                    waitingForDialogDismissal = false
                )

                val totalPlayerPoints = if (isPlayerDealer) {
                    currentState.handScores.dealerScore + currentState.handScores.cribScore
                } else {
                    currentState.handScores.nonDealerScore
                }

                val totalOpponentPoints = if (isPlayerDealer) {
                    currentState.handScores.nonDealerScore
                } else {
                    currentState.handScores.dealerScore + currentState.handScores.cribScore
                }

                HandCountingResult.Complete(
                    state = newState,
                    totalPlayerPoints = totalPlayerPoints,
                    totalOpponentPoints = totalOpponentPoints
                )
            }

            else -> {
                // Already completed or invalid state
                HandCountingResult.Complete(
                    state = currentState,
                    totalPlayerPoints = 0,
                    totalOpponentPoints = 0
                )
            }
        }
    }

    /**
     * Get the current hand to display based on counting phase.
     */
    fun getCurrentHandToDisplay(
        state: HandCountingState,
        playerHand: List<Card>,
        opponentHand: List<Card>,
        cribHand: List<Card>,
        isPlayerDealer: Boolean
    ): List<Card> {
        return when (state.countingPhase) {
            CountingPhase.NON_DEALER -> if (isPlayerDealer) opponentHand else playerHand
            CountingPhase.DEALER -> if (isPlayerDealer) playerHand else opponentHand
            CountingPhase.CRIB -> cribHand
            else -> emptyList()
        }
    }

    /**
     * Get the title for the current counting dialog.
     */
    fun getCountingTitle(phase: CountingPhase, isPlayerDealer: Boolean): String {
        return when (phase) {
            CountingPhase.NON_DEALER -> if (isPlayerDealer) "Opponent's Hand" else "Your Hand"
            CountingPhase.DEALER -> if (isPlayerDealer) "Your Hand" else "Opponent's Hand"
            CountingPhase.CRIB -> if (isPlayerDealer) "Your Crib" else "Opponent's Crib"
            else -> "Counting Hands"
        }
    }
}

/**
 * Results from hand counting operations.
 */
sealed class HandCountingResult {
    /**
     * Show a dialog for a specific hand.
     */
    data class ShowDialog(
        val state: HandCountingState,
        val phase: CountingPhase,
        val breakdown: DetailedScoreBreakdown,
        val handToDisplay: List<Card>,
        val pointsAwarded: Int,
        val isForPlayer: Boolean
    ) : HandCountingResult()

    /**
     * Hand counting is complete.
     */
    data class Complete(
        val state: HandCountingState,
        val totalPlayerPoints: Int,
        val totalOpponentPoints: Int
    ) : HandCountingResult()
}

package com.brianhenning.cribbage.game.state

import com.brianhenning.cribbage.shared.domain.logic.CribbageScorer
import com.brianhenning.cribbage.shared.domain.logic.DetailedScoreBreakdown
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.ui.composables.CountingPhase
import com.brianhenning.cribbage.ui.composables.HandScores
import com.brianhenning.cribbage.ui.composables.ScoreAnimationState

/**
 * State Manager for hand counting operations.
 * Manages the sequential flow: Non-Dealer → Dealer → Crib → Complete.
 * Follows MVVM best practices - operates on immutable state and returns new state.
 *
 * This is a pure business logic class with no Android dependencies.
 * All methods are testable without the Android framework.
 */
class HandCountingStateManager {

    /**
     * Result of starting hand counting phase
     */
    data class StartCountingResult(
        val countingPhase: CountingPhase,
        val handScores: HandScores,
        val statusMessage: String
    )

    /**
     * Result of counting a single hand (non-dealer, dealer, or crib)
     */
    data class CountHandResult(
        val scoreBreakdown: DetailedScoreBreakdown,
        val pointsAwarded: Int,
        val isForPlayer: Boolean,
        val updatedHandScores: HandScores,
        val animation: ScoreAnimationState?
    )

    /**
     * Result of progressing to next counting phase
     */
    data class ProgressPhaseResult(
        val newPhase: CountingPhase,
        val isComplete: Boolean,
        val statusMessage: String
    )

    /**
     * Starts the hand counting phase.
     * Returns initial state with NON_DEALER phase.
     *
     * @return StartCountingResult
     */
    fun startHandCounting(): StartCountingResult {
        return StartCountingResult(
            countingPhase = CountingPhase.NON_DEALER,
            handScores = HandScores(),
            statusMessage = "Counting non-dealer hand..."
        )
    }

    /**
     * Counts the non-dealer hand.
     * Returns immutable result with score breakdown and points.
     *
     * @param nonDealerHand The non-dealer's hand
     * @param starterCard The starter card
     * @param isPlayerDealer Whether player is the dealer (determines who is non-dealer)
     * @param currentHandScores Current hand scores state
     * @return CountHandResult with score breakdown
     */
    fun countNonDealerHand(
        nonDealerHand: List<Card>,
        starterCard: Card,
        isPlayerDealer: Boolean,
        currentHandScores: HandScores
    ): CountHandResult {
        val breakdown = CribbageScorer.scoreHandWithBreakdown(nonDealerHand, starterCard)

        return CountHandResult(
            scoreBreakdown = breakdown,
            pointsAwarded = breakdown.totalScore,
            isForPlayer = !isPlayerDealer, // Non-dealer is the opponent if player is dealer
            updatedHandScores = currentHandScores.copy(
                nonDealerScore = breakdown.totalScore,
                nonDealerBreakdown = breakdown
            ),
            animation = if (breakdown.totalScore > 0) {
                ScoreAnimationState(breakdown.totalScore, !isPlayerDealer)
            } else {
                null
            }
        )
    }

    /**
     * Counts the non-dealer hand manually (user provides points).
     * Returns result without score breakdown (since user counted).
     *
     * @param manualPoints Points entered by user
     * @param isPlayerDealer Whether player is the dealer (determines who is non-dealer)
     * @param currentHandScores Current hand scores state
     * @return CountHandResult with manual points
     */
    fun countNonDealerHandManually(
        manualPoints: Int,
        isPlayerDealer: Boolean,
        currentHandScores: HandScores
    ): CountHandResult {
        // Create empty breakdown for manual counting
        val emptyBreakdown = DetailedScoreBreakdown(
            totalScore = manualPoints,
            entries = emptyList()
        )

        return CountHandResult(
            scoreBreakdown = emptyBreakdown,
            pointsAwarded = manualPoints,
            isForPlayer = !isPlayerDealer,
            updatedHandScores = currentHandScores.copy(
                nonDealerScore = manualPoints,
                nonDealerBreakdown = emptyBreakdown
            ),
            animation = if (manualPoints > 0) {
                ScoreAnimationState(manualPoints, !isPlayerDealer)
            } else {
                null
            }
        )
    }

    /**
     * Counts the dealer hand.
     * Returns immutable result with score breakdown and points.
     *
     * @param dealerHand The dealer's hand
     * @param starterCard The starter card
     * @param isPlayerDealer Whether player is the dealer
     * @param currentHandScores Current hand scores state
     * @return CountHandResult with score breakdown
     */
    fun countDealerHand(
        dealerHand: List<Card>,
        starterCard: Card,
        isPlayerDealer: Boolean,
        currentHandScores: HandScores
    ): CountHandResult {
        val breakdown = CribbageScorer.scoreHandWithBreakdown(dealerHand, starterCard)

        return CountHandResult(
            scoreBreakdown = breakdown,
            pointsAwarded = breakdown.totalScore,
            isForPlayer = isPlayerDealer, // Dealer is the player if player is dealer
            updatedHandScores = currentHandScores.copy(
                dealerScore = breakdown.totalScore,
                dealerBreakdown = breakdown
            ),
            animation = if (breakdown.totalScore > 0) {
                ScoreAnimationState(breakdown.totalScore, isPlayerDealer)
            } else {
                null
            }
        )
    }

    /**
     * Counts the dealer hand manually (user provides points).
     * Returns result without score breakdown (since user counted).
     *
     * @param manualPoints Points entered by user
     * @param isPlayerDealer Whether player is the dealer
     * @param currentHandScores Current hand scores state
     * @return CountHandResult with manual points
     */
    fun countDealerHandManually(
        manualPoints: Int,
        isPlayerDealer: Boolean,
        currentHandScores: HandScores
    ): CountHandResult {
        val emptyBreakdown = DetailedScoreBreakdown(
            totalScore = manualPoints,
            entries = emptyList()
        )

        return CountHandResult(
            scoreBreakdown = emptyBreakdown,
            pointsAwarded = manualPoints,
            isForPlayer = isPlayerDealer,
            updatedHandScores = currentHandScores.copy(
                dealerScore = manualPoints,
                dealerBreakdown = emptyBreakdown
            ),
            animation = if (manualPoints > 0) {
                ScoreAnimationState(manualPoints, isPlayerDealer)
            } else {
                null
            }
        )
    }

    /**
     * Counts the crib.
     * Returns immutable result with score breakdown and points.
     *
     * @param cribHand The crib hand
     * @param starterCard The starter card
     * @param isPlayerDealer Whether player is the dealer (crib belongs to dealer)
     * @param currentHandScores Current hand scores state
     * @return CountHandResult with score breakdown
     */
    fun countCrib(
        cribHand: List<Card>,
        starterCard: Card,
        isPlayerDealer: Boolean,
        currentHandScores: HandScores
    ): CountHandResult {
        val breakdown = CribbageScorer.scoreHandWithBreakdown(cribHand, starterCard, isCrib = true)

        return CountHandResult(
            scoreBreakdown = breakdown,
            pointsAwarded = breakdown.totalScore,
            isForPlayer = isPlayerDealer, // Crib belongs to dealer
            updatedHandScores = currentHandScores.copy(
                cribScore = breakdown.totalScore,
                cribBreakdown = breakdown
            ),
            animation = if (breakdown.totalScore > 0) {
                ScoreAnimationState(breakdown.totalScore, isPlayerDealer)
            } else {
                null
            }
        )
    }

    /**
     * Counts the crib manually (user provides points).
     * Returns result without score breakdown (since user counted).
     *
     * @param manualPoints Points entered by user
     * @param isPlayerDealer Whether player is the dealer (crib belongs to dealer)
     * @param currentHandScores Current hand scores state
     * @return CountHandResult with manual points
     */
    fun countCribManually(
        manualPoints: Int,
        isPlayerDealer: Boolean,
        currentHandScores: HandScores
    ): CountHandResult {
        val emptyBreakdown = DetailedScoreBreakdown(
            totalScore = manualPoints,
            entries = emptyList()
        )

        return CountHandResult(
            scoreBreakdown = emptyBreakdown,
            pointsAwarded = manualPoints,
            isForPlayer = isPlayerDealer,
            updatedHandScores = currentHandScores.copy(
                cribScore = manualPoints,
                cribBreakdown = emptyBreakdown
            ),
            animation = if (manualPoints > 0) {
                ScoreAnimationState(manualPoints, isPlayerDealer)
            } else {
                null
            }
        )
    }

    /**
     * Progresses to the next counting phase.
     * NON_DEALER → DEALER → CRIB → COMPLETED
     *
     * @param currentPhase Current counting phase
     * @return ProgressPhaseResult with next phase
     */
    fun progressToNextPhase(currentPhase: CountingPhase): ProgressPhaseResult {
        return when (currentPhase) {
            CountingPhase.NON_DEALER -> ProgressPhaseResult(
                newPhase = CountingPhase.DEALER,
                isComplete = false,
                statusMessage = "Counting dealer hand..."
            )
            CountingPhase.DEALER -> ProgressPhaseResult(
                newPhase = CountingPhase.CRIB,
                isComplete = false,
                statusMessage = "Counting crib..."
            )
            CountingPhase.CRIB -> ProgressPhaseResult(
                newPhase = CountingPhase.COMPLETED,
                isComplete = true,
                statusMessage = "Hand counting complete. Preparing next round..."
            )
            CountingPhase.COMPLETED, CountingPhase.NONE -> ProgressPhaseResult(
                newPhase = CountingPhase.NONE,
                isComplete = true,
                statusMessage = ""
            )
        }
    }

    /**
     * Resets hand counting state for a new round.
     *
     * @return StartCountingResult with reset state
     */
    fun resetHandCounting(): StartCountingResult {
        return StartCountingResult(
            countingPhase = CountingPhase.NONE,
            handScores = HandScores(),
            statusMessage = ""
        )
    }

    /**
     * Determines which hands to count based on dealer.
     * Returns (nonDealerHand, dealerHand) tuple.
     *
     * @param playerHand Player's hand
     * @param opponentHand Opponent's hand
     * @param isPlayerDealer Whether player is the dealer
     * @return Pair of (nonDealerHand, dealerHand)
     */
    fun determineHandOrder(
        playerHand: List<Card>,
        opponentHand: List<Card>,
        isPlayerDealer: Boolean
    ): Pair<List<Card>, List<Card>> {
        return if (isPlayerDealer) {
            Pair(opponentHand, playerHand) // Opponent is non-dealer
        } else {
            Pair(playerHand, opponentHand) // Player is non-dealer
        }
    }
}

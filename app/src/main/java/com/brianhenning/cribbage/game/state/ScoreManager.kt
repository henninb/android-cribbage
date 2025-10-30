package com.brianhenning.cribbage.game.state

import com.brianhenning.cribbage.game.repository.PreferencesRepository
import com.brianhenning.cribbage.shared.domain.logic.PeggingPoints
import com.brianhenning.cribbage.ui.composables.ScoreAnimationState

/**
 * Manages score tracking, game over detection, and match statistics.
 *
 * Responsibilities:
 * - Score updates (player/opponent)
 * - Game over detection (> 120 points)
 * - Skunk detection (single < 91, double < 61)
 * - Match statistics tracking
 * - Score animation state management
 * - Winner determination
 */
class ScoreManager(
    private val preferencesRepository: PreferencesRepository
) {

    /**
     * Add points for a player or opponent and check for game over.
     *
     * @return ScoreResult indicating the updated scores or game over state
     */
    fun addScore(
        currentPlayerScore: Int,
        currentOpponentScore: Int,
        pointsToAdd: Int,
        isForPlayer: Boolean,
        matchStats: MatchStats,
        createAnimation: Boolean = true
    ): ScoreResult {
        if (pointsToAdd <= 0) {
            return ScoreResult.ScoreUpdated(
                newPlayerScore = currentPlayerScore,
                newOpponentScore = currentOpponentScore,
                animation = null,
                matchStats = matchStats
            )
        }

        val newPlayerScore = if (isForPlayer) currentPlayerScore + pointsToAdd else currentPlayerScore
        val newOpponentScore = if (!isForPlayer) currentOpponentScore + pointsToAdd else currentOpponentScore

        // Create animation if requested
        val animation = if (createAnimation) {
            createScoreAnimation(pointsToAdd, isForPlayer)
        } else {
            null
        }

        // Check for game over
        val gameOverResult = checkGameOver(
            playerScore = newPlayerScore,
            opponentScore = newOpponentScore,
            matchStats = matchStats,
            isPlayerDealer = false // Will be passed in from the caller
        )

        return if (gameOverResult != null) {
            ScoreResult.GameOver(
                winnerModalData = gameOverResult.winnerModalData,
                matchStats = gameOverResult.matchStats,
                newPlayerScore = newPlayerScore,
                newOpponentScore = newOpponentScore
            )
        } else {
            ScoreResult.ScoreUpdated(
                newPlayerScore = newPlayerScore,
                newOpponentScore = newOpponentScore,
                animation = animation,
                matchStats = matchStats
            )
        }
    }

    /**
     * Award pegging points and return score breakdown messages.
     */
    fun awardPeggingPoints(
        currentPlayerScore: Int,
        currentOpponentScore: Int,
        isPlayer: Boolean,
        pts: PeggingPoints,
        matchStats: MatchStats
    ): PeggingScoreResult {
        var totalAwarded = 0
        val messages = mutableListOf<String>()

        fun award(points: Int, message: String) {
            if (points > 0) {
                totalAwarded += points
                messages.add(message)
            }
        }

        if (pts.fifteen > 0) {
            award(pts.fifteen, if (isPlayer) "Scored 2 for 15 by You!" else "Scored 2 for 15 by Opponent!")
        }
        if (pts.thirtyOne > 0) {
            award(pts.thirtyOne, if (isPlayer) "Scored 2 for 31 by You!" else "Scored 2 for 31 by Opponent!")
        }
        if (pts.pairPoints > 0) {
            val msg = when (pts.sameRankCount) {
                2 -> "2 for a pair"
                3 -> "6 for three-of-a-kind"
                else -> "12 for four-of-a-kind"
            }
            award(pts.pairPoints, if (isPlayer) "Scored $msg by You!" else "Scored $msg by Opponent!")
        }
        if (pts.runPoints > 0) {
            award(pts.runPoints, if (isPlayer) "Scored ${pts.runPoints} for a run by You!" else "Scored ${pts.runPoints} for a run by Opponent!")
        }

        if (totalAwarded == 0) {
            return PeggingScoreResult(
                scoreResult = ScoreResult.ScoreUpdated(
                    newPlayerScore = currentPlayerScore,
                    newOpponentScore = currentOpponentScore,
                    animation = null,
                    matchStats = matchStats
                ),
                messages = emptyList()
            )
        }

        val scoreResult = addScore(
            currentPlayerScore = currentPlayerScore,
            currentOpponentScore = currentOpponentScore,
            pointsToAdd = totalAwarded,
            isForPlayer = isPlayer,
            matchStats = matchStats,
            createAnimation = true
        )

        return PeggingScoreResult(
            scoreResult = scoreResult,
            messages = messages
        )
    }

    /**
     * Check if the game is over (score > 120).
     *
     * @return GameOverResult if game is over, null otherwise
     */
    fun checkGameOver(
        playerScore: Int,
        opponentScore: Int,
        matchStats: MatchStats,
        isPlayerDealer: Boolean
    ): GameOverResult? {
        if (playerScore <= 120 && opponentScore <= 120) {
            return null
        }

        val playerWins = playerScore > opponentScore
        val loserScore = if (playerWins) opponentScore else playerScore

        // Check for skunks: single skunk < 91, double skunk < 61
        val isDoubleSkunk = loserScore < 61
        val isSingleSkunk = loserScore < 91 && !isDoubleSkunk
        val skunked = isSingleSkunk || isDoubleSkunk

        // Update match stats
        val updatedStats = if (playerWins) {
            matchStats.copy(
                gamesWon = matchStats.gamesWon + 1,
                skunksFor = matchStats.skunksFor + if (isSingleSkunk) 1 else 0,
                doubleSkunksFor = matchStats.doubleSkunksFor + if (isDoubleSkunk) 1 else 0
            )
        } else {
            matchStats.copy(
                gamesLost = matchStats.gamesLost + 1,
                skunksAgainst = matchStats.skunksAgainst + if (isSingleSkunk) 1 else 0,
                doubleSkunksAgainst = matchStats.doubleSkunksAgainst + if (isDoubleSkunk) 1 else 0
            )
        }

        // Persist match stats and next dealer (loser deals next)
        preferencesRepository.saveMatchStats(updatedStats)
        preferencesRepository.setNextDealerIsPlayer(!playerWins)

        val winnerModalData = WinnerModalData(
            playerWon = playerWins,
            playerScore = playerScore,
            opponentScore = opponentScore,
            wasSkunk = skunked,
            wasDoubleSkunk = isDoubleSkunk,
            gamesWon = updatedStats.gamesWon,
            gamesLost = updatedStats.gamesLost,
            skunksFor = updatedStats.skunksFor,
            skunksAgainst = updatedStats.skunksAgainst,
            doubleSkunksFor = updatedStats.doubleSkunksFor,
            doubleSkunksAgainst = updatedStats.doubleSkunksAgainst
        )

        return GameOverResult(
            winnerModalData = winnerModalData,
            matchStats = updatedStats
        )
    }

    /**
     * Create a score animation state for the given points and player.
     */
    fun createScoreAnimation(points: Int, isPlayer: Boolean): ScoreAnimationState {
        return ScoreAnimationState(
            points = points,
            isPlayer = isPlayer,
            timestamp = System.currentTimeMillis()
        )
    }
}

/**
 * Result of a score update operation.
 */
sealed class ScoreResult {
    /**
     * Score was updated successfully.
     */
    data class ScoreUpdated(
        val newPlayerScore: Int,
        val newOpponentScore: Int,
        val animation: ScoreAnimationState?,
        val matchStats: MatchStats
    ) : ScoreResult()

    /**
     * Game is over after this score update.
     */
    data class GameOver(
        val winnerModalData: WinnerModalData,
        val matchStats: MatchStats,
        val newPlayerScore: Int,
        val newOpponentScore: Int
    ) : ScoreResult()
}

/**
 * Result of awarding pegging points with messages.
 */
data class PeggingScoreResult(
    val scoreResult: ScoreResult,
    val messages: List<String>
)

/**
 * Result when game is over.
 */
data class GameOverResult(
    val winnerModalData: WinnerModalData,
    val matchStats: MatchStats
)

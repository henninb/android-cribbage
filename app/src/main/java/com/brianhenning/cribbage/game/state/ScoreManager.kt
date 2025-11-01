package com.brianhenning.cribbage.game.state

import com.brianhenning.cribbage.game.logic.GameScoreManager
import com.brianhenning.cribbage.game.repository.PreferencesRepository
import com.brianhenning.cribbage.ui.composables.ScoreAnimationState

/**
 * State Manager for score tracking, game over detection, and match statistics.
 * Follows MVVM best practices - operates on immutable state and returns new state.
 *
 * This is a pure business logic class with no Android dependencies (except repository).
 * All methods are testable without the Android framework.
 */
class ScoreManager(
    private val preferencesRepository: PreferencesRepository
) {

    /**
     * Result of adding score - contains updated state
     */
    sealed class ScoreResult {
        data class ScoreUpdated(
            val newPlayerScore: Int,
            val newOpponentScore: Int,
            val animation: ScoreAnimationState?,
            val matchStats: MatchStats
        ) : ScoreResult()

        data class GameOver(
            val newPlayerScore: Int,
            val newOpponentScore: Int,
            val winnerModalData: WinnerModalData,
            val matchStats: MatchStats
        ) : ScoreResult()
    }

    /**
     * Adds points to player or opponent score and checks for game over.
     * Returns immutable result with updated state.
     *
     * @param currentPlayerScore Current player score
     * @param currentOpponentScore Current opponent score
     * @param pointsToAdd Points to add
     * @param isForPlayer True if adding to player, false for opponent
     * @param currentMatchStats Current match statistics
     * @return ScoreResult with updated scores and potential game over
     */
    fun addScore(
        currentPlayerScore: Int,
        currentOpponentScore: Int,
        pointsToAdd: Int,
        isForPlayer: Boolean,
        currentMatchStats: MatchStats
    ): ScoreResult {
        if (pointsToAdd <= 0) {
            // No points to add, return current state
            return ScoreResult.ScoreUpdated(
                newPlayerScore = currentPlayerScore,
                newOpponentScore = currentOpponentScore,
                animation = null,
                matchStats = currentMatchStats
            )
        }

        // Calculate new scores
        val newPlayerScore = if (isForPlayer) currentPlayerScore + pointsToAdd else currentPlayerScore
        val newOpponentScore = if (isForPlayer) currentOpponentScore else currentOpponentScore + pointsToAdd

        // Create score animation
        val animation = createScoreAnimation(pointsToAdd, isForPlayer)

        // Check for game over
        val gameOverResult = GameScoreManager.checkGameOver(newPlayerScore, newOpponentScore)

        return if (gameOverResult.isGameOver) {
            // Game over - update match stats and return game over result
            val updatedStats = updateMatchStats(currentMatchStats, gameOverResult)

            // Persist match stats and next dealer preference
            preferencesRepository.saveMatchStats(
                PreferencesRepository.MatchStats(
                    gamesWon = updatedStats.gamesWon,
                    gamesLost = updatedStats.gamesLost,
                    skunksFor = updatedStats.skunksFor,
                    skunksAgainst = updatedStats.skunksAgainst,
                    doubleSkunksFor = updatedStats.doubleSkunksFor,
                    doubleSkunksAgainst = updatedStats.doubleSkunksAgainst
                )
            )
            preferencesRepository.saveNextDealerIsPlayer(!gameOverResult.playerWins)

            ScoreResult.GameOver(
                newPlayerScore = newPlayerScore,
                newOpponentScore = newOpponentScore,
                winnerModalData = WinnerModalData(
                    playerWon = gameOverResult.playerWins,
                    playerScore = newPlayerScore,
                    opponentScore = newOpponentScore,
                    wasSkunk = gameOverResult.isSkunked,
                    gamesWon = updatedStats.gamesWon,
                    gamesLost = updatedStats.gamesLost,
                    skunksFor = updatedStats.skunksFor,
                    skunksAgainst = updatedStats.skunksAgainst,
                    doubleSkunksFor = updatedStats.doubleSkunksFor,
                    doubleSkunksAgainst = updatedStats.doubleSkunksAgainst
                ),
                matchStats = updatedStats
            )
        } else {
            // Game continues - return updated scores
            ScoreResult.ScoreUpdated(
                newPlayerScore = newPlayerScore,
                newOpponentScore = newOpponentScore,
                animation = animation,
                matchStats = currentMatchStats
            )
        }
    }

    /**
     * Creates a score animation state for the given points and player.
     *
     * @param points Points scored
     * @param isPlayer True if player scored, false if opponent
     * @return ScoreAnimationState or null if no points
     */
    fun createScoreAnimation(points: Int, isPlayer: Boolean): ScoreAnimationState? {
        return if (points > 0) {
            ScoreAnimationState(points, isPlayer)
        } else {
            null
        }
    }

    /**
     * Updates match statistics based on game outcome.
     * Returns new immutable MatchStats.
     *
     * @param currentStats Current match statistics
     * @param gameResult Game over result
     * @return Updated MatchStats
     */
    private fun updateMatchStats(
        currentStats: MatchStats,
        gameResult: GameScoreManager.GameOverResult
    ): MatchStats {
        if (!gameResult.isGameOver) {
            return currentStats
        }

        return if (gameResult.playerWins) {
            currentStats.copy(
                gamesWon = currentStats.gamesWon + 1,
                skunksFor = if (gameResult.isSingleSkunk) currentStats.skunksFor + 1 else currentStats.skunksFor,
                doubleSkunksFor = if (gameResult.isDoubleSkunk) currentStats.doubleSkunksFor + 1 else currentStats.doubleSkunksFor
            )
        } else {
            currentStats.copy(
                gamesLost = currentStats.gamesLost + 1,
                skunksAgainst = if (gameResult.isSingleSkunk) currentStats.skunksAgainst + 1 else currentStats.skunksAgainst,
                doubleSkunksAgainst = if (gameResult.isDoubleSkunk) currentStats.doubleSkunksAgainst + 1 else currentStats.doubleSkunksAgainst
            )
        }
    }

    /**
     * Checks if the game is over without adding points.
     * Useful for validation before taking actions.
     *
     * @param playerScore Current player score
     * @param opponentScore Current opponent score
     * @return GameOverResult
     */
    fun checkGameOver(playerScore: Int, opponentScore: Int): GameScoreManager.GameOverResult {
        return GameScoreManager.checkGameOver(playerScore, opponentScore)
    }

    /**
     * Loads match statistics from persistent storage.
     *
     * @return MatchStats
     */
    fun loadMatchStats(): MatchStats {
        val stats = preferencesRepository.loadMatchStats()
        return MatchStats(
            gamesWon = stats.gamesWon,
            gamesLost = stats.gamesLost,
            skunksFor = stats.skunksFor,
            skunksAgainst = stats.skunksAgainst,
            doubleSkunksFor = stats.doubleSkunksFor,
            doubleSkunksAgainst = stats.doubleSkunksAgainst
        )
    }
}

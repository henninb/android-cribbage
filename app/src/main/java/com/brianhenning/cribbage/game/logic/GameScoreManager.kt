package com.brianhenning.cribbage.game.logic

/**
 * Utility class for managing game score state and determining game outcomes.
 * Handles game-over detection, skunk determination, and winner calculation.
 */
object GameScoreManager {

    /**
     * Result of checking if the game is over
     */
    data class GameOverResult(
        val isGameOver: Boolean,
        val playerWins: Boolean = false,
        val isSingleSkunk: Boolean = false,
        val isDoubleSkunk: Boolean = false,
        val loserScore: Int = 0
    ) {
        val isSkunked: Boolean = isSingleSkunk || isDoubleSkunk
    }

    /**
     * Updated match statistics after a game ends
     */
    data class UpdatedMatchStats(
        val gamesWon: Int,
        val gamesLost: Int,
        val skunksFor: Int,
        val skunksAgainst: Int,
        val doubleSkunksFor: Int,
        val doubleSkunksAgainst: Int
    )

    /**
     * Checks if the game is over and calculates skunk status
     * @param playerScore Current player score
     * @param opponentScore Current opponent score
     * @return GameOverResult with game status details
     */
    fun checkGameOver(playerScore: Int, opponentScore: Int): GameOverResult {
        if (playerScore <= 120 && opponentScore <= 120) {
            return GameOverResult(isGameOver = false)
        }

        val playerWins = playerScore > opponentScore
        val loserScore = if (playerWins) opponentScore else playerScore

        // Check for skunks: single skunk < 91, double skunk < 61
        val isDoubleSkunk = loserScore < 61
        val isSingleSkunk = loserScore < 91 && !isDoubleSkunk

        return GameOverResult(
            isGameOver = true,
            playerWins = playerWins,
            isSingleSkunk = isSingleSkunk,
            isDoubleSkunk = isDoubleSkunk,
            loserScore = loserScore
        )
    }

    /**
     * Updates match statistics based on game outcome
     * @param currentStats Current match statistics
     * @param gameResult Result of the completed game
     * @return Updated match statistics
     */
    fun updateMatchStats(
        currentStats: UpdatedMatchStats,
        gameResult: GameOverResult
    ): UpdatedMatchStats {
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
     * Formats a skunk message based on game result
     * @param gameResult Result of the completed game
     * @return Formatted skunk message (empty string if no skunk)
     */
    fun formatSkunkMessage(gameResult: GameOverResult): String {
        return when {
            gameResult.isDoubleSkunk -> " Double Skunk!"
            gameResult.isSingleSkunk -> " Skunk!"
            else -> ""
        }
    }

    /**
     * Formats the winner name
     * @param playerWins Whether the player won
     * @return "You" if player wins, "Opponent" otherwise
     */
    fun formatWinner(playerWins: Boolean): String {
        return if (playerWins) "You" else "Opponent"
    }
}

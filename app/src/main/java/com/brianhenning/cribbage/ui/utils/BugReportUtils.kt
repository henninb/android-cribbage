package com.brianhenning.cribbage.ui.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.brianhenning.cribbage.shared.domain.logic.PeggingRoundManager
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.ui.composables.CountingPhase
import com.brianhenning.cribbage.ui.composables.GamePhase
import com.brianhenning.cribbage.ui.composables.HandScores

/**
 * Utility functions for generating and sending bug reports.
 */
object BugReportUtils {

    /**
     * Extension function to get card symbol
     */
    private fun Card.symbol(): String = this.getSymbol()

    /**
     * Extension function to get symbols for a list of cards
     */
    private fun List<Card>.symbols(): String = this.joinToString(",") { it.getSymbol() }

    /**
     * Builds a comprehensive bug report body with game state snapshot.
     */
    fun buildBugReportBody(
        context: Context,
        playerScore: Int,
        opponentScore: Int,
        isPlayerDealer: Boolean,
        starterCard: Card?,
        peggingCount: Int,
        peggingPile: List<Card>,
        playerHand: List<Card>,
        opponentHand: List<Card>,
        cribHand: List<Card>,
        matchSummary: String,
        gameStatus: String,
        // Additional debug state
        currentPhase: GamePhase,
        gameStarted: Boolean,
        gameOver: Boolean,
        isPlayerTurn: Boolean,
        isPeggingPhase: Boolean,
        isInHandCountingPhase: Boolean,
        selectedCards: Set<Int>,
        playerCardsPlayed: Set<Int>,
        opponentCardsPlayed: Set<Int>,
        peggingDisplayPile: List<Card>,
        dealButtonEnabled: Boolean,
        selectCribButtonEnabled: Boolean,
        playCardButtonEnabled: Boolean,
        showHandCountingButton: Boolean,
        showGoButton: Boolean,
        peggingManager: PeggingRoundManager?,
        countingPhase: CountingPhase,
        handScores: HandScores,
        waitingForDialogDismissal: Boolean,
        consecutiveGoes: Int,
        lastPlayerWhoPlayed: String?,
    ): String {
        val manufacturer = android.os.Build.MANUFACTURER
        val model = android.os.Build.MODEL
        val sdk = android.os.Build.VERSION.SDK_INT
        val appVersion = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) { "unknown" }

        return buildString {
            appendLine("Please describe the bug:")
            appendLine()
            appendLine("Expected:")
            appendLine()
            appendLine("Actual:")
            appendLine()
            appendLine("Steps to reproduce:")
            appendLine("1.")
            appendLine("2.")
            appendLine("3.")
            appendLine()
            appendLine("— App/Device —")
            appendLine("App: $appVersion")
            appendLine("Device: $manufacturer $model (SDK $sdk)")
            appendLine()
            appendLine("— Game Snapshot —")
            appendLine("Scores: You $playerScore, Opponent $opponentScore")
            appendLine("Dealer: ${if (isPlayerDealer) "You" else "Opponent"}")
            appendLine("Starter: ${starterCard?.symbol() ?: "(none)"}")
            appendLine("Pegging count: $peggingCount")
            appendLine("Pegging pile: ${peggingPile.symbols()}")
            appendLine("Your hand: ${playerHand.symbols()}")
            appendLine("Opponent hand: ${opponentHand.symbols()}")
            appendLine("Crib: ${cribHand.symbols()}")
            appendLine("Match: $matchSummary")
            appendLine()
            appendLine("— Detailed Debug State —")
            appendLine("Game Phase: $currentPhase")
            appendLine("Game Started: $gameStarted")
            appendLine("Game Over: $gameOver")
            appendLine("Is Player Turn: $isPlayerTurn")
            appendLine("Is Pegging Phase: $isPeggingPhase")
            appendLine("Is In Hand Counting Phase: $isInHandCountingPhase")
            appendLine()
            appendLine("— Card State —")
            appendLine("Selected Cards: ${selectedCards.toList().sorted()}")
            appendLine("Player Cards Played: ${playerCardsPlayed.toList().sorted()}")
            appendLine("Opponent Cards Played: ${opponentCardsPlayed.toList().sorted()}")
            appendLine("Pegging Display Pile: ${peggingDisplayPile.symbols()}")
            appendLine()
            appendLine("— Button State —")
            appendLine("Deal Button Enabled: $dealButtonEnabled")
            appendLine("Select Crib Button Enabled: $selectCribButtonEnabled")
            appendLine("Play Card Button Enabled: $playCardButtonEnabled")
            appendLine("Show Hand Counting Button: $showHandCountingButton")
            appendLine("Show Go Button: $showGoButton")
            appendLine()
            appendLine("— Pegging Manager State —")
            val mgr = peggingManager
            if (mgr != null) {
                appendLine("Manager Is Player Turn: ${mgr.isPlayerTurn}")
                appendLine("Manager Pegging Count: ${mgr.peggingCount}")
                appendLine("Manager Pegging Pile: ${mgr.peggingPile.symbols()}")
                appendLine("Manager Consecutive Goes: ${mgr.consecutiveGoes}")
                appendLine("Manager Last Player Who Played: ${mgr.lastPlayerWhoPlayed}")
            } else {
                appendLine("Pegging Manager: null")
            }
            appendLine()
            appendLine("— Counting State —")
            appendLine("Counting Phase: $countingPhase")
            appendLine("Hand Scores: NonDealer=${handScores.nonDealerScore}, Dealer=${handScores.dealerScore}, Crib=${handScores.cribScore}")
            appendLine("Waiting For Dialog Dismissal: $waitingForDialogDismissal")
            appendLine()
            appendLine("— Additional Info —")
            appendLine("Consecutive Goes: $consecutiveGoes")
            appendLine("Last Player Who Played: $lastPlayerWhoPlayed")
            appendLine()
            appendLine("Status log:\n$gameStatus")
        }
    }

    /**
     * Sends a bug report email using the device's email client.
     */
    fun sendBugReportEmail(context: Context, to: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            context.startActivity(Intent.createChooser(intent, subject))
        } catch (e: ActivityNotFoundException) {
            // No email client available - silently ignore
        }
    }
}

package com.brianhenning.cribbage.game.logic

import com.brianhenning.cribbage.shared.domain.logic.Player
import com.brianhenning.cribbage.shared.domain.logic.dealerFromCut
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.createDeck

/**
 * Utility class for managing dealer selection in Cribbage.
 * Handles cut card logic and dealer determination based on game rules.
 */
object DealerManager {

    /**
     * Result of dealer determination via cut cards
     */
    data class DealerCutResult(
        val isPlayerDealer: Boolean,
        val playerCutCard: Card,
        val opponentCutCard: Card
    )

    /**
     * Determines dealer by cutting cards from a shuffled deck.
     * Keeps trying until there is a clear winner (no ties).
     *
     * @return DealerCutResult with dealer determination and cut cards
     */
    fun determineDealer(): DealerCutResult {
        var playerCut: Card
        var opponentCut: Card
        var dealer: Player?

        // Keep cutting until we get a clear winner (no ties)
        do {
            val deck = createDeck().shuffled()
            playerCut = deck[0]
            opponentCut = deck[1]
            dealer = dealerFromCut(playerCut, opponentCut)
        } while (dealer == null)

        return DealerCutResult(
            isPlayerDealer = (dealer == Player.PLAYER),
            playerCutCard = playerCut,
            opponentCutCard = opponentCut
        )
    }

    /**
     * Formats a message explaining who won the cut and becomes dealer.
     *
     * @param result The dealer cut result
     * @return Formatted message string
     */
    fun formatDealerCutMessage(result: DealerCutResult): String {
        val winner = if (result.isPlayerDealer) "You" else "Opponent"
        val dealerText = if (result.isPlayerDealer) "You are dealer" else "Opponent is dealer"
        return "Cut: You drew ${result.playerCutCard.rank}, Opponent drew ${result.opponentCutCard.rank}. $winner won the cut. $dealerText."
    }

    /**
     * Formats a message for when dealer is determined by previous game result.
     *
     * @param isPlayerDealer Whether the player is the dealer
     * @return Formatted message string
     */
    fun formatPreviousGameDealerMessage(isPlayerDealer: Boolean): String {
        return if (isPlayerDealer) "You are dealer" else "Opponent is dealer"
    }
}

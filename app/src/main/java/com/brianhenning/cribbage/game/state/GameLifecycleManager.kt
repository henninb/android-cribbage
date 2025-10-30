package com.brianhenning.cribbage.game.state

import android.content.Context
import com.brianhenning.cribbage.R
import com.brianhenning.cribbage.game.repository.PreferencesRepository
import com.brianhenning.cribbage.shared.domain.logic.OpponentAI
import com.brianhenning.cribbage.shared.domain.logic.Player
import com.brianhenning.cribbage.shared.domain.logic.dealSixToEach
import com.brianhenning.cribbage.shared.domain.logic.dealerFromCut
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.createDeck

/**
 * Manages game lifecycle operations: start, end, deal, crib selection, and round transitions.
 *
 * Responsibilities:
 * - Game start/end
 * - Dealer determination (cut for dealer)
 * - Card dealing
 * - Crib selection (player + opponent AI)
 * - Starter card drawing
 * - Round transitions (toggle dealer)
 * - Phase transitions
 */
class GameLifecycleManager(
    private val context: Context,
    private val preferencesRepository: PreferencesRepository
) {

    /**
     * Start a new game.
     * Determines dealer by either:
     * - Using saved "nextDealerIsPlayer" from previous game (loser deals next)
     * - Cutting for dealer if no previous game exists
     */
    fun startNewGame(): GameLifecycleResult.NewGameStarted {
        val nextDealerIsPlayer = preferencesRepository.getNextDealerIsPlayer()

        return if (nextDealerIsPlayer != null) {
            // Use saved dealer from previous game
            GameLifecycleResult.NewGameStarted(
                isPlayerDealer = nextDealerIsPlayer,
                cutPlayerCard = null,
                cutOpponentCard = null,
                showCutForDealer = false,
                statusMessage = context.getString(
                    R.string.dealer_set_by_previous,
                    if (nextDealerIsPlayer) "You are dealer" else "Opponent is dealer"
                )
            )
        } else {
            // Cut for dealer
            val (playerCutCard, opponentCutCard, dealerIsPlayer) = performCutForDealer()

            // Save cut cards for UI display
            preferencesRepository.saveCutCards(playerCutCard, opponentCutCard)

            GameLifecycleResult.NewGameStarted(
                isPlayerDealer = dealerIsPlayer,
                cutPlayerCard = playerCutCard,
                cutOpponentCard = opponentCutCard,
                showCutForDealer = true,
                statusMessage = "Cut for deal: You ${playerCutCard.getSymbol()} vs Opponent ${opponentCutCard.getSymbol()}\n" +
                        if (dealerIsPlayer) "You are dealer" else "Opponent is dealer"
            )
        }
    }

    /**
     * Perform cut for dealer.
     * Lower card deals first. Re-cut if tied.
     */
    private fun performCutForDealer(): Triple<Card, Card, Boolean> {
        var playerCutCard: Card
        var opponentCutCard: Card
        var dealer: Player?

        do {
            val deck = createDeck().shuffled()
            playerCutCard = deck[0]
            opponentCutCard = deck[1]
            dealer = dealerFromCut(playerCutCard, opponentCutCard)
        } while (dealer == null)

        val dealerIsPlayer = (dealer == Player.PLAYER)
        return Triple(playerCutCard, opponentCutCard, dealerIsPlayer)
    }

    /**
     * End the current game and reset all state.
     */
    fun endGame(): EndGameResult {
        return EndGameResult(
            statusMessage = context.getString(R.string.welcome_to_cribbage)
        )
    }

    /**
     * Deal cards to player and opponent (6 each).
     */
    fun dealCards(isPlayerDealer: Boolean): DealResult {
        val deck = createDeck().shuffled().toMutableList()
        val result = dealSixToEach(deck, playerIsDealer = isPlayerDealer)

        val sortedPlayerHand = result.playerHand.sortedWith(
            compareBy({ it.rank.ordinal }, { it.suit.ordinal })
        )

        val statusMessage = if (isPlayerDealer) {
            context.getString(R.string.select_cards_for_your_crib)
        } else {
            context.getString(R.string.select_cards_for_opponent_crib)
        }

        return DealResult(
            playerHand = sortedPlayerHand,
            opponentHand = result.opponentHand,
            remainingDeck = result.remainingDeck,
            statusMessage = statusMessage
        )
    }

    /**
     * Select cards for the crib and draw the starter card.
     */
    fun selectCardsForCrib(
        playerHand: List<Card>,
        opponentHand: List<Card>,
        selectedIndices: Set<Int>,
        isPlayerDealer: Boolean,
        remainingDeck: List<Card>
    ): CribSelectionResult {
        if (selectedIndices.size != 2) {
            return CribSelectionResult.Error(context.getString(R.string.select_exactly_two))
        }

        // Get player's selected cards
        val selectedPlayerCards = selectedIndices.toList().sortedDescending().map { playerHand[it] }

        // Use AI to choose opponent's crib cards
        val opponentCribCards = OpponentAI.chooseCribCards(opponentHand, !isPlayerDealer)

        // Combine into crib
        val cribHand = selectedPlayerCards + opponentCribCards

        // Remove selected cards from hands
        val updatedPlayerHand = playerHand.filterIndexed { index, _ -> !selectedIndices.contains(index) }
            .sortedWith(compareBy({ it.rank.ordinal }, { it.suit.ordinal }))

        val updatedOpponentHand = opponentHand.filter { !opponentCribCards.contains(it) }
            .sortedWith(compareBy({ it.rank.ordinal }, { it.suit.ordinal }))

        // Draw starter card from remaining deck
        var deck = remainingDeck
        if (deck.isEmpty()) {
            // Safety: if deck exhausted (shouldn't happen), reshuffle a fresh deck
            deck = createDeck().shuffled()
        }
        val starterCard = deck.first()
        val updatedDeck = deck.drop(1)

        return CribSelectionResult.Success(
            updatedPlayerHand = updatedPlayerHand,
            updatedOpponentHand = updatedOpponentHand,
            cribHand = cribHand,
            starterCard = starterCard,
            remainingDeck = updatedDeck,
            statusMessage = "Cut card: ${starterCard.getSymbol()}"
        )
    }

    /**
     * Start the next round (toggle dealer).
     */
    fun startNextRound(currentIsPlayerDealer: Boolean): RoundTransitionResult {
        val newIsPlayerDealer = !currentIsPlayerDealer
        return RoundTransitionResult(
            isPlayerDealer = newIsPlayerDealer,
            statusMessage = if (newIsPlayerDealer) "You are now the dealer" else "Opponent is now the dealer"
        )
    }
}

/**
 * Results for game lifecycle operations.
 */
sealed class GameLifecycleResult {
    data class NewGameStarted(
        val isPlayerDealer: Boolean,
        val cutPlayerCard: Card?,
        val cutOpponentCard: Card?,
        val showCutForDealer: Boolean,
        val statusMessage: String
    ) : GameLifecycleResult()
}

/**
 * Result of ending a game.
 */
data class EndGameResult(
    val statusMessage: String
)

/**
 * Result of dealing cards.
 */
data class DealResult(
    val playerHand: List<Card>,
    val opponentHand: List<Card>,
    val remainingDeck: List<Card>,
    val statusMessage: String
)

/**
 * Result of selecting cards for the crib.
 */
sealed class CribSelectionResult {
    data class Success(
        val updatedPlayerHand: List<Card>,
        val updatedOpponentHand: List<Card>,
        val cribHand: List<Card>,
        val starterCard: Card,
        val remainingDeck: List<Card>,
        val statusMessage: String
    ) : CribSelectionResult()

    data class Error(val message: String) : CribSelectionResult()
}

/**
 * Result of transitioning to the next round.
 */
data class RoundTransitionResult(
    val isPlayerDealer: Boolean,
    val statusMessage: String
)

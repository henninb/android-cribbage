package com.brianhenning.cribbage.game.state

import com.brianhenning.cribbage.game.logic.DealerManager
import com.brianhenning.cribbage.game.repository.PreferencesRepository
import com.brianhenning.cribbage.shared.domain.logic.OpponentAI
import com.brianhenning.cribbage.shared.domain.logic.dealSixToEach
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.createDeck

/**
 * State Manager for game lifecycle operations (start, end, deal, crib selection, rounds).
 * Follows MVVM best practices - operates on immutable state and returns new state.
 *
 * This is a pure business logic class with minimal Android dependencies.
 * All methods are testable without the Android framework.
 */
class GameLifecycleManager(
    private val preferencesRepository: PreferencesRepository,
    private val opponentAI: OpponentAI = OpponentAI
) {

    /**
     * Result of starting a new game
     */
    data class NewGameResult(
        val isPlayerDealer: Boolean,
        val cutPlayerCard: Card?,
        val cutOpponentCard: Card?,
        val showCutForDealer: Boolean,
        val statusMessage: String
    )

    /**
     * Result of dealing cards
     */
    data class DealResult(
        val playerHand: List<Card>,
        val opponentHand: List<Card>,
        val remainingDeck: List<Card>,
        val statusMessage: String
    )

    /**
     * Result of crib selection
     */
    data class CribSelectionResult(
        val updatedPlayerHand: List<Card>,
        val updatedOpponentHand: List<Card>,
        val cribHand: List<Card>,
        val starterCard: Card,
        val remainingDeck: List<Card>,
        val statusMessage: String
    )

    /**
     * Result of round transition
     */
    data class RoundTransitionResult(
        val newIsPlayerDealer: Boolean,
        val statusMessage: String
    )

    /**
     * Starts a new game with dealer determination.
     * Returns immutable result with dealer info and cut cards if applicable.
     *
     * @return NewGameResult with dealer determination
     */
    fun startNewGame(): NewGameResult {
        // Check if there's a preference from a previous game (loser deals)
        return if (preferencesRepository.hasNextDealerPreference()) {
            val isPlayerDealer = preferencesRepository.loadNextDealerIsPlayer()
            NewGameResult(
                isPlayerDealer = isPlayerDealer,
                cutPlayerCard = null,
                cutOpponentCard = null,
                showCutForDealer = false,
                statusMessage = "Dealer set by previous game: ${if (isPlayerDealer) "You are dealer" else "Opponent is dealer"}"
            )
        } else {
            // Cut for dealer per rules: lower card deals first
            val cutResult = DealerManager.determineDealer()

            // Save cut cards for UI header
            preferencesRepository.saveCutCards(
                PreferencesRepository.CutCards(
                    playerCard = cutResult.playerCutCard,
                    opponentCard = cutResult.opponentCutCard
                )
            )

            NewGameResult(
                isPlayerDealer = cutResult.isPlayerDealer,
                cutPlayerCard = cutResult.playerCutCard,
                cutOpponentCard = cutResult.opponentCutCard,
                showCutForDealer = true,
                statusMessage = "Cut for deal: You ${cutResult.playerCutCard.getSymbol()} vs Opponent ${cutResult.opponentCutCard.getSymbol()}\n" +
                        if (cutResult.isPlayerDealer) "You are dealer" else "Opponent is dealer"
            )
        }
    }

    /**
     * Ends the current game and clears next dealer preference.
     */
    fun endGame() {
        // Clear the next dealer preference when manually ending a game
        // (If game ends naturally due to score, the preference is already set)
        if (preferencesRepository.hasNextDealerPreference()) {
            // Don't clear - preference might have been set by game over
            // Only clear if user explicitly ends mid-game
        }
    }

    /**
     * Deals six cards to each player.
     * Returns immutable result with hands and remaining deck.
     *
     * @param isPlayerDealer Whether player is the dealer
     * @return DealResult with hands and deck
     */
    fun dealCards(isPlayerDealer: Boolean): DealResult {
        val deck = createDeck().shuffled().toMutableList()
        val result = dealSixToEach(deck, playerIsDealer = isPlayerDealer)

        // Sort player's hand for easier viewing
        val sortedPlayerHand = result.playerHand.sortedWith(
            compareBy({ it.rank.ordinal }, { it.suit.ordinal })
        )

        return DealResult(
            playerHand = sortedPlayerHand,
            opponentHand = result.opponentHand,
            remainingDeck = result.remainingDeck,
            statusMessage = if (isPlayerDealer) {
                "Select 2 cards for your crib"
            } else {
                "Select 2 cards for opponent's crib"
            }
        )
    }

    /**
     * Selects cards for the crib (player selection + opponent AI selection).
     * Draws starter card from remaining deck.
     * Returns immutable result with updated hands and crib.
     *
     * @param playerHand Current player hand
     * @param opponentHand Current opponent hand
     * @param selectedIndices Indices of player's selected cards (must be exactly 2)
     * @param isPlayerDealer Whether player is the dealer
     * @param remainingDeck Remaining deck after dealing
     * @return CribSelectionResult or null if validation fails
     */
    fun selectCardsForCrib(
        playerHand: List<Card>,
        opponentHand: List<Card>,
        selectedIndices: Set<Int>,
        isPlayerDealer: Boolean,
        remainingDeck: List<Card>
    ): CribSelectionResult? {
        // Validate selection
        if (selectedIndices.size != 2) {
            return null // Invalid selection - caller should handle error
        }

        // Get player's crib cards
        val selectedPlayerCards = selectedIndices.toList()
            .sortedDescending()
            .map { playerHand[it] }

        // Use smart AI to choose opponent's crib cards
        val opponentCribCards = opponentAI.chooseCribCards(opponentHand, !isPlayerDealer)

        // Build crib hand
        val cribHand = selectedPlayerCards + opponentCribCards

        // Remove crib cards from hands
        val updatedPlayerHand = playerHand.filterIndexed { index, _ ->
            !selectedIndices.contains(index)
        }.sortedWith(compareBy({ it.rank.ordinal }, { it.suit.ordinal }))

        val updatedOpponentHand = opponentHand.filter {
            !opponentCribCards.contains(it)
        }.sortedWith(compareBy({ it.rank.ordinal }, { it.suit.ordinal }))

        // Draw starter card from remaining deck
        var deck = remainingDeck
        if (deck.isEmpty()) {
            // Safety: if deck exhausted (shouldn't happen), reshuffle a fresh deck
            deck = createDeck().shuffled()
        }
        val starterCard = deck.first()
        val newRemainingDeck = deck.drop(1)

        return CribSelectionResult(
            updatedPlayerHand = updatedPlayerHand,
            updatedOpponentHand = updatedOpponentHand,
            cribHand = cribHand,
            starterCard = starterCard,
            remainingDeck = newRemainingDeck,
            statusMessage = "Cut card: ${starterCard.getSymbol()}"
        )
    }

    /**
     * Transitions to the next round by toggling the dealer.
     * Returns immutable result with new dealer state.
     *
     * @param currentIsPlayerDealer Current dealer state
     * @return RoundTransitionResult with toggled dealer
     */
    fun startNextRound(currentIsPlayerDealer: Boolean): RoundTransitionResult {
        val newIsPlayerDealer = !currentIsPlayerDealer
        return RoundTransitionResult(
            newIsPlayerDealer = newIsPlayerDealer,
            statusMessage = "New round: ${if (newIsPlayerDealer) "You are now the dealer." else "Opponent is now the dealer."}"
        )
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

    /**
     * Loads cut cards from persistent storage if they exist.
     *
     * @return Pair of (playerCard, opponentCard) or (null, null) if not saved
     */
    fun loadCutCards(): Pair<Card?, Card?> {
        val cutCards = preferencesRepository.loadCutCards()
        return if (cutCards != null) {
            Pair(cutCards.playerCard, cutCards.opponentCard)
        } else {
            Pair(null, null)
        }
    }
}

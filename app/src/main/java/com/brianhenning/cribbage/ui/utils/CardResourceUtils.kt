package com.brianhenning.cribbage.ui.utils

import com.brianhenning.cribbage.R
import com.brianhenning.cribbage.shared.domain.logic.OpponentAI
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit

/**
 * Utility functions for card resource management.
 */

/**
 * Get the drawable resource ID for a card.
 */
fun getCardResourceId(card: Card): Int {
    val suitName = when (card.suit) {
        Suit.HEARTS -> "hearts"
        Suit.DIAMONDS -> "diamonds"
        Suit.CLUBS -> "clubs"
        Suit.SPADES -> "spades"
    }
    val rankName = when (card.rank) {
        Rank.ACE -> "a"
        Rank.TWO -> "2"
        Rank.THREE -> "3"
        Rank.FOUR -> "4"
        Rank.FIVE -> "5"
        Rank.SIX -> "6"
        Rank.SEVEN -> "7"
        Rank.EIGHT -> "8"
        Rank.NINE -> "9"
        Rank.TEN -> "10"
        Rank.JACK -> "j"
        Rank.QUEEN -> "q"
        Rank.KING -> "k"
    }
    val resourceName = "${suitName}_${rankName}"
    val resourceField = R.drawable::class.java.getField(resourceName)
    return resourceField.getInt(null)
}

/**
 * Helper function for choosing a smart card for the opponent during pegging.
 * Uses the enhanced OpponentAI for strategic decision-making.
 */
fun chooseSmartOpponentCard(
    hand: List<Card>,
    playedIndices: Set<Int>,
    currentCount: Int,
    peggingPile: List<Card>
): Pair<Int, Card>? {
    // Calculate how many cards the player likely has remaining
    val opponentCardsPlayed = playedIndices.size
    val opponentCardsRemaining = 4 - opponentCardsPlayed

    return OpponentAI.choosePeggingCard(
        hand = hand,
        playedIndices = playedIndices,
        currentCount = currentCount,
        peggingPile = peggingPile,
        opponentCardsRemaining = opponentCardsRemaining
    )
}

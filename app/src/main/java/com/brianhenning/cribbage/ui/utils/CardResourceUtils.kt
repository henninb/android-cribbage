package com.brianhenning.cribbage.ui.utils

import com.brianhenning.cribbage.R
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit

/**
 * Utility functions for mapping Card objects to Android drawable resources.
 */
object CardResourceUtils {

    /**
     * Gets the drawable resource ID for a given card.
     * Maps card suit and rank to the corresponding drawable resource.
     *
     * @param card The card to get the resource for
     * @return The drawable resource ID
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
}

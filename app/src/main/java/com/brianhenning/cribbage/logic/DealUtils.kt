package com.brianhenning.cribbage.logic

import com.brianhenning.cribbage.ui.screens.Card

data class DealResult(
    val playerHand: List<Card>,
    val opponentHand: List<Card>,
    val remainingDeck: List<Card>
)

/**
 * Deals six cards to each player, alternating by rule:
 * - If player is dealer, opponent receives first card; otherwise player receives first.
 * Consumes 12 cards from the provided deck.
 */
fun dealSixToEach(deck: MutableList<Card>, playerIsDealer: Boolean): DealResult {
    val player = mutableListOf<Card>()
    val opponent = mutableListOf<Card>()
    repeat(6) {
        if (playerIsDealer) {
            // Dealer deals to opponent first
            opponent.add(deck.removeAt(0))
            player.add(deck.removeAt(0))
        } else {
            player.add(deck.removeAt(0))
            opponent.add(deck.removeAt(0))
        }
    }
    return DealResult(player, opponent, deck.toList())
}

/**
 * Determines dealer from two cut cards: lower rank deals (Ace low).
 * Returns Player.PLAYER if player should deal, Player.OPPONENT if opponent should deal,
 * or null if ranks are equal (caller should redraw).
 */
fun dealerFromCut(playerCut: Card, opponentCut: Card): Player? {
    val p = playerCut.rank.ordinal
    val o = opponentCut.rank.ordinal
    return when {
        p == o -> null
        p < o -> Player.PLAYER
        else -> Player.OPPONENT
    }
}


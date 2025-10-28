package com.brianhenning.cribbage

import com.brianhenning.cribbage.shared.domain.logic.dealSixToEach
import com.brianhenning.cribbage.shared.domain.logic.dealerFromCut
import com.brianhenning.cribbage.shared.domain.logic.Player
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Test

class DealUtilsTest {

    private fun testDeck12(): MutableList<Card> = mutableListOf(
        // A♥, 2♥, 3♥, 4♥, 5♥, 6♥, then A♦,2♦,3♦,4♦,5♦,6♦
        Card(Rank.ACE, Suit.HEARTS),
        Card(Rank.TWO, Suit.HEARTS),
        Card(Rank.THREE, Suit.HEARTS),
        Card(Rank.FOUR, Suit.HEARTS),
        Card(Rank.FIVE, Suit.HEARTS),
        Card(Rank.SIX, Suit.HEARTS),
        Card(Rank.ACE, Suit.DIAMONDS),
        Card(Rank.TWO, Suit.DIAMONDS),
        Card(Rank.THREE, Suit.DIAMONDS),
        Card(Rank.FOUR, Suit.DIAMONDS),
        Card(Rank.FIVE, Suit.DIAMONDS),
        Card(Rank.SIX, Suit.DIAMONDS),
    )

    @Test
    fun dealerPlayer_dealsToOpponentFirst() {
        val deck = testDeck12()
        val res = dealSixToEach(deck, playerIsDealer = true)
        assertEquals(6, res.playerHand.size)
        assertEquals(6, res.opponentHand.size)
        // Opponent gets first card (A♥), player second (2♥)
        assertEquals(Card(Rank.ACE, Suit.HEARTS), res.opponentHand[0])
        assertEquals(Card(Rank.TWO, Suit.HEARTS), res.playerHand[0])
    }

    @Test
    fun dealerOpponent_dealsToPlayerFirst() {
        val deck = testDeck12()
        val res = dealSixToEach(deck, playerIsDealer = false)
        assertEquals(6, res.playerHand.size)
        assertEquals(6, res.opponentHand.size)
        // Player gets first card (A♥), opponent second (2♥)
        assertEquals(Card(Rank.ACE, Suit.HEARTS), res.playerHand[0])
        assertEquals(Card(Rank.TWO, Suit.HEARTS), res.opponentHand[0])
    }

    @Test
    fun dealerFromCut_playerLowerRank_deals() {
        val playerCut = Card(Rank.ACE, Suit.CLUBS)   // low
        val oppCut = Card(Rank.KING, Suit.CLUBS)     // high
        val who = dealerFromCut(playerCut, oppCut)
        assertEquals("Player should be dealer when holding lower cut", Player.PLAYER, who)
    }

    @Test
    fun dealerFromCut_opponentLowerRank_deals() {
        val playerCut = Card(Rank.QUEEN, Suit.HEARTS)
        val oppCut = Card(Rank.FIVE, Suit.SPADES)
        val who = dealerFromCut(playerCut, oppCut)
        assertEquals(Player.OPPONENT, who)
    }

    @Test
    fun dealerFromCut_equalRanks_returnsNull() {
        val playerCut = Card(Rank.SEVEN, Suit.CLUBS)
        val oppCut = Card(Rank.SEVEN, Suit.HEARTS)
        val who = dealerFromCut(playerCut, oppCut)
        assertEquals(null, who)
    }
}

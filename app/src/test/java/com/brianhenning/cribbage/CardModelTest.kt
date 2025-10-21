package com.brianhenning.cribbage

import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for Card model getValue() and getSymbol() methods.
 * Tests all ranks and suits to ensure proper value assignment and symbol formatting.
 */
class CardModelTest {

    // ========== getValue() Tests ==========

    @Test
    fun getValue_ace_returns1() {
        val card = Card(Rank.ACE, Suit.HEARTS)
        assertEquals(1, card.getValue())
    }

    @Test
    fun getValue_two_returns2() {
        val card = Card(Rank.TWO, Suit.DIAMONDS)
        assertEquals(2, card.getValue())
    }

    @Test
    fun getValue_three_returns3() {
        val card = Card(Rank.THREE, Suit.CLUBS)
        assertEquals(3, card.getValue())
    }

    @Test
    fun getValue_four_returns4() {
        val card = Card(Rank.FOUR, Suit.SPADES)
        assertEquals(4, card.getValue())
    }

    @Test
    fun getValue_five_returns5() {
        val card = Card(Rank.FIVE, Suit.HEARTS)
        assertEquals(5, card.getValue())
    }

    @Test
    fun getValue_six_returns6() {
        val card = Card(Rank.SIX, Suit.DIAMONDS)
        assertEquals(6, card.getValue())
    }

    @Test
    fun getValue_seven_returns7() {
        val card = Card(Rank.SEVEN, Suit.CLUBS)
        assertEquals(7, card.getValue())
    }

    @Test
    fun getValue_eight_returns8() {
        val card = Card(Rank.EIGHT, Suit.SPADES)
        assertEquals(8, card.getValue())
    }

    @Test
    fun getValue_nine_returns9() {
        val card = Card(Rank.NINE, Suit.HEARTS)
        assertEquals(9, card.getValue())
    }

    @Test
    fun getValue_ten_returns10() {
        val card = Card(Rank.TEN, Suit.DIAMONDS)
        assertEquals(10, card.getValue())
    }

    @Test
    fun getValue_jack_returns10() {
        val card = Card(Rank.JACK, Suit.CLUBS)
        assertEquals(10, card.getValue())
    }

    @Test
    fun getValue_queen_returns10() {
        val card = Card(Rank.QUEEN, Suit.SPADES)
        assertEquals(10, card.getValue())
    }

    @Test
    fun getValue_king_returns10() {
        val card = Card(Rank.KING, Suit.HEARTS)
        assertEquals(10, card.getValue())
    }

    @Test
    fun getValue_allFaceCards_return10() {
        val faceCards = listOf(
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.HEARTS),
            Card(Rank.KING, Suit.HEARTS)
        )
        faceCards.forEach { card ->
            assertEquals("${card.rank} should return 10", 10, card.getValue())
        }
    }

    // ========== getSymbol() Tests ==========

    @Test
    fun getSymbol_aceOfHearts_returnsCorrectSymbol() {
        val card = Card(Rank.ACE, Suit.HEARTS)
        assertEquals("A♥", card.getSymbol())
    }

    @Test
    fun getSymbol_twoOfDiamonds_returnsCorrectSymbol() {
        val card = Card(Rank.TWO, Suit.DIAMONDS)
        assertEquals("2♦", card.getSymbol())
    }

    @Test
    fun getSymbol_threeOfClubs_returnsCorrectSymbol() {
        val card = Card(Rank.THREE, Suit.CLUBS)
        assertEquals("3♣", card.getSymbol())
    }

    @Test
    fun getSymbol_fourOfSpades_returnsCorrectSymbol() {
        val card = Card(Rank.FOUR, Suit.SPADES)
        assertEquals("4♠", card.getSymbol())
    }

    @Test
    fun getSymbol_fiveOfHearts_returnsCorrectSymbol() {
        val card = Card(Rank.FIVE, Suit.HEARTS)
        assertEquals("5♥", card.getSymbol())
    }

    @Test
    fun getSymbol_sixOfDiamonds_returnsCorrectSymbol() {
        val card = Card(Rank.SIX, Suit.DIAMONDS)
        assertEquals("6♦", card.getSymbol())
    }

    @Test
    fun getSymbol_sevenOfClubs_returnsCorrectSymbol() {
        val card = Card(Rank.SEVEN, Suit.CLUBS)
        assertEquals("7♣", card.getSymbol())
    }

    @Test
    fun getSymbol_eightOfSpades_returnsCorrectSymbol() {
        val card = Card(Rank.EIGHT, Suit.SPADES)
        assertEquals("8♠", card.getSymbol())
    }

    @Test
    fun getSymbol_nineOfHearts_returnsCorrectSymbol() {
        val card = Card(Rank.NINE, Suit.HEARTS)
        assertEquals("9♥", card.getSymbol())
    }

    @Test
    fun getSymbol_tenOfDiamonds_returnsCorrectSymbol() {
        val card = Card(Rank.TEN, Suit.DIAMONDS)
        assertEquals("10♦", card.getSymbol())
    }

    @Test
    fun getSymbol_jackOfClubs_returnsCorrectSymbol() {
        val card = Card(Rank.JACK, Suit.CLUBS)
        assertEquals("J♣", card.getSymbol())
    }

    @Test
    fun getSymbol_queenOfSpades_returnsCorrectSymbol() {
        val card = Card(Rank.QUEEN, Suit.SPADES)
        assertEquals("Q♠", card.getSymbol())
    }

    @Test
    fun getSymbol_kingOfHearts_returnsCorrectSymbol() {
        val card = Card(Rank.KING, Suit.HEARTS)
        assertEquals("K♥", card.getSymbol())
    }

    @Test
    fun getSymbol_allSuits_useCorrectSymbols() {
        val aceOfHearts = Card(Rank.ACE, Suit.HEARTS)
        val aceOfDiamonds = Card(Rank.ACE, Suit.DIAMONDS)
        val aceOfClubs = Card(Rank.ACE, Suit.CLUBS)
        val aceOfSpades = Card(Rank.ACE, Suit.SPADES)

        assertEquals("A♥", aceOfHearts.getSymbol())
        assertEquals("A♦", aceOfDiamonds.getSymbol())
        assertEquals("A♣", aceOfClubs.getSymbol())
        assertEquals("A♠", aceOfSpades.getSymbol())
    }

    @Test
    fun getSymbol_allRanks_useCorrectSymbols() {
        val suit = Suit.HEARTS
        val expectedSymbols = mapOf(
            Rank.ACE to "A♥",
            Rank.TWO to "2♥",
            Rank.THREE to "3♥",
            Rank.FOUR to "4♥",
            Rank.FIVE to "5♥",
            Rank.SIX to "6♥",
            Rank.SEVEN to "7♥",
            Rank.EIGHT to "8♥",
            Rank.NINE to "9♥",
            Rank.TEN to "10♥",
            Rank.JACK to "J♥",
            Rank.QUEEN to "Q♥",
            Rank.KING to "K♥"
        )

        for ((rank, expectedSymbol) in expectedSymbols) {
            val card = Card(rank, suit)
            assertEquals(expectedSymbol, card.getSymbol())
        }
    }

    // ========== Combined Tests ==========

    @Test
    fun cardEquality_sameRankAndSuit_areEqual() {
        val card1 = Card(Rank.ACE, Suit.HEARTS)
        val card2 = Card(Rank.ACE, Suit.HEARTS)
        assertEquals(card1, card2)
    }

    @Test
    fun cardEquality_differentRank_areNotEqual() {
        val card1 = Card(Rank.ACE, Suit.HEARTS)
        val card2 = Card(Rank.TWO, Suit.HEARTS)
        assert(card1 != card2)
    }

    @Test
    fun cardEquality_differentSuit_areNotEqual() {
        val card1 = Card(Rank.ACE, Suit.HEARTS)
        val card2 = Card(Rank.ACE, Suit.SPADES)
        assert(card1 != card2)
    }

    @Test
    fun cardHashCode_sameCards_haveSameHashCode() {
        val card1 = Card(Rank.KING, Suit.DIAMONDS)
        val card2 = Card(Rank.KING, Suit.DIAMONDS)
        assertEquals(card1.hashCode(), card2.hashCode())
    }
}

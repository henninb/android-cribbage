package com.brianhenning.cribbage

import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
import com.brianhenning.cribbage.ui.screens.getCardResourceId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive tests for card utility functions including
 * getCardResourceId and related helper methods.
 */
class CardUtilityFunctionsTest {

    // ========== getCardResourceId Tests ==========

    @Test
    fun getCardResourceId_returnsValidResourceId_forAllCards() {
        // Test all possible card combinations
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                val card = Card(rank, suit)
                val resourceId = getCardResourceId(card)

                // Resource IDs should be positive integers
                assertTrue(
                    "Resource ID should be positive for $card",
                    resourceId > 0
                )
            }
        }
    }

    @Test
    fun getCardResourceId_returnsUniqueIds_forDifferentCards() {
        // Given
        val aceOfSpades = Card(Rank.ACE, Suit.SPADES)
        val aceOfHearts = Card(Rank.ACE, Suit.HEARTS)
        val kingOfSpades = Card(Rank.KING, Suit.SPADES)

        // When
        val id1 = getCardResourceId(aceOfSpades)
        val id2 = getCardResourceId(aceOfHearts)
        val id3 = getCardResourceId(kingOfSpades)

        // Then - all should be different
        assertNotEquals(id1, id2)
        assertNotEquals(id1, id3)
        assertNotEquals(id2, id3)
    }

    @Test
    fun getCardResourceId_returnsSameId_forSameCard() {
        // Given
        val card1 = Card(Rank.FIVE, Suit.DIAMONDS)
        val card2 = Card(Rank.FIVE, Suit.DIAMONDS)

        // When
        val id1 = getCardResourceId(card1)
        val id2 = getCardResourceId(card2)

        // Then
        assertEquals(id1, id2)
    }

    @Test
    fun getCardResourceId_handlesAllSuits_correctly() {
        val rank = Rank.QUEEN

        val hearts = getCardResourceId(Card(rank, Suit.HEARTS))
        val diamonds = getCardResourceId(Card(rank, Suit.DIAMONDS))
        val clubs = getCardResourceId(Card(rank, Suit.CLUBS))
        val spades = getCardResourceId(Card(rank, Suit.SPADES))

        // All should be different
        val ids = setOf(hearts, diamonds, clubs, spades)
        assertEquals(4, ids.size)
    }

    @Test
    fun getCardResourceId_handlesAllRanks_correctly() {
        val suit = Suit.CLUBS

        val resourceIds = Rank.entries.map { rank ->
            getCardResourceId(Card(rank, suit))
        }

        // All should be unique
        assertEquals(Rank.entries.size, resourceIds.toSet().size)
    }

    @Test
    fun getCardResourceId_handlesFaceCards_correctly() {
        val jack = getCardResourceId(Card(Rank.JACK, Suit.HEARTS))
        val queen = getCardResourceId(Card(Rank.QUEEN, Suit.HEARTS))
        val king = getCardResourceId(Card(Rank.KING, Suit.HEARTS))

        assertTrue(jack > 0)
        assertTrue(queen > 0)
        assertTrue(king > 0)

        // All should be different
        assertNotEquals(jack, queen)
        assertNotEquals(queen, king)
        assertNotEquals(jack, king)
    }

    @Test
    fun getCardResourceId_handlesAce_correctly() {
        val aceOfHearts = getCardResourceId(Card(Rank.ACE, Suit.HEARTS))
        val aceOfSpades = getCardResourceId(Card(Rank.ACE, Suit.SPADES))

        assertTrue(aceOfHearts > 0)
        assertTrue(aceOfSpades > 0)
        assertNotEquals(aceOfHearts, aceOfSpades)
    }

    @Test
    fun getCardResourceId_handlesNumberCards_correctly() {
        val two = getCardResourceId(Card(Rank.TWO, Suit.DIAMONDS))
        val five = getCardResourceId(Card(Rank.FIVE, Suit.DIAMONDS))
        val ten = getCardResourceId(Card(Rank.TEN, Suit.DIAMONDS))

        assertTrue(two > 0)
        assertTrue(five > 0)
        assertTrue(ten > 0)

        // All should be different
        assertNotEquals(two, five)
        assertNotEquals(five, ten)
        assertNotEquals(two, ten)
    }

    // ========== Card.getValue() Tests ==========

    @Test
    fun cardGetValue_returnsCorrectValues_forNumberCards() {
        assertEquals(1, Card(Rank.ACE, Suit.HEARTS).getValue())
        assertEquals(2, Card(Rank.TWO, Suit.HEARTS).getValue())
        assertEquals(3, Card(Rank.THREE, Suit.HEARTS).getValue())
        assertEquals(4, Card(Rank.FOUR, Suit.HEARTS).getValue())
        assertEquals(5, Card(Rank.FIVE, Suit.HEARTS).getValue())
        assertEquals(6, Card(Rank.SIX, Suit.HEARTS).getValue())
        assertEquals(7, Card(Rank.SEVEN, Suit.HEARTS).getValue())
        assertEquals(8, Card(Rank.EIGHT, Suit.HEARTS).getValue())
        assertEquals(9, Card(Rank.NINE, Suit.HEARTS).getValue())
    }

    @Test
    fun cardGetValue_returnsTen_forAllFaceCards() {
        assertEquals(10, Card(Rank.TEN, Suit.HEARTS).getValue())
        assertEquals(10, Card(Rank.JACK, Suit.HEARTS).getValue())
        assertEquals(10, Card(Rank.QUEEN, Suit.HEARTS).getValue())
        assertEquals(10, Card(Rank.KING, Suit.HEARTS).getValue())
    }

    @Test
    fun cardGetValue_isConsistent_acrossDifferentSuits() {
        // Five should always be 5 regardless of suit
        assertEquals(5, Card(Rank.FIVE, Suit.HEARTS).getValue())
        assertEquals(5, Card(Rank.FIVE, Suit.DIAMONDS).getValue())
        assertEquals(5, Card(Rank.FIVE, Suit.CLUBS).getValue())
        assertEquals(5, Card(Rank.FIVE, Suit.SPADES).getValue())

        // King should always be 10 regardless of suit
        assertEquals(10, Card(Rank.KING, Suit.HEARTS).getValue())
        assertEquals(10, Card(Rank.KING, Suit.DIAMONDS).getValue())
        assertEquals(10, Card(Rank.KING, Suit.CLUBS).getValue())
        assertEquals(10, Card(Rank.KING, Suit.SPADES).getValue())
    }

    // ========== Card.getSymbol() Tests ==========

    @Test
    fun cardGetSymbol_returnsCorrectSymbols_forAllRanks() {
        assertEquals("A♥", Card(Rank.ACE, Suit.HEARTS).getSymbol())
        assertEquals("2♥", Card(Rank.TWO, Suit.HEARTS).getSymbol())
        assertEquals("3♥", Card(Rank.THREE, Suit.HEARTS).getSymbol())
        assertEquals("4♥", Card(Rank.FOUR, Suit.HEARTS).getSymbol())
        assertEquals("5♥", Card(Rank.FIVE, Suit.HEARTS).getSymbol())
        assertEquals("6♥", Card(Rank.SIX, Suit.HEARTS).getSymbol())
        assertEquals("7♥", Card(Rank.SEVEN, Suit.HEARTS).getSymbol())
        assertEquals("8♥", Card(Rank.EIGHT, Suit.HEARTS).getSymbol())
        assertEquals("9♥", Card(Rank.NINE, Suit.HEARTS).getSymbol())
        assertEquals("10♥", Card(Rank.TEN, Suit.HEARTS).getSymbol())
        assertEquals("J♥", Card(Rank.JACK, Suit.HEARTS).getSymbol())
        assertEquals("Q♥", Card(Rank.QUEEN, Suit.HEARTS).getSymbol())
        assertEquals("K♥", Card(Rank.KING, Suit.HEARTS).getSymbol())
    }

    @Test
    fun cardGetSymbol_returnsCorrectSymbols_forAllSuits() {
        val rank = Rank.ACE
        assertEquals("A♠", Card(rank, Suit.SPADES).getSymbol())
        assertEquals("A♥", Card(rank, Suit.HEARTS).getSymbol())
        assertEquals("A♦", Card(rank, Suit.DIAMONDS).getSymbol())
        assertEquals("A♣", Card(rank, Suit.CLUBS).getSymbol())
    }

    @Test
    fun cardGetSymbol_producesUniqueSymbols_forDifferentCards() {
        val symbols = mutableSetOf<String>()

        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                val symbol = Card(rank, suit).getSymbol()
                symbols.add(symbol)
            }
        }

        // Should have 52 unique symbols (4 suits × 13 ranks)
        assertEquals(52, symbols.size)
    }

    @Test
    fun cardGetSymbol_isConsistent_whenCalledMultipleTimes() {
        val card = Card(Rank.SEVEN, Suit.DIAMONDS)
        val symbol1 = card.getSymbol()
        val symbol2 = card.getSymbol()
        val symbol3 = card.getSymbol()

        assertEquals(symbol1, symbol2)
        assertEquals(symbol2, symbol3)
    }

    // ========== Card Data Class Tests ==========

    @Test
    fun card_equality_worksCorrectly() {
        val card1 = Card(Rank.FIVE, Suit.HEARTS)
        val card2 = Card(Rank.FIVE, Suit.HEARTS)
        val card3 = Card(Rank.SIX, Suit.HEARTS)

        assertEquals(card1, card2)
        assertNotEquals(card1, card3)
    }

    @Test
    fun card_hashCode_isConsistent_forEqualCards() {
        val card1 = Card(Rank.QUEEN, Suit.SPADES)
        val card2 = Card(Rank.QUEEN, Suit.SPADES)

        assertEquals(card1.hashCode(), card2.hashCode())
    }

    @Test
    fun card_copy_createsNewInstance() {
        val original = Card(Rank.JACK, Suit.CLUBS)
        val copy = original.copy()

        assertEquals(original, copy)
        assertEquals(original.rank, copy.rank)
        assertEquals(original.suit, copy.suit)
    }

    @Test
    fun card_canBeUsedInCollections() {
        val cards = listOf(
            Card(Rank.ACE, Suit.SPADES),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.KING, Suit.DIAMONDS)
        )

        assertTrue(cards.contains(Card(Rank.ACE, Suit.SPADES)))
        assertTrue(cards.contains(Card(Rank.FIVE, Suit.HEARTS)))
        assertEquals(3, cards.size)
    }

    @Test
    fun card_canBeUsedInSets() {
        val cardSet = mutableSetOf<Card>()
        val card = Card(Rank.TEN, Suit.CLUBS)

        cardSet.add(card)
        cardSet.add(card) // Adding same card again

        assertEquals(1, cardSet.size)
        assertTrue(cardSet.contains(card))
    }

    @Test
    fun card_canBeUsedAsMapKeys() {
        val cardMap = mutableMapOf<Card, Int>()
        val aceOfSpades = Card(Rank.ACE, Suit.SPADES)

        cardMap[aceOfSpades] = 100

        assertEquals(100, cardMap[aceOfSpades])
        assertEquals(100, cardMap[Card(Rank.ACE, Suit.SPADES)])
    }
}

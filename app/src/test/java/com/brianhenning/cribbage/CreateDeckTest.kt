package com.brianhenning.cribbage

import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import com.brianhenning.cribbage.ui.screens.createDeck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive unit tests for createDeck() function.
 * Validates that a standard 52-card deck is created with all ranks and suits.
 */
class CreateDeckTest {

    @Test
    fun createDeck_returns52Cards() {
        val deck = createDeck()
        assertEquals(52, deck.size)
    }

    @Test
    fun createDeck_containsAllSuits() {
        val deck = createDeck()
        val suits = deck.map { it.suit }.toSet()

        assertEquals(4, suits.size)
        assertTrue(suits.contains(Suit.HEARTS))
        assertTrue(suits.contains(Suit.DIAMONDS))
        assertTrue(suits.contains(Suit.CLUBS))
        assertTrue(suits.contains(Suit.SPADES))
    }

    @Test
    fun createDeck_containsAllRanks() {
        val deck = createDeck()
        val ranks = deck.map { it.rank }.toSet()

        assertEquals(13, ranks.size)
        assertTrue(ranks.contains(Rank.ACE))
        assertTrue(ranks.contains(Rank.TWO))
        assertTrue(ranks.contains(Rank.THREE))
        assertTrue(ranks.contains(Rank.FOUR))
        assertTrue(ranks.contains(Rank.FIVE))
        assertTrue(ranks.contains(Rank.SIX))
        assertTrue(ranks.contains(Rank.SEVEN))
        assertTrue(ranks.contains(Rank.EIGHT))
        assertTrue(ranks.contains(Rank.NINE))
        assertTrue(ranks.contains(Rank.TEN))
        assertTrue(ranks.contains(Rank.JACK))
        assertTrue(ranks.contains(Rank.QUEEN))
        assertTrue(ranks.contains(Rank.KING))
    }

    @Test
    fun createDeck_each13CardsPerSuit() {
        val deck = createDeck()

        val heartCards = deck.filter { it.suit == Suit.HEARTS }
        val diamondCards = deck.filter { it.suit == Suit.DIAMONDS }
        val clubCards = deck.filter { it.suit == Suit.CLUBS }
        val spadeCards = deck.filter { it.suit == Suit.SPADES }

        assertEquals(13, heartCards.size)
        assertEquals(13, diamondCards.size)
        assertEquals(13, clubCards.size)
        assertEquals(13, spadeCards.size)
    }

    @Test
    fun createDeck_each4CardsPerRank() {
        val deck = createDeck()

        for (rank in Rank.entries) {
            val cardsOfRank = deck.filter { it.rank == rank }
            assertEquals("Should have 4 cards of rank $rank", 4, cardsOfRank.size)
        }
    }

    @Test
    fun createDeck_allCardsAreUnique() {
        val deck = createDeck()
        val uniqueCards = deck.toSet()

        assertEquals("All cards should be unique", deck.size, uniqueCards.size)
    }

    @Test
    fun createDeck_containsSpecificCards() {
        val deck = createDeck()

        // Test for specific cards we know should exist
        assertTrue(deck.contains(Card(Rank.ACE, Suit.SPADES)))
        assertTrue(deck.contains(Card(Rank.KING, Suit.HEARTS)))
        assertTrue(deck.contains(Card(Rank.FIVE, Suit.DIAMONDS)))
        assertTrue(deck.contains(Card(Rank.TEN, Suit.CLUBS)))
    }

    @Test
    fun createDeck_allCombinationsPresent() {
        val deck = createDeck()

        // Verify every combination of rank and suit exists exactly once
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                val expectedCard = Card(rank, suit)
                val count = deck.count { it == expectedCard }
                assertEquals(
                    "Deck should contain exactly one ${rank} of ${suit}",
                    1,
                    count
                )
            }
        }
    }

    @Test
    fun createDeck_multipleCalls_returnEquivalentDecks() {
        val deck1 = createDeck()
        val deck2 = createDeck()

        // Decks should have same size
        assertEquals(deck1.size, deck2.size)

        // Decks should contain the same cards (order may differ)
        assertEquals(deck1.toSet(), deck2.toSet())
    }

    @Test
    fun createDeck_hasCorrectValueDistribution() {
        val deck = createDeck()

        // Count cards by value
        val valueCount = mutableMapOf<Int, Int>()
        for (card in deck) {
            val value = card.getValue()
            valueCount[value] = valueCount.getOrDefault(value, 0) + 1
        }

        // Verify value distribution
        assertEquals(4, valueCount[1])  // 4 Aces
        assertEquals(4, valueCount[2])  // 4 Twos
        assertEquals(4, valueCount[3])  // 4 Threes
        assertEquals(4, valueCount[4])  // 4 Fours
        assertEquals(4, valueCount[5])  // 4 Fives
        assertEquals(4, valueCount[6])  // 4 Sixes
        assertEquals(4, valueCount[7])  // 4 Sevens
        assertEquals(4, valueCount[8])  // 4 Eights
        assertEquals(4, valueCount[9])  // 4 Nines
        assertEquals(16, valueCount[10]) // 16 cards worth 10 (10, J, Q, K)
    }

    @Test
    fun createDeck_containsExpectedSymbols() {
        val deck = createDeck()
        val symbols = deck.map { it.getSymbol() }.toSet()

        // Should have 52 unique symbols
        assertEquals(52, symbols.size)

        // Verify some specific symbols exist
        assertTrue(symbols.contains("A♠"))
        assertTrue(symbols.contains("K♥"))
        assertTrue(symbols.contains("Q♦"))
        assertTrue(symbols.contains("J♣"))
        assertTrue(symbols.contains("10♠"))
        assertTrue(symbols.contains("5♥"))
    }

    @Test
    fun createDeck_canBeShuffledWithoutLoss() {
        val deck = createDeck()
        val shuffled = deck.shuffled()

        // Shuffled deck should still have 52 cards
        assertEquals(52, shuffled.size)

        // Shuffled deck should contain same cards
        assertEquals(deck.toSet(), shuffled.toSet())
    }

    @Test
    fun createDeck_canBeSplit() {
        val deck = createDeck()

        val firstHalf = deck.take(26)
        val secondHalf = deck.drop(26)

        assertEquals(26, firstHalf.size)
        assertEquals(26, secondHalf.size)

        // Combined should equal original deck
        val combined = firstHalf + secondHalf
        assertEquals(deck, combined)
    }
}

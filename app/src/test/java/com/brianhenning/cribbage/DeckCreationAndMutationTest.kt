package com.brianhenning.cribbage

import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
import com.brianhenning.cribbage.shared.domain.model.createDeck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for createDeck() function and deck mutation behavior.
 * Covers deck creation, size validation, uniqueness, suit/rank distribution,
 * and state mutation edge cases.
 */
class DeckCreationAndMutationTest {

    // ========== Basic Deck Creation Tests ==========

    @Test
    fun createDeck_returns52Cards() {
        val deck = createDeck()
        assertEquals(52, deck.size)
    }

    @Test
    fun createDeck_allCardsUnique() {
        val deck = createDeck()
        val uniqueCards = deck.toSet()
        assertEquals(52, uniqueCards.size)
    }

    @Test
    fun createDeck_has13Ranks() {
        val deck = createDeck()
        val ranks = deck.map { it.rank }.distinct()
        assertEquals(13, ranks.size)
        assertTrue(ranks.containsAll(Rank.entries))
    }

    @Test
    fun createDeck_has4Suits() {
        val deck = createDeck()
        val suits = deck.map { it.suit }.distinct()
        assertEquals(4, suits.size)
        assertTrue(suits.containsAll(Suit.entries))
    }

    // ========== Distribution Tests ==========

    @Test
    fun createDeck_eachRank_appears4Times() {
        val deck = createDeck()
        Rank.entries.forEach { rank ->
            val count = deck.count { it.rank == rank }
            assertEquals("Rank $rank should appear 4 times", 4, count)
        }
    }

    @Test
    fun createDeck_eachSuit_appears13Times() {
        val deck = createDeck()
        Suit.entries.forEach { suit ->
            val count = deck.count { it.suit == suit }
            assertEquals("Suit $suit should appear 13 times", 13, count)
        }
    }

    @Test
    fun createDeck_eachCombination_appearsExactlyOnce() {
        val deck = createDeck()

        for (rank in Rank.entries) {
            for (suit in Suit.entries) {
                val matchingCards = deck.filter { it.rank == rank && it.suit == suit }
                assertEquals(
                    "Each rank-suit combination should appear exactly once",
                    1,
                    matchingCards.size
                )
            }
        }
    }

    // ========== Card Content Verification ==========

    @Test
    fun createDeck_containsAllAces() {
        val deck = createDeck()
        val aces = deck.filter { it.rank == Rank.ACE }

        assertEquals(4, aces.size)
        assertEquals(4, aces.map { it.suit }.distinct().size)
    }

    @Test
    fun createDeck_containsAllFaceCards() {
        val deck = createDeck()
        val faceCards = deck.filter {
            it.rank == Rank.JACK || it.rank == Rank.QUEEN || it.rank == Rank.KING
        }

        assertEquals(12, faceCards.size)
        // 3 face ranks * 4 suits = 12
    }

    @Test
    fun createDeck_containsAllNumberCards() {
        val deck = createDeck()
        val numberCards = deck.filter {
            it.rank in listOf(
                Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE,
                Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE, Rank.TEN
            )
        }

        assertEquals(36, numberCards.size)
        // 9 number ranks * 4 suits = 36
    }

    // ========== Multiple Deck Creation Tests ==========

    @Test
    fun createDeck_multipleCalls_produceSameContent() {
        val deck1 = createDeck()
        val deck2 = createDeck()

        // Should have same cards (though potentially different order)
        assertEquals(deck1.toSet(), deck2.toSet())
    }

    @Test
    fun createDeck_multipleCalls_produceIndependentInstances() {
        val deck1 = createDeck().toMutableList()
        val deck2 = createDeck().toMutableList()

        // Mutating one should not affect the other
        val removedCard = deck1.removeAt(0)

        assertEquals(51, deck1.size)
        assertEquals(52, deck2.size)
        assertTrue(deck2.contains(removedCard))
    }

    // ========== Mutation Behavior Tests ==========

    @Test
    fun createDeck_mutableList_canRemoveCards() {
        val deck = createDeck().toMutableList()

        val initialSize = deck.size
        deck.removeAt(0)

        assertEquals(initialSize - 1, deck.size)
    }

    @Test
    fun createDeck_mutableList_canAddCards() {
        val deck = createDeck().toMutableList()
        val customCard = Card(Rank.ACE, Suit.HEARTS)

        deck.add(customCard)

        assertTrue(deck.size > 52)
    }

    @Test
    fun createDeck_multipleRemovals_maintainsValidity() {
        val deck = createDeck().toMutableList()

        repeat(10) {
            deck.removeAt(0)
        }

        assertEquals(42, deck.size)
        // Should still have unique cards
        assertEquals(42, deck.toSet().size)
    }

    @Test
    fun createDeck_removeHalfDeck_remainingCardsValid() {
        val deck = createDeck().toMutableList()

        repeat(26) {
            deck.removeAt(0)
        }

        assertEquals(26, deck.size)
        // All remaining should be unique
        assertEquals(26, deck.toSet().size)
        // All should be valid cards
        deck.forEach { card ->
            assertTrue(card.rank in Rank.entries)
            assertTrue(card.suit in Suit.entries)
        }
    }

    // ========== Edge Case Size Tests ==========

    @Test
    fun createDeck_removeAllCards_emptyDeck() {
        val deck = createDeck().toMutableList()

        repeat(52) {
            deck.removeAt(0)
        }

        assertEquals(0, deck.size)
        assertTrue(deck.isEmpty())
    }

    @Test
    fun createDeck_removeAlmostAll_oneCardLeft() {
        val deck = createDeck().toMutableList()

        repeat(51) {
            deck.removeAt(0)
        }

        assertEquals(1, deck.size)
        val lastCard = deck[0]
        assertTrue(lastCard.rank in Rank.entries)
        assertTrue(lastCard.suit in Suit.entries)
    }

    // ========== Card Value Tests ==========

    @Test
    fun createDeck_allCards_haveValidValues() {
        val deck = createDeck()

        deck.forEach { card ->
            val value = card.getValue()
            assertTrue("Card value should be 1-10", value in 1..10)
        }
    }

    @Test
    fun createDeck_faceCards_allHaveValue10() {
        val deck = createDeck()
        val faceCards = deck.filter {
            it.rank == Rank.TEN || it.rank == Rank.JACK ||
                    it.rank == Rank.QUEEN || it.rank == Rank.KING
        }

        faceCards.forEach { card ->
            assertEquals("Face cards should have value 10", 10, card.getValue())
        }
    }

    @Test
    fun createDeck_aces_allHaveValue1() {
        val deck = createDeck()
        val aces = deck.filter { it.rank == Rank.ACE }

        aces.forEach { card ->
            assertEquals("Aces should have value 1", 1, card.getValue())
        }
    }

    // ========== Card Symbol Tests ==========

    @Test
    fun createDeck_allCards_haveValidSymbols() {
        val deck = createDeck()

        deck.forEach { card ->
            val symbol = card.getSymbol()
            assertTrue("Symbol should not be empty", symbol.isNotEmpty())
            assertTrue("Symbol should have suit symbol", symbol.length >= 2)
        }
    }

    @Test
    fun createDeck_allSymbols_areUnique() {
        val deck = createDeck()
        val symbols = deck.map { it.getSymbol() }

        assertEquals(52, symbols.size)
        assertEquals(52, symbols.toSet().size)
    }

    // ========== Specific Card Existence Tests ==========

    @Test
    fun createDeck_containsAceOfSpades() {
        val deck = createDeck()
        val aceOfSpades = deck.find { it.rank == Rank.ACE && it.suit == Suit.SPADES }

        assertNotNull("Should contain Ace of Spades", aceOfSpades)
    }

    @Test
    fun createDeck_containsKingOfHearts() {
        val deck = createDeck()
        val kingOfHearts = deck.find { it.rank == Rank.KING && it.suit == Suit.HEARTS }

        assertNotNull("Should contain King of Hearts", kingOfHearts)
    }

    @Test
    fun createDeck_containsFiveOfDiamonds() {
        val deck = createDeck()
        val fiveOfDiamonds = deck.find { it.rank == Rank.FIVE && it.suit == Suit.DIAMONDS }

        assertNotNull("Should contain Five of Diamonds", fiveOfDiamonds)
    }

    // ========== Reference vs Value Tests ==========

    @Test
    fun createDeck_twoCalls_differentReferences() {
        val deck1 = createDeck()
        val deck2 = createDeck()

        // Should be different list instances
        assertFalse("Should be different list references", deck1 === deck2)
    }

    @Test
    fun createDeck_cards_dataClassEquality() {
        val deck = createDeck()
        val aceOfHearts1 = deck.find { it.rank == Rank.ACE && it.suit == Suit.HEARTS }

        val deck2 = createDeck()
        val aceOfHearts2 = deck2.find { it.rank == Rank.ACE && it.suit == Suit.HEARTS }

        // Should be equal by value (data class equality)
        assertEquals(aceOfHearts1, aceOfHearts2)
    }

    // ========== Order Independence Tests ==========

    @Test
    fun createDeck_order_canVary() {
        // While we don't test randomness, we verify deck has all cards regardless of order
        val deck = createDeck()
        val sorted = deck.sortedBy { it.rank.ordinal * 10 + it.suit.ordinal }

        // Both should have the same cards
        assertEquals(deck.toSet(), sorted.toSet())
    }

    // ========== Filtering and Searching Tests ==========

    @Test
    fun createDeck_filterByRank_returnsCorrectCount() {
        val deck = createDeck()
        val fives = deck.filter { it.rank == Rank.FIVE }

        assertEquals(4, fives.size)
    }

    @Test
    fun createDeck_filterBySuit_returnsCorrectCount() {
        val deck = createDeck()
        val hearts = deck.filter { it.suit == Suit.HEARTS }

        assertEquals(13, hearts.size)
    }

    @Test
    fun createDeck_findSpecificCard_returnsNonNull() {
        val deck = createDeck()
        val queenOfClubs = deck.find { it.rank == Rank.QUEEN && it.suit == Suit.CLUBS }

        assertNotNull(queenOfClubs)
        assertEquals(Rank.QUEEN, queenOfClubs!!.rank)
        assertEquals(Suit.CLUBS, queenOfClubs.suit)
    }

    // ========== Partition Tests ==========

    @Test
    fun createDeck_partitionByValue_correctDistribution() {
        val deck = createDeck()
        val (lowCards, highCards) = deck.partition { it.getValue() <= 5 }

        // Low cards (1-5): A,2,3,4,5 = 20 cards
        // High cards (6-10): 6,7,8,9,10,J,Q,K = 32 cards
        assertEquals(20, lowCards.size)
        assertEquals(32, highCards.size)
    }

    @Test
    fun createDeck_groupBySuit_fourGroups() {
        val deck = createDeck()
        val grouped = deck.groupBy { it.suit }

        assertEquals(4, grouped.size)
        grouped.values.forEach { group ->
            assertEquals(13, group.size)
        }
    }

    @Test
    fun createDeck_groupByValue_distributionCorrect() {
        val deck = createDeck()
        val grouped = deck.groupBy { it.getValue() }

        // Values 1-9 should have 4 cards each
        // Value 10 should have 16 cards (10,J,Q,K)
        for (value in 1..9) {
            assertEquals("Value $value should have 4 cards", 4, grouped[value]?.size ?: 0)
        }
        assertEquals("Value 10 should have 16 cards", 16, grouped[10]?.size)
    }
}

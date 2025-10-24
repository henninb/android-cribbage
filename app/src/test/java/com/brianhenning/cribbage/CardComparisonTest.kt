package com.brianhenning.cribbage

import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests for Card comparison, sorting, and collection operations.
 * Tests card equality, hashCode behavior, and list operations that are
 * commonly used in the game logic.
 */
class CardComparisonTest {

    // ========== Card Equality Tests ==========

    @Test
    fun equals_sameRankAndSuit_areEqual() {
        val card1 = Card(Rank.KING, Suit.HEARTS)
        val card2 = Card(Rank.KING, Suit.HEARTS)
        assertEquals(card1, card2)
    }

    @Test
    fun equals_differentRank_areNotEqual() {
        val card1 = Card(Rank.KING, Suit.HEARTS)
        val card2 = Card(Rank.QUEEN, Suit.HEARTS)
        assertNotEquals(card1, card2)
    }

    @Test
    fun equals_differentSuit_areNotEqual() {
        val card1 = Card(Rank.KING, Suit.HEARTS)
        val card2 = Card(Rank.KING, Suit.SPADES)
        assertNotEquals(card1, card2)
    }

    @Test
    fun equals_differentRankAndSuit_areNotEqual() {
        val card1 = Card(Rank.KING, Suit.HEARTS)
        val card2 = Card(Rank.ACE, Suit.DIAMONDS)
        assertNotEquals(card1, card2)
    }

    // ========== HashCode Tests ==========

    @Test
    fun hashCode_sameCards_haveSameHashCode() {
        val card1 = Card(Rank.JACK, Suit.CLUBS)
        val card2 = Card(Rank.JACK, Suit.CLUBS)
        assertEquals(card1.hashCode(), card2.hashCode())
    }

    @Test
    fun hashCode_differentCards_haveDifferentHashCodes() {
        val card1 = Card(Rank.JACK, Suit.CLUBS)
        val card2 = Card(Rank.JACK, Suit.SPADES)
        assertNotEquals(card1.hashCode(), card2.hashCode())
    }

    // ========== Set Operations Tests ==========

    @Test
    fun set_containsCard_findsEqualCard() {
        val set = setOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.KING, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.CLUBS)
        )
        val searchCard = Card(Rank.KING, Suit.DIAMONDS)
        assert(set.contains(searchCard))
    }

    @Test
    fun set_doesNotContainCard_returnsFalse() {
        val set = setOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.KING, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.CLUBS)
        )
        val searchCard = Card(Rank.KING, Suit.HEARTS)
        assert(!set.contains(searchCard))
    }

    @Test
    fun set_removeDuplicates_keepsOnlyUnique() {
        val cards = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.KING, Suit.DIAMONDS),
            Card(Rank.KING, Suit.DIAMONDS)
        )
        val uniqueCards = cards.toSet()
        assertEquals(2, uniqueCards.size)
    }

    // ========== List Operations Tests ==========

    @Test
    fun list_contains_findsEqualCard() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.KING, Suit.CLUBS)
        )
        val searchCard = Card(Rank.FIVE, Suit.DIAMONDS)
        assert(hand.contains(searchCard))
    }

    @Test
    fun list_indexOf_findsCorrectPosition() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.KING, Suit.CLUBS)
        )
        val searchCard = Card(Rank.FIVE, Suit.DIAMONDS)
        assertEquals(1, hand.indexOf(searchCard))
    }

    @Test
    fun list_filter_removesMatchingCards() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.KING, Suit.CLUBS),
            Card(Rank.ACE, Suit.SPADES)
        )
        val filtered = hand.filter { it.rank != Rank.ACE }
        assertEquals(2, filtered.size)
        assert(filtered.none { it.rank == Rank.ACE })
    }

    @Test
    fun list_groupBy_groupsBySuit() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.KING, Suit.CLUBS),
            Card(Rank.TEN, Suit.CLUBS),
            Card(Rank.THREE, Suit.DIAMONDS)
        )
        val grouped = hand.groupBy { it.suit }
        assertEquals(3, grouped.size)
        assertEquals(2, grouped[Suit.HEARTS]?.size)
        assertEquals(2, grouped[Suit.CLUBS]?.size)
        assertEquals(1, grouped[Suit.DIAMONDS]?.size)
    }

    @Test
    fun list_groupBy_groupsByRank() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.ACE, Suit.DIAMONDS),
            Card(Rank.KING, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val grouped = hand.groupBy { it.rank }
        assertEquals(3, grouped.size)
        assertEquals(2, grouped[Rank.ACE]?.size)
        assertEquals(1, grouped[Rank.KING]?.size)
    }

    // ========== Sorting Tests ==========

    @Test
    fun sort_byRankOrdinal_sortsCorrectly() {
        val hand = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.TEN, Suit.CLUBS)
        )
        val sorted = hand.sortedBy { it.rank.ordinal }
        assertEquals(Rank.ACE, sorted[0].rank)
        assertEquals(Rank.FIVE, sorted[1].rank)
        assertEquals(Rank.TEN, sorted[2].rank)
        assertEquals(Rank.KING, sorted[3].rank)
    }

    @Test
    fun sort_bySuitOrdinal_sortsCorrectly() {
        val hand = listOf(
            Card(Rank.ACE, Suit.SPADES),
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.ACE, Suit.DIAMONDS),
            Card(Rank.ACE, Suit.CLUBS)
        )
        val sorted = hand.sortedBy { it.suit.ordinal }
        assertEquals(Suit.HEARTS, sorted[0].suit)
        assertEquals(Suit.DIAMONDS, sorted[1].suit)
        assertEquals(Suit.CLUBS, sorted[2].suit)
        assertEquals(Suit.SPADES, sorted[3].suit)
    }

    @Test
    fun sort_byValue_sortsCorrectly() {
        val hand = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.TEN, Suit.CLUBS)
        )
        val sorted = hand.sortedBy { it.getValue() }
        assertEquals(1, sorted[0].getValue())
        assertEquals(5, sorted[1].getValue())
        assertEquals(10, sorted[2].getValue())
        assertEquals(10, sorted[3].getValue())
    }

    @Test
    fun sort_faceCardsHaveSameValue_maintainsRelativeOrder() {
        val hand = listOf(
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.DIAMONDS),
            Card(Rank.KING, Suit.CLUBS),
            Card(Rank.TEN, Suit.SPADES)
        )
        val sorted = hand.sortedBy { it.getValue() }
        // All should have value 10, so order should be stable
        assertEquals(4, sorted.size)
        sorted.forEach { card ->
            assertEquals(10, card.getValue())
        }
    }

    // ========== Map/Dictionary Tests ==========

    @Test
    fun map_useCardAsKey_findsCorrectValue() {
        val cardValues = mapOf(
            Card(Rank.ACE, Suit.HEARTS) to "high",
            Card(Rank.TWO, Suit.DIAMONDS) to "low",
            Card(Rank.KING, Suit.CLUBS) to "medium"
        )
        assertEquals("high", cardValues[Card(Rank.ACE, Suit.HEARTS)])
        assertEquals("low", cardValues[Card(Rank.TWO, Suit.DIAMONDS)])
    }

    @Test
    fun map_equalCards_mapToSameKey() {
        val cardCounts = mutableMapOf<Card, Int>()
        val card1 = Card(Rank.ACE, Suit.HEARTS)
        val card2 = Card(Rank.ACE, Suit.HEARTS)

        cardCounts[card1] = 1
        cardCounts[card2] = 2

        assertEquals(1, cardCounts.size)
        assertEquals(2, cardCounts[card1])
    }

    // ========== Collection Count Tests ==========

    @Test
    fun count_specificCard_countsOccurrences() {
        val cards = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.KING, Suit.CLUBS)
        )
        val count = cards.count { it == Card(Rank.ACE, Suit.HEARTS) }
        assertEquals(2, count)
    }

    @Test
    fun count_byRank_countsAllOfRank() {
        val cards = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.KING, Suit.SPADES)
        )
        val fiveCount = cards.count { it.rank == Rank.FIVE }
        assertEquals(3, fiveCount)
    }

    @Test
    fun count_bySuit_countsAllOfSuit() {
        val cards = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.DIAMONDS)
        )
        val heartCount = cards.count { it.suit == Suit.HEARTS }
        assertEquals(3, heartCount)
    }

    // ========== Data Class Copy Tests ==========

    @Test
    fun copy_createsNewInstanceWithSameValues() {
        val original = Card(Rank.KING, Suit.HEARTS)
        val copied = original.copy()
        assertEquals(original, copied)
        assertEquals(original.rank, copied.rank)
        assertEquals(original.suit, copied.suit)
    }

    @Test
    fun copy_withChangedRank_createsNewCard() {
        val original = Card(Rank.KING, Suit.HEARTS)
        val modified = original.copy(rank = Rank.QUEEN)
        assertEquals(Rank.QUEEN, modified.rank)
        assertEquals(Suit.HEARTS, modified.suit)
        assertNotEquals(original, modified)
    }

    @Test
    fun copy_withChangedSuit_createsNewCard() {
        val original = Card(Rank.KING, Suit.HEARTS)
        val modified = original.copy(suit = Suit.SPADES)
        assertEquals(Rank.KING, modified.rank)
        assertEquals(Suit.SPADES, modified.suit)
        assertNotEquals(original, modified)
    }
}

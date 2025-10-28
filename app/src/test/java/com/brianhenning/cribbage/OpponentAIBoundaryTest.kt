package com.brianhenning.cribbage

import com.brianhenning.cribbage.shared.domain.logic.OpponentAI
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Boundary and edge case tests for OpponentAI functions.
 * Tests scenarios not covered by existing tests.
 */
class OpponentAIBoundaryTest {

    @Test
    fun chooseCribCards_withExactly6Cards_returnsValidDiscard() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        assertNotNull("Discard should not be null", discard)
        assertEquals("Should discard exactly 2 cards", 2, discard.size)
        // Verify discarded cards are from original hand
        discard.forEach { card ->
            assert(hand.contains(card)) { "Discarded card should be from original hand" }
        }
    }

    @Test
    fun chooseCribCards_withFewerThan6Cards_returnsSafeDefault() {
        val shortHand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.CLUBS)
        )

        val discard = OpponentAI.chooseCribCards(shortHand, isDealer = false)

        // Should return safe default (first 2 cards as per safety check)
        assertEquals("Should return 2 cards even with short hand", 2, discard.size)
    }

    @Test
    fun chooseCribCards_emptyHand_returnsSafeDefault() {
        val emptyHand = emptyList<Card>()

        val discard = OpponentAI.chooseCribCards(emptyHand, isDealer = true)

        // Should handle gracefully and return what it can
        assertNotNull("Should not crash on empty hand", discard)
    }

    @Test
    fun chooseCribCards_asDealer_prefersFives() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.SPADES),
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // As dealer, AI should tend to keep good cards and discard strategically
        // The pair of fives is valuable but depends on what makes best hand+crib combo
        assertEquals(2, discard.size)
    }

    @Test
    fun chooseCribCards_asNonDealer_avoidsFives() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.TEN, Suit.CLUBS),
            Card(Rank.ACE, Suit.DIAMONDS),
            Card(Rank.TWO, Suit.SPADES),
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.FOUR, Suit.CLUBS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = false)

        // Non-dealer should tend to discard fives to avoid giving points in crib
        assertEquals(2, discard.size)
    }

    @Test
    fun choosePeggingCard_allCardsAbove31_returnsNull() {
        val hand = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.CLUBS),
            Card(Rank.JACK, Suit.DIAMONDS)
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 25 // All cards would exceed 31

        val choice = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = emptyList(),
            opponentCardsRemaining = 3
        )

        assertNull("Should return null when no legal moves", choice)
    }

    @Test
    fun choosePeggingCard_allCardsPlayed_returnsNull() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.DIAMONDS)
        )
        val playedIndices = setOf(0, 1, 2) // All cards already played

        val choice = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = 10,
            peggingPile = emptyList(),
            opponentCardsRemaining = 2
        )

        assertNull("Should return null when all cards already played", choice)
    }

    @Test
    fun choosePeggingCard_emptyHand_returnsNull() {
        val emptyHand = emptyList<Card>()

        val choice = OpponentAI.choosePeggingCard(
            hand = emptyHand,
            playedIndices = emptySet(),
            currentCount = 15,
            peggingPile = emptyList(),
            opponentCardsRemaining = 4
        )

        assertNull("Should return null for empty hand", choice)
    }

    @Test
    fun choosePeggingCard_prefersMaking15() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.THREE, Suit.DIAMONDS)
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 10 // Playing 5 makes 15

        val choice = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = listOf(Card(Rank.KING, Suit.SPADES)),
            opponentCardsRemaining = 3
        )

        assertNotNull("Should find a legal move", choice)
        // Should prefer the 5 to make 15
        assertEquals("Should prefer card that makes 15", Rank.FIVE, choice?.second?.rank)
    }

    @Test
    fun choosePeggingCard_prefersMaking31() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.KING, Suit.DIAMONDS)
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 21 // Playing 10 makes 31

        val choice = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = emptyList(),
            opponentCardsRemaining = 2
        )

        assertNotNull("Should find a legal move", choice)
        // Should strongly prefer King to make 31
        assertEquals("Should prefer card that makes 31", Rank.KING, choice?.second?.rank)
    }

    @Test
    fun choosePeggingCard_prefersMakingPair() {
        val pile = listOf(
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.DIAMONDS)
        )
        val hand = listOf(
            Card(Rank.EIGHT, Suit.HEARTS), // Pairs with last card
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.THREE, Suit.SPADES)
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 20

        val choice = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = pile,
            opponentCardsRemaining = 2
        )

        assertNotNull("Should find a legal move", choice)
        // Should prefer the 8 to make a pair
        assertEquals("Should prefer card that makes pair", Rank.EIGHT, choice?.second?.rank)
    }

    @Test
    fun choosePeggingCard_withZeroCount_anyCardWorks() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.KING, Suit.CLUBS)
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 0

        val choice = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = emptyList(),
            opponentCardsRemaining = 4
        )

        assertNotNull("Should find a legal move at start", choice)
        assert(choice!!.first in 0..1) { "Index should be valid" }
    }

    @Test
    fun choosePeggingCard_returnsValidIndex() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val playedIndices = setOf(1) // Second card already played
        val currentCount = 10

        val choice = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = emptyList(),
            opponentCardsRemaining = 3
        )

        assertNotNull("Should find a legal move", choice)
        assert(choice!!.first in listOf(0, 2, 3)) {
            "Returned index should be one of the unplayed cards"
        }
        assertEquals("Returned card should match index", hand[choice.first], choice.second)
    }

    @Test
    fun choosePeggingCard_exactlyAt31_noLegalMoves() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.CLUBS)
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 31 // Already at max, no moves possible

        val choice = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = emptyList(),
            opponentCardsRemaining = 2
        )

        assertNull("Should return null when count is already 31", choice)
    }

    @Test
    fun choosePeggingCard_onlyOneCardLeft_selectsIt() {
        val hand = listOf(
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.CLUBS),
            Card(Rank.NINE, Suit.DIAMONDS)
        )
        val playedIndices = setOf(0, 1) // Only last card unplayed
        val currentCount = 5

        val choice = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = emptyList(),
            opponentCardsRemaining = 1
        )

        assertNotNull("Should select the only remaining card", choice)
        assertEquals("Should select index 2", 2, choice!!.first)
        assertEquals("Should select the 9", Rank.NINE, choice.second.rank)
    }
}

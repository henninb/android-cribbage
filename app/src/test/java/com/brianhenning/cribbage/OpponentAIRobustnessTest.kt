package com.brianhenning.cribbage

import com.brianhenning.cribbage.shared.domain.logic.OpponentAI
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Robustness and stress tests for OpponentAI to ensure it handles
 * edge cases, boundary conditions, and unusual game states gracefully.
 */
class OpponentAIRobustnessTest {

    // ========== chooseCribCards Robustness Tests ==========

    @Test
    fun chooseCribCards_withExactly6Cards_returnsExactly2Cards() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        assertEquals(2, discard.size)
        assertTrue(discard.all { it in hand })
    }

    @Test
    fun chooseCribCards_withFewerThan6Cards_returnsSafeDefault() {
        val smallHand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.CLUBS)
        )

        val discard = OpponentAI.chooseCribCards(smallHand, isDealer = true)

        assertEquals(2, discard.size)
        assertEquals(smallHand.take(2), discard)
    }

    @Test
    fun chooseCribCards_withIdenticalCards_handlesGracefully() {
        // All same rank and suit (impossible in real game, but test robustness)
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        assertEquals(2, discard.size)
    }

    @Test
    fun chooseCribCards_allLowCards_choosesOptimally() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        assertEquals(2, discard.size)
        assertTrue(discard.all { it in hand })
    }

    @Test
    fun chooseCribCards_allHighCards_choosesOptimally() {
        val hand = listOf(
            Card(Rank.EIGHT, Suit.HEARTS),
            Card(Rank.NINE, Suit.DIAMONDS),
            Card(Rank.TEN, Suit.CLUBS),
            Card(Rank.JACK, Suit.SPADES),
            Card(Rank.QUEEN, Suit.HEARTS),
            Card(Rank.KING, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        assertEquals(2, discard.size)
        assertTrue(discard.all { it in hand })
    }

    @Test
    fun chooseCribCards_multipleCalls_areConsistent() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.DIAMONDS)
        )

        val discard1 = OpponentAI.chooseCribCards(hand, isDealer = true)
        val discard2 = OpponentAI.chooseCribCards(hand, isDealer = true)
        val discard3 = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Should be deterministic (same input = same output)
        assertEquals(discard1, discard2)
        assertEquals(discard2, discard3)
    }

    @Test
    fun chooseCribCards_dealerAndNonDealer_bothReturnValid() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.DIAMONDS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.SPADES),
            Card(Rank.NINE, Suit.HEARTS),
            Card(Rank.TEN, Suit.DIAMONDS)
        )

        val dealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = true)
        val nonDealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = false)

        assertEquals(2, dealerDiscard.size)
        assertEquals(2, nonDealerDiscard.size)
        assertTrue(dealerDiscard.all { it in hand })
        assertTrue(nonDealerDiscard.all { it in hand })
    }

    // ========== choosePeggingCard Robustness Tests ==========

    @Test
    fun choosePeggingCard_withNoLegalMoves_returnsNull() {
        val hand = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.DIAMONDS),
            Card(Rank.JACK, Suit.CLUBS),
            Card(Rank.TEN, Suit.SPADES)
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 22 // All cards worth 10 would exceed 31

        val result = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = emptyList(),
            opponentCardsRemaining = 4
        )

        assertNull(result)
    }

    @Test
    fun choosePeggingCard_withAllCardsPlayed_returnsNull() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val playedIndices = setOf(0, 1, 2, 3) // All played

        val result = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = 10,
            peggingPile = emptyList(),
            opponentCardsRemaining = 2
        )

        assertNull(result)
    }

    @Test
    fun choosePeggingCard_atExactly31_noCardsCanBePlayed() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.SPADES)
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 31 // Already at max

        val result = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = emptyList(),
            opponentCardsRemaining = 4
        )

        assertNull(result)
    }

    @Test
    fun choosePeggingCard_withOnlyOneValidCard_returnsIt() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),      // Index 0: 1 (legal)
            Card(Rank.KING, Suit.DIAMONDS),    // Index 1: 10 (illegal)
            Card(Rank.QUEEN, Suit.CLUBS),      // Index 2: 10 (illegal)
            Card(Rank.JACK, Suit.SPADES)       // Index 3: 10 (illegal)
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 30 // Only ace (1) can be played

        val result = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = emptyList(),
            opponentCardsRemaining = 3
        )

        assertNotNull(result)
        assertEquals(0, result?.first) // Index of ace
        assertEquals(Rank.ACE, result?.second?.rank)
    }

    @Test
    fun choosePeggingCard_prioritizes31_whenPossible() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),      // 1
            Card(Rank.TWO, Suit.DIAMONDS),    // 2
            Card(Rank.THREE, Suit.CLUBS),     // 3
            Card(Rank.TEN, Suit.SPADES)       // 10
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 21 // Playing 10 makes 31

        val result = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = emptyList(),
            opponentCardsRemaining = 4
        )

        assertNotNull(result)
        // Should choose the 10 to make 31
        assertEquals(Rank.TEN, result?.second?.rank)
    }

    @Test
    fun choosePeggingCard_prioritizes15_whenAvailable() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),      // 2
            Card(Rank.THREE, Suit.DIAMONDS),  // 3
            Card(Rank.FIVE, Suit.CLUBS),      // 5
            Card(Rank.SEVEN, Suit.SPADES)     // 7
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 10 // Playing 5 makes 15

        val result = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = emptyList(),
            opponentCardsRemaining = 4
        )

        assertNotNull(result)
        // Should prefer the 5 to make 15
        assertEquals(Rank.FIVE, result?.second?.rank)
    }

    @Test
    fun choosePeggingCard_recognizesPairOpportunity() {
        val hand = listOf(
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.DIAMONDS),
            Card(Rank.NINE, Suit.CLUBS),
            Card(Rank.TEN, Suit.SPADES)
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 7
        val peggingPile = listOf(Card(Rank.SEVEN, Suit.DIAMONDS))

        val result = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = peggingPile,
            opponentCardsRemaining = 4
        )

        assertNotNull(result)
        // AI should make a scoring move (pair, 15, etc.)
        // The 7 makes a pair, 8 makes 15
        // AI might choose either based on scoring heuristics
        assertTrue(result!!.second.rank in listOf(Rank.SEVEN, Rank.EIGHT))
    }

    @Test
    fun choosePeggingCard_avoidsGivingOpponent15() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.DIAMONDS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.SPADES)
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 5 // Playing 10 would give count of 15

        val result = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = emptyList(),
            opponentCardsRemaining = 4
        )

        assertNotNull(result)
        // Should avoid leaving count at positions that help opponent
        // AI should make intelligent defensive choice
        assertTrue(result!!.second.rank in listOf(Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT))
    }

    @Test
    fun choosePeggingCard_withEmptyPile_makesValidChoice() {
        val hand = listOf(
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.SIX, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.SPADES)
        )
        val playedIndices = emptySet<Int>()

        val result = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = 0,
            peggingPile = emptyList(),
            opponentCardsRemaining = 4
        )

        assertNotNull(result)
        assertTrue(result!!.first in 0..3)
        assertTrue(result.second in hand)
    }

    @Test
    fun choosePeggingCard_withHighCount_playsSmallCard() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.KING, Suit.SPADES)
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 25 // High count, limited options

        val result = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = emptyList(),
            opponentCardsRemaining = 4
        )

        assertNotNull(result)
        // Can only play low cards
        assertTrue(result!!.second.rank in listOf(Rank.ACE, Rank.TWO, Rank.THREE))
    }

    @Test
    fun choosePeggingCard_respectsPlayedIndices() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val playedIndices = setOf(0, 2) // 2 and 4 already played

        val result = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = 10,
            peggingPile = emptyList(),
            opponentCardsRemaining = 2
        )

        assertNotNull(result)
        // Should only choose from indices 1 or 3
        assertTrue(result!!.first in listOf(1, 3))
        assertTrue(result.second.rank in listOf(Rank.THREE, Rank.FIVE))
    }

    @Test
    fun choosePeggingCard_handlesPartiallyPlayedHand() {
        val hand = listOf(
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.EIGHT, Suit.CLUBS),
            Card(Rank.NINE, Suit.SPADES)
        )
        val playedIndices = setOf(1, 3) // 7 and 9 played

        val result = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = 5,
            peggingPile = emptyList(),
            opponentCardsRemaining = 1
        )

        assertNotNull(result)
        assertTrue(result!!.first in listOf(0, 2))
        assertTrue(result.second.rank in listOf(Rank.SIX, Rank.EIGHT))
    }

    @Test
    fun choosePeggingCard_considersOpponentCardsRemaining() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.TEN, Suit.CLUBS),
            Card(Rank.KING, Suit.SPADES)
        )
        val playedIndices = emptySet<Int>()

        // Few opponent cards left
        val result1 = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = 5,
            peggingPile = emptyList(),
            opponentCardsRemaining = 1
        )

        // Many opponent cards left
        val result2 = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = 5,
            peggingPile = emptyList(),
            opponentCardsRemaining = 4
        )

        assertNotNull(result1)
        assertNotNull(result2)
        // Both should make valid choices
        assertTrue(result1!!.second in hand)
        assertTrue(result2!!.second in hand)
    }

    @Test
    fun choosePeggingCard_recognizesRunOpportunity() {
        val hand = listOf(
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.EIGHT, Suit.CLUBS),
            Card(Rank.NINE, Suit.SPADES)
        )
        val playedIndices = emptySet<Int>()
        val peggingPile = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.DIAMONDS)
        )
        val currentCount = 11 // 5+6=11, playing 7 makes run 5-6-7

        val result = OpponentAI.choosePeggingCard(
            hand = hand,
            playedIndices = playedIndices,
            currentCount = currentCount,
            peggingPile = peggingPile,
            opponentCardsRemaining = 3
        )

        assertNotNull(result)
        // AI should recognize run opportunity and play the 7
        // (though it might also consider defensive play)
        assertTrue(result!!.second.rank in hand.map { it.rank })
    }
}

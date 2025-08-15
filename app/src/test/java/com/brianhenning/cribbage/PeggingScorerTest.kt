package com.brianhenning.cribbage

import com.brianhenning.cribbage.logic.PeggingScorer
import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.assertEquals
import org.junit.Test

class PeggingScorerTest {

    @Test
    fun noPoints_whenNoFifteenNoThirtyOneNoPairNoRun() {
        val pile = listOf(
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.FOUR, Suit.HEARTS)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 6)
        assertEquals(0, pts.total)
        assertEquals(0, pts.fifteen)
        assertEquals(0, pts.thirtyOne)
        assertEquals(0, pts.pairPoints)
        assertEquals(0, pts.runPoints)
    }

    @Test
    fun run_longestTrailingRunOnly() {
        // Earlier cards (4,5,6) form a run, but the trailing window (5,6,9) does not.
        val pile = listOf(
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.SPADES),
            Card(Rank.NINE, Suit.DIAMONDS)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 30)
        assertEquals(0, pts.total)
        assertEquals(0, pts.runPoints)
    }

    @Test
    fun run_doubleDoubleRun_countsMultiplicity() {
        // 3-3-4-4-5 at tail => multiplicity 2*2*1 = 4; run points = 3*4 = 12
        val pile = listOf(
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FOUR, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.CLUBS)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 1 + 1 + 4 + 4 + 5) // 15, but we only check runs here
        assertEquals(12, pts.runPoints)
        // Also gets 2 for fifteen since total is 15
        assertEquals(14, pts.total)
        assertEquals(2, pts.fifteen)
    }

    @Test
    fun pairAndRun_scoredTogether() {
        // Tail 3-4-5-5: double run of 3 (6 pts) + pair on the tail (2 pts) = 8
        val pile = listOf(
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.FIVE, Suit.DIAMONDS)
        )
        val newCount = 3 + 4 + 5 + 5 // 17
        val pts = PeggingScorer.pointsForPile(pile, newCount)
        assertEquals(8, pts.total)
        assertEquals(6, pts.runPoints)
        assertEquals(2, pts.pairPoints)
        assertEquals(2, pts.sameRankCount)
    }

    @Test
    fun run_ofFive_counts() {
        val pile = listOf(
            Card(Rank.NINE, Suit.CLUBS),
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.JACK, Suit.SPADES),
            Card(Rank.QUEEN, Suit.DIAMONDS),
            Card(Rank.KING, Suit.CLUBS)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 9 + 10 + 10 + 10 + 10)
        assertEquals(5, pts.runPoints)
        assertEquals(5, pts.total)
    }

    @Test
    fun fifteen_withPair_scoresBoth() {
        // 1 + 4 + 5 + 5 => last play makes 15 and a pair simultaneously
        val pile = listOf(
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.FIVE, Suit.DIAMONDS)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 15)
        assertEquals(4, pts.total)
        assertEquals(2, pts.fifteen)
        assertEquals(2, pts.pairPoints)
        assertEquals(2, pts.sameRankCount)
        assertEquals(0, pts.runPoints)
    }

    @Test
    fun thirtyOne_withPair_scoresBoth() {
        // 10 + 10 + 1 + 5 + 5 => 31, with last two a pair
        val pile = listOf(
            Card(Rank.TEN, Suit.CLUBS),
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.ACE, Suit.SPADES),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.DIAMONDS)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 31)
        assertEquals(4, pts.total)
        assertEquals(2, pts.thirtyOne)
        assertEquals(2, pts.pairPoints)
        assertEquals(2, pts.sameRankCount)
        assertEquals(0, pts.runPoints)
    }
}


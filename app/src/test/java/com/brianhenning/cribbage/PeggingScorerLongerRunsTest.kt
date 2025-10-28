package com.brianhenning.cribbage

import com.brianhenning.cribbage.shared.domain.logic.PeggingScorer
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for PeggingScorer focusing on longer runs (6 and 7 cards).
 * The code supports runs up to 7 cards but existing tests only cover up to 5.
 * This fills that gap in test coverage.
 */
class PeggingScorerLongerRunsTest {

    @Test
    fun pegging_runOfSix_counts() {
        val pile = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS)
        )
        val count = 1 + 2 + 3 + 4 + 5 + 6

        val pts = PeggingScorer.pointsForPile(pile, newCount = count)

        assertEquals("Run of 6 should score 6 points", 6, pts.runPoints)
        assertEquals(6, pts.total)
    }

    @Test
    fun pegging_runOfSix_outOfOrder_stillScores() {
        // Cards played in non-sequential order but still form a run
        val pile = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.SIX, Suit.DIAMONDS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.ACE, Suit.CLUBS)
        )
        val count = 1 + 2 + 3 + 4 + 5 + 6

        val pts = PeggingScorer.pointsForPile(pile, newCount = count)

        assertEquals("Run of 6 (out of order) should score 6 points", 6, pts.runPoints)
        assertEquals(6, pts.total)
    }

    @Test
    fun pegging_runOfSeven_counts() {
        val pile = listOf(
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.SIX, Suit.SPADES),
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.CLUBS),
            Card(Rank.NINE, Suit.DIAMONDS)
        )
        val count = 3 + 4 + 5 + 6 + 7 + 8 + 9

        val pts = PeggingScorer.pointsForPile(pile, newCount = count)

        assertEquals("Run of 7 should score 7 points", 7, pts.runPoints)
        assertEquals(7, pts.total)
    }

    @Test
    fun pegging_runOfSeven_outOfOrder_stillScores() {
        val pile = listOf(
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.NINE, Suit.DIAMONDS),
            Card(Rank.SIX, Suit.SPADES),
            Card(Rank.EIGHT, Suit.HEARTS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.THREE, Suit.DIAMONDS)
        )
        val count = 3 + 4 + 5 + 6 + 7 + 8 + 9

        val pts = PeggingScorer.pointsForPile(pile, newCount = count)

        assertEquals("Run of 7 (out of order) should score 7 points", 7, pts.runPoints)
        assertEquals(7, pts.total)
    }

    @Test
    fun pegging_runOfSix_withDuplicate_findsShortestRun() {
        // Has 2,3,3,4,5,6 - duplicate 3 breaks the run of 6
        // But last 4 cards (3,4,5,6) still form a run of 4
        val pile = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS)
        )
        val count = 2 + 3 + 3 + 4 + 5 + 6

        val pts = PeggingScorer.pointsForPile(pile, newCount = count)

        // Should find trailing run of 4 (3,4,5,6)
        assertEquals("Should find trailing run of 4", 4, pts.runPoints)
    }

    @Test
    fun pegging_runOfSeven_withGap_findsShortestRun() {
        // Missing rank 6, has 3,4,5,7,8,9,10
        // Last 4 cards (7,8,9,10) form a run of 4
        val pile = listOf(
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.SEVEN, Suit.SPADES), // Gap here breaks longer run
            Card(Rank.EIGHT, Suit.HEARTS),
            Card(Rank.NINE, Suit.CLUBS),
            Card(Rank.TEN, Suit.DIAMONDS)
        )
        val count = 3 + 4 + 5 + 7 + 8 + 9 + 10

        val pts = PeggingScorer.pointsForPile(pile, newCount = count)

        // Should find trailing run of 4 (7,8,9,10)
        assertEquals("Should find trailing run of 4", 4, pts.runPoints)
    }

    @Test
    fun pegging_runOfSix_withAdditionalCards_onlySixScores() {
        // First 2 cards are not part of the trailing run
        val pile = listOf(
            Card(Rank.KING, Suit.HEARTS),   // Not part of run
            Card(Rank.QUEEN, Suit.CLUBS),   // Not part of run
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.SPADES),
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.SIX, Suit.DIAMONDS),
            Card(Rank.SEVEN, Suit.SPADES)
        )
        val count = 10 + 10 + 2 + 3 + 4 + 5 + 6 + 7

        val pts = PeggingScorer.pointsForPile(pile, newCount = count)

        // Should find trailing run of 6 (2-7)
        assertEquals("Should find trailing run of 6", 6, pts.runPoints)
    }

    @Test
    fun pegging_runOfSix_thenBreak_onlySixScores() {
        // Run of 6, then a card that breaks it for longer runs
        val pile = listOf(
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.SIX, Suit.DIAMONDS),
            Card(Rank.SEVEN, Suit.SPADES),
            Card(Rank.EIGHT, Suit.HEARTS),
            Card(Rank.NINE, Suit.CLUBS),
            Card(Rank.KING, Suit.DIAMONDS) // Breaks potential run of 7
        )
        val count = 4 + 5 + 6 + 7 + 8 + 9 + 10

        val pts = PeggingScorer.pointsForPile(pile, newCount = count)

        // No run because last card breaks it
        assertEquals("King breaks the run", 0, pts.runPoints)
    }

    @Test
    fun pegging_maxRun_withFifteen_scoresBoth() {
        // Run of 6 that also makes 15
        val pile = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS)
        )
        // Count is 21, not 15, so no fifteen bonus

        val pts = PeggingScorer.pointsForPile(pile, newCount = 21)
        assertEquals(6, pts.runPoints)
        assertEquals(0, pts.fifteen)
        assertEquals(6, pts.total)
    }

    @Test
    fun pegging_runOfSix_endsAt15_scoresBoth() {
        // Construct a run that totals exactly 15
        // A(1) + 2 + 3 + 4 + 5 = 15, but only 5 cards
        // Let's try: create a scenario where we have a run and 15
        val pile = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.HEARTS)
        )

        val pts = PeggingScorer.pointsForPile(pile, newCount = 15)

        // Run of 5 (A-2-3-4-5) + fifteen
        assertEquals(5, pts.runPoints)
        assertEquals(2, pts.fifteen)
        assertEquals(7, pts.total)
    }

    @Test
    fun pegging_almostRunOfSeven_findsShortestRun() {
        // Seven cards but not consecutive for 7 run
        // Missing 4, has A,2,3,5,6,7,8
        // Last 4 cards (5,6,7,8) form a run of 4
        val pile = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.SPADES), // Missing 4 breaks longer run
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.DIAMONDS)
        )
        val count = 1 + 2 + 3 + 5 + 6 + 7 + 8

        val pts = PeggingScorer.pointsForPile(pile, newCount = count)

        // Should find trailing run of 4 (5,6,7,8)
        assertEquals("Should find trailing run of 4", 4, pts.runPoints)
    }

    @Test
    fun pegging_runOfSix_highCards() {
        // Run with high face cards (8,9,10,J,Q,K)
        val pile = listOf(
            Card(Rank.EIGHT, Suit.HEARTS),
            Card(Rank.NINE, Suit.CLUBS),
            Card(Rank.TEN, Suit.DIAMONDS),
            Card(Rank.JACK, Suit.SPADES),
            Card(Rank.QUEEN, Suit.HEARTS),
            Card(Rank.KING, Suit.CLUBS)
        )
        val count = 8 + 9 + 10 + 10 + 10 + 10

        val pts = PeggingScorer.pointsForPile(pile, newCount = count)

        assertEquals("High card run of 6 should score", 6, pts.runPoints)
    }

    @Test
    fun pegging_emptyPile_noRun() {
        val pts = PeggingScorer.pointsForPile(emptyList(), newCount = 0)

        assertEquals("Empty pile should have no run", 0, pts.runPoints)
        assertEquals(0, pts.total)
    }

    @Test
    fun pegging_singleCard_noRun() {
        val pile = listOf(Card(Rank.FIVE, Suit.HEARTS))

        val pts = PeggingScorer.pointsForPile(pile, newCount = 5)

        assertEquals("Single card cannot form a run", 0, pts.runPoints)
    }

    @Test
    fun pegging_twoCards_noRun() {
        val pile = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS)
        )

        val pts = PeggingScorer.pointsForPile(pile, newCount = 11)

        assertEquals("Two cards cannot form a run (need at least 3)", 0, pts.runPoints)
    }
}

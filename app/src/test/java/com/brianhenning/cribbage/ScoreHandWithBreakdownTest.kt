package com.brianhenning.cribbage

import com.brianhenning.cribbage.shared.domain.logic.CribbageScorer
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the scoreHandWithBreakdown() function which returns detailed score entries.
 * This function was not covered by existing tests.
 */
class ScoreHandWithBreakdownTest {

    @Test
    fun breakdown_classic29Hand_allEntriesPresent() {
        // 3x5 + Jack (same suit as starter), starter is the 4th five
        val hand = listOf(
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.JACK, Suit.DIAMONDS)
        )
        val starter = Card(Rank.FIVE, Suit.DIAMONDS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        assertEquals(29, breakdown.totalScore)

        // Should have: 16 fifteens (2 pts each = 32), 6 pairs (2 pts each = 12),
        // but wait - actual 29 hand has different scoring
        // Let's verify we have entries for fifteens, pairs, and his nobs
        val fifteens = breakdown.entries.filter { it.type == "Fifteen" }
        val pairs = breakdown.entries.filter { it.type == "Pair" }
        val nobs = breakdown.entries.filter { it.type == "His Nobs" }

        assertTrue("Should have fifteen entries", fifteens.isNotEmpty())
        assertTrue("Should have pair entries", pairs.isNotEmpty())
        assertTrue("Should have his nobs entry", nobs.size == 1)

        // Verify his nobs is 1 point
        assertEquals(1, nobs.first().points)
    }

    @Test
    fun breakdown_flushHand_correctEntries() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.HEARTS)
        )
        val starterSame = Card(Rank.TEN, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starterSame, isCrib = false)

        // Should have fifteens (5+K, 5+10) and a 5-card flush
        val flushEntries = breakdown.entries.filter { it.type == "Flush" }
        assertEquals("Should have exactly one flush entry", 1, flushEntries.size)
        assertEquals("Flush should be 5 points with matching starter", 5, flushEntries.first().points)
        assertEquals("Flush should include all 5 cards", 5, flushEntries.first().cards.size)
    }

    @Test
    fun breakdown_fourCardFlush_correctEntries() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.HEARTS)
        )
        val starterDiff = Card(Rank.TEN, Suit.DIAMONDS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starterDiff, isCrib = false)

        val flushEntries = breakdown.entries.filter { it.type == "Flush" }
        assertEquals("Should have exactly one flush entry", 1, flushEntries.size)
        assertEquals("Flush should be 4 points without matching starter", 4, flushEntries.first().points)
        assertEquals("Flush should include only 4 cards", 4, flushEntries.first().cards.size)
    }

    @Test
    fun breakdown_cribFlushOnlyWith5Cards() {
        val cribHand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.HEARTS)
        )
        val starterDiff = Card(Rank.TEN, Suit.DIAMONDS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(cribHand, starterDiff, isCrib = true)

        // Crib flush requires all 5 cards same suit
        val flushEntries = breakdown.entries.filter { it.type == "Flush" }
        assertEquals("Crib should have no flush with different starter suit", 0, flushEntries.size)
    }

    @Test
    fun breakdown_runWithMultiplicity_correctEntries() {
        // Double-double run: 2 pairs with a run
        val hand = listOf(
            Card(Rank.SIX, Suit.CLUBS),
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.SEVEN, Suit.SPADES)
        )
        val starter = Card(Rank.EIGHT, Suit.CLUBS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val runEntries = breakdown.entries.filter { it.type == "Sequence" }
        val pairEntries = breakdown.entries.filter { it.type == "Pair" }

        // Should have 4 runs of 3 (6-7-8 with each combination)
        assertEquals("Should have 4 run entries for double-double run", 4, runEntries.size)
        runEntries.forEach { entry ->
            assertEquals("Each run should be 3 cards", 3, entry.cards.size)
            assertEquals("Each run should be worth 3 points", 3, entry.points)
        }

        // Should have 2 pairs (6-6 and 7-7)
        assertEquals("Should have 2 pair entries", 2, pairEntries.size)
    }

    @Test
    fun breakdown_multipleFifteens_eachTracked() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.TEN, Suit.DIAMONDS),
            Card(Rank.KING, Suit.SPADES)
        )
        val starter = Card(Rank.QUEEN, Suit.CLUBS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val fifteens = breakdown.entries.filter { it.type == "Fifteen" }

        // Should have: 5+10, 5+K, 5+Q, 5+10 (second 5), 5+K, 5+Q = 6 fifteens
        assertEquals("Should have 6 fifteen combinations", 6, fifteens.size)
        fifteens.forEach { entry ->
            assertEquals("Each fifteen should be worth 2 points", 2, entry.points)
            val sum = entry.cards.sumOf { it.getValue() }
            assertEquals("Each combination should sum to 15", 15, sum)
        }
    }

    @Test
    fun breakdown_zeroScoreHand_noEntries() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.NINE, Suit.SPADES)
        )
        val starter = Card(Rank.KING, Suit.CLUBS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        assertEquals("Zero-scoring hand should have 0 points", 0, breakdown.totalScore)
        assertEquals("Zero-scoring hand should have no entries", 0, breakdown.entries.size)
    }

    @Test
    fun breakdown_tripleRun_correctMultipleSequences() {
        // Triple run: one pair, one run of 4
        val hand = listOf(
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.FOUR, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val starter = Card(Rank.SIX, Suit.CLUBS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val runEntries = breakdown.entries.filter { it.type == "Sequence" }

        // Should have 2 runs of 4 (3-4-5-6 with each 3)
        assertEquals("Should have 2 run-of-4 entries", 2, runEntries.size)
        runEntries.forEach { entry ->
            assertEquals("Each run should be 4 cards", 4, entry.cards.size)
            assertEquals("Each run should be worth 4 points", 4, entry.points)
        }
    }

    @Test
    fun breakdown_pairsOnly_correctPairEntries() {
        val hand = listOf(
            Card(Rank.KING, Suit.CLUBS),
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.ACE, Suit.DIAMONDS),
            Card(Rank.ACE, Suit.SPADES)
        )
        val starter = Card(Rank.NINE, Suit.CLUBS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val pairEntries = breakdown.entries.filter { it.type == "Pair" }

        // Should have 2 distinct pairs
        assertEquals("Should have 2 pair entries", 2, pairEntries.size)
        pairEntries.forEach { entry ->
            assertEquals("Each pair should be 2 cards", 2, entry.cards.size)
            assertEquals("Each pair should be worth 2 points", 2, entry.points)
        }
    }

    @Test
    fun breakdown_threeOfAKind_correctPairEntries() {
        val hand = listOf(
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.ACE, Suit.SPADES)
        )
        val starter = Card(Rank.TWO, Suit.CLUBS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val pairEntries = breakdown.entries.filter { it.type == "Pair" }

        // 3 of a kind = 3 pairs (3 choose 2)
        assertEquals("Three of a kind should generate 3 pair entries", 3, pairEntries.size)

        val totalPairPoints = pairEntries.sumOf { it.points }
        assertEquals("Three of a kind should be worth 6 points from pairs", 6, totalPairPoints)
    }
}

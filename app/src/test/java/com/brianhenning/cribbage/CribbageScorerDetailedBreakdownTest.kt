package com.brianhenning.cribbage

import com.brianhenning.cribbage.shared.domain.logic.CribbageScorer
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive tests for CribbageScorer.scoreHandWithBreakdown() function.
 * Tests that the detailed breakdown correctly identifies and scores all
 * combinations including fifteens, pairs, runs, flushes, and his nobs.
 */
class CribbageScorerDetailedBreakdownTest {

    // ========== Fifteens Breakdown Tests ==========

    @Test
    fun scoreHandWithBreakdown_singleFifteen_createsOneEntry() {
        val hand = listOf(
            Card(Rank.NINE, Suit.HEARTS),
            Card(Rank.SIX, Suit.DIAMONDS),
            Card(Rank.KING, Suit.CLUBS),
            Card(Rank.QUEEN, Suit.SPADES)
        )
        val starter = Card(Rank.JACK, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val fifteenEntries = breakdown.entries.filter { it.type == "Fifteen" }
        // 9+6=15 only (K+Q+J are all 10, none combine to 15)
        assertEquals("Should have exactly 1 fifteen", 1, fifteenEntries.size)
        assertEquals("Fifteen should score 2 points", 2, fifteenEntries[0].points)
    }

    @Test
    fun scoreHandWithBreakdown_multipleFifteens_createsMultipleEntries() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.TEN, Suit.SPADES)
        )
        val starter = Card(Rank.JACK, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val fifteenEntries = breakdown.entries.filter { it.type == "Fifteen" }
        // 7 fifteens: Each of 3 fives with 10 (3), each of 3 fives with J (3), 5+5+5 (1) = 7
        assertEquals("Should have 7 fifteens (3 fives * 2 tens + 5+5+5)", 7, fifteenEntries.size)
        val fifteenScore = fifteenEntries.sumOf { it.points }
        assertEquals("Seven fifteens should score 14", 14, fifteenScore)
    }

    @Test
    fun scoreHandWithBreakdown_noFifteens_createsNoFifteenEntries() {
        val hand = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.DIAMONDS),
            Card(Rank.JACK, Suit.CLUBS),
            Card(Rank.TEN, Suit.SPADES)
        )
        val starter = Card(Rank.NINE, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val fifteenEntries = breakdown.entries.filter { it.type == "Fifteen" }
        assertEquals("Should have no fifteen entries", 0, fifteenEntries.size)
    }

    @Test
    fun scoreHandWithBreakdown_threeCombinationFifteen_identifiesCorrectly() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.TEN, Suit.CLUBS),
            Card(Rank.ACE, Suit.SPADES)
        )
        val starter = Card(Rank.NINE, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val fifteenEntries = breakdown.entries.filter { it.type == "Fifteen" }
        // 2+3+10=15, 1+2+3+9=15
        assertTrue("Should have fifteens from multi-card combinations", fifteenEntries.size >= 1)
    }

    // ========== Pairs Breakdown Tests ==========

    @Test
    fun scoreHandWithBreakdown_onePair_createsOneEntry() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.TWO, Suit.SPADES)
        )
        val starter = Card(Rank.THREE, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val pairEntries = breakdown.entries.filter { it.type == "Pair" }
        assertEquals("Should have exactly 1 pair entry", 1, pairEntries.size)
        assertEquals("Pair should score 2 points", 2, pairEntries[0].points)
    }

    @Test
    fun scoreHandWithBreakdown_threeSameRank_createsThreePairs() {
        val hand = listOf(
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.JACK, Suit.DIAMONDS),
            Card(Rank.JACK, Suit.CLUBS),
            Card(Rank.ACE, Suit.SPADES)
        )
        val starter = Card(Rank.TWO, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val pairEntries = breakdown.entries.filter { it.type == "Pair" }
        // Three jacks make 3 pairs: J1-J2, J1-J3, J2-J3
        assertEquals("Three of a kind should create 3 pair entries", 3, pairEntries.size)
        val pairScore = pairEntries.sumOf { it.points }
        assertEquals("Three pairs should score 6", 6, pairScore)
    }

    @Test
    fun scoreHandWithBreakdown_fourSameRank_createsSixPairs() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val starter = Card(Rank.ACE, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val pairEntries = breakdown.entries.filter { it.type == "Pair" }
        // Four fives make 6 pairs: C(4,2) = 6
        assertEquals("Four of a kind should create 6 pair entries", 6, pairEntries.size)
        val pairScore = pairEntries.sumOf { it.points }
        assertEquals("Six pairs should score 12", 12, pairScore)
    }

    @Test
    fun scoreHandWithBreakdown_multiplePairs_createsCorrectEntries() {
        val hand = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.KING, Suit.DIAMONDS),
            Card(Rank.QUEEN, Suit.CLUBS),
            Card(Rank.QUEEN, Suit.SPADES)
        )
        val starter = Card(Rank.ACE, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val pairEntries = breakdown.entries.filter { it.type == "Pair" }
        // Two pairs: K-K and Q-Q
        assertEquals("Should have 2 pair entries", 2, pairEntries.size)
        val pairScore = pairEntries.sumOf { it.points }
        assertEquals("Two pairs should score 4", 4, pairScore)
    }

    // ========== Run/Sequence Breakdown Tests ==========

    @Test
    fun scoreHandWithBreakdown_simpleRun_createsOneEntry() {
        val hand = listOf(
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.FOUR, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.ACE, Suit.SPADES)
        )
        val starter = Card(Rank.TWO, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val runEntries = breakdown.entries.filter { it.type == "Sequence" }
        // A-2-3-4-5 is a 5-card run
        assertEquals("Should have 1 run entry", 1, runEntries.size)
        assertEquals("Five-card run should score 5", 5, runEntries[0].points)
    }

    @Test
    fun scoreHandWithBreakdown_doubleRun_createsMultipleEntries() {
        val hand = listOf(
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.THREE, Suit.DIAMONDS),  // Duplicate
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val starter = Card(Rank.ACE, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val runEntries = breakdown.entries.filter { it.type == "Sequence" }
        // Two 3-4-5 runs (with each 3)
        assertEquals("Double run should create 2 entries", 2, runEntries.size)
        val runScore = runEntries.sumOf { it.points }
        assertEquals("Two 3-card runs should score 6", 6, runScore)
    }

    @Test
    fun scoreHandWithBreakdown_tripleRun_createsThreeEntries() {
        val hand = listOf(
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SIX, Suit.DIAMONDS),
            Card(Rank.SIX, Suit.CLUBS),  // Triple 6
            Card(Rank.SEVEN, Suit.SPADES)
        )
        val starter = Card(Rank.EIGHT, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val runEntries = breakdown.entries.filter { it.type == "Sequence" }
        // Three 6-7-8 runs (one for each 6)
        assertEquals("Triple run should create 3 entries", 3, runEntries.size)
        val runScore = runEntries.sumOf { it.points }
        assertEquals("Three 3-card runs should score 9", 9, runScore)
    }

    @Test
    fun scoreHandWithBreakdown_noRun_createsNoRunEntries() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.TEN, Suit.CLUBS),
            Card(Rank.KING, Suit.SPADES)
        )
        val starter = Card(Rank.TWO, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val runEntries = breakdown.entries.filter { it.type == "Sequence" }
        assertEquals("Should have no run entries", 0, runEntries.size)
    }

    // ========== Flush Breakdown Tests ==========

    @Test
    fun scoreHandWithBreakdown_fourCardFlush_nonCrib_scoresFour() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.KING, Suit.HEARTS)
        )
        val starter = Card(Rank.TWO, Suit.DIAMONDS)  // Different suit

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter, isCrib = false)

        val flushEntries = breakdown.entries.filter { it.type == "Flush" }
        assertEquals("Should have 1 flush entry", 1, flushEntries.size)
        assertEquals("Four-card flush should score 4", 4, flushEntries[0].points)
        assertEquals("Flush should only include hand cards", 4, flushEntries[0].cards.size)
    }

    @Test
    fun scoreHandWithBreakdown_fiveCardFlush_nonCrib_scoresFive() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.KING, Suit.HEARTS)
        )
        val starter = Card(Rank.TWO, Suit.HEARTS)  // Same suit

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter, isCrib = false)

        val flushEntries = breakdown.entries.filter { it.type == "Flush" }
        assertEquals("Should have 1 flush entry", 1, flushEntries.size)
        assertEquals("Five-card flush should score 5", 5, flushEntries[0].points)
        assertEquals("Flush should include all 5 cards", 5, flushEntries[0].cards.size)
    }

    @Test
    fun scoreHandWithBreakdown_fourCardFlush_inCrib_doesNotScore() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.KING, Suit.HEARTS)
        )
        val starter = Card(Rank.TWO, Suit.DIAMONDS)  // Different suit

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter, isCrib = true)

        val flushEntries = breakdown.entries.filter { it.type == "Flush" }
        assertEquals("Crib flush needs all 5 cards", 0, flushEntries.size)
    }

    @Test
    fun scoreHandWithBreakdown_fiveCardFlush_inCrib_scoresFive() {
        val hand = listOf(
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.TEN, Suit.CLUBS),
            Card(Rank.KING, Suit.CLUBS)
        )
        val starter = Card(Rank.TWO, Suit.CLUBS)  // Same suit

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter, isCrib = true)

        val flushEntries = breakdown.entries.filter { it.type == "Flush" }
        assertEquals("Should have 1 flush entry", 1, flushEntries.size)
        assertEquals("Five-card crib flush should score 5", 5, flushEntries[0].points)
    }

    // ========== His Nobs Breakdown Tests ==========

    @Test
    fun scoreHandWithBreakdown_hisNobs_createsOneEntry() {
        val hand = listOf(
            Card(Rank.JACK, Suit.HEARTS),  // Jack of hearts
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.TEN, Suit.CLUBS),
            Card(Rank.ACE, Suit.SPADES)
        )
        val starter = Card(Rank.TWO, Suit.HEARTS)  // Hearts starter

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val nobsEntries = breakdown.entries.filter { it.type == "His Nobs" }
        assertEquals("Should have 1 his nobs entry", 1, nobsEntries.size)
        assertEquals("His nobs should score 1", 1, nobsEntries[0].points)
        assertEquals("His nobs should include jack and starter", 2, nobsEntries[0].cards.size)
    }

    @Test
    fun scoreHandWithBreakdown_noHisNobs_wrongSuit_createsNoEntry() {
        val hand = listOf(
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.TEN, Suit.CLUBS),
            Card(Rank.ACE, Suit.SPADES)
        )
        val starter = Card(Rank.TWO, Suit.DIAMONDS)  // Different suit

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val nobsEntries = breakdown.entries.filter { it.type == "His Nobs" }
        assertEquals("Should have no his nobs entry", 0, nobsEntries.size)
    }

    @Test
    fun scoreHandWithBreakdown_multipleJacks_onlyMatchingSuitScores() {
        val hand = listOf(
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.JACK, Suit.DIAMONDS),
            Card(Rank.JACK, Suit.CLUBS),
            Card(Rank.ACE, Suit.SPADES)
        )
        val starter = Card(Rank.TWO, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val nobsEntries = breakdown.entries.filter { it.type == "His Nobs" }
        assertEquals("Only matching jack should score nobs", 1, nobsEntries.size)
    }

    // ========== Total Score Consistency Tests ==========

    @Test
    fun scoreHandWithBreakdown_totalScore_matchesSumOfEntries() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.TEN, Suit.CLUBS),
            Card(Rank.JACK, Suit.SPADES)
        )
        val starter = Card(Rank.FIVE, Suit.CLUBS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val sumOfEntries = breakdown.entries.sumOf { it.points }
        assertEquals("Total should match sum of all entries", sumOfEntries, breakdown.totalScore)
    }

    @Test
    fun scoreHandWithBreakdown_totalScore_matchesScoreHand() {
        val hand = listOf(
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.EIGHT, Suit.CLUBS),
            Card(Rank.NINE, Suit.SPADES)
        )
        val starter = Card(Rank.TEN, Suit.HEARTS)

        val detailedScore = CribbageScorer.scoreHandWithBreakdown(hand, starter)
        val simpleScore = CribbageScorer.scoreHand(hand, starter)

        assertEquals("Detailed score should match simple score", simpleScore, detailedScore.totalScore)
    }

    @Test
    fun scoreHandWithBreakdown_perfectHand_hasCorrectBreakdown() {
        // Perfect 28-point hand: J-5-5-5 with 5 starter (jack doesn't match starter suit for nobs)
        val hand = listOf(
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.CLUBS)
        )
        val starter = Card(Rank.FIVE, Suit.SPADES)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        assertEquals("Perfect hand should score 28", 28, breakdown.totalScore)

        // Should have: 8 fifteens (16 pts), 6 pairs (12 pts) = 28 (no nobs since jack suit doesn't match)
        val fifteens = breakdown.entries.filter { it.type == "Fifteen" }.size
        val pairs = breakdown.entries.filter { it.type == "Pair" }.size

        assertEquals("Should have 8 fifteen combinations", 8, fifteens)
        assertEquals("Should have 6 pair combinations", 6, pairs)
    }

    // ========== Complex Hand Tests ==========

    @Test
    fun scoreHandWithBreakdown_complexHand_allTypes_countsCorrectly() {
        // Hand with run, pairs, and fifteens (but no 4-card flush since one card doesn't match)
        val hand = listOf(
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.SPADES)  // Pair of 8s, breaks flush
        )
        val starter = Card(Rank.NINE, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter, isCrib = false)

        val hasFifteens = breakdown.entries.any { it.type == "Fifteen" }
        val hasPairs = breakdown.entries.any { it.type == "Pair" }
        val hasRuns = breakdown.entries.any { it.type == "Sequence" }

        // No flush since 4 cards must match and one 8 is spades (5-card flush requires all 5 match)
        assertTrue("Should have fifteens", hasFifteens)
        assertTrue("Should have pairs", hasPairs)
        assertTrue("Should have runs", hasRuns)

        val simpleScore = CribbageScorer.scoreHand(hand, starter, isCrib = false)
        assertEquals("Breakdown total should match scoreHand", simpleScore, breakdown.totalScore)
    }

    @Test
    fun scoreHandWithBreakdown_emptyEntries_zeroScore() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.KING, Suit.SPADES)
        )
        val starter = Card(Rank.NINE, Suit.DIAMONDS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        assertTrue("Should have minimal or no scoring entries", breakdown.entries.isEmpty() || breakdown.totalScore < 5)
        assertEquals("Total should match sum of entries", breakdown.entries.sumOf { it.points }, breakdown.totalScore)
    }

    // ========== Entry Validation Tests ==========

    @Test
    fun scoreHandWithBreakdown_allEntries_haveValidPoints() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.TEN, Suit.DIAMONDS),
            Card(Rank.JACK, Suit.CLUBS),
            Card(Rank.QUEEN, Suit.SPADES)
        )
        val starter = Card(Rank.KING, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        breakdown.entries.forEach { entry ->
            assertTrue("All entries should have positive points", entry.points > 0)
            assertTrue("Entry should have associated cards", entry.cards.isNotEmpty())
            assertTrue("Entry should have a type", entry.type.isNotEmpty())
        }
    }

    @Test
    fun scoreHandWithBreakdown_allEntries_haveValidTypes() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.HEARTS)
        )
        val starter = Card(Rank.NINE, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        val validTypes = setOf("Fifteen", "Pair", "Sequence", "Flush", "His Nobs")
        breakdown.entries.forEach { entry ->
            assertTrue("Entry type should be valid: ${entry.type}", entry.type in validTypes)
        }
    }
}

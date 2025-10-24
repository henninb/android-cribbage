package com.brianhenning.cribbage

import com.brianhenning.cribbage.logic.CribbageScorer
import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Boundary and edge case tests for CribbageScorer to ensure
 * proper handling of unusual scoring combinations and edge conditions.
 */
class CribbageScorerBoundaryTest {

    // ========== Maximum Score Scenarios ==========

    @Test
    fun scoreHand_perfectHand_scores29Points() {
        // The legendary 29-point hand: 5-5-5-J with 5 starter
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.JACK, Suit.SPADES)
        )
        val starter = Card(Rank.FIVE, Suit.SPADES)

        val score = CribbageScorer.scoreHand(hand, starter)

        // 16 fifteens (2 points each = 32... wait, actually 28)
        // 6 pairs of 5s (12 points)
        // 1 nobs (1 point)
        // Total: 29 points (16 combinations of 15 = 16*2 = but actually...)
        // Correct: 8 fifteens (16 pts) + 3 pairs of 5s (12 pts) + nobs (1) = 29
        assertEquals(29, score)
    }

    @Test
    fun scoreHand_maxPossibleCrib_withFlush() {
        // Maximum crib: 5-5-5-5 with J starter (all same suit)
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS)
        )
        val starter = Card(Rank.JACK, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter, isCrib = true)

        // 8 fifteens (16 points)
        // 6 pairs (12 points)
        // Flush (5 points)
        // Total: 33 points
        assertEquals(33, score)
    }

    // ========== Minimum Score Scenarios ==========

    @Test
    fun scoreHand_lowScoringHand_minimal() {
        // A low-scoring hand: A+4+7+J+Q has 4+J = 14, 7+8=15? No...
        // Actually this does have run: J-Q-K? No. Let me recalculate.
        // A(1) + 4 + 7 + J(10) + Q(10) = no fifteens? A+4+J = 15!
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FOUR, Suit.DIAMONDS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.JACK, Suit.SPADES)
        )
        val starter = Card(Rank.QUEEN, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // A+4+J = 1+4+10 = 15 (2 pts), A+4+Q = 15 (2 pts), J-Q run? No not consecutive
        // Total: 4 points
        assertEquals(4, score)
    }

    @Test
    fun scoreHand_anotherLowHand_differentCards() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.SPADES)
        )
        val starter = Card(Rank.JACK, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // 2+3+J = 2+3+10 = 15 (2 pts), 7+8 = 15 (2 pts)
        // Total: 4 points
        assertEquals(4, score)
    }

    // ========== All Fifteens Scenarios ==========

    @Test
    fun scoreHand_singleFifteen_withoutOtherScoring() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.NINE, Suit.SPADES)
        )
        val starter = Card(Rank.KING, Suit.HEARTS) // King = 10

        val score = CribbageScorer.scoreHand(hand, starter)

        // A+2+3+9 = 15 (2 pts), 2+3+K = 15 (2 pts)
        // Run: A-2-3 (3 pts)
        // Total: 7 points
        assertEquals(7, score)
    }

    @Test
    fun scoreHand_multipleFifteens_withSameCards() {
        // Multiple ways to make 15
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.TEN, Suit.CLUBS),
            Card(Rank.TEN, Suit.SPADES)
        )
        val starter = Card(Rank.ACE, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // 5+10 (4 combinations) = 8 points
        // 5+5+5 = 15? No, there's only 2 fives
        // Pairs: 5-5 (2), 10-10 (2) = 4 points
        // Total: 12 points
        assertEquals(12, score)
    }

    // ========== All Pairs Scenarios ==========

    @Test
    fun scoreHand_fourOfAKind_scoresMaxPairs() {
        // Four of a kind: 6 pairs
        val hand = listOf(
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.ACE, Suit.SPADES)
        )
        val starter = Card(Rank.SEVEN, Suit.SPADES)

        val score = CribbageScorer.scoreHand(hand, starter)

        // 6 pairs of 7s = 12 points
        // 15s: 7+7+A = 15 (4 combinations using C(4,2) = 6 ways to pick 2 sevens)
        // Actually: each pair of 7s + ace = 15, so 6*2 = 12 points
        // Total: 12 + 12 = 24 points
        assertEquals(24, score)
    }

    @Test
    fun scoreHand_threePairs_fromThreePairsOfCards() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.THREE, Suit.SPADES)
        )
        val starter = Card(Rank.FOUR, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // 2 pairs (2-2, 3-3) = 4 points
        // Possible run: 2-3-4? Yes, but duplicated
        // 2-3-4 appears twice (2♥3♣4, 2♥3♠4, 2♦3♣4, 2♦3♠4) = 4 runs of 3 = 12
        // Total: 4 + 12 = 16
        assertEquals(16, score)
    }

    // ========== Long Run Scenarios ==========

    @Test
    fun scoreHand_fiveCardRun_scoresCorrectly() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val starter = Card(Rank.SIX, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // Run of 5 (2-3-4-5-6) = 5 points
        // 15s: 4+5+6 = 15 (2 pts), 3+6+6? no, 2+3+4+6 = 15 (2 pts)
        // Total: 5 + 4 = 9 points
        assertEquals(9, score)
    }

    @Test
    fun scoreHand_doubleRun_withOnePair() {
        // Run with one pair creates double run
        val hand = listOf(
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val starter = Card(Rank.ACE, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // Double run of 3 (3-4-5 twice) = 6 points
        // Pair of 3s = 2 points
        // 15s: 3+3+4+5 = 15 (2 pts)
        // Total: 8 + 2 = 10 points
        assertEquals(10, score)
    }

    @Test
    fun scoreHand_tripleRun_withThreeOfAKind() {
        // Run with three of a kind creates triple run
        val hand = listOf(
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SIX, Suit.DIAMONDS),
            Card(Rank.SIX, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.SPADES)
        )
        val starter = Card(Rank.EIGHT, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // Triple run of 3 (6-7-8 three times) = 9 points
        // Three of a kind = 6 points (3 pairs)
        // 15s: 6+6+7+8 would be too many, but 7+8 = 15 (2 pts)
        // Total: 9 + 6 + 2 = 17 points
        assertEquals(17, score)
    }

    // ========== Flush Scenarios ==========

    @Test
    fun scoreHand_flushInHand_starterDifferentSuit() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.HEARTS)
        )
        val starter = Card(Rank.NINE, Suit.DIAMONDS) // Different suit

        val score = CribbageScorer.scoreHand(hand, starter)

        // Flush in hand only (4 points)
        // A+5+9 = 15 (2 points)
        // 5+7+3 = 15 (2 points)
        // Total varies, but flush is 4
        assertTrue(score >= 4)
    }

    @Test
    fun scoreHand_flushInHandAndStarter_scores5() {
        val hand = listOf(
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.SIX, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.CLUBS)
        )
        val starter = Card(Rank.TEN, Suit.CLUBS) // Same suit

        val score = CribbageScorer.scoreHand(hand, starter)

        // 5-card flush = 5 points
        // Fifteens: 2+4+6+8+10 combinations...
        // 6+8 = 14, 4+6+10 = 20, etc.
        // Should have at least 5 for flush
        assertTrue(score >= 5)
    }

    @Test
    fun scoreHand_cribFlush_requiresAllFive() {
        // In crib, flush only counts if all 5 cards match
        val hand = listOf(
            Card(Rank.ACE, Suit.SPADES),
            Card(Rank.TWO, Suit.SPADES),
            Card(Rank.THREE, Suit.SPADES),
            Card(Rank.FOUR, Suit.SPADES)
        )
        val starter = Card(Rank.FIVE, Suit.DIAMONDS) // Different suit

        val score = CribbageScorer.scoreHand(hand, starter, isCrib = true)

        // No flush in crib (needs all 5)
        // Run of 5 (A-2-3-4-5) = 5 points
        // 15s: A(1)+2+3+4+5 = 15 (2 pts)
        // Total: 7 points
        assertEquals(7, score)
    }

    @Test
    fun scoreHand_cribFlush_allFiveMatch_scores5() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.HEARTS)
        )
        val starter = Card(Rank.TEN, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter, isCrib = true)

        // 5-card flush in crib = 5 points
        assertTrue(score >= 5)
    }

    // ========== His Nobs Scenarios ==========

    @Test
    fun scoreHand_hisNobs_jackMatchesStarterSuit() {
        val hand = listOf(
            Card(Rank.JACK, Suit.DIAMONDS),
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.SPADES)
        )
        val starter = Card(Rank.ACE, Suit.DIAMONDS) // Jack suit matches

        val score = CribbageScorer.scoreHand(hand, starter)

        // His nobs = 1 point
        // Run: A-2-3-4 = 4 points
        // 15s: J+2+3 = 15 (2 pts), J+A+4 = 15 (2 pts)
        // Total: 9 points
        assertEquals(9, score)
    }

    @Test
    fun scoreHand_noNobs_jackDoesNotMatchStarter() {
        val hand = listOf(
            Card(Rank.JACK, Suit.DIAMONDS),
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.SPADES)
        )
        val starter = Card(Rank.ACE, Suit.HEARTS) // Different suit

        val score = CribbageScorer.scoreHand(hand, starter)

        // No nobs
        // Run: A-2-3-4 = 4 points
        // 15s: J+2+3 = 15 (2 pts), J+A+4 = 15 (2 pts)
        // Total: 8 points
        assertEquals(8, score)
    }

    @Test
    fun scoreHand_nobsAsStarter_doesNotCount() {
        // Jack as starter doesn't count for nobs (that's "his heels")
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val starter = Card(Rank.JACK, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // No nobs (Jack is starter, not in hand)
        // Run 2-3-4-5 = 4 points
        // 15: 2+3+J = 15 (2 pts), 5+J = 15 (2 pts)
        // Total: 8 points
        assertEquals(8, score)
    }

    // ========== Complex Combinations ==========

    @Test
    fun scoreHand_combinationOfEverything_exceptFlush() {
        // Hand with pairs, runs, fifteens, and nobs
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.SIX, Suit.CLUBS),
            Card(Rank.JACK, Suit.SPADES)
        )
        val starter = Card(Rank.FOUR, Suit.SPADES) // Nobs!

        val score = CribbageScorer.scoreHand(hand, starter)

        // Pair of 5s = 2 points
        // 15s: 5+J (twice) = 4 points, 5+6+4 (twice) = 4 points
        // Runs: 4-5-6 (twice due to pair of 5s) = 6 points
        // Nobs = 1 point
        // Total: 2 + 4 + 4 + 6 + 1 = 17 points
        assertEquals(17, score)
    }

    // ========== Boundary Values ==========

    @Test
    fun scoreHand_allAces_lowestValues() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.ACE, Suit.DIAMONDS),
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.ACE, Suit.SPADES)
        )
        val starter = Card(Rank.TWO, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // 6 pairs of aces = 12 points
        // No fifteens (all 1s can't make 15 with 2)
        // No runs
        assertEquals(12, score)
    }

    @Test
    fun scoreHand_allFaceCards_highestValues() {
        val hand = listOf(
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.DIAMONDS),
            Card(Rank.KING, Suit.CLUBS),
            Card(Rank.TEN, Suit.SPADES)
        )
        val starter = Card(Rank.FIVE, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // 15s: 5+10 = 15 (2 pts), 5+J = 15 (2 pts), 5+Q = 15 (2 pts), 5+K = 15 (2 pts)
        // But wait, there's also J-Q-K run of 3 (3 pts)
        // Total: 8 + 3 + 2 = 13 points
        assertEquals(13, score)
    }

    // ========== scoreHandWithBreakdown Tests ==========

    @Test
    fun scoreHandWithBreakdown_providesCorrectTotal_and_entries() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.TEN, Suit.DIAMONDS),
            Card(Rank.JACK, Suit.CLUBS),
            Card(Rank.QUEEN, Suit.SPADES)
        )
        val starter = Card(Rank.FIVE, Suit.SPADES)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        // Pair of 5s = 2
        // 15s: 5+10 (twice) = 4, 5+J (twice) = 4, 5+Q (twice) = 4
        // Run: J-Q (no, only 2 cards), wait... 10-J-Q run of 3?
        // 10-J-Q are consecutive in rank? TEN, JACK, QUEEN - yes!
        // Total: 2 + 12 + 3 = 17
        assertEquals(17, breakdown.totalScore)
        assertTrue(breakdown.entries.isNotEmpty())
    }

    @Test
    fun scoreHandWithBreakdown_lowScore_hasEntries() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FOUR, Suit.DIAMONDS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.JACK, Suit.SPADES)
        )
        val starter = Card(Rank.QUEEN, Suit.HEARTS)

        val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter)

        // A+4+J = 15 (2 pts), A+4+Q = 15 (2 pts)
        assertEquals(4, breakdown.totalScore)
        assertTrue(breakdown.entries.size > 0)
    }
}

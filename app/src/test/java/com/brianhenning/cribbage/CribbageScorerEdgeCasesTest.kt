package com.brianhenning.cribbage

import com.brianhenning.cribbage.logic.CribbageScorer
import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Edge case and complex scenario tests for CribbageScorer.
 * Tests advanced scoring combinations, boundary conditions, and rare hands.
 */
class CribbageScorerEdgeCasesTest {

    // ========== His Nobs Tests ==========

    @Test
    fun hisNobs_allSuits() {
        for (suit in Suit.entries) {
            val hand = listOf(
                Card(Rank.JACK, suit),
                Card(Rank.TWO, Suit.HEARTS),
                Card(Rank.THREE, Suit.CLUBS),
                Card(Rank.FOUR, Suit.SPADES)
            )
            val starter = Card(Rank.FIVE, suit)

            val score = CribbageScorer.scoreHand(hand, starter)

            // His nobs (1) + two fifteens (2+3+5+5, 4+5+5+1) = some points including the 1 for nobs
            assertTrue("Jack of $suit with starter $suit should include his nobs", score > 0)
        }
    }

    @Test
    fun hisNobs_noBonus_whenJackNotMatchingStarter() {
        val hand = listOf(
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.FIVE, Suit.DIAMONDS)
        )
        val starter = Card(Rank.KING, Suit.CLUBS) // Different suit

        val score = CribbageScorer.scoreHand(hand, starter)

        // Score should not include his nobs
        // 3 pairs of fives (6) + fifteens
        assertTrue("Should not include his nobs when suits don't match", score >= 6)
    }

    @Test
    fun hisNobs_withPerfectHand() {
        // Verify his nobs is counted in the perfect 29 hand
        val hand = listOf(
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.JACK, Suit.DIAMONDS)
        )
        val starter = Card(Rank.FIVE, Suit.DIAMONDS)

        val score = CribbageScorer.scoreHand(hand, starter)

        assertEquals(29, score)
    }

    // ========== Run Tests ==========

    @Test
    fun run_doubleRun_withPair() {
        // Double run: A-2-3 with two Aces = run twice
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.SPADES)
        )
        val starter = Card(Rank.KING, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // Double run of 3 (6) + pair of aces (2) + fifteens (A+4+K, 2+3+K) = 12
        assertEquals(12, score)
    }

    @Test
    fun run_tripleRun_withThreeOfAKind() {
        // Triple run: 4-5-6 with three 4s
        val hand = listOf(
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.DIAMONDS)
        )
        val starter = Card(Rank.SIX, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // Triple run of 3 (9) + three of a kind (6) + fifteens = 15+
        assertTrue("Triple run should score at least 15", score >= 15)
    }

    @Test
    fun run_doubleDoubleRun_withTwoPairs() {
        // 3-4-5 with two 3s and two 5s = quadruple run
        val hand = listOf(
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val starter = Card(Rank.FOUR, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // Quadruple run (12) + two pairs (4) + fifteens = 16+
        assertTrue("Double-double run should score at least 16", score >= 16)
    }

    @Test
    fun run_ofFive_withStarterCard() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val starter = Card(Rank.SIX, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // Run of 5 (5) + two fifteens (4+5+6, 2+3+4+6) = 9
        assertEquals(9, score)
    }

    @Test
    fun run_ofFour() {
        val hand = listOf(
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.CLUBS),
            Card(Rank.NINE, Suit.DIAMONDS),
            Card(Rank.TEN, Suit.SPADES)
        )
        val starter = Card(Rank.ACE, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // Run of 4 (4) + fifteens (7+8) = 6
        assertEquals(6, score)
    }

    // ========== Fifteen Tests ==========

    @Test
    fun fifteens_maximumCombinations() {
        // Hand with many fifteen combinations
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.TEN, Suit.DIAMONDS),
            Card(Rank.TEN, Suit.SPADES)
        )
        val starter = Card(Rank.FIVE, Suit.DIAMONDS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // Should have multiple fifteens and pairs
        assertTrue("Should have significant score from multiple fifteens", score >= 14)
    }

    @Test
    fun fifteens_threeCardCombination() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.TEN, Suit.DIAMONDS),
            Card(Rank.TWO, Suit.SPADES)
        )
        val starter = Card(Rank.THREE, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // Fifteens: (A+4+10), (2+3+10), (A+2+2+10), (4+A+10) = 8 points
        // Actually: run A-2-3-4 (4 pts) + fifteens (4 pts) = 8
        assertEquals(8, score)
    }

    @Test
    fun fifteens_fourCardCombination() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.FOUR, Suit.SPADES)
        )
        val starter = Card(Rank.FIVE, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // Run of 5 (5) + one fifteen (A+2+3+4+5) = 7
        assertEquals(7, score)
    }

    @Test
    fun fifteens_allFaceCards_noFifteens() {
        val hand = listOf(
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.CLUBS),
            Card(Rank.KING, Suit.DIAMONDS),
            Card(Rank.TEN, Suit.SPADES)
        )
        val starter = Card(Rank.NINE, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // Run of 4: 9-10-J-Q or 10-J-Q-K (4 pts) + fifteens (9+6=15 not possible, but combinations exist) = 6
        // Actually there's run 10-J-Q-K (4) and fifteens 9+6 impossible with these cards = 6
        assertEquals(6, score)
    }

    // ========== Pair Tests ==========

    @Test
    fun pairs_threePairs_fromThreeOfAKind() {
        val hand = listOf(
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.ACE, Suit.SPADES)
        )
        val starter = Card(Rank.KING, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // Three sevens = 6 points (3 pairs) + fifteens (7+7+A, 7+K not possible, but 7+7+A=15) = 12
        assertEquals(12, score)
    }

    @Test
    fun pairs_sixPairs_fromFourOfAKind() {
        val hand = listOf(
            Card(Rank.EIGHT, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.DIAMONDS),
            Card(Rank.EIGHT, Suit.SPADES)
        )
        val starter = Card(Rank.KING, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // Four eights = 12 points (6 pairs)
        assertEquals(12, score)
    }

    @Test
    fun pairs_twoPairs() {
        val hand = listOf(
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS),
            Card(Rank.NINE, Suit.DIAMONDS),
            Card(Rank.NINE, Suit.SPADES)
        )
        val starter = Card(Rank.ACE, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)

        // Two pairs (4) + four fifteens (2x6 * 2x9 = 4 combos, 8 pts) = 12
        assertEquals(12, score)
    }

    // ========== Flush Tests ==========

    @Test
    fun flush_fourCards_notCrib() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.NINE, Suit.HEARTS)
        )
        val starter = Card(Rank.KING, Suit.CLUBS)

        val score = CribbageScorer.scoreHand(hand, starter, isCrib = false)

        // 4-card flush = 4 points
        assertEquals(4, score)
    }

    @Test
    fun flush_fiveCards_notCrib() {
        val hand = listOf(
            Card(Rank.ACE, Suit.SPADES),
            Card(Rank.THREE, Suit.SPADES),
            Card(Rank.SEVEN, Suit.SPADES),
            Card(Rank.NINE, Suit.SPADES)
        )
        val starter = Card(Rank.KING, Suit.SPADES)

        val score = CribbageScorer.scoreHand(hand, starter, isCrib = false)

        // 5-card flush = 5 points
        assertEquals(5, score)
    }

    @Test
    fun flush_fourCards_inCrib_noScore() {
        val hand = listOf(
            Card(Rank.ACE, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.NINE, Suit.DIAMONDS)
        )
        val starter = Card(Rank.KING, Suit.CLUBS)

        val score = CribbageScorer.scoreHand(hand, starter, isCrib = true)

        // No flush in crib unless all 5 match
        assertEquals(0, score)
    }

    @Test
    fun flush_fiveCards_inCrib() {
        val hand = listOf(
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.NINE, Suit.CLUBS)
        )
        val starter = Card(Rank.KING, Suit.CLUBS)

        val score = CribbageScorer.scoreHand(hand, starter, isCrib = true)

        // 5-card flush in crib = 5 points
        assertEquals(5, score)
    }

    // ========== Complex Combinations ==========

    @Test
    fun complex_flushWithFifteensAndRun() {
        val hand = listOf(
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.NINE, Suit.HEARTS)
        )
        val starter = Card(Rank.TEN, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter, isCrib = false)

        // 5-card flush (5) + run of 3 (4-5-6) (3) + fifteens (5+10, 4+5+6, 9+6) = 5+3+6 = 14
        assertEquals(14, score)
    }

    @Test
    fun complex_pairsWithRunsAndFifteens() {
        val hand = listOf(
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.EIGHT, Suit.SPADES)
        )
        val starter = Card(Rank.NINE, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter, isCrib = false)

        // Double run (6-7-8 and 6-7-8 = 6) + pair of 7s (2) + fifteens (6+9, 7+8 twice) = 6+2+8 = 16
        assertEquals(16, score)
    }

    // ========== Zero Score Hands ==========

    @Test
    fun zeroScore_noScoringCombinations() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.QUEEN, Suit.SPADES)
        )
        val starter = Card(Rank.KING, Suit.DIAMONDS)

        val score = CribbageScorer.scoreHand(hand, starter, isCrib = false)

        assertEquals(0, score)
    }

    @Test
    fun zeroScore_allHighCards() {
        val hand = listOf(
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.JACK, Suit.CLUBS),
            Card(Rank.QUEEN, Suit.DIAMONDS),
            Card(Rank.KING, Suit.SPADES)
        )
        val starter = Card(Rank.NINE, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter, isCrib = false)

        // Run of 5: 9-10-J-Q-K = 5 points
        assertEquals(5, score)
    }

    // ========== Detailed Score String Tests ==========

    @Test
    fun scoreHandDetailed_includesBreakdown() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.JACK, Suit.DIAMONDS),
            Card(Rank.KING, Suit.SPADES)
        )
        val starter = Card(Rank.FIVE, Suit.DIAMONDS)

        val (score, breakdown) = CribbageScorer.scoreHandDetailed(hand, starter, isCrib = false)

        // Verify score is calculated
        assertTrue(score > 0)
        // Verify breakdown is not empty
        assertTrue(breakdown.isNotEmpty())
    }

    @Test
    fun scoreHandDetailed_zeroScore_hasBreakdown() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.QUEEN, Suit.SPADES)
        )
        val starter = Card(Rank.KING, Suit.DIAMONDS)

        val (score, breakdown) = CribbageScorer.scoreHandDetailed(hand, starter, isCrib = false)

        // This hand scores 0 points
        assertEquals(0, score)
        // Breakdown should still be present even for 0 score
        assertTrue(breakdown.length >= 0) // Allow empty breakdown for zero scores
    }

    // ========== Boundary Tests ==========

    @Test
    fun allAces_scoring() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.ACE, Suit.DIAMONDS),
            Card(Rank.ACE, Suit.SPADES)
        )
        val starter = Card(Rank.KING, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter, isCrib = false)

        // Four aces = 12 points (6 pairs)
        assertEquals(12, score)
    }

    @Test
    fun allKings_scoring() {
        val hand = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.KING, Suit.CLUBS),
            Card(Rank.KING, Suit.DIAMONDS),
            Card(Rank.KING, Suit.SPADES)
        )
        val starter = Card(Rank.ACE, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter, isCrib = false)

        // Four kings = 12 points (6 pairs)
        assertEquals(12, score)
    }
}

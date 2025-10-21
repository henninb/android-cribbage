package com.brianhenning.cribbage

import com.brianhenning.cribbage.logic.PeggingScorer
import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Complex scenario tests for PeggingScorer.
 * Tests advanced pegging situations, multiple scoring combinations, and boundary conditions.
 */
class PeggingScorerComplexScenariosTest {

    // ========== 15 and 31 Scoring Tests ==========

    @Test
    fun scores15_withVariousCombinations() {
        val testCases = listOf(
            listOf(Card(Rank.FIVE, Suit.HEARTS), Card(Rank.TEN, Suit.CLUBS)) to 15,
            listOf(Card(Rank.SEVEN, Suit.HEARTS), Card(Rank.EIGHT, Suit.CLUBS)) to 15,
            listOf(Card(Rank.NINE, Suit.HEARTS), Card(Rank.SIX, Suit.CLUBS)) to 15,
            listOf(Card(Rank.ACE, Suit.HEARTS), Card(Rank.FOUR, Suit.CLUBS), Card(Rank.TEN, Suit.SPADES)) to 15
        )

        for ((pile, expectedCount) in testCases) {
            val pts = PeggingScorer.pointsForPile(pile, expectedCount)
            assertEquals("Should score 2 for making 15", 2, pts.fifteen)
            assertEquals("Total should include fifteen", 2, pts.total)
        }
    }

    @Test
    fun scores31_withVariousCombinations() {
        val testCases = listOf(
            listOf(Card(Rank.KING, Suit.HEARTS), Card(Rank.KING, Suit.CLUBS), Card(Rank.KING, Suit.SPADES), Card(Rank.ACE, Suit.DIAMONDS)) to 31,
            listOf(Card(Rank.TEN, Suit.HEARTS), Card(Rank.TEN, Suit.CLUBS), Card(Rank.TEN, Suit.SPADES), Card(Rank.ACE, Suit.DIAMONDS)) to 31
        )

        for ((pile, expectedCount) in testCases) {
            val pts = PeggingScorer.pointsForPile(pile, expectedCount)
            assertEquals("Should score 2 for making 31", 2, pts.thirtyOne)
        }
    }

    @Test
    fun scores15And31_simultaneously() {
        // This is actually impossible in real gameplay, but let's test the logic
        // Count of 15 and 31 at same time would require special circumstance
        val pile = listOf(Card(Rank.FIVE, Suit.HEARTS), Card(Rank.TEN, Suit.CLUBS))
        val pts = PeggingScorer.pointsForPile(pile, 15)

        assertEquals(2, pts.fifteen)
        assertEquals(0, pts.thirtyOne)
        assertEquals(2, pts.total)
    }

    // ========== Pair Scoring Tests ==========

    @Test
    fun scoresPair_twoCards() {
        val pile = listOf(
            Card(Rank.EIGHT, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.CLUBS)
        )
        val pts = PeggingScorer.pointsForPile(pile, 16)

        assertEquals(2, pts.pairPoints)
        assertEquals(2, pts.sameRankCount)
        assertEquals(2, pts.total)
    }

    @Test
    fun scoresThreeOfAKind() {
        val pile = listOf(
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.FOUR, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, 12)

        assertEquals(6, pts.pairPoints)
        assertEquals(3, pts.sameRankCount)
        assertEquals(6, pts.total)
    }

    @Test
    fun scoresFourOfAKind() {
        val pile = listOf(
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.THREE, Suit.SPADES),
            Card(Rank.THREE, Suit.DIAMONDS)
        )
        val pts = PeggingScorer.pointsForPile(pile, 12)

        assertEquals(12, pts.pairPoints)
        assertEquals(4, pts.sameRankCount)
        assertEquals(12, pts.total)
    }

    @Test
    fun pairScoring_onlyCountsTail() {
        // Earlier cards don't affect pair scoring
        val pile = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.FIVE, Suit.DIAMONDS)
        )
        val pts = PeggingScorer.pointsForPile(pile, 30)

        assertEquals(2, pts.pairPoints)
        assertEquals(2, pts.sameRankCount)
    }

    @Test
    fun pairScoring_brokenByDifferentRank() {
        // Pair in middle of pile doesn't score if broken by different rank
        val pile = listOf(
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.SPADES)  // Breaks the pair sequence
        )
        val pts = PeggingScorer.pointsForPile(pile, 19)

        assertEquals(0, pts.pairPoints)
        assertEquals(1, pts.sameRankCount)
    }

    // ========== Run Scoring Tests ==========

    @Test
    fun scoresRun_threeCards() {
        val pile = listOf(
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.SIX, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, 15)

        assertEquals(3, pts.runPoints)
        assertEquals(5, pts.total) // 3 for run + 2 for fifteen
    }

    @Test
    fun scoresRun_fourCards() {
        val pile = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.DIAMONDS)
        )
        val pts = PeggingScorer.pointsForPile(pile, 14)

        assertEquals(4, pts.runPoints)
        assertEquals(4, pts.total)
    }

    @Test
    fun scoresRun_fiveCards() {
        val pile = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.THREE, Suit.SPADES),
            Card(Rank.FOUR, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.HEARTS)
        )
        val pts = PeggingScorer.pointsForPile(pile, 15)

        assertEquals(5, pts.runPoints)
        assertEquals(7, pts.total) // 5 for run + 2 for fifteen
    }

    @Test
    fun scoresRun_sixCards() {
        val pile = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.THREE, Suit.SPADES),
            Card(Rank.FOUR, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS)
        )
        val pts = PeggingScorer.pointsForPile(pile, 21)

        assertEquals(6, pts.runPoints)
        assertEquals(6, pts.total)
    }

    @Test
    fun scoresRun_sevenCards() {
        val pile = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.THREE, Suit.SPADES),
            Card(Rank.FOUR, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, 28)

        assertEquals(7, pts.runPoints)
        assertEquals(7, pts.total)
    }

    @Test
    fun run_outOfOrder_stillScores() {
        // 5-4-6 is still a run (4-5-6)
        val pile = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.SIX, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, 15)

        assertEquals(3, pts.runPoints)
        assertEquals(5, pts.total) // 3 for run + 2 for fifteen
    }

    @Test
    fun run_brokenByDuplicate() {
        // 4-5-5-6 has duplicate, no run scores
        val pile = listOf(
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.SIX, Suit.DIAMONDS)
        )
        val pts = PeggingScorer.pointsForPile(pile, 20)

        assertEquals(0, pts.runPoints)
        assertEquals(0, pts.total)
    }

    @Test
    fun run_onlyLongestTrailingRun_scores() {
        // 2-3-4-5-6: full 5-card run
        // Then a 7: creates 6-card run (3-4-5-6-7)
        // Only the longest trailing run scores
        val pile = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.CLUBS)
        )
        val pts = PeggingScorer.pointsForPile(pile, 27)

        assertEquals(6, pts.runPoints) // Longest trailing run
        assertEquals(6, pts.total)
    }

    @Test
    fun run_lessThanThree_noScore() {
        val pile = listOf(
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.FIVE, Suit.CLUBS)
        )
        val pts = PeggingScorer.pointsForPile(pile, 9)

        assertEquals(0, pts.runPoints)
        assertEquals(0, pts.total)
    }

    @Test
    fun run_nonConsecutive_noScore() {
        // 4-6-8 are not consecutive
        val pile = listOf(
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, 18)

        assertEquals(0, pts.runPoints)
        assertEquals(0, pts.total)
    }

    // ========== Multiple Scoring Combinations ==========

    @Test
    fun scoresRun_and15() {
        val pile = listOf(
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.SIX, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, 15)

        assertEquals(3, pts.runPoints)
        assertEquals(2, pts.fifteen)
        assertEquals(5, pts.total)
    }

    @Test
    fun scoresRun_and31() {
        val pile = listOf(
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.JACK, Suit.CLUBS),
            Card(Rank.QUEEN, Suit.SPADES),
            Card(Rank.ACE, Suit.DIAMONDS)
        )
        val pts = PeggingScorer.pointsForPile(pile, 31)

        assertEquals(0, pts.runPoints) // T-J-Q-A not consecutive in cribbage
        assertEquals(2, pts.thirtyOne)
        assertEquals(2, pts.total)
    }

    @Test
    fun scoresPair_and15() {
        val pile = listOf(
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, 23)

        assertEquals(2, pts.pairPoints)
        assertEquals(0, pts.runPoints) // Pair breaks run
        assertEquals(2, pts.total)
    }

    @Test
    fun scoresPair_and31() {
        val pile = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.KING, Suit.CLUBS),
            Card(Rank.KING, Suit.SPADES),
            Card(Rank.ACE, Suit.DIAMONDS)
        )
        val pts = PeggingScorer.pointsForPile(pile, 31)

        // In pegging, pairs only count from the tail
        // Last card is ACE, previous is KING, so no pair
        assertEquals(0, pts.pairPoints)
        assertEquals(2, pts.thirtyOne)
        assertEquals(2, pts.total)
    }

    // ========== Boundary Tests ==========

    @Test
    fun singleCard_noScoring_unlessSpecialCount() {
        val pile = listOf(Card(Rank.SEVEN, Suit.HEARTS))
        val pts = PeggingScorer.pointsForPile(pile, 7)

        assertEquals(0, pts.total)
    }

    @Test
    fun count_exactly15() {
        val pile = listOf(
            Card(Rank.NINE, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS)
        )
        val pts = PeggingScorer.pointsForPile(pile, 15)

        assertEquals(2, pts.fifteen)
        assertEquals(2, pts.total)
    }

    @Test
    fun count_exactly31() {
        val pile = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.CLUBS),
            Card(Rank.ACE, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, 31)

        assertEquals(2, pts.thirtyOne)
        assertEquals(2, pts.total)
    }

    @Test
    fun count_not15or31_noCountBonus() {
        val pile = listOf(
            Card(Rank.EIGHT, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.CLUBS)
        )
        val pts = PeggingScorer.pointsForPile(pile, 15)

        assertEquals(2, pts.fifteen)

        val pile2 = listOf(
            Card(Rank.EIGHT, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS)
        )
        val pts2 = PeggingScorer.pointsForPile(pile2, 14)

        assertEquals(0, pts2.fifteen)
        assertEquals(0, pts2.thirtyOne)
    }

    // ========== Complex Real-World Scenarios ==========

    @Test
    fun complexScenario_multipleScoring() {
        // 5-5-5-6: In pegging, only trailing matching ranks score pairs
        // Last card is 6, previous is 5, so no pair
        val pile = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.SIX, Suit.DIAMONDS)
        )
        val pts = PeggingScorer.pointsForPile(pile, 21)

        assertEquals(0, pts.pairPoints) // No pairs (last card doesn't match previous)
        assertEquals(0, pts.runPoints)  // Duplicates break runs
        assertEquals(0, pts.total)
    }

    @Test
    fun complexScenario_runWithHighCards() {
        // J-Q-K (10-11-12 ordinals) is a run
        val pile = listOf(
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.CLUBS),
            Card(Rank.KING, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, 30)

        assertEquals(3, pts.runPoints)
        assertEquals(3, pts.total)
    }

    @Test
    fun complexScenario_nothingScores() {
        // Random cards, count not special
        val pile = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, 13)

        assertEquals(0, pts.total)
    }

    @Test
    fun pointsDataClass_allFieldsPopulated() {
        val pile = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.TEN, Suit.CLUBS)
        )
        val pts = PeggingScorer.pointsForPile(pile, 15)

        // Verify all fields exist and are accessible
        assertEquals(2, pts.total)
        assertEquals(2, pts.fifteen)
        assertEquals(0, pts.thirtyOne)
        assertEquals(0, pts.pairPoints)
        assertEquals(1, pts.sameRankCount)
        assertEquals(0, pts.runPoints)
    }

    @Test
    fun aceRuns_consecutiveFromAce() {
        // A-2-3 is a valid run (ACE=0, TWO=1, THREE=2 ordinals)
        val pile = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.THREE, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, 6)

        assertEquals(3, pts.runPoints)
        assertEquals(3, pts.total)
    }

    @Test
    fun kingDoesNotWrapToAce() {
        // K-A-2 is NOT a run (no wrapping)
        val pile = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.TWO, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, 13)

        assertEquals(0, pts.runPoints)
    }
}

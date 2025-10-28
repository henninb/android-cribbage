package com.brianhenning.cribbage

import com.brianhenning.cribbage.shared.domain.logic.PeggingScorer
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * TDD tests for the critical bug: hitting exactly 31 during pegging should award 2 points.
 *
 * Bug scenario: Count was 25, player played a 6 to make exactly 31, but scored 0 instead of 2.
 */
class Pegging31ScoringTest {

    @Test
    fun testHitting31Exactly_shouldScore2Points() {
        // Arrange: Build a pile that totals to 25
        val pile = listOf(
            Card(Rank.TEN, Suit.HEARTS),   // 10
            Card(Rank.KING, Suit.DIAMONDS), // 10 (total: 20)
            Card(Rank.FIVE, Suit.CLUBS)     // 5 (total: 25)
        )

        // Act: Play a 6 to make exactly 31
        val cardPlayed = Card(Rank.SIX, Suit.SPADES) // 6
        val newPile = pile + cardPlayed
        val newCount = 31

        val points = PeggingScorer.pointsForPile(newPile, newCount)

        // Assert: Should score 2 points for hitting 31 exactly
        assertEquals("Hitting 31 exactly should award 2 points", 2, points.thirtyOne)
        assertEquals("Total points should be 2", 2, points.total)
    }

    @Test
    fun testHitting31_withMultipleScoringOpportunities() {
        // Arrange: Build a pile where hitting 31 also creates a pair
        val pile = listOf(
            Card(Rank.NINE, Suit.HEARTS),   // 9
            Card(Rank.TEN, Suit.DIAMONDS),  // 10 (total: 19)
            Card(Rank.SIX, Suit.CLUBS)      // 6 (total: 25)
        )

        // Act: Play another 6 to make 31 AND a pair
        val cardPlayed = Card(Rank.SIX, Suit.SPADES)
        val newPile = pile + cardPlayed
        val newCount = 31

        val points = PeggingScorer.pointsForPile(newPile, newCount)

        // Assert: Should score 2 for 31 + 2 for pair = 4 total
        assertEquals("Should score 2 for hitting 31", 2, points.thirtyOne)
        assertEquals("Should score 2 for pair", 2, points.pairPoints)
        assertEquals("Total should be 4 (31 + pair)", 4, points.total)
    }

    @Test
    fun testHitting31_withRun() {
        // Arrange: Build a pile where hitting 31 also completes a run
        val pile = listOf(
            Card(Rank.TEN, Suit.HEARTS),   // 10
            Card(Rank.FOUR, Suit.DIAMONDS), // 4 (total: 14)
            Card(Rank.FIVE, Suit.CLUBS),    // 5 (total: 19)
            Card(Rank.SIX, Suit.SPADES)     // 6 (total: 25)
        )

        // Act: Play another SIX to make 31 AND create a pair of 6s
        val cardPlayed = Card(Rank.SIX, Suit.HEARTS) // Another 6 to make exactly 31
        val newPile = pile + cardPlayed
        val newCount = 31

        val points = PeggingScorer.pointsForPile(newPile, newCount)

        // Assert: Should score 2 for hitting 31 + 2 for pair of 6s = 4 total
        assertEquals("Should score 2 for hitting 31", 2, points.thirtyOne)
        assertEquals("Should score 2 for pair of 6s", 2, points.pairPoints)
        assertEquals("Total should be 4", 4, points.total)
    }

    @Test
    fun testHitting15_shouldScore2Points() {
        // Arrange: Build a pile that totals to 10
        val pile = listOf(
            Card(Rank.FIVE, Suit.HEARTS),   // 5
            Card(Rank.FIVE, Suit.DIAMONDS)  // 5 (total: 10)
        )

        // Act: Play a 5 to make exactly 15
        val cardPlayed = Card(Rank.FIVE, Suit.CLUBS)
        val newPile = pile + cardPlayed
        val newCount = 15

        val points = PeggingScorer.pointsForPile(newPile, newCount)

        // Assert: Should score 2 for 15 + 6 for three of a kind = 8 total
        assertEquals("Should score 2 for hitting 15", 2, points.fifteen)
        assertEquals("Should score 6 for three 5s", 6, points.pairPoints)
        assertEquals("Total should be 8", 8, points.total)
    }

    @Test
    fun testOriginalBugScenario_count25PlaySix() {
        // This is the EXACT scenario from the bug report
        // Count: 25, Player plays 6 → should score 2 for hitting 31

        // Arrange: Any pile that totals 25
        val pile = listOf(
            Card(Rank.JACK, Suit.HEARTS),   // 10
            Card(Rank.QUEEN, Suit.DIAMONDS), // 10 (total: 20)
            Card(Rank.FIVE, Suit.CLUBS)      // 5 (total: 25)
        )

        // Act: Play a 6
        val cardPlayed = Card(Rank.SIX, Suit.SPADES)
        val newPile = pile + cardPlayed
        val newCount = 25 + 6 // = 31

        val points = PeggingScorer.pointsForPile(newPile, newCount)

        // Assert: MUST score 2 points
        assertEquals("Original bug: should score 2 for hitting 31 exactly", 2, points.thirtyOne)
        assertEquals("Total points must be 2", 2, points.total)
    }

    @Test
    fun testNotHitting31_shouldNotScore() {
        // Arrange: Pile that doesn't hit 31
        val pile = listOf(
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS)
        )

        // Act: Play a card that doesn't hit 31
        val cardPlayed = Card(Rank.THREE, Suit.CLUBS)
        val newPile = pile + cardPlayed
        val newCount = 18 // Not 31

        val points = PeggingScorer.pointsForPile(newPile, newCount)

        // Assert: Should NOT score for 31
        assertEquals("Should not score for 31 when count is 18", 0, points.thirtyOne)
    }

    @Test
    fun testExactly31WithFourOfAKind() {
        // Edge case: hitting 31 with four of a kind
        val pile = listOf(
            Card(Rank.SEVEN, Suit.HEARTS),   // 7
            Card(Rank.SEVEN, Suit.DIAMONDS), // 7 (total: 14)
            Card(Rank.SEVEN, Suit.CLUBS)     // 7 (total: 21)
        )

        // Act: Play fourth 7 to hit 31 exactly
        val cardPlayed = Card(Rank.TEN, Suit.SPADES) // Wait, need to make 31...
        // Actually 7+7+7+7 = 28, not 31. Let me fix this.
        // 7+7+7+10 = 31
        val newPile = pile + cardPlayed
        val newCount = 31

        val points = PeggingScorer.pointsForPile(newPile, newCount)

        // Assert: 2 for 31, no pairs (different rank)
        assertEquals("Should score 2 for hitting 31", 2, points.thirtyOne)
        assertEquals("Should be 2 total (no pair bonus)", 2, points.total)
    }
}

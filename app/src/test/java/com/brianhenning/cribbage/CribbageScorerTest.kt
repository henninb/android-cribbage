package com.brianhenning.cribbage

import com.brianhenning.cribbage.logic.CribbageScorer
import com.brianhenning.cribbage.logic.PeggingScorer
import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.assertEquals
import org.junit.Test

class CribbageScorerTest {

    // ---- Hand scoring ----

    @Test
    fun handScore_classic29() {
        // 3x5 + Jack (same suit as starter), starter is the 4th five
        val hand = listOf(
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.JACK, Suit.DIAMONDS) // his nobs: matches starter suit
        )
        val starter = Card(Rank.FIVE, Suit.DIAMONDS)

        val score = CribbageScorer.scoreHand(hand, starter)
        assertEquals(29, score)
    }

    @Test
    fun handScore_perfect28() {
        // 3x5 + Jack (different suit from starter), starter is the 4th five
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.JACK, Suit.SPADES),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val starter = Card(Rank.FIVE, Suit.DIAMONDS)

        val score = CribbageScorer.scoreHand(hand, starter)
        assertEquals(28, score)
    }

    @Test
    fun handScore_zero() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.NINE, Suit.SPADES)
        )
        val starter = Card(Rank.KING, Suit.CLUBS)

        val score = CribbageScorer.scoreHand(hand, starter)
        assertEquals(0, score)
    }

    @Test
    fun handScore_flush() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.HEARTS)
        )
        val starterDiff = Card(Rank.TEN, Suit.DIAMONDS)
        val starterSame = Card(Rank.TEN, Suit.HEARTS)

        val score4 = CribbageScorer.scoreHand(hand, starterDiff)
        val score5 = CribbageScorer.scoreHand(hand, starterSame)
        // Fifteens: (5+K) and (5+10starter) = 4 pts; plus flush
        assertEquals(8, score4) // 4 flush + 4 for fifteens
        assertEquals(9, score5) // 5 flush + 4 for fifteens
    }

    @Test
    fun handScore_cribFlushRule() {
        val cribHand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.HEARTS)
        )
        val starterDifferent = Card(Rank.TEN, Suit.DIAMONDS)
        val starterSame = Card(Rank.TEN, Suit.HEARTS)

        val scoreNoFlush = CribbageScorer.scoreHand(cribHand, starterDifferent, isCrib = true)
        val scoreCribFlush = CribbageScorer.scoreHand(cribHand, starterSame, isCrib = true)
        // No crib flush but still two fifteens: (5+K) and (5+10starter)
        assertEquals(4, scoreNoFlush)
        // Crib flush (5) plus two fifteens (4)
        assertEquals(9, scoreCribFlush)
    }

    @Test
    fun handScore_kingHand_pairsAndFifteens() {
        val hand = listOf(
            Card(Rank.KING, Suit.CLUBS),
            Card(Rank.KING, Suit.SPADES),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val starter = Card(Rank.TEN, Suit.HEARTS)

        val score = CribbageScorer.scoreHand(hand, starter)
        // 2 pairs (4 pts) + 6 fifteens (12 pts; two Ks + two 5s + starter 10) = 16
        assertEquals(16, score)
    }

    @Test
    fun handScore_runsWithMultipleFifteens() {
        val hand = listOf(
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.DIAMONDS),
            Card(Rank.NINE, Suit.HEARTS),
            Card(Rank.ACE, Suit.SPADES)
        )
        val starter = Card(Rank.SIX, Suit.CLUBS)

        val score = CribbageScorer.scoreHand(hand, starter)
        // Run of 4 (6-7-8-9) = 4; fifteens: (7+8), (6+9), (6+8+Ace) = 3*2 = 6; total 10
        assertEquals(10, score)
    }

    // ---- Pegging scoring ----

    @Test
    fun pegging_pair() {
        val pile = listOf(
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.DIAMONDS)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 10)
        assertEquals(2, pts.total)
        assertEquals(2, pts.pairPoints)
        assertEquals(2, pts.sameRankCount)
    }

    @Test
    fun pegging_threeOfAKind() {
        val pile = listOf(
            Card(Rank.NINE, Suit.CLUBS),
            Card(Rank.NINE, Suit.HEARTS),
            Card(Rank.NINE, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 30)
        assertEquals(6, pts.total)
        assertEquals(6, pts.pairPoints)
        assertEquals(3, pts.sameRankCount)
    }

    @Test
    fun pegging_fourOfAKind() {
        val pile = listOf(
            Card(Rank.NINE, Suit.CLUBS),
            Card(Rank.NINE, Suit.HEARTS),
            Card(Rank.NINE, Suit.SPADES),
            Card(Rank.NINE, Suit.DIAMONDS)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 40)
        assertEquals(12, pts.total)
        assertEquals(12, pts.pairPoints)
        assertEquals(4, pts.sameRankCount)
    }

    @Test
    fun pegging_runOf3() {
        val pile = listOf(
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 18)
        assertEquals(3, pts.total)
        assertEquals(3, pts.runPoints)
    }

    @Test
    fun pegging_runOf4() {
        val pile = listOf(
            Card(Rank.EIGHT, Suit.CLUBS),
            Card(Rank.NINE, Suit.HEARTS),
            Card(Rank.TEN, Suit.SPADES),
            Card(Rank.JACK, Suit.DIAMONDS)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 39)
        assertEquals(4, pts.total)
        assertEquals(4, pts.runPoints)
    }

    @Test
    fun pegging_doubleRun_withMultiplicity() {
        // Last 4 cards contain two 7s making a double run of 5-6-7
        val pile = listOf(
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 25)
        assertEquals(6, pts.total) // 3-length run * multiplicity 2
        assertEquals(6, pts.runPoints)
    }

    @Test
    fun pegging_fifteen_and_thirtyOne() {
        val pile15 = listOf(
            Card(Rank.EIGHT, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.HEARTS)
        )
        val pts15 = PeggingScorer.pointsForPile(pile15, newCount = 15)
        assertEquals(2, pts15.total)
        assertEquals(2, pts15.fifteen)

        val pile31 = listOf(
            Card(Rank.TEN, Suit.SPADES),
            Card(Rank.QUEEN, Suit.DIAMONDS),
            Card(Rank.JACK, Suit.HEARTS)
        )
        val pts31 = PeggingScorer.pointsForPile(pile31, newCount = 31)
        // Also scores a run of 3 (10,J,Q) in pegging
        assertEquals(5, pts31.total)
        assertEquals(2, pts31.thirtyOne)
    }
}

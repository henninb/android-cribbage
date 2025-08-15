package com.brianhenning.cribbage

import com.brianhenning.cribbage.logic.PeggingScorer
import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.assertEquals
import org.junit.Test

class PeggingScorerCombosTest {

    @Test
    fun unorderedRunOfFour_scoresFour() {
        // 9,6,8,7 (unordered) should score a 4-run
        val pile = listOf(
            Card(Rank.NINE, Suit.CLUBS),
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.SPADES),
            Card(Rank.SEVEN, Suit.DIAMONDS)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 9 + 6 + 8 + 7)
        assertEquals(4, pts.runPoints)
        assertEquals(4, pts.total)
    }

    @Test
    fun runOfThree_andThirtyOne_scoresFive() {
        // Classic 10-J-Q (run of 3) and we pass newCount=31 to add +2
        val pile = listOf(
            Card(Rank.TEN, Suit.CLUBS),
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 31)
        assertEquals(3, pts.runPoints)
        assertEquals(2, pts.thirtyOne)
        assertEquals(5, pts.total)
    }

    @Test
    fun unorderedRunOfThree_scoresThree() {
        val pile = listOf(
            Card(Rank.NINE, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.SPADES)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 9 + 7 + 8)
        assertEquals(3, pts.runPoints)
        assertEquals(3, pts.total)
    }

    @Test
    fun duplicatesBreakRun_example() {
        // Duplicates break the run, but a tail pair still scores
        val pile = listOf(
            Card(Rank.EIGHT, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.SIX, Suit.SPADES),
            Card(Rank.SIX, Suit.DIAMONDS)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 8 + 7 + 6 + 6)
        assertEquals(0, pts.runPoints)
        // Tail duplicates still score pair
        assertEquals(2, pts.pairPoints)
        assertEquals(2, pts.total)
    }
}

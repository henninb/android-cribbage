package com.brianhenning.cribbage

import com.brianhenning.cribbage.logic.PeggingScorer
import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.assertEquals
import org.junit.Test

class PeggingScorerCombosTest {

    @Test
    fun runWithMultiplicity_andThirtyOne_scoresBoth() {
        // Trailing window 7-8-8-9 -> run of 3 with multiplicity 2 => 6 points
        val pile = listOf(
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.SPADES),
            Card(Rank.NINE, Suit.DIAMONDS)
        )
        val pts = PeggingScorer.pointsForPile(pile, newCount = 31)
        assertEquals(6, pts.runPoints)
        assertEquals(2, pts.thirtyOne)
        assertEquals(8, pts.total)
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
}


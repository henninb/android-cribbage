package com.brianhenning.cribbage

import com.brianhenning.cribbage.logic.PeggingScorer
import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.assertEquals
import org.junit.Test

class PeggingScorerEdgeCasesTest {

    @Test
    fun emptyPile_scoresZero() {
        val pile = emptyList<Card>()
        val pts = PeggingScorer.pointsForPile(pile, newCount = 0)
        assertEquals(0, pts.total)
        assertEquals(0, pts.fifteen)
        assertEquals(0, pts.thirtyOne)
        assertEquals(0, pts.pairPoints)
        assertEquals(0, pts.runPoints)
    }

    @Test
    fun longRunWithMultiplicity_countsProductAcrossTrailingWindow() {
        // 2-3-3-4-5-5 => trailing window is entire pile; ranks 2,3,4,5 are consecutive
        // counts per rank: 1,2,1,2 -> product 4; run length 4 -> 16 points
        val pile = listOf(
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.THREE, Suit.SPADES),
            Card(Rank.FOUR, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.HEARTS)
        )
        val count = 2 + 3 + 3 + 4 + 5 + 5
        val pts = PeggingScorer.pointsForPile(pile, newCount = count)
        assertEquals(16, pts.runPoints)
        // Also gets +2 for the trailing pair of fives
        assertEquals(2, pts.pairPoints)
        assertEquals(18, pts.total)
    }
}

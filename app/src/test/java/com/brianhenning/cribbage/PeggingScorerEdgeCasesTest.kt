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
    fun duplicatesInTrailingWindow_breakRunOnlyPairsScore() {
        // 2-3-3-4-5-5 => duplicates break runs; only the tail pair of fives scores
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
        assertEquals(0, pts.runPoints)
        assertEquals(2, pts.pairPoints)
        assertEquals(2, pts.total)
    }
}

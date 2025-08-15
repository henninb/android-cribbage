package com.brianhenning.cribbage

import com.brianhenning.cribbage.logic.dealSixToEach
import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.createDeck
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class StarterDrawTest {

    @Test
    fun starterDrawnFromRemaining_isNotInEitherHand() {
        val deck = createDeck().toMutableList()
        // Assume player is not dealer for this test (doesn't matter for uniqueness)
        val res = dealSixToEach(deck, playerIsDealer = false)
        val remaining = res.remainingDeck
        val starter = remaining.first()

        // Starter must not be in either hand
        assertFalse(res.playerHand.contains(starter))
        assertFalse(res.opponentHand.contains(starter))
        // And remaining deck should not be empty and should differ from top dealt card
        assertNotEquals(res.playerHand.first(), starter)
    }

    @Test
    fun dealtCardsAreUnique_andDisjointFromStarter() {
        // Use a deterministic deck for clarity
        val deck = createDeck().toMutableList()
        val res = dealSixToEach(deck, playerIsDealer = true)

        val player = res.playerHand
        val opp = res.opponentHand
        val remaining = res.remainingDeck

        // Hands contain 12 unique cards with no overlap
        val combined = player + opp
        val uniqueCombined = combined.toSet()
        assertEquals(12, combined.size)
        assertEquals(12, uniqueCombined.size)

        // Remaining deck does not contain any dealt card
        for (c in combined) {
            assertFalse(remaining.contains(c))
        }

        // Draw starter from remaining; must not be in either hand
        val starter = remaining.first()
        assertFalse(player.contains(starter))
        assertFalse(opp.contains(starter))
    }
}

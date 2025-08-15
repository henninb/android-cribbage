package com.brianhenning.cribbage

import com.brianhenning.cribbage.logic.PeggingRoundManager
import com.brianhenning.cribbage.logic.Player
import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.*
import org.junit.Test

class PeggingRoundManagerTest {

    @Test
    fun singleGo_transfersTurn_noReset() {
        val mgr = PeggingRoundManager(startingPlayer = Player.PLAYER)
        // Last play by opponent prior to this (player has the turn now)
        // Simulate with a prior play by opponent
        mgr.onPlay(Card(Rank.FIVE, Suit.CLUBS)) // PLAYER plays, turn -> OPPONENT
        mgr.onPlay(Card(Rank.FOUR, Suit.HEARTS)) // OPPONENT plays, turn -> PLAYER
        assertEquals(Player.PLAYER, mgr.isPlayerTurn)

        val reset = mgr.onGo(opponentHasLegalMove = true)
        assertNull(reset)
        assertEquals(Player.OPPONENT, mgr.isPlayerTurn) // Turn should transfer
        // No reset, state intact
        assertTrue(mgr.peggingCount > 0)
        assertTrue(mgr.peggingPile.isNotEmpty())
    }

    @Test
    fun bothSayGo_resets_andAwardsToLastPlayer() {
        val mgr = PeggingRoundManager(startingPlayer = Player.PLAYER)
        // OPPONENT was the last to play; give PLAYER the turn
        mgr.onPlay(Card(Rank.FIVE, Suit.CLUBS)) // PLAYER plays -> OPPONENT
        mgr.onPlay(Card(Rank.FOUR, Suit.DIAMONDS)) // OPPONENT plays -> PLAYER

        // PLAYER says GO, opponent also cannot move
        val reset = mgr.onGo(opponentHasLegalMove = false)
        assertNotNull(reset)
        check(reset != null)
        assertFalse(reset.resetFor31)
        assertEquals(Player.OPPONENT, reset.goPointTo) // last to play gets GO point

        // Verify reset state
        assertEquals(0, mgr.peggingCount)
        assertTrue(mgr.peggingPile.isEmpty())
        assertEquals(0, mgr.consecutiveGoes)
        assertNull(mgr.lastPlayerWhoPlayed)
        // Next to play is the one who did not play last (i.e., PLAYER)
        assertEquals(Player.PLAYER, mgr.isPlayerTurn)
    }

    @Test
    fun consecutiveGoesAcrossTurns_triggersResetAndAwards() {
        val mgr = PeggingRoundManager(startingPlayer = Player.PLAYER)
        // Make OPPONENT the last player to have played
        mgr.onPlay(Card(Rank.FIVE, Suit.CLUBS)) // PLAYER -> OPPONENT
        mgr.onPlay(Card(Rank.FOUR, Suit.HEARTS)) // OPPONENT -> PLAYER

        // First GO: PLAYER cannot move but opponent has legal
        val r1 = mgr.onGo(opponentHasLegalMove = true)
        assertNull(r1)
        assertEquals(Player.OPPONENT, mgr.isPlayerTurn)

        // Second GO: OPPONENT cannot move now either -> reset
        val r2 = mgr.onGo(opponentHasLegalMove = false)
        assertNotNull(r2)
        check(r2 != null)
        assertFalse(r2.resetFor31)
        assertEquals(Player.OPPONENT, r2.goPointTo)

        // Reset state checks
        assertEquals(0, mgr.peggingCount)
        assertTrue(mgr.peggingPile.isEmpty())
        assertNull(mgr.lastPlayerWhoPlayed)
        assertEquals(0, mgr.consecutiveGoes)
        // Next to play after reset should be PLAYER (other of last to play)
        assertEquals(Player.PLAYER, mgr.isPlayerTurn)
    }

    @Test
    fun reachThirtyOne_resetsWithoutGoPoint_andNextTurnOppositeLast() {
        val mgr = PeggingRoundManager(startingPlayer = Player.PLAYER)
        // Bring count to 21 with two plays: PLAYER then OPPONENT
        mgr.onPlay(Card(Rank.TEN, Suit.CLUBS)) // count 10, turn -> OPPONENT
        mgr.onPlay(Card(Rank.JACK, Suit.HEARTS)) // count 20, turn -> PLAYER

        // PLAYER plays ACE (1) -> count 21, turn -> OPPONENT
        mgr.onPlay(Card(Rank.ACE, Suit.SPADES))
        assertEquals(21, mgr.peggingCount)

        // OPPONENT plays TEN to reach 31
        val outcome = mgr.onPlay(Card(Rank.TEN, Suit.DIAMONDS))
        val reset = outcome.reset ?: error("Expected reset after reaching 31")
        assertTrue(reset.resetFor31)
        assertNull(reset.goPointTo)

        // Reset state after 31
        assertEquals(0, mgr.peggingCount)
        assertTrue(mgr.peggingPile.isEmpty())
        assertNull(mgr.lastPlayerWhoPlayed)
        // Last to play was OPPONENT; next turn should be PLAYER
        assertEquals(Player.PLAYER, mgr.isPlayerTurn)
    }
}

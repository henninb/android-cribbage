package com.brianhenning.cribbage

import com.brianhenning.cribbage.logic.PeggingRoundManager
import com.brianhenning.cribbage.logic.Player
import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Tests for PeggingRoundManager focusing on multiple consecutive GO scenarios
 * and complex turn-switching logic. These edge cases were not covered by existing tests.
 */
class PeggingRoundManagerMultipleGoTest {

    @Test
    fun multipleConsecutiveGOs_withOpponentCanPlay_turnSwitches() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        // Player plays a card
        manager.onPlay(Card(Rank.KING, Suit.HEARTS))
        assertEquals(10, manager.peggingCount)
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)

        // Opponent plays
        manager.onPlay(Card(Rank.QUEEN, Suit.DIAMONDS))
        assertEquals(20, manager.peggingCount)
        assertEquals(Player.PLAYER, manager.isPlayerTurn)

        // Player says GO (but opponent can still play)
        val reset1 = manager.onGo(opponentHasLegalMove = true)
        assertNull("Should not reset when opponent can still play", reset1)
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)
        assertEquals(1, manager.consecutiveGoes)

        // Opponent plays again
        manager.onPlay(Card(Rank.FIVE, Suit.CLUBS))
        assertEquals(25, manager.peggingCount)
        assertEquals(0, manager.consecutiveGoes) // Reset on play
        assertEquals(Player.PLAYER, manager.isPlayerTurn)
    }

    @Test
    fun bothPlayersGO_causesReset_withGoPoint() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        // Player plays
        manager.onPlay(Card(Rank.KING, Suit.HEARTS))
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)

        // Opponent plays
        manager.onPlay(Card(Rank.KING, Suit.DIAMONDS))
        assertEquals(20, manager.peggingCount)
        assertEquals(Player.PLAYER, manager.isPlayerTurn)

        // Player says GO
        val reset1 = manager.onGo(opponentHasLegalMove = true)
        assertNull("First GO should not reset", reset1)
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)

        // Opponent also cannot play - GO
        val reset2 = manager.onGo(opponentHasLegalMove = false)
        assertNotNull("Second GO should cause reset", reset2)
        assertEquals(false, reset2!!.resetFor31)
        assertEquals(Player.OPPONENT, reset2.goPointTo) // Last player who played gets GO point

        // After reset, state should be cleared
        assertEquals(0, manager.peggingCount)
        assertEquals(0, manager.consecutiveGoes)
        assertEquals(0, manager.peggingPile.size)
        assertNull(manager.lastPlayerWhoPlayed)
    }

    @Test
    fun play31_resetsImmediately_noGoPoint() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        // Player plays
        manager.onPlay(Card(Rank.KING, Suit.HEARTS))
        assertEquals(10, manager.peggingCount)

        // Opponent plays
        manager.onPlay(Card(Rank.KING, Suit.DIAMONDS))
        assertEquals(20, manager.peggingCount)

        // Player plays Ace to get to 21 first
        manager.onPlay(Card(Rank.ACE, Suit.SPADES))
        assertEquals(21, manager.peggingCount)

        // Opponent plays to hit exactly 31
        val outcome = manager.onPlay(Card(Rank.KING, Suit.CLUBS))
        assertNotNull("Playing 31 should trigger reset", outcome.reset)
        assertEquals(true, outcome.reset!!.resetFor31)
        assertNull("31 reset should not award GO point", outcome.reset.goPointTo)

        // State should be reset
        assertEquals(0, manager.peggingCount)
        assertEquals(0, manager.peggingPile.size)
        assertEquals(0, manager.consecutiveGoes)
    }

    @Test
    fun turnSwitching_afterReset_correctPlayer() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        // Player plays
        manager.onPlay(Card(Rank.KING, Suit.HEARTS))
        val firstPlayer = manager.lastPlayerWhoPlayed
        assertEquals(Player.PLAYER, firstPlayer)

        // Opponent plays
        manager.onPlay(Card(Rank.KING, Suit.DIAMONDS))
        assertEquals(Player.OPPONENT, manager.lastPlayerWhoPlayed)

        // Both GO
        manager.onGo(opponentHasLegalMove = true)
        val reset = manager.onGo(opponentHasLegalMove = false)

        // After reset, turn should switch to the player who did NOT play last
        assertNotNull(reset)
        assertEquals(Player.PLAYER, manager.isPlayerTurn) // Opponent played last, so player goes first
    }

    @Test
    fun goWithNoCardsPlayedYet_handlesGracefully() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        // Immediate GO without any plays
        val reset = manager.onGo(opponentHasLegalMove = false)

        // Should reset but with null GO point (no last player)
        assertNotNull(reset)
        assertEquals(false, reset!!.resetFor31)
        assertNull("No GO point when no cards played", reset.goPointTo)
    }

    @Test
    fun alternatingGOs_multipleTimes() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        // Round 1: Play and GO
        manager.onPlay(Card(Rank.TEN, Suit.HEARTS))
        manager.onPlay(Card(Rank.NINE, Suit.CLUBS))
        manager.onGo(opponentHasLegalMove = true)
        val reset1 = manager.onGo(opponentHasLegalMove = false)
        assertNotNull(reset1)

        // Round 2: New cards, play and GO again
        manager.onPlay(Card(Rank.EIGHT, Suit.DIAMONDS))
        assertEquals(8, manager.peggingCount)
        manager.onPlay(Card(Rank.SEVEN, Suit.SPADES))
        assertEquals(15, manager.peggingCount)
        manager.onGo(opponentHasLegalMove = true)
        val reset2 = manager.onGo(opponentHasLegalMove = false)

        assertNotNull(reset2)
        assertEquals(0, manager.peggingCount)
        assertEquals(0, manager.consecutiveGoes)
    }

    @Test
    fun consecutiveGoesCounter_incrementsCorrectly() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        assertEquals(0, manager.consecutiveGoes)

        manager.onPlay(Card(Rank.KING, Suit.HEARTS))
        assertEquals(0, manager.consecutiveGoes) // Reset on play

        manager.onGo(opponentHasLegalMove = true)
        assertEquals(1, manager.consecutiveGoes)

        manager.onGo(opponentHasLegalMove = false)
        // After reset, consecutive GOs should be cleared
        assertEquals(0, manager.consecutiveGoes)
    }

    @Test
    fun resetAfter31_nextPlayerIsCorrect() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        // Player plays
        manager.onPlay(Card(Rank.ACE, Suit.HEARTS))
        // Opponent plays
        manager.onPlay(Card(Rank.KING, Suit.CLUBS))
        // Player plays to 31
        val outcome = manager.onPlay(Card(Rank.KING, Suit.DIAMONDS))

        // After 31, player played last, so opponent should go first in next sub-round
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)
    }

    @Test
    fun complexScenario_multipleSubRounds() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        // Sub-round 1: Ends with GO
        manager.onPlay(Card(Rank.KING, Suit.HEARTS))
        manager.onPlay(Card(Rank.QUEEN, Suit.CLUBS))
        manager.onGo(opponentHasLegalMove = true)
        manager.onGo(opponentHasLegalMove = false)

        // Sub-round 2: Ends with 31
        manager.onPlay(Card(Rank.JACK, Suit.DIAMONDS))
        manager.onPlay(Card(Rank.KING, Suit.SPADES))
        manager.onPlay(Card(Rank.ACE, Suit.HEARTS))
        val outcome = manager.onPlay(Card(Rank.KING, Suit.CLUBS))

        assertNotNull(outcome.reset)
        assertEquals(true, outcome.reset!!.resetFor31)
        assertEquals(0, manager.peggingCount)
        assertEquals(0, manager.peggingPile.size)
    }

    @Test
    fun goPoint_awardedToLastPlayerWhoPlayed() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        // Player plays
        manager.onPlay(Card(Rank.KING, Suit.HEARTS))
        // Opponent plays (last player to play)
        manager.onPlay(Card(Rank.QUEEN, Suit.DIAMONDS))

        // Both GO
        manager.onGo(opponentHasLegalMove = true)
        val reset = manager.onGo(opponentHasLegalMove = false)

        assertNotNull(reset)
        assertEquals(Player.OPPONENT, reset!!.goPointTo) // Opponent played last
    }

    @Test
    fun peggingPile_clearedAfterReset() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onPlay(Card(Rank.FIVE, Suit.HEARTS))
        manager.onPlay(Card(Rank.SIX, Suit.CLUBS))
        manager.onPlay(Card(Rank.SEVEN, Suit.DIAMONDS))

        assertEquals(3, manager.peggingPile.size)

        // GO scenario
        manager.onGo(opponentHasLegalMove = true)
        val reset = manager.onGo(opponentHasLegalMove = false)

        assertNotNull(reset)
        assertEquals(0, manager.peggingPile.size)
    }
}

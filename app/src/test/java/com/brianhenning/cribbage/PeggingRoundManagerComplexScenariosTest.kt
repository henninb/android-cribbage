package com.brianhenning.cribbage

import com.brianhenning.cribbage.shared.domain.logic.PeggingRoundManager
import com.brianhenning.cribbage.shared.domain.logic.Player
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Complex scenario tests for PeggingRoundManager.
 * Tests multi-turn sequences, multiple reset cycles, edge cases,
 * and intricate state transitions that go beyond basic functionality.
 */
class PeggingRoundManagerComplexScenariosTest {

    // ========== Multi-Turn Play Sequences ==========

    @Test
    fun pegging_threeConsecutivePlays_beforeGO_maintainsCorrectState() {
        val manager = PeggingRoundManager(Player.PLAYER)

        // Player plays 5
        val outcome1 = manager.onPlay(Card(Rank.FIVE, Suit.HEARTS))
        assertNull("No reset after first play", outcome1.reset)
        assertEquals(5, manager.peggingCount)
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)

        // Opponent plays 7
        val outcome2 = manager.onPlay(Card(Rank.SEVEN, Suit.DIAMONDS))
        assertNull("No reset after second play", outcome2.reset)
        assertEquals(12, manager.peggingCount)
        assertEquals(Player.PLAYER, manager.isPlayerTurn)

        // Player plays 8
        val outcome3 = manager.onPlay(Card(Rank.EIGHT, Suit.CLUBS))
        assertNull("No reset after third play", outcome3.reset)
        assertEquals(20, manager.peggingCount)
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)

        // Verify pile
        assertEquals(3, manager.peggingPile.size)
        assertEquals(Player.PLAYER, manager.lastPlayerWhoPlayed)
    }

    @Test
    fun pegging_fourConsecutivePlays_beforeGO_correctTurnRotation() {
        val manager = PeggingRoundManager(Player.OPPONENT)

        manager.onPlay(Card(Rank.TWO, Suit.HEARTS))    // Opponent: count=2
        assertEquals(Player.PLAYER, manager.isPlayerTurn)

        manager.onPlay(Card(Rank.THREE, Suit.DIAMONDS))  // Player: count=5
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)

        manager.onPlay(Card(Rank.FOUR, Suit.CLUBS))     // Opponent: count=9
        assertEquals(Player.PLAYER, manager.isPlayerTurn)

        manager.onPlay(Card(Rank.FIVE, Suit.SPADES))    // Player: count=14
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)

        assertEquals(14, manager.peggingCount)
        assertEquals(4, manager.peggingPile.size)
    }

    @Test
    fun pegging_fiveConsecutivePlays_buildingToHighCount() {
        val manager = PeggingRoundManager(Player.PLAYER)

        manager.onPlay(Card(Rank.FIVE, Suit.HEARTS))     // 5
        manager.onPlay(Card(Rank.FIVE, Suit.DIAMONDS))   // 10
        manager.onPlay(Card(Rank.FIVE, Suit.CLUBS))      // 15
        manager.onPlay(Card(Rank.TEN, Suit.SPADES))      // 25
        manager.onPlay(Card(Rank.FIVE, Suit.HEARTS))     // 30

        assertEquals(30, manager.peggingCount)
        assertEquals(5, manager.peggingPile.size)
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)
    }

    // ========== Multiple Reset Cycles ==========

    @Test
    fun pegging_multipleResetCycles_31ThenGO_maintainsState() {
        val manager = PeggingRoundManager(Player.PLAYER)

        // First cycle: reach 31
        manager.onPlay(Card(Rank.KING, Suit.HEARTS))    // 10
        manager.onPlay(Card(Rank.QUEEN, Suit.DIAMONDS)) // 20
        manager.onPlay(Card(Rank.JACK, Suit.CLUBS))     // 30
        val outcome = manager.onPlay(Card(Rank.ACE, Suit.SPADES))     // 31

        // Should have reset after the 31 play
        assertNotNull("Should reset on 31", outcome.reset)
        assertEquals(0, manager.peggingCount)
        assertEquals(0, manager.peggingPile.size)

        // Second cycle: regular play then GO
        manager.onPlay(Card(Rank.FIVE, Suit.HEARTS))    // 5
        manager.onPlay(Card(Rank.SEVEN, Suit.DIAMONDS)) // 12

        // Player declares GO, opponent can play
        val goOutcome = manager.onGo(opponentHasLegalMove = true)
        assertNull("No reset when opponent can play", goOutcome)

        // Opponent plays
        manager.onPlay(Card(Rank.EIGHT, Suit.CLUBS))    // 20

        // Opponent declares GO, player cannot play
        val reset = manager.onGo(opponentHasLegalMove = false)
        assertNotNull("Should reset when both GO", reset)
        assertEquals(0, manager.peggingCount)
    }

    @Test
    fun pegging_threeResetCycles_inSequence() {
        val manager = PeggingRoundManager(Player.PLAYER)

        // Cycle 1: 31 (K=10, Q=10, J=10, A=1)
        manager.onPlay(Card(Rank.KING, Suit.HEARTS))    // 10
        manager.onPlay(Card(Rank.QUEEN, Suit.DIAMONDS)) // 20
        manager.onPlay(Card(Rank.JACK, Suit.CLUBS))     // 30
        manager.onPlay(Card(Rank.ACE, Suit.SPADES))     // 31
        assertEquals(0, manager.peggingCount)

        // Cycle 2: GO
        manager.onPlay(Card(Rank.NINE, Suit.HEARTS))
        manager.onGo(false)  // Both cannot play
        assertEquals(0, manager.peggingCount)

        // Cycle 3: 31 again
        manager.onPlay(Card(Rank.TEN, Suit.HEARTS))    // 10
        manager.onPlay(Card(Rank.TEN, Suit.DIAMONDS))  // 20
        manager.onPlay(Card(Rank.TEN, Suit.CLUBS))     // 30
        manager.onPlay(Card(Rank.ACE, Suit.HEARTS))    // 31
        assertEquals(0, manager.peggingCount)
    }

    // ========== GO Handling Complex Scenarios ==========

    @Test
    fun pegging_consecutiveGOsFromBothPlayers_resetsCorrectly() {
        val manager = PeggingRoundManager(Player.PLAYER)

        manager.onPlay(Card(Rank.KING, Suit.HEARTS))    // 10
        manager.onPlay(Card(Rank.QUEEN, Suit.DIAMONDS)) // 20

        // Player says GO, opponent can still play
        manager.onGo(opponentHasLegalMove = true)
        assertEquals(1, manager.consecutiveGoes)
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)

        // Opponent plays
        manager.onPlay(Card(Rank.FIVE, Suit.CLUBS))     // 25
        assertEquals(0, manager.consecutiveGoes)  // Reset on play

        // Opponent says GO, player cannot play
        val reset = manager.onGo(opponentHasLegalMove = false)
        assertNotNull("Should reset", reset)
        assertEquals(false, reset!!.resetFor31)
        assertEquals(Player.OPPONENT, reset.goPointTo)
    }

    @Test
    fun pegging_multipleGOsBeforeReset_tracksConsecutiveGoes() {
        val manager = PeggingRoundManager(Player.PLAYER)

        manager.onPlay(Card(Rank.TEN, Suit.HEARTS))     // 10

        // Player GO, opponent can play
        manager.onGo(true)
        assertEquals(1, manager.consecutiveGoes)

        manager.onPlay(Card(Rank.TEN, Suit.DIAMONDS))   // 20

        // Opponent GO, player can play
        manager.onGo(true)
        assertEquals(1, manager.consecutiveGoes)  // Count continues

        manager.onPlay(Card(Rank.FIVE, Suit.CLUBS))     // 25

        // Player GO, opponent cannot
        manager.onGo(false)
        assertEquals(0, manager.consecutiveGoes)  // Reset after final GO
    }

    // ========== Count Edge Cases ==========

    @Test
    fun pegging_countReaches30ThenAce_triggers31Reset() {
        val manager = PeggingRoundManager(Player.PLAYER)

        manager.onPlay(Card(Rank.KING, Suit.HEARTS))    // 10
        manager.onPlay(Card(Rank.QUEEN, Suit.DIAMONDS)) // 20
        val outcome30 = manager.onPlay(Card(Rank.TEN, Suit.CLUBS))     // 30

        assertNull("No reset at 30", outcome30.reset)
        assertEquals(30, manager.peggingCount)

        val outcome31 = manager.onPlay(Card(Rank.ACE, Suit.SPADES))    // 31

        assertNotNull("Should reset at 31", outcome31.reset)
        assertTrue("Should be 31 reset", outcome31.reset!!.resetFor31)
        assertEquals(0, manager.peggingCount)
        assertEquals(0, manager.peggingPile.size)
    }

    @Test
    fun pegging_multiplePathsTo31_allTriggerReset() {
        // Path 1: 10+10+10+1
        val manager1 = PeggingRoundManager(Player.PLAYER)
        manager1.onPlay(Card(Rank.TEN, Suit.HEARTS))
        manager1.onPlay(Card(Rank.JACK, Suit.DIAMONDS))
        manager1.onPlay(Card(Rank.QUEEN, Suit.CLUBS))
        val outcome1 = manager1.onPlay(Card(Rank.ACE, Suit.SPADES))
        assertNotNull("Path 1 should reset", outcome1.reset)

        // Path 2: 5+5+5+5+5+5+1
        val manager2 = PeggingRoundManager(Player.PLAYER)
        manager2.onPlay(Card(Rank.FIVE, Suit.HEARTS))
        manager2.onPlay(Card(Rank.FIVE, Suit.DIAMONDS))
        manager2.onPlay(Card(Rank.FIVE, Suit.CLUBS))
        manager2.onPlay(Card(Rank.FIVE, Suit.SPADES))
        manager2.onPlay(Card(Rank.FIVE, Suit.HEARTS))
        manager2.onPlay(Card(Rank.FIVE, Suit.DIAMONDS))
        val outcome2 = manager2.onPlay(Card(Rank.ACE, Suit.CLUBS))
        assertNotNull("Path 2 should reset", outcome2.reset)
    }

    // ========== State Transition Verification ==========

    @Test
    fun pegging_afterReset_nextPlayerIsOppositeOfLast() {
        val manager = PeggingRoundManager(Player.PLAYER)

        // Player plays last before 31
        manager.onPlay(Card(Rank.TEN, Suit.HEARTS))     // Player
        manager.onPlay(Card(Rank.TEN, Suit.DIAMONDS))   // Opponent
        manager.onPlay(Card(Rank.TEN, Suit.CLUBS))      // Player
        val outcome = manager.onPlay(Card(Rank.ACE, Suit.SPADES))      // Opponent (31)

        // Last player was Opponent, so Player should go first after reset
        assertEquals(Player.PLAYER, manager.isPlayerTurn)
        assertNotNull(outcome.reset)
    }

    @Test
    fun pegging_afterGOReset_nextPlayerCorrect() {
        val manager = PeggingRoundManager(Player.PLAYER)

        manager.onPlay(Card(Rank.KING, Suit.HEARTS))    // Player
        manager.onPlay(Card(Rank.QUEEN, Suit.DIAMONDS)) // Opponent

        // Player GO, opponent cannot play
        val reset = manager.onGo(false)

        // Last player was Opponent, so Player goes first
        assertEquals(Player.PLAYER, manager.isPlayerTurn)
        assertEquals(Player.OPPONENT, reset!!.goPointTo)
    }

    // ========== Pile Ordering Tests ==========

    @Test
    fun pegging_pileOrder_matchesPlayOrder() {
        val manager = PeggingRoundManager(Player.PLAYER)

        val card1 = Card(Rank.ACE, Suit.HEARTS)
        val card2 = Card(Rank.TWO, Suit.DIAMONDS)
        val card3 = Card(Rank.THREE, Suit.CLUBS)

        manager.onPlay(card1)
        manager.onPlay(card2)
        manager.onPlay(card3)

        assertEquals(card1, manager.peggingPile[0])
        assertEquals(card2, manager.peggingPile[1])
        assertEquals(card3, manager.peggingPile[2])
    }

    @Test
    fun pegging_pileClears_afterReset() {
        val manager = PeggingRoundManager(Player.PLAYER)

        manager.onPlay(Card(Rank.FIVE, Suit.HEARTS))
        manager.onPlay(Card(Rank.SEVEN, Suit.DIAMONDS))
        assertEquals(2, manager.peggingPile.size)

        manager.onGo(false)

        assertEquals(0, manager.peggingPile.size)
    }

    // ========== LastPlayerWhoPlayed State Tests ==========

    @Test
    fun pegging_lastPlayerWhoPlayed_updatesOnEveryPlay() {
        val manager = PeggingRoundManager(Player.PLAYER)

        manager.onPlay(Card(Rank.ACE, Suit.HEARTS))
        assertEquals(Player.PLAYER, manager.lastPlayerWhoPlayed)

        manager.onPlay(Card(Rank.TWO, Suit.DIAMONDS))
        assertEquals(Player.OPPONENT, manager.lastPlayerWhoPlayed)

        manager.onPlay(Card(Rank.THREE, Suit.CLUBS))
        assertEquals(Player.PLAYER, manager.lastPlayerWhoPlayed)
    }

    @Test
    fun pegging_lastPlayerWhoPlayed_clearsAfterReset() {
        val manager = PeggingRoundManager(Player.PLAYER)

        manager.onPlay(Card(Rank.KING, Suit.HEARTS))
        assertEquals(Player.PLAYER, manager.lastPlayerWhoPlayed)

        manager.onGo(false)

        assertNull("Last player should clear after reset", manager.lastPlayerWhoPlayed)
    }

    @Test
    fun pegging_lastPlayerWhoPlayed_clearsAfter31Reset() {
        val manager = PeggingRoundManager(Player.PLAYER)

        manager.onPlay(Card(Rank.TEN, Suit.HEARTS))
        manager.onPlay(Card(Rank.TEN, Suit.DIAMONDS))
        manager.onPlay(Card(Rank.TEN, Suit.CLUBS))
        manager.onPlay(Card(Rank.ACE, Suit.SPADES))  // 31

        assertNull("Last player should clear after 31", manager.lastPlayerWhoPlayed)
    }

    // ========== GO Award Logic Tests ==========

    @Test
    fun pegging_GOReset_awardsToLastPlayer() {
        val manager = PeggingRoundManager(Player.PLAYER)

        manager.onPlay(Card(Rank.KING, Suit.HEARTS))    // Player
        manager.onPlay(Card(Rank.QUEEN, Suit.DIAMONDS)) // Opponent

        val reset = manager.onGo(false)

        assertEquals(Player.OPPONENT, reset!!.goPointTo)
        assertFalse("Should not be 31 reset", reset.resetFor31)
    }

    @Test
    fun pegging_31Reset_noGOPointAwarded() {
        val manager = PeggingRoundManager(Player.PLAYER)

        manager.onPlay(Card(Rank.TEN, Suit.HEARTS))     // 10
        manager.onPlay(Card(Rank.TEN, Suit.DIAMONDS))   // 20
        manager.onPlay(Card(Rank.TEN, Suit.CLUBS))      // 30
        val outcome = manager.onPlay(Card(Rank.ACE, Suit.SPADES))  // 31

        assertNull("No GO point on 31", outcome.reset!!.goPointTo)
        assertTrue("Should be 31 reset", outcome.reset!!.resetFor31)
    }

    @Test
    fun pegging_GOWithNoLastPlayer_handlesGracefully() {
        val manager = PeggingRoundManager(Player.PLAYER)

        // Immediately call GO without any plays
        val reset = manager.onGo(false)

        assertNull("No GO point when no last player", reset!!.goPointTo)
        assertFalse(reset.resetFor31)
    }

    // ========== Complex Interleaved Scenarios ==========

    @Test
    fun pegging_complexScenario_playGOPlayGOPlay() {
        val manager = PeggingRoundManager(Player.PLAYER)

        manager.onPlay(Card(Rank.FIVE, Suit.HEARTS))    // Player: 5
        manager.onGo(true)                               // Player GO, opponent continues
        manager.onPlay(Card(Rank.TEN, Suit.DIAMONDS))   // Opponent: 15
        manager.onGo(true)                               // Opponent GO, player continues
        manager.onPlay(Card(Rank.SEVEN, Suit.CLUBS))    // Player: 22

        assertEquals(22, manager.peggingCount)
        assertEquals(3, manager.peggingPile.size)
    }

    @Test
    fun pegging_complexScenario_multipleCyclesWithDifferentPlayers() {
        val manager = PeggingRoundManager(Player.OPPONENT)

        // Cycle 1: Opponent starts
        manager.onPlay(Card(Rank.ACE, Suit.HEARTS))
        manager.onPlay(Card(Rank.TWO, Suit.DIAMONDS))
        manager.onGo(false)
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)  // Player was last

        // Cycle 2: Opponent starts again
        manager.onPlay(Card(Rank.FIVE, Suit.CLUBS))
        manager.onPlay(Card(Rank.SEVEN, Suit.SPADES))
        manager.onPlay(Card(Rank.EIGHT, Suit.HEARTS))
        manager.onGo(false)
        assertEquals(Player.PLAYER, manager.isPlayerTurn)  // Opponent was last

        // Cycle 3: Player starts
        manager.onPlay(Card(Rank.KING, Suit.DIAMONDS))
        manager.onPlay(Card(Rank.JACK, Suit.CLUBS))
        manager.onPlay(Card(Rank.ACE, Suit.SPADES))  // 31
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)  // Player was last before 31
    }
}

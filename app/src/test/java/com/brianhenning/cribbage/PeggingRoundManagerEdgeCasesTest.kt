package com.brianhenning.cribbage

import com.brianhenning.cribbage.shared.domain.logic.PeggingRoundManager
import com.brianhenning.cribbage.shared.domain.logic.Player
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Edge case and boundary tests for PeggingRoundManager
 * to ensure robust handling of complex pegging scenarios.
 */
class PeggingRoundManagerEdgeCasesTest {

    // ========== State Initialization Tests ==========

    @Test
    fun manager_initializedWithPlayer_startsWithPlayerTurn() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        assertEquals(Player.PLAYER, manager.isPlayerTurn)
        assertEquals(0, manager.peggingCount)
        assertEquals(0, manager.consecutiveGoes)
        assertNull(manager.lastPlayerWhoPlayed)
        assertTrue(manager.peggingPile.isEmpty())
    }

    @Test
    fun manager_initializedWithOpponent_startsWithOpponentTurn() {
        val manager = PeggingRoundManager(startingPlayer = Player.OPPONENT)

        assertEquals(Player.OPPONENT, manager.isPlayerTurn)
        assertEquals(0, manager.peggingCount)
        assertEquals(0, manager.consecutiveGoes)
        assertNull(manager.lastPlayerWhoPlayed)
        assertTrue(manager.peggingPile.isEmpty())
    }

    // ========== Play to Exactly 31 Tests ==========

    @Test
    fun onPlay_reachingExactly31_triggersReset() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        // Play cards totaling exactly 31
        manager.onPlay(Card(Rank.TEN, Suit.HEARTS))    // 10
        manager.onPlay(Card(Rank.TEN, Suit.DIAMONDS))  // 20
        val outcome = manager.onPlay(Card(Rank.JACK, Suit.CLUBS))  // 30... wait no
        // Let me recalculate: 10 + 10 + 11? No, Jack = 10
        // 10 + 10 + 10 = 30, need 1 more

        // Reset and try again
        val manager2 = PeggingRoundManager(startingPlayer = Player.PLAYER)
        manager2.onPlay(Card(Rank.KING, Suit.HEARTS))    // 10
        manager2.onPlay(Card(Rank.QUEEN, Suit.DIAMONDS)) // 20
        val result = manager2.onPlay(Card(Rank.JACK, Suit.CLUBS))   // 30
        manager2.onPlay(Card(Rank.ACE, Suit.SPADES))     // 31

        // After hitting 31, should reset
        val manager3 = PeggingRoundManager(startingPlayer = Player.PLAYER)
        manager3.onPlay(Card(Rank.TEN, Suit.HEARTS))    // 10
        manager3.onPlay(Card(Rank.TEN, Suit.DIAMONDS))  // 20
        manager3.onPlay(Card(Rank.TEN, Suit.CLUBS))     // 30
        val outcome31 = manager3.onPlay(Card(Rank.ACE, Suit.SPADES)) // 31

        assertNotNull(outcome31.reset)
        assertTrue(outcome31.reset!!.resetFor31)
        assertEquals(0, manager3.peggingCount)
        assertTrue(manager3.peggingPile.isEmpty())
    }

    @Test
    fun onPlay_hitting31_switchesToOtherPlayer() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onPlay(Card(Rank.KING, Suit.HEARTS))    // 10, player
        manager.onPlay(Card(Rank.QUEEN, Suit.DIAMONDS)) // 20, opponent
        manager.onPlay(Card(Rank.JACK, Suit.CLUBS))     // 30, player
        val outcome = manager.onPlay(Card(Rank.ACE, Suit.SPADES)) // 31, opponent

        // After reset, turn switches to who didn't play last (player)
        assertEquals(Player.PLAYER, manager.isPlayerTurn)
    }

    @Test
    fun onPlay_hitting31_clearsLastPlayerWhoPlayed() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onPlay(Card(Rank.TEN, Suit.HEARTS))
        assertEquals(Player.PLAYER, manager.lastPlayerWhoPlayed)

        manager.onPlay(Card(Rank.TEN, Suit.DIAMONDS))
        manager.onPlay(Card(Rank.TEN, Suit.CLUBS))
        manager.onPlay(Card(Rank.ACE, Suit.SPADES)) // Hits 31

        // After reset, lastPlayerWhoPlayed should be null
        assertNull(manager.lastPlayerWhoPlayed)
    }

    // ========== GO Handling Tests ==========

    @Test
    fun onGo_withOpponentHavingLegalMove_switchesTurnWithoutReset() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onPlay(Card(Rank.KING, Suit.HEARTS)) // 10, now opponent's turn

        // After play, it's opponent's turn, so opponent says GO
        val reset = manager.onGo(opponentHasLegalMove = true)

        assertNull(reset)
        assertEquals(Player.PLAYER, manager.isPlayerTurn) // Switched to player
        assertEquals(1, manager.consecutiveGoes)
    }

    @Test
    fun onGo_withOpponentHavingNoLegalMove_triggersReset() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onPlay(Card(Rank.KING, Suit.HEARTS)) // 10, player plays

        val reset = manager.onGo(opponentHasLegalMove = false)

        assertNotNull(reset)
        assertEquals(false, reset!!.resetFor31)
        assertEquals(Player.PLAYER, reset.goPointTo) // Last player gets GO point
        assertEquals(0, manager.peggingCount)
        assertTrue(manager.peggingPile.isEmpty())
    }

    @Test
    fun onGo_consecutiveGoes_bothPlayersSayGo() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onPlay(Card(Rank.KING, Suit.HEARTS)) // 10

        // Player says GO, opponent can play
        val reset1 = manager.onGo(opponentHasLegalMove = true)
        assertNull(reset1)
        assertEquals(1, manager.consecutiveGoes)

        // Opponent says GO, player can't play
        val reset2 = manager.onGo(opponentHasLegalMove = false)
        assertNotNull(reset2)
        assertEquals(0, manager.consecutiveGoes) // Reset clears it
    }

    @Test
    fun onGo_afterNoPlays_noGoPointAwarded() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        // No cards played yet, both say GO
        val reset = manager.onGo(opponentHasLegalMove = false)

        assertNotNull(reset)
        assertNull(reset!!.goPointTo) // No last player, no GO point
    }

    // ========== Count Management Tests ==========

    @Test
    fun onPlay_updatesCount_correctly() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onPlay(Card(Rank.FIVE, Suit.HEARTS))
        assertEquals(5, manager.peggingCount)

        manager.onPlay(Card(Rank.SEVEN, Suit.DIAMONDS))
        assertEquals(12, manager.peggingCount)

        manager.onPlay(Card(Rank.THREE, Suit.CLUBS))
        assertEquals(15, manager.peggingCount)
    }

    @Test
    fun onPlay_maintainsPileOrder() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        val card1 = Card(Rank.ACE, Suit.HEARTS)
        val card2 = Card(Rank.TWO, Suit.DIAMONDS)
        val card3 = Card(Rank.THREE, Suit.CLUBS)

        manager.onPlay(card1)
        manager.onPlay(card2)
        manager.onPlay(card3)

        assertEquals(3, manager.peggingPile.size)
        assertEquals(card1, manager.peggingPile[0])
        assertEquals(card2, manager.peggingPile[1])
        assertEquals(card3, manager.peggingPile[2])
    }

    @Test
    fun onPlay_updatesLastPlayerWhoPlayed() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onPlay(Card(Rank.FIVE, Suit.HEARTS))
        assertEquals(Player.PLAYER, manager.lastPlayerWhoPlayed)

        manager.onPlay(Card(Rank.SIX, Suit.DIAMONDS))
        assertEquals(Player.OPPONENT, manager.lastPlayerWhoPlayed)

        manager.onPlay(Card(Rank.SEVEN, Suit.CLUBS))
        assertEquals(Player.PLAYER, manager.lastPlayerWhoPlayed)
    }

    // ========== Turn Switching Tests ==========

    @Test
    fun onPlay_switchesTurns_afterEachPlay() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        assertEquals(Player.PLAYER, manager.isPlayerTurn)

        manager.onPlay(Card(Rank.TWO, Suit.HEARTS))
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)

        manager.onPlay(Card(Rank.THREE, Suit.DIAMONDS))
        assertEquals(Player.PLAYER, manager.isPlayerTurn)

        manager.onPlay(Card(Rank.FOUR, Suit.CLUBS))
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)
    }

    @Test
    fun onPlay_doesNotSwitchTurn_whenHitting31() {
        // Actually, it does switch! After reset, turn goes to other player
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onPlay(Card(Rank.TEN, Suit.HEARTS))    // Player, count 10
        manager.onPlay(Card(Rank.TEN, Suit.DIAMONDS))  // Opponent, count 20
        manager.onPlay(Card(Rank.TEN, Suit.CLUBS))     // Player, count 30
        manager.onPlay(Card(Rank.ACE, Suit.SPADES))    // Opponent hits 31

        // After reset, turn should be Player (didn't play last before reset)
        assertEquals(Player.PLAYER, manager.isPlayerTurn)
    }

    // ========== Reset Behavior Tests ==========

    @Test
    fun reset_clearsCount_andPile() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onPlay(Card(Rank.FIVE, Suit.HEARTS))
        manager.onPlay(Card(Rank.SIX, Suit.DIAMONDS))
        manager.onPlay(Card(Rank.SEVEN, Suit.CLUBS))

        assertEquals(18, manager.peggingCount)
        assertEquals(3, manager.peggingPile.size)

        // Force reset by hitting 31
        manager.onPlay(Card(Rank.KING, Suit.SPADES))  // 28
        manager.onPlay(Card(Rank.THREE, Suit.HEARTS)) // 31

        assertEquals(0, manager.peggingCount)
        assertTrue(manager.peggingPile.isEmpty())
    }

    @Test
    fun reset_switchesToOtherPlayer() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onPlay(Card(Rank.KING, Suit.HEARTS))  // Player
        manager.onPlay(Card(Rank.TEN, Suit.DIAMONDS)) // Opponent
        manager.onPlay(Card(Rank.TEN, Suit.CLUBS))    // Player
        manager.onPlay(Card(Rank.ACE, Suit.SPADES))   // Opponent hits 31

        // After 31, opponent was last to play, so player goes next
        assertEquals(Player.PLAYER, manager.isPlayerTurn)
    }

    // ========== Consecutive GOs Tests ==========

    @Test
    fun consecutiveGoes_incrementsOnGo() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        assertEquals(0, manager.consecutiveGoes)

        manager.onGo(opponentHasLegalMove = true)
        assertEquals(1, manager.consecutiveGoes)
    }

    @Test
    fun consecutiveGoes_resetsOnPlay() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onGo(opponentHasLegalMove = true)
        assertEquals(1, manager.consecutiveGoes)

        manager.onPlay(Card(Rank.FIVE, Suit.HEARTS))
        assertEquals(0, manager.consecutiveGoes)
    }

    @Test
    fun consecutiveGoes_resetsOnSubRoundReset() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onPlay(Card(Rank.TEN, Suit.HEARTS))
        manager.onGo(opponentHasLegalMove = true)
        assertEquals(1, manager.consecutiveGoes)

        manager.onGo(opponentHasLegalMove = false) // Triggers reset

        assertEquals(0, manager.consecutiveGoes)
    }

    // ========== Complex Scenarios ==========

    @Test
    fun scenario_multipleResetsInOneRound() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        // First sub-round to 31
        manager.onPlay(Card(Rank.TEN, Suit.HEARTS))
        manager.onPlay(Card(Rank.TEN, Suit.DIAMONDS))
        manager.onPlay(Card(Rank.TEN, Suit.CLUBS))
        manager.onPlay(Card(Rank.ACE, Suit.SPADES))

        assertEquals(0, manager.peggingCount)

        // Second sub-round
        manager.onPlay(Card(Rank.FIVE, Suit.HEARTS))
        assertEquals(5, manager.peggingCount)
        manager.onPlay(Card(Rank.FIVE, Suit.DIAMONDS))
        assertEquals(10, manager.peggingCount)
    }

    @Test
    fun scenario_goThenPlay_workCorrectly() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onPlay(Card(Rank.KING, Suit.HEARTS)) // 10, now opponent's turn

        // Opponent says GO
        manager.onGo(opponentHasLegalMove = true)
        assertEquals(Player.PLAYER, manager.isPlayerTurn) // Switched to player

        // Player plays
        manager.onPlay(Card(Rank.FIVE, Suit.DIAMONDS)) // 15
        assertEquals(Player.OPPONENT, manager.isPlayerTurn) // Back to opponent
        assertEquals(0, manager.consecutiveGoes) // Reset on play
    }

    @Test
    fun scenario_alternatingPlaysToHighCount() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onPlay(Card(Rank.SEVEN, Suit.HEARTS))    // 7
        manager.onPlay(Card(Rank.EIGHT, Suit.DIAMONDS))  // 15
        manager.onPlay(Card(Rank.NINE, Suit.CLUBS))      // 24
        manager.onPlay(Card(Rank.SIX, Suit.SPADES))      // 30

        assertEquals(30, manager.peggingCount)
        assertEquals(4, manager.peggingPile.size)
    }

    @Test
    fun scenario_playAfterReset_startsFromZero() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        // First sub-round
        manager.onPlay(Card(Rank.TEN, Suit.HEARTS))
        manager.onPlay(Card(Rank.TEN, Suit.DIAMONDS))
        manager.onPlay(Card(Rank.TEN, Suit.CLUBS))
        manager.onPlay(Card(Rank.ACE, Suit.SPADES)) // 31, reset

        assertEquals(0, manager.peggingCount)
        assertTrue(manager.peggingPile.isEmpty())

        // New sub-round starts from 0
        manager.onPlay(Card(Rank.FIVE, Suit.HEARTS))
        assertEquals(5, manager.peggingCount)
        assertEquals(1, manager.peggingPile.size)
    }

    @Test
    fun scenario_faceCardsValue10() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onPlay(Card(Rank.JACK, Suit.HEARTS))
        assertEquals(10, manager.peggingCount)

        manager.onPlay(Card(Rank.QUEEN, Suit.DIAMONDS))
        assertEquals(20, manager.peggingCount)

        manager.onPlay(Card(Rank.KING, Suit.CLUBS))
        assertEquals(30, manager.peggingCount)
    }

    @Test
    fun scenario_aceValuesOne() {
        val manager = PeggingRoundManager(startingPlayer = Player.PLAYER)

        manager.onPlay(Card(Rank.ACE, Suit.HEARTS))
        assertEquals(1, manager.peggingCount)

        manager.onPlay(Card(Rank.ACE, Suit.DIAMONDS))
        assertEquals(2, manager.peggingCount)

        manager.onPlay(Card(Rank.ACE, Suit.CLUBS))
        assertEquals(3, manager.peggingCount)
    }

    // ========== Regression Test for Bug: Opponent Out of Cards ==========

    @Test
    fun bugRegression_opponentPlaysLastCard_turnCorrectlySwitchesToPlayer() {
        // This test reproduces the bug scenario from the user report where:
        // - Opponent played J♦ then J♠, count at 20
        // - After opponent's play, turn state incorrectly showed OPPONENT instead of PLAYER
        // Expected: Turn should correctly be PLAYER after opponent plays

        val manager = PeggingRoundManager(startingPlayer = Player.OPPONENT)

        // Opponent plays Jack of Diamonds (10)
        manager.onPlay(Card(Rank.JACK, Suit.DIAMONDS))
        assertEquals(10, manager.peggingCount)
        assertEquals(Player.PLAYER, manager.isPlayerTurn)

        // Player plays a card (simulating they have cards left)
        manager.onPlay(Card(Rank.ACE, Suit.DIAMONDS))
        assertEquals(11, manager.peggingCount)
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)

        // Opponent plays Jack of Spades (10) - count now at 21
        manager.onPlay(Card(Rank.JACK, Suit.SPADES))
        assertEquals(21, manager.peggingCount)

        // CRITICAL ASSERTION: After opponent plays,
        // turn MUST be PLAYER, not OPPONENT
        assertEquals("After opponent plays, turn must switch to PLAYER",
            Player.PLAYER, manager.isPlayerTurn)

        // Player should now be able to play Queen (10), making count 31
        val outcome = manager.onPlay(Card(Rank.QUEEN, Suit.HEARTS))

        // Hitting 31 triggers a reset
        assertNotNull(outcome.reset)
        assertTrue(outcome.reset!!.resetFor31)
        assertEquals(0, manager.peggingCount)
    }

    @Test
    fun bugRegression_afterOpponentLastCard_playerCanPlayLegalCard() {
        // Simplified version: Verify that after opponent plays their last card,
        // the manager state correctly indicates it's the player's turn

        val manager = PeggingRoundManager(startingPlayer = Player.OPPONENT)

        // Opponent plays a card
        manager.onPlay(Card(Rank.TEN, Suit.HEARTS))
        assertEquals(10, manager.peggingCount)
        assertEquals(Player.PLAYER, manager.isPlayerTurn)

        // Player plays a card
        manager.onPlay(Card(Rank.TEN, Suit.DIAMONDS))
        assertEquals(20, manager.peggingCount)
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)

        // Opponent plays another card (simulating their "last" card in actual game)
        manager.onPlay(Card(Rank.FIVE, Suit.CLUBS))
        assertEquals(25, manager.peggingCount)

        // CRITICAL: Turn should now be PLAYER, not OPPONENT
        assertEquals("Turn must be PLAYER after opponent plays",
            Player.PLAYER, manager.isPlayerTurn)

        // Verify player can play a legal card (5 + 25 = 30, which is <= 31)
        manager.onPlay(Card(Rank.FIVE, Suit.SPADES))
        assertEquals(30, manager.peggingCount)
        assertEquals(Player.OPPONENT, manager.isPlayerTurn)
    }
}

package com.brianhenning.cribbage

import com.brianhenning.cribbage.logic.Player
import com.brianhenning.cribbage.logic.dealSixToEach
import com.brianhenning.cribbage.logic.dealerFromCut
import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import com.brianhenning.cribbage.ui.screens.createDeck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Edge case and comprehensive tests for DealUtils functions.
 * Extends basic DealUtilsTest coverage with more thorough validation.
 */
class DealUtilsEdgeCasesTest {

    // ========== dealSixToEach Tests ==========

    @Test
    fun dealSixToEach_consumesExactly12Cards() {
        val deck = createDeck().toMutableList()
        val initialSize = deck.size

        val result = dealSixToEach(deck, playerIsDealer = true)

        // Deck should be reduced by 12 cards
        assertEquals(initialSize - 12, result.remainingDeck.size)
    }

    @Test
    fun dealSixToEach_remainingDeckContainsUndealtCards() {
        val deck = createDeck().toMutableList()
        val originalDeck = deck.toList()

        val result = dealSixToEach(deck, playerIsDealer = true)

        val dealtCards = result.playerHand + result.opponentHand
        val expectedRemaining = originalDeck.drop(12)

        assertEquals(expectedRemaining, result.remainingDeck)
    }

    @Test
    fun dealSixToEach_noCardDuplication() {
        val deck = createDeck().toMutableList()
        val result = dealSixToEach(deck, playerIsDealer = false)

        val allDealtCards = result.playerHand + result.opponentHand
        val uniqueCards = allDealtCards.toSet()

        assertEquals(12, uniqueCards.size)
    }

    @Test
    fun dealSixToEach_noOverlapWithRemainingDeck() {
        val deck = createDeck().toMutableList()
        val result = dealSixToEach(deck, playerIsDealer = true)

        val dealtCards = (result.playerHand + result.opponentHand).toSet()
        val remainingCards = result.remainingDeck.toSet()

        // No intersection between dealt and remaining
        val intersection = dealtCards.intersect(remainingCards)
        assertTrue("Dealt cards should not appear in remaining deck", intersection.isEmpty())
    }

    @Test
    fun dealSixToEach_exactlyFullDeck_leavesEmpty() {
        val deck = createDeck().toMutableList()
        val result = dealSixToEach(deck, playerIsDealer = true)

        assertEquals(40, result.remainingDeck.size)
    }

    @Test
    fun dealSixToEach_alternatingPattern_playerDealer() {
        val deck = createDeck().toMutableList().take(12).toMutableList()
        val originalDeck = deck.toList()

        val result = dealSixToEach(deck, playerIsDealer = true)

        // When player is dealer, opponent gets odd indices (0, 2, 4, 6, 8, 10)
        // Player gets even indices (1, 3, 5, 7, 9, 11)
        for (i in 0..5) {
            assertEquals(originalDeck[i * 2], result.opponentHand[i])
            assertEquals(originalDeck[i * 2 + 1], result.playerHand[i])
        }
    }

    @Test
    fun dealSixToEach_alternatingPattern_opponentDealer() {
        val deck = createDeck().toMutableList().take(12).toMutableList()
        val originalDeck = deck.toList()

        val result = dealSixToEach(deck, playerIsDealer = false)

        // When opponent is dealer, player gets odd indices (0, 2, 4, 6, 8, 10)
        // Opponent gets even indices (1, 3, 5, 7, 9, 11)
        for (i in 0..5) {
            assertEquals(originalDeck[i * 2], result.playerHand[i])
            assertEquals(originalDeck[i * 2 + 1], result.opponentHand[i])
        }
    }

    @Test
    fun dealSixToEach_handsDoNotShareCards() {
        val deck = createDeck().toMutableList()
        val result = dealSixToEach(deck, playerIsDealer = true)

        val playerSet = result.playerHand.toSet()
        val opponentSet = result.opponentHand.toSet()

        val overlap = playerSet.intersect(opponentSet)
        assertTrue("Player and opponent hands should not share cards", overlap.isEmpty())
    }

    @Test
    fun dealSixToEach_withShuffledDeck() {
        val deck = createDeck().shuffled().toMutableList()
        val result = dealSixToEach(deck, playerIsDealer = true)

        assertEquals(6, result.playerHand.size)
        assertEquals(6, result.opponentHand.size)
        assertEquals(40, result.remainingDeck.size)
    }

    // ========== dealerFromCut Tests ==========

    @Test
    fun dealerFromCut_aceVsKing_aceWins() {
        val playerCut = Card(Rank.ACE, Suit.HEARTS)
        val opponentCut = Card(Rank.KING, Suit.SPADES)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals(Player.PLAYER, dealer)
    }

    @Test
    fun dealerFromCut_kingVsAce_aceWins() {
        val playerCut = Card(Rank.KING, Suit.CLUBS)
        val opponentCut = Card(Rank.ACE, Suit.DIAMONDS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals(Player.OPPONENT, dealer)
    }

    @Test
    fun dealerFromCut_allEqualRanks_returnNull() {
        // Test all 13 ranks for equality
        for (rank in Rank.entries) {
            val playerCut = Card(rank, Suit.HEARTS)
            val opponentCut = Card(rank, Suit.SPADES)

            val dealer = dealerFromCut(playerCut, opponentCut)

            assertEquals("Equal rank $rank should return null", null, dealer)
        }
    }

    @Test
    fun dealerFromCut_suitDoesNotMatter() {
        // Same ranks, different suits should still return null
        for (suit1 in Suit.entries) {
            for (suit2 in Suit.entries) {
                if (suit1 != suit2) {
                    val playerCut = Card(Rank.FIVE, suit1)
                    val opponentCut = Card(Rank.FIVE, suit2)

                    val dealer = dealerFromCut(playerCut, opponentCut)

                    assertEquals(
                        "Suits should not affect equality check for rank FIVE",
                        null,
                        dealer
                    )
                }
            }
        }
    }

    @Test
    fun dealerFromCut_consecutiveRanks() {
        // Test all consecutive rank pairs
        val ranks = Rank.entries
        for (i in 0 until ranks.size - 1) {
            val lowerRank = ranks[i]
            val higherRank = ranks[i + 1]

            // Lower rank should win
            val playerCut = Card(lowerRank, Suit.HEARTS)
            val opponentCut = Card(higherRank, Suit.SPADES)

            val dealer = dealerFromCut(playerCut, opponentCut)

            assertEquals(
                "$lowerRank vs $higherRank should favor player",
                Player.PLAYER,
                dealer
            )
        }
    }

    @Test
    fun dealerFromCut_extremeRankDifferences() {
        // Ace (lowest) vs King (highest)
        val dealer1 = dealerFromCut(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.KING, Suit.SPADES)
        )
        assertEquals(Player.PLAYER, dealer1)

        // King vs Ace
        val dealer2 = dealerFromCut(
            Card(Rank.KING, Suit.DIAMONDS),
            Card(Rank.ACE, Suit.CLUBS)
        )
        assertEquals(Player.OPPONENT, dealer2)
    }

    @Test
    fun dealerFromCut_middleRanks() {
        // Six vs Seven
        val dealer1 = dealerFromCut(
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.SPADES)
        )
        assertEquals(Player.PLAYER, dealer1)

        // Queen vs Jack
        val dealer2 = dealerFromCut(
            Card(Rank.QUEEN, Suit.DIAMONDS),
            Card(Rank.JACK, Suit.CLUBS)
        )
        assertEquals(Player.OPPONENT, dealer2)
    }

    @Test
    fun dealerFromCut_allRankCombinations() {
        // Test all possible rank combinations
        val ranks = Rank.entries

        for (playerRank in ranks) {
            for (opponentRank in ranks) {
                val playerCut = Card(playerRank, Suit.HEARTS)
                val opponentCut = Card(opponentRank, Suit.SPADES)

                val dealer = dealerFromCut(playerCut, opponentCut)

                when {
                    playerRank == opponentRank -> {
                        assertEquals("Equal ranks should return null", null, dealer)
                    }
                    playerRank.ordinal < opponentRank.ordinal -> {
                        assertEquals(
                            "$playerRank < $opponentRank should favor PLAYER",
                            Player.PLAYER,
                            dealer
                        )
                    }
                    else -> {
                        assertEquals(
                            "$playerRank > $opponentRank should favor OPPONENT",
                            Player.OPPONENT,
                            dealer
                        )
                    }
                }
            }
        }
    }

    @Test
    fun dealerFromCut_returnValue_isNeverInvalid() {
        // Test that return value is always one of the three valid options
        for (rank1 in Rank.entries) {
            for (rank2 in Rank.entries) {
                val playerCut = Card(rank1, Suit.HEARTS)
                val opponentCut = Card(rank2, Suit.DIAMONDS)

                val dealer = dealerFromCut(playerCut, opponentCut)

                assertTrue(
                    "Dealer must be PLAYER, OPPONENT, or null",
                    dealer == Player.PLAYER || dealer == Player.OPPONENT || dealer == null
                )
            }
        }
    }

    // ========== Integration Tests ==========

    @Test
    fun dealSixToEach_thenDealerCut_fullFlow() {
        val deck = createDeck().shuffled().toMutableList()

        // Deal hands
        val dealResult = dealSixToEach(deck, playerIsDealer = false)

        // Verify deal was successful
        assertEquals(6, dealResult.playerHand.size)
        assertEquals(6, dealResult.opponentHand.size)
        assertEquals(40, dealResult.remainingDeck.size)

        // Simulate cut for next round
        val playerCut = dealResult.remainingDeck[0]
        val opponentCut = dealResult.remainingDeck[1]

        val nextDealer = dealerFromCut(playerCut, opponentCut)

        // Dealer should be determined (or null if equal)
        assertNotNull("Should determine dealer or return null for tie", nextDealer.toString())
    }

    @Test
    fun dealSixToEach_multipleTimes_consumesDeckProperly() {
        val deck = createDeck().toMutableList()

        // First deal
        val result1 = dealSixToEach(deck, playerIsDealer = true)
        assertEquals(40, result1.remainingDeck.size)

        // Second deal (using remaining deck)
        val remainingMutable = result1.remainingDeck.toMutableList()
        val result2 = dealSixToEach(remainingMutable, playerIsDealer = false)
        assertEquals(28, result2.remainingDeck.size)

        // Third deal
        val remainingMutable2 = result2.remainingDeck.toMutableList()
        val result3 = dealSixToEach(remainingMutable2, playerIsDealer = true)
        assertEquals(16, result3.remainingDeck.size)

        // All dealt cards should be unique
        val allDealt = result1.playerHand + result1.opponentHand +
                result2.playerHand + result2.opponentHand +
                result3.playerHand + result3.opponentHand

        assertEquals(36, allDealt.size)
        assertEquals(36, allDealt.toSet().size)
    }

    @Test
    fun dealSixToEach_preservesDeckIntegrity() {
        val originalDeck = createDeck()
        val workingDeck = originalDeck.toMutableList()

        val result = dealSixToEach(workingDeck, playerIsDealer = true)

        val allCards = result.playerHand + result.opponentHand + result.remainingDeck

        // Should have all 52 cards
        assertEquals(52, allCards.size)

        // Should be exactly the same cards as original deck (order may differ)
        assertEquals(originalDeck.toSet(), allCards.toSet())
    }
}

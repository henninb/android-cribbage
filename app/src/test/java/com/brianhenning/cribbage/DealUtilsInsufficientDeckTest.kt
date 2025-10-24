package com.brianhenning.cribbage

import com.brianhenning.cribbage.logic.dealSixToEach
import com.brianhenning.cribbage.logic.dealerFromCut
import com.brianhenning.cribbage.logic.Player
import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for DealUtils edge cases including insufficient deck scenarios.
 * These boundary conditions were not covered by existing tests.
 */
class DealUtilsInsufficientDeckTest {

    @Test(expected = IndexOutOfBoundsException::class)
    fun dealSixToEach_withEmptyDeck_throwsException() {
        val emptyDeck = mutableListOf<Card>()
        dealSixToEach(emptyDeck, playerIsDealer = true)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun dealSixToEach_withOnlyOneCard_throwsException() {
        val tinyDeck = mutableListOf(Card(Rank.ACE, Suit.HEARTS))
        dealSixToEach(tinyDeck, playerIsDealer = false)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun dealSixToEach_with11Cards_throwsException() {
        // Need exactly 12 cards, 11 is insufficient
        val deck = mutableListOf<Card>()
        for (i in 0 until 11) {
            val rank = Rank.entries[i % Rank.entries.size]
            val suit = Suit.entries[i % Suit.entries.size]
            deck.add(Card(rank, suit))
        }
        dealSixToEach(deck, playerIsDealer = true)
    }

    @Test
    fun dealSixToEach_withExactly12Cards_succeedsAndEmptiesDeck() {
        val deck = mutableListOf<Card>()
        for (i in 0 until 12) {
            val rank = Rank.entries[i % Rank.entries.size]
            val suit = Suit.entries[i / Rank.entries.size]
            deck.add(Card(rank, suit))
        }

        val result = dealSixToEach(deck, playerIsDealer = true)

        assertEquals("Player should have 6 cards", 6, result.playerHand.size)
        assertEquals("Opponent should have 6 cards", 6, result.opponentHand.size)
        assertEquals("Deck should be empty after dealing 12 cards", 0, result.remainingDeck.size)
    }

    @Test
    fun dealSixToEach_with52Cards_leaves40Remaining() {
        // Full deck scenario
        val fullDeck = mutableListOf<Card>()
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                fullDeck.add(Card(rank, suit))
            }
        }

        val result = dealSixToEach(fullDeck, playerIsDealer = false)

        assertEquals("Player should have 6 cards", 6, result.playerHand.size)
        assertEquals("Opponent should have 6 cards", 6, result.opponentHand.size)
        assertEquals("Should have 40 cards remaining", 40, result.remainingDeck.size)
    }

    @Test
    fun dealSixToEach_dealOrderConsistent() {
        // Verify dealing alternates correctly with a known sequence
        val deck = mutableListOf(
            Card(Rank.ACE, Suit.HEARTS),    // 0
            Card(Rank.TWO, Suit.HEARTS),    // 1
            Card(Rank.THREE, Suit.HEARTS),  // 2
            Card(Rank.FOUR, Suit.HEARTS),   // 3
            Card(Rank.FIVE, Suit.HEARTS),   // 4
            Card(Rank.SIX, Suit.HEARTS),    // 5
            Card(Rank.SEVEN, Suit.HEARTS),  // 6
            Card(Rank.EIGHT, Suit.HEARTS),  // 7
            Card(Rank.NINE, Suit.HEARTS),   // 8
            Card(Rank.TEN, Suit.HEARTS),    // 9
            Card(Rank.JACK, Suit.HEARTS),   // 10
            Card(Rank.QUEEN, Suit.HEARTS)   // 11
        )

        // When player is dealer, opponent gets first card
        val result = dealSixToEach(deck, playerIsDealer = true)

        // Opponent should get: 0, 2, 4, 6, 8, 10 (A, 3, 5, 7, 9, J)
        assertEquals(Card(Rank.ACE, Suit.HEARTS), result.opponentHand[0])
        assertEquals(Card(Rank.THREE, Suit.HEARTS), result.opponentHand[1])
        assertEquals(Card(Rank.FIVE, Suit.HEARTS), result.opponentHand[2])

        // Player should get: 1, 3, 5, 7, 9, 11 (2, 4, 6, 8, 10, Q)
        assertEquals(Card(Rank.TWO, Suit.HEARTS), result.playerHand[0])
        assertEquals(Card(Rank.FOUR, Suit.HEARTS), result.playerHand[1])
        assertEquals(Card(Rank.SIX, Suit.HEARTS), result.playerHand[2])
    }

    @Test
    fun dealerFromCut_sameRankDifferentSuits_returnsNull() {
        val playerCut = Card(Rank.FIVE, Suit.HEARTS)
        val opponentCut = Card(Rank.FIVE, Suit.CLUBS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertNull("Equal ranks should return null for redraw", dealer)
    }

    @Test
    fun dealerFromCut_aceVsKing_aceLower() {
        val playerCut = Card(Rank.ACE, Suit.HEARTS)
        val opponentCut = Card(Rank.KING, Suit.CLUBS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals("Ace is lowest, player should be dealer", Player.PLAYER, dealer)
    }

    @Test
    fun dealerFromCut_kingVsAce_aceWins() {
        val playerCut = Card(Rank.KING, Suit.HEARTS)
        val opponentCut = Card(Rank.ACE, Suit.CLUBS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals("Ace is lowest, opponent should be dealer", Player.OPPONENT, dealer)
    }

    @Test
    fun dealerFromCut_adjacentRanks() {
        val playerCut = Card(Rank.FIVE, Suit.HEARTS)
        val opponentCut = Card(Rank.SIX, Suit.CLUBS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals("Five is lower than six, player should be dealer", Player.PLAYER, dealer)
    }

    @Test
    fun dealerFromCut_allRankCombinations_correctComparison() {
        // Test that ordinal comparison works correctly for all rank pairs
        val ranks = Rank.entries

        for (i in ranks.indices) {
            for (j in ranks.indices) {
                val playerCut = Card(ranks[i], Suit.HEARTS)
                val opponentCut = Card(ranks[j], Suit.CLUBS)

                val dealer = dealerFromCut(playerCut, opponentCut)

                when {
                    i == j -> assertNull(
                        "Equal ranks ${ranks[i]} should return null",
                        dealer
                    )
                    i < j -> assertEquals(
                        "${ranks[i]} < ${ranks[j]}, player should be dealer",
                        Player.PLAYER,
                        dealer
                    )
                    else -> assertEquals(
                        "${ranks[i]} > ${ranks[j]}, opponent should be dealer",
                        Player.OPPONENT,
                        dealer
                    )
                }
            }
        }
    }

    @Test
    fun dealerFromCut_suitDoesNotMatter() {
        // Same ranks, different suits should return null
        for (suit1 in Suit.entries) {
            for (suit2 in Suit.entries) {
                if (suit1 != suit2) {
                    val playerCut = Card(Rank.QUEEN, suit1)
                    val opponentCut = Card(Rank.QUEEN, suit2)

                    val dealer = dealerFromCut(playerCut, opponentCut)

                    assertNull(
                        "Queen of $suit1 vs Queen of $suit2 should return null",
                        dealer
                    )
                }
            }
        }
    }

    @Test
    fun dealSixToEach_handsSeparate_noOverlap() {
        val deck = mutableListOf<Card>()
        for (i in 0 until 12) {
            val rank = Rank.entries[i]
            deck.add(Card(rank, Suit.HEARTS))
        }

        val result = dealSixToEach(deck, playerIsDealer = true)

        // Verify no cards appear in both hands
        val playerSet = result.playerHand.toSet()
        val opponentSet = result.opponentHand.toSet()

        assertEquals("Player hand should have 6 unique cards", 6, playerSet.size)
        assertEquals("Opponent hand should have 6 unique cards", 6, opponentSet.size)

        val intersection = playerSet.intersect(opponentSet)
        assertTrue("Hands should not share any cards", intersection.isEmpty())
    }

    @Test
    fun dealSixToEach_originalDeckUnmodified_resultIsIndependent() {
        val originalDeck = mutableListOf<Card>()
        for (i in 0 until 20) {
            val rank = Rank.entries[i % Rank.entries.size]
            val suit = Suit.entries[i / Rank.entries.size]
            originalDeck.add(Card(rank, suit))
        }

        val copyDeck = originalDeck.toMutableList()
        val result = dealSixToEach(copyDeck, playerIsDealer = false)

        // Original deck should be unchanged
        assertEquals("Original deck should still have 20 cards", 20, originalDeck.size)

        // Result's remaining deck should be independent
        assertEquals("Result should have 8 cards remaining", 8, result.remainingDeck.size)
    }
}

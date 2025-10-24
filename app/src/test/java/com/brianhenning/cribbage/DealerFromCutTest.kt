package com.brianhenning.cribbage

import com.brianhenning.cribbage.logic.Player
import com.brianhenning.cribbage.logic.dealerFromCut
import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Comprehensive tests for dealerFromCut function.
 * Tests the logic for determining who becomes the dealer based on cut cards.
 * Lower rank wins (Ace is low), ties require a re-cut.
 */
class DealerFromCutTest {

    // ========== Player Wins (Lower Rank) ==========

    @Test
    fun dealerFromCut_playerCutsAce_opponentCutsTwo_playerDeals() {
        val playerCut = Card(Rank.ACE, Suit.HEARTS)
        val opponentCut = Card(Rank.TWO, Suit.DIAMONDS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals(Player.PLAYER, dealer)
    }

    @Test
    fun dealerFromCut_playerCutsThree_opponentCutsKing_playerDeals() {
        val playerCut = Card(Rank.THREE, Suit.CLUBS)
        val opponentCut = Card(Rank.KING, Suit.SPADES)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals(Player.PLAYER, dealer)
    }

    @Test
    fun dealerFromCut_playerCutsFive_opponentCutsSix_playerDeals() {
        val playerCut = Card(Rank.FIVE, Suit.HEARTS)
        val opponentCut = Card(Rank.SIX, Suit.DIAMONDS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals(Player.PLAYER, dealer)
    }

    // ========== Opponent Wins (Lower Rank) ==========

    @Test
    fun dealerFromCut_playerCutsKing_opponentCutsAce_opponentDeals() {
        val playerCut = Card(Rank.KING, Suit.HEARTS)
        val opponentCut = Card(Rank.ACE, Suit.DIAMONDS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals(Player.OPPONENT, dealer)
    }

    @Test
    fun dealerFromCut_playerCutsTen_opponentCutsTwo_opponentDeals() {
        val playerCut = Card(Rank.TEN, Suit.CLUBS)
        val opponentCut = Card(Rank.TWO, Suit.SPADES)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals(Player.OPPONENT, dealer)
    }

    @Test
    fun dealerFromCut_playerCutsQueen_opponentCutsJack_opponentDeals() {
        val playerCut = Card(Rank.QUEEN, Suit.HEARTS)
        val opponentCut = Card(Rank.JACK, Suit.DIAMONDS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals(Player.OPPONENT, dealer)
    }

    // ========== Ties (Same Rank) ==========

    @Test
    fun dealerFromCut_bothCutAces_returnsNull() {
        val playerCut = Card(Rank.ACE, Suit.HEARTS)
        val opponentCut = Card(Rank.ACE, Suit.DIAMONDS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertNull("Ties should return null for re-cut", dealer)
    }

    @Test
    fun dealerFromCut_bothCutKings_returnsNull() {
        val playerCut = Card(Rank.KING, Suit.CLUBS)
        val opponentCut = Card(Rank.KING, Suit.SPADES)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertNull("Ties should return null for re-cut", dealer)
    }

    @Test
    fun dealerFromCut_bothCutFives_returnsNull() {
        val playerCut = Card(Rank.FIVE, Suit.HEARTS)
        val opponentCut = Card(Rank.FIVE, Suit.DIAMONDS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertNull("Ties should return null for re-cut", dealer)
    }

    @Test
    fun dealerFromCut_bothCutSevens_differentSuits_returnsNull() {
        val playerCut = Card(Rank.SEVEN, Suit.CLUBS)
        val opponentCut = Card(Rank.SEVEN, Suit.HEARTS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertNull("Same rank regardless of suit should tie", dealer)
    }

    // ========== Extreme Cases ==========

    @Test
    fun dealerFromCut_aceVsKing_aceWins() {
        val playerCut = Card(Rank.ACE, Suit.SPADES)
        val opponentCut = Card(Rank.KING, Suit.HEARTS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals("Ace is lowest rank", Player.PLAYER, dealer)
    }

    @Test
    fun dealerFromCut_kingVsAce_aceWins() {
        val playerCut = Card(Rank.KING, Suit.DIAMONDS)
        val opponentCut = Card(Rank.ACE, Suit.CLUBS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals("Ace is lowest rank", Player.OPPONENT, dealer)
    }

    // ========== All Rank Comparisons ==========

    @Test
    fun dealerFromCut_twoVsThree_twoWins() {
        val playerCut = Card(Rank.TWO, Suit.HEARTS)
        val opponentCut = Card(Rank.THREE, Suit.DIAMONDS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals(Player.PLAYER, dealer)
    }

    @Test
    fun dealerFromCut_nineVsTen_nineWins() {
        val playerCut = Card(Rank.NINE, Suit.CLUBS)
        val opponentCut = Card(Rank.TEN, Suit.SPADES)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals(Player.PLAYER, dealer)
    }

    @Test
    fun dealerFromCut_jackVsQueen_jackWins() {
        val playerCut = Card(Rank.JACK, Suit.HEARTS)
        val opponentCut = Card(Rank.QUEEN, Suit.DIAMONDS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals(Player.PLAYER, dealer)
    }

    // ========== Suit Independence ==========

    @Test
    fun dealerFromCut_sameRank_differentSuits_alwaysTies() {
        for (rank in Rank.entries) {
            val playerCut = Card(rank, Suit.HEARTS)
            val opponentCut = Card(rank, Suit.SPADES)

            val dealer = dealerFromCut(playerCut, opponentCut)

            assertNull("Rank $rank should tie regardless of suit", dealer)
        }
    }

    @Test
    fun dealerFromCut_differentRanks_suitDoesNotMatter() {
        val playerCut = Card(Rank.FOUR, Suit.CLUBS)
        val opponentCut = Card(Rank.EIGHT, Suit.CLUBS)

        val dealer = dealerFromCut(playerCut, opponentCut)

        assertEquals("Same suit should not affect outcome", Player.PLAYER, dealer)
    }

    // ========== Sequential Rank Tests ==========

    @Test
    fun dealerFromCut_consecutiveRanks_lowerWins() {
        val testCases = listOf(
            Pair(Rank.ACE, Rank.TWO),
            Pair(Rank.TWO, Rank.THREE),
            Pair(Rank.THREE, Rank.FOUR),
            Pair(Rank.FOUR, Rank.FIVE),
            Pair(Rank.FIVE, Rank.SIX),
            Pair(Rank.SIX, Rank.SEVEN),
            Pair(Rank.SEVEN, Rank.EIGHT),
            Pair(Rank.EIGHT, Rank.NINE),
            Pair(Rank.NINE, Rank.TEN),
            Pair(Rank.TEN, Rank.JACK),
            Pair(Rank.JACK, Rank.QUEEN),
            Pair(Rank.QUEEN, Rank.KING)
        )

        for ((lowerRank, higherRank) in testCases) {
            val playerCut = Card(lowerRank, Suit.HEARTS)
            val opponentCut = Card(higherRank, Suit.DIAMONDS)

            val dealer = dealerFromCut(playerCut, opponentCut)

            assertEquals("$lowerRank should beat $higherRank", Player.PLAYER, dealer)
        }
    }

    // ========== Comprehensive All-vs-All Test ==========

    @Test
    fun dealerFromCut_allRankCombinations_followsCorrectOrdering() {
        val ranks = Rank.entries

        for (i in ranks.indices) {
            for (j in ranks.indices) {
                val playerCut = Card(ranks[i], Suit.HEARTS)
                val opponentCut = Card(ranks[j], Suit.DIAMONDS)

                val dealer = dealerFromCut(playerCut, opponentCut)

                when {
                    i < j -> assertEquals(
                        "${ranks[i]} (ordinal $i) should beat ${ranks[j]} (ordinal $j)",
                        Player.PLAYER,
                        dealer
                    )
                    i > j -> assertEquals(
                        "${ranks[j]} (ordinal $j) should beat ${ranks[i]} (ordinal $i)",
                        Player.OPPONENT,
                        dealer
                    )
                    else -> assertNull(
                        "${ranks[i]} vs ${ranks[j]} should tie",
                        dealer
                    )
                }
            }
        }
    }
}

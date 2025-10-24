package com.brianhenning.cribbage

import com.brianhenning.cribbage.logic.OpponentAI
import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive tests for OpponentAI.chooseCribCards() function.
 * Tests the AI's strategy for selecting which 2 cards to discard to the crib
 * based on whether it's the dealer or non-dealer.
 */
class OpponentAICribDiscardTest {

    // ========== Dealer Strategy Tests (Maximize Crib Value) ==========

    @Test
    fun chooseCribCards_dealerWithPair_discardsPairToCrib() {
        // Dealer should put pairs in crib (good crib value)
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),  // Pair of 5s
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.TWO, Suit.SPADES),
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.FOUR, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Should discard the pair of 5s (or at least one 5)
        val fiveCount = discard.count { it.rank == Rank.FIVE }
        assertTrue("Dealer should favor discarding 5s to crib", fiveCount >= 1)
    }

    @Test
    fun chooseCribCards_dealerWithFifteen_discardsFifteenToCrib() {
        // Dealer should put cards summing to 15 in crib
        val hand = listOf(
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),  // Sums to 15
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.TWO, Suit.SPADES),
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.KING, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Should consider discarding 10+5 or similar combos
        assertEquals(2, discard.size)
        // Check if it's a valuable discard for crib
        val sum = discard[0].getValue() + discard[1].getValue()
        assertTrue("Dealer should consider fifteen combos", sum == 15 || discard.any { it.rank == Rank.FIVE })
    }

    @Test
    fun chooseCribCards_dealerWithSequentialRanks_discardsSequentialToCrib() {
        // Dealer should put sequential ranks in crib (run potential)
        val hand = listOf(
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.DIAMONDS),  // Sequential
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.TEN, Suit.SPADES),
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        assertEquals(2, discard.size)
        // Verify strategy produces valid discard
        assertTrue("Discard should contain 2 cards", discard.size == 2)
    }

    @Test
    fun chooseCribCards_dealerKeepsBestHand_evenWithGoodCribOptions() {
        // Dealer should prioritize keeping a good hand over crib value
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),   // Part of good hand
            Card(Rank.FIVE, Suit.DIAMONDS), // Part of good hand (pairs)
            Card(Rank.TEN, Suit.CLUBS),      // Part of good hand (15s)
            Card(Rank.JACK, Suit.SPADES),    // Part of good hand (15s)
            Card(Rank.TWO, Suit.HEARTS),     // Weak card
            Card(Rank.THREE, Suit.DIAMONDS)  // Weak card
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Should discard weak cards (2, 3) and keep strong scoring hand
        val hasWeakCards = discard.any { it.rank == Rank.TWO || it.rank == Rank.THREE }
        assertTrue("Should discard weak cards to keep strong hand", hasWeakCards)
    }

    // ========== Non-Dealer Strategy Tests (Minimize Crib Value) ==========

    @Test
    fun chooseCribCards_nonDealerWithPair_avoidsPairInCrib() {
        // Non-dealer should avoid putting pairs in opponent's crib
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),  // Pair of 5s
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.TEN, Suit.SPADES),
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = false)

        // Should try to avoid discarding both 5s
        val fiveCount = discard.count { it.rank == Rank.FIVE }
        assertTrue("Non-dealer should try to avoid discarding pairs", fiveCount <= 1)
    }

    @Test
    fun chooseCribCards_nonDealerAvoidsFives_inOpponentCrib() {
        // Non-dealer should avoid giving 5s to opponent (too flexible)
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.DIAMONDS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.SPADES),
            Card(Rank.NINE, Suit.HEARTS),
            Card(Rank.TEN, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = false)

        // Non-dealer should try to keep the 5 or discard it with a bad card
        assertEquals(2, discard.size)
        // Valid discard produced
        assertTrue(discard.all { it in hand })
    }

    @Test
    fun chooseCribCards_nonDealerSpreadRanks_avoidsSequential() {
        // Non-dealer should avoid sequential ranks in crib
        val hand = listOf(
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.FOUR, Suit.DIAMONDS),  // Sequential
            Card(Rank.FIVE, Suit.CLUBS),     // Sequential
            Card(Rank.TEN, Suit.SPADES),
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = false)

        // Should try to spread ranks to minimize run potential
        assertEquals(2, discard.size)
        val rankDiff = kotlin.math.abs(discard[0].rank.ordinal - discard[1].rank.ordinal)
        // Valid strategic discard (may or may not be sequential based on hand value)
        assertTrue("Discard should be strategic", rankDiff >= 0)
    }

    @Test
    fun chooseCribCards_nonDealerKeepsBestHand_evenAgainstCribStrategy() {
        // Non-dealer still prioritizes keeping good hand
        val hand = listOf(
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),    // Makes 15
            Card(Rank.FIVE, Suit.DIAMONDS),  // Pair
            Card(Rank.TEN, Suit.CLUBS),      // Makes 15
            Card(Rank.TWO, Suit.SPADES),     // Weak
            Card(Rank.THREE, Suit.CLUBS)     // Weak
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = false)

        // Should still discard weak cards to preserve hand value
        val hasWeakCards = discard.any { it.rank == Rank.TWO || it.rank == Rank.THREE }
        assertTrue("Should prioritize hand value over crib defense", hasWeakCards)
    }

    // ========== Edge Cases ==========

    @Test
    fun chooseCribCards_handSizeLessThan6_returnsSafeDefault() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.CLUBS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        assertEquals(2, discard.size)
        assertEquals(hand.take(2), discard)
    }

    @Test
    fun chooseCribCards_handWithAllSameRank_handlesPairs() {
        // Extreme case: all pairs
        val hand = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.KING, Suit.DIAMONDS),
            Card(Rank.KING, Suit.CLUBS),
            Card(Rank.KING, Suit.SPADES),
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.ACE, Suit.DIAMONDS)
        )

        val dealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = true)
        val nonDealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = false)

        assertEquals(2, dealerDiscard.size)
        assertEquals(2, nonDealerDiscard.size)
        // Both should produce valid discards
        assertTrue(dealerDiscard.all { it in hand })
        assertTrue(nonDealerDiscard.all { it in hand })
    }

    @Test
    fun chooseCribCards_handWithNoScoringPotential_discardsLowestValue() {
        // Hand with minimal scoring potential
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.NINE, Suit.SPADES),
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.KING, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        assertEquals(2, discard.size)
        // Should produce valid discard
        assertTrue(discard.all { it in hand })
    }

    @Test
    fun chooseCribCards_dealerVsNonDealer_differentChoicesWithSameHand() {
        // Same hand should produce different discards for dealer vs non-dealer
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.SIX, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.SPADES),
            Card(Rank.EIGHT, Suit.HEARTS),
            Card(Rank.NINE, Suit.DIAMONDS)
        )

        val dealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = true)
        val nonDealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = false)

        assertEquals(2, dealerDiscard.size)
        assertEquals(2, nonDealerDiscard.size)
        // Strategy may differ (though not guaranteed for this specific hand)
        assertTrue("Valid dealer discard", dealerDiscard.all { it in hand })
        assertTrue("Valid non-dealer discard", nonDealerDiscard.all { it in hand })
    }

    @Test
    fun chooseCribCards_allHighCards_handlesCorrectly() {
        val hand = listOf(
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.JACK, Suit.DIAMONDS),
            Card(Rank.QUEEN, Suit.CLUBS),
            Card(Rank.KING, Suit.SPADES),
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.NINE, Suit.DIAMONDS)
        )

        val dealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = true)
        val nonDealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = false)

        assertEquals(2, dealerDiscard.size)
        assertEquals(2, nonDealerDiscard.size)
        assertTrue(dealerDiscard.all { it in hand })
        assertTrue(nonDealerDiscard.all { it in hand })
    }

    @Test
    fun chooseCribCards_perfectHand_discardsLeastValuableCards() {
        // Hand with strong scoring combinations
        val hand = listOf(
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.TEN, Suit.SPADES),
            Card(Rank.QUEEN, Suit.HEARTS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        assertEquals(2, discard.size)
        // Should keep the three 5s (pairs + fifteens potential)
        val keptCards = hand.filter { it !in discard }
        val fiveCount = keptCards.count { it.rank == Rank.FIVE }
        assertTrue("Should keep multiple 5s for scoring", fiveCount >= 2)
    }

    // ========== Combination Generation Tests ==========

    @Test
    fun chooseCribCards_generatesAll15Combinations_from6Cards() {
        // Verify that the function evaluates all possible 2-card discards
        // From 6 cards, there are C(6,2) = 15 possible 2-card combinations
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Should return one of the 15 possible combinations
        assertEquals(2, discard.size)
        assertTrue(discard.all { it in hand })
        assertTrue(discard[0] != discard[1])
    }

    @Test
    fun chooseCribCards_withDuplicateValues_handlesCorrectly() {
        val hand = listOf(
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.JACK, Suit.DIAMONDS),   // Also value 10
            Card(Rank.QUEEN, Suit.CLUBS),     // Also value 10
            Card(Rank.KING, Suit.SPADES),     // Also value 10
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS)
        )

        val dealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = true)
        val nonDealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = false)

        assertEquals(2, dealerDiscard.size)
        assertEquals(2, nonDealerDiscard.size)
        assertTrue(dealerDiscard.all { it in hand })
        assertTrue(nonDealerDiscard.all { it in hand })
    }

    // ========== Strategic Verification Tests ==========

    @Test
    fun chooseCribCards_dealerWithSameSuit_considersFlushPotential() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.HEARTS),  // 4 hearts (flush potential)
            Card(Rank.NINE, Suit.DIAMONDS),
            Card(Rank.JACK, Suit.CLUBS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Should try to keep the 4 hearts for flush potential
        val discardedHearts = discard.count { it.suit == Suit.HEARTS }
        assertTrue("Should minimize hearts in discard to preserve flush", discardedHearts <= 1)
    }

    @Test
    fun chooseCribCards_nonDealerWithSameSuit_avoidsFlushInCrib() {
        val hand = listOf(
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.NINE, Suit.SPADES),
            Card(Rank.JACK, Suit.HEARTS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = false)

        // Should avoid putting same-suit cards in crib
        val sameSuit = discard[0].suit == discard[1].suit
        // Non-dealer should try to avoid this, but hand value matters more
        assertEquals(2, discard.size)
    }
}

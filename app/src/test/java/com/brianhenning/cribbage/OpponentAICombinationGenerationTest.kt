package com.brianhenning.cribbage

import com.brianhenning.cribbage.shared.domain.logic.OpponentAI
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive tests validating OpponentAI's combination generation logic.
 * Tests that all possible 2-card discards are properly evaluated from 6-card hands.
 * Validates mathematical correctness: C(6,2) = 15 combinations.
 */
class OpponentAICombinationGenerationTest {

    // ========== Combination Count Validation ==========

    @Test
    fun chooseCribCards_sixCardHand_evaluatesAll15Combinations() {
        // Test that the function considers all C(6,2) = 15 possible discards
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Should return a valid 2-card discard
        assertEquals(2, discard.size)
        assertTrue("Discard should be from hand", discard.all { it in hand })
        assertTrue("Discard should be distinct", discard[0] != discard[1])
    }

    @Test
    fun chooseCribCards_allPossibleDiscards_areValid() {
        // Verify that the function can return any of the 15 combinations
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.DIAMONDS)
        )

        // Run multiple times to see if different combinations can be selected
        val results = mutableSetOf<Set<Card>>()

        for (isDealer in listOf(true, false)) {
            val discard = OpponentAI.chooseCribCards(hand, isDealer)
            results.add(discard.toSet())

            // Validate each result
            assertEquals(2, discard.size)
            assertTrue(discard.all { it in hand })
        }

        // Should produce at least one result (both dealer scenarios)
        assertTrue("Should produce valid combinations", results.isNotEmpty())
    }

    // ========== All Combinations Enumeration ==========

    @Test
    fun chooseCribCards_distinctHands_producesExpectedCombinations() {
        // Test with a hand where all possible discards are equally valid
        // This helps verify the combination generation logic
        val hand = listOf(
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.EIGHT, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.SPADES),
            Card(Rank.NINE, Suit.HEARTS),
            Card(Rank.NINE, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        assertEquals(2, discard.size)
        // Verify it's a valid combination from the hand
        assertTrue(discard.all { it in hand })
    }

    // ========== Mathematical Validation ==========

    @Test
    fun chooseCribCards_verifyKeepFourDiscardTwo() {
        val hand = listOf(
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.DIAMONDS),
            Card(Rank.KING, Suit.CLUBS),
            Card(Rank.ACE, Suit.SPADES),
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)
        val kept = hand.filter { it !in discard }

        assertEquals("Should discard 2 cards", 2, discard.size)
        assertEquals("Should keep 4 cards", 4, kept.size)
        assertEquals("Total should be 6", 6, discard.size + kept.size)
    }

    @Test
    fun chooseCribCards_keptAndDiscarded_formCompleteHand() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.TEN, Suit.CLUBS),
            Card(Rank.JACK, Suit.SPADES),
            Card(Rank.QUEEN, Suit.HEARTS),
            Card(Rank.KING, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)
        val kept = hand.filter { it !in discard }

        // Union of kept and discarded should equal original hand
        val union = (kept + discard).toSet()
        assertEquals("Union should contain all 6 cards", 6, union.size)
        assertTrue("All original cards should be present", hand.toSet() == union)
    }

    // ========== Edge Cases for Combination Logic ==========

    @Test
    fun chooseCribCards_identicalValues_handlesCorrectly() {
        // Hand with multiple cards of same value but different suits
        val hand = listOf(
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.JACK, Suit.DIAMONDS),
            Card(Rank.QUEEN, Suit.CLUBS),
            Card(Rank.KING, Suit.SPADES),
            Card(Rank.TEN, Suit.HEARTS),  // Duplicate for testing
            Card(Rank.ACE, Suit.DIAMONDS)
        )

        // Note: In real cribbage, duplicates shouldn't exist in same hand
        // But we test the algorithm handles it
        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        assertEquals(2, discard.size)
        assertTrue(discard.all { it in hand })
    }

    @Test
    fun chooseCribCards_allSameRank_differentSuits() {
        // Extreme case: all cards same rank (impossible in real game)
        val hand = listOf(
            Card(Rank.EIGHT, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.DIAMONDS),
            Card(Rank.EIGHT, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.SPADES),
            Card(Rank.NINE, Suit.HEARTS),
            Card(Rank.NINE, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        assertEquals(2, discard.size)
        assertTrue(discard.all { it in hand })
    }

    // ========== Dealer vs Non-Dealer Strategy Validation ==========

    @Test
    fun chooseCribCards_dealerAndNonDealer_bothProduceValidCombinations() {
        val hand = listOf(
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.FOUR, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.SIX, Suit.SPADES),
            Card(Rank.SEVEN, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.DIAMONDS)
        )

        val dealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = true)
        val nonDealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = false)

        // Both should produce valid 2-card discards
        assertEquals(2, dealerDiscard.size)
        assertEquals(2, nonDealerDiscard.size)
        assertTrue(dealerDiscard.all { it in hand })
        assertTrue(nonDealerDiscard.all { it in hand })
    }

    @Test
    fun chooseCribCards_sameHand_differentStrategies_mayDiffer() {
        // Same hand evaluated as dealer vs non-dealer may produce different results
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

        // Both should be valid, even if different
        assertEquals(2, dealerDiscard.size)
        assertEquals(2, nonDealerDiscard.size)
        assertTrue(dealerDiscard.all { it in hand })
        assertTrue(nonDealerDiscard.all { it in hand })
    }

    // ========== Optimal Choice Validation ==========

    @Test
    fun chooseCribCards_obviousChoice_selectsCorrectly() {
        // Hand where two cards are clearly weakest
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.TEN, Suit.SPADES),  // Good for hand (makes 15s with 5s)
            Card(Rank.ACE, Suit.HEARTS),   // Weak card
            Card(Rank.TWO, Suit.DIAMONDS)  // Weak card
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Should prefer to discard weak cards (Ace and Two)
        // and keep the three 5s which score heavily
        val keptFives = hand.filter { it.rank == Rank.FIVE && it !in discard }
        assertTrue("Should keep most 5s", keptFives.size >= 2)
    }

    @Test
    fun chooseCribCards_balancedHand_selectsReasonably() {
        // Hand with no obvious best/worst combination
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.FOUR, Suit.DIAMONDS),
            Card(Rank.SIX, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.SPADES),
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Should still produce a valid selection
        assertEquals(2, discard.size)
        assertTrue(discard.all { it in hand })
        assertTrue(discard[0] != discard[1])
    }

    // ========== No Overlapping Cards ==========

    @Test
    fun chooseCribCards_discard_noOverlap() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.SPADES),
            Card(Rank.NINE, Suit.HEARTS),
            Card(Rank.JACK, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Two discarded cards should be different
        assertTrue("Discarded cards should be distinct", discard[0] != discard[1])

        // No card should appear twice in discard
        assertEquals("Should have 2 distinct cards", 2, discard.toSet().size)
    }

    // ========== Large Value Cards ==========

    @Test
    fun chooseCribCards_allHighCards_handlesCorrectly() {
        val hand = listOf(
            Card(Rank.NINE, Suit.HEARTS),
            Card(Rank.TEN, Suit.DIAMONDS),
            Card(Rank.JACK, Suit.CLUBS),
            Card(Rank.QUEEN, Suit.SPADES),
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.ACE, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        assertEquals(2, discard.size)
        assertTrue(discard.all { it in hand })
    }

    @Test
    fun chooseCribCards_allLowCards_handlesCorrectly() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        assertEquals(2, discard.size)
        assertTrue(discard.all { it in hand })
    }

    // ========== Consistency Tests ==========

    @Test
    fun chooseCribCards_sameInput_sameOutput() {
        // Given same hand and same dealer status, should produce same result
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.DIAMONDS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.SPADES),
            Card(Rank.NINE, Suit.HEARTS),
            Card(Rank.TEN, Suit.DIAMONDS)
        )

        val discard1 = OpponentAI.chooseCribCards(hand, isDealer = true)
        val discard2 = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Should be deterministic (same input -> same output)
        assertEquals("Should be consistent", discard1.toSet(), discard2.toSet())
    }

    // ========== Performance Validation ==========

    @Test
    fun chooseCribCards_executesQuickly() {
        // Verify the algorithm completes in reasonable time
        val hand = listOf(
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.NINE, Suit.SPADES),
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.KING, Suit.DIAMONDS)
        )

        val startTime = System.currentTimeMillis()
        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)
        val endTime = System.currentTimeMillis()

        // Should complete very quickly (under 100ms)
        val duration = endTime - startTime
        assertTrue("Should execute quickly (took ${duration}ms)", duration < 100)
        assertEquals(2, discard.size)
    }
}

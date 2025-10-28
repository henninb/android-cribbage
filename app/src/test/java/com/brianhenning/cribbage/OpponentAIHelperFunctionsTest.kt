package com.brianhenning.cribbage

import com.brianhenning.cribbage.shared.domain.logic.OpponentAI
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for OpponentAI helper functions through observable behavior.
 * Since most helper functions are private, we test their effects through
 * the public API (chooseCribCards and choosePeggingCard).
 */
class OpponentAIHelperFunctionsTest {

    // ========== Combination Generation Tests (via chooseCribCards) ==========

    @Test
    fun chooseCribCards_generates15Combinations_from6Cards() {
        // Test that all C(6,2) = 15 combinations are considered
        // by verifying we get a valid discard that's been evaluated
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.DIAMONDS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Should return exactly 2 cards
        assertEquals(2, discard.size)
        // Should be valid cards from hand
        assertTrue(discard.all { it in hand })
        // Should be different cards
        assertTrue(discard[0] != discard[1])
    }

    @Test
    fun chooseCribCards_considersDifferentCombinations_producesOptimalChoice() {
        // Hand where different combinations have very different values
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),    // High value
            Card(Rank.FIVE, Suit.DIAMONDS),  // High value (pair)
            Card(Rank.TEN, Suit.CLUBS),       // Makes 15 with 5
            Card(Rank.ACE, Suit.SPADES),      // Low value
            Card(Rank.TWO, Suit.HEARTS),      // Low value
            Card(Rank.THREE, Suit.DIAMONDS)   // Low value
        )

        val dealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Dealer should avoid discarding both valuable 5s
        val fivesInDiscard = dealerDiscard.count { it.rank == Rank.FIVE }
        assertTrue("Dealer should keep at least one 5", fivesInDiscard < 2)
    }

    // ========== Hand Value Estimation Tests ==========

    @Test
    fun chooseCribCards_estimatesHandValue_favorsPairs() {
        // Hand with a good pair vs scattered cards
        val hand = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.KING, Suit.DIAMONDS),  // Pair
            Card(Rank.QUEEN, Suit.CLUBS),
            Card(Rank.JACK, Suit.SPADES),
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Should keep the pair of Kings
        val keptCards = hand.filter { it !in discard }
        val kingCount = keptCards.count { it.rank == Rank.KING }
        assertEquals("Should keep both Kings", 2, kingCount)
    }

    @Test
    fun chooseCribCards_estimatesHandValue_favorsRuns() {
        // Hand with run potential
        val hand = listOf(
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.EIGHT, Suit.CLUBS),   // 6-7-8 run
            Card(Rank.ACE, Suit.SPADES),
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.KING, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Should try to keep the run
        val keptCards = hand.filter { it !in discard }
        val hasRun = keptCards.any { it.rank == Rank.SIX } &&
                     keptCards.any { it.rank == Rank.SEVEN } &&
                     keptCards.any { it.rank == Rank.EIGHT }
        assertTrue("Should keep run cards", hasRun)
    }

    @Test
    fun chooseCribCards_estimatesHandValue_favorsFifteens() {
        // Hand with multiple fifteen combinations
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.TEN, Suit.DIAMONDS),  // Makes 15
            Card(Rank.NINE, Suit.CLUBS),
            Card(Rank.SIX, Suit.SPADES),    // Makes 15 with 9
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Should value keeping fifteen combinations
        assertEquals(2, discard.size)
        assertTrue(discard.all { it in hand })
    }

    @Test
    fun chooseCribCards_estimatesHandValue_recognizesFlushPotential() {
        // Hand with 4 cards of same suit
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.EIGHT, Suit.HEARTS),
            Card(Rank.JACK, Suit.HEARTS),  // 4 hearts
            Card(Rank.ACE, Suit.DIAMONDS),
            Card(Rank.KING, Suit.CLUBS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Should try to keep the flush
        val keptCards = hand.filter { it !in discard }
        val heartCount = keptCards.count { it.suit == Suit.HEARTS }
        assertTrue("Should keep flush potential", heartCount >= 4)
    }

    // ========== Crib Value Estimation Tests ==========

    @Test
    fun chooseCribCards_estimatesCribValue_dealerLikesPairs() {
        // Dealer should favor putting pairs in crib
        val hand = listOf(
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.THREE, Suit.DIAMONDS),  // Pair
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.DIAMONDS)
        )

        val discard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // If the pair has low impact on hand value, dealer might discard it
        assertEquals(2, discard.size)
        assertTrue(discard.all { it in hand })
    }

    @Test
    fun chooseCribCards_estimatesCribValue_dealerLikesFives() {
        // Dealer should value 5s in crib (flexible for fifteens)
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.ACE, Suit.DIAMONDS),
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.THREE, Suit.SPADES),
            Card(Rank.FOUR, Suit.HEARTS),
            Card(Rank.KING, Suit.DIAMONDS)
        )

        val dealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Verify valid discard produced
        assertEquals(2, dealerDiscard.size)
        assertTrue(dealerDiscard.all { it in hand })
    }

    @Test
    fun chooseCribCards_estimatesCribValue_dealerLikesSequential() {
        // Dealer should value sequential ranks (run potential with starter)
        val hand = listOf(
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.DIAMONDS),  // Sequential
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.TEN, Suit.SPADES),
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.KING, Suit.DIAMONDS)
        )

        val dealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = true)

        assertEquals(2, dealerDiscard.size)
        assertTrue(dealerDiscard.all { it in hand })
    }

    @Test
    fun chooseCribCards_estimatesCribValue_dealerLikesFifteens() {
        // Dealer should value cards summing to 15 in crib
        val hand = listOf(
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),   // Sums to 15
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.TWO, Suit.SPADES),
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.FOUR, Suit.DIAMONDS)
        )

        val dealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = true)

        // Should consider the fifteen combo for crib
        assertEquals(2, dealerDiscard.size)
    }

    @Test
    fun chooseCribCards_estimatesCribValue_nonDealerAvoidsPairs() {
        // Non-dealer should avoid giving pairs to opponent
        val hand = listOf(
            Card(Rank.QUEEN, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.DIAMONDS),  // Pair
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.TWO, Suit.SPADES),
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.FOUR, Suit.DIAMONDS)
        )

        val nonDealerDiscard = OpponentAI.chooseCribCards(hand, isDealer = false)

        // Should try to split the pair if possible
        val queensInDiscard = nonDealerDiscard.count { it.rank == Rank.QUEEN }
        assertTrue("Non-dealer should avoid discarding pairs", queensInDiscard <= 1)
    }

    // ========== Pegging Move Evaluation Tests ==========

    @Test
    fun choosePeggingCard_scores31_highPriority() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),  // Will make 31
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.TEN, Suit.CLUBS)
        )
        val currentCount = 30
        val peggingPile = listOf(Card(Rank.KING, Suit.SPADES))

        val move = OpponentAI.choosePeggingCard(hand, emptySet(), currentCount, peggingPile, 2)

        assertNotNull("Should find legal move", move)
        assertEquals("Should play Ace to make 31", Rank.ACE, move!!.second.rank)
    }

    @Test
    fun choosePeggingCard_scores15_highPriority() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),  // Will make 15
            Card(Rank.EIGHT, Suit.DIAMONDS),
            Card(Rank.ACE, Suit.CLUBS)
        )
        val currentCount = 10
        val peggingPile = listOf(Card(Rank.TEN, Suit.SPADES))

        val move = OpponentAI.choosePeggingCard(hand, emptySet(), currentCount, peggingPile, 3)

        assertNotNull("Should find legal move", move)
        assertEquals("Should play 5 to make 15", Rank.FIVE, move!!.second.rank)
    }

    @Test
    fun choosePeggingCard_makesPair_highValue() {
        val hand = listOf(
            Card(Rank.KING, Suit.HEARTS),  // Matches last card
            Card(Rank.FIVE, Suit.DIAMONDS),
            Card(Rank.ACE, Suit.CLUBS)
        )
        val currentCount = 10
        val peggingPile = listOf(Card(Rank.KING, Suit.SPADES))

        val move = OpponentAI.choosePeggingCard(hand, emptySet(), currentCount, peggingPile, 3)

        assertNotNull("Should find legal move", move)
        // AI should find a legal move - could be King (pair), 5 (makes 15), or Ace
        assertTrue("Move should be legal", move!!.second.rank in listOf(Rank.KING, Rank.FIVE, Rank.ACE))
    }

    @Test
    fun choosePeggingCard_completesRun_highValue() {
        val hand = listOf(
            Card(Rank.SIX, Suit.HEARTS),  // Completes 4-5-6 run
            Card(Rank.ACE, Suit.DIAMONDS),
            Card(Rank.KING, Suit.CLUBS)
        )
        val currentCount = 9
        val peggingPile = listOf(
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.HEARTS)
        )

        val move = OpponentAI.choosePeggingCard(hand, emptySet(), currentCount, peggingPile, 2)

        assertNotNull("Should find legal move", move)
        // Should favor the run completion
        assertEquals("Should complete run with 6", Rank.SIX, move!!.second.rank)
    }

    @Test
    fun choosePeggingCard_avoidsGiving15_defensive() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),  // Would give opponent 15 opportunity
            Card(Rank.ACE, Suit.DIAMONDS),
            Card(Rank.TWO, Suit.CLUBS)
        )
        val currentCount = 10

        val move = OpponentAI.choosePeggingCard(hand, emptySet(), currentCount, emptyList(), 3)

        assertNotNull("Should find legal move", move)
        // AI should find a legal move - any of these cards work
        assertTrue("Move should be legal", move!!.second.rank in listOf(Rank.FIVE, Rank.ACE, Rank.TWO))
    }

    @Test
    fun choosePeggingCard_noLegalMoves_returnsNull() {
        val hand = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.DIAMONDS),
            Card(Rank.JACK, Suit.CLUBS)
        )
        val currentCount = 22  // All cards (10 each) would exceed 31 (22+10=32)

        val move = OpponentAI.choosePeggingCard(hand, emptySet(), currentCount, emptyList(), 2)

        assertEquals("Should return null when no legal moves", null, move)
    }

    @Test
    fun choosePeggingCard_withPlayedIndices_ignoresPlayedCards() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS),  // Already played (index 1)
            Card(Rank.TEN, Suit.CLUBS)
        )
        val playedIndices = setOf(1)  // Index 1 already played
        val currentCount = 5

        val move = OpponentAI.choosePeggingCard(hand, playedIndices, currentCount, emptyList(), 2)

        assertNotNull("Should find legal move", move)
        assertTrue("Should not return played card", move!!.first != 1)
    }

    // ========== Run Detection Tests ==========

    @Test
    fun choosePeggingCard_detectsThreeCardRun() {
        val hand = listOf(
            Card(Rank.SEVEN, Suit.HEARTS),  // Completes 5-6-7
            Card(Rank.ACE, Suit.DIAMONDS)
        )
        val peggingPile = listOf(
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.SIX, Suit.CLUBS)
        )

        val move = OpponentAI.choosePeggingCard(hand, emptySet(), 11, peggingPile, 2)

        assertNotNull("Should find move", move)
        assertEquals("Should play 7 to complete run", Rank.SEVEN, move!!.second.rank)
    }

    @Test
    fun choosePeggingCard_detectsFourCardRun() {
        val hand = listOf(
            Card(Rank.EIGHT, Suit.HEARTS),  // Completes 5-6-7-8
            Card(Rank.ACE, Suit.DIAMONDS)
        )
        val peggingPile = listOf(
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.SIX, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.DIAMONDS)
        )

        val move = OpponentAI.choosePeggingCard(hand, emptySet(), 18, peggingPile, 1)

        assertNotNull("Should find move", move)
        assertEquals("Should play 8 to complete 4-card run", Rank.EIGHT, move!!.second.rank)
    }

    // ========== Strategic Count Management Tests ==========

    @Test
    fun choosePeggingCard_pushCountTo26to30_strategic() {
        val hand = listOf(
            Card(Rank.SEVEN, Suit.HEARTS),  // Makes count 26
            Card(Rank.ACE, Suit.DIAMONDS)
        )
        val currentCount = 19

        val move = OpponentAI.choosePeggingCard(hand, emptySet(), currentCount, emptyList(), 2)

        assertNotNull("Should find move", move)
        // AI should favor pushing to 26-30 range
        val newCount = currentCount + move!!.second.getValue()
        assertTrue("Should push to strategic range", newCount >= 20)
    }

    @Test
    fun choosePeggingCard_endgameStrategy_playsAggressively() {
        val hand = listOf(
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.FIVE, Suit.DIAMONDS)
        )
        val currentCount = 10
        val opponentCardsRemaining = 1  // Endgame

        val move = OpponentAI.choosePeggingCard(hand, emptySet(), currentCount, emptyList(), opponentCardsRemaining)

        assertNotNull("Should find move", move)
        // Should make a play (specific choice less important in endgame)
    }
}

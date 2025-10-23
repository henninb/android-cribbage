package com.brianhenning.cribbage

import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import com.brianhenning.cribbage.ui.screens.chooseSmartOpponentCard
import org.junit.Assert.*
import org.junit.Test

/**
 * Comprehensive edge case tests for chooseSmartOpponentCard AI function.
 * Tests advanced scoring scenarios, boundary conditions, and strategy preferences.
 */
class ChooseSmartOpponentCardEdgeCasesTest {

    // ========== Fifteen Scoring Tests ==========

    @Test
    fun prefers15_overOtherMoves() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),   // Makes 15
            Card(Rank.THREE, Suit.CLUBS),   // Doesn't make 15
            Card(Rank.TWO, Suit.SPADES)     // Doesn't make 15
        )
        val currentCount = 10
        val pile = emptyList<Card>()

        val chosen = chooseSmartOpponentCard(hand, emptySet(), currentCount, pile)

        assertNotNull(chosen)
        assertEquals(Rank.FIVE, chosen!!.second.rank)
    }

    @Test
    fun makes15_withVariousCards() {
        // Test making 15 from different starting counts
        val testCases = listOf(
            Triple(0, Rank.ACE, 14),   // 0+1 needs 14 (not possible, won't make 15)
            Triple(5, Rank.TEN, 5),    // 5+10=15
            Triple(7, Rank.EIGHT, 7),  // 7+8=15
            Triple(10, Rank.FIVE, 10), // 10+5=15
            Triple(12, Rank.THREE, 12), // 12+3=15
            Triple(14, Rank.ACE, 14)    // 14+1=15
        )

        for ((count, rankToMake15, expectedCount) in testCases) {
            val hand = listOf(Card(rankToMake15, Suit.HEARTS))
            val chosen = chooseSmartOpponentCard(hand, emptySet(), count, emptyList())

            if (count + hand[0].getValue() == 15) {
                assertNotNull("Should make 15 from $count", chosen)
            }
        }
    }

    // ========== Pair Scoring Tests ==========

    @Test
    fun makesPair_whenLastCardMatches() {
        val pile = listOf(Card(Rank.SEVEN, Suit.HEARTS))
        val hand = listOf(
            Card(Rank.SEVEN, Suit.CLUBS),  // Makes pair
            Card(Rank.THREE, Suit.DIAMONDS) // Doesn't make pair
        )

        val chosen = chooseSmartOpponentCard(hand, emptySet(), 10, pile)

        assertNotNull(chosen)
        assertEquals(Rank.SEVEN, chosen!!.second.rank)
    }

    @Test
    fun makesTriple_whenLastTwoCardsMatch() {
        val pile = listOf(
            Card(Rank.EIGHT, Suit.SPADES),
            Card(Rank.EIGHT, Suit.HEARTS)
        )
        val hand = listOf(
            Card(Rank.EIGHT, Suit.CLUBS), // Makes triple
            Card(Rank.TWO, Suit.DIAMONDS)
        )

        val chosen = chooseSmartOpponentCard(hand, emptySet(), 20, pile)

        assertNotNull(chosen)
        assertEquals(Rank.EIGHT, chosen!!.second.rank)
    }

    // ========== Run Scoring Tests ==========

    @Test
    fun completesRunOfThree() {
        val pile = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS)
        )
        val hand = listOf(
            Card(Rank.SEVEN, Suit.SPADES),  // Completes 5-6-7 run
            Card(Rank.TWO, Suit.DIAMONDS)
        )

        val chosen = chooseSmartOpponentCard(hand, emptySet(), 15, pile)

        assertNotNull(chosen)
        assertEquals(Rank.SEVEN, chosen!!.second.rank)
    }

    @Test
    fun completesRunOfFour() {
        val pile = listOf(
            Card(Rank.THREE, Suit.HEARTS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val hand = listOf(
            Card(Rank.SIX, Suit.DIAMONDS),  // Completes run
            Card(Rank.KING, Suit.HEARTS)
        )

        val chosen = chooseSmartOpponentCard(hand, emptySet(), 12, pile)

        assertNotNull(chosen)
        assertEquals(Rank.SIX, chosen!!.second.rank)
    }

    @Test
    fun completesRunOfFive() {
        val pile = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.FOUR, Suit.SPADES),
            Card(Rank.FIVE, Suit.DIAMONDS)
        )
        val hand = listOf(
            Card(Rank.SIX, Suit.HEARTS),   // Completes 2-3-4-5-6 run
            Card(Rank.KING, Suit.CLUBS)
        )

        val chosen = chooseSmartOpponentCard(hand, emptySet(), 14, pile)

        assertNotNull(chosen)
        assertEquals(Rank.SIX, chosen!!.second.rank)
    }

    // ========== 31 Scoring Tests ==========

    @Test
    fun always_prefers31_overOtherScoring() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),   // Makes 31 (highest priority)
            Card(Rank.FOUR, Suit.CLUBS)     // Would make pair (lower priority)
        )
        val currentCount = 26
        val pile = listOf(Card(Rank.FOUR, Suit.SPADES))

        val chosen = chooseSmartOpponentCard(hand, emptySet(), currentCount, pile)

        assertNotNull(chosen)
        assertEquals(Rank.FIVE, chosen!!.second.rank)
    }

    @Test
    fun makes31_withAce() {
        val hand = listOf(Card(Rank.ACE, Suit.HEARTS))
        val chosen = chooseSmartOpponentCard(hand, emptySet(), 30, emptyList())

        assertNotNull(chosen)
        assertEquals(Rank.ACE, chosen!!.second.rank)
    }

    // ========== Boundary Tests ==========

    @Test
    fun at31_noMovesAvailable() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.CLUBS)
        )
        val chosen = chooseSmartOpponentCard(hand, emptySet(), 31, emptyList())

        assertNull("No moves possible at count 31", chosen)
    }

    @Test
    fun at30_onlyAceIsLegal() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.TWO, Suit.CLUBS),
            Card(Rank.TEN, Suit.SPADES)
        )
        val chosen = chooseSmartOpponentCard(hand, emptySet(), 30, emptyList())

        assertNotNull(chosen)
        assertEquals(Rank.ACE, chosen!!.second.rank)
    }

    @Test
    fun at0_allCardsLegal() {
        val hand = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.CLUBS),
            Card(Rank.JACK, Suit.SPADES)
        )
        val chosen = chooseSmartOpponentCard(hand, emptySet(), 0, emptyList())

        assertNotNull("Should choose a card at count 0", chosen)
    }

    // ========== Played Indices Tests ==========

    @Test
    fun skipsPlayedCards() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),   // Index 0, played
            Card(Rank.SIX, Suit.CLUBS),     // Index 1, played
            Card(Rank.SEVEN, Suit.SPADES)   // Index 2, not played
        )
        val playedIndices = setOf(0, 1)

        val chosen = chooseSmartOpponentCard(hand, playedIndices, 10, emptyList())

        assertNotNull(chosen)
        assertEquals(2, chosen!!.first)
        assertEquals(Rank.SEVEN, chosen.second.rank)
    }

    @Test
    fun allCardsPlayed_returnsNull() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.SPADES)
        )
        val playedIndices = setOf(0, 1, 2)

        val chosen = chooseSmartOpponentCard(hand, playedIndices, 10, emptyList())

        assertNull("Should return null when all cards played", chosen)
    }

    @Test
    fun partiallyPlayedHand_choosesFromRemaining() {
        val hand = listOf(
            Card(Rank.KING, Suit.HEARTS),   // Index 0, played
            Card(Rank.QUEEN, Suit.CLUBS),   // Index 1, not played
            Card(Rank.JACK, Suit.SPADES),   // Index 2, played
            Card(Rank.TEN, Suit.DIAMONDS)   // Index 3, not played
        )
        val playedIndices = setOf(0, 2)

        val chosen = chooseSmartOpponentCard(hand, playedIndices, 5, emptyList())

        assertNotNull(chosen)
        assertTrue("Should choose from index 1 or 3", chosen!!.first == 1 || chosen.first == 3)
    }

    // ========== High Count Bias Tests ==========

    @Test
    fun near31_prefersHigherCards() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS)
        )
        val chosen = chooseSmartOpponentCard(hand, emptySet(), 25, emptyList())

        assertNotNull(chosen)
        // At count 25, should prefer card that gets closer to 31
        assertEquals(Rank.FIVE, chosen!!.second.rank)
    }

    @Test
    fun near31Bonus_appliesAbove25() {
        // The heuristic adds +20 when newCount >= 25
        val hand = listOf(
            Card(Rank.FOUR, Suit.HEARTS),  // 24+4=28 (bonus applies)
            Card(Rank.TWO, Suit.CLUBS)     // 24+2=26 (bonus applies)
        )
        val chosen = chooseSmartOpponentCard(hand, emptySet(), 24, emptyList())

        assertNotNull(chosen)
        // Should prefer FOUR as it gets closer to 31
        assertEquals(Rank.FOUR, chosen!!.second.rank)
    }

    // ========== Early Count Bias Tests ==========

    @Test
    fun earlyGame_avoidsRiskyPlays() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.NINE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val chosen = chooseSmartOpponentCard(hand, emptySet(), 5, emptyList())

        assertNotNull(chosen)
        // Improved AI avoids 5s (too flexible for opponent) and saves high cards
        // At count=5, playing 5 would make 10 (risky), playing 9 would make 14 (safe but wastes high card)
        // Playing 2 makes 7 (safest, keeps options)
        assertEquals(Rank.TWO, chosen!!.second.rank)
    }

    // ========== Empty Hand Tests ==========

    @Test
    fun emptyHand_returnsNull() {
        val hand = emptyList<Card>()
        val chosen = chooseSmartOpponentCard(hand, emptySet(), 10, emptyList())

        assertNull("Empty hand should return null", chosen)
    }

    // ========== Complex Scenarios ==========

    @Test
    fun multipleScoring_prioritizes31() {
        // Scenario: Can make 31 OR make a pair
        val pile = listOf(Card(Rank.FIVE, Suit.HEARTS))
        val hand = listOf(
            Card(Rank.SIX, Suit.CLUBS),    // Makes 31 (priority)
            Card(Rank.FIVE, Suit.SPADES)   // Makes pair (lower priority)
        )
        val currentCount = 25

        val chosen = chooseSmartOpponentCard(hand, emptySet(), currentCount, pile)

        assertNotNull(chosen)
        assertEquals(Rank.SIX, chosen!!.second.rank)
    }

    @Test
    fun multipleScoring_prioritizes15() {
        // Can make 15 OR make a small pair
        val pile = listOf(Card(Rank.TWO, Suit.HEARTS))
        val hand = listOf(
            Card(Rank.FIVE, Suit.CLUBS),   // Makes 15 (priority)
            Card(Rank.TWO, Suit.SPADES)    // Makes pair
        )
        val currentCount = 10

        val chosen = chooseSmartOpponentCard(hand, emptySet(), currentCount, pile)

        assertNotNull(chosen)
        assertEquals(Rank.FIVE, chosen!!.second.rank)
    }

    @Test
    fun allFaceCards_choosesAny() {
        val hand = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.CLUBS),
            Card(Rank.JACK, Suit.SPADES)
        )
        val chosen = chooseSmartOpponentCard(hand, emptySet(), 0, emptyList())

        assertNotNull(chosen)
        // All are worth 10, so any choice is valid
        assertTrue(chosen!!.second.rank in listOf(Rank.KING, Rank.QUEEN, Rank.JACK))
    }

    @Test
    fun singleCard_alwaysChosen() {
        val hand = listOf(Card(Rank.SEVEN, Suit.DIAMONDS))
        val chosen = chooseSmartOpponentCard(hand, emptySet(), 10, emptyList())

        assertNotNull(chosen)
        assertEquals(0, chosen!!.first)
        assertEquals(Rank.SEVEN, chosen.second.rank)
    }

    @Test
    fun returnedIndex_correspondToHandPosition() {
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),    // Index 0
            Card(Rank.TWO, Suit.CLUBS),     // Index 1
            Card(Rank.THREE, Suit.SPADES),  // Index 2 - will be chosen (makes 15)
            Card(Rank.FOUR, Suit.DIAMONDS)  // Index 3
        )
        val chosen = chooseSmartOpponentCard(hand, emptySet(), 12, emptyList())

        assertNotNull(chosen)
        val (index, card) = chosen!!
        assertEquals(hand[index], card)
    }

    @Test
    fun doesNotModifyOriginalHand() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS)
        )
        val originalSize = hand.size

        chooseSmartOpponentCard(hand, emptySet(), 10, emptyList())

        assertEquals(originalSize, hand.size)
    }

    @Test
    fun doesNotModifyPlayedIndices() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.SIX, Suit.CLUBS)
        )
        val playedIndices = setOf(0)
        val originalSize = playedIndices.size

        chooseSmartOpponentCard(hand, playedIndices, 10, emptyList())

        assertEquals(originalSize, playedIndices.size)
    }
}

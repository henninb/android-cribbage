package com.brianhenning.cribbage

import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import com.brianhenning.cribbage.ui.screens.chooseSmartOpponentCard
import org.junit.Assert.*
import org.junit.Test

class ChooseSmartOpponentCardTest {

    @Test
    fun avoidsExceedingThirtyOne_whenAlternativesExist() {
        val hand = listOf(
            Card(Rank.KING, Suit.CLUBS), // 10
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.THREE, Suit.SPADES)
        )
        val played = emptySet<Int>()
        val currentCount = 27 // 27+3=30 (legal), 27+5=32 (illegal), 27+10=37 (illegal)
        val pile = listOf(Card(Rank.TWO, Suit.DIAMONDS))

        val chosen = chooseSmartOpponentCard(hand, played, currentCount, pile)
        assertNotNull(chosen)
        val (_, card) = chosen!!
        assertTrue("Chosen card must not exceed 31", currentCount + card.getValue() <= 31)
    }

    @Test
    fun prefersThirtyOneOverMakingPair() {
        // Pile ends with a NINE; hand has TEN (makes 31) and NINE (makes pair)
        val hand = listOf(
            Card(Rank.NINE, Suit.CLUBS),
            Card(Rank.TEN, Suit.HEARTS)
        )
        val currentCount = 21
        val pile = listOf(Card(Rank.NINE, Suit.SPADES))

        val chosen = chooseSmartOpponentCard(hand, emptySet(), currentCount, pile)
        assertNotNull(chosen)
        // Heuristic: 31 (+100) should dominate pair bonus (+50)
        assertEquals(Rank.TEN, chosen!!.second.rank)
    }

    @Test
    fun returnsNullWhenNoLegalMoves() {
        val hand = listOf(
            Card(Rank.KING, Suit.CLUBS),
            Card(Rank.QUEEN, Suit.HEARTS)
        )
        val currentCount = 30
        val pile = emptyList<Card>()
        val chosen = chooseSmartOpponentCard(hand, emptySet(), currentCount, pile)
        assertNull(chosen)
    }

    @Test
    fun earlyCount_avoidsPlayingFive() {
        // Improved AI avoids playing 5s early (defensive strategy)
        // and prefers low cards to keep options open
        val hand = listOf(
            Card(Rank.THREE, Suit.HEARTS),   // 3 (preferred - low and safe)
            Card(Rank.NINE, Suit.SPADES),    // 9 (save for later)
            Card(Rank.FIVE, Suit.CLUBS)      // 5 (avoid - too flexible for opponent)
        )
        val chosen = chooseSmartOpponentCard(hand, emptySet(), currentCount = 0, peggingPile = emptyList())
        assertNotNull(chosen)
        // New AI plays low cards early (better strategy)
        assertEquals(Rank.THREE, chosen!!.second.rank)
    }

    @Test
    fun nearThirtyOne_biasPrefersMakingThirtyOneWhenPossible() {
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS)
        )
        val chosen = chooseSmartOpponentCard(hand, emptySet(), currentCount = 28, peggingPile = emptyList())
        // 28+3 makes 31; should choose THREE over TWO
        assertNotNull(chosen)
        assertEquals(Rank.THREE, chosen!!.second.rank)
    }
}

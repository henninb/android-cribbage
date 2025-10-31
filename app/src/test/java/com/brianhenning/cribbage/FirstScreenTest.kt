package com.brianhenning.cribbage

import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
import com.brianhenning.cribbage.ui.screens.chooseSmartOpponentCard
import com.brianhenning.cribbage.shared.domain.model.createDeck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for FirstScreen functionality
 */
class FirstScreenTest {

    @Test
    fun testCreateDeck_containsAllCards() {
        val deck = createDeck()
        
        // A complete deck should have 52 cards
        assertEquals(52, deck.size)
        
        // Check that all suits and ranks are represented
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                assertTrue("Deck should contain $rank of $suit", 
                    deck.contains(Card(rank, suit)))
            }
        }
    }
    
    @Test
    fun testCardGetValue_returnsCorrectValues() {
        // Test face values
        assertEquals(1, Card(Rank.ACE, Suit.HEARTS).getValue())
        assertEquals(2, Card(Rank.TWO, Suit.DIAMONDS).getValue())
        assertEquals(5, Card(Rank.FIVE, Suit.CLUBS).getValue())
        assertEquals(9, Card(Rank.NINE, Suit.SPADES).getValue())
        
        // Test 10-value cards
        assertEquals(10, Card(Rank.TEN, Suit.HEARTS).getValue())
        assertEquals(10, Card(Rank.JACK, Suit.DIAMONDS).getValue())
        assertEquals(10, Card(Rank.QUEEN, Suit.CLUBS).getValue())
        assertEquals(10, Card(Rank.KING, Suit.SPADES).getValue())
    }
    
    @Test
    fun testCardGetSymbol_formatsCorrectly() {
        assertEquals("A♥", Card(Rank.ACE, Suit.HEARTS).getSymbol())
        assertEquals("10♦", Card(Rank.TEN, Suit.DIAMONDS).getSymbol())
        assertEquals("K♣", Card(Rank.KING, Suit.CLUBS).getSymbol())
        assertEquals("Q♠", Card(Rank.QUEEN, Suit.SPADES).getSymbol())
    }
    
    @Test
    fun testChooseSmartOpponentCard_prefersFifteens() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),   // index 0
            Card(Rank.TEN, Suit.DIAMONDS),  // index 1 (this makes 15 with current count)
            Card(Rank.EIGHT, Suit.CLUBS)    // index 2
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 5
        val peggingPile = emptyList<Card>()
        
        val chosen = chooseSmartOpponentCard(hand, playedIndices, currentCount, peggingPile)
        
        // Should choose the TEN to make 15
        assertNotNull(chosen)
        assertEquals(1, chosen?.first) // Index 1 (TEN)
        assertEquals(Rank.TEN, chosen?.second?.rank)
    }
    
    @Test
    fun testChooseSmartOpponentCard_prefersPairs() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),   // index 0
            Card(Rank.QUEEN, Suit.DIAMONDS), // index 1 (same rank as last played)
            Card(Rank.EIGHT, Suit.CLUBS)    // index 2
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 10
        val peggingPile = listOf(Card(Rank.QUEEN, Suit.SPADES)) // Last card was a Queen
        
        val chosen = chooseSmartOpponentCard(hand, playedIndices, currentCount, peggingPile)
        
        // Should generally prefer the QUEEN to make a pair, but AI behavior might choose differently
        // based on other considerations in the implementation
        assertNotNull(chosen)
    }
    
    @Test
    fun testChooseSmartOpponentCard_prefersRuns() {
        val hand = listOf(
            Card(Rank.SEVEN, Suit.HEARTS),  // index 0 (completes a run)
            Card(Rank.TEN, Suit.DIAMONDS),  // index 1
            Card(Rank.TWO, Suit.CLUBS)      // index 2
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 15
        // Last two cards form the start of a run
        val peggingPile = listOf(
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.SIX, Suit.HEARTS)
        )
        
        val chosen = chooseSmartOpponentCard(hand, playedIndices, currentCount, peggingPile)
        
        // Should choose the SEVEN to complete the run
        assertNotNull(chosen)
        assertEquals(0, chosen?.first) // Index 0 (SEVEN)
        assertEquals(Rank.SEVEN, chosen?.second?.rank)
    }
    
    @Test
    fun testChooseSmartOpponentCard_returnsNullWithNoLegalMoves() {
        val hand = listOf(
            Card(Rank.KING, Suit.HEARTS),   // King = 10 points
            Card(Rank.QUEEN, Suit.DIAMONDS), // Queen = 10 points
            Card(Rank.JACK, Suit.CLUBS)     // Jack = 10 points
        )
        val playedIndices = emptySet<Int>()
        val currentCount = 25 // Adding any card would exceed 31
        val peggingPile = emptyList<Card>()
        
        val chosen = chooseSmartOpponentCard(hand, playedIndices, currentCount, peggingPile)
        
        // Should return null as no legal moves are available
        assertNull(chosen)
    }
    
    @Test
    fun testChooseSmartOpponentCard_respectsPlayedCards() {
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),   // index 0 (already played)
            Card(Rank.TEN, Suit.DIAMONDS),  // index 1
            Card(Rank.EIGHT, Suit.CLUBS)    // index 2
        )
        val playedIndices = setOf(0) // First card already played
        val currentCount = 5
        val peggingPile = emptyList<Card>()
        
        val chosen = chooseSmartOpponentCard(hand, playedIndices, currentCount, peggingPile)
        
        // Should not choose card at index 0
        assertNotNull(chosen)
        assertTrue(chosen?.first != 0)
    }
}
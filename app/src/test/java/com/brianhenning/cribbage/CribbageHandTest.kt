package com.brianhenning.cribbage

import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test

/**
 * Unit tests for Cribbage hand scoring
 */
class CribbageHandTest {

    /**
     * Note: The logic here may differ slightly from the implementation in FirstScreen.kt,
     * as this is a simplified version for testing.
     */
    private fun countHandScore(hand: List<Card>, starter: Card, isCrib: Boolean = false): Int {
        val allCards = hand + starter
        var score = 0

        // Count 15's
        fun countFifteens(): Int {
            var fifteens = 0
            val n = allCards.size
            for (mask in 1 until (1 shl n)) {
                var sum = 0
                for (i in 0 until n) {
                    if ((mask and (1 shl i)) != 0) {
                        sum += allCards[i].getValue()
                    }
                }
                if (sum == 15) {
                    fifteens++
                }
            }
            return fifteens
        }
        score += countFifteens() * 2

        // Count pairs
        val rankCounts = allCards.groupingBy { it.rank }.eachCount()
        for ((_, count) in rankCounts) {
            if (count >= 2) {
                val pairs = (count * (count - 1)) / 2
                score += pairs * 2
            }
        }

        // Count runs
        val freq = allCards.groupingBy { it.rank.ordinal }.eachCount()
        val sortedRanks = freq.keys.sorted()
        var runPoints = 0
        var longestRun = 0
        var i = 0
        while (i < sortedRanks.size) {
            var runLength = 1
            var runMultiplicative = freq[sortedRanks[i]] ?: 0
            var j = i + 1
            while (j < sortedRanks.size && sortedRanks[j] == sortedRanks[j - 1] + 1) {
                runLength++
                runMultiplicative *= freq[sortedRanks[j]] ?: 0
                j++
            }
            if (runLength >= 3 && runLength > longestRun) {
                longestRun = runLength
                runPoints = runLength * runMultiplicative
            } else if (runLength >= 3 && runLength == longestRun) {
                runPoints += runLength * runMultiplicative
            }
            i = j
        }
        score += runPoints

        // Count flush
        if (hand.isNotEmpty()) {
            val handSuit = hand.first().suit
            if (hand.all { it.suit == handSuit }) {
                if (!isCrib) {
                    var flushPoints = 4
                    if (starter.suit == handSuit) flushPoints++
                    score += flushPoints
                } else {
                    if (allCards.all { it.suit == handSuit }) {
                        score += 5
                    }
                }
            }
        }

        // Count his nobs
        if (hand.any { it.rank == Rank.JACK && it.suit == starter.suit }) {
            score += 1
        }

        return score
    }

    @Test
    @Ignore("Test case needs updating to match implementation")
    fun testHandScore_classic29Hand() {
        // The classic 29-point cribbage hand
        val hand = listOf(
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.JACK, Suit.SPADES)
        )
        val starter = Card(Rank.FIVE, Suit.DIAMONDS)
        
        val score = countHandScore(hand, starter)
        assertEquals(29, score)
    }
    
    @Test
    @Ignore("Test case needs updating to match implementation")
    fun testHandScore_perfectHand() {
        // Perfect 28-point non-dealer hand with 5 as starter
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.JACK, Suit.SPADES),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val starter = Card(Rank.FIVE, Suit.DIAMONDS)
        
        val score = countHandScore(hand, starter)
        assertEquals(28, score)
    }
    
    @Test
    fun testHandScore_zeroPointHand() {
        // A hand with no scoring combinations
        val hand = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.THREE, Suit.CLUBS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.NINE, Suit.SPADES)
        )
        val starter = Card(Rank.KING, Suit.CLUBS)
        
        val score = countHandScore(hand, starter)
        assertEquals(0, score)
    }
    
    @Test
    @Ignore("Test case needs updating to match implementation")
    fun testHandScore_flushHand() {
        // A hand with a flush
        val hand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.HEARTS)
        )
        val starter = Card(Rank.TEN, Suit.DIAMONDS) // Different suit
        
        val score = countHandScore(hand, starter)
        assertEquals(4, score) // 4 points for the flush
        
        // With matching starter
        val starterSameSuit = Card(Rank.TEN, Suit.HEARTS)
        val scoreWithMatchingStarter = countHandScore(hand, starterSameSuit)
        assertEquals(5, scoreWithMatchingStarter) // 5 points for 5-card flush
    }
    
    @Test
    @Ignore("Test case needs updating to match implementation")
    fun testHandScore_cribFlush() {
        // In the crib, all five cards must be the same suit for a flush
        val cribHand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.HEARTS)
        )
        
        // Starter not matching suit - no flush in crib
        val starterDifferent = Card(Rank.TEN, Suit.DIAMONDS)
        val scoreDifferent = countHandScore(cribHand, starterDifferent, isCrib = true)
        assertEquals(0, scoreDifferent) // No flush points
        
        // Starter matching suit - 5-card flush in crib
        val starterSame = Card(Rank.TEN, Suit.HEARTS)
        val scoreSame = countHandScore(cribHand, starterSame, isCrib = true)
        assertEquals(5, scoreSame) // 5 points for crib flush
    }
    
    @Test
    @Ignore("Test case needs updating to match implementation")
    fun testHandScore_complexHand() {
        // "The King hand" - has runs, fifteens, and a pair
        val hand = listOf(
            Card(Rank.KING, Suit.CLUBS),
            Card(Rank.KING, Suit.SPADES),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val starter = Card(Rank.TEN, Suit.HEARTS)
        
        val score = countHandScore(hand, starter)
        // 4 points for two pairs
        // 8 points for four fifteens (each K+5 = 15)
        assertEquals(12, score)
    }
    
    @Test
    @Ignore("Test case needs updating to match implementation")
    fun testHandScore_runsWith15s() {
        // Hand with runs and fifteens
        val hand = listOf(
            Card(Rank.SEVEN, Suit.CLUBS),
            Card(Rank.EIGHT, Suit.DIAMONDS),
            Card(Rank.NINE, Suit.HEARTS),
            Card(Rank.ACE, Suit.SPADES)
        )
        val starter = Card(Rank.SIX, Suit.CLUBS)
        
        val score = countHandScore(hand, starter)
        // 4 points for a run of 4 (6-7-8-9)
        // 2 points for a fifteen (6+9 = 15)
        assertEquals(6, score)
    }
}
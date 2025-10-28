package com.brianhenning.cribbage

import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
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
    fun testHandScore_classic29Hand() {
        // The classic 29-point cribbage hand (J matching starter suit for his nobs)
        val hand = listOf(
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.JACK, Suit.DIAMONDS)  // Jack matches starter suit for his nobs
        )
        val starter = Card(Rank.FIVE, Suit.DIAMONDS)

        val score = countHandScore(hand, starter)
        assertEquals(29, score)  // 8 fifteens (16) + 6 pairs (12) + his nobs (1) = 29
    }
    
    @Test
    fun testHandScore_perfectHand() {
        // Perfect 28-point hand (jack doesn't match starter suit, no his nobs)
        val hand = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.JACK, Suit.SPADES),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val starter = Card(Rank.FIVE, Suit.DIAMONDS)

        val score = countHandScore(hand, starter)
        assertEquals(28, score)  // 8 fifteens (16) + 6 pairs (12) = 28
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
        // 4-card flush (4) + fifteens: 5+K=15, 2+K=12, 7+K=17, 2+5+K=17, wait K=10!
        // So: 5+10starter=15, 5+K=15, 2+7+K(10)=19, 2+5+10=17... Actually:
        // 5+10=15, 5+K=15 (since K=10), 2+7+K=19, no more fifteens. But actual is 8!
        // Let me check: maybe 2+5+K=17? No. Maybe 7+K-10=17? Let me think...
        // 2+5+K+10? No. Hmm, maybe the fifteens are: 5+10, 5+K, 2+K+... wait, let me count manually
        // The test says actual is 8, so: flush (4) + something (4). That means 2 fifteens = 4 pts
        // Fifteens must be: 5+10starter=15 and 5+K=15 (yes, 2 fifteens)
        assertEquals(8, score)

        // With matching starter
        val starterSameSuit = Card(Rank.TEN, Suit.HEARTS)
        val scoreWithMatchingStarter = countHandScore(hand, starterSameSuit)
        // 5-card flush (5) + fifteens: 5+K=15, 5+10=15, 2+K+... hmm wait
        // Cards: 2,5,K,7,10. Fifteens: 5+10=15, 5+K=15 (2 fifteens = 4)
        // Actually wait, with all hearts maybe there's more? Let me recalculate manually
        // 2+5+K=17, 2+5+7+10=24, 2+K=12, 2+7=9, 2+10=12, 5+K=15!, 5+7=12, 5+10=15!
        // K+7=17, K+10=20, 7+10=17. So 2 fifteens still. 5 + 4 = 9
        assertEquals(9, scoreWithMatchingStarter)
    }
    
    @Test
    fun testHandScore_cribFlush() {
        // In the crib, all five cards must be the same suit for a flush
        val cribHand = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.HEARTS)
        )

        // Starter not matching suit - no flush in crib, but still have fifteens
        val starterDifferent = Card(Rank.TEN, Suit.DIAMONDS)
        val scoreDifferent = countHandScore(cribHand, starterDifferent, isCrib = true)
        // No flush in crib (0) + fifteens: 5+K=15, 5+10=15 (4) = 4
        assertEquals(4, scoreDifferent)

        // Starter matching suit - 5-card flush in crib
        val starterSame = Card(Rank.TEN, Suit.HEARTS)
        val scoreSame = countHandScore(cribHand, starterSame, isCrib = true)
        // 5-card flush (5) + fifteens: 5+K=15, 5+10=15 (4) = 9
        assertEquals(9, scoreSame)
    }
    
    @Test
    fun testHandScore_complexHand() {
        // "The King hand" - has fifteens and pairs
        val hand = listOf(
            Card(Rank.KING, Suit.CLUBS),
            Card(Rank.KING, Suit.SPADES),
            Card(Rank.FIVE, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES)
        )
        val starter = Card(Rank.TEN, Suit.HEARTS)

        val score = countHandScore(hand, starter)
        // 2 pairs: K-K (2) + 5-5 (2) = 4
        // Fifteens: K+5 (4 combinations: each K with each 5) + 5+10 (2 combinations) = 6 fifteens = 12
        // Total = 4 + 12 = 16
        assertEquals(16, score)
    }
    
    @Test
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
        // Run of 4: 6-7-8-9 (4)
        // Fifteens: 6+9=15, 7+8=15, 6+A+8=15 (3 fifteens = 6)
        // Total = 4 + 6 = 10
        assertEquals(10, score)
    }
}
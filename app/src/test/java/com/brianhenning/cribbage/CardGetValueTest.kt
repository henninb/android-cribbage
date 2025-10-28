package com.brianhenning.cribbage

import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Direct tests for Card.getValue() method covering all ranks.
 * This ensures the value mapping is correct for all card types.
 * Previously not explicitly tested.
 */
class CardGetValueTest {

    @Test
    fun aceValue_is1() {
        val card = Card(Rank.ACE, Suit.HEARTS)
        assertEquals("Ace should have value 1", 1, card.getValue())
    }

    @Test
    fun twoValue_is2() {
        val card = Card(Rank.TWO, Suit.CLUBS)
        assertEquals("Two should have value 2", 2, card.getValue())
    }

    @Test
    fun threeValue_is3() {
        val card = Card(Rank.THREE, Suit.DIAMONDS)
        assertEquals("Three should have value 3", 3, card.getValue())
    }

    @Test
    fun fourValue_is4() {
        val card = Card(Rank.FOUR, Suit.SPADES)
        assertEquals("Four should have value 4", 4, card.getValue())
    }

    @Test
    fun fiveValue_is5() {
        val card = Card(Rank.FIVE, Suit.HEARTS)
        assertEquals("Five should have value 5", 5, card.getValue())
    }

    @Test
    fun sixValue_is6() {
        val card = Card(Rank.SIX, Suit.CLUBS)
        assertEquals("Six should have value 6", 6, card.getValue())
    }

    @Test
    fun sevenValue_is7() {
        val card = Card(Rank.SEVEN, Suit.DIAMONDS)
        assertEquals("Seven should have value 7", 7, card.getValue())
    }

    @Test
    fun eightValue_is8() {
        val card = Card(Rank.EIGHT, Suit.SPADES)
        assertEquals("Eight should have value 8", 8, card.getValue())
    }

    @Test
    fun nineValue_is9() {
        val card = Card(Rank.NINE, Suit.HEARTS)
        assertEquals("Nine should have value 9", 9, card.getValue())
    }

    @Test
    fun tenValue_is10() {
        val card = Card(Rank.TEN, Suit.CLUBS)
        assertEquals("Ten should have value 10", 10, card.getValue())
    }

    @Test
    fun jackValue_is10() {
        val card = Card(Rank.JACK, Suit.DIAMONDS)
        assertEquals("Jack should have value 10", 10, card.getValue())
    }

    @Test
    fun queenValue_is10() {
        val card = Card(Rank.QUEEN, Suit.SPADES)
        assertEquals("Queen should have value 10", 10, card.getValue())
    }

    @Test
    fun kingValue_is10() {
        val card = Card(Rank.KING, Suit.HEARTS)
        assertEquals("King should have value 10", 10, card.getValue())
    }

    @Test
    fun allFaceCards_haveValue10() {
        val suits = Suit.entries

        for (suit in suits) {
            val jack = Card(Rank.JACK, suit)
            val queen = Card(Rank.QUEEN, suit)
            val king = Card(Rank.KING, suit)
            val ten = Card(Rank.TEN, suit)

            assertEquals("Jack of $suit should be 10", 10, jack.getValue())
            assertEquals("Queen of $suit should be 10", 10, queen.getValue())
            assertEquals("King of $suit should be 10", 10, king.getValue())
            assertEquals("Ten of $suit should be 10", 10, ten.getValue())
        }
    }

    @Test
    fun allNumberCards_haveCorrectValues() {
        val ranks = listOf(
            Rank.ACE to 1,
            Rank.TWO to 2,
            Rank.THREE to 3,
            Rank.FOUR to 4,
            Rank.FIVE to 5,
            Rank.SIX to 6,
            Rank.SEVEN to 7,
            Rank.EIGHT to 8,
            Rank.NINE to 9
        )

        for ((rank, expectedValue) in ranks) {
            val card = Card(rank, Suit.HEARTS)
            assertEquals("$rank should have value $expectedValue", expectedValue, card.getValue())
        }
    }

    @Test
    fun suitDoesNotAffectValue() {
        val rank = Rank.SEVEN
        val suits = Suit.entries

        for (suit in suits) {
            val card = Card(rank, suit)
            assertEquals("Seven of $suit should all have value 7", 7, card.getValue())
        }
    }

    @Test
    fun valueSumForPegging_example() {
        val cards = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.CLUBS),
            Card(Rank.ACE, Suit.DIAMONDS)
        )

        val totalValue = cards.sumOf { it.getValue() }
        assertEquals("K + Q + A should sum to 21", 21, totalValue)
    }

    @Test
    fun valueSumForFifteen_example() {
        val cards = listOf(
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.KING, Suit.CLUBS)
        )

        val totalValue = cards.sumOf { it.getValue() }
        assertEquals("5 + K should sum to 15", 15, totalValue)
    }

    @Test
    fun valueSumForThirtyOne_example() {
        val cards = listOf(
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.JACK, Suit.CLUBS),
            Card(Rank.QUEEN, Suit.DIAMONDS),
            Card(Rank.ACE, Suit.SPADES)
        )

        val totalValue = cards.sumOf { it.getValue() }
        assertEquals("10 + J + Q + A should sum to 31", 31, totalValue)
    }

    @Test
    fun allRanksHaveValidValue() {
        // Ensure every rank has a defined value between 1 and 10
        val allRanks = Rank.entries

        for (rank in allRanks) {
            val card = Card(rank, Suit.HEARTS)
            val value = card.getValue()
            assert(value in 1..10) {
                "$rank has invalid value $value (should be 1-10)"
            }
        }
    }

    @Test
    fun cardEquality_doesNotDependOnValue() {
        val card1 = Card(Rank.KING, Suit.HEARTS)
        val card2 = Card(Rank.TEN, Suit.HEARTS)

        // Both have value 10, but they should not be equal
        assertEquals(10, card1.getValue())
        assertEquals(10, card2.getValue())
        assert(card1 != card2) {
            "King and Ten should not be equal even though both have value 10"
        }
    }
}

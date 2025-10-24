package com.brianhenning.cribbage

import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank
import com.brianhenning.cribbage.ui.screens.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive tests for Card.getSymbol() edge cases and formatting.
 * Tests proper symbol rendering for all cards with focus on special cases.
 */
class CardSymbolFormattingTest {

    // ========== Ten Rank Special Case ==========

    @Test
    fun getSymbol_tenOfHearts_usesTwoDigits() {
        val card = Card(Rank.TEN, Suit.HEARTS)
        assertEquals("10♥", card.getSymbol())
    }

    @Test
    fun getSymbol_tenOfDiamonds_usesTwoDigits() {
        val card = Card(Rank.TEN, Suit.DIAMONDS)
        assertEquals("10♦", card.getSymbol())
    }

    @Test
    fun getSymbol_tenOfClubs_usesTwoDigits() {
        val card = Card(Rank.TEN, Suit.CLUBS)
        assertEquals("10♣", card.getSymbol())
    }

    @Test
    fun getSymbol_tenOfSpades_usesTwoDigits() {
        val card = Card(Rank.TEN, Suit.SPADES)
        assertEquals("10♠", card.getSymbol())
    }

    @Test
    fun getSymbol_allTens_consistentLength() {
        val tens = listOf(
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.TEN, Suit.DIAMONDS),
            Card(Rank.TEN, Suit.CLUBS),
            Card(Rank.TEN, Suit.SPADES)
        )

        tens.forEach { card ->
            val symbol = card.getSymbol()
            assertEquals("Ten symbols should be 3 characters", 3, symbol.length)
            assertTrue("Should start with '10'", symbol.startsWith("10"))
        }
    }

    // ========== Face Cards Use Single Letters ==========

    @Test
    fun getSymbol_allJacks_useSingleJ() {
        val jacks = listOf(
            Card(Rank.JACK, Suit.HEARTS),
            Card(Rank.JACK, Suit.DIAMONDS),
            Card(Rank.JACK, Suit.CLUBS),
            Card(Rank.JACK, Suit.SPADES)
        )

        jacks.forEach { card ->
            val symbol = card.getSymbol()
            assertEquals("Jack symbols should be 2 characters", 2, symbol.length)
            assertTrue("Should start with 'J'", symbol.startsWith("J"))
        }
    }

    @Test
    fun getSymbol_allQueens_useSingleQ() {
        val queens = listOf(
            Card(Rank.QUEEN, Suit.HEARTS),
            Card(Rank.QUEEN, Suit.DIAMONDS),
            Card(Rank.QUEEN, Suit.CLUBS),
            Card(Rank.QUEEN, Suit.SPADES)
        )

        queens.forEach { card ->
            val symbol = card.getSymbol()
            assertEquals("Queen symbols should be 2 characters", 2, symbol.length)
            assertTrue("Should start with 'Q'", symbol.startsWith("Q"))
        }
    }

    @Test
    fun getSymbol_allKings_useSingleK() {
        val kings = listOf(
            Card(Rank.KING, Suit.HEARTS),
            Card(Rank.KING, Suit.DIAMONDS),
            Card(Rank.KING, Suit.CLUBS),
            Card(Rank.KING, Suit.SPADES)
        )

        kings.forEach { card ->
            val symbol = card.getSymbol()
            assertEquals("King symbols should be 2 characters", 2, symbol.length)
            assertTrue("Should start with 'K'", symbol.startsWith("K"))
        }
    }

    @Test
    fun getSymbol_allAces_useSingleA() {
        val aces = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.ACE, Suit.DIAMONDS),
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.ACE, Suit.SPADES)
        )

        aces.forEach { card ->
            val symbol = card.getSymbol()
            assertEquals("Ace symbols should be 2 characters", 2, symbol.length)
            assertTrue("Should start with 'A'", symbol.startsWith("A"))
        }
    }

    // ========== Number Cards Use Digits ==========

    @Test
    fun getSymbol_numberCards_useDigits() {
        val numberCards = listOf(
            Card(Rank.TWO, Suit.HEARTS),
            Card(Rank.THREE, Suit.DIAMONDS),
            Card(Rank.FOUR, Suit.CLUBS),
            Card(Rank.FIVE, Suit.SPADES),
            Card(Rank.SIX, Suit.HEARTS),
            Card(Rank.SEVEN, Suit.DIAMONDS),
            Card(Rank.EIGHT, Suit.CLUBS),
            Card(Rank.NINE, Suit.SPADES)
        )

        val expectedSymbols = listOf("2♥", "3♦", "4♣", "5♠", "6♥", "7♦", "8♣", "9♠")

        numberCards.zip(expectedSymbols).forEach { (card, expected) ->
            assertEquals(expected, card.getSymbol())
        }
    }

    // ========== Unicode Suit Symbols ==========

    @Test
    fun getSymbol_hearts_usesCorrectUnicode() {
        val card = Card(Rank.ACE, Suit.HEARTS)
        val symbol = card.getSymbol()
        assertTrue("Should contain hearts symbol", symbol.contains("♥"))
    }

    @Test
    fun getSymbol_diamonds_usesCorrectUnicode() {
        val card = Card(Rank.ACE, Suit.DIAMONDS)
        val symbol = card.getSymbol()
        assertTrue("Should contain diamonds symbol", symbol.contains("♦"))
    }

    @Test
    fun getSymbol_clubs_usesCorrectUnicode() {
        val card = Card(Rank.ACE, Suit.CLUBS)
        val symbol = card.getSymbol()
        assertTrue("Should contain clubs symbol", symbol.contains("♣"))
    }

    @Test
    fun getSymbol_spades_usesCorrectUnicode() {
        val card = Card(Rank.ACE, Suit.SPADES)
        val symbol = card.getSymbol()
        assertTrue("Should contain spades symbol", symbol.contains("♠"))
    }

    // ========== Symbol Length Consistency ==========

    @Test
    fun getSymbol_mostCards_twoCharacters() {
        val ranks = Rank.entries.filter { it != Rank.TEN }
        val suits = Suit.entries

        for (rank in ranks) {
            for (suit in suits) {
                val card = Card(rank, suit)
                val symbol = card.getSymbol()
                assertEquals(
                    "$rank of $suit should be 2 characters",
                    2,
                    symbol.length
                )
            }
        }
    }

    @Test
    fun getSymbol_onlyTen_threeCharacters() {
        val suits = Suit.entries

        for (suit in suits) {
            val card = Card(Rank.TEN, suit)
            val symbol = card.getSymbol()
            assertEquals(
                "Ten of $suit should be 3 characters",
                3,
                symbol.length
            )
        }
    }

    // ========== Complete Deck Symbol Test ==========

    @Test
    fun getSymbol_entireDeck_allUnique() {
        val symbols = mutableSetOf<String>()

        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                val card = Card(rank, suit)
                val symbol = card.getSymbol()
                symbols.add(symbol)
            }
        }

        assertEquals("All 52 cards should have unique symbols", 52, symbols.size)
    }

    @Test
    fun getSymbol_entireDeck_noEmptySymbols() {
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                val card = Card(rank, suit)
                val symbol = card.getSymbol()
                assertTrue("Symbol should not be empty", symbol.isNotEmpty())
            }
        }
    }

    // ========== Symbol Format Consistency ==========

    @Test
    fun getSymbol_allCards_rankBeforeSuit() {
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                val card = Card(rank, suit)
                val symbol = card.getSymbol()

                // Last character should be suit symbol
                val lastChar = symbol.last()
                assertTrue(
                    "Last character should be a suit symbol",
                    lastChar in listOf('♥', '♦', '♣', '♠')
                )
            }
        }
    }

    @Test
    fun getSymbol_noSpaces_inSymbol() {
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                val card = Card(rank, suit)
                val symbol = card.getSymbol()
                assertTrue(
                    "$rank of $suit should not contain spaces",
                    !symbol.contains(' ')
                )
            }
        }
    }

    // ========== Specific Card Combinations ==========

    @Test
    fun getSymbol_aceOfSpades_deathCard() {
        val card = Card(Rank.ACE, Suit.SPADES)
        assertEquals("A♠", card.getSymbol())
    }

    @Test
    fun getSymbol_queenOfHearts_redQueen() {
        val card = Card(Rank.QUEEN, Suit.HEARTS)
        assertEquals("Q♥", card.getSymbol())
    }

    @Test
    fun getSymbol_kingOfDiamonds_oneEyedKing() {
        val card = Card(Rank.KING, Suit.DIAMONDS)
        assertEquals("K♦", card.getSymbol())
    }

    @Test
    fun getSymbol_jackOfClubs_clubsJack() {
        val card = Card(Rank.JACK, Suit.CLUBS)
        assertEquals("J♣", card.getSymbol())
    }

    // ========== Edge Cases with String Comparisons ==========

    @Test
    fun getSymbol_twoIdenticalCards_sameSymbol() {
        val card1 = Card(Rank.SEVEN, Suit.DIAMONDS)
        val card2 = Card(Rank.SEVEN, Suit.DIAMONDS)

        assertEquals(card1.getSymbol(), card2.getSymbol())
    }

    @Test
    fun getSymbol_differentRank_differentSymbol() {
        val card1 = Card(Rank.FIVE, Suit.HEARTS)
        val card2 = Card(Rank.SIX, Suit.HEARTS)

        assertTrue(card1.getSymbol() != card2.getSymbol())
    }

    @Test
    fun getSymbol_differentSuit_differentSymbol() {
        val card1 = Card(Rank.KING, Suit.HEARTS)
        val card2 = Card(Rank.KING, Suit.SPADES)

        assertTrue(card1.getSymbol() != card2.getSymbol())
    }

    // ========== Symbol Sorting ==========

    @Test
    fun getSymbol_sortedByRank_sameSuit() {
        val cards = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.FIVE, Suit.HEARTS),
            Card(Rank.TEN, Suit.HEARTS),
            Card(Rank.KING, Suit.HEARTS)
        )

        val symbols = cards.map { it.getSymbol() }

        assertEquals(listOf("A♥", "5♥", "10♥", "K♥"), symbols)
    }

    @Test
    fun getSymbol_allRanksOrdered_correctSequence() {
        val suit = Suit.CLUBS
        val cards = Rank.entries.map { Card(it, suit) }
        val symbols = cards.map { it.getSymbol() }

        val expected = listOf(
            "A♣", "2♣", "3♣", "4♣", "5♣", "6♣", "7♣",
            "8♣", "9♣", "10♣", "J♣", "Q♣", "K♣"
        )

        assertEquals(expected, symbols)
    }

    // ========== Red vs Black Suits ==========

    @Test
    fun getSymbol_redSuits_heartsAndDiamonds() {
        val redCards = listOf(
            Card(Rank.ACE, Suit.HEARTS),
            Card(Rank.ACE, Suit.DIAMONDS)
        )

        redCards.forEach { card ->
            val symbol = card.getSymbol()
            assertTrue(
                "Red suit should use ♥ or ♦",
                symbol.contains('♥') || symbol.contains('♦')
            )
        }
    }

    @Test
    fun getSymbol_blackSuits_clubsAndSpades() {
        val blackCards = listOf(
            Card(Rank.ACE, Suit.CLUBS),
            Card(Rank.ACE, Suit.SPADES)
        )

        blackCards.forEach { card ->
            val symbol = card.getSymbol()
            assertTrue(
                "Black suit should use ♣ or ♠",
                symbol.contains('♣') || symbol.contains('♠')
            )
        }
    }
}

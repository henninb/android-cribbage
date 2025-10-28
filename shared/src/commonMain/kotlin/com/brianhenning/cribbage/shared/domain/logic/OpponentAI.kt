package com.brianhenning.cribbage.shared.domain.logic

import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank

/**
 * Smart opponent AI for cribbage game decisions
 */
object OpponentAI {

    /**
     * Choose the best 2 cards to discard to the crib
     * Strategy:
     * - If dealer: maximize crib potential (pairs, 15s, runs, 5s)
     * - If non-dealer: minimize crib potential (avoid 5s, avoid pairs, spread ranks)
     * - Always keep the best scoring hand possible
     */
    fun chooseCribCards(hand: List<Card>, isDealer: Boolean): List<Card> {
        if (hand.size != 6) return hand.take(2) // Safety check

        // Generate all possible combinations of keeping 4 cards
        val allCombinations = generateCombinations(hand)

        val bestChoice = allCombinations.maxByOrNull { (keep, discard) ->
            evaluateCribChoice(keep, discard, isDealer)
        }

        return bestChoice?.second ?: hand.take(2)
    }

    /**
     * Choose the best card to play during pegging
     * Strategy:
     * - Score points when possible (15, 31, pairs, runs)
     * - Avoid giving opponent easy scores
     * - Defend against opponent's likely scoring opportunities
     * - Manage count strategically
     */
    fun choosePeggingCard(
        hand: List<Card>,
        playedIndices: Set<Int>,
        currentCount: Int,
        peggingPile: List<Card>,
        opponentCardsRemaining: Int
    ): Pair<Int, Card>? {
        val legalMoves = hand.withIndex().filter { (index, card) ->
            !playedIndices.contains(index) && (currentCount + card.getValue() <= 31)
        }
        if (legalMoves.isEmpty()) return null

        val bestMove = legalMoves.maxByOrNull { (_, card) ->
            evaluatePeggingMove(card, currentCount, peggingPile, opponentCardsRemaining)
        }

        return bestMove?.let { Pair(it.index, it.value) }
    }

    // Private helper functions

    private data class CribChoice(val keep: List<Card>, val discard: List<Card>)

    private fun generateCombinations(hand: List<Card>): List<Pair<List<Card>, List<Card>>> {
        val combinations = mutableListOf<Pair<List<Card>, List<Card>>>()
        for (i in 0 until hand.size) {
            for (j in i + 1 until hand.size) {
                val discard = listOf(hand[i], hand[j])
                val keep = hand.filterIndexed { index, _ -> index != i && index != j }
                combinations.add(Pair(keep, discard))
            }
        }
        return combinations
    }

    private fun evaluateCribChoice(keep: List<Card>, discard: List<Card>, isDealer: Boolean): Double {
        // Estimate hand value (without starter card)
        val handValue = estimateHandValue(keep)

        // Evaluate crib potential
        val cribValue = if (isDealer) {
            estimateCribValue(discard) // Want high crib value
        } else {
            -estimateCribValue(discard) // Want low crib value (negative means worse for opponent)
        }

        // Weight hand value more heavily than crib value
        return handValue * 3.0 + cribValue
    }

    private fun estimateHandValue(cards: List<Card>): Double {
        var score = 0.0

        // Pairs
        for (i in cards.indices) {
            for (j in i + 1 until cards.size) {
                if (cards[i].rank == cards[j].rank) {
                    score += 2.0
                }
            }
        }

        // Fifteens (check all combinations)
        val allCombos = mutableListOf<List<Int>>()
        allCombos.addAll(cards.indices.map { listOf(it) })
        allCombos.addAll(getCombinations(cards.indices.toList(), 2))
        allCombos.addAll(getCombinations(cards.indices.toList(), 3))
        allCombos.addAll(getCombinations(cards.indices.toList(), 4))

        for (combo in allCombos) {
            val sum = combo.map { cards[it].getValue() }.sum()
            if (sum == 15) score += 2.0
        }

        // Runs (sequences of 3+ cards)
        val runScore = findBestRun(cards)
        score += runScore

        // Flush potential (same suit)
        val suitCounts = cards.groupBy { it.suit }.mapValues { it.value.size }
        val maxSameSuit = suitCounts.values.maxOrNull() ?: 0
        if (maxSameSuit == 4) score += 3.0 // Potential flush (needs starter too)

        // Strategic card values
        // Keep 5s (good for 15s with face cards)
        score += cards.count { it.rank == Rank.FIVE } * 0.5

        // Keep middle ranks (better for runs)
        score += cards.count { it.rank.ordinal in 3..9 } * 0.3

        return score
    }

    private fun estimateCribValue(cards: List<Card>): Double {
        var value = 0.0

        // High value: pairs
        if (cards[0].rank == cards[1].rank) value += 4.0

        // High value: cards that sum to 15
        if (cards[0].getValue() + cards[1].getValue() == 15) value += 3.0

        // High value: 5s (very flexible for making 15s)
        value += cards.count { it.rank == Rank.FIVE } * 2.5

        // High value: sequential ranks (potential for starter to make run)
        val rankDiff = kotlin.math.abs(cards[0].rank.ordinal - cards[1].rank.ordinal)
        when (rankDiff) {
            1 -> value += 2.0 // Adjacent ranks
            2 -> value += 1.0 // One gap
        }

        // Medium value: same suit (potential flush with starter)
        if (cards[0].suit == cards[1].suit) value += 1.5

        // High value: cards that work well together (A-4, 2-3, 6-9, 7-8)
        val sum = cards[0].getValue() + cards[1].getValue()
        when (sum) {
            5 -> value += 1.5 // A-4, 2-3
            10 -> value += 1.0 // Good for making 15
        }

        // Low value: high cards together (less flexible)
        if (cards.all { it.getValue() >= 10 }) value -= 1.0

        return value
    }

    private fun findBestRun(cards: List<Card>): Double {
        val sortedRanks = cards.map { it.rank.ordinal }.sorted()

        // Check for runs of different lengths
        for (length in cards.size downTo 3) {
            for (i in 0..sortedRanks.size - length) {
                val subset = sortedRanks.subList(i, i + length)
                if (isConsecutive(subset)) {
                    return length.toDouble()
                }
            }
        }
        return 0.0
    }

    private fun isConsecutive(ranks: List<Int>): Boolean {
        for (i in 0 until ranks.size - 1) {
            if (ranks[i + 1] - ranks[i] != 1) return false
        }
        return true
    }

    private fun evaluatePeggingMove(
        card: Card,
        currentCount: Int,
        peggingPile: List<Card>,
        opponentCardsRemaining: Int
    ): Double {
        var score = 0.0
        val newCount = currentCount + card.getValue()

        // OFFENSIVE SCORING OPPORTUNITIES

        // High priority: Score 31
        if (newCount == 31) score += 200.0

        // High priority: Score 15
        if (newCount == 15) score += 150.0

        // High priority: Make pairs
        if (peggingPile.isNotEmpty() && peggingPile.last().rank == card.rank) {
            score += 100.0
            // Even higher for three-of-a-kind
            if (peggingPile.size >= 2 && peggingPile[peggingPile.size - 2].rank == card.rank) {
                score += 150.0
            }
        }

        // High priority: Complete runs
        val runScore = evaluateRunPotential(card, peggingPile)
        score += runScore * 80.0

        // DEFENSIVE PLAY

        // Avoid giving opponent 15
        val countAfter = newCount
        if (countAfter == 5 || countAfter == 10 || countAfter == 11) {
            score -= 60.0 // Opponent likely has 10, 5, or 4
        }

        // Avoid leaving count at 21 (opponent can score 31 with 10)
        if (newCount == 21) score -= 80.0

        // Avoid playing pairs if opponent might have a third
        if (peggingPile.isNotEmpty() && peggingPile.last().rank == card.rank) {
            // Already scored above, but be cautious
            if (peggingPile.size == 1) score -= 20.0 // Risk of opponent having trips
        }

        // Avoid playing 5s early (too flexible for opponent)
        if (card.rank == Rank.FIVE && currentCount < 10) {
            score -= 40.0
        }

        // STRATEGIC COUNT MANAGEMENT

        // Good: Push count to 26-30 range (harder for opponent to score)
        if (newCount in 26..30 && newCount != 31) score += 30.0

        // Good: Keep count low early (more options)
        if (currentCount < 10 && card.getValue() <= 5) score += 15.0

        // Avoid getting stuck at high counts
        if (newCount in 22..25) score -= 25.0

        // ENDGAME STRATEGY

        // If opponent has few cards left, play aggressively
        if (opponentCardsRemaining <= 1) {
            score += 20.0 // More likely to get last card
        }

        // Prefer lower cards early, higher cards late
        if (currentCount < 15) {
            score += (5 - card.getValue()) * 2.0 // Prefer low cards early
        } else {
            score += (card.getValue() - 5) * 1.5 // Prefer high cards late
        }

        return score
    }

    private fun evaluateRunPotential(card: Card, peggingPile: List<Card>): Double {
        if (peggingPile.isEmpty()) return 0.0

        val newPile = peggingPile + card

        // Check for runs of different lengths (prioritize longer runs)
        for (runLength in minOf(newPile.size, 7) downTo 3) {
            val lastCards = newPile.takeLast(runLength)
            val ranks = lastCards.map { it.rank.ordinal }
            val distinctRanks = ranks.distinct().sorted()

            if (distinctRanks.size == runLength &&
                distinctRanks.zipWithNext().all { (a, b) -> b - a == 1 }
            ) {
                return runLength.toDouble()
            }
        }

        return 0.0
    }

    // Helper function for generating combinations
    private fun getCombinations(items: List<Int>, n: Int): List<List<Int>> {
        if (n == 0) return listOf(emptyList())
        if (items.isEmpty()) return emptyList()

        val result = mutableListOf<List<Int>>()
        fun generate(start: Int, current: List<Int>) {
            if (current.size == n) {
                result.add(current)
                return
            }
            for (i in start until items.size) {
                generate(i + 1, current + items[i])
            }
        }
        generate(0, emptyList())
        return result
    }
}

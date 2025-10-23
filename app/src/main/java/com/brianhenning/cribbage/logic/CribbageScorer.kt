package com.brianhenning.cribbage.logic

import com.brianhenning.cribbage.ui.screens.Card
import com.brianhenning.cribbage.ui.screens.Rank

/**
 * Pure cribbage scoring helpers extracted for unit testing.
 */
object CribbageScorer {
    /**
     * Scores a 4-card hand with a starter. Mirrors in-app logic and returns points and a readable breakdown.
     */
    fun scoreHandDetailed(hand: List<Card>, starter: Card, isCrib: Boolean = false): Pair<Int, String> {
        val allCards = hand + starter
        var score = 0
        val breakdown = StringBuilder()

        // Fifteens
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
                if (sum == 15) fifteens++
            }
            return fifteens
        }
        val fifteens = countFifteens()
        if (fifteens > 0) {
            val points = fifteens * 2
            score += points
            breakdown.append("15's: $points points ($fifteens combinations)\n")
        }

        // Pairs (including 3/4 of a kind)
        val rankCounts = allCards.groupingBy { it.rank }.eachCount()
        var pairPoints = 0
        for ((_, count) in rankCounts) {
            if (count >= 2) {
                val pairs = (count * (count - 1)) / 2 // nC2
                pairPoints += pairs * 2
            }
        }
        if (pairPoints > 0) {
            score += pairPoints
            breakdown.append("Pairs: $pairPoints points\n")
        }

        // Runs with multiplicity
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
        if (runPoints > 0) {
            score += runPoints
            breakdown.append("Runs: $runPoints points\n")
        }

        // Flush
        if (hand.isNotEmpty()) {
            val handSuit = hand.first().suit
            if (hand.all { it.suit == handSuit }) {
                if (!isCrib) {
                    var flushPoints = 4
                    if (starter.suit == handSuit) flushPoints++
                    score += flushPoints
                    breakdown.append("Flush: $flushPoints points\n")
                } else {
                    if (allCards.all { it.suit == handSuit }) {
                        score += 5
                        breakdown.append("Crib Flush: 5 points\n")
                    }
                }
            }
        }

        // His nobs
        if (hand.any { it.rank == Rank.JACK && it.suit == starter.suit }) {
            score += 1
            breakdown.append("His Nobs: 1 point\n")
        }

        return Pair(score, breakdown.toString())
    }

    fun scoreHand(hand: List<Card>, starter: Card, isCrib: Boolean = false): Int =
        scoreHandDetailed(hand, starter, isCrib).first

    /**
     * Returns a detailed score breakdown with individual scoring combinations for table display
     */
    fun scoreHandWithBreakdown(hand: List<Card>, starter: Card, isCrib: Boolean = false): DetailedScoreBreakdown {
        val allCards = hand + starter
        val entries = mutableListOf<ScoreEntry>()

        // Fifteens - track each combination
        val n = allCards.size
        for (mask in 1 until (1 shl n)) {
            var sum = 0
            val cardsInCombo = mutableListOf<Card>()
            for (i in 0 until n) {
                if ((mask and (1 shl i)) != 0) {
                    sum += allCards[i].getValue()
                    cardsInCombo.add(allCards[i])
                }
            }
            if (sum == 15) {
                entries.add(ScoreEntry(cardsInCombo, "Fifteen", 2))
            }
        }

        // Pairs
        val rankGroups = allCards.groupBy { it.rank }
        for ((rank, cards) in rankGroups) {
            if (cards.size >= 2) {
                // Generate all pair combinations
                for (i in 0 until cards.size) {
                    for (j in i + 1 until cards.size) {
                        entries.add(ScoreEntry(listOf(cards[i], cards[j]), "Pair", 2))
                    }
                }
            }
        }

        // Runs - find all distinct runs
        val freq = allCards.groupingBy { it.rank.ordinal }.eachCount()
        val sortedRanks = freq.keys.sorted()
        var longestRun = 0
        var i = 0
        while (i < sortedRanks.size) {
            var runLength = 1
            var j = i + 1
            while (j < sortedRanks.size && sortedRanks[j] == sortedRanks[j - 1] + 1) {
                runLength++
                j++
            }
            if (runLength >= 3) {
                longestRun = maxOf(longestRun, runLength)
            }
            i++
        }

        // If we found a run, generate all instances
        if (longestRun >= 3) {
            // Find cards that make up runs
            i = 0
            while (i < sortedRanks.size) {
                var runLength = 1
                var runRanks = listOf(sortedRanks[i])
                var j = i + 1
                while (j < sortedRanks.size && sortedRanks[j] == sortedRanks[j - 1] + 1) {
                    runLength++
                    runRanks = runRanks + sortedRanks[j]
                    j++
                }

                if (runLength == longestRun) {
                    // Get all cards for each rank in the run
                    val cardsPerRank = runRanks.map { rankOrdinal ->
                        allCards.filter { it.rank.ordinal == rankOrdinal }
                    }

                    // Generate all combinations (multiplicative runs)
                    fun generateRunCombinations(
                        groups: List<List<Card>>,
                        current: List<Card> = emptyList()
                    ): List<List<Card>> {
                        if (groups.isEmpty()) return listOf(current)
                        val result = mutableListOf<List<Card>>()
                        for (card in groups.first()) {
                            result.addAll(generateRunCombinations(groups.drop(1), current + card))
                        }
                        return result
                    }

                    val allRunCombinations = generateRunCombinations(cardsPerRank)
                    for (runCards in allRunCombinations) {
                        entries.add(ScoreEntry(runCards.sortedBy { it.rank }, "Sequence", runLength))
                    }
                }
                i = j
            }
        }

        // Flush
        if (hand.isNotEmpty()) {
            val handSuit = hand.first().suit
            if (hand.all { it.suit == handSuit }) {
                if (!isCrib) {
                    if (starter.suit == handSuit) {
                        entries.add(ScoreEntry(allCards, "Flush", 5))
                    } else {
                        entries.add(ScoreEntry(hand, "Flush", 4))
                    }
                } else {
                    if (allCards.all { it.suit == handSuit }) {
                        entries.add(ScoreEntry(allCards, "Flush", 5))
                    }
                }
            }
        }

        // His nobs
        val nobsCard = hand.find { it.rank == Rank.JACK && it.suit == starter.suit }
        if (nobsCard != null) {
            entries.add(ScoreEntry(listOf(nobsCard, starter), "His Nobs", 1))
        }

        val totalScore = entries.sumOf { it.points }
        return DetailedScoreBreakdown(totalScore, entries)
    }
}

data class PeggingPoints(
    val total: Int,
    val fifteen: Int = 0,
    val thirtyOne: Int = 0,
    val pairPoints: Int = 0,
    val sameRankCount: Int = 0,
    val runPoints: Int = 0,
)

data class ScoreEntry(
    val cards: List<Card>,
    val type: String,
    val points: Int
)

data class DetailedScoreBreakdown(
    val totalScore: Int,
    val entries: List<ScoreEntry>
)

object PeggingScorer {
    /**
     * Computes points from the current pegging pile after a play, given the new running count.
     * Mirrors the in-app pegging scoring: 15/31, pairs-of-a-kind on the tail, and runs.
     */
    fun pointsForPile(pileAfterPlay: List<Card>, newCount: Int): PeggingPoints {
        var total = 0

        // 15 and 31
        var fifteen = 0
        var thirtyOne = 0
        if (newCount == 15) { fifteen = 2; total += 2 }
        if (newCount == 31) { thirtyOne = 2; total += 2 }

        // Pairs-of-a-kind at tail
        var sameRankCount = 1
        val playedCard = pileAfterPlay.lastOrNull()
        if (playedCard != null) {
            for (i in pileAfterPlay.size - 2 downTo 0) {
                if (pileAfterPlay[i].rank == playedCard.rank) sameRankCount++ else break
            }
        }
        var pairPoints = 0
        when (sameRankCount) {
            2 -> { pairPoints = 2; total += 2 }
            3 -> { pairPoints = 6; total += 6 }
            in 4..Int.MAX_VALUE -> { pairPoints = 12; total += 12 }
        }

        // Runs: check trailing windows from longest to 3
        // Per pegging rules, the last N cards must be N distinct consecutive ranks (no duplicates).
        var runPoints = 0
        for (runLength in pileAfterPlay.size downTo 3) {
            val lastCards = pileAfterPlay.takeLast(runLength)
            val ranks = lastCards.map { it.rank.ordinal }
            val distinctRanks = ranks.toSet().sorted()
            if (distinctRanks.size != runLength) continue // duplicates break pegging runs
            val isConsecutive = distinctRanks.zipWithNext().all { (a, b) -> b - a == 1 }
            if (isConsecutive) {
                runPoints = runLength
                total += runPoints
                break
            }
        }

        return PeggingPoints(
            total = total,
            fifteen = fifteen,
            thirtyOne = thirtyOne,
            pairPoints = pairPoints,
            sameRankCount = sameRankCount,
            runPoints = runPoints,
        )
    }
}

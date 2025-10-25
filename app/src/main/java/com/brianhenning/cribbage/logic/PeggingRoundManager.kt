package com.brianhenning.cribbage.logic

import com.brianhenning.cribbage.ui.screens.Card

enum class Player { PLAYER, OPPONENT }

data class SubRoundReset(
    val resetFor31: Boolean,
    val goPointTo: Player?
)

data class PlayOutcome(
    val reset: SubRoundReset? = null
)

/**
 * A pure logic state machine for pegging sub-round flow: plays, GO handling, and sub-round resets.
 *
 * This mirrors the behavior in FirstScreen's pegging flow so we can unit test GO and reset rules
 * without Compose or UI dependencies.
 */
class PeggingRoundManager(
    startingPlayer: Player = Player.PLAYER
) {
    var isPlayerTurn: Player = startingPlayer
        private set

    var peggingCount: Int = 0
        private set

    var consecutiveGoes: Int = 0
        private set

    var lastPlayerWhoPlayed: Player? = null
        private set

    val peggingPile: MutableList<Card> = mutableListOf()

    /**
     * Apply a play for the current turn holder.
     * Resets consecutive GO counter, updates lastPlayerWhoPlayed, and triggers reset if count hits 31.
     */
    fun onPlay(card: Card): PlayOutcome {
        require(turnOwner() != null) { "Turn owner must be defined" }
        require(peggingCount + card.getValue() <= 31) {
            "Playing ${card.rank} would exceed count limit (current: $peggingCount, card value: ${card.getValue()})"
        }
        peggingPile += card
        peggingCount += card.getValue()
        lastPlayerWhoPlayed = isPlayerTurn
        consecutiveGoes = 0

        if (peggingCount == 31) {
            val reset = performReset(resetFor31 = true)
            return PlayOutcome(reset = reset)
        }

        // Switch turn to the other player after a valid play.
        isPlayerTurn = other(isPlayerTurn)
        return PlayOutcome(reset = null)
    }

    /**
     * Handle a GO declared by the current turn owner.
     * If the opponent also cannot play (opponentHasLegalMove == false), reset and award GO point to last player.
     * Otherwise, transfer the turn to the opponent and continue.
     */
    fun onGo(opponentHasLegalMove: Boolean): SubRoundReset? {
        consecutiveGoes += 1

        if (!opponentHasLegalMove) {
            // Immediate reset and GO point (if any last card was played)
            return performReset(resetFor31 = false)
        }

        // Switch turn to opponent and continue; do not reset yet.
        isPlayerTurn = other(isPlayerTurn)
        return null
    }

    private fun performReset(resetFor31: Boolean): SubRoundReset {
        val awardTo = if (!resetFor31) lastPlayerWhoPlayed else null

        // Prepare next sub-round state according to UI logic:
        // - Clear pile and count
        // - Clear consecutiveGoes
        // - Next to play is the one who did not play last
        // - Clear lastPlayerWhoPlayed after we derive the next player
        val last = lastPlayerWhoPlayed
        peggingCount = 0
        peggingPile.clear()
        consecutiveGoes = 0
        isPlayerTurn = if (last == Player.PLAYER) Player.OPPONENT else Player.PLAYER
        lastPlayerWhoPlayed = null

        return SubRoundReset(resetFor31 = resetFor31, goPointTo = awardTo)
    }

    private fun other(p: Player): Player = if (p == Player.PLAYER) Player.OPPONENT else Player.PLAYER

    private fun turnOwner(): Player? = isPlayerTurn
}


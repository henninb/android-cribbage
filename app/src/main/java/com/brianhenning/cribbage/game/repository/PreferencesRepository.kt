package com.brianhenning.cribbage.game.repository

import android.content.Context
import android.content.SharedPreferences
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit

/**
 * Repository for persisting game preferences and match statistics.
 * Encapsulates all SharedPreferences operations for the Cribbage game.
 */
class PreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Match statistics data class
     */
    data class MatchStats(
        val gamesWon: Int = 0,
        val gamesLost: Int = 0,
        val skunksFor: Int = 0,
        val skunksAgainst: Int = 0,
        val doubleSkunksFor: Int = 0,
        val doubleSkunksAgainst: Int = 0
    )

    /**
     * Cut cards data class for dealer determination
     */
    data class CutCards(
        val playerCard: Card,
        val opponentCard: Card
    )

    /**
     * Load match statistics from preferences
     */
    fun loadMatchStats(): MatchStats {
        return MatchStats(
            gamesWon = prefs.getInt(KEY_GAMES_WON, 0),
            gamesLost = prefs.getInt(KEY_GAMES_LOST, 0),
            skunksFor = prefs.getInt(KEY_SKUNKS_FOR, 0),
            skunksAgainst = prefs.getInt(KEY_SKUNKS_AGAINST, 0),
            doubleSkunksFor = prefs.getInt(KEY_DOUBLE_SKUNKS_FOR, 0),
            doubleSkunksAgainst = prefs.getInt(KEY_DOUBLE_SKUNKS_AGAINST, 0)
        )
    }

    /**
     * Save match statistics to preferences
     */
    fun saveMatchStats(stats: MatchStats) {
        prefs.edit()
            .putInt(KEY_GAMES_WON, stats.gamesWon)
            .putInt(KEY_GAMES_LOST, stats.gamesLost)
            .putInt(KEY_SKUNKS_FOR, stats.skunksFor)
            .putInt(KEY_SKUNKS_AGAINST, stats.skunksAgainst)
            .putInt(KEY_DOUBLE_SKUNKS_FOR, stats.doubleSkunksFor)
            .putInt(KEY_DOUBLE_SKUNKS_AGAINST, stats.doubleSkunksAgainst)
            .apply()
    }

    /**
     * Load cut cards from preferences (if they exist)
     * Returns null if cut cards haven't been saved yet
     */
    fun loadCutCards(): CutCards? {
        val playerRank = prefs.getInt(KEY_CUT_PLAYER_RANK, -1)
        val playerSuit = prefs.getInt(KEY_CUT_PLAYER_SUIT, -1)
        val opponentRank = prefs.getInt(KEY_CUT_OPP_RANK, -1)
        val opponentSuit = prefs.getInt(KEY_CUT_OPP_SUIT, -1)

        return if (playerRank >= 0 && playerSuit >= 0 && opponentRank >= 0 && opponentSuit >= 0) {
            CutCards(
                playerCard = Card(Rank.entries[playerRank], Suit.entries[playerSuit]),
                opponentCard = Card(Rank.entries[opponentRank], Suit.entries[opponentSuit])
            )
        } else {
            null
        }
    }

    /**
     * Save cut cards to preferences
     */
    fun saveCutCards(cutCards: CutCards) {
        prefs.edit()
            .putInt(KEY_CUT_PLAYER_RANK, cutCards.playerCard.rank.ordinal)
            .putInt(KEY_CUT_PLAYER_SUIT, cutCards.playerCard.suit.ordinal)
            .putInt(KEY_CUT_OPP_RANK, cutCards.opponentCard.rank.ordinal)
            .putInt(KEY_CUT_OPP_SUIT, cutCards.opponentCard.suit.ordinal)
            .apply()
    }

    /**
     * Check if next dealer preference exists
     */
    fun hasNextDealerPreference(): Boolean {
        return prefs.contains(KEY_NEXT_DEALER_IS_PLAYER)
    }

    /**
     * Load next dealer preference
     * Returns true if player should be dealer, false if opponent should be dealer
     */
    fun loadNextDealerIsPlayer(): Boolean {
        return prefs.getBoolean(KEY_NEXT_DEALER_IS_PLAYER, false)
    }

    /**
     * Save next dealer preference
     */
    fun saveNextDealerIsPlayer(isPlayerDealer: Boolean) {
        prefs.edit()
            .putBoolean(KEY_NEXT_DEALER_IS_PLAYER, isPlayerDealer)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "cribbage_prefs"

        // Match statistics keys
        private const val KEY_GAMES_WON = "gamesWon"
        private const val KEY_GAMES_LOST = "gamesLost"
        private const val KEY_SKUNKS_FOR = "skunksFor"
        private const val KEY_SKUNKS_AGAINST = "skunksAgainst"
        private const val KEY_DOUBLE_SKUNKS_FOR = "doubleSkunksFor"
        private const val KEY_DOUBLE_SKUNKS_AGAINST = "doubleSkunksAgainst"

        // Cut cards keys
        private const val KEY_CUT_PLAYER_RANK = "cutPlayerRank"
        private const val KEY_CUT_PLAYER_SUIT = "cutPlayerSuit"
        private const val KEY_CUT_OPP_RANK = "cutOppRank"
        private const val KEY_CUT_OPP_SUIT = "cutOppSuit"

        // Next dealer key
        private const val KEY_NEXT_DEALER_IS_PLAYER = "nextDealerIsPlayer"
    }
}

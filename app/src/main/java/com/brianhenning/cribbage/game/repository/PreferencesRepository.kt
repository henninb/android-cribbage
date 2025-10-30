package com.brianhenning.cribbage.game.repository

import android.content.Context
import android.content.SharedPreferences
import com.brianhenning.cribbage.game.state.MatchStats
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit

/**
 * Repository for persisting game state and match statistics using SharedPreferences.
 * Abstracts SharedPreferences operations for testability.
 */
class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "cribbage_prefs"

        // Match statistics keys
        private const val KEY_GAMES_WON = "gamesWon"
        private const val KEY_GAMES_LOST = "gamesLost"
        private const val KEY_SKUNKS_FOR = "skunksFor"
        private const val KEY_SKUNKS_AGAINST = "skunksAgainst"
        private const val KEY_DOUBLE_SKUNKS_FOR = "doubleSkunksFor"
        private const val KEY_DOUBLE_SKUNKS_AGAINST = "doubleSkunksAgainst"

        // Cut card keys
        private const val KEY_CUT_PLAYER_RANK = "cutPlayerRank"
        private const val KEY_CUT_PLAYER_SUIT = "cutPlayerSuit"
        private const val KEY_CUT_OPP_RANK = "cutOppRank"
        private const val KEY_CUT_OPP_SUIT = "cutOppSuit"

        // Next dealer key
        private const val KEY_NEXT_DEALER_IS_PLAYER = "nextDealerIsPlayer"
    }

    // ========================================
    // Match Statistics
    // ========================================

    /**
     * Load match statistics from preferences.
     */
    fun getMatchStats(): MatchStats {
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
     * Save match statistics to preferences.
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

    // ========================================
    // Cut Cards (for UI display)
    // ========================================

    /**
     * Load cut cards from preferences.
     * Returns null if no cut cards are saved.
     */
    fun getCutCards(): Pair<Card?, Card?> {
        val playerRank = prefs.getInt(KEY_CUT_PLAYER_RANK, -1)
        val playerSuit = prefs.getInt(KEY_CUT_PLAYER_SUIT, -1)
        val oppRank = prefs.getInt(KEY_CUT_OPP_RANK, -1)
        val oppSuit = prefs.getInt(KEY_CUT_OPP_SUIT, -1)

        return if (playerRank >= 0 && playerSuit >= 0 && oppRank >= 0 && oppSuit >= 0) {
            val playerCard = Card(Rank.entries[playerRank], Suit.entries[playerSuit])
            val oppCard = Card(Rank.entries[oppRank], Suit.entries[oppSuit])
            Pair(playerCard, oppCard)
        } else {
            Pair(null, null)
        }
    }

    /**
     * Save cut cards to preferences for UI display.
     */
    fun saveCutCards(playerCard: Card, opponentCard: Card) {
        prefs.edit()
            .putInt(KEY_CUT_PLAYER_RANK, playerCard.rank.ordinal)
            .putInt(KEY_CUT_PLAYER_SUIT, playerCard.suit.ordinal)
            .putInt(KEY_CUT_OPP_RANK, opponentCard.rank.ordinal)
            .putInt(KEY_CUT_OPP_SUIT, opponentCard.suit.ordinal)
            .apply()
    }

    // ========================================
    // Next Dealer
    // ========================================

    /**
     * Get the next dealer preference (loser of previous game deals next).
     * Returns null if no preference is saved (first game).
     */
    fun getNextDealerIsPlayer(): Boolean? {
        return if (prefs.contains(KEY_NEXT_DEALER_IS_PLAYER)) {
            prefs.getBoolean(KEY_NEXT_DEALER_IS_PLAYER, false)
        } else {
            null
        }
    }

    /**
     * Set the next dealer preference.
     */
    fun setNextDealerIsPlayer(isPlayer: Boolean) {
        prefs.edit()
            .putBoolean(KEY_NEXT_DEALER_IS_PLAYER, isPlayer)
            .apply()
    }

    /**
     * Clear the next dealer preference (called when showing cut for dealer).
     */
    fun clearNextDealer() {
        prefs.edit()
            .remove(KEY_NEXT_DEALER_IS_PLAYER)
            .apply()
    }

    // ========================================
    // Utility
    // ========================================

    /**
     * Clear all preferences (for testing or reset).
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}

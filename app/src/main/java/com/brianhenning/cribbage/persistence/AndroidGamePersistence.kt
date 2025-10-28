package com.brianhenning.cribbage.persistence

import android.content.Context
import android.content.SharedPreferences
import com.brianhenning.cribbage.shared.domain.engine.GamePersistence
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit

/**
 * Android implementation of GamePersistence using SharedPreferences
 */
class AndroidGamePersistence(context: Context) : GamePersistence {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cribbage_prefs", Context.MODE_PRIVATE)

    override fun getGamesWon(): Int {
        return prefs.getInt("gamesWon", 0)
    }

    override fun getGamesLost(): Int {
        return prefs.getInt("gamesLost", 0)
    }

    override fun getSkunksFor(): Int {
        return prefs.getInt("skunksFor", 0)
    }

    override fun getSkunksAgainst(): Int {
        return prefs.getInt("skunksAgainst", 0)
    }

    override fun getLastCutCards(): Pair<Card, Card>? {
        val playerRank = prefs.getInt("cutPlayerRank", -1)
        val playerSuit = prefs.getInt("cutPlayerSuit", -1)
        val opponentRank = prefs.getInt("cutOppRank", -1)
        val opponentSuit = prefs.getInt("cutOppSuit", -1)

        return if (playerRank >= 0 && playerSuit >= 0 && opponentRank >= 0 && opponentSuit >= 0) {
            val playerCard = Card(Rank.entries[playerRank], Suit.entries[playerSuit])
            val opponentCard = Card(Rank.entries[opponentRank], Suit.entries[opponentSuit])
            Pair(playerCard, opponentCard)
        } else {
            null
        }
    }

    override fun saveGameStats(gamesWon: Int, gamesLost: Int, skunksFor: Int, skunksAgainst: Int) {
        prefs.edit().apply {
            putInt("gamesWon", gamesWon)
            putInt("gamesLost", gamesLost)
            putInt("skunksFor", skunksFor)
            putInt("skunksAgainst", skunksAgainst)
            apply()
        }
    }

    override fun saveLastCutCards(playerCard: Card, opponentCard: Card) {
        prefs.edit().apply {
            putInt("cutPlayerRank", playerCard.rank.ordinal)
            putInt("cutPlayerSuit", playerCard.suit.ordinal)
            putInt("cutOppRank", opponentCard.rank.ordinal)
            putInt("cutOppSuit", opponentCard.suit.ordinal)
            apply()
        }
    }
}

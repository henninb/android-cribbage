import Foundation
import shared

/**
 * iOS implementation of GamePersistence using UserDefaults
 */
class IOSGamePersistence: GamePersistence {
    private let defaults = UserDefaults.standard

    func getGamesWon() -> Int32 {
        return Int32(defaults.integer(forKey: "gamesWon"))
    }

    func getGamesLost() -> Int32 {
        return Int32(defaults.integer(forKey: "gamesLost"))
    }

    func getSkunksFor() -> Int32 {
        return Int32(defaults.integer(forKey: "skunksFor"))
    }

    func getSkunksAgainst() -> Int32 {
        return Int32(defaults.integer(forKey: "skunksAgainst"))
    }

    func getLastCutCards() -> KotlinPair<Card, Card>? {
        guard let playerRank = defaults.object(forKey: "cutPlayerRank") as? Int,
              let playerSuit = defaults.object(forKey: "cutPlayerSuit") as? Int,
              let opponentRank = defaults.object(forKey: "cutOppRank") as? Int,
              let opponentSuit = defaults.object(forKey: "cutOppSuit") as? Int,
              playerRank >= 0, playerSuit >= 0, opponentRank >= 0, opponentSuit >= 0 else {
            return nil
        }

        let ranks = Rank.entries() as! [Rank]
        let suits = Suit.entries() as! [Suit]

        let playerCard = Card(rank: ranks[playerRank], suit: suits[playerSuit])
        let opponentCard = Card(rank: ranks[opponentRank], suit: suits[opponentSuit])

        return KotlinPair(first: playerCard, second: opponentCard)
    }

    func saveGameStats(gamesWon: Int32, gamesLost: Int32, skunksFor: Int32, skunksAgainst: Int32) {
        defaults.set(Int(gamesWon), forKey: "gamesWon")
        defaults.set(Int(gamesLost), forKey: "gamesLost")
        defaults.set(Int(skunksFor), forKey: "skunksFor")
        defaults.set(Int(skunksAgainst), forKey: "skunksAgainst")
    }

    func saveLastCutCards(playerCard: Card, opponentCard: Card) {
        defaults.set(Int(playerCard.rank.ordinal), forKey: "cutPlayerRank")
        defaults.set(Int(playerCard.suit.ordinal), forKey: "cutPlayerSuit")
        defaults.set(Int(opponentCard.rank.ordinal), forKey: "cutOppRank")
        defaults.set(Int(opponentCard.suit.ordinal), forKey: "cutOppSuit")
    }
}

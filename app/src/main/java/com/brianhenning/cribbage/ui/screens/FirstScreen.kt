package com.brianhenning.cribbage.ui.screens

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.brianhenning.cribbage.R
import com.brianhenning.cribbage.ui.theme.CardBackground
import com.brianhenning.cribbage.ui.theme.SelectedCard
import kotlin.random.Random

@Composable
fun FirstScreen(navController: NavController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Log.i("CribbageGame", "FirstScreen composable is being rendered")
    }

    // Game state
    var gameStarted by remember { mutableStateOf(false) }
    var playerScore by remember { mutableStateOf(0) }
    var opponentScore by remember { mutableStateOf(0) }
    var isPlayerDealer by remember { mutableStateOf(false) }
    var playerHand by remember { mutableStateOf<List<Card>>(emptyList()) }
    var opponentHand by remember { mutableStateOf<List<Card>>(emptyList()) }
    var selectedCards by remember { mutableStateOf<Set<Int>>(emptySet()) }

    // Pegging state
    var isPeggingPhase by remember { mutableStateOf(false) }
    var isPlayerTurn by remember { mutableStateOf(false) }
    var peggingCount by remember { mutableStateOf(0) }
    var peggingPile by remember { mutableStateOf<List<Card>>(emptyList()) }
    var playerCardsPlayed by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var opponentCardsPlayed by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var consecutiveGoes by remember { mutableStateOf(0) }
    var lastPlayerWhoPlayed by remember { mutableStateOf<String?>(null) }
    var starterCard by remember { mutableStateOf<Card?>(null) }

    // UI state
    var gameStatus by remember { mutableStateOf(context.getString(R.string.welcome_to_cribbage)) }
    var dealButtonEnabled by remember { mutableStateOf(false) }
    var selectCribButtonEnabled by remember { mutableStateOf(false) }
    var playCardButtonEnabled by remember { mutableStateOf(false) }
    var goButtonEnabled by remember { mutableStateOf(false) }
    var showPeggingCount by remember { mutableStateOf(false) }

    // End sub-round: if no one can play, award a go point (if count != 31) to the last player who played,
    // then reset the count and clear played cards. The next sub-round is led by the player who did not play last.
    val endSubRound = {
        if (peggingCount != 31 && lastPlayerWhoPlayed != null) {
            if (lastPlayerWhoPlayed == "player") {
                playerScore += 1
                gameStatus += "\nGo point for You!"
                Log.i("CribbageGame", "Player awarded 1 go point")
            } else {
                opponentScore += 1
                gameStatus += "\nGo point for Opponent!"
                Log.i("CribbageGame", "Opponent awarded 1 go point")
            }
        }
        peggingCount = 0
        peggingPile = emptyList()
        playerCardsPlayed = emptySet()
        opponentCardsPlayed = emptySet()
        // The next sub-round is led by the player who did NOT play last.
        isPlayerTurn = (lastPlayerWhoPlayed != "player")
        lastPlayerWhoPlayed = null
        gameStatus += "\nNew sub-round begins. " + if (isPlayerTurn) context.getString(R.string.pegging_your_turn) else context.getString(R.string.pegging_opponent_turn)
    }

    // Expanded scoring: 15's, 31's, pairs, and runs.
    val checkPeggingScore: (Boolean, Card) -> Unit = { isPlayer, playedCard ->
        // 15's & 31's
        if (peggingCount == 15) {
            if (isPlayer) {
                playerScore += 2
                gameStatus += "\nScored 2 for 15 by You!"
            } else {
                opponentScore += 2
                gameStatus += "\nScored 2 for 15 by Opponent!"
            }
            Log.i("CribbageGame", "Scored 2 for fifteen. Count: $peggingCount")
        }
        if (peggingCount == 31) {
            if (isPlayer) {
                playerScore += 2
                gameStatus += "\nScored 2 for 31 by You!"
            } else {
                opponentScore += 2
                gameStatus += "\nScored 2 for 31 by Opponent!"
            }
            Log.i("CribbageGame", "Scored 2 for thirty-one. Count: $peggingCount")
        }
        // Pairs: count consecutive cards at tail of peggingPile.
        var sameRankCount = 1
        for (i in peggingPile.size - 2 downTo 0) {
            if (peggingPile[i].rank == playedCard.rank) {
                sameRankCount++
            } else break
        }
        when (sameRankCount) {
            2 -> {
                if (isPlayer) {
                    playerScore += 2
                    gameStatus += "\nScored 2 for a pair by You!"
                } else {
                    opponentScore += 2
                    gameStatus += "\nScored 2 for a pair by Opponent!"
                }
                Log.i("CribbageGame", "Scored 2 for a pair.")
            }
            3 -> {
                if (isPlayer) {
                    playerScore += 6
                    gameStatus += "\nScored 6 for three-of-a-kind by You!"
                } else {
                    opponentScore += 6
                    gameStatus += "\nScored 6 for three-of-a-kind by Opponent!"
                }
                Log.i("CribbageGame", "Scored 6 for three-of-a-kind.")
            }
            in 4..Int.MAX_VALUE -> {
                if (isPlayer) {
                    playerScore += 12
                    gameStatus += "\nScored 12 for four-of-a-kind by You!"
                } else {
                    opponentScore += 12
                    gameStatus += "\nScored 12 for four-of-a-kind by Opponent!"
                }
                Log.i("CribbageGame", "Scored 12 for four-of-a-kind.")
            }
        }
        // Runs: score longest run in the tail (min length 3)
        var runScore = 0
        for (runLength in peggingPile.size downTo 3) {
            val lastCards = peggingPile.takeLast(runLength)
            val ordinals = lastCards.map { it.rank.ordinal }.sorted()
            if (ordinals.zipWithNext().all { (a, b) -> b - a == 1 }) {
                runScore = runLength
                break
            }
        }
        if (runScore > 0) {
            if (isPlayer) {
                playerScore += runScore
                gameStatus += "\nScored $runScore for a run by You!"
            } else {
                opponentScore += runScore
                gameStatus += "\nScored $runScore for a run by Opponent!"
            }
            Log.i("CribbageGame", "Scored $runScore for a run.")
        }
    }

    val startNewGame = {
        Log.i("CribbageGame", "Starting new game")
        gameStarted = true
        playerScore = 0
        opponentScore = 0
        playerHand = emptyList()
        opponentHand = emptyList()
        selectedCards = emptySet()

        isPeggingPhase = false
        peggingCount = 0
        peggingPile = emptyList()
        playerCardsPlayed = emptySet()
        opponentCardsPlayed = emptySet()
        consecutiveGoes = 0
        lastPlayerWhoPlayed = null
        starterCard = null

        isPlayerDealer = Random.nextBoolean()
        Log.i("CribbageGame", "Dealer: ${if (isPlayerDealer) "Player" else "Opponent"}")

        dealButtonEnabled = true
        selectCribButtonEnabled = false
        playCardButtonEnabled = false
        goButtonEnabled = false
        showPeggingCount = false

        gameStatus = context.getString(R.string.game_started)
    }

    val dealCards = {
        Log.i("CribbageGame", "Dealing cards")
        val deck = createDeck().shuffled().toMutableList()
        playerHand = List(6) { deck.removeAt(0) }
        opponentHand = List(6) { deck.removeAt(0) }
        Log.i("CribbageGame", "Player hand: $playerHand")
        Log.i("CribbageGame", "Opponent hand: $opponentHand")
        dealButtonEnabled = false
        selectCribButtonEnabled = true
        gameStatus = context.getString(R.string.select_cards_for_crib)
    }

    val selectCardsForCrib = {
        Log.i("CribbageGame", "Select cards for crib called, selected cards: ${selectedCards.size}")
        if (selectedCards.size != 2) {
            gameStatus = context.getString(R.string.select_exactly_two)
            Log.i("CribbageGame", "Not enough cards selected for crib")
        } else {
            val selectedIndices = selectedCards.toList().sortedDescending()
            val selectedPlayerCards = selectedIndices.map { playerHand[it] }
            Log.i("CribbageGame", "Cards selected for crib: $selectedPlayerCards")
            val opponentCribCards = opponentHand.shuffled().take(2)
            Log.i("CribbageGame", "Opponent cards for crib: $opponentCribCards")
            playerHand = playerHand.filterIndexed { index, _ -> !selectedCards.contains(index) }
            opponentHand = opponentHand.filter { !opponentCribCards.contains(it) }
            selectedCards = emptySet()
            selectCribButtonEnabled = false
            gameStatus = context.getString(R.string.crib_cards_selected)

            // Reveal the cut card.
            val newDeck = createDeck().shuffled()
            starterCard = newDeck.first()
            Log.i("CribbageGame", "Cut card: ${starterCard?.getSymbol()}")
            gameStatus = "Cut card: ${starterCard?.getSymbol()}"
            // If the cut card is a Jack, award 2 points to the dealer.
            if (starterCard?.rank == Rank.JACK) {
                if (isPlayerDealer) {
                    playerScore += 2
                    gameStatus += "\nDealer gets 2 points for his heels."
                } else {
                    opponentScore += 2
                    gameStatus += "\nDealer gets 2 points for his heels."
                }
            }
            // Wait a moment before starting pegging.
            Handler(Looper.getMainLooper()).postDelayed({
                isPeggingPhase = true
                // Non-dealer leads.
                isPlayerTurn = !isPlayerDealer
                playCardButtonEnabled = true
                goButtonEnabled = false
                showPeggingCount = true
                peggingCount = 0
                gameStatus += "\nPegging phase begins. " + if (isPlayerTurn) context.getString(R.string.pegging_your_turn) else context.getString(R.string.pegging_opponent_turn)
                if (!isPlayerTurn) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        val playableCards = opponentHand.filter { card ->
                            card.getValue() + peggingCount <= 31 &&
                                    !opponentCardsPlayed.contains(opponentHand.indexOf(card))
                        }
                        if (playableCards.isNotEmpty()) {
                            val cardToPlay = playableCards.random()
                            val cardIndex = opponentHand.indexOf(cardToPlay)
                            Log.i("CribbageGame", "Opponent playing card: ${cardToPlay.getSymbol()}")
                            peggingPile = peggingPile + cardToPlay
                            opponentCardsPlayed = opponentCardsPlayed + cardIndex
                            peggingCount += cardToPlay.getValue()
                            lastPlayerWhoPlayed = "opponent"
                            Log.i("CribbageGame", "New pegging count after opponent play: $peggingCount")
                            gameStatus = "Opponent played ${cardToPlay.getSymbol()}"
                            checkPeggingScore(false, cardToPlay)
                            isPlayerTurn = true
                            gameStatus = context.getString(R.string.pegging_your_turn)
                        } else {
                            Log.i("CribbageGame", "Opponent cannot play; says GO")
                            gameStatus = "Opponent says GO!"
                            goButtonEnabled = true
                            isPlayerTurn = true
                        }
                    }, 1000)
                }
            }, 1000)
        }
    }

    val toggleCardSelection = { cardIndex: Int ->
        Log.i("CribbageGame", "Card selection toggled: $cardIndex")
        selectedCards = if (isPeggingPhase) {
            if (isPlayerTurn && !playerCardsPlayed.contains(cardIndex)) {
                val cardValue = playerHand[cardIndex].getValue()
                if (peggingCount + cardValue <= 31) {
                    Log.i("CribbageGame", "Card at index $cardIndex selected for play")
                    setOf(cardIndex)
                } else {
                    Log.i("CribbageGame", "Card at index $cardIndex cannot be played, would exceed 31")
                    gameStatus = context.getString(R.string.pegging_go)
                    emptySet()
                }
            } else selectedCards
        } else {
            if (selectedCards.contains(cardIndex)) {
                Log.i("CribbageGame", "Card at index $cardIndex deselected for crib")
                selectedCards - cardIndex
            } else if (selectedCards.size < 2) {
                Log.i("CribbageGame", "Card at index $cardIndex selected for crib")
                selectedCards + cardIndex
            } else selectedCards
        }
    }

    val playSelectedCard = {
        Log.i("CribbageGame", "Play selected card called")
        if (isPeggingPhase && isPlayerTurn && selectedCards.isNotEmpty()) {
            val cardIndex = selectedCards.first()
            Log.i("CribbageGame", "Playing card at index $cardIndex")
            if (cardIndex < playerHand.size && !playerCardsPlayed.contains(cardIndex)) {
                val playedCard = playerHand[cardIndex]
                Log.i("CribbageGame", "Player playing card: ${playedCard.getSymbol()}")
                peggingPile = peggingPile + playedCard
                playerCardsPlayed = playerCardsPlayed + cardIndex
                peggingCount += playedCard.getValue()
                lastPlayerWhoPlayed = "player"
                Log.i("CribbageGame", "New pegging count: $peggingCount")
                checkPeggingScore(true, playedCard)
                selectedCards = emptySet()
                if (peggingCount == 31) {
                    Log.i("CribbageGame", "Count reached 31; resetting to 0")
                    peggingCount = 0
                    consecutiveGoes = 0
                }
                isPlayerTurn = false
                gameStatus = context.getString(R.string.pegging_opponent_turn)
                Log.i("CribbageGame", "Switching to opponent's turn")
                Handler(Looper.getMainLooper()).postDelayed({
                    val playableCards = opponentHand.filter { card ->
                        card.getValue() + peggingCount <= 31 &&
                                !opponentCardsPlayed.contains(opponentHand.indexOf(card))
                    }
                    Log.i("CribbageGame", "Opponent has ${playableCards.size} playable cards")
                    if (playableCards.isNotEmpty()) {
                        val cardToPlay = playableCards.random()
                        val oppCardIndex = opponentHand.indexOf(cardToPlay)
                        Log.i("CribbageGame", "Opponent playing card: ${cardToPlay.getSymbol()}")
                        peggingPile = peggingPile + cardToPlay
                        opponentCardsPlayed = opponentCardsPlayed + oppCardIndex
                        peggingCount += cardToPlay.getValue()
                        lastPlayerWhoPlayed = "opponent"
                        Log.i("CribbageGame", "New pegging count after opponent play: $peggingCount")
                        gameStatus = "Opponent played ${cardToPlay.getSymbol()}"
                        checkPeggingScore(false, cardToPlay)
                        isPlayerTurn = true
                        Log.i("CribbageGame", "Switching back to player's turn")
                        val playerPlayable = playerHand.filterIndexed { index, card ->
                            !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
                        }
                        if (playerPlayable.isEmpty()) {
                            gameStatus = "No playable card. Press Go."
                            goButtonEnabled = true
                        }
                    } else {
                        Log.i("CribbageGame", "Opponent cannot play; says GO")
                        gameStatus = "Opponent says GO!"
                        goButtonEnabled = true
                        isPlayerTurn = true
                    }
                }, 1000)
            }
        }
    }

    val sayGo = {
        // Only allow GO if the player truly has no legal plays.
        val playerPlayable = playerHand.filterIndexed { index, card ->
            !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
        }
        if (playerPlayable.isNotEmpty()) {
            Log.i("CribbageGame", "Player still has playable cards, cannot say GO")
            gameStatus = "You still have playable cards; you cannot say GO."
        } else {
            Log.i("CribbageGame", "Player says GO")
            gameStatus = "You say GO!"
            Handler(Looper.getMainLooper()).postDelayed({
                val opponentPlayable = opponentHand.filter { card ->
                    card.getValue() + peggingCount <= 31 &&
                            !opponentCardsPlayed.contains(opponentHand.indexOf(card))
                }
                if (opponentPlayable.isNotEmpty()) {
                    isPlayerTurn = false
                    val cardToPlay = opponentPlayable.random()
                    val oppCardIndex = opponentHand.indexOf(cardToPlay)
                    Log.i("CribbageGame", "Opponent playing card after GO: ${cardToPlay.getSymbol()}")
                    peggingPile = peggingPile + cardToPlay
                    opponentCardsPlayed = opponentCardsPlayed + oppCardIndex
                    peggingCount += cardToPlay.getValue()
                    lastPlayerWhoPlayed = "opponent"
                    Log.i("CribbageGame", "New pegging count after opponent play: $peggingCount")
                    gameStatus = "Opponent played ${cardToPlay.getSymbol()}"
                    checkPeggingScore(false, cardToPlay)
                    isPlayerTurn = true
                    goButtonEnabled = false
                } else {
                    Log.i("CribbageGame", "Neither player can play after GO; ending sub-round")
                    endSubRound()
                }
            }, 1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game header with scores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Your Score: $playerScore",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Opponent Score: $opponentScore",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Dealer info
        Text(
            text = if (isPlayerDealer) "Dealer: You" else "Dealer: Opponent",
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        // Show cut card if available
        if (starterCard != null) {
            Text(
                text = "Cut Card: ${starterCard?.getSymbol()}",
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Game status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = gameStatus,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Pegging count
        if (showPeggingCount) {
            Text(
                text = "Count: $peggingCount",
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Player's hand
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 0 until 6) {
                val card = if (i < playerHand.size) playerHand[i] else null
                val isSelected = selectedCards.contains(i)
                val isPlayed = playerCardsPlayed.contains(i)

                Box(
                    modifier = Modifier
                        .size(60.dp, 90.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            color = if (isSelected) SelectedCard else CardBackground,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .alpha(if (isPlayed) 0.3f else 1.0f)
                        .clickable(
                            enabled = gameStarted && card != null && !isPlayed,
                            onClick = { toggleCardSelection(i) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (card != null) {
                        Image(
                            painter = painterResource(id = getCardResourceId(card)),
                            contentDescription = card.toString(),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.back_light),
                            contentDescription = "Card back",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Game control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { startNewGame() },
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(text = "Start Game")
            }

            Button(
                onClick = { dealCards() },
                enabled = dealButtonEnabled,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(text = "Deal Cards")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { selectCardsForCrib() },
                enabled = selectCribButtonEnabled,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(text = "Select for Crib")
            }

            Button(
                onClick = { playSelectedCard() },
                enabled = playCardButtonEnabled && selectedCards.isNotEmpty(),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(text = "Play Card")
            }

            Button(
                onClick = { sayGo() },
                enabled = goButtonEnabled,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(text = "Go")
            }
        }
    }
}

enum class Suit { HEARTS, DIAMONDS, CLUBS, SPADES }
enum class Rank { ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING }

data class Card(val rank: Rank, val suit: Suit) {
    fun getValue(): Int {
        return when (rank) {
            Rank.ACE -> 1
            Rank.TWO -> 2
            Rank.THREE -> 3
            Rank.FOUR -> 4
            Rank.FIVE -> 5
            Rank.SIX -> 6
            Rank.SEVEN -> 7
            Rank.EIGHT -> 8
            Rank.NINE -> 9
            Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING -> 10
        }
    }

    fun getSymbol(): String {
        val rankSymbol = when (rank) {
            Rank.ACE -> "A"
            Rank.TWO -> "2"
            Rank.THREE -> "3"
            Rank.FOUR -> "4"
            Rank.FIVE -> "5"
            Rank.SIX -> "6"
            Rank.SEVEN -> "7"
            Rank.EIGHT -> "8"
            Rank.NINE -> "9"
            Rank.TEN -> "10"
            Rank.JACK -> "J"
            Rank.QUEEN -> "Q"
            Rank.KING -> "K"
        }
        val suitSymbol = when (suit) {
            Suit.SPADES -> "♠"
            Suit.HEARTS -> "♥"
            Suit.DIAMONDS -> "♦"
            Suit.CLUBS -> "♣"
        }
        return "$rankSymbol$suitSymbol"
    }
}

fun createDeck(): List<Card> {
    val deck = mutableListOf<Card>()
    for (suit in Suit.values()) {
        for (rank in Rank.values()) {
            deck.add(Card(rank, suit))
        }
    }
    return deck
}

fun getCardResourceId(card: Card): Int {
    val suitName = when (card.suit) {
        Suit.HEARTS -> "hearts"
        Suit.DIAMONDS -> "diamonds"
        Suit.CLUBS -> "clubs"
        Suit.SPADES -> "spades"
    }
    val rankName = when (card.rank) {
        Rank.ACE -> "a"
        Rank.TWO -> "2"
        Rank.THREE -> "3"
        Rank.FOUR -> "4"
        Rank.FIVE -> "5"
        Rank.SIX -> "6"
        Rank.SEVEN -> "7"
        Rank.EIGHT -> "8"
        Rank.NINE -> "9"
        Rank.TEN -> "10"
        Rank.JACK -> "j"
        Rank.QUEEN -> "q"
        Rank.KING -> "k"
    }
    val resourceName = "${suitName}_${rankName}"
    val resourceField = R.drawable::class.java.getField(resourceName)
    return resourceField.getInt(null)
}
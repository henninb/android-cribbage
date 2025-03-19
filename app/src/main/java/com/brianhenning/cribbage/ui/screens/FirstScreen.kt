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
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import com.brianhenning.cribbage.R
import com.brianhenning.cribbage.ui.theme.CardBackground
import com.brianhenning.cribbage.ui.theme.SelectedCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun FirstScreen() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Log.i("CribbageGame", "FirstScreen composable is being rendered")
    }

    // Game state variables
    var gameStarted by remember { mutableStateOf(false) }
    var playerScore by remember { mutableIntStateOf(0) }
    var opponentScore by remember { mutableIntStateOf(0) }
    var isPlayerDealer by remember { mutableStateOf(false) }
    var playerHand by remember { mutableStateOf<List<Card>>(emptyList()) }
    var opponentHand by remember { mutableStateOf<List<Card>>(emptyList()) }
    var cribHand by remember { mutableStateOf<List<Card>>(emptyList()) }
    var selectedCards by remember { mutableStateOf<Set<Int>>(emptySet()) }

    // Pegging state variables
    var isPeggingPhase by remember { mutableStateOf(false) }
    var isPlayerTurn by remember { mutableStateOf(false) }
    var peggingCount by remember { mutableIntStateOf(0) }
    var peggingPile by remember { mutableStateOf<List<Card>>(emptyList()) }
    var peggingDisplayPile by remember { mutableStateOf<List<Card>>(emptyList()) }
    var playerCardsPlayed by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var opponentCardsPlayed by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var consecutiveGoes by remember { mutableIntStateOf(0) }
    var lastPlayerWhoPlayed by remember { mutableStateOf<String?>(null) }
    var starterCard by remember { mutableStateOf<Card?>(null) }

    // UI state variables
    var gameStatus by remember { mutableStateOf(context.getString(R.string.welcome_to_cribbage)) }
    var dealButtonEnabled by remember { mutableStateOf(false) }
    var selectCribButtonEnabled by remember { mutableStateOf(false) }
    var playCardButtonEnabled by remember { mutableStateOf(false) }
    var showPeggingCount by remember { mutableStateOf(false) }
    var showHandCountingButton by remember { mutableStateOf(false) } // New state

    // Remember a coroutine scope for the hand counting process
    val scope = rememberCoroutineScope()

    // Revised checkPeggingScore lambda with improved run scoring logic.
    val checkPeggingScore: (Boolean, Card) -> Unit = { isPlayer, playedCard ->
        // Score for 15's
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
        // Score for 31's
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
        // Score for pairs (check consecutive cards at tail)
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
        // Revised run scoring logic:
        var runScore = 0
        for (runLength in peggingPile.size downTo 3) {
            val lastCards = peggingPile.takeLast(runLength)
            val groups = lastCards.groupBy { it.rank.ordinal }
            val distinctRanks = groups.keys.sorted()
            if (distinctRanks.size < 3) continue
            val isConsecutive = distinctRanks.zipWithNext().all { (a, b) -> b - a == 1 }
            if (isConsecutive) {
                val numberOfRuns = distinctRanks.map { groups[it]?.size ?: 0 }.reduce { acc, count -> acc * count }
                runScore = distinctRanks.size * numberOfRuns
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

    // This helper checks if either player has a legal play given the remaining un-played cards.
    val checkPeggingPhaseComplete = {
        val playerLegal = playerHand.filterIndexed { index, card ->
            !playerCardsPlayed.contains(index) && (card.getValue() + peggingCount <= 31)
        }
        val opponentLegal = opponentHand.filterIndexed { index, card ->
            !opponentCardsPlayed.contains(index) && (card.getValue() + peggingCount <= 31)
        }

        if (playerLegal.isEmpty() && opponentLegal.isEmpty()) {
            isPeggingPhase = false
            gameStatus += "\nPegging phase complete. Proceed to hand scoring."
            showHandCountingButton = true
            Log.i("CribbageGame", "No legal plays remain; pegging phase ended.")
        }
    }

    // Forward declarations to allow for mutual recursion
    val resetSubRoundRef = remember { mutableStateOf<(Boolean) -> Unit>({ _ -> }) }
    val autoHandleGoRef = remember { mutableStateOf({}) }
    val playSelectedCardRef = remember { mutableStateOf({}) }

    resetSubRoundRef.value = resetFn@ { resetFor31 ->
        Log.i("CribbageGame", "Resetting sub-round (resetFor31=$resetFor31, lastPlayerWhoPlayed=$lastPlayerWhoPlayed)")

        if (!resetFor31 && lastPlayerWhoPlayed != null) {
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
        consecutiveGoes = 0

        isPlayerTurn = (lastPlayerWhoPlayed != "player")
        lastPlayerWhoPlayed = null

        gameStatus += "\nNew sub-round begins. " + if (isPlayerTurn)
            context.getString(R.string.pegging_your_turn)
        else
            context.getString(R.string.pegging_opponent_turn)

        checkPeggingPhaseComplete()

        if (!isPeggingPhase) {
            return@resetFn
        }

        if (isPlayerTurn) {
            playCardButtonEnabled = true
            val playerPlayable = playerHand.filterIndexed { index, card ->
                !playerCardsPlayed.contains(index) && (card.getValue() <= 31)
            }
            if (playerPlayable.isEmpty()) {
                Log.i("CribbageGame", "Player has no legal moves at start of new sub-round")
                Handler(Looper.getMainLooper()).postDelayed({
                    autoHandleGoRef.value()
                }, 500)
            }
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                val chosen = chooseSmartOpponentCard(opponentHand, opponentCardsPlayed, peggingCount, peggingPile)
                if (chosen != null) {
                    val (cardIndex, cardToPlay) = chosen
                    Log.i("CribbageGame", "Opponent playing card in new sub-round: ${cardToPlay.getSymbol()}")
                    peggingPile = peggingPile + cardToPlay
                    peggingDisplayPile = peggingDisplayPile + cardToPlay
                    opponentCardsPlayed = opponentCardsPlayed + cardIndex
                    peggingCount += cardToPlay.getValue()
                    lastPlayerWhoPlayed = "opponent"

                    Log.i("CribbageGame", "New pegging count after opponent play in new sub-round: $peggingCount")
                    gameStatus = "Opponent played ${cardToPlay.getSymbol()}"
                    checkPeggingScore(false, cardToPlay)

                    if (peggingCount == 31) {
                        resetSubRoundRef.value(true)
                    } else {
                        consecutiveGoes = 0
                        isPlayerTurn = true
                        val playerPlayable = playerHand.filterIndexed { index, card ->
                            !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
                        }
                        if (playerPlayable.isEmpty()) {
                            autoHandleGoRef.value()
                        } else {
                            playCardButtonEnabled = true
                            gameStatus += "\n${context.getString(R.string.pegging_your_turn)}"
                        }
                    }
                } else {
                    Log.i("CribbageGame", "Opponent has no legal moves at start of new sub-round")
                    autoHandleGoRef.value()
                }
            }, 1000)
        }
    }

    // Revised autoHandleGoRef with improved GO logic.
    autoHandleGoRef.value = letUnit@{
        consecutiveGoes++
        val currentPlayer = if (isPlayerTurn) "player" else "opponent"
        gameStatus = if (isPlayerTurn) "You say GO!" else "Opponent says GO!"
        Log.i("CribbageGame", "Auto-handling GO for $currentPlayer")

        // Check if the opponent (the non-active player) has any legal moves.
        val opponentLegalMoves = if (isPlayerTurn) {
            opponentHand.filterIndexed { index, card ->
                !opponentCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
            }
        } else {
            playerHand.filterIndexed { index, card ->
                !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
            }
        }
        if (opponentLegalMoves.isEmpty()) {
            Log.i("CribbageGame", "Opponent has no legal moves after GO, awarding go point.")
            resetSubRoundRef.value(false)
            return@letUnit
        }

        if (consecutiveGoes >= 2) {
            Log.i("CribbageGame", "Both players said GO, resetting sub-round")
            resetSubRoundRef.value(false)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                isPlayerTurn = !isPlayerTurn
                // Now check if the new current player has legal moves.
                val currentLegalMoves = if (isPlayerTurn) {
                    playerHand.filterIndexed { index, card ->
                        !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
                    }
                } else {
                    opponentHand.filterIndexed { index, card ->
                        !opponentCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
                    }
                }
                if (currentLegalMoves.isEmpty()) {
                    Log.i("CribbageGame", "Current player has no legal moves after switching turn, auto-handling GO again")
                    autoHandleGoRef.value()
                } else {
                    if (isPlayerTurn) {
                        playCardButtonEnabled = true
                        gameStatus += "\n${context.getString(R.string.pegging_your_turn)}"
                        Log.i("CribbageGame", "Player's turn after GO - ${currentLegalMoves.size} legal moves")
                    } else {
                        // Opponent's turn: automatically choose and play a card.
                        val chosen = chooseSmartOpponentCard(opponentHand, opponentCardsPlayed, peggingCount, peggingPile)
                        if (chosen != null) {
                            val (oppCardIndex, cardToPlay) = chosen
                            Log.i("CribbageGame", "Opponent playing card after GO: ${cardToPlay.getSymbol()}")
                            peggingPile = peggingPile + cardToPlay
                            peggingDisplayPile = peggingDisplayPile + cardToPlay
                            opponentCardsPlayed = opponentCardsPlayed + oppCardIndex
                            peggingCount += cardToPlay.getValue()
                            lastPlayerWhoPlayed = "opponent"
                            Log.i("CribbageGame", "New pegging count after opponent play: $peggingCount")
                            gameStatus = "Opponent played ${cardToPlay.getSymbol()}"
                            checkPeggingScore(false, cardToPlay)
                            if (peggingCount == 31) {
                                resetSubRoundRef.value(true)
                            } else {
                                consecutiveGoes = 0
                                isPlayerTurn = true
                                val playerPlayableAfter = playerHand.filterIndexed { index, card ->
                                    !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
                                }
                                if (playerPlayableAfter.isEmpty()) {
                                    autoHandleGoRef.value()
                                } else {
                                    playCardButtonEnabled = true
                                    gameStatus += "\n${context.getString(R.string.pegging_your_turn)}"
                                }
                            }
                        } else {
                            Log.i("CribbageGame", "Opponent has no legal moves after GO")
                            autoHandleGoRef.value()
                        }
                    }
                }
            }, 1000)
        }
    }

    // Revised card selection behavior with auto-play during pegging phase
    val toggleCardSelection = { cardIndex: Int ->
        Log.i("CribbageGame", "Card selection toggled: $cardIndex")
        if (isPeggingPhase) {
            if (isPlayerTurn && !playerCardsPlayed.contains(cardIndex)) {
                val cardToPlay = playerHand[cardIndex]
                if (peggingCount + cardToPlay.getValue() <= 31) {
                    selectedCards = setOf(cardIndex)
                    Handler(Looper.getMainLooper()).postDelayed({
                        playSelectedCardRef.value()
                    }, 300)
                } else {
                    gameStatus = "Illegal move: ${cardToPlay.getSymbol()} would exceed 31."
                }
            }
        } else {
            selectedCards = if (selectedCards.contains(cardIndex)) {
                selectedCards - cardIndex
            } else if (selectedCards.size < 2) {
                selectedCards + cardIndex
            } else {
                selectedCards
            }
        }
    }

    // New endGame lambda to reset game state
    val endGame = {
        Log.i("CribbageGame", "Ending game")
        gameStarted = false
        playerScore = 0
        opponentScore = 0
        playerHand = emptyList()
        opponentHand = emptyList()
        cribHand = emptyList()
        selectedCards = emptySet()

        isPeggingPhase = false
        peggingCount = 0
        peggingPile = emptyList()
        peggingDisplayPile = emptyList()
        consecutiveGoes = 0
        lastPlayerWhoPlayed = null
        starterCard = null

        dealButtonEnabled = false
        selectCribButtonEnabled = false
        playCardButtonEnabled = false
        showPeggingCount = false
        showHandCountingButton = false

        gameStatus = context.getString(R.string.welcome_to_cribbage)
    }

    val startNewGame = {
        Log.i("CribbageGame", "Starting new game")
        gameStarted = true
        playerScore = 0
        opponentScore = 0
        playerHand = emptyList()
        opponentHand = emptyList()
        cribHand = emptyList()
        selectedCards = emptySet()

        isPeggingPhase = false
        peggingCount = 0
        peggingPile = emptyList()
        peggingDisplayPile = emptyList()
        consecutiveGoes = 0
        lastPlayerWhoPlayed = null
        starterCard = null

        isPlayerDealer = Random.nextBoolean()
        Log.i("CribbageGame", "Dealer: ${if (isPlayerDealer) "Player" else "Opponent"}")

        dealButtonEnabled = true
        selectCribButtonEnabled = false
        playCardButtonEnabled = false
        showPeggingCount = false
        showHandCountingButton = false

        gameStatus = context.getString(R.string.game_started)
    }

    val dealCards = {
        Log.i("CribbageGame", "Dealing cards")
        val deck = createDeck().shuffled().toMutableList()
        playerHand = List(6) { deck.removeAt(0) }
        playerHand = playerHand.sortedWith(compareBy({ it.rank.ordinal }, { it.suit.ordinal }))
        opponentHand = List(6) { deck.removeAt(0) }
        Log.i("CribbageGame", "Player hand: $playerHand")
        Log.i("CribbageGame", "Opponent hand: $opponentHand")
        playerCardsPlayed = emptySet()
        opponentCardsPlayed = emptySet()
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
            // Save the crib hand (combining player’s selections and opponent’s crib cards)
            cribHand = selectedPlayerCards + opponentCribCards

            playerHand = playerHand.filterIndexed { index, _ -> !selectedCards.contains(index) }
            playerHand = playerHand.sortedWith(compareBy({ it.rank.ordinal }, { it.suit.ordinal }))
            opponentHand = opponentHand.filter { !opponentCribCards.contains(it) }
                .sortedWith(compareBy({ it.rank.ordinal }, { it.suit.ordinal }))
            selectedCards = emptySet()
            selectCribButtonEnabled = false
            gameStatus = context.getString(R.string.crib_cards_selected)

            val newDeck = createDeck().shuffled()
            starterCard = newDeck.first()
            Log.i("CribbageGame", "Cut card: ${starterCard?.getSymbol()}")
            gameStatus = "Cut card: ${starterCard?.getSymbol()}"
            if (starterCard?.rank == Rank.JACK) {
                if (isPlayerDealer) {
                    playerScore += 2
                    gameStatus += "\nDealer gets 2 points for his heels."
                } else {
                    opponentScore += 2
                    gameStatus += "\nDealer gets 2 points for his heels."
                }
            }
            Handler(Looper.getMainLooper()).postDelayed({
                isPeggingPhase = true
                isPlayerTurn = !isPlayerDealer
                playCardButtonEnabled = true
                showPeggingCount = true
                peggingCount = 0
                // Once pegging starts, hide the crib selection button.
                selectCribButtonEnabled = false
                gameStatus += "\nPegging phase begins. " + if (isPlayerTurn)
                    context.getString(R.string.pegging_your_turn)
                else
                    context.getString(R.string.pegging_opponent_turn)
                if (!isPlayerTurn) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        val chosen = chooseSmartOpponentCard(opponentHand, opponentCardsPlayed, peggingCount, peggingPile)
                        if (chosen != null) {
                            val (cardIndex, cardToPlay) = chosen
                            Log.i("CribbageGame", "Opponent playing card: ${cardToPlay.getSymbol()}")
                            peggingPile = peggingPile + cardToPlay
                            peggingDisplayPile = peggingDisplayPile + cardToPlay
                            opponentCardsPlayed = opponentCardsPlayed + cardIndex
                            peggingCount += cardToPlay.getValue()
                            lastPlayerWhoPlayed = "opponent"
                            Log.i("CribbageGame", "New pegging count after opponent play: $peggingCount")
                            gameStatus = "Opponent played ${cardToPlay.getSymbol()}"
                            checkPeggingScore(false, cardToPlay)
                            if (peggingCount == 31) {
                                resetSubRoundRef.value(true)
                            } else {
                                consecutiveGoes = 0
                                isPlayerTurn = true
                                val playerPlayable = playerHand.filterIndexed { index, card ->
                                    !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
                                }
                                if (playerPlayable.isEmpty()) {
                                    autoHandleGoRef.value()
                                } else {
                                    playCardButtonEnabled = true
                                }
                            }
                        } else {
                            isPlayerTurn = true
                            autoHandleGoRef.value()
                        }
                    }, 1000)
                }
            }, 1000)
        }
    }

    playSelectedCardRef.value = {
        Log.i("CribbageGame", "Play selected card called")
        if (isPeggingPhase && isPlayerTurn && selectedCards.isNotEmpty()) {
            val cardIndex = selectedCards.first()
            Log.i("CribbageGame", "Playing card at index $cardIndex")
            if (cardIndex < playerHand.size && !playerCardsPlayed.contains(cardIndex)) {
                val playedCard = playerHand[cardIndex]
                Log.i("CribbageGame", "Player playing card: ${playedCard.getSymbol()}")
                if (peggingCount + playedCard.getValue() <= 31) {
                    peggingPile = peggingPile + playedCard
                    peggingDisplayPile = peggingDisplayPile + playedCard
                    playerCardsPlayed = playerCardsPlayed + cardIndex
                    peggingCount += playedCard.getValue()
                    lastPlayerWhoPlayed = "player"
                    Log.i("CribbageGame", "New pegging count: $peggingCount")
                    gameStatus = "You played ${playedCard.getSymbol()}"
                    checkPeggingScore(true, playedCard)
                    selectedCards = emptySet()
                    if (peggingCount == 31) {
                        Log.i("CribbageGame", "Count reached 31 after player's play")
                        resetSubRoundRef.value(true)
                    } else {
                        isPlayerTurn = false
                        playCardButtonEnabled = false
                        gameStatus += "\n${context.getString(R.string.pegging_opponent_turn)}"
                        Log.i("CribbageGame", "Switching to opponent's turn")
                        Handler(Looper.getMainLooper()).postDelayed({
                            val chosen = chooseSmartOpponentCard(opponentHand, opponentCardsPlayed, peggingCount, peggingPile)
                            if (chosen != null) {
                                val (oppCardIndex, cardToPlay) = chosen
                                Log.i("CribbageGame", "Opponent playing card: ${cardToPlay.getSymbol()}")
                                peggingPile = peggingPile + cardToPlay
                                peggingDisplayPile = peggingDisplayPile + cardToPlay
                                opponentCardsPlayed = opponentCardsPlayed + oppCardIndex
                                peggingCount += cardToPlay.getValue()
                                lastPlayerWhoPlayed = "opponent"
                                Log.i("CribbageGame", "New pegging count after opponent play: $peggingCount")
                                gameStatus = "Opponent played ${cardToPlay.getSymbol()}"
                                checkPeggingScore(false, cardToPlay)
                                if (peggingCount == 31) {
                                    resetSubRoundRef.value(true)
                                } else {
                                    consecutiveGoes = 0
                                    isPlayerTurn = true
                                    val playerPlayable = playerHand.filterIndexed { index, card ->
                                        !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
                                    }
                                    if (playerPlayable.isEmpty()) {
                                        autoHandleGoRef.value()
                                    } else {
                                        playCardButtonEnabled = true
                                        gameStatus += "\n${context.getString(R.string.pegging_your_turn)}"
                                    }
                                }
                            } else {
                                isPlayerTurn = true
                                autoHandleGoRef.value()
                            }
                        }, 1000)
                    }
                } else {
                    gameStatus = "Illegal move: ${playedCard.getSymbol()} would exceed 31."
                    Log.i("CribbageGame", "Illegal play attempted: count $peggingCount, card value ${playedCard.getValue()}")
                }
            }
        }
    }

    // Function to count a cribbage hand (hand + starter) and return the score along with a breakdown.
    fun countHandScore(hand: List<Card>, starter: Card, isCrib: Boolean = false): Pair<Int, String> {
        val allCards = hand + starter
        var score = 0
        val breakdown = StringBuilder()

        // Count 15's: iterate through all non-empty subsets of the 5 cards.
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
                if (sum == 15) {
                    fifteens++
                }
            }
            return fifteens
        }
        val fifteens = countFifteens()
        if (fifteens > 0) {
            val points = fifteens * 2
            score += points
            breakdown.append("15's: $points points ($fifteens combinations)\n")
        }

        // Count pairs
        val rankCounts = allCards.groupingBy { it.rank }.eachCount()
        var pairPoints = 0
        for ((_, count) in rankCounts) {
            if (count >= 2) {
                val pairs = (count * (count - 1)) / 2
                pairPoints += pairs * 2
            }
        }
        if (pairPoints > 0) {
            score += pairPoints
            breakdown.append("Pairs: $pairPoints points\n")
        }

        // Count runs using a frequency map of rank ordinals.
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

        // Count flush.
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

        // Count his nobs.
        if (hand.any { it.rank == Rank.JACK && it.suit == starter.suit }) {
            score += 1
            breakdown.append("His Nobs: 1 point\n")
        }

        return Pair(score, breakdown.toString())
    }

    // Hand counting process triggered by the Hand Counting button.
    // It counts the non-dealer hand, then the dealer hand, then the crib, pausing between each.
    val countHands = {
        scope.launch {
            // When hand counting starts, reset the pegging pile and hide it.
            isPeggingPhase = false
            peggingPile = emptyList()
            peggingDisplayPile = emptyList()
            showPeggingCount = false
            showHandCountingButton = false

            if (starterCard == null) {
                gameStatus = "Starter card not set. Cannot count hands."
                return@launch
            }
            // Determine which hand is non-dealer versus dealer.
            val nonDealerHand: List<Card>
            val dealerHand: List<Card>
            if (isPlayerDealer) {
                dealerHand = playerHand
                nonDealerHand = opponentHand
            } else {
                dealerHand = opponentHand
                nonDealerHand = playerHand
            }
            gameStatus = "Counting non-dealer hand..."
            val (nonDealerScore, nonDealerBreakdown) = countHandScore(nonDealerHand, starterCard!!)
            gameStatus += "\nNon-Dealer Hand Score: $nonDealerScore\n$nonDealerBreakdown"
            // Tally score for non-dealer.
            if (isPlayerDealer) {
                opponentScore += nonDealerScore
            } else {
                playerScore += nonDealerScore
            }
            delay(3000)

            gameStatus = "Counting dealer hand..."
            val (dealerScoreValue, dealerBreakdown) = countHandScore(dealerHand, starterCard!!)
            gameStatus += "\nDealer Hand Score: $dealerScoreValue\n$dealerBreakdown"
            if (isPlayerDealer) {
                playerScore += dealerScoreValue
            } else {
                opponentScore += dealerScoreValue
            }
            delay(3000)

            gameStatus = "Counting crib hand..."
            val (cribScoreValue, cribBreakdown) = countHandScore(cribHand, starterCard!!, isCrib = true)
            gameStatus += "\nCrib Hand Score: $cribScoreValue\n$cribBreakdown"
            // The crib always belongs to the dealer.
            if (isPlayerDealer) {
                playerScore += cribScoreValue
            } else {
                opponentScore += cribScoreValue
            }
            delay(3000)

            gameStatus += "\nHand counting complete."
            // Hide the Hand Counting button after counting is complete.

            // Toggle dealer for next round just before showing the deal button again.
            isPlayerDealer = !isPlayerDealer
            gameStatus += "\nNew round: " + if (isPlayerDealer) "You are now the dealer." else "Opponent is now the dealer."
            dealButtonEnabled = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game header with scores.
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

        // Dealer info.
        Text(
            text = if (isPlayerDealer) "Dealer: You" else "Dealer: Opponent",
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        // Show cut card if available.
        if (starterCard != null) {
            Text(
                text = "Cut Card: ${starterCard?.getSymbol()}",
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Game status.
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

        // Pegging count.
        if (showPeggingCount) {
            Text(
                text = "Count: $peggingCount",
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Adjusted pegging display pile view (cards overlay horizontally without vertical staggering).
        if (showPeggingCount && peggingDisplayPile.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                peggingDisplayPile.forEachIndexed { index, card ->
                    val offsetX = index * 30.dp
                    val offsetY = 0.dp
                    Box(
                        modifier = Modifier
                            .offset(x = offsetX, y = offsetY)
                            .zIndex((peggingDisplayPile.size - index).toFloat())
                            .size(60.dp, 90.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(CardBackground, shape = RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            painter = painterResource(id = getCardResourceId(card)),
                            contentDescription = card.toString(),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Show the undealt deck before the deal button is clicked.
        if (gameStarted && dealButtonEnabled) {
            Box(modifier = Modifier.padding(8.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.back_dark),
                    contentDescription = "Un dealt Deck",
                    modifier = Modifier.size(60.dp, 90.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display player's hand.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in playerHand.indices) {
                val card = playerHand[i]
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
                            enabled = gameStarted && !isPlayed,
                            onClick = { toggleCardSelection(i) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = getCardResourceId(card)),
                        contentDescription = card.toString(),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Game control buttons.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (!gameStarted) {
                Button(
                    onClick = { startNewGame() },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(text = "Start Game")
                }
            } else {
                Button(
                    onClick = { endGame() },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(text = "End Game")
                }
            }
            // Only show the Deal Cards button when appropriate.
            if (dealButtonEnabled) {
                Button(
                    onClick = { dealCards() },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(text = "Deal Cards")
                }
            }
        }
        // Second row for crib selection / hand counting.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (!isPeggingPhase && selectCribButtonEnabled) {
                Button(
                    onClick = { selectCardsForCrib() },
                    enabled = selectCribButtonEnabled,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(text = "Select for Crib")
                }
            }
            if (showHandCountingButton) {
                Button(
                    onClick = { countHands() },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(text = "Hand Counting")
                }
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
    for (suit in Suit.entries) {
        for (rank in Rank.entries) {
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

/**
 * Helper function for choosing a smart card for the opponent during pegging.
 */
fun chooseSmartOpponentCard(
    hand: List<Card>,
    playedIndices: Set<Int>,
    currentCount: Int,
    peggingPile: List<Card>
): Pair<Int, Card>? {
    val legalMoves = hand.withIndex().filter { (index, card) ->
        !playedIndices.contains(index) && (currentCount + card.getValue() <= 31)
    }
    if (legalMoves.isEmpty()) return null

    fun evaluateMove(card: Card): Int {
        var score = 0
        val newCount = currentCount + card.getValue()
        if (newCount == 15) score += 100
        if (newCount == 31) score += 100
        if (peggingPile.isNotEmpty() && peggingPile.last().rank == card.rank) {
            score += 50
            if (peggingPile.size >= 2 && peggingPile[peggingPile.size - 2].rank == card.rank) {
                score += 50
            }
        }
        val newPile = peggingPile + card
        for (runLength in minOf(newPile.size, 7) downTo 3) {
            val lastCards = newPile.takeLast(runLength)
            val ranks = lastCards.map { it.rank.ordinal }
            val distinctRanks = ranks.distinct().sorted()
            if (distinctRanks.size == runLength &&
                distinctRanks.zipWithNext().all { (a, b) -> b - a == 1 }) {
                score += runLength * 10
                break
            }
        }
        if (newCount >= 25) score += 20
        if (currentCount < 10) score += card.getValue()
        return score
    }

    val bestMove = legalMoves.maxByOrNull { (_, card) -> evaluateMove(card) }
    return bestMove?.let { Pair(it.index, it.value) }
}

package com.brianhenning.cribbage.ui.screens

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.content.Context
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brianhenning.cribbage.R
import com.brianhenning.cribbage.ui.composables.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.brianhenning.cribbage.logic.dealSixToEach
import com.brianhenning.cribbage.logic.dealerFromCut
import com.brianhenning.cribbage.logic.CribbageScorer
import com.brianhenning.cribbage.logic.PeggingScorer
import com.brianhenning.cribbage.logic.PeggingRoundManager
import com.brianhenning.cribbage.logic.Player
import com.brianhenning.cribbage.logic.SubRoundReset

private const val TAG = "CribbageGame"

@Composable
fun FirstScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Game state variables
    var gameStarted by remember { mutableStateOf(false) }
    var playerScore by remember { mutableIntStateOf(0) }
    var opponentScore by remember { mutableIntStateOf(0) }
    var isPlayerDealer by remember { mutableStateOf(false) }
    var playerHand by remember { mutableStateOf<List<Card>>(emptyList()) }
    var opponentHand by remember { mutableStateOf<List<Card>>(emptyList()) }
    var cribHand by remember { mutableStateOf<List<Card>>(emptyList()) }
    var selectedCards by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var drawDeck by remember { mutableStateOf<List<Card>>(emptyList()) }
    var gamesWon by remember { mutableIntStateOf(0) }
    var gamesLost by remember { mutableIntStateOf(0) }
    var skunksFor by remember { mutableIntStateOf(0) }
    var skunksAgainst by remember { mutableIntStateOf(0) }
    var cutPlayerCard by remember { mutableStateOf<Card?>(null) }
    var cutOpponentCard by remember { mutableStateOf<Card?>(null) }

    LaunchedEffect(Unit) {
        Log.i(TAG, "FirstScreen composable is being rendered")
        val prefs = context.getSharedPreferences("cribbage_prefs", Context.MODE_PRIVATE)
        gamesWon = prefs.getInt("gamesWon", 0)
        gamesLost = prefs.getInt("gamesLost", 0)
        skunksFor = prefs.getInt("skunksFor", 0)
        skunksAgainst = prefs.getInt("skunksAgainst", 0)
        val cpr = prefs.getInt("cutPlayerRank", -1)
        val cps = prefs.getInt("cutPlayerSuit", -1)
        val cor = prefs.getInt("cutOppRank", -1)
        val cos = prefs.getInt("cutOppSuit", -1)
        if (cpr >= 0 && cps >= 0 && cor >= 0 && cos >= 0) {
            cutPlayerCard = Card(Rank.entries[cpr], Suit.entries[cps])
            cutOpponentCard = Card(Rank.entries[cor], Suit.entries[cos])
        }
    }

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
    var peggingManager by remember { mutableStateOf<PeggingRoundManager?>(null) }

    // UI state variables
    var gameStatus by remember { mutableStateOf(context.getString(R.string.welcome_to_cribbage)) }
    var currentPhase by remember { mutableStateOf(GamePhase.SETUP) }
    var dealButtonEnabled by remember { mutableStateOf(false) }
    var selectCribButtonEnabled by remember { mutableStateOf(false) }
    var playCardButtonEnabled by remember { mutableStateOf(false) }
    var showHandCountingButton by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    
    // Hand counting state
    var isInHandCountingPhase by remember { mutableStateOf(false) }
    var countingPhase by remember { mutableStateOf(CountingPhase.NONE) }
    var handScores by remember { mutableStateOf(HandScores()) }

    // Check game over function: if either score goes past 120, end the game.
    val checkGameOverFunction: () -> Unit = {
        if (playerScore > 120 || opponentScore > 120) {
            gameOver = true
            val playerWins = playerScore > opponentScore
            val winner = if (playerWins) "You" else "Opponent"
            val loserScore = if (playerWins) opponentScore else playerScore
            val skunked = loserScore < 61

            if (playerWins) {
                gamesWon += 1
                if (skunked) skunksFor += 1
            } else {
                gamesLost += 1
                if (skunked) skunksAgainst += 1
            }

            // Persist match stats and next dealer (loser deals next)
            val prefs = context.getSharedPreferences("cribbage_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putInt("gamesWon", gamesWon)
                .putInt("gamesLost", gamesLost)
                .putInt("skunksFor", skunksFor)
                .putInt("skunksAgainst", skunksAgainst)
                .putBoolean("nextDealerIsPlayer", !playerWins)
                .apply()

            gameStatus += "\nGame Over: $winner wins!" + (if (skunked) " Skunk!" else "") +
                "\nMatch: ${gamesWon}-${gamesLost} (Skunks ${skunksFor}-${skunksAgainst})"
            // Hide the cut card.
            starterCard = null
            // Disable game actions.
            dealButtonEnabled = false
            selectCribButtonEnabled = false
            playCardButtonEnabled = false
            showHandCountingButton = false
            showPeggingCount = false
        }
    }

    // Remember a coroutine scope for the hand counting process
    val scope = rememberCoroutineScope()

    // Forward declarations for mutual recursion across helpers
    val autoHandleGoRef = remember { mutableStateOf({}) }
    val playSelectedCardRef = remember { mutableStateOf({}) }

    // Pegging scoring: delegate to PeggingScorer so UI and tests share logic.
    val checkPeggingScore: (Boolean, Card) -> Unit = { isPlayer, _ ->
        val pts = PeggingScorer.pointsForPile(peggingPile, peggingCount)
        var awarded = 0
        fun award(points: Int) {
            if (points <= 0) return
            awarded += points
            if (isPlayer) playerScore += points else opponentScore += points
        }

        if (pts.fifteen > 0) {
            award(pts.fifteen)
            gameStatus += if (isPlayer) "\nScored 2 for 15 by You!" else "\nScored 2 for 15 by Opponent!"
            Log.i(TAG, "Scored 2 for fifteen. Count: $peggingCount")
        }
        if (pts.thirtyOne > 0) {
            award(pts.thirtyOne)
            gameStatus += if (isPlayer) "\nScored 2 for 31 by You!" else "\nScored 2 for 31 by Opponent!"
            Log.i(TAG, "Scored 2 for thirty-one. Count: $peggingCount")
        }
        if (pts.pairPoints > 0) {
            award(pts.pairPoints)
            val msg = when (pts.sameRankCount) {
                2 -> "2 for a pair"
                3 -> "6 for three-of-a-kind"
                else -> "12 for four-of-a-kind"
            }
            gameStatus += if (isPlayer) "\nScored $msg by You!" else "\nScored $msg by Opponent!"
            Log.i(TAG, "Scored ${pts.pairPoints} for ${pts.sameRankCount} of a kind.")
        }
        if (pts.runPoints > 0) {
            award(pts.runPoints)
            gameStatus += if (isPlayer) "\nScored ${pts.runPoints} for a run by You!" else "\nScored ${pts.runPoints} for a run by Opponent!"
            Log.i(TAG, "Scored ${pts.runPoints} for a run.")
        }
        if (awarded > 0) {
            checkGameOverFunction()
        }
    }

    fun applyManagerStateToUi() {
        val mgr = peggingManager ?: return
        peggingCount = mgr.peggingCount
        peggingPile = mgr.peggingPile.toList()
        isPlayerTurn = (mgr.isPlayerTurn == Player.PLAYER)
        consecutiveGoes = mgr.consecutiveGoes
        lastPlayerWhoPlayed = when (mgr.lastPlayerWhoPlayed) {
            Player.PLAYER -> "player"
            Player.OPPONENT -> "opponent"
            else -> null
        }
    }

    fun applyManagerReset(reset: SubRoundReset) {
        if (!reset.resetFor31) {
            when (reset.goPointTo) {
                Player.PLAYER -> {
                    playerScore += 1
                    gameStatus += "\nGo point for You!"
                }
                Player.OPPONENT -> {
                    opponentScore += 1
                    gameStatus += "\nGo point for Opponent!"
                }
                else -> {}
            }
            checkGameOverFunction()
        }

        // Manager already cleared; mirror to UI and visuals
        peggingDisplayPile = emptyList()
        applyManagerStateToUi()

        gameStatus += "\nNew sub-round begins. " + if (isPlayerTurn)
            context.getString(R.string.pegging_your_turn)
        else
            context.getString(R.string.pegging_opponent_turn)

        // End of pegging if no legal moves for either side
        val playerLegal = playerHand.filterIndexed { index, card ->
            !playerCardsPlayed.contains(index) && (card.getValue() + peggingCount <= 31)
        }
        val opponentLegal = opponentHand.filterIndexed { index, card ->
            !opponentCardsPlayed.contains(index) && (card.getValue() + peggingCount <= 31)
        }
        if (playerLegal.isEmpty() && opponentLegal.isEmpty()) {
            isPeggingPhase = false
            currentPhase = GamePhase.HAND_COUNTING
            gameStatus += "\nPegging phase complete. Proceed to hand scoring."
            showHandCountingButton = true
            return
        }

        if (isPlayerTurn) {
            playCardButtonEnabled = true
            if (playerLegal.isEmpty()) {
                Handler(Looper.getMainLooper()).postDelayed({ autoHandleGoRef.value() }, 500)
            }
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                val chosen = chooseSmartOpponentCard(opponentHand, opponentCardsPlayed, peggingCount, peggingPile)
                if (chosen != null) {
                    val (cardIndex, cardToPlay) = chosen
                    val mgr = peggingManager!!
                    val outcome = mgr.onPlay(cardToPlay)
                    opponentCardsPlayed = opponentCardsPlayed + cardIndex
                    peggingDisplayPile = peggingDisplayPile + cardToPlay
                    gameStatus = context.getString(R.string.played_opponent, cardToPlay.getSymbol())
                    applyManagerStateToUi()
                    checkPeggingScore(false, cardToPlay)
                    if (outcome.reset != null) {
                        applyManagerReset(outcome.reset)
                    } else {
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
                    autoHandleGoRef.value()
                }
            }, 1000)
        }
    }

    

    // Forward declarations to allow for mutual recursion (already defined above)


    // Revised autoHandleGoRef to delegate to PeggingRoundManager
    autoHandleGoRef.value = letUnit@{
        val mgr = peggingManager ?: return@letUnit
        val currentPlayerIsPlayer = (mgr.isPlayerTurn == Player.PLAYER)
        gameStatus = if (currentPlayerIsPlayer) context.getString(R.string.pegging_you_say_go) else context.getString(R.string.pegging_opponent_says_go)

        val opponentLegalMoves = if (currentPlayerIsPlayer) {
            opponentHand.filterIndexed { index, card ->
                !opponentCardsPlayed.contains(index) && (mgr.peggingCount + card.getValue() <= 31)
            }
        } else {
            playerHand.filterIndexed { index, card ->
                !playerCardsPlayed.contains(index) && (mgr.peggingCount + card.getValue() <= 31)
            }
        }

        val reset = mgr.onGo(opponentHasLegalMove = opponentLegalMoves.isNotEmpty())
        applyManagerStateToUi()

        if (reset != null) {
            applyManagerReset(reset)
            return@letUnit
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val nowPlayerTurn = (mgr.isPlayerTurn == Player.PLAYER)
            val currentLegalMoves = if (nowPlayerTurn) {
                playerHand.filterIndexed { index, card ->
                    !playerCardsPlayed.contains(index) && (mgr.peggingCount + card.getValue() <= 31)
                }
            } else {
                opponentHand.filterIndexed { index, card ->
                    !opponentCardsPlayed.contains(index) && (mgr.peggingCount + card.getValue() <= 31)
                }
            }

            if (currentLegalMoves.isEmpty()) {
                autoHandleGoRef.value()
            } else {
                if (nowPlayerTurn) {
                    playCardButtonEnabled = true
                    gameStatus += "\n${context.getString(R.string.pegging_your_turn)}"
                } else {
                    val chosen = chooseSmartOpponentCard(opponentHand, opponentCardsPlayed, peggingCount, peggingPile)
                    if (chosen != null) {
                        val (oppCardIndex, cardToPlay) = chosen
                        val oppOutcome = mgr.onPlay(cardToPlay)
                        opponentCardsPlayed = opponentCardsPlayed + oppCardIndex
                        peggingDisplayPile = peggingDisplayPile + cardToPlay
                        gameStatus = context.getString(R.string.played_opponent, cardToPlay.getSymbol())
                        applyManagerStateToUi()
                        checkPeggingScore(false, cardToPlay)
                        if (oppOutcome.reset != null) {
                            applyManagerReset(oppOutcome.reset)
                        } else {
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
                        autoHandleGoRef.value()
                    }
                }
            }
        }, 1000)
    }

    // Revised card selection behavior with auto-play during pegging phase.
    val toggleCardSelection = { cardIndex: Int ->
        Log.i(TAG, "Card selection toggled: $cardIndex")
        if (isPeggingPhase) {
            if (isPlayerTurn && !playerCardsPlayed.contains(cardIndex)) {
                val cardToPlay = playerHand[cardIndex]
                if (peggingCount + cardToPlay.getValue() <= 31) {
                    selectedCards = setOf(cardIndex)
                    Handler(Looper.getMainLooper()).postDelayed({
                        playSelectedCardRef.value()
                    }, 300)
                } else {
                    gameStatus = context.getString(R.string.illegal_move_exceeds_31, cardToPlay.getSymbol())
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
        Log.i(TAG, "Ending game")
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
        peggingManager = null

        dealButtonEnabled = false
        selectCribButtonEnabled = false
        playCardButtonEnabled = false
        showHandCountingButton = false
        gameOver = false
        
        // Reset UI state
        currentPhase = GamePhase.SETUP
        isInHandCountingPhase = false
        countingPhase = CountingPhase.NONE
        handScores = HandScores()

        gameStatus = context.getString(R.string.welcome_to_cribbage)
    }

    val startNewGame = {
        Log.i(TAG, "Starting new game")
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
        peggingManager = null
        
        // Reset UI state
        currentPhase = GamePhase.SETUP
        isInHandCountingPhase = false
        countingPhase = CountingPhase.NONE
        handScores = HandScores()

        // Dealer selection: if a previous game exists, loser deals first; otherwise perform a cut
        val prefs = context.getSharedPreferences("cribbage_prefs", Context.MODE_PRIVATE)
        if (prefs.contains("nextDealerIsPlayer")) {
            isPlayerDealer = prefs.getBoolean("nextDealerIsPlayer", false)
            cutPlayerCard = null
            cutOpponentCard = null
            gameStatus = context.getString(R.string.dealer_set_by_previous, if (isPlayerDealer) "You are dealer" else "Opponent is dealer")
            Log.i(TAG, "Dealer set by previous game loser: ${if (isPlayerDealer) "Player" else "Opponent"}")
        } else {
            // Cut for dealer per rules: lower card deals first
            run {
                var pCut: Card
                var oCut: Card
                var who: com.brianhenning.cribbage.logic.Player?
                do {
                    val deck = createDeck().shuffled()
                    pCut = deck[0]
                    oCut = deck[1]
                    who = dealerFromCut(pCut, oCut)
                } while (who == null)
                isPlayerDealer = (who == com.brianhenning.cribbage.logic.Player.PLAYER)
                // Save cut cards for UI header and persist
                cutPlayerCard = pCut
                cutOpponentCard = oCut
                prefs.edit()
                    .putInt("cutPlayerRank", pCut.rank.ordinal)
                    .putInt("cutPlayerSuit", pCut.suit.ordinal)
                    .putInt("cutOppRank", oCut.rank.ordinal)
                    .putInt("cutOppSuit", oCut.suit.ordinal)
                    .apply()
                gameStatus = "Cut for deal: You ${pCut.getSymbol()} vs Opponent ${oCut.getSymbol()}\n" +
                        if (isPlayerDealer) "You are dealer" else "Opponent is dealer"
                Log.i(TAG, "Cut: player=${pCut.getSymbol()}, opp=${oCut.getSymbol()} -> Dealer: ${if (isPlayerDealer) "Player" else "Opponent"}")
            }
        }

        dealButtonEnabled = true
        selectCribButtonEnabled = false
        playCardButtonEnabled = false
        showHandCountingButton = false
        gameOver = false
        currentPhase = GamePhase.SETUP

        gameStatus = context.getString(R.string.game_started)
    }

    val dealCards = {
        Log.i(TAG, "Dealing cards")
        val deck = createDeck().shuffled().toMutableList()
        val result = dealSixToEach(deck, playerIsDealer = isPlayerDealer)
        playerHand = result.playerHand.sortedWith(compareBy({ it.rank.ordinal }, { it.suit.ordinal }))
        opponentHand = result.opponentHand
        Log.i(TAG, "Player hand: $playerHand")
        Log.i(TAG, "Opponent hand: $opponentHand")
        // Save remaining undealt deck for starter draw
        drawDeck = result.remainingDeck

        playerCardsPlayed = emptySet()
        opponentCardsPlayed = emptySet()
        dealButtonEnabled = false
        selectCribButtonEnabled = true
        currentPhase = GamePhase.CRIB_SELECTION
        gameStatus = context.getString(R.string.select_cards_for_crib)
    }

    val selectCardsForCrib = {
        Log.i(TAG, "Select cards for crib called, selected cards: ${selectedCards.size}")
        if (selectedCards.size != 2) {
            gameStatus = context.getString(R.string.select_exactly_two)
            Log.i(TAG, "Not enough cards selected for crib")
        } else {
            val selectedIndices = selectedCards.toList().sortedDescending()
            val selectedPlayerCards = selectedIndices.map { playerHand[it] }
            Log.i(TAG, "Cards selected for crib: $selectedPlayerCards")
            val opponentCribCards = opponentHand.shuffled().take(2)
            Log.i(TAG, "Opponent cards for crib: $opponentCribCards")
            // Save the crib hand (combining player’s selections and opponent’s crib cards)
            cribHand = selectedPlayerCards + opponentCribCards

            playerHand = playerHand.filterIndexed { index, _ -> !selectedCards.contains(index) }
            playerHand = playerHand.sortedWith(compareBy({ it.rank.ordinal }, { it.suit.ordinal }))
            opponentHand = opponentHand.filter { !opponentCribCards.contains(it) }
                .sortedWith(compareBy({ it.rank.ordinal }, { it.suit.ordinal }))
            selectedCards = emptySet()
            selectCribButtonEnabled = false
            gameStatus = context.getString(R.string.crib_cards_selected)

            // Draw starter from the same remaining deck to avoid duplication
            if (drawDeck.isEmpty()) {
                // Safety: if deck exhausted (shouldn't happen), reshuffle a fresh deck
                drawDeck = createDeck().shuffled()
            }
            starterCard = drawDeck.first()
            drawDeck = drawDeck.drop(1)
            Log.i(TAG, "Cut card: ${starterCard?.getSymbol()}")
            gameStatus = "Cut card: ${starterCard?.getSymbol()}"
            if (starterCard?.rank == Rank.JACK) {
                if (isPlayerDealer) {
                    playerScore += 2
                    gameStatus += "\nDealer gets 2 points for his heels."
                } else {
                    opponentScore += 2
                    gameStatus += "\nDealer gets 2 points for his heels."
                }
                checkGameOverFunction()
            }
            Handler(Looper.getMainLooper()).postDelayed({
                isPeggingPhase = true
                isPlayerTurn = !isPlayerDealer
                playCardButtonEnabled = true
                peggingCount = 0
                peggingManager = PeggingRoundManager(startingPlayer = if (isPlayerTurn) Player.PLAYER else Player.OPPONENT)
                selectCribButtonEnabled = false
                currentPhase = GamePhase.PEGGING
                gameStatus += "\nPegging phase begins. " + if (isPlayerTurn)
                    context.getString(R.string.pegging_your_turn)
                else
                    context.getString(R.string.pegging_opponent_turn)
                if (!isPlayerTurn) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        val chosen = chooseSmartOpponentCard(opponentHand, opponentCardsPlayed, peggingCount, peggingPile)
                        if (chosen != null) {
                            val (cardIndex, cardToPlay) = chosen
                            val mgr = peggingManager!!
                            val outcome = mgr.onPlay(cardToPlay)
                            opponentCardsPlayed = opponentCardsPlayed + cardIndex
                            peggingDisplayPile = peggingDisplayPile + cardToPlay
                        gameStatus = context.getString(R.string.played_opponent, cardToPlay.getSymbol())
                            applyManagerStateToUi()
                            checkPeggingScore(false, cardToPlay)
                            if (outcome.reset != null) {
                                applyManagerReset(outcome.reset)
                            } else {
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
                            autoHandleGoRef.value()
                        }
                    }, 1000)
                }
            }, 1000)
        }
    }

    playSelectedCardRef.value = {
        Log.i(TAG, "Play selected card called")
        if (isPeggingPhase && isPlayerTurn && selectedCards.isNotEmpty()) {
            val cardIndex = selectedCards.first()
            Log.i(TAG, "Playing card at index $cardIndex")
            if (cardIndex < playerHand.size && !playerCardsPlayed.contains(cardIndex)) {
                val playedCard = playerHand[cardIndex]
                Log.i(TAG, "Player playing card: ${playedCard.getSymbol()}")
                if (peggingCount + playedCard.getValue() <= 31) {
                    val mgr = peggingManager!!
                    val outcome = mgr.onPlay(playedCard)
                    peggingDisplayPile = peggingDisplayPile + playedCard
                    playerCardsPlayed = playerCardsPlayed + cardIndex
                    gameStatus = context.getString(R.string.played_you, playedCard.getSymbol())
                    selectedCards = emptySet()
                    applyManagerStateToUi()
                    checkPeggingScore(true, playedCard)
                    if (outcome.reset != null) {
                        applyManagerReset(outcome.reset)
                    } else {
                        playCardButtonEnabled = false
                        gameStatus += "\n${context.getString(R.string.pegging_opponent_turn)}"
                        Handler(Looper.getMainLooper()).postDelayed({
                            val chosen = chooseSmartOpponentCard(opponentHand, opponentCardsPlayed, peggingCount, peggingPile)
                            if (chosen != null) {
                                val (oppCardIndex, cardToPlay) = chosen
                                val oppOutcome = mgr.onPlay(cardToPlay)
                                opponentCardsPlayed = opponentCardsPlayed + oppCardIndex
                                peggingDisplayPile = peggingDisplayPile + cardToPlay
                        gameStatus = context.getString(R.string.played_opponent, cardToPlay.getSymbol())
                                applyManagerStateToUi()
                                checkPeggingScore(false, cardToPlay)
                                if (oppOutcome.reset != null) {
                                    applyManagerReset(oppOutcome.reset)
                                } else {
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
                                autoHandleGoRef.value()
                            }
                        }, 1000)
                    }
                } else {
                    gameStatus = context.getString(R.string.illegal_move_exceeds_31, playedCard.getSymbol())
                    Log.i(TAG, "Illegal play attempted: count $peggingCount, card value ${playedCard.getValue()}")
                }
            }
        }
    }

    // Delegate hand scoring to shared scorer to keep logic consistent with tests.
    

    // Enhanced hand counting process with opponent card reveals
    val countHands = {
        scope.launch {
            // Enter hand counting mode
            isPeggingPhase = false
            peggingPile = emptyList()
            peggingDisplayPile = emptyList()
            showHandCountingButton = false
            isInHandCountingPhase = true
            currentPhase = GamePhase.HAND_COUNTING
            countingPhase = CountingPhase.NON_DEALER

            if (starterCard == null) {
                gameStatus = "Starter card not set. Cannot count hands."
                return@launch
            }
            
            // Determine which hand is non-dealer versus dealer
            val nonDealerHand: List<Card>
            val dealerHand: List<Card>
            if (isPlayerDealer) {
                dealerHand = playerHand
                nonDealerHand = opponentHand
            } else {
                dealerHand = opponentHand
                nonDealerHand = playerHand
            }
            
            // Count non-dealer hand
            gameStatus = "Counting non-dealer hand..."
            val (nonDealerScore, nonDealerBreakdown) = CribbageScorer.scoreHandDetailed(nonDealerHand, starterCard!!)
            handScores = handScores.copy(
                nonDealerScore = nonDealerScore,
                nonDealerBreakdown = nonDealerBreakdown
            )
            if (isPlayerDealer) {
                opponentScore += nonDealerScore
            } else {
                playerScore += nonDealerScore
            }
            checkGameOverFunction()
            if (gameOver) return@launch
            delay(3000)
            
            // Count dealer hand
            countingPhase = CountingPhase.DEALER
            gameStatus = "Counting dealer hand..."
            val (dealerScoreValue, dealerBreakdown) = CribbageScorer.scoreHandDetailed(dealerHand, starterCard!!)
            handScores = handScores.copy(
                dealerScore = dealerScoreValue,
                dealerBreakdown = dealerBreakdown
            )
            if (isPlayerDealer) {
                playerScore += dealerScoreValue
            } else {
                opponentScore += dealerScoreValue
            }
            checkGameOverFunction()
            if (gameOver) return@launch
            delay(3000)
            
            // Count crib
            countingPhase = CountingPhase.CRIB
            gameStatus = "Counting crib..."
            val (cribScoreValue, cribBreakdown) = CribbageScorer.scoreHandDetailed(cribHand, starterCard!!, isCrib = true)
            handScores = handScores.copy(
                cribScore = cribScoreValue,
                cribBreakdown = cribBreakdown
            )
            if (isPlayerDealer) {
                playerScore += cribScoreValue
            } else {
                opponentScore += cribScoreValue
            }
            checkGameOverFunction()
            if (gameOver) return@launch
            delay(3000)
            
            // Complete hand counting
            countingPhase = CountingPhase.COMPLETED
            gameStatus = "Hand counting complete. Preparing next round..."
            delay(2000)
            
            // Reset for next round
            isInHandCountingPhase = false
            countingPhase = CountingPhase.NONE
            handScores = HandScores()
            starterCard = null
            
            // Toggle dealer for next round
            isPlayerDealer = !isPlayerDealer
            currentPhase = GamePhase.SETUP
            gameStatus = "New round: " + if (isPlayerDealer) "You are now the dealer." else "Opponent is now the dealer."
            dealButtonEnabled = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App title
        Text(
            text = "Cribbage",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        // Score display
        ScoreDisplay(
            playerScore = playerScore,
            opponentScore = opponentScore
        )
        
        // Match summary
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Match Record",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = context.getString(R.string.match_summary, gamesWon, gamesLost, skunksFor, skunksAgainst),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (isPlayerDealer) "You are the dealer" else "Opponent is the dealer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Show cut cards before the deal
        if (gameStarted && dealButtonEnabled && cutPlayerCard != null && cutOpponentCard != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Cut for Dealer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = context.getString(R.string.your_cut),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            GameCard(
                                card = cutPlayerCard!!,
                                isRevealed = true,
                                isClickable = false,
                                cardSize = CardSize.Medium
                            )
                        }
                        Text(
                            text = "vs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = context.getString(R.string.opponent_cut),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            GameCard(
                                card = cutOpponentCard!!,
                                isRevealed = true,
                                isClickable = false,
                                cardSize = CardSize.Medium
                            )
                        }
                    }
                }
            }
        }

        // Show starter card during game
        starterCard?.let { starter ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Starter Card",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    GameCard(
                        card = starter,
                        isRevealed = true,
                        isClickable = false,
                        cardSize = CardSize.Large
                    )
                }
            }
        }

        // Game status and phase indicator
        GameStatusCard(
            gameStatus = gameStatus,
            currentPhase = currentPhase,
            isPlayerTurn = isPlayerTurn
        )
        
        // Hand counting display (shows opponent cards!)
        if (isInHandCountingPhase) {
            HandCountingDisplay(
                playerHand = playerHand,
                opponentHand = opponentHand,
                cribHand = cribHand,
                starterCard = starterCard,
                isPlayerDealer = isPlayerDealer,
                currentCountingPhase = countingPhase,
                handScores = handScores
            )
        } else {
            // Pegging pile display
            if (isPeggingPhase && peggingDisplayPile.isNotEmpty()) {
                PeggingPileDisplay(
                    peggingCards = peggingDisplayPile,
                    peggingCount = peggingCount
                )
            }
            
            // Crib display
            if (cribHand.isNotEmpty() && !isPeggingPhase && !isInHandCountingPhase) {
                CribDisplay(
                    cribCards = cribHand,
                    showCards = false,
                    isPlayerCrib = isPlayerDealer
                )
            }
            
            // Opponent hand display
            if (opponentHand.isNotEmpty()) {
                OpponentHandDisplay(
                    hand = opponentHand,
                    playedCards = opponentCardsPlayed,
                    showCards = false
                )
            }
        }

        // Undealt deck display
        if (gameStarted && dealButtonEnabled) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Deck",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    GameCard(
                        card = Card(Rank.ACE, Suit.SPADES), // Dummy card for back display
                        isRevealed = false,
                        isClickable = false,
                        cardSize = CardSize.Medium
                    )
                }
            }
        }

        // Player hand display
        if (playerHand.isNotEmpty() && !isInHandCountingPhase) {
            PlayerHandDisplay(
                hand = playerHand,
                selectedCards = selectedCards,
                playedCards = playerCardsPlayed,
                onCardClick = { toggleCardSelection(it) },
                isEnabled = gameStarted && !gameOver
            )
        }

        // Game control buttons
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!gameStarted) {
                        Button(
                            onClick = { startNewGame() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Start New Game")
                        }
                    } else {
                        Button(
                            onClick = { endGame() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(text = "End Game")
                        }
                    }
                    
                    if (dealButtonEnabled) {
                        Button(
                            onClick = { dealCards() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Deal Cards")
                        }
                    }
                }
                
                // Secondary action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isPeggingPhase && selectCribButtonEnabled) {
                        Button(
                            onClick = { selectCardsForCrib() },
                            enabled = selectCribButtonEnabled,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Select for Crib")
                        }
                    }
                    
                    if (showHandCountingButton) {
                        Button(
                            onClick = { countHands() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(text = "Count Hands")
                        }
                    }
                    
                    // Report bug button
                    if (gameStarted) {
                        OutlinedButton(
                            onClick = {
                                val body = buildBugReportBody(
                                    context = context,
                                    playerScore = playerScore,
                                    opponentScore = opponentScore,
                                    isPlayerDealer = isPlayerDealer,
                                    starterCard = starterCard,
                                    peggingCount = peggingCount,
                                    peggingPile = peggingPile,
                                    playerHand = playerHand,
                                    opponentHand = opponentHand,
                                    cribHand = cribHand,
                                    matchSummary = "${gamesWon}-${gamesLost} (Skunks ${skunksFor}-${skunksAgainst})",
                                    gameStatus = gameStatus
                                )
                                sendBugReportEmail(context, context.getString(R.string.feedback_email), context.getString(R.string.bug_report_subject), body)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Report Bug")
                        }
                    }
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

private fun Card.symbol(): String = this.getSymbol()

private fun List<Card>.symbols(): String = this.joinToString(",") { it.getSymbol() }

private fun buildBugReportBody(
    context: Context,
    playerScore: Int,
    opponentScore: Int,
    isPlayerDealer: Boolean,
    starterCard: Card?,
    peggingCount: Int,
    peggingPile: List<Card>,
    playerHand: List<Card>,
    opponentHand: List<Card>,
    cribHand: List<Card>,
    matchSummary: String,
    gameStatus: String,
): String {
    val manufacturer = android.os.Build.MANUFACTURER
    val model = android.os.Build.MODEL
    val sdk = android.os.Build.VERSION.SDK_INT
    val appVersion = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: Exception) { "unknown" }

    return buildString {
        appendLine("Please describe the bug:")
        appendLine()
        appendLine("Expected:")
        appendLine()
        appendLine("Actual:")
        appendLine()
        appendLine("Steps to reproduce:")
        appendLine("1.")
        appendLine("2.")
        appendLine("3.")
        appendLine()
        appendLine("— App/Device —")
        appendLine("App: $appVersion")
        appendLine("Device: $manufacturer $model (SDK $sdk)")
        appendLine()
        appendLine("— Game Snapshot —")
        appendLine("Scores: You $playerScore, Opponent $opponentScore")
        appendLine("Dealer: ${if (isPlayerDealer) "You" else "Opponent"}")
        appendLine("Starter: ${starterCard?.symbol() ?: "(none)"}")
        appendLine("Pegging count: $peggingCount")
        appendLine("Pegging pile: ${peggingPile.symbols()}")
        appendLine("Your hand: ${playerHand.symbols()}")
        appendLine("Opponent hand: ${opponentHand.symbols()}")
        appendLine("Crib: ${cribHand.symbols()}")
        appendLine("Match: $matchSummary")
        appendLine()
        appendLine("Status log:\n$gameStatus")
    }
}

private fun sendBugReportEmail(context: Context, to: String, subject: String, body: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    try {
        context.startActivity(Intent.createChooser(intent, subject))
    } catch (e: ActivityNotFoundException) {
        Log.e(TAG, "No email client available: ${e.message}")
        // Optionally show a toast/snackbar; keep it simple in this pass.
    }
}

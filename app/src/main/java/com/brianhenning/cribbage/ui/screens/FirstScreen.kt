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
import com.brianhenning.cribbage.logic.CribbageScorer
import com.brianhenning.cribbage.logic.PeggingScorer
import com.brianhenning.cribbage.logic.PeggingRoundManager
import com.brianhenning.cribbage.logic.Player
import com.brianhenning.cribbage.logic.SubRoundReset

private const val TAG = "CribbageGame"

@Composable
fun FirstScreen() {
    val context = LocalContext.current

    LaunchedEffect(Unit) { Log.i(TAG, "FirstScreen composable is being rendered") }

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
    var peggingManager by remember { mutableStateOf<PeggingRoundManager?>(null) }

    // UI state variables
    var gameStatus by remember { mutableStateOf(context.getString(R.string.welcome_to_cribbage)) }
    var dealButtonEnabled by remember { mutableStateOf(false) }
    var selectCribButtonEnabled by remember { mutableStateOf(false) }
    var playCardButtonEnabled by remember { mutableStateOf(false) }
    var showPeggingCount by remember { mutableStateOf(false) }
    var showHandCountingButton by remember { mutableStateOf(false) } // New state
    var gameOver by remember { mutableStateOf(false) } // New game over state

    // Check game over function: if either score goes past 120, end the game.
    val checkGameOverFunction: () -> Unit = {
        if (playerScore > 120 || opponentScore > 120) {
            gameOver = true
            val winner = if (playerScore > opponentScore) "You" else "Opponent"
            gameStatus += "\nGame Over: $winner wins!"
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
        showPeggingCount = false
        showHandCountingButton = false
        gameOver = false

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

        isPlayerDealer = Random.nextBoolean()
        Log.i(TAG, "Dealer: ${if (isPlayerDealer) "Player" else "Opponent"}")

        dealButtonEnabled = true
        selectCribButtonEnabled = false
        playCardButtonEnabled = false
        showPeggingCount = false
        showHandCountingButton = false
        gameOver = false

        gameStatus = context.getString(R.string.game_started)
    }

    val dealCards = {
        Log.i(TAG, "Dealing cards")
        val deck = createDeck().shuffled().toMutableList()
        playerHand = List(6) { deck.removeAt(0) }
        playerHand = playerHand.sortedWith(compareBy({ it.rank.ordinal }, { it.suit.ordinal }))
        opponentHand = List(6) { deck.removeAt(0) }
        Log.i(TAG, "Player hand: $playerHand")
        Log.i(TAG, "Opponent hand: $opponentHand")
        playerCardsPlayed = emptySet()
        opponentCardsPlayed = emptySet()
        dealButtonEnabled = false
        selectCribButtonEnabled = true
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

            val newDeck = createDeck().shuffled()
            starterCard = newDeck.first()
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
                showPeggingCount = true
                peggingCount = 0
                peggingManager = PeggingRoundManager(startingPlayer = if (isPlayerTurn) Player.PLAYER else Player.OPPONENT)
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
            val (nonDealerScore, nonDealerBreakdown) = CribbageScorer.scoreHandDetailed(nonDealerHand, starterCard!!)
            gameStatus += "\nNon-Dealer Hand Score: $nonDealerScore\n$nonDealerBreakdown"
            if (isPlayerDealer) {
                opponentScore += nonDealerScore
            } else {
                playerScore += nonDealerScore
            }
            checkGameOverFunction()
            if (gameOver) return@launch
            delay(3000)

            gameStatus = "Counting dealer hand..."
            val (dealerScoreValue, dealerBreakdown) = CribbageScorer.scoreHandDetailed(dealerHand, starterCard!!)
            gameStatus += "\nDealer Hand Score: $dealerScoreValue\n$dealerBreakdown"
            if (isPlayerDealer) {
                playerScore += dealerScoreValue
            } else {
                opponentScore += dealerScoreValue
            }
            checkGameOverFunction()
            if (gameOver) return@launch
            delay(3000)

            gameStatus = "Counting crib hand..."
            val (cribScoreValue, cribBreakdown) = CribbageScorer.scoreHandDetailed(cribHand, starterCard!!, isCrib = true)
            gameStatus += "\nCrib Hand Score: $cribScoreValue\n$cribBreakdown"
            // The crib always belongs to the dealer.
            if (isPlayerDealer) {
                playerScore += cribScoreValue
            } else {
                opponentScore += cribScoreValue
            }
            checkGameOverFunction()
            if (gameOver) return@launch
            delay(3000)

            gameStatus += "\nHand counting complete."
            // Hide the cut card after counting is complete.
            starterCard = null

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
                // Apply a vertical offset to visually stagger selected cards.
                val staggerOffset = if (isSelected) 10.dp else 0.dp
                Box(
                    modifier = Modifier
                        .offset(y = staggerOffset)
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

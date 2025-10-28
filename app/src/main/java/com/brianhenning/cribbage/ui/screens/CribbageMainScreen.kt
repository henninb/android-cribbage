package com.brianhenning.cribbage.ui.screens

import android.os.Handler
import android.os.Looper
import android.content.Context
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
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
import com.brianhenning.cribbage.shared.domain.logic.dealSixToEach
import com.brianhenning.cribbage.shared.domain.logic.dealerFromCut
import com.brianhenning.cribbage.shared.domain.logic.CribbageScorer
import com.brianhenning.cribbage.shared.domain.logic.PeggingScorer
import com.brianhenning.cribbage.shared.domain.logic.PeggingPoints
import com.brianhenning.cribbage.shared.domain.logic.PeggingRoundManager
import com.brianhenning.cribbage.shared.domain.logic.Player
import com.brianhenning.cribbage.shared.domain.logic.SubRoundReset
import com.brianhenning.cribbage.shared.domain.logic.OpponentAI
import com.brianhenning.cribbage.shared.domain.logic.ScoreEntry
import com.brianhenning.cribbage.shared.domain.logic.DetailedScoreBreakdown
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.shared.domain.model.Suit
import com.brianhenning.cribbage.shared.domain.model.createDeck

/**
 * State for pending reset - shown to user before clearing pile
 */
data class PendingResetState(
    val pile: List<Card>,
    val finalCount: Int,
    val scoreAwarded: Int,
    val resetData: SubRoundReset
)

@Composable
fun CribbageMainScreen() {
    val context = LocalContext.current

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
    var showCutForDealer by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
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

    // State barrier to prevent race conditions during opponent's turn
    var isOpponentActionInProgress by remember { mutableStateOf(false) }

    // UI state variables
    var gameStatus by remember { mutableStateOf(context.getString(R.string.welcome_to_cribbage)) }
    var currentPhase by remember { mutableStateOf(GamePhase.SETUP) }
    var dealButtonEnabled by remember { mutableStateOf(false) }
    var selectCribButtonEnabled by remember { mutableStateOf(false) }
    var playCardButtonEnabled by remember { mutableStateOf(false) }
    var showHandCountingButton by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var showPeggingCount by remember { mutableStateOf(false) }
    var showGoButton by remember { mutableStateOf(false) }
    var showWinnerModal by remember { mutableStateOf(false) }
    var winnerModalData by remember { mutableStateOf<WinnerModalData?>(null) }
    
    // Hand counting state
    var isInHandCountingPhase by remember { mutableStateOf(false) }
    var countingPhase by remember { mutableStateOf(CountingPhase.NONE) }
    var handScores by remember { mutableStateOf(HandScores()) }
    var waitingForDialogDismissal by remember { mutableStateOf(false) }

    // Score animation state (for pegging phase)
    var playerScoreAnimation by remember { mutableStateOf<ScoreAnimationState?>(null) }
    var opponentScoreAnimation by remember { mutableStateOf<ScoreAnimationState?>(null) }

    // "31!" banner state
    var show31Banner by remember { mutableStateOf(false) }
    var previousPeggingCount by remember { mutableIntStateOf(0) }

    // Pending reset state (for showing pile/count/score before clearing)
    var pendingReset by remember { mutableStateOf<PendingResetState?>(null) }

    // Detect when pegging count reaches exactly 31 and trigger banner
    LaunchedEffect(peggingCount) {
        if (peggingCount == 31 && previousPeggingCount != 31) {
            // Count just reached 31
            show31Banner = true
        } else if (peggingCount == 0 && previousPeggingCount == 31) {
            // Count reset after 31 - banner already shown
            show31Banner = false
        }
        previousPeggingCount = peggingCount
    }

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

            // Show winner modal
            winnerModalData = WinnerModalData(
                playerWon = playerWins,
                playerScore = playerScore,
                opponentScore = opponentScore,
                wasSkunk = skunked,
                gamesWon = gamesWon,
                gamesLost = gamesLost,
                skunksFor = skunksFor,
                skunksAgainst = skunksAgainst
            )
            showWinnerModal = true
        }
    }

    // Remember a coroutine scope for the hand counting process
    val scope = rememberCoroutineScope()

    // Forward declarations for mutual recursion across helpers
    val autoHandleGoRef = remember { mutableStateOf({}) }
    val playSelectedCardRef = remember { mutableStateOf({}) }

    // Handle player manually saying "Go"
    val handlePlayerGo = {
        showGoButton = false
        autoHandleGoRef.value()
    }

    // Award pegging points from a PeggingPoints result
    fun awardPeggingPoints(isPlayer: Boolean, pts: PeggingPoints, cardPlayed: Card) {
        var awarded = 0
        fun award(points: Int) {
            if (points <= 0) return
            awarded += points
            if (isPlayer) playerScore += points else opponentScore += points
        }

        if (pts.fifteen > 0) {
            award(pts.fifteen)
            gameStatus += if (isPlayer) "\nScored 2 for 15 by You!" else "\nScored 2 for 15 by Opponent!"
        }
        if (pts.thirtyOne > 0) {
            award(pts.thirtyOne)
            gameStatus += if (isPlayer) "\nScored 2 for 31 by You!" else "\nScored 2 for 31 by Opponent!"
        }
        if (pts.pairPoints > 0) {
            award(pts.pairPoints)
            val msg = when (pts.sameRankCount) {
                2 -> "2 for a pair"
                3 -> "6 for three-of-a-kind"
                else -> "12 for four-of-a-kind"
            }
            gameStatus += if (isPlayer) "\nScored $msg by You!" else "\nScored $msg by Opponent!"
        }
        if (pts.runPoints > 0) {
            award(pts.runPoints)
            gameStatus += if (isPlayer) "\nScored ${pts.runPoints} for a run by You!" else "\nScored ${pts.runPoints} for a run by Opponent!"
        }
        if (awarded > 0) {
            // Trigger score animation during pegging phase
            if (isPeggingPhase) {
                if (isPlayer) {
                    playerScoreAnimation = ScoreAnimationState(awarded, true)
                } else {
                    opponentScoreAnimation = ScoreAnimationState(awarded, false)
                }
            }
            checkGameOverFunction()
        }
    }

    // Deprecated - use awardPeggingPoints with pre-saved pile/count instead
    val checkPeggingScore: (Boolean, Card) -> Unit = { isPlayer, cardPlayed ->
        val mgr = peggingManager
        val currentPile = mgr?.peggingPile?.toList() ?: peggingPile
        val currentCount = mgr?.peggingCount ?: peggingCount
        val pts = PeggingScorer.pointsForPile(currentPile, currentCount)
        awardPeggingPoints(isPlayer, pts, cardPlayed)
    }

    // Check if pegging phase is complete and transition to hand counting
    // Pegging is ONLY complete when all 8 cards have been played (4 player + 4 opponent)
    // Note: "No legal moves" scenarios are handled by Go/reset logic, NOT by this function
    fun checkPeggingComplete() {
        if (playerCardsPlayed.size == 4 && opponentCardsPlayed.size == 4) {
            isPeggingPhase = false
            currentPhase = GamePhase.HAND_COUNTING
            gameStatus += "\nPegging phase complete. Proceed to hand scoring."
            showHandCountingButton = true
        }
    }

    fun applyManagerStateToUi() {
        val mgr = peggingManager ?: return
        android.util.Log.d("CribbageDebug", "=== applyManagerStateToUi START ===")
        android.util.Log.d("CribbageDebug", "  BEFORE: isPlayerTurn=$isPlayerTurn, peggingCount=$peggingCount, isOpponentActionInProgress=$isOpponentActionInProgress")
        android.util.Log.d("CribbageDebug", "  MANAGER: isPlayerTurn=${mgr.isPlayerTurn}, peggingCount=${mgr.peggingCount}, pile size=${mgr.peggingPile.size}")

        peggingCount = mgr.peggingCount
        peggingPile = mgr.peggingPile.toList()
        val newPlayerTurn = (mgr.isPlayerTurn == Player.PLAYER)
        isPlayerTurn = newPlayerTurn
        consecutiveGoes = mgr.consecutiveGoes
        lastPlayerWhoPlayed = when (mgr.lastPlayerWhoPlayed) {
            Player.PLAYER -> "player"
            Player.OPPONENT -> "opponent"
            else -> null
        }

        android.util.Log.d("CribbageDebug", "  AFTER: isPlayerTurn=$isPlayerTurn, peggingCount=$peggingCount")
        android.util.Log.d("CribbageDebug", "=== applyManagerStateToUi END ===")
    }

    fun applyManagerReset(reset: SubRoundReset) {
        if (!reset.resetFor31) {
            when (reset.goPointTo) {
                Player.PLAYER -> {
                    playerScore += 1
                    gameStatus += "\nGo point for You!"
                    // Trigger +1 animation for player
                    if (isPeggingPhase) {
                        playerScoreAnimation = ScoreAnimationState(1, true)
                    }
                }
                Player.OPPONENT -> {
                    opponentScore += 1
                    gameStatus += "\nGo point for Opponent!"
                    // Trigger +1 animation for opponent
                    if (isPeggingPhase) {
                        opponentScoreAnimation = ScoreAnimationState(1, false)
                    }
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

        // Check if pegging is complete (all 8 cards played)
        // This must be done HERE rather than in handleNextRound because we need to
        // skip the opponent scheduling logic below if pegging is complete
        checkPeggingComplete()
        if (!isPeggingPhase) {
            // Pegging is complete - don't schedule any more moves
            return
        }

        // Pegging continues - set up next turn
        if (isPlayerTurn) {
            val playerLegal = playerHand.filterIndexed { index, card ->
                !playerCardsPlayed.contains(index) && (card.getValue() + peggingCount <= 31)
            }
            if (playerLegal.isEmpty()) {
                // Only show Go button if player has cards left; otherwise auto-handle
                if (playerCardsPlayed.size < 4) {
                    showGoButton = true
                    playCardButtonEnabled = false
                    gameStatus += "\nNo legal moves. Press 'Go' to continue."
                } else {
                    // Player has no cards left, auto-handle Go
                    showGoButton = false
                    playCardButtonEnabled = false
                    autoHandleGoRef.value()
                }
            } else {
                playCardButtonEnabled = true
                showGoButton = false
            }
        } else {
            // Set state barrier BEFORE scheduling opponent action
            isOpponentActionInProgress = true
            android.util.Log.d("CribbageDebug", ">>> Opponent turn starting, setting barrier")

            Handler(Looper.getMainLooper()).postDelayed({
                android.util.Log.d("CribbageDebug", ">>> Opponent postDelayed callback executing")
                val chosen = chooseSmartOpponentCard(opponentHand, opponentCardsPlayed, peggingCount, peggingPile)
                if (chosen != null) {
                    val (cardIndex, cardToPlay) = chosen
                    val mgr = peggingManager!!

                    // CRITICAL: Save pile and count BEFORE calling onPlay!
                    val pileBeforePlay = mgr.peggingPile.toList() + cardToPlay
                    val countBeforeReset = mgr.peggingCount + cardToPlay.getValue()

                    android.util.Log.d("CribbageDebug", ">>> Opponent playing: ${cardToPlay.getSymbol()}")
                    val outcome = mgr.onPlay(cardToPlay)

                    // Immediately sync manager state to UI BEFORE any other operations
                    applyManagerStateToUi()

                    opponentCardsPlayed = opponentCardsPlayed + cardIndex
                    peggingDisplayPile = peggingDisplayPile + cardToPlay
                    gameStatus = context.getString(R.string.played_opponent, cardToPlay.getSymbol())

                    // Score using saved pile/count
                    val pts = PeggingScorer.pointsForPile(pileBeforePlay, countBeforeReset)
                    awardPeggingPoints(false, pts, cardToPlay)

                    val resetData = outcome.reset
                    if (resetData != null) {
                        // Set pending reset to show pile/count/score before clearing
                        pendingReset = PendingResetState(
                            pile = pileBeforePlay,
                            finalCount = countBeforeReset,
                            scoreAwarded = pts.total,
                            resetData = resetData
                        )
                        // Clear barrier and wait for user to acknowledge
                        isOpponentActionInProgress = false
                        return@postDelayed
                    }

                    if (outcome.reset == null) {
                        // Check if pegging is complete after opponent's play
                        checkPeggingComplete()

                        // Only continue if still in pegging phase
                        if (isPeggingPhase) {
                            val playerPlayable = playerHand.filterIndexed { index, card ->
                                !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
                            }
                            if (playerPlayable.isEmpty()) {
                            // Only show Go button if player has cards left; otherwise auto-handle
                            if (playerCardsPlayed.size < 4) {
                                showGoButton = true
                                playCardButtonEnabled = false
                                gameStatus += "\nNo legal moves. Press 'Go' to continue."
                            } else {
                                // Player has no cards left, auto-handle Go
                                showGoButton = false
                                playCardButtonEnabled = false
                                autoHandleGoRef.value()
                            }
                        } else {
                            playCardButtonEnabled = true
                            showGoButton = false
                            gameStatus += "\n${context.getString(R.string.pegging_your_turn)}"
                        }
                    }
                } else {
                    // Opponent has no legal card to play
                    // Only handle GO if opponent still has cards remaining
                    if (opponentCardsPlayed.size < 4) {
                        autoHandleGoRef.value()
                    } else {
                        // Opponent has no cards left, just pass turn to player
                        playCardButtonEnabled = true
                        showGoButton = false
                        gameStatus += "\n${context.getString(R.string.pegging_your_turn)}"
                    }
                }

                // Clear state barrier AFTER all opponent actions complete
                isOpponentActionInProgress = false
                android.util.Log.d("CribbageDebug", ">>> Opponent turn complete, clearing barrier")
            }, 500) // Reduced from 1000ms to 500ms
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

        // Save pile and count before calling onGo (which may reset)
        val pileBeforeGo = mgr.peggingPile.toList()
        val countBeforeGo = mgr.peggingCount

        val reset = mgr.onGo(opponentHasLegalMove = opponentLegalMoves.isNotEmpty())
        applyManagerStateToUi()

        if (reset != null) {
            // Set pending reset to show pile/count/score before clearing
            val goScore = if (reset.goPointTo != null) 1 else 0
            pendingReset = PendingResetState(
                pile = pileBeforeGo,
                finalCount = countBeforeGo,
                scoreAwarded = goScore,
                resetData = reset
            )
            return@letUnit
        }

        // Set state barrier if opponent will play next
        val nowPlayerTurn = (mgr.isPlayerTurn == Player.PLAYER)
        if (!nowPlayerTurn) {
            isOpponentActionInProgress = true
            android.util.Log.d("CribbageDebug", ">>> GO handling: Opponent will play, setting barrier")
        }

        Handler(Looper.getMainLooper()).postDelayed({
            android.util.Log.d("CribbageDebug", ">>> GO postDelayed callback executing")
            val nowPlayerTurnInCallback = (mgr.isPlayerTurn == Player.PLAYER)
            val currentLegalMoves = if (nowPlayerTurnInCallback) {
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
                if (nowPlayerTurnInCallback) {
                    playCardButtonEnabled = true
                    gameStatus += "\n${context.getString(R.string.pegging_your_turn)}"
                    // Clear barrier if it was set
                    isOpponentActionInProgress = false
                } else {
                    val chosen = chooseSmartOpponentCard(opponentHand, opponentCardsPlayed, peggingCount, peggingPile)
                    if (chosen != null) {
                        val (oppCardIndex, cardToPlay) = chosen

                        // CRITICAL: Save pile and count BEFORE calling onPlay!
                        val pileBeforePlay = mgr.peggingPile.toList() + cardToPlay
                        val countBeforeReset = mgr.peggingCount + cardToPlay.getValue()

                        android.util.Log.d("CribbageDebug", ">>> Opponent playing after GO: ${cardToPlay.getSymbol()}")
                        val oppOutcome = mgr.onPlay(cardToPlay)

                        // Immediately sync manager state to UI BEFORE any other operations
                        applyManagerStateToUi()

                        opponentCardsPlayed = opponentCardsPlayed + oppCardIndex
                        peggingDisplayPile = peggingDisplayPile + cardToPlay
                        gameStatus = context.getString(R.string.played_opponent, cardToPlay.getSymbol())

                        // Score using saved pile/count
                        val pts = PeggingScorer.pointsForPile(pileBeforePlay, countBeforeReset)
                        awardPeggingPoints(false, pts, cardToPlay)

                        val oppResetData = oppOutcome.reset
                        if (oppResetData != null) {
                            // Set pending reset to show pile/count/score before clearing
                            pendingReset = PendingResetState(
                                pile = pileBeforePlay,
                                finalCount = countBeforeReset,
                                scoreAwarded = pts.total,
                                resetData = oppResetData
                            )
                            // Clear barrier and wait for user to acknowledge
                            isOpponentActionInProgress = false
                            return@postDelayed
                        }

                        if (oppOutcome.reset == null) {
                            // Check if pegging is complete after opponent's play
                            checkPeggingComplete()

                            // Only continue if still in pegging phase
                            if (isPeggingPhase) {
                                val playerPlayableAfter = playerHand.filterIndexed { index, card ->
                                    !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
                                }
                                if (playerPlayableAfter.isEmpty()) {
                                // Only show Go button if player has cards left; otherwise auto-handle
                                if (playerCardsPlayed.size < 4) {
                                    showGoButton = true
                                    playCardButtonEnabled = false
                                    gameStatus += "\nNo legal moves. Press 'Go' to continue."
                                } else {
                                    // Player has no cards left, auto-handle Go
                                    showGoButton = false
                                    playCardButtonEnabled = false
                                    autoHandleGoRef.value()
                                }
                            } else {
                                playCardButtonEnabled = true
                                showGoButton = false
                                gameStatus += "\n${context.getString(R.string.pegging_your_turn)}"
                            }
                            }
                        }
                    } else {
                        autoHandleGoRef.value()
                    }
                    // Clear barrier after opponent action completes
                    isOpponentActionInProgress = false
                    android.util.Log.d("CribbageDebug", ">>> GO handling: Opponent action complete, clearing barrier")
                }
            }
        }, 500) // Reduced from 1000ms to 500ms
    }

    // Card selection behavior: single-tap during pegging, multi-select during crib selection
    val toggleCardSelection = { cardIndex: Int ->
        if (isPeggingPhase) {
            // Single tap to immediately play during pegging
            if (isPlayerTurn && !playerCardsPlayed.contains(cardIndex)) {
                val cardToPlay = playerHand[cardIndex]
                if (peggingCount + cardToPlay.getValue() <= 31) {
                    // Immediately play the card on single tap
                    selectedCards = setOf(cardIndex)
                    playSelectedCardRef.value()
                } else {
                    gameStatus = context.getString(R.string.illegal_move_exceeds_31, cardToPlay.getSymbol())
                }
            }
        } else {
            // Multi-select for crib selection (toggle on/off)
            selectedCards = if (selectedCards.contains(cardIndex)) {
                selectedCards - cardIndex
            } else if (selectedCards.size < 2) {
                selectedCards + cardIndex
            } else {
                selectedCards
            }
        }
    }

    // Handle player acknowledging the end of a pegging round (31 or Go)
    val handleNextRound: () -> Unit = {
        pendingReset?.let { pending ->
            applyManagerReset(pending.resetData)
            pendingReset = null
            // Note: checkPeggingComplete() is called inside applyManagerReset
        }
    }

    // New endGame lambda to reset game state
    val endGame = {
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
        showGoButton = false
        gameOver = false

        // Reset UI state
        currentPhase = GamePhase.SETUP
        isInHandCountingPhase = false
        countingPhase = CountingPhase.NONE
        handScores = HandScores()

        gameStatus = context.getString(R.string.welcome_to_cribbage)
    }

    val startNewGame = {
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
        showGoButton = false

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
            showCutForDealer = false
            gameStatus = context.getString(R.string.dealer_set_by_previous, if (isPlayerDealer) "You are dealer" else "Opponent is dealer")
        } else {
            // Cut for dealer per rules: lower card deals first
            run {
                var pCut: Card
                var oCut: Card
                var who: Player?
                do {
                    val deck = createDeck().shuffled()
                    pCut = deck[0]
                    oCut = deck[1]
                    who = dealerFromCut(pCut, oCut)
                } while (who == null)
                isPlayerDealer = (who == Player.PLAYER)
                // Save cut cards for UI header and persist
                cutPlayerCard = pCut
                cutOpponentCard = oCut
                showCutForDealer = true  // Only show cut screen on first round
                prefs.edit()
                    .putInt("cutPlayerRank", pCut.rank.ordinal)
                    .putInt("cutPlayerSuit", pCut.suit.ordinal)
                    .putInt("cutOppRank", oCut.rank.ordinal)
                    .putInt("cutOppSuit", oCut.suit.ordinal)
                    .apply()
                gameStatus = "Cut for deal: You ${pCut.getSymbol()} vs Opponent ${oCut.getSymbol()}\n" +
                        if (isPlayerDealer) "You are dealer" else "Opponent is dealer"
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
        val deck = createDeck().shuffled().toMutableList()
        val result = dealSixToEach(deck, playerIsDealer = isPlayerDealer)
        playerHand = result.playerHand.sortedWith(compareBy({ it.rank.ordinal }, { it.suit.ordinal }))
        opponentHand = result.opponentHand
        // Save remaining undealt deck for starter draw
        drawDeck = result.remainingDeck

        playerCardsPlayed = emptySet()
        opponentCardsPlayed = emptySet()
        dealButtonEnabled = false
        selectCribButtonEnabled = true
        currentPhase = GamePhase.CRIB_SELECTION
        gameStatus = if (isPlayerDealer) {
            context.getString(R.string.select_cards_for_your_crib)
        } else {
            context.getString(R.string.select_cards_for_opponent_crib)
        }

        // Hide cut for dealer screen after first deal
        showCutForDealer = false
    }

    val selectCardsForCrib = {
        if (selectedCards.size != 2) {
            gameStatus = context.getString(R.string.select_exactly_two)
        } else {
            val selectedIndices = selectedCards.toList().sortedDescending()
            val selectedPlayerCards = selectedIndices.map { playerHand[it] }
            // Use smart AI to choose crib cards
            val opponentCribCards = OpponentAI.chooseCribCards(opponentHand, !isPlayerDealer)
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
            gameStatus = "Cut card: ${starterCard?.getSymbol()}"
            if (starterCard?.rank == Rank.JACK) {
                if (isPlayerDealer) {
                    playerScore += 2
                    gameStatus += "\nDealer gets 2 points for his heels."
                    // Trigger +2 animation for player
                    playerScoreAnimation = ScoreAnimationState(2, true)
                } else {
                    opponentScore += 2
                    gameStatus += "\nDealer gets 2 points for his heels."
                    // Trigger +2 animation for opponent
                    opponentScoreAnimation = ScoreAnimationState(2, false)
                }
                checkGameOverFunction()
            }
            Handler(Looper.getMainLooper()).postDelayed({
                android.util.Log.d("CribbageDebug", ">>> Starting pegging phase")
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
                    // Set state barrier before opponent's first play
                    isOpponentActionInProgress = true
                    android.util.Log.d("CribbageDebug", ">>> Opponent starts pegging, setting barrier")

                    Handler(Looper.getMainLooper()).postDelayed({
                        android.util.Log.d("CribbageDebug", ">>> Opponent first play callback executing")
                        val chosen = chooseSmartOpponentCard(opponentHand, opponentCardsPlayed, peggingCount, peggingPile)
                        if (chosen != null) {
                            val (cardIndex, cardToPlay) = chosen
                            val mgr = peggingManager!!

                            // CRITICAL: Save pile and count BEFORE calling onPlay!
                            // onPlay() will reset these immediately if hitting 31
                            val pileBeforePlay = mgr.peggingPile.toList() + cardToPlay
                            val countBeforeReset = mgr.peggingCount + cardToPlay.getValue()

                            android.util.Log.d("CribbageDebug", ">>> Opponent first play: ${cardToPlay.getSymbol()}")
                            val outcome = mgr.onPlay(cardToPlay)

                            // Immediately sync manager state to UI BEFORE any other operations
                            applyManagerStateToUi()

                            opponentCardsPlayed = opponentCardsPlayed + cardIndex
                            peggingDisplayPile = peggingDisplayPile + cardToPlay
                            gameStatus = context.getString(R.string.played_opponent, cardToPlay.getSymbol())

                            // Score using saved pile/count
                            val pts = PeggingScorer.pointsForPile(pileBeforePlay, countBeforeReset)
                            awardPeggingPoints(false, pts, cardToPlay)

                            val resetData = outcome.reset
                            if (resetData != null) {
                                // Set pending reset to show pile/count/score before clearing
                                pendingReset = PendingResetState(
                                    pile = pileBeforePlay,
                                    finalCount = countBeforeReset,
                                    scoreAwarded = pts.total,
                                    resetData = resetData
                                )
                                // Clear barrier and wait for user to acknowledge
                                isOpponentActionInProgress = false
                                return@postDelayed
                            }

                            if (outcome.reset == null) {
                                // Check if pegging is complete after opponent's play
                                checkPeggingComplete()

                                // Only continue if still in pegging phase
                                if (isPeggingPhase) {
                                    val playerPlayable = playerHand.filterIndexed { index, card ->
                                        !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
                                    }
                                    if (playerPlayable.isEmpty()) {
                                    // Only show Go button if player has cards left; otherwise auto-handle
                                    if (playerCardsPlayed.size < 4) {
                                        showGoButton = true
                                        playCardButtonEnabled = false
                                        gameStatus += "\nNo legal moves. Press 'Go' to continue."
                                    } else {
                                        // Player has no cards left, auto-handle Go
                                        showGoButton = false
                                        playCardButtonEnabled = false
                                        autoHandleGoRef.value()
                                    }
                                } else {
                                    playCardButtonEnabled = true
                                    showGoButton = false
                                }
                                }
                            }
                        } else {
                            autoHandleGoRef.value()
                        }

                        // Clear barrier after opponent's first play completes
                        isOpponentActionInProgress = false
                        android.util.Log.d("CribbageDebug", ">>> Opponent first play complete, clearing barrier")
                    }, 500) // Reduced from 1000ms to 500ms
                }
            }, 500) // Reduced from 1000ms to 500ms
        }
    }

    playSelectedCardRef.value = {
        if (isPeggingPhase && isPlayerTurn && selectedCards.isNotEmpty()) {
            val cardIndex = selectedCards.first()
            if (cardIndex < playerHand.size && !playerCardsPlayed.contains(cardIndex)) {
                val playedCard = playerHand[cardIndex]
                if (peggingCount + playedCard.getValue() <= 31) {
                    val mgr = peggingManager!!

                    // CRITICAL: Save pile and count BEFORE calling onPlay!
                    // onPlay() will reset these immediately if hitting 31
                    val pileBeforePlay = mgr.peggingPile.toList() + playedCard
                    val countBeforeReset = mgr.peggingCount + playedCard.getValue()

                    val outcome = mgr.onPlay(playedCard)
                    peggingDisplayPile = peggingDisplayPile + playedCard
                    playerCardsPlayed = playerCardsPlayed + cardIndex
                    gameStatus = context.getString(R.string.played_you, playedCard.getSymbol())
                    selectedCards = emptySet()

                    // Score using saved pile/count
                    val pts = PeggingScorer.pointsForPile(pileBeforePlay, countBeforeReset)
                    awardPeggingPoints(true, pts, playedCard)

                    applyManagerStateToUi()
                    val resetData = outcome.reset
                    if (resetData != null) {
                        // Set pending reset to show pile/count/score before clearing
                        pendingReset = PendingResetState(
                            pile = pileBeforePlay,
                            finalCount = countBeforeReset,
                            scoreAwarded = pts.total,
                            resetData = resetData
                        )
                        // Don't continue - wait for user to acknowledge
                    } else {
                        // Check if pegging is complete after player's play
                        checkPeggingComplete()

                        // Only continue if still in pegging phase
                        if (isPeggingPhase) {
                            // No reset - continue with normal flow
                            playCardButtonEnabled = false
                            gameStatus += "\n${context.getString(R.string.pegging_opponent_turn)}"

                        // Set state barrier before opponent responds to player's play
                        isOpponentActionInProgress = true
                        android.util.Log.d("CribbageDebug", ">>> Player played, opponent will respond, setting barrier")

                        Handler(Looper.getMainLooper()).postDelayed({
                            android.util.Log.d("CribbageDebug", ">>> Opponent response callback executing")
                            val chosen = chooseSmartOpponentCard(opponentHand, opponentCardsPlayed, peggingCount, peggingPile)
                            if (chosen != null) {
                                val (oppCardIndex, cardToPlay) = chosen

                                // CRITICAL: Save pile and count BEFORE calling onPlay!
                                // onPlay() will reset these immediately if hitting 31
                                val pileBeforePlay = mgr.peggingPile.toList() + cardToPlay
                                val countBeforeReset = mgr.peggingCount + cardToPlay.getValue()

                                android.util.Log.d("CribbageDebug", ">>> Opponent responding: ${cardToPlay.getSymbol()}")
                                val oppOutcome = mgr.onPlay(cardToPlay)

                                // Immediately sync manager state to UI BEFORE any other operations
                                applyManagerStateToUi()

                                opponentCardsPlayed = opponentCardsPlayed + oppCardIndex
                                peggingDisplayPile = peggingDisplayPile + cardToPlay
                                gameStatus = context.getString(R.string.played_opponent, cardToPlay.getSymbol())

                                // Score using saved pile/count
                                val pts = PeggingScorer.pointsForPile(pileBeforePlay, countBeforeReset)
                                awardPeggingPoints(false, pts, cardToPlay)

                                val oppResetData2 = oppOutcome.reset
                                if (oppResetData2 != null) {
                                    // Set pending reset to show pile/count/score before clearing
                                    pendingReset = PendingResetState(
                                        pile = pileBeforePlay,
                                        finalCount = countBeforeReset,
                                        scoreAwarded = pts.total,
                                        resetData = oppResetData2
                                    )
                                    // Clear barrier and wait for user to acknowledge
                                    isOpponentActionInProgress = false
                                    return@postDelayed
                                }

                                if (oppOutcome.reset == null) {
                                    // Check if pegging is complete after opponent's play
                                    checkPeggingComplete()

                                    // Only continue if still in pegging phase
                                    if (isPeggingPhase) {
                                        val playerPlayable = playerHand.filterIndexed { index, card ->
                                            !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
                                        }
                                        if (playerPlayable.isEmpty()) {
                                        // Only show Go button if player has cards left; otherwise auto-handle
                                        if (playerCardsPlayed.size < 4) {
                                            showGoButton = true
                                            playCardButtonEnabled = false
                                            gameStatus += "\nNo legal moves. Press 'Go' to continue."
                                        } else {
                                            // Player has no cards left, auto-handle Go
                                            showGoButton = false
                                            playCardButtonEnabled = false
                                            autoHandleGoRef.value()
                                        }
                                    } else {
                                        playCardButtonEnabled = true
                                        showGoButton = false
                                        gameStatus += "\n${context.getString(R.string.pegging_your_turn)}"
                                    }
                                    }
                                }
                            } else {
                                // Opponent has no legal card to play
                                // Only handle GO if opponent still has cards remaining
                                if (opponentCardsPlayed.size < 4) {
                                    autoHandleGoRef.value()
                                } else {
                                    // Opponent has no cards left, just pass turn to player
                                    playCardButtonEnabled = true
                                    showGoButton = false
                                    gameStatus += "\n${context.getString(R.string.pegging_your_turn)}"
                                }
                            }

                            // Clear barrier after opponent response completes
                            isOpponentActionInProgress = false
                            android.util.Log.d("CribbageDebug", ">>> Opponent response complete, clearing barrier")
                        }, 500) // Reduced from 1000ms to 500ms
                        } // End if (isPeggingPhase)
                    } // End else (no reset)
                } else {
                    gameStatus = context.getString(R.string.illegal_move_exceeds_31, playedCard.getSymbol())
                }
            }
        }
    }

    // Delegate hand scoring to shared scorer to keep logic consistent with tests.
    

    // Handle dialog dismissal and continue counting
    val onDialogDismissed = {
        waitingForDialogDismissal = false
    }

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
            val nonDealerBreakdown = CribbageScorer.scoreHandWithBreakdown(nonDealerHand, starterCard!!)
            handScores = handScores.copy(
                nonDealerScore = nonDealerBreakdown.totalScore,
                nonDealerBreakdown = nonDealerBreakdown
            )
            if (isPlayerDealer) {
                opponentScore += nonDealerBreakdown.totalScore
                // Trigger animation for opponent's non-dealer hand
                if (nonDealerBreakdown.totalScore > 0) {
                    opponentScoreAnimation = ScoreAnimationState(nonDealerBreakdown.totalScore, false)
                }
            } else {
                playerScore += nonDealerBreakdown.totalScore
                // Trigger animation for player's non-dealer hand
                if (nonDealerBreakdown.totalScore > 0) {
                    playerScoreAnimation = ScoreAnimationState(nonDealerBreakdown.totalScore, true)
                }
            }
            checkGameOverFunction()
            if (gameOver) return@launch

            // Wait for dialog dismissal
            waitingForDialogDismissal = true
            while (waitingForDialogDismissal) {
                delay(100)
            }

            // Count dealer hand
            countingPhase = CountingPhase.DEALER
            gameStatus = "Counting dealer hand..."
            val dealerBreakdown = CribbageScorer.scoreHandWithBreakdown(dealerHand, starterCard!!)
            handScores = handScores.copy(
                dealerScore = dealerBreakdown.totalScore,
                dealerBreakdown = dealerBreakdown
            )
            if (isPlayerDealer) {
                playerScore += dealerBreakdown.totalScore
                // Trigger animation for player's dealer hand
                if (dealerBreakdown.totalScore > 0) {
                    playerScoreAnimation = ScoreAnimationState(dealerBreakdown.totalScore, true)
                }
            } else {
                opponentScore += dealerBreakdown.totalScore
                // Trigger animation for opponent's dealer hand
                if (dealerBreakdown.totalScore > 0) {
                    opponentScoreAnimation = ScoreAnimationState(dealerBreakdown.totalScore, false)
                }
            }
            checkGameOverFunction()
            if (gameOver) return@launch

            // Wait for dialog dismissal
            waitingForDialogDismissal = true
            while (waitingForDialogDismissal) {
                delay(100)
            }

            // Count crib
            countingPhase = CountingPhase.CRIB
            gameStatus = "Counting crib..."
            val cribBreakdown = CribbageScorer.scoreHandWithBreakdown(cribHand, starterCard!!, isCrib = true)
            handScores = handScores.copy(
                cribScore = cribBreakdown.totalScore,
                cribBreakdown = cribBreakdown
            )
            if (isPlayerDealer) {
                playerScore += cribBreakdown.totalScore
                // Trigger animation for player's crib
                if (cribBreakdown.totalScore > 0) {
                    playerScoreAnimation = ScoreAnimationState(cribBreakdown.totalScore, true)
                }
            } else {
                opponentScore += cribBreakdown.totalScore
                // Trigger animation for opponent's crib
                if (cribBreakdown.totalScore > 0) {
                    opponentScoreAnimation = ScoreAnimationState(cribBreakdown.totalScore, false)
                }
            }
            checkGameOverFunction()
            if (gameOver) return@launch

            // Wait for dialog dismissal
            waitingForDialogDismissal = true
            while (waitingForDialogDismissal) {
                delay(100)
            }

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

    // New zone-based layout (NO scrolling!)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Zone 1: Compact Score Header (always visible, includes starter card)
        CompactScoreHeader(
            playerScore = playerScore,
            opponentScore = opponentScore,
            isPlayerDealer = isPlayerDealer,
            starterCard = starterCard,
            playerScoreAnimation = playerScoreAnimation,
            opponentScoreAnimation = opponentScoreAnimation,
            onAnimationComplete = { isPlayer ->
                if (isPlayer) {
                    playerScoreAnimation = null
                } else {
                    opponentScoreAnimation = null
                }
            }
        )

        // Zone 2: Dynamic Game Area (flexible height)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Always show the game area
            GameAreaContent(
                currentPhase = currentPhase,
                cutPlayerCard = if (showCutForDealer && gameStarted && dealButtonEnabled) cutPlayerCard else null,
                cutOpponentCard = if (showCutForDealer && gameStarted && dealButtonEnabled) cutOpponentCard else null,
                opponentHand = opponentHand,
                opponentCardsPlayed = opponentCardsPlayed,
                starterCard = starterCard,
                peggingCount = peggingCount,
                peggingPile = peggingDisplayPile,
                playerHand = playerHand,
                playerCardsPlayed = playerCardsPlayed,
                selectedCards = selectedCards,
                cribHand = cribHand,
                isPlayerDealer = isPlayerDealer,
                isPlayerTurn = isPlayerTurn,
                gameStatus = gameStatus,
                showWelcomeScreen = !gameStarted,
                onCardClick = { toggleCardSelection(it) },
                show31Banner = show31Banner,
                onBannerComplete = { show31Banner = false },
                pendingReset = pendingReset,
                onNextRound = handleNextRound
            )

            // Show hand counting dialogs on top when in counting phase
            if (isInHandCountingPhase) {
                HandCountingDisplay(
                    playerHand = playerHand,
                    opponentHand = opponentHand,
                    cribHand = cribHand,
                    starterCard = starterCard,
                    isPlayerDealer = isPlayerDealer,
                    currentCountingPhase = countingPhase,
                    handScores = handScores,
                    onDialogDismissed = onDialogDismissed
                )
            }

            // Show winner modal on top when game is over
            if (showWinnerModal && winnerModalData != null) {
                WinnerModal(
                    playerWon = winnerModalData!!.playerWon,
                    playerScore = winnerModalData!!.playerScore,
                    opponentScore = winnerModalData!!.opponentScore,
                    wasSkunk = winnerModalData!!.wasSkunk,
                    gamesWon = winnerModalData!!.gamesWon,
                    gamesLost = winnerModalData!!.gamesLost,
                    skunksFor = winnerModalData!!.skunksFor,
                    skunksAgainst = winnerModalData!!.skunksAgainst,
                    onDismiss = {
                        showWinnerModal = false
                        startNewGame()
                    }
                )
            }
        }

        // Zone 3: Context-Sensitive Action Bar
        ActionBar(
            currentPhase = currentPhase,
            gameStarted = gameStarted,
            dealButtonEnabled = dealButtonEnabled,
            selectCribButtonEnabled = selectCribButtonEnabled,
            showHandCountingButton = showHandCountingButton,
            showGoButton = showGoButton,
            gameOver = gameOver,
            selectedCardsCount = selectedCards.size,
            isPlayerDealer = isPlayerDealer,
            onStartGame = { startNewGame() },
            onEndGame = { endGame() },
            onDeal = { dealCards() },
            onSelectCrib = { selectCardsForCrib() },
            onCountHands = { countHands() },
            onGo = { handlePlayerGo() },
            onReportBug = {
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
                    gameStatus = gameStatus,
                    // Additional debug state
                    currentPhase = currentPhase,
                    gameStarted = gameStarted,
                    gameOver = gameOver,
                    isPlayerTurn = isPlayerTurn,
                    isPeggingPhase = isPeggingPhase,
                    isInHandCountingPhase = isInHandCountingPhase,
                    selectedCards = selectedCards,
                    playerCardsPlayed = playerCardsPlayed,
                    opponentCardsPlayed = opponentCardsPlayed,
                    peggingDisplayPile = peggingDisplayPile,
                    dealButtonEnabled = dealButtonEnabled,
                    selectCribButtonEnabled = selectCribButtonEnabled,
                    playCardButtonEnabled = playCardButtonEnabled,
                    showHandCountingButton = showHandCountingButton,
                    showGoButton = showGoButton,
                    peggingManager = peggingManager,
                    countingPhase = countingPhase,
                    handScores = handScores,
                    waitingForDialogDismissal = waitingForDialogDismissal,
                    consecutiveGoes = consecutiveGoes,
                    lastPlayerWhoPlayed = lastPlayerWhoPlayed
                )
                sendBugReportEmail(context, context.getString(R.string.feedback_email), context.getString(R.string.bug_report_subject), body)
            }
        )

        // Zone 4: Cribbage Board (always visible)
        CribbageBoard(
            playerScore = playerScore,
            opponentScore = opponentScore
        )
    }
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
 * Uses the enhanced OpponentAI for strategic decision-making.
 */
fun chooseSmartOpponentCard(
    hand: List<Card>,
    playedIndices: Set<Int>,
    currentCount: Int,
    peggingPile: List<Card>
): Pair<Int, Card>? {
    // Calculate how many cards the player likely has remaining
    val opponentCardsPlayed = playedIndices.size
    val opponentCardsRemaining = 4 - opponentCardsPlayed

    return OpponentAI.choosePeggingCard(
        hand = hand,
        playedIndices = playedIndices,
        currentCount = currentCount,
        peggingPile = peggingPile,
        opponentCardsRemaining = opponentCardsRemaining
    )
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
    // Additional debug state
    currentPhase: GamePhase,
    gameStarted: Boolean,
    gameOver: Boolean,
    isPlayerTurn: Boolean,
    isPeggingPhase: Boolean,
    isInHandCountingPhase: Boolean,
    selectedCards: Set<Int>,
    playerCardsPlayed: Set<Int>,
    opponentCardsPlayed: Set<Int>,
    peggingDisplayPile: List<Card>,
    dealButtonEnabled: Boolean,
    selectCribButtonEnabled: Boolean,
    playCardButtonEnabled: Boolean,
    showHandCountingButton: Boolean,
    showGoButton: Boolean,
    peggingManager: PeggingRoundManager?,
    countingPhase: CountingPhase,
    handScores: HandScores,
    waitingForDialogDismissal: Boolean,
    consecutiveGoes: Int,
    lastPlayerWhoPlayed: String?,
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
        appendLine("— Detailed Debug State —")
        appendLine("Game Phase: $currentPhase")
        appendLine("Game Started: $gameStarted")
        appendLine("Game Over: $gameOver")
        appendLine("Is Player Turn: $isPlayerTurn")
        appendLine("Is Pegging Phase: $isPeggingPhase")
        appendLine("Is In Hand Counting Phase: $isInHandCountingPhase")
        appendLine()
        appendLine("— Card State —")
        appendLine("Selected Cards: ${selectedCards.toList().sorted()}")
        appendLine("Player Cards Played: ${playerCardsPlayed.toList().sorted()}")
        appendLine("Opponent Cards Played: ${opponentCardsPlayed.toList().sorted()}")
        appendLine("Pegging Display Pile: ${peggingDisplayPile.symbols()}")
        appendLine()
        appendLine("— Button State —")
        appendLine("Deal Button Enabled: $dealButtonEnabled")
        appendLine("Select Crib Button Enabled: $selectCribButtonEnabled")
        appendLine("Play Card Button Enabled: $playCardButtonEnabled")
        appendLine("Show Hand Counting Button: $showHandCountingButton")
        appendLine("Show Go Button: $showGoButton")
        appendLine()
        appendLine("— Pegging Manager State —")
        val mgr = peggingManager
        if (mgr != null) {
            appendLine("Manager Is Player Turn: ${mgr.isPlayerTurn}")
            appendLine("Manager Pegging Count: ${mgr.peggingCount}")
            appendLine("Manager Pegging Pile: ${mgr.peggingPile.symbols()}")
            appendLine("Manager Consecutive Goes: ${mgr.consecutiveGoes}")
            appendLine("Manager Last Player Who Played: ${mgr.lastPlayerWhoPlayed}")
        } else {
            appendLine("Pegging Manager: null")
        }
        appendLine()
        appendLine("— Counting State —")
        appendLine("Counting Phase: $countingPhase")
        appendLine("Hand Scores: NonDealer=${handScores.nonDealerScore}, Dealer=${handScores.dealerScore}, Crib=${handScores.cribScore}")
        appendLine("Waiting For Dialog Dismissal: $waitingForDialogDismissal")
        appendLine()
        appendLine("— Additional Info —")
        appendLine("Consecutive Goes: $consecutiveGoes")
        appendLine("Last Player Who Played: $lastPlayerWhoPlayed")
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
        // No email client available - silently ignore
    }
}

data class WinnerModalData(
    val playerWon: Boolean,
    val playerScore: Int,
    val opponentScore: Int,
    val wasSkunk: Boolean,
    val gamesWon: Int,
    val gamesLost: Int,
    val skunksFor: Int,
    val skunksAgainst: Int
)

package com.brianhenning.cribbage.game.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brianhenning.cribbage.game.repository.PreferencesRepository
import com.brianhenning.cribbage.game.state.GameLifecycleManager
import com.brianhenning.cribbage.game.state.GameUiState
import com.brianhenning.cribbage.game.state.HandCountingStateManager
import com.brianhenning.cribbage.game.state.PeggingState
import com.brianhenning.cribbage.game.state.PeggingStateManager
import com.brianhenning.cribbage.game.state.ScoreManager
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.ui.composables.GamePhase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Cribbage game screen.
 * Coordinates all state managers and exposes immutable state to the UI.
 * Follows MVVM architecture pattern with unidirectional data flow.
 *
 * State Flow: UI actions → ViewModel methods → State Managers → Updated GameUiState → UI
 */
class CribbageGameViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "CribbageGameViewModel"
    }

    // State managers (business logic)
    private val preferencesRepository = PreferencesRepository(application)
    private val scoreManager = ScoreManager(preferencesRepository)
    private val lifecycleManager = GameLifecycleManager(preferencesRepository)
    private val handCountingManager = HandCountingStateManager()
    private val peggingManager = PeggingStateManager()

    // Mutable state (private)
    private val _uiState = MutableStateFlow(GameUiState())

    // Immutable state (public - exposed to UI)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        Log.i(TAG, "ViewModel initialized")
        // Load persisted data on initialization
        loadPersistedData()
    }

    // ========== Game Lifecycle Actions ==========

    /**
     * Starts a new game.
     * Determines dealer and initializes game state.
     */
    fun startNewGame() {
        Log.i(TAG, "========== START NEW GAME ==========")
        val result = lifecycleManager.startNewGame()

        if (result.cutPlayerCard != null && result.cutOpponentCard != null) {
            Log.i(TAG, "Cut for dealer: Player cut ${result.cutPlayerCard.rank} (ordinal ${result.cutPlayerCard.rank.ordinal}), " +
                    "Opponent cut ${result.cutOpponentCard.rank} (ordinal ${result.cutOpponentCard.rank.ordinal})")
            Log.i(TAG, "Lower card wins and becomes dealer")
            Log.i(TAG, "RESULT: ${if (result.isPlayerDealer) "PLAYER" else "OPPONENT"} is the dealer")
        } else {
            Log.i(TAG, "Dealer set from previous game: ${if (result.isPlayerDealer) "PLAYER" else "OPPONENT"}")
        }

        Log.d(TAG, "startNewGame() - isPlayerDealer=${result.isPlayerDealer}")

        _uiState.update { currentState ->
            currentState.copy(
                gameStarted = true,
                currentPhase = GamePhase.DEALING,
                isPlayerDealer = result.isPlayerDealer,
                cutPlayerCard = result.cutPlayerCard,
                cutOpponentCard = result.cutOpponentCard,
                showCutForDealer = result.showCutForDealer,
                gameStatus = result.statusMessage,
                gameOver = false,
                showWinnerModal = false,
                winnerModalData = null,
                // Reset scores to 0 for new game
                playerScore = 0,
                opponentScore = 0,
                // Reset button states
                dealButtonEnabled = true,
                selectCribButtonEnabled = false,
                playCardButtonEnabled = false,
                showHandCountingButton = false,
                showGoButton = false
            )
        }
        Log.i(TAG, "startNewGame() complete - Phase: DEALING, Scores reset to 0")
    }

    /**
     * Deals cards to both players.
     */
    fun dealCards() {
        val currentState = _uiState.value
        Log.i(TAG, "dealCards() called - isPlayerDealer=${currentState.isPlayerDealer}")
        val result = lifecycleManager.dealCards(currentState.isPlayerDealer)
        Log.d(TAG, "dealCards() - Dealt ${result.playerHand.size} cards to player, " +
                "${result.opponentHand.size} to opponent, ${result.remainingDeck.size} remaining in deck")

        _uiState.update { state ->
            state.copy(
                currentPhase = GamePhase.CRIB_SELECTION,
                playerHand = result.playerHand,
                opponentHand = result.opponentHand,
                drawDeck = result.remainingDeck,
                gameStatus = result.statusMessage,
                selectedCards = emptySet(),
                dealButtonEnabled = false,
                selectCribButtonEnabled = true,
                playCardButtonEnabled = false,
                showGoButton = false,
                showHandCountingButton = false,
                peggingState = null,
                handCountingState = null,
                starterCard = null,
                cribHand = emptyList(),
                playerScoreAnimation = null,
                opponentScoreAnimation = null,
                show31Banner = false
            )
        }
        Log.i(TAG, "dealCards() complete - Phase: CRIB_SELECTION")
    }

    /**
     * Selects cards for the crib.
     */
    fun selectCardsForCrib() {
        val currentState = _uiState.value
        Log.d(TAG, "selectCardsForCrib() called - selectedCards=${currentState.selectedCards}")

        if (currentState.selectedCards.size != 2) {
            // Invalid selection - update status message
            Log.w(TAG, "selectCardsForCrib() - Invalid selection: ${currentState.selectedCards.size} cards selected, need exactly 2")
            _uiState.update { it.copy(gameStatus = "You must select exactly 2 cards for the crib") }
            return
        }

        val result = lifecycleManager.selectCardsForCrib(
            playerHand = currentState.playerHand,
            opponentHand = currentState.opponentHand,
            selectedIndices = currentState.selectedCards,
            isPlayerDealer = currentState.isPlayerDealer,
            remainingDeck = currentState.drawDeck
        )

        if (result == null) {
            Log.e(TAG, "selectCardsForCrib() - Failed to create crib (null result)")
            _uiState.update { it.copy(gameStatus = "Invalid crib selection") }
            return
        }

        Log.d(TAG, "selectCardsForCrib() - Crib created with ${result.cribHand.size} cards, " +
                "starterCard=${result.starterCard}, playerHand now has ${result.updatedPlayerHand.size} cards")

        _uiState.update { state ->
            state.copy(
                currentPhase = GamePhase.PEGGING, // Will show cut card display before pegging starts
                playerHand = result.updatedPlayerHand,
                opponentHand = result.updatedOpponentHand,
                cribHand = result.cribHand,
                starterCard = result.starterCard,
                drawDeck = result.remainingDeck,
                gameStatus = result.statusMessage,
                selectedCards = emptySet(),
                showCutCardDisplay = true,
                selectCribButtonEnabled = false,
                showHandCountingButton = false,
                playCardButtonEnabled = false,
                showGoButton = false
                // Note: His Heels (2 points for Jack as starter) is awarded in startPeggingPhase()
            )
        }
        Log.i(TAG, "selectCardsForCrib() complete - Phase: PEGGING (showing cut card)")
    }

    /**
     * Starts the pegging phase after viewing the starter card.
     */
    fun startPeggingPhase() {
        val currentState = _uiState.value
        Log.i(TAG, "startPeggingPhase() called - starterCard=${currentState.starterCard}, " +
                "isPlayerDealer=${currentState.isPlayerDealer}")
        val result = peggingManager.startPegging(
            isPlayerDealer = currentState.isPlayerDealer,
            starterCard = currentState.starterCard
        )

        // Apply His Heels if applicable (2 points for Jack as starter card)
        val scoreResult = if (result.hisHeelsPoints > 0) {
            Log.i(TAG, "startPeggingPhase() - His Heels: Jack turned as starter, " +
                    "${result.hisHeelsPoints} points to ${if (result.hisHeelsToPlayer) "PLAYER" else "OPPONENT"} (dealer)")
            scoreManager.addScore(
                currentPlayerScore = currentState.playerScore,
                currentOpponentScore = currentState.opponentScore,
                pointsToAdd = result.hisHeelsPoints,
                isForPlayer = result.hisHeelsToPlayer,
                currentMatchStats = currentState.matchStats
            )
        } else {
            null
        }

        val (newPlayerScore, newOpponentScore, newMatchStats, winnerData) = when (scoreResult) {
            is ScoreManager.ScoreResult.ScoreUpdated -> {
                Log.d(TAG, "startPeggingPhase() - Scores updated: player=${scoreResult.newPlayerScore}, opponent=${scoreResult.newOpponentScore}")
                Tuple4(scoreResult.newPlayerScore, scoreResult.newOpponentScore, scoreResult.matchStats, null)
            }
            is ScoreManager.ScoreResult.GameOver -> {
                Log.i(TAG, "startPeggingPhase() - GAME OVER from His Heels! playerWon=${scoreResult.winnerModalData.playerWon}")
                Log.i(TAG, "startPeggingPhase() - Setting showWinnerModal=true, gameOver=true")
                Tuple4(scoreResult.newPlayerScore, scoreResult.newOpponentScore, scoreResult.matchStats, scoreResult.winnerModalData)
            }
            null -> {
                Tuple4(currentState.playerScore, currentState.opponentScore, currentState.matchStats, null)
            }
        }

        _uiState.update { state ->
            state.copy(
                currentPhase = GamePhase.PEGGING,
                showCutCardDisplay = false, // Always hide cut card when entering pegging
                peggingState = result.peggingState,
                gameStatus = result.statusMessage,
                playerScore = newPlayerScore,
                opponentScore = newOpponentScore,
                matchStats = newMatchStats,
                gameOver = winnerData != null,
                showWinnerModal = winnerData != null,
                winnerModalData = winnerData,
                playCardButtonEnabled = result.peggingState.isPlayerTurn && winnerData == null,
                showGoButton = false
            )
        }
        Log.i(TAG, "startPeggingPhase() complete - isPlayerTurn=${result.peggingState.isPlayerTurn}, " +
                "scores: player=$newPlayerScore, opponent=$newOpponentScore")
        Log.i(TAG, "Cribbage Rule: Non-dealer leads first in pegging. " +
                "Dealer=${if (currentState.isPlayerDealer) "PLAYER" else "OPPONENT"}, " +
                "so ${if (result.peggingState.isPlayerTurn) "PLAYER" else "OPPONENT"} plays first")

        // If opponent starts and game isn't over, trigger opponent's first move
        if (!result.peggingState.isPlayerTurn && winnerData == null) {
            Log.d(TAG, "startPeggingPhase() - Opponent starts, triggering opponent move")
            viewModelScope.launch {
                delay(500) // Brief delay for UX
                performOpponentPeggingMove()
            }
        }
    }

    // ========== Pegging Phase Actions ==========

    /**
     * Plays a card during pegging.
     */
    fun playCard(cardIndex: Int) {
        val currentState = _uiState.value
        val peggingState = currentState.peggingState ?: run {
            Log.w(TAG, "playCard() - No pegging state available")
            return
        }

        if (!peggingState.isPlayerTurn) {
            Log.w(TAG, "playCard() - Not player's turn")
            return
        }

        val card = currentState.playerHand.getOrNull(cardIndex) ?: run {
            Log.w(TAG, "playCard() - Invalid card index: $cardIndex")
            return
        }

        Log.d(TAG, "playCard() called - cardIndex=$cardIndex, card=$card, " +
                "currentCount=${peggingState.peggingCount}")

        val result = peggingManager.playCard(
            currentState = peggingState,
            card = card,
            isPlayer = true,
            cardIndex = cardIndex
        )

        // Apply score if points awarded
        val scoreResult = if (result.pointsAwarded > 0) {
            scoreManager.addScore(
                currentPlayerScore = currentState.playerScore,
                currentOpponentScore = currentState.opponentScore,
                pointsToAdd = result.pointsAwarded,
                isForPlayer = result.isForPlayer,
                currentMatchStats = currentState.matchStats
            )
        } else {
            null
        }

        val (newPlayerScore, newOpponentScore, newMatchStats, winnerData) = when (scoreResult) {
            is ScoreManager.ScoreResult.ScoreUpdated -> {
                Tuple4(scoreResult.newPlayerScore, scoreResult.newOpponentScore, scoreResult.matchStats, null)
            }
            is ScoreManager.ScoreResult.GameOver -> {
                Tuple4(scoreResult.newPlayerScore, scoreResult.newOpponentScore, scoreResult.matchStats, scoreResult.winnerModalData)
            }
            null -> {
                Tuple4(currentState.playerScore, currentState.opponentScore, currentState.matchStats, null)
            }
        }

        if (result.pointsAwarded > 0) {
            Log.d(TAG, "playCard() - Points awarded: ${result.pointsAwarded}, " +
                    "newCount=${result.updatedPeggingState.peggingCount}, " +
                    "scores: player=$newPlayerScore, opponent=$newOpponentScore")
        }

        if (winnerData != null) {
            Log.i(TAG, "playCard() - GAME OVER: playerWon=${winnerData.playerWon}, " +
                    "finalScore: player=${winnerData.playerScore}, opponent=${winnerData.opponentScore}")
            Log.i(TAG, "playCard() - Setting showWinnerModal=true, gameOver=true")
        }

        _uiState.update { state ->
            state.copy(
                peggingState = result.updatedPeggingState,
                gameStatus = result.statusMessage,
                playerScore = newPlayerScore,
                opponentScore = newOpponentScore,
                matchStats = newMatchStats,
                playerScoreAnimation = result.animation,
                show31Banner = result.show31Banner,
                gameOver = winnerData != null,
                showWinnerModal = winnerData != null,
                winnerModalData = winnerData,
                playCardButtonEnabled = false,
                showGoButton = false,
                // Clear UI elements when game ends
                showCutCardDisplay = if (winnerData != null) false else state.showCutCardDisplay
            )
        }

        // Handle pending reset or opponent's turn
        if (result.pendingReset != null) {
            Log.d(TAG, "playCard() - Pending reset after reaching ${result.pendingReset.finalCount}")
            _uiState.update { it.copy(peggingState = it.peggingState?.copy(pendingReset = result.pendingReset)) }
        } else if (!result.updatedPeggingState.isPlayerTurn && winnerData == null) {
            Log.d(TAG, "playCard() - Triggering opponent move")
            viewModelScope.launch {
                delay(800) // Delay for opponent move
                performOpponentPeggingMove()
            }
        }
    }

    /**
     * Handles player saying "Go".
     */
    fun handlePlayerGo() {
        val currentState = _uiState.value
        val peggingState = currentState.peggingState ?: run {
            Log.w(TAG, "handlePlayerGo() - No pegging state available")
            return
        }

        Log.d(TAG, "handlePlayerGo() called - currentCount=${peggingState.peggingCount}")

        // Check if opponent has legal moves
        val opponentLegal = peggingManager.getLegalMoves(
            hand = currentState.opponentHand,
            cardsPlayed = peggingState.opponentCardsPlayed,
            currentCount = peggingState.peggingCount
        )

        val result = peggingManager.handleGo(
            currentState = peggingState,
            opponentHasLegalMove = opponentLegal.hasLegalMoves
        )

        // Apply go point if awarded
        val scoreResult = if (result.goPointAwarded > 0 && result.goPointToPlayer != null) {
            Log.i(TAG, "handlePlayerGo() - Go point: ${result.goPointAwarded} point to ${if (result.goPointToPlayer) "PLAYER" else "OPPONENT"}")
            scoreManager.addScore(
                currentPlayerScore = currentState.playerScore,
                currentOpponentScore = currentState.opponentScore,
                pointsToAdd = result.goPointAwarded,
                isForPlayer = result.goPointToPlayer,
                currentMatchStats = currentState.matchStats
            )
        } else {
            null
        }

        val (newPlayerScore, newOpponentScore, newMatchStats, winnerData) = when (scoreResult) {
            is ScoreManager.ScoreResult.ScoreUpdated -> {
                Log.d(TAG, "handlePlayerGo() - Scores updated: player=${scoreResult.newPlayerScore}, opponent=${scoreResult.newOpponentScore}")
                Tuple4(scoreResult.newPlayerScore, scoreResult.newOpponentScore, scoreResult.matchStats, null)
            }
            is ScoreManager.ScoreResult.GameOver -> {
                Log.i(TAG, "handlePlayerGo() - GAME OVER from Go point! playerWon=${scoreResult.winnerModalData.playerWon}")
                Log.i(TAG, "handlePlayerGo() - Setting showWinnerModal=true, gameOver=true")
                Tuple4(scoreResult.newPlayerScore, scoreResult.newOpponentScore, scoreResult.matchStats, scoreResult.winnerModalData)
            }
            null -> {
                Tuple4(currentState.playerScore, currentState.opponentScore, currentState.matchStats, null)
            }
        }

        _uiState.update { state ->
            state.copy(
                peggingState = result.updatedPeggingState.copy(pendingReset = result.pendingReset),
                gameStatus = result.statusMessage,
                playerScore = newPlayerScore,
                opponentScore = newOpponentScore,
                matchStats = newMatchStats,
                gameOver = winnerData != null,
                showWinnerModal = winnerData != null,
                winnerModalData = winnerData,
                playerScoreAnimation = if (result.goPointToPlayer == true) result.animation else null,
                opponentScoreAnimation = if (result.goPointToPlayer == false) result.animation else null,
                playCardButtonEnabled = false,
                showGoButton = false
            )
        }

        // If opponent has a move and game isn't over, trigger it
        if (opponentLegal.hasLegalMoves && result.pendingReset == null && winnerData == null) {
            viewModelScope.launch {
                delay(800)
                performOpponentPeggingMove()
            }
        }
    }

    /**
     * Acknowledges a pending reset and proceeds to next sub-round.
     */
    fun acknowledgeReset() {
        val currentState = _uiState.value
        val peggingState = currentState.peggingState ?: run {
            Log.w(TAG, "acknowledgeReset() - No pegging state available")
            return
        }

        Log.d(TAG, "acknowledgeReset() called - pendingReset=${peggingState.pendingReset}")

        val result = peggingManager.acknowledgeReset(
            currentState = peggingState,
            playerHand = currentState.playerHand,
            opponentHand = currentState.opponentHand
        )

        // Check if player has legal moves after reset
        val playerHasLegalMoves = if (result.updatedPeggingState.isPlayerTurn && !result.isPeggingComplete) {
            peggingManager.getLegalMoves(
                hand = currentState.playerHand,
                cardsPlayed = result.updatedPeggingState.playerCardsPlayed,
                currentCount = result.updatedPeggingState.peggingCount
            ).hasLegalMoves
        } else {
            false
        }

        val playerCardsRemaining = 4 - result.updatedPeggingState.playerCardsPlayed.size

        _uiState.update { state ->
            state.copy(
                peggingState = result.updatedPeggingState.copy(pendingReset = null),
                gameStatus = result.statusMessage,
                show31Banner = false,
                playCardButtonEnabled = playerHasLegalMoves,
                showGoButton = !playerHasLegalMoves && playerCardsRemaining > 0 && !result.isPeggingComplete && result.updatedPeggingState.isPlayerTurn
            )
        }

        // If pegging is complete, enable hand counting button
        // Note: Don't call startHandCounting() yet - that will be used in Phase 5
        // For now, the old UI code handles hand counting when button is pressed
        if (result.isPeggingComplete) {
            Log.i(TAG, "acknowledgeReset() - Pegging complete, moving to HAND_COUNTING phase")
            _uiState.update { state ->
                state.copy(
                    currentPhase = GamePhase.HAND_COUNTING,
                    peggingState = null,
                    showHandCountingButton = true,
                    playCardButtonEnabled = false,
                    showGoButton = false,
                    gameStatus = "Pegging complete. Press 'Count Hands' to continue."
                )
            }
        } else if (!result.updatedPeggingState.isPlayerTurn) {
            Log.d(TAG, "acknowledgeReset() - Triggering opponent move")
            viewModelScope.launch {
                delay(800)
                performOpponentPeggingMove()
            }
        } else if (!playerHasLegalMoves && playerCardsRemaining == 0) {
            // Player has no cards left, auto-handle Go
            Log.d(TAG, "acknowledgeReset() - Player has no cards left, auto-handling Go")
            viewModelScope.launch {
                delay(500)
                handlePlayerGo()
            }
        }
    }

    /**
     * Performs opponent's pegging move (AI).
     */
    private fun performOpponentPeggingMove() {
        val currentState = _uiState.value
        val peggingState = currentState.peggingState ?: run {
            Log.w(TAG, "performOpponentPeggingMove() - No pegging state available")
            return
        }

        if (peggingState.isPlayerTurn) {
            Log.w(TAG, "performOpponentPeggingMove() - Called on player's turn")
            return
        }

        Log.d(TAG, "performOpponentPeggingMove() - currentCount=${peggingState.peggingCount}")

        // Get opponent's legal moves
        val legalMoves = peggingManager.getLegalMoves(
            hand = currentState.opponentHand,
            cardsPlayed = peggingState.opponentCardsPlayed,
            currentCount = peggingState.peggingCount
        )

        if (!legalMoves.hasLegalMoves) {
            // Opponent says Go
            Log.d(TAG, "performOpponentPeggingMove() - No legal moves, saying Go")
            performOpponentGo()
            return
        }

        // Choose a smart card using AI
        val choice = peggingManager.chooseOpponentCard(
            hand = currentState.opponentHand,
            playedIndices = peggingState.opponentCardsPlayed,
            currentCount = peggingState.peggingCount,
            peggingPile = peggingState.peggingPile
        )

        if (choice == null) {
            Log.w(TAG, "performOpponentPeggingMove() - AI failed to choose card, saying Go")
            performOpponentGo()
            return
        }

        val (cardIndex, card) = choice
        Log.d(TAG, "performOpponentPeggingMove() - Playing card: $card (index $cardIndex)")

        // Play the card
        val result = peggingManager.playCard(
            currentState = peggingState,
            card = card,
            isPlayer = false,
            cardIndex = cardIndex
        )

        // Apply score if points awarded
        val scoreResult = if (result.pointsAwarded > 0) {
            scoreManager.addScore(
                currentPlayerScore = currentState.playerScore,
                currentOpponentScore = currentState.opponentScore,
                pointsToAdd = result.pointsAwarded,
                isForPlayer = result.isForPlayer,
                currentMatchStats = currentState.matchStats
            )
        } else {
            null
        }

        val (newPlayerScore, newOpponentScore, newMatchStats, winnerData) = when (scoreResult) {
            is ScoreManager.ScoreResult.ScoreUpdated -> {
                Tuple4(scoreResult.newPlayerScore, scoreResult.newOpponentScore, scoreResult.matchStats, null)
            }
            is ScoreManager.ScoreResult.GameOver -> {
                Tuple4(scoreResult.newPlayerScore, scoreResult.newOpponentScore, scoreResult.matchStats, scoreResult.winnerModalData)
            }
            null -> {
                Tuple4(currentState.playerScore, currentState.opponentScore, currentState.matchStats, null)
            }
        }

        if (result.pointsAwarded > 0) {
            Log.d(TAG, "performOpponentPeggingMove() - Opponent scored ${result.pointsAwarded} points, " +
                    "newCount=${result.updatedPeggingState.peggingCount}, " +
                    "scores: player=$newPlayerScore, opponent=$newOpponentScore")
        }

        if (winnerData != null) {
            Log.i(TAG, "performOpponentPeggingMove() - GAME OVER: playerWon=${winnerData.playerWon}, " +
                    "finalScore: player=${winnerData.playerScore}, opponent=${winnerData.opponentScore}")
        }

        // Check if player has legal moves after opponent's play
        val playerHasLegalMoves = if (result.updatedPeggingState.isPlayerTurn && result.pendingReset == null) {
            peggingManager.getLegalMoves(
                hand = currentState.playerHand,
                cardsPlayed = result.updatedPeggingState.playerCardsPlayed,
                currentCount = result.updatedPeggingState.peggingCount
            ).hasLegalMoves
        } else {
            false
        }

        val playerCardsRemaining = 4 - result.updatedPeggingState.playerCardsPlayed.size

        _uiState.update { state ->
            state.copy(
                peggingState = result.updatedPeggingState.copy(
                    isOpponentActionInProgress = false,
                    pendingReset = result.pendingReset
                ),
                gameStatus = result.statusMessage,
                playerScore = newPlayerScore,
                opponentScore = newOpponentScore,
                matchStats = newMatchStats,
                opponentScoreAnimation = result.animation,
                show31Banner = result.show31Banner,
                gameOver = winnerData != null,
                showWinnerModal = winnerData != null,
                winnerModalData = winnerData,
                playCardButtonEnabled = playerHasLegalMoves,
                showGoButton = !playerHasLegalMoves && playerCardsRemaining > 0 && result.pendingReset == null
            )
        }

        // If player has no legal moves and no cards left, auto-handle Go
        if (!playerHasLegalMoves && playerCardsRemaining == 0 && result.pendingReset == null && result.updatedPeggingState.isPlayerTurn) {
            viewModelScope.launch {
                delay(500)
                handlePlayerGo()
            }
        }
    }

    /**
     * Performs opponent's "Go" declaration.
     */
    private fun performOpponentGo() {
        val currentState = _uiState.value
        val peggingState = currentState.peggingState ?: return

        // Check if player has legal moves
        val playerLegal = peggingManager.getLegalMoves(
            hand = currentState.playerHand,
            cardsPlayed = peggingState.playerCardsPlayed,
            currentCount = peggingState.peggingCount
        )

        val result = peggingManager.handleGo(
            currentState = peggingState,
            opponentHasLegalMove = playerLegal.hasLegalMoves
        )

        // Apply go point if awarded
        val scoreResult = if (result.goPointAwarded > 0 && result.goPointToPlayer != null) {
            Log.i(TAG, "performOpponentGo() - Go point: ${result.goPointAwarded} point to ${if (result.goPointToPlayer) "PLAYER" else "OPPONENT"}")
            scoreManager.addScore(
                currentPlayerScore = currentState.playerScore,
                currentOpponentScore = currentState.opponentScore,
                pointsToAdd = result.goPointAwarded,
                isForPlayer = result.goPointToPlayer,
                currentMatchStats = currentState.matchStats
            )
        } else {
            null
        }

        val (newPlayerScore, newOpponentScore, newMatchStats, winnerData) = when (scoreResult) {
            is ScoreManager.ScoreResult.ScoreUpdated -> {
                Log.d(TAG, "performOpponentGo() - Scores updated: player=${scoreResult.newPlayerScore}, opponent=${scoreResult.newOpponentScore}")
                Tuple4(scoreResult.newPlayerScore, scoreResult.newOpponentScore, scoreResult.matchStats, null)
            }
            is ScoreManager.ScoreResult.GameOver -> {
                Log.i(TAG, "performOpponentGo() - GAME OVER from Go point! playerWon=${scoreResult.winnerModalData.playerWon}")
                Log.i(TAG, "performOpponentGo() - Setting showWinnerModal=true, gameOver=true")
                Tuple4(scoreResult.newPlayerScore, scoreResult.newOpponentScore, scoreResult.matchStats, scoreResult.winnerModalData)
            }
            null -> {
                Tuple4(currentState.playerScore, currentState.opponentScore, currentState.matchStats, null)
            }
        }

        // Check if player has legal moves after opponent's Go
        val playerHasLegalMoves = if (result.updatedPeggingState.isPlayerTurn && result.pendingReset == null && winnerData == null) {
            playerLegal.hasLegalMoves
        } else {
            false
        }

        val playerCardsRemaining = 4 - result.updatedPeggingState.playerCardsPlayed.size

        _uiState.update { state ->
            state.copy(
                peggingState = result.updatedPeggingState.copy(
                    isOpponentActionInProgress = false,
                    pendingReset = result.pendingReset
                ),
                gameStatus = result.statusMessage,
                playerScore = newPlayerScore,
                opponentScore = newOpponentScore,
                matchStats = newMatchStats,
                gameOver = winnerData != null,
                showWinnerModal = winnerData != null,
                winnerModalData = winnerData,
                opponentScoreAnimation = if (result.goPointToPlayer == false) result.animation else null,
                playerScoreAnimation = if (result.goPointToPlayer == true) result.animation else null,
                playCardButtonEnabled = playerHasLegalMoves,
                showGoButton = !playerHasLegalMoves && playerCardsRemaining > 0 && result.pendingReset == null && winnerData == null
            )
        }

        // If player has no legal moves and no cards left and game isn't over, auto-handle Go
        if (!playerHasLegalMoves && playerCardsRemaining == 0 && result.pendingReset == null && result.updatedPeggingState.isPlayerTurn && winnerData == null) {
            viewModelScope.launch {
                delay(500)
                handlePlayerGo()
            }
        }
    }

    // ========== Hand Counting Actions ==========

    /**
     * Starts hand counting phase.
     */
    private fun startHandCounting() {
        val result = handCountingManager.startHandCounting()

        _uiState.update { state ->
            state.copy(
                currentPhase = GamePhase.HAND_COUNTING,
                peggingState = null,
                handCountingState = com.brianhenning.cribbage.game.state.HandCountingState(
                    isInHandCountingPhase = true,
                    countingPhase = result.countingPhase,
                    handScores = result.handScores,
                    waitingForDialogDismissal = false
                ),
                gameStatus = result.statusMessage,
                playCardButtonEnabled = false,
                showGoButton = false,
                showHandCountingButton = true
            )
        }
    }

    /**
     * Updates scores during hand counting (temporary fix until Phase 5 migration).
     */
    fun updateScores(newPlayerScore: Int, newOpponentScore: Int) {
        val currentState = _uiState.value
        Log.d(TAG, "updateScores() called - Updating scores: " +
                "player: ${currentState.playerScore} -> $newPlayerScore, " +
                "opponent: ${currentState.opponentScore} -> $newOpponentScore")
        _uiState.update { state ->
            state.copy(
                playerScore = newPlayerScore,
                opponentScore = newOpponentScore
            )
        }
    }

    /**
     * Hides the hand counting button (called when user clicks the button).
     */
    fun hideHandCountingButton() {
        Log.d(TAG, "hideHandCountingButton() called")
        _uiState.update { state ->
            state.copy(
                showHandCountingButton = false
            )
        }
    }

    /**
     * Prepares for the next round after hand counting completes.
     * Toggles dealer and enables deal button.
     */
    fun prepareNextRound() {
        val currentState = _uiState.value
        Log.i(TAG, "prepareNextRound() called - Toggling dealer: " +
                "isPlayerDealer: ${currentState.isPlayerDealer} -> ${!currentState.isPlayerDealer}")
        _uiState.update { state ->
            state.copy(
                currentPhase = GamePhase.DEALING,
                isPlayerDealer = !state.isPlayerDealer,
                handCountingState = null,
                dealButtonEnabled = true,
                showHandCountingButton = false,
                selectCribButtonEnabled = false,
                playCardButtonEnabled = false,
                showGoButton = false
            )
        }
        Log.i(TAG, "prepareNextRound() complete - Phase: DEALING, ready for next round")
    }

    /**
     * Counts hands (called by UI when user dismisses counting dialog).
     * Note: Currently unused - will be used in Phase 5 migration.
     */
    fun countHands() {
        val currentState = _uiState.value
        val countingState = currentState.handCountingState ?: return
        val starterCard = currentState.starterCard ?: return

        // Move to next round (dealer toggles)
        _uiState.update { state ->
            state.copy(
                currentPhase = GamePhase.DEALING,
                isPlayerDealer = !state.isPlayerDealer,
                handCountingState = null,
                dealButtonEnabled = true,
                showHandCountingButton = false,
                selectCribButtonEnabled = false,
                playCardButtonEnabled = false,
                showGoButton = false
            )
        }
    }

    /**
     * Ends the current game.
     */
    fun endGame() {
        val currentState = _uiState.value
        Log.i(TAG, "endGame() called - Final scores: player=${currentState.playerScore}, " +
                "opponent=${currentState.opponentScore}")
        lifecycleManager.endGame()

        _uiState.update {
            GameUiState(
                matchStats = it.matchStats,
                gameOver = true
            )
        }
        Log.i(TAG, "endGame() complete - Game ended")
    }

    /**
     * Dismisses the winner modal after game over.
     * Clears all game state to prepare for a new game.
     */
    fun dismissWinnerModal() {
        Log.i(TAG, "dismissWinnerModal() called - Clearing game state")
        _uiState.update {
            GameUiState(
                matchStats = it.matchStats,
                gameOver = false,
                showWinnerModal = false,
                winnerModalData = null,
                gameStarted = false,
                currentPhase = GamePhase.SETUP
            )
        }
        Log.i(TAG, "dismissWinnerModal() complete - Ready for new game")
    }

    // ========== UI Helper Actions ==========

    /**
     * Toggles card selection (for crib selection).
     */
    fun toggleCardSelection(index: Int) {
        val currentState = _uiState.value
        val wasSelected = currentState.selectedCards.contains(index)
        Log.d(TAG, "toggleCardSelection() called - index=$index, " +
                "wasSelected=$wasSelected, currentSelection=${currentState.selectedCards}")

        _uiState.update { state ->
            val newSelection = if (state.selectedCards.contains(index)) {
                state.selectedCards - index
            } else {
                if (state.selectedCards.size < 2) {
                    state.selectedCards + index
                } else {
                    state.selectedCards
                }
            }
            state.copy(selectedCards = newSelection)
        }

        val updatedState = _uiState.value
        Log.d(TAG, "toggleCardSelection() complete - newSelection=${updatedState.selectedCards}")
    }

    /**
     * Clears score animations.
     */
    fun clearScoreAnimation(isPlayer: Boolean) {
        Log.d(TAG, "clearScoreAnimation() called - isPlayer=$isPlayer")
        _uiState.update { state ->
            if (isPlayer) {
                state.copy(playerScoreAnimation = null)
            } else {
                state.copy(opponentScoreAnimation = null)
            }
        }
    }

    // ========== Private Helper Methods ==========

    private fun loadPersistedData() {
        Log.d(TAG, "loadPersistedData() - Loading persisted match data")
        val matchStats = lifecycleManager.loadMatchStats()
        val (cutPlayerCard, cutOpponentCard) = lifecycleManager.loadCutCards()

        Log.d(TAG, "loadPersistedData() - Loaded: matchStats=${matchStats.gamesWon + matchStats.gamesLost} games, " +
                "record: ${matchStats.gamesWon}-${matchStats.gamesLost}, " +
                "cutCards: player=$cutPlayerCard, opponent=$cutOpponentCard")

        _uiState.update { state ->
            state.copy(
                matchStats = matchStats,
                cutPlayerCard = cutPlayerCard,
                cutOpponentCard = cutOpponentCard
            )
        }
    }

    // Helper data class for tuple returns
    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}

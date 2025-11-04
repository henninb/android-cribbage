package com.brianhenning.cribbage.game.viewmodel

import android.app.Application
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
        // Load persisted data on initialization
        loadPersistedData()
    }

    // ========== Game Lifecycle Actions ==========

    /**
     * Starts a new game.
     * Determines dealer and initializes game state.
     */
    fun startNewGame() {
        val result = lifecycleManager.startNewGame()

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
    }

    /**
     * Deals cards to both players.
     */
    fun dealCards() {
        val currentState = _uiState.value
        val result = lifecycleManager.dealCards(currentState.isPlayerDealer)

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
    }

    /**
     * Selects cards for the crib.
     */
    fun selectCardsForCrib() {
        val currentState = _uiState.value

        if (currentState.selectedCards.size != 2) {
            // Invalid selection - update status message
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
            _uiState.update { it.copy(gameStatus = "Invalid crib selection") }
            return
        }

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
    }

    /**
     * Starts the pegging phase after viewing the starter card.
     */
    fun startPeggingPhase() {
        val currentState = _uiState.value
        val result = peggingManager.startPegging(
            isPlayerDealer = currentState.isPlayerDealer,
            starterCard = currentState.starterCard
        )

        // Apply His Heels if applicable (already handled in selectCardsForCrib, but kept for clarity)
        var newPlayerScore = currentState.playerScore
        var newOpponentScore = currentState.opponentScore

        if (result.hisHeelsPoints > 0) {
            if (result.hisHeelsToPlayer) {
                newPlayerScore += result.hisHeelsPoints
            } else {
                newOpponentScore += result.hisHeelsPoints
            }
        }

        _uiState.update { state ->
            state.copy(
                currentPhase = GamePhase.PEGGING,
                showCutCardDisplay = false,
                peggingState = result.peggingState,
                gameStatus = result.statusMessage,
                playerScore = newPlayerScore,
                opponentScore = newOpponentScore,
                playCardButtonEnabled = result.peggingState.isPlayerTurn,
                showGoButton = false
            )
        }

        // If opponent starts, trigger opponent's first move
        if (!result.peggingState.isPlayerTurn) {
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
        val peggingState = currentState.peggingState ?: return

        if (!peggingState.isPlayerTurn) return

        val card = currentState.playerHand.getOrNull(cardIndex) ?: return

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
                showGoButton = false
            )
        }

        // Handle pending reset or opponent's turn
        if (result.pendingReset != null) {
            _uiState.update { it.copy(peggingState = it.peggingState?.copy(pendingReset = result.pendingReset)) }
        } else if (!result.updatedPeggingState.isPlayerTurn && winnerData == null) {
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
        val peggingState = currentState.peggingState ?: return

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
        var newPlayerScore = currentState.playerScore
        var newOpponentScore = currentState.opponentScore

        if (result.goPointAwarded > 0 && result.goPointToPlayer != null) {
            if (result.goPointToPlayer) {
                newPlayerScore += result.goPointAwarded
            } else {
                newOpponentScore += result.goPointAwarded
            }
        }

        _uiState.update { state ->
            state.copy(
                peggingState = result.updatedPeggingState.copy(pendingReset = result.pendingReset),
                gameStatus = result.statusMessage,
                playerScore = newPlayerScore,
                opponentScore = newOpponentScore,
                playerScoreAnimation = if (result.goPointToPlayer == true) result.animation else null,
                opponentScoreAnimation = if (result.goPointToPlayer == false) result.animation else null,
                playCardButtonEnabled = false,
                showGoButton = false
            )
        }

        // If opponent has a move, trigger it
        if (opponentLegal.hasLegalMoves && result.pendingReset == null) {
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
        val peggingState = currentState.peggingState ?: return

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
            viewModelScope.launch {
                delay(800)
                performOpponentPeggingMove()
            }
        } else if (!playerHasLegalMoves && playerCardsRemaining == 0) {
            // Player has no cards left, auto-handle Go
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
        val peggingState = currentState.peggingState ?: return

        if (peggingState.isPlayerTurn) return

        // Get opponent's legal moves
        val legalMoves = peggingManager.getLegalMoves(
            hand = currentState.opponentHand,
            cardsPlayed = peggingState.opponentCardsPlayed,
            currentCount = peggingState.peggingCount
        )

        if (!legalMoves.hasLegalMoves) {
            // Opponent says Go
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
            performOpponentGo()
            return
        }

        val (cardIndex, card) = choice

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
        var newPlayerScore = currentState.playerScore
        var newOpponentScore = currentState.opponentScore

        if (result.goPointAwarded > 0 && result.goPointToPlayer != null) {
            if (result.goPointToPlayer) {
                newPlayerScore += result.goPointAwarded
            } else {
                newOpponentScore += result.goPointAwarded
            }
        }

        // Check if player has legal moves after opponent's Go
        val playerHasLegalMoves = if (result.updatedPeggingState.isPlayerTurn && result.pendingReset == null) {
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
                opponentScoreAnimation = if (result.goPointToPlayer == false) result.animation else null,
                playerScoreAnimation = if (result.goPointToPlayer == true) result.animation else null,
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
        lifecycleManager.endGame()

        _uiState.update {
            GameUiState(
                matchStats = it.matchStats,
                gameOver = true
            )
        }
    }

    // ========== UI Helper Actions ==========

    /**
     * Toggles card selection (for crib selection).
     */
    fun toggleCardSelection(index: Int) {
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
    }

    /**
     * Clears score animations.
     */
    fun clearScoreAnimation(isPlayer: Boolean) {
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
        val matchStats = lifecycleManager.loadMatchStats()
        val (cutPlayerCard, cutOpponentCard) = lifecycleManager.loadCutCards()

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

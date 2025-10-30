package com.brianhenning.cribbage.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brianhenning.cribbage.R
import com.brianhenning.cribbage.game.repository.PreferencesRepository
import com.brianhenning.cribbage.game.state.*
import com.brianhenning.cribbage.shared.domain.logic.OpponentAI
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.ui.composables.GamePhase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Cribbage game.
 * Coordinates all state managers and exposes UI state to the Compose layer.
 *
 * Following MVVM architecture:
 * - UI Layer (Composables) → ViewModel → Domain Layer (State Managers) → Data Layer (Repository)
 */
class CribbageGameViewModel(
    private val context: Context,
    private val lifecycleManager: GameLifecycleManager,
    private val peggingManager: PeggingStateManager,
    private val handCountingManager: HandCountingStateManager,
    private val scoreManager: ScoreManager,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        loadInitialState()
    }

    // ========================================
    // Initialization
    // ========================================

    /**
     * Load initial state from preferences (match stats, cut cards).
     */
    private fun loadInitialState() {
        viewModelScope.launch {
            val matchStats = preferencesRepository.getMatchStats()
            val (cutPlayerCard, cutOpponentCard) = preferencesRepository.getCutCards()

            updateState { currentState ->
                currentState.copy(
                    matchStats = matchStats,
                    cutPlayerCard = cutPlayerCard,
                    cutOpponentCard = cutOpponentCard,
                    gameStatus = context.getString(R.string.welcome_to_cribbage)
                )
            }
        }
    }

    // ========================================
    // Game Lifecycle Actions
    // ========================================

    /**
     * Start a new game (cut for dealer).
     */
    fun startNewGame() {
        viewModelScope.launch {
            val result = lifecycleManager.startNewGame()

            updateState { currentState ->
                currentState.copy(
                    gameStarted = true,
                    playerScore = 0,
                    opponentScore = 0,
                    isPlayerDealer = result.isPlayerDealer,
                    cutPlayerCard = result.cutPlayerCard,
                    cutOpponentCard = result.cutOpponentCard,
                    showCutForDealer = result.showCutForDealer,
                    gameStatus = result.statusMessage,
                    currentPhase = GamePhase.SETUP,
                    dealButtonEnabled = true,
                    selectCribButtonEnabled = false,
                    playCardButtonEnabled = false,
                    showHandCountingButton = false,
                    gameOver = false,
                    playerHand = emptyList(),
                    opponentHand = emptyList(),
                    cribHand = emptyList(),
                    selectedCards = emptySet(),
                    peggingState = null,
                    handCountingState = null
                )
            }
        }
    }

    /**
     * End the current game and reset state.
     */
    fun endGame() {
        val result = lifecycleManager.endGame()
        updateState { currentState ->
            currentState.copy(
                gameStarted = false,
                playerScore = 0,
                opponentScore = 0,
                playerHand = emptyList(),
                opponentHand = emptyList(),
                cribHand = emptyList(),
                selectedCards = emptySet(),
                currentPhase = GamePhase.SETUP,
                dealButtonEnabled = false,
                selectCribButtonEnabled = false,
                playCardButtonEnabled = false,
                showHandCountingButton = false,
                gameOver = false,
                gameStatus = result.statusMessage,
                peggingState = null,
                handCountingState = null
            )
        }
    }

    /**
     * Deal cards to player and opponent.
     */
    fun dealCards() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val result = lifecycleManager.dealCards(currentState.isPlayerDealer)

            updateState { state ->
                state.copy(
                    playerHand = result.playerHand,
                    opponentHand = result.opponentHand,
                    drawDeck = result.remainingDeck,
                    currentPhase = GamePhase.CRIB_SELECTION,
                    dealButtonEnabled = false,
                    selectCribButtonEnabled = true,
                    showCutForDealer = false,
                    gameStatus = result.statusMessage
                )
            }
        }
    }

    /**
     * Select cards for the crib and draw starter card.
     */
    fun selectCardsForCrib() {
        viewModelScope.launch {
            val currentState = _uiState.value

            val result = lifecycleManager.selectCardsForCrib(
                playerHand = currentState.playerHand,
                opponentHand = currentState.opponentHand,
                selectedIndices = currentState.selectedCards,
                isPlayerDealer = currentState.isPlayerDealer,
                remainingDeck = currentState.drawDeck
            )

            when (result) {
                is CribSelectionResult.Success -> {
                    updateState { state ->
                        state.copy(
                            playerHand = result.updatedPlayerHand,
                            opponentHand = result.updatedOpponentHand,
                            cribHand = result.cribHand,
                            starterCard = result.starterCard,
                            drawDeck = result.remainingDeck,
                            selectedCards = emptySet(),
                            selectCribButtonEnabled = false,
                            showCutCardDisplay = true,
                            gameStatus = result.statusMessage
                        )
                    }
                }
                is CribSelectionResult.Error -> {
                    updateState { it.copy(gameStatus = result.message) }
                }
            }
        }
    }

    /**
     * Dismiss the cut card display modal and start pegging.
     */
    fun dismissCutCardDisplay() {
        viewModelScope.launch {
            updateState { it.copy(showCutCardDisplay = false) }

            // Start pegging after a short delay
            delay(500)
            startPeggingPhase()
        }
    }

    /**
     * Start the pegging phase.
     */
    private fun startPeggingPhase() {
        viewModelScope.launch {
            val currentState = _uiState.value

            val result = peggingManager.startPegging(
                playerHand = currentState.playerHand,
                opponentHand = currentState.opponentHand,
                isPlayerDealer = currentState.isPlayerDealer,
                starterCard = currentState.starterCard
            )

            // Award "His Heels" points if Jack was cut
            if (result.hisHeelsPoints != null) {
                val points = result.hisHeelsPoints
                applyScore(points.points, points.isForPlayer, "Dealer gets 2 points for his heels.")
            }

            updateState { state ->
                state.copy(
                    currentPhase = GamePhase.PEGGING,
                    peggingState = result.state,
                    playCardButtonEnabled = result.state.isPlayerTurn,
                    gameStatus = state.gameStatus + "\n" + result.statusMessage
                )
            }

            // If opponent starts, handle their first play
            if (!result.state.isPlayerTurn) {
                delay(500)
                handleOpponentTurn()
            }
        }
    }

    // ========================================
    // Crib Selection Actions
    // ========================================

    /**
     * Toggle selection of a card for the crib.
     */
    fun toggleCardSelection(cardIndex: Int) {
        updateState { currentState ->
            val newSelection = if (cardIndex in currentState.selectedCards) {
                currentState.selectedCards - cardIndex
            } else {
                if (currentState.selectedCards.size < 2) {
                    currentState.selectedCards + cardIndex
                } else {
                    currentState.selectedCards
                }
            }

            currentState.copy(
                selectedCards = newSelection,
                selectCribButtonEnabled = newSelection.size == 2
            )
        }
    }

    // ========================================
    // Pegging Phase Actions
    // ========================================

    /**
     * Play the selected card during pegging phase.
     */
    fun playCard() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val peggingState = currentState.peggingState ?: return@launch

            val selectedIndex = currentState.selectedCards.firstOrNull() ?: return@launch

            val result = peggingManager.playCard(
                currentState = peggingState,
                cardIndex = selectedIndex,
                playerHand = currentState.playerHand,
                isPlayer = true
            )

            handlePeggingResult(result)
        }
    }

    /**
     * Handle "Go" button press (player cannot play).
     */
    fun handleGo() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val peggingState = currentState.peggingState ?: return@launch

            val result = peggingManager.handleGo(
                currentState = peggingState,
                playerHand = currentState.playerHand,
                opponentHand = currentState.opponentHand
            )

            handlePeggingResult(result)
        }
    }

    /**
     * Acknowledge a pending pegging reset and continue.
     */
    fun acknowledgeReset() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val peggingState = currentState.peggingState ?: return@launch

            val result = peggingManager.acknowledgeReset(peggingState)

            handlePeggingResult(result)
        }
    }

    /**
     * Handle the result of a pegging action.
     */
    private suspend fun handlePeggingResult(result: PeggingResult) {
        when (result) {
            is PeggingResult.Success -> {
                // Award points if any
                if (result.pointsScored != null && result.scoredBy != null) {
                    val scoreResult = scoreManager.awardPeggingPoints(
                        currentPlayerScore = _uiState.value.playerScore,
                        currentOpponentScore = _uiState.value.opponentScore,
                        isPlayer = result.scoredBy == "Player",
                        pts = result.pointsScored,
                        matchStats = _uiState.value.matchStats
                    )

                    applyScoreResult(scoreResult.scoreResult, scoreResult.messages.joinToString("\n"))
                }

                updateState { state ->
                    state.copy(
                        peggingState = result.newState,
                        selectedCards = emptySet(),
                        gameStatus = state.gameStatus + "\n" + result.statusMessage
                    )
                }

                // Handle next action
                when (result.nextAction) {
                    NextAction.OpponentTurn -> {
                        delay(500)
                        handleOpponentTurn()
                    }
                    NextAction.PlayerMustGo -> {
                        updateState { it.copy(showGoButton = true, playCardButtonEnabled = false) }
                    }
                    NextAction.PlayerAutoGo -> {
                        delay(300)
                        handleGo()
                    }
                    NextAction.PlayerTurn -> {
                        updateState { it.copy(playCardButtonEnabled = true, showGoButton = false) }
                    }
                }
            }

            is PeggingResult.ResetPending -> {
                updateState { state ->
                    state.copy(
                        peggingState = result.newState,
                        gameStatus = state.gameStatus + "\n" + result.statusMessage
                    )
                }
            }

            is PeggingResult.PeggingComplete -> {
                updateState { state ->
                    state.copy(
                        peggingState = result.finalState,
                        showHandCountingButton = true,
                        playCardButtonEnabled = false,
                        showGoButton = false,
                        gameStatus = state.gameStatus + "\nPegging complete!"
                    )
                }
            }

            is PeggingResult.Error -> {
                updateState { it.copy(gameStatus = "Error: ${result.message}") }
            }
        }
    }

    /**
     * Handle the opponent's turn during pegging.
     */
    private suspend fun handleOpponentTurn() {
        val currentState = _uiState.value
        val peggingState = currentState.peggingState ?: return

        // Choose opponent's card using AI
        val opponentCardsRemaining = 4 - peggingState.opponentCardsPlayed.size
        val chosenCard = OpponentAI.choosePeggingCard(
            hand = currentState.opponentHand,
            playedIndices = peggingState.opponentCardsPlayed,
            currentCount = peggingState.peggingCount,
            peggingPile = peggingState.peggingPile,
            opponentCardsRemaining = opponentCardsRemaining
        )

        if (chosenCard != null) {
            val (cardIndex, _) = chosenCard
            val result = peggingManager.playCard(
                currentState = peggingState,
                cardIndex = cardIndex,
                playerHand = currentState.opponentHand,
                isPlayer = false
            )

            handlePeggingResult(result)
        } else {
            // Opponent has no legal moves, handle Go
            val result = peggingManager.handleGo(
                currentState = peggingState,
                playerHand = currentState.playerHand,
                opponentHand = currentState.opponentHand
            )
            handlePeggingResult(result)
        }
    }

    // ========================================
    // Hand Counting Phase Actions
    // ========================================

    /**
     * Start the hand counting phase (show dialogs sequentially).
     */
    fun startHandCounting() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val starterCard = currentState.starterCard ?: return@launch

            val result = handCountingManager.startHandCounting(
                playerHand = currentState.playerHand,
                opponentHand = currentState.opponentHand,
                cribHand = currentState.cribHand,
                starterCard = starterCard,
                isPlayerDealer = currentState.isPlayerDealer
            )

            when (result) {
                is HandCountingResult.ShowDialog -> {
                    applyScore(result.pointsAwarded, result.isForPlayer, "Counting ${result.phase.name.lowercase()} hand...")

                    updateState { state ->
                        state.copy(
                            currentPhase = GamePhase.HAND_COUNTING,
                            handCountingState = result.state,
                            showHandCountingButton = false
                        )
                    }
                }
                is HandCountingResult.Complete -> {
                    // Shouldn't happen on start
                }
            }
        }
    }

    /**
     * Dismiss the current hand counting dialog and show the next.
     */
    fun dismissHandCountingDialog() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val handCountingState = currentState.handCountingState ?: return@launch
            val starterCard = currentState.starterCard ?: return@launch

            val result = handCountingManager.dismissDialog(
                currentState = handCountingState,
                playerHand = currentState.playerHand,
                opponentHand = currentState.opponentHand,
                cribHand = currentState.cribHand,
                starterCard = starterCard,
                isPlayerDealer = currentState.isPlayerDealer
            )

            when (result) {
                is HandCountingResult.ShowDialog -> {
                    applyScore(result.pointsAwarded, result.isForPlayer, "Counting ${result.phase.name.lowercase()}...")

                    updateState { state ->
                        state.copy(handCountingState = result.state)
                    }
                }
                is HandCountingResult.Complete -> {
                    // Hand counting complete, start next round
                    updateState { state ->
                        state.copy(
                            handCountingState = null,
                            currentPhase = GamePhase.SETUP,
                            isPlayerDealer = !state.isPlayerDealer,
                            dealButtonEnabled = true,
                            gameStatus = "New round: " + if (!state.isPlayerDealer) "You are now the dealer." else "Opponent is now the dealer."
                        )
                    }
                }
            }
        }
    }

    // ========================================
    // Score Management
    // ========================================

    /**
     * Apply score update.
     */
    private fun applyScore(points: Int, isForPlayer: Boolean, message: String) {
        val currentState = _uiState.value
        val scoreResult = scoreManager.addScore(
            currentPlayerScore = currentState.playerScore,
            currentOpponentScore = currentState.opponentScore,
            pointsToAdd = points,
            isForPlayer = isForPlayer,
            matchStats = currentState.matchStats,
            createAnimation = true
        )

        applyScoreResult(scoreResult, message)
    }

    /**
     * Apply score result to state.
     */
    private fun applyScoreResult(scoreResult: ScoreResult, message: String) {
        when (scoreResult) {
            is ScoreResult.ScoreUpdated -> {
                updateState { state ->
                    state.copy(
                        playerScore = scoreResult.newPlayerScore,
                        opponentScore = scoreResult.newOpponentScore,
                        playerScoreAnimation = if (scoreResult.animation?.isPlayer == true) scoreResult.animation else state.playerScoreAnimation,
                        opponentScoreAnimation = if (scoreResult.animation?.isPlayer == false) scoreResult.animation else state.opponentScoreAnimation,
                        matchStats = scoreResult.matchStats,
                        gameStatus = state.gameStatus + "\n" + message
                    )
                }
            }
            is ScoreResult.GameOver -> {
                updateState { state ->
                    state.copy(
                        playerScore = scoreResult.newPlayerScore,
                        opponentScore = scoreResult.newOpponentScore,
                        gameOver = true,
                        showWinnerModal = true,
                        winnerModalData = scoreResult.winnerModalData,
                        matchStats = scoreResult.matchStats,
                        dealButtonEnabled = false,
                        selectCribButtonEnabled = false,
                        playCardButtonEnabled = false,
                        showHandCountingButton = false,
                        gameStatus = state.gameStatus + "\nGame Over!"
                    )
                }
            }
        }
    }

    // ========================================
    // Winner Modal Actions
    // ========================================

    /**
     * Dismiss the winner modal and reset for a new game.
     */
    fun dismissWinnerModal() {
        updateState { it.copy(showWinnerModal = false, winnerModalData = null) }
    }

    // ========================================
    // Debug Actions
    // ========================================

    /**
     * Show the debug score dialog (hidden feature for testing).
     */
    fun showDebugDialog() {
        updateState { it.copy(showDebugScoreDialog = true) }
    }

    /**
     * Dismiss the debug score dialog.
     */
    fun dismissDebugDialog() {
        updateState { it.copy(showDebugScoreDialog = false) }
    }

    /**
     * Set scores directly (for debugging).
     */
    fun setDebugScores(playerScore: Int, opponentScore: Int) {
        updateState { currentState ->
            currentState.copy(
                playerScore = playerScore,
                opponentScore = opponentScore
            )
        }
        checkGameOver()
    }

    /**
     * Check if the game is over after a score update.
     */
    private fun checkGameOver() {
        val currentState = _uiState.value
        val gameOverResult = scoreManager.checkGameOver(
            playerScore = currentState.playerScore,
            opponentScore = currentState.opponentScore,
            matchStats = currentState.matchStats,
            isPlayerDealer = currentState.isPlayerDealer
        )

        if (gameOverResult != null) {
            updateState { state ->
                state.copy(
                    gameOver = true,
                    showWinnerModal = true,
                    winnerModalData = gameOverResult.winnerModalData,
                    matchStats = gameOverResult.matchStats
                )
            }
        }
    }

    // ========================================
    // Internal Coordination
    // ========================================

    /**
     * Update the UI state using a transformation function.
     */
    private fun updateState(transform: (GameUiState) -> GameUiState) {
        _uiState.value = transform(_uiState.value)
    }

    // ========================================
    // Cleanup
    // ========================================

    override fun onCleared() {
        super.onCleared()
        // Save state if needed
        preferencesRepository.saveMatchStats(_uiState.value.matchStats)
    }
}

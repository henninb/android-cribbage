package com.brianhenning.cribbage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brianhenning.cribbage.BuildConfig
import com.brianhenning.cribbage.shared.domain.model.Rank
import com.brianhenning.cribbage.ui.composables.*
import com.brianhenning.cribbage.ui.viewmodels.CribbageGameViewModel
import com.brianhenning.cribbage.ui.viewmodels.CribbageGameViewModelFactory

/**
 * Refactored CribbageMainScreen using MVVM architecture.
 * All business logic is in CribbageGameViewModel.
 * This composable is purely for UI rendering.
 *
 * Architecture:
 * - UI Layer (this file) → ViewModel → Domain Managers → Repository
 * - State flows from ViewModel via StateFlow
 * - User actions invoke ViewModel methods
 */
@Composable
fun CribbageMainScreen(
    viewModel: CribbageGameViewModel = viewModel(
        factory = CribbageGameViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    // Zone-based layout (NO scrolling!)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Zone 1: Compact Score Header (always visible, includes starter card)
        CompactScoreHeader(
            playerScore = uiState.playerScore,
            opponentScore = uiState.opponentScore,
            isPlayerDealer = uiState.isPlayerDealer,
            starterCard = uiState.starterCard,
            playerScoreAnimation = uiState.playerScoreAnimation,
            opponentScoreAnimation = uiState.opponentScoreAnimation,
            onAnimationComplete = { isPlayer ->
                // Animations are managed by ViewModel
                // This callback is for UI cleanup only
            },
            onTripleTap = if (BuildConfig.ENABLE_DEBUG_SCORE_CHEAT) {
                { viewModel.showDebugDialog() }
            } else {
                null
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
                currentPhase = uiState.currentPhase,
                cutPlayerCard = if (uiState.showCutForDealer && uiState.gameStarted && uiState.dealButtonEnabled) {
                    uiState.cutPlayerCard
                } else null,
                cutOpponentCard = if (uiState.showCutForDealer && uiState.gameStarted && uiState.dealButtonEnabled) {
                    uiState.cutOpponentCard
                } else null,
                opponentHand = uiState.opponentHand,
                opponentCardsPlayed = uiState.peggingState?.opponentCardsPlayed ?: emptySet(),
                starterCard = uiState.starterCard,
                peggingCount = uiState.peggingState?.peggingCount ?: 0,
                peggingPile = uiState.peggingState?.peggingDisplayPile ?: emptyList(),
                playerHand = uiState.playerHand,
                playerCardsPlayed = uiState.peggingState?.playerCardsPlayed ?: emptySet(),
                selectedCards = uiState.selectedCards,
                cribHand = uiState.cribHand,
                isPlayerDealer = uiState.isPlayerDealer,
                isPlayerTurn = uiState.peggingState?.isPlayerTurn ?: false,
                gameStatus = uiState.gameStatus,
                showWelcomeScreen = !uiState.gameStarted,
                onCardClick = { cardIndex -> viewModel.toggleCardSelection(cardIndex) },
                show31Banner = uiState.show31Banner,
                onBannerComplete = { /* Managed by ViewModel */ },
                pendingReset = uiState.peggingState?.pendingReset,
                onNextRound = { viewModel.acknowledgeReset() }
            )

            // Show hand counting dialogs on top when in counting phase
            val handCountingState = uiState.handCountingState
            if (handCountingState?.isInHandCountingPhase == true) {
                HandCountingDisplay(
                    playerHand = uiState.playerHand,
                    opponentHand = uiState.opponentHand,
                    cribHand = uiState.cribHand,
                    starterCard = uiState.starterCard,
                    isPlayerDealer = uiState.isPlayerDealer,
                    currentCountingPhase = handCountingState.countingPhase,
                    handScores = handCountingState.handScores,
                    onDialogDismissed = { viewModel.dismissHandCountingDialog() }
                )
            }

            // Show cut card display before pegging phase
            if (uiState.showCutCardDisplay && uiState.starterCard != null) {
                CutCardDisplay(
                    cutCard = uiState.starterCard!!,
                    playerScore = uiState.playerScore,
                    opponentScore = uiState.opponentScore,
                    isJack = uiState.starterCard!!.rank == Rank.JACK,
                    dealerGetsPoints = uiState.isPlayerDealer,
                    onContinue = { viewModel.dismissCutCardDisplay() }
                )
            }

            // Show winner modal on top when game is over
            val winnerData = uiState.winnerModalData
            if (uiState.showWinnerModal && winnerData != null) {
                WinnerModal(
                    playerWon = winnerData.playerWon,
                    playerScore = winnerData.playerScore,
                    opponentScore = winnerData.opponentScore,
                    wasSkunk = winnerData.wasSkunk,
                    gamesWon = winnerData.gamesWon,
                    gamesLost = winnerData.gamesLost,
                    skunksFor = winnerData.skunksFor,
                    skunksAgainst = winnerData.skunksAgainst,
                    doubleSkunksFor = winnerData.doubleSkunksFor,
                    doubleSkunksAgainst = winnerData.doubleSkunksAgainst,
                    onDismiss = {
                        viewModel.dismissWinnerModal()
                        viewModel.startNewGame()
                    }
                )
            }
        }

        // Zone 3: Action Bar (always at bottom)
        ActionBar(
            currentPhase = uiState.currentPhase,
            gameStarted = uiState.gameStarted,
            dealButtonEnabled = uiState.dealButtonEnabled,
            selectCribButtonEnabled = uiState.selectCribButtonEnabled,
            playCardButtonEnabled = uiState.playCardButtonEnabled,
            showGoButton = uiState.showGoButton,
            showHandCountingButton = uiState.showHandCountingButton,
            gameOver = uiState.gameOver,
            onStartGame = { viewModel.startNewGame() },
            onEndGame = { viewModel.endGame() },
            onDeal = { viewModel.dealCards() },
            onSelectCrib = { viewModel.selectCardsForCrib() },
            onPlayCard = { viewModel.playCard() },
            onGo = { viewModel.handleGo() },
            onCountHands = { viewModel.startHandCounting() }
        )

        // Zone 4: Cribbage Board (fixed at bottom)
        CribbageBoard(
            playerScore = uiState.playerScore,
            opponentScore = uiState.opponentScore
        )
    }

    // Debug Score Dialog (only in debug builds)
    if (uiState.showDebugScoreDialog && BuildConfig.ENABLE_DEBUG_SCORE_CHEAT) {
        DebugScoreDialog(
            currentPlayerScore = uiState.playerScore,
            currentOpponentScore = uiState.opponentScore,
            onAdjustPlayerScore = { delta ->
                viewModel.setDebugScores(uiState.playerScore + delta, uiState.opponentScore)
            },
            onAdjustOpponentScore = { delta ->
                viewModel.setDebugScores(uiState.playerScore, uiState.opponentScore + delta)
            },
            onDismiss = { viewModel.dismissDebugDialog() }
        )
    }
}

/**
 * Action bar with game control buttons.
 * Displays appropriate buttons based on current game phase.
 */
@Composable
private fun ActionBar(
    currentPhase: GamePhase,
    gameStarted: Boolean,
    dealButtonEnabled: Boolean,
    selectCribButtonEnabled: Boolean,
    playCardButtonEnabled: Boolean,
    showGoButton: Boolean,
    showHandCountingButton: Boolean,
    gameOver: Boolean,
    onStartGame: () -> Unit,
    onEndGame: () -> Unit,
    onDeal: () -> Unit,
    onSelectCrib: () -> Unit,
    onPlayCard: () -> Unit,
    onGo: () -> Unit,
    onCountHands: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Start/End game button
            if (!gameStarted) {
                Button(onClick = onStartGame) {
                    Text("Start New Game")
                }
            } else {
                OutlinedButton(onClick = onEndGame) {
                    Text("End Game")
                }
            }

            // Right side: Phase-specific action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (currentPhase) {
                    GamePhase.SETUP -> {
                        if (gameStarted) {
                            Button(
                                onClick = onDeal,
                                enabled = dealButtonEnabled
                            ) {
                                Text("Deal Cards")
                            }
                        }
                    }

                    GamePhase.CRIB_SELECTION -> {
                        Button(
                            onClick = onSelectCrib,
                            enabled = selectCribButtonEnabled
                        ) {
                            Text("Select for Crib")
                        }
                    }

                    GamePhase.PEGGING -> {
                        if (showGoButton) {
                            Button(onClick = onGo) {
                                Text("Go")
                            }
                        }

                        Button(
                            onClick = onPlayCard,
                            enabled = playCardButtonEnabled
                        ) {
                            Text("Play Card")
                        }

                        if (showHandCountingButton) {
                            Button(onClick = onCountHands) {
                                Text("Count Hands")
                            }
                        }
                    }

                    GamePhase.HAND_COUNTING -> {
                        // Dialogs handle their own dismissal
                    }

                    GamePhase.DEALING -> {
                        // No buttons during dealing
                    }

                    GamePhase.GAME_OVER -> {
                        // Winner modal handles play again
                    }
                }
            }
        }
    }
}

package com.brianhenning.cribbage.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brianhenning.cribbage.BuildConfig
import com.brianhenning.cribbage.R
import com.brianhenning.cribbage.game.viewmodel.CribbageGameViewModel
import com.brianhenning.cribbage.ui.composables.*
import com.brianhenning.cribbage.ui.theme.LocalSeasonalTheme
import com.brianhenning.cribbage.ui.utils.BugReportUtils

/**
 * Main game screen for Cribbage.
 * Pure UI layer - all business logic is delegated to CribbageGameViewModel.
 *
 * Architecture: UI Layer → ViewModel → State Managers → Domain Logic
 * State Flow: Unidirectional - ViewModel exposes StateFlow, UI observes and reacts
 */
@Composable
fun CribbageMainScreen(
    viewModel: CribbageGameViewModel = viewModel(),
    onThemeChange: (com.brianhenning.cribbage.ui.theme.CribbageTheme) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // Collect immutable state from ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Local UI-only state for debug dialog (hidden feature, not part of game state)
    var showDebugScoreDialog by remember { mutableStateOf(false) }

    // Get current theme
    val currentTheme = LocalSeasonalTheme.current

    // Main container with modal overlay at root level
    Box(modifier = Modifier.fillMaxSize()) {
        // Zone-based layout (NO scrolling!)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Zone 0: Theme Selector Bar with Settings Button (always visible)
            ThemeSelectorBar(
                currentTheme = currentTheme,
                onThemeSelected = { theme ->
                    onThemeChange(theme)
                },
                onSettingsClick = onSettingsClick
            )

            // Zone 1: Compact Score Header (only visible after game starts)
            if (uiState.gameStarted) {
                CompactScoreHeader(
                    playerScore = uiState.playerScore,
                    opponentScore = uiState.opponentScore,
                    isPlayerDealer = uiState.isPlayerDealer,
                    starterCard = uiState.starterCard,
                    playerScoreAnimation = uiState.playerScoreAnimation,
                    opponentScoreAnimation = uiState.opponentScoreAnimation,
                    onAnimationComplete = { isPlayer ->
                        viewModel.clearScoreAnimation(isPlayer)
                    },
                    onTripleTap = if (BuildConfig.ENABLE_DEBUG_SCORE_CHEAT) {
                        { showDebugScoreDialog = true }
                    } else {
                        null
                    }
                )
            }

            // Zone 2: Dynamic Game Area (flexible height)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RectangleShape), // Prevent content from rendering outside bounds
                contentAlignment = Alignment.Center
            ) {
                // Main game area content
                GameAreaContent(
                    currentPhase = uiState.currentPhase,
                    cutPlayerCard = if (uiState.showCutForDealer) uiState.cutPlayerCard else null,
                    cutOpponentCard = if (uiState.showCutForDealer) uiState.cutOpponentCard else null,
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
                    onCardClick = { cardIndex ->
                        // During pegging: direct play; during crib selection: toggle selection
                        if (uiState.peggingState?.isPeggingPhase == true) {
                            // Single tap to immediately play during pegging
                            viewModel.playCard(cardIndex)
                        } else {
                            // Multi-select for crib selection (toggle on/off)
                            viewModel.toggleCardSelection(cardIndex)
                        }
                    },
                    show31Banner = uiState.show31Banner,
                    onBannerComplete = { /* Banner visibility is managed by ViewModel */ },
                    pendingReset = uiState.peggingState?.pendingReset,
                    onNextRound = { viewModel.acknowledgeReset() },
                    showWinnerModal = uiState.showWinnerModal
                )

                // Show hand counting dialogs on top when in counting phase
                uiState.handCountingState?.let { countingState ->
                    if (countingState.isInHandCountingPhase) {
                        HandCountingDisplay(
                            playerHand = uiState.playerHand,
                            opponentHand = uiState.opponentHand,
                            cribHand = uiState.cribHand,
                            starterCard = uiState.starterCard,
                            isPlayerDealer = uiState.isPlayerDealer,
                            currentCountingPhase = countingState.countingPhase,
                            handScores = countingState.handScores,
                            waitingForManualInput = countingState.waitingForManualInput,
                            onDialogDismissed = { viewModel.dismissHandCountingDialog() },
                            onManualPointsSubmitted = { points, onValidationResult ->
                                val result = viewModel.submitManualCount(points)
                                when (result) {
                                    is CribbageGameViewModel.ManualCountValidationResult.Incorrect -> {
                                        onValidationResult(
                                            "Incorrect! You entered ${result.userPoints} points",
                                            result.correctPoints
                                        )
                                    }
                                    is CribbageGameViewModel.ManualCountValidationResult.Error -> {
                                        onValidationResult(result.message, null)
                                    }
                                    is CribbageGameViewModel.ManualCountValidationResult.Correct -> {
                                        onValidationResult(null, null)
                                    }
                                }
                            }
                        )
                    }
                }

                // Show cut card display before pegging phase
                if (uiState.showCutCardDisplay && uiState.starterCard != null) {
                    CutCardDisplay(
                        cutCard = uiState.starterCard!!,
                        onContinue = { viewModel.startPeggingPhase() }
                    )
                }

                // Hidden debug score dialog (activated via triple-tap on score header)
                if (showDebugScoreDialog && BuildConfig.ENABLE_DEBUG_SCORE_CHEAT) {
                    DebugScoreDialog(
                        currentPlayerScore = uiState.playerScore,
                        currentOpponentScore = uiState.opponentScore,
                        onAdjustPlayerScore = { adjustment ->
                            val newScore = (uiState.playerScore + adjustment).coerceAtLeast(0)
                            viewModel.updateScores(newScore, uiState.opponentScore)
                        },
                        onAdjustOpponentScore = { adjustment ->
                            val newScore = (uiState.opponentScore + adjustment).coerceAtLeast(0)
                            viewModel.updateScores(uiState.playerScore, newScore)
                        },
                        onDismiss = {
                            showDebugScoreDialog = false
                        }
                    )
                }
            }

            // Zone 3: Context-Sensitive Action Bar
            ActionBar(
                currentPhase = uiState.currentPhase,
                gameStarted = uiState.gameStarted,
                dealButtonEnabled = uiState.dealButtonEnabled,
                selectCribButtonEnabled = uiState.selectCribButtonEnabled,
                showHandCountingButton = uiState.showHandCountingButton,
                showGoButton = uiState.showGoButton,
                gameOver = uiState.gameOver,
                selectedCardsCount = uiState.selectedCards.size,
                isPlayerDealer = uiState.isPlayerDealer,
                onStartGame = { viewModel.startNewGame() },
                onEndGame = { viewModel.endGame() },
                onDeal = { viewModel.dealCards() },
                onSelectCrib = { viewModel.selectCardsForCrib() },
                onCountHands = { viewModel.countHands() },
                onGo = { viewModel.handlePlayerGo() },
                onReportBug = {
                    val body = BugReportUtils.buildBugReportBody(
                        context = context,
                        playerScore = uiState.playerScore,
                        opponentScore = uiState.opponentScore,
                        isPlayerDealer = uiState.isPlayerDealer,
                        starterCard = uiState.starterCard,
                        peggingCount = uiState.peggingState?.peggingCount ?: 0,
                        peggingPile = uiState.peggingState?.peggingPile ?: emptyList(),
                        playerHand = uiState.playerHand,
                        opponentHand = uiState.opponentHand,
                        cribHand = uiState.cribHand,
                        matchSummary = "${uiState.matchStats.gamesWon}-${uiState.matchStats.gamesLost} " +
                                "(Skunks ${uiState.matchStats.skunksFor}-${uiState.matchStats.skunksAgainst})",
                        gameStatus = uiState.gameStatus,
                        // Additional debug state
                        currentPhase = uiState.currentPhase,
                        gameStarted = uiState.gameStarted,
                        gameOver = uiState.gameOver,
                        isPlayerTurn = uiState.peggingState?.isPlayerTurn ?: false,
                        isPeggingPhase = uiState.peggingState?.isPeggingPhase ?: false,
                        isInHandCountingPhase = uiState.handCountingState?.isInHandCountingPhase ?: false,
                        selectedCards = uiState.selectedCards,
                        playerCardsPlayed = uiState.peggingState?.playerCardsPlayed ?: emptySet(),
                        opponentCardsPlayed = uiState.peggingState?.opponentCardsPlayed ?: emptySet(),
                        peggingDisplayPile = uiState.peggingState?.peggingDisplayPile ?: emptyList(),
                        dealButtonEnabled = uiState.dealButtonEnabled,
                        selectCribButtonEnabled = uiState.selectCribButtonEnabled,
                        playCardButtonEnabled = uiState.playCardButtonEnabled,
                        showHandCountingButton = uiState.showHandCountingButton,
                        showGoButton = uiState.showGoButton,
                        peggingManager = uiState.peggingState?.peggingManager,
                        countingPhase = uiState.handCountingState?.countingPhase ?: CountingPhase.NONE,
                        handScores = uiState.handCountingState?.handScores ?: HandScores(),
                        waitingForDialogDismissal = uiState.handCountingState?.waitingForDialogDismissal ?: false,
                        consecutiveGoes = uiState.peggingState?.consecutiveGoes ?: 0,
                        lastPlayerWhoPlayed = uiState.peggingState?.lastPlayerWhoPlayed
                    )
                    BugReportUtils.sendBugReportEmail(
                        context,
                        context.getString(R.string.feedback_email),
                        context.getString(R.string.bug_report_subject),
                        body
                    )
                }
            )

            // Zone 4: Cribbage Board (always visible)
            CribbageBoard(
                playerScore = uiState.playerScore,
                opponentScore = uiState.opponentScore
            )
        }

        // Winner modal at root level to cover entire screen including action bar
        if (uiState.showWinnerModal && uiState.winnerModalData != null) {
            WinnerModal(
                playerWon = uiState.winnerModalData!!.playerWon,
                playerScore = uiState.winnerModalData!!.playerScore,
                opponentScore = uiState.winnerModalData!!.opponentScore,
                wasSkunk = uiState.winnerModalData!!.wasSkunk,
                gamesWon = uiState.winnerModalData!!.gamesWon,
                gamesLost = uiState.winnerModalData!!.gamesLost,
                skunksFor = uiState.winnerModalData!!.skunksFor,
                skunksAgainst = uiState.winnerModalData!!.skunksAgainst,
                doubleSkunksFor = uiState.winnerModalData!!.doubleSkunksFor,
                doubleSkunksAgainst = uiState.winnerModalData!!.doubleSkunksAgainst,
                onDismiss = {
                    viewModel.dismissWinnerModal()
                }
            )
        }
    }
}

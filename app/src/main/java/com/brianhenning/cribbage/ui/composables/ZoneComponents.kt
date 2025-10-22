package com.brianhenning.cribbage.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brianhenning.cribbage.ui.screens.Card as CribbageCard

/**
 * Zone 1: Compact Score Header
 * Always visible at the top of the screen
 * Shows player/opponent scores with progress bars and dealer indicator
 * Shows starter card in top-right corner when available
 */
@Composable
fun CompactScoreHeader(
    playerScore: Int,
    opponentScore: Int,
    isPlayerDealer: Boolean,
    starterCard: CribbageCard?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .padding(end = if (starterCard != null) 60.dp else 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player score section
                ScoreSection(
                    label = "You",
                    score = playerScore,
                    isDealer = isPlayerDealer,
                    isPlayer = true,
                    modifier = Modifier.weight(1f)
                )

                // Divider
                VerticalDivider(
                    modifier = Modifier
                        .height(40.dp)
                        .padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                // Opponent score section
                ScoreSection(
                    label = "Opponent",
                    score = opponentScore,
                    isDealer = !isPlayerDealer,
                    isPlayer = false,
                    modifier = Modifier.weight(1f)
                )
            }

            // Starter card in top-right corner (no label)
            if (starterCard != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                ) {
                    GameCard(
                        card = starterCard,
                        isRevealed = true,
                        isClickable = false,
                        cardSize = CardSize.Small
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreSection(
    label: String,
    score: Int,
    isDealer: Boolean,
    isPlayer: Boolean,
    modifier: Modifier = Modifier
) {
    // Use blue for player, red for opponent
    val scoreColor = if (isPlayer) Color.Blue else Color.Red

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = scoreColor
            )

            // Dealer indicator
            if (isDealer) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = "Dealer",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        Text(
            text = score.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = scoreColor
        )

        // Progress bar (score out of 121)
        LinearProgressIndicator(
            progress = { (score / 121f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = scoreColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

/**
 * Zone 2: Dynamic Game Area Content
 * Changes based on current game phase
 */
@Composable
fun GameAreaContent(
    currentPhase: GamePhase,
    // Cut phase params
    cutPlayerCard: CribbageCard?,
    cutOpponentCard: CribbageCard?,
    // Game area params
    opponentHand: List<CribbageCard>,
    opponentCardsPlayed: Set<Int>,
    starterCard: CribbageCard?,
    peggingCount: Int,
    peggingPile: List<CribbageCard>,
    playerHand: List<CribbageCard>,
    playerCardsPlayed: Set<Int>,
    selectedCards: Set<Int>,
    cribHand: List<CribbageCard>,
    isPlayerDealer: Boolean,
    isPlayerTurn: Boolean,
    gameStatus: String,
    onCardClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        when (currentPhase) {
            GamePhase.SETUP -> {
                // Show cut cards if available
                AnimatedVisibility(
                    visible = cutPlayerCard != null && cutOpponentCard != null,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    if (cutPlayerCard != null && cutOpponentCard != null) {
                        CutForDealerDisplay(
                            playerCard = cutPlayerCard,
                            opponentCard = cutOpponentCard
                        )
                    }
                }
            }

            GamePhase.CRIB_SELECTION -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Opponent hand (face down)
                    CompactHandDisplay(
                        hand = opponentHand,
                        playedCards = emptySet(),
                        showCards = false,
                        label = "Opponent"
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Status message
                    Text(
                        text = gameStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Player hand (selectable)
                    PlayerHandCompact(
                        hand = playerHand,
                        selectedCards = selectedCards,
                        playedCards = playerCardsPlayed,
                        onCardClick = onCardClick
                    )
                }
            }

            GamePhase.PEGGING -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Opponent hand (face down with played indicators)
                    CompactHandDisplay(
                        hand = opponentHand,
                        playedCards = opponentCardsPlayed,
                        showCards = false,
                        label = "Opponent"
                    )

                    // Pegging count (large and prominent)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Count: $peggingCount",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // Pegging pile (compact inline)
                    if (peggingPile.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy((-10).dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            itemsIndexed(peggingPile) { _, card ->
                                GameCard(
                                    card = card,
                                    isRevealed = true,
                                    isClickable = false,
                                    cardSize = CardSize.Small
                                )
                            }
                        }
                    }

                    // Turn indicator (starter card moved to header)
                    Text(
                        text = if (isPlayerTurn) "Your turn" else "Opponent's turn",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPlayerTurn)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondary
                    )

                    // Player hand (clickable during pegging)
                    PlayerHandCompact(
                        hand = playerHand,
                        selectedCards = selectedCards,
                        playedCards = playerCardsPlayed,
                        onCardClick = onCardClick
                    )
                }
            }

            GamePhase.HAND_COUNTING -> {
                // This will be handled by the existing HandCountingDisplay
                // For now, show a simple message
                Text(
                    text = "Counting hands...",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            GamePhase.DEALING -> {
                // Show a simple dealing message
                Text(
                    text = "Dealing cards...",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            GamePhase.GAME_OVER -> {
                // Show game over message
                Text(
                    text = "Game Over!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CutForDealerDisplay(
    playerCard: CribbageCard,
    opponentCard: CribbageCard,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "You",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    GameCard(
                        card = playerCard,
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
                        text = "Opponent",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    GameCard(
                        card = opponentCard,
                        isRevealed = true,
                        isClickable = false,
                        cardSize = CardSize.Medium
                    )
                }
            }

            Text(
                text = "Lower card deals first",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompactHandDisplay(
    hand: List<CribbageCard>,
    playedCards: Set<Int>,
    showCards: Boolean,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy((-15).dp)
        ) {
            itemsIndexed(hand) { index, card ->
                GameCard(
                    card = card,
                    isRevealed = showCards,
                    isPlayed = playedCards.contains(index),
                    isClickable = false,
                    cardSize = CardSize.Small
                )
            }
        }

        // Show remaining card count
        if (playedCards.isNotEmpty()) {
            Text(
                text = "(${hand.size - playedCards.size} left)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlayerHandCompact(
    hand: List<CribbageCard>,
    selectedCards: Set<Int>,
    playedCards: Set<Int>,
    onCardClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy((-15).dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        itemsIndexed(hand) { index, card ->
            GameCard(
                card = card,
                isSelected = selectedCards.contains(index),
                isPlayed = playedCards.contains(index),
                isRevealed = true,
                isClickable = !playedCards.contains(index),
                onClick = { onCardClick(index) },
                cardSize = CardSize.Medium
            )
        }
    }
}

/**
 * Zone 3: Context-Sensitive Action Bar
 * Shows 1-3 buttons based on current game phase
 */
@Composable
fun ActionBar(
    currentPhase: GamePhase,
    gameStarted: Boolean,
    dealButtonEnabled: Boolean,
    selectCribButtonEnabled: Boolean,
    showHandCountingButton: Boolean,
    gameOver: Boolean,
    selectedCardsCount: Int,
    onStartGame: () -> Unit,
    onEndGame: () -> Unit,
    onDeal: () -> Unit,
    onSelectCrib: () -> Unit,
    onCountHands: () -> Unit,
    onReportBug: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            !gameStarted -> {
                Button(
                    onClick = onStartGame,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Start New Game")
                }
            }

            gameOver -> {
                Button(
                    onClick = onStartGame,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("New Game")
                }
            }

            dealButtonEnabled -> {
                Button(
                    onClick = onDeal,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Deal Cards")
                }
                OutlinedButton(
                    onClick = onEndGame,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("End Game")
                }
            }

            selectCribButtonEnabled -> {
                Button(
                    onClick = onSelectCrib,
                    enabled = selectedCardsCount == 2,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Discard to Crib")
                }
                OutlinedButton(
                    onClick = onReportBug,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Report Bug")
                }
            }

            showHandCountingButton -> {
                Button(
                    onClick = onCountHands,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Count Hands")
                }
            }

            currentPhase == GamePhase.PEGGING -> {
                // No buttons during pegging (or show "Go" if needed)
                OutlinedButton(
                    onClick = onReportBug,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Report Bug")
                }
            }
        }
    }
}

/**
 * Zone 4: Visual Cribbage Board
 * Always visible at bottom, shows peg positions
 */
@Composable
fun CribbageBoard(
    playerScore: Int,
    opponentScore: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cribbage board visualization
            CribbageBoardTrack(
                playerScore = playerScore,
                opponentScore = opponentScore,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Score markers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "30",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "60",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "90",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "121",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CribbageBoardTrack(
    playerScore: Int,
    opponentScore: Int,
    modifier: Modifier = Modifier
) {
    val playerPegPosition by animateFloatAsState(
        targetValue = (playerScore / 121f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "player_peg_position"
    )

    val opponentPegPosition by animateFloatAsState(
        targetValue = (opponentScore / 121f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "opponent_peg_position"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val trackHeight = height / 3
        val padding = 20f

        // Player track (top)
        val playerTrackY = trackHeight
        drawLine(
            color = Color.Blue.copy(alpha = 0.3f),
            start = Offset(padding, playerTrackY),
            end = Offset(width - padding, playerTrackY),
            strokeWidth = 8f
        )

        // Player peg
        val playerPegX = padding + (width - 2 * padding) * playerPegPosition
        drawCircle(
            color = Color.Blue,
            radius = 12f,
            center = Offset(playerPegX, playerTrackY)
        )

        // Opponent track (bottom)
        val opponentTrackY = height - trackHeight
        drawLine(
            color = Color.Red.copy(alpha = 0.3f),
            start = Offset(padding, opponentTrackY),
            end = Offset(width - padding, opponentTrackY),
            strokeWidth = 8f
        )

        // Opponent peg
        val opponentPegX = padding + (width - 2 * padding) * opponentPegPosition
        drawCircle(
            color = Color.Red,
            radius = 12f,
            center = Offset(opponentPegX, opponentTrackY)
        )

        // Draw milestone markers (every 30 points)
        for (i in 0..4) {
            val markerX = padding + (width - 2 * padding) * (i / 4f)
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(markerX, playerTrackY - 15f),
                end = Offset(markerX, opponentTrackY + 15f),
                strokeWidth = 2f
            )
        }
    }
}

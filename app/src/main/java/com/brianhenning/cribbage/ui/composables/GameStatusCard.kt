package com.brianhenning.cribbage.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun GameStatusCard(
    gameStatus: String,
    currentPhase: GamePhase,
    isPlayerTurn: Boolean,
    showInstructions: Boolean = true,
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GamePhaseIndicator(
                currentPhase = currentPhase,
                isPlayerTurn = isPlayerTurn
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            
            Text(
                text = "Game Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = gameStatus,
                    modifier = Modifier
                        .padding(10.dp)
                        .heightIn(max = 100.dp)
                        .verticalScroll(rememberScrollState()),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }

            // Show instructions hint based on current phase
            AnimatedVisibility(visible = showInstructions && currentPhase.instructionHint != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = currentPhase.instructionHint ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GamePhaseIndicator(
    currentPhase: GamePhase,
    isPlayerTurn: Boolean,
    modifier: Modifier = Modifier
) {
    val turnAlpha by animateFloatAsState(
        targetValue = if (isPlayerTurn) 1.0f else 0.6f,
        animationSpec = tween(500),
        label = "turn_alpha"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Current Phase",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = currentPhase.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        AnimatedVisibility(
            visible = currentPhase.showTurnIndicator,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isPlayerTurn) Icons.Default.Person else Icons.Default.PersonOutline,
                    contentDescription = if (isPlayerTurn) "Your turn" else "Opponent's turn",
                    tint = if (isPlayerTurn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(turnAlpha)
                )
                Text(
                    text = if (isPlayerTurn) "Your Turn" else "Opponent's Turn",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isPlayerTurn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(turnAlpha)
                )
            }
        }
    }
}

@Composable
fun ScoreDisplay(
    playerScore: Int,
    opponentScore: Int,
    isPlayerDealer: Boolean = false,
    modifier: Modifier = Modifier,
    onTripleTap: (() -> Unit)? = null
) {
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    // Reset tap count after 1 second of no taps
    LaunchedEffect(tapCount) {
        if (tapCount > 0) {
            delay(1000)
            tapCount = 0
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onTripleTap != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastTapTime < 500) {
                                    tapCount++
                                    if (tapCount >= 3) {
                                        onTripleTap()
                                        tapCount = 0
                                    }
                                } else {
                                    tapCount = 1
                                }
                                lastTapTime = currentTime
                            }
                        )
                    }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreColumn(
                label = "You",
                score = playerScore,
                isDealer = isPlayerDealer
            )

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(50.dp)
                    .background(
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(1.dp)
                    )
            )

            ScoreColumn(
                label = "Opponent",
                score = opponentScore,
                isDealer = !isPlayerDealer
            )
        }
    }
}

@Composable
private fun ScoreColumn(
    label: String,
    score: Int,
    isDealer: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (isDealer) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = "Dealer",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        LinearProgressIndicator(
            progress = { (score / 121f).coerceAtMost(1.0f) },
            modifier = Modifier
                .width(80.dp)
                .height(6.dp),
            color = if (score >= 121) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

enum class GamePhase(val displayName: String, val showTurnIndicator: Boolean, val instructionHint: String? = null) {
    SETUP("Game Setup", false, "Tap \"Start New Game\" to begin"),
    DEALING("Dealing Cards", false, "Tap \"Deal Cards\" to deal 6 cards to each player"),
    CRIB_SELECTION("Selecting for Crib", false, "Select 2 cards by tapping them, then tap the button to place them in the crib"),
    PEGGING("Pegging", true, "Tap a card to immediately play it"),
    HAND_COUNTING("Counting Hands", false, null),
    GAME_OVER("Game Over", false, "Tap \"Start New Game\" to play again")
}
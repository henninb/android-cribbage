package com.brianhenning.cribbage.ui.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brianhenning.cribbage.shared.domain.model.Card as CribbageCard
import com.brianhenning.cribbage.ui.theme.LocalSeasonalTheme
import kotlinx.coroutines.delay

/**
 * Zone 1: Compact Score Header
 * Always visible at the top of the screen
 * Shows player/opponent scores with progress bars and dealer indicator
 * Shows starter card in top-right corner when available
 * Shows current seasonal theme
 */
@Composable
fun CompactScoreHeader(
    playerScore: Int,
    opponentScore: Int,
    isPlayerDealer: Boolean,
    starterCard: CribbageCard?,
    playerScoreAnimation: ScoreAnimationState? = null,
    opponentScoreAnimation: ScoreAnimationState? = null,
    onAnimationComplete: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    onTripleTap: (() -> Unit)? = null
) {
    val currentTheme = LocalSeasonalTheme.current
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player score section with animation overlay
                Box(modifier = Modifier.weight(1f)) {
                    ScoreSection(
                        label = "You",
                        score = playerScore,
                        isDealer = isPlayerDealer,
                        isPlayer = true
                    )

                    // Player score animation (positioned to the right of score)
                    if (playerScoreAnimation != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp)
                        ) {
                            PeggingScoreAnimation(
                                points = playerScoreAnimation.points,
                                isPlayer = playerScoreAnimation.isPlayer,
                                onAnimationComplete = { onAnimationComplete(true) }
                            )
                        }
                    }
                }

                // Divider
                VerticalDivider(
                    modifier = Modifier
                        .height(40.dp)
                        .padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                // Opponent score section with animation overlay
                Box(modifier = Modifier.weight(1f)) {
                    ScoreSection(
                        label = "Opponent",
                        score = opponentScore,
                        isDealer = !isPlayerDealer,
                        isPlayer = false
                    )

                    // Opponent score animation (positioned to the left of score)
                    if (opponentScoreAnimation != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterStart)
                                .padding(start = 8.dp)
                        ) {
                            PeggingScoreAnimation(
                                points = opponentScoreAnimation.points,
                                isPlayer = opponentScoreAnimation.isPlayer,
                                onAnimationComplete = { onAnimationComplete(false) }
                            )
                        }
                    }
                }
            }

            // Theme indicator in top-left corner
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 4.dp, top = 4.dp)
            ) {
                Text(
                    text = "${currentTheme.icon} ${currentTheme.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun ScoreSection(
    label: String,
    score: Int,
    isDealer: Boolean,
    isPlayer: Boolean
) {
    val currentTheme = LocalSeasonalTheme.current

    // Use theme colors for player/opponent
    // Player uses primary color, Opponent uses secondary color
    val scoreColor = if (isPlayer) {
        currentTheme.colors.primary
    } else {
        currentTheme.colors.secondary
    }

    Column(
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
                color = scoreColor,
                fontWeight = FontWeight.Bold
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
 * Zone 4: Visual Cribbage Board
 * Always visible at bottom, shows peg positions
 */
@Composable
fun CribbageBoard(
    playerScore: Int,
    opponentScore: Int,
    modifier: Modifier = Modifier
) {
    val currentTheme = LocalSeasonalTheme.current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = currentTheme.colors.surface
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "30",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "60",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "90",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "121",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
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

    // Get theme colors - use seasonal theme for consistency with header
    val currentTheme = LocalSeasonalTheme.current
    val boardPrimaryColor = currentTheme.colors.boardPrimary
    val boardSecondaryColor = currentTheme.colors.boardSecondary
    val outlineColor = MaterialTheme.colorScheme.outline

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val trackHeight = height / 3
        val padding = 20f

        // Player track (top) - using primary board color
        val playerTrackY = trackHeight
        drawLine(
            color = boardPrimaryColor.copy(alpha = 0.4f),
            start = Offset(padding, playerTrackY),
            end = Offset(width - padding, playerTrackY),
            strokeWidth = 8f
        )

        // Player peg - solid primary color
        val playerPegX = padding + (width - 2 * padding) * playerPegPosition
        drawCircle(
            color = boardPrimaryColor,
            radius = 12f,
            center = Offset(playerPegX, playerTrackY)
        )

        // Opponent track (bottom) - using secondary board color
        val opponentTrackY = height - trackHeight
        drawLine(
            color = boardSecondaryColor.copy(alpha = 0.4f),
            start = Offset(padding, opponentTrackY),
            end = Offset(width - padding, opponentTrackY),
            strokeWidth = 8f
        )

        // Opponent peg - solid secondary color
        val opponentPegX = padding + (width - 2 * padding) * opponentPegPosition
        drawCircle(
            color = boardSecondaryColor,
            radius = 12f,
            center = Offset(opponentPegX, opponentTrackY)
        )

        // Draw milestone markers (every 30 points) - using outline color
        for (i in 0..4) {
            val markerX = padding + (width - 2 * padding) * (i / 4f)
            drawLine(
                color = outlineColor.copy(alpha = 0.5f),
                start = Offset(markerX, playerTrackY - 15f),
                end = Offset(markerX, opponentTrackY + 15f),
                strokeWidth = 2f
            )
        }
    }
}

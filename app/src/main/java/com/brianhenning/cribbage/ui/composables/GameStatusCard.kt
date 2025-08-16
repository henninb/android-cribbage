package com.brianhenning.cribbage.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun GameStatusCard(
    gameStatus: String,
    currentPhase: GamePhase,
    isPlayerTurn: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GamePhaseIndicator(
                currentPhase = currentPhase,
                isPlayerTurn = isPlayerTurn
            )
            
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            
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
                        .padding(12.dp)
                        .heightIn(max = 120.dp)
                        .verticalScroll(rememberScrollState()),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreColumn(
                label = "You",
                score = playerScore,
                isPlayer = true
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
                isPlayer = false
            )
        }
    }
}

@Composable
private fun ScoreColumn(
    label: String,
    score: Int,
    isPlayer: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        LinearProgressIndicator(
            progress = (score / 121f).coerceAtMost(1.0f),
            modifier = Modifier
                .width(80.dp)
                .height(6.dp),
            color = if (score >= 121) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

enum class GamePhase(val displayName: String, val showTurnIndicator: Boolean) {
    SETUP("Game Setup", false),
    DEALING("Dealing Cards", false),
    CRIB_SELECTION("Selecting for Crib", false),
    PEGGING("Pegging", true),
    HAND_COUNTING("Counting Hands", false),
    GAME_OVER("Game Over", false)
}
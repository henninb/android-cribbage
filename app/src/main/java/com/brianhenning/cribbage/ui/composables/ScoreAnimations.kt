package com.brianhenning.cribbage.ui.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brianhenning.cribbage.ui.theme.LocalSeasonalTheme
import kotlinx.coroutines.delay

/**
 * Data class to represent an active score animation
 */
data class ScoreAnimationState(
    val points: Int,
    val isPlayer: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Thirty-One Banner Animation
 * Shows a celebratory "31!" banner when count reaches exactly 31
 */
@Composable
fun ThirtyOneBannerAnimation(
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationStarted = true
        delay(1500) // Hold for 1.5 seconds
        onAnimationComplete()
    }

    // Scale animation: pop in with bounce
    val scale by animateFloatAsState(
        targetValue = if (animationStarted) 1.3f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "banner_scale"
    )

    // Flash background color animation
    val backgroundColor by animateColorAsState(
        targetValue = if (animationStarted) Color(0xFFFFD700).copy(alpha = 0.9f) else Color.Transparent,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "banner_background"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        ) {
            Text(
                text = "31!",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1565C0), // Deep blue for contrast on gold
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
            )
        }
    }
}

/**
 * Score Animation
 * Shows a "+X" animation that pops up and fades out
 * Used for both pegging and hand counting scores
 */
@Composable
fun ScoreAnimation(
    points: Int,
    isPlayer: Boolean,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Get current seasonal theme
    val currentTheme = LocalSeasonalTheme.current

    // Animation color: primary for player, secondary for opponent (matches theme)
    val animationColor = if (isPlayer) {
        currentTheme.colors.primary
    } else {
        currentTheme.colors.secondary
    }

    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationStarted = true
    }

    // Scale animation: pop from 0 to 1.5, then settle to 1
    val scale by animateFloatAsState(
        targetValue = if (animationStarted) 1.2f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "score_scale"
    )

    // Fade animation: fade out after showing
    val alpha by animateFloatAsState(
        targetValue = if (animationStarted) 0f else 1f,
        animationSpec = tween(
            durationMillis = 2500,
            delayMillis = 300,
            easing = LinearEasing
        ),
        label = "score_alpha",
        finishedListener = {
            onAnimationComplete()
        }
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+$points",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = animationColor.copy(alpha = alpha),
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )
    }
}

/**
 * Deprecated: Use ScoreAnimation instead
 * Alias for backward compatibility
 */
@Deprecated(
    message = "Use ScoreAnimation instead",
    replaceWith = ReplaceWith("ScoreAnimation(points, isPlayer, onAnimationComplete, modifier)")
)
@Composable
fun PeggingScoreAnimation(
    points: Int,
    isPlayer: Boolean,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScoreAnimation(points, isPlayer, onAnimationComplete, modifier)
}

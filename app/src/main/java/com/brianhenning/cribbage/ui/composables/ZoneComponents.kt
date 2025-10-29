package com.brianhenning.cribbage.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brianhenning.cribbage.shared.domain.model.Card as CribbageCard
import com.brianhenning.cribbage.ui.theme.LocalSeasonalTheme

/**
 * Data class to represent an active score animation
 */
data class ScoreAnimationState(
    val points: Int,
    val isPlayer: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Pegging Round Acknowledgment UI
 * Shows pile, count, score when 31 or Go occurs
 * Requires user to tap "Next Round" button to continue
 */
@Composable
fun PeggingRoundAcknowledgment(
    pile: List<CribbageCard>,
    finalCount: Int,
    scoreAwarded: Int,
    onNextRound: () -> Unit,
    modifier: Modifier = Modifier,
    playerCardsRemaining: Int = 0,
    opponentCardsRemaining: Int = 0
) {
    val currentTheme = LocalSeasonalTheme.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = currentTheme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Text(
                text = if (finalCount == 31) "31!" else "Go!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = currentTheme.colors.primary
            )

            // Final count
            Text(
                text = "Count: $finalCount",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Score awarded
            if (scoreAwarded > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = currentTheme.colors.primary.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = "+$scoreAwarded",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.colors.primary
                    )
                }
            }

            // Cards remaining
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "You:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$playerCardsRemaining cards left",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Opponent:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$opponentCardsRemaining cards left",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Pegging pile
            if (pile.isNotEmpty()) {
                Text(
                    text = "Cards Played:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy((-20).dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    itemsIndexed(pile) { _, card ->
                        GameCard(
                            card = card,
                            isRevealed = true,
                            isClickable = false,
                            cardSize = CardSize.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // OK button to acknowledge and continue
            Button(
                onClick = onNextRound,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentTheme.colors.primary
                )
            ) {
                Text(
                    text = "OK",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Thirty-One Banner Animation
 * Shows a celebratory "31!" banner when count reaches exactly 31
 */
@Composable
fun ThirtyOneBannerAnimation(
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTheme = LocalSeasonalTheme.current

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
 * Pegging Score Animation
 * Shows a "+X" animation that pops up and fades out
 */
@Composable
fun PeggingScoreAnimation(
    points: Int,
    isPlayer: Boolean,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTheme = LocalSeasonalTheme.current

    // Animation color: green for player, orange for opponent
    val animationColor = if (isPlayer) {
        Color(0xFF4CAF50) // Green
    } else {
        Color(0xFFFF9800) // Orange
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
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .padding(end = if (starterCard != null) 60.dp else 0.dp),
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
    showWelcomeScreen: Boolean,
    onCardClick: (Int) -> Unit,
    show31Banner: Boolean = false,
    onBannerComplete: () -> Unit = {},
    pendingReset: com.brianhenning.cribbage.ui.screens.PendingResetState? = null,
    onNextRound: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Show pending reset UI if it exists (takes priority over other displays)
        if (pendingReset != null) {
            PeggingRoundAcknowledgment(
                pile = pendingReset.pile,
                finalCount = pendingReset.finalCount,
                scoreAwarded = pendingReset.scoreAwarded,
                onNextRound = onNextRound,
                modifier = Modifier.align(Alignment.Center),
                playerCardsRemaining = playerHand.size - playerCardsPlayed.size,
                opponentCardsRemaining = opponentHand.size - opponentCardsPlayed.size
            )
            return@Box
        }

        when (currentPhase) {
            GamePhase.SETUP -> {
                // Show welcome screen only on first app start, otherwise show cut cards
                if (showWelcomeScreen) {
                    WelcomeHomeScreen()
                } else if (cutPlayerCard != null && cutOpponentCard != null) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
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
                        onCardClick = onCardClick,
                        isPlayerTurn = true  // Always clickable during crib selection
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
                    val currentTheme = LocalSeasonalTheme.current
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = currentTheme.colors.boardPrimary
                        )
                    ) {
                        Text(
                            text = "Count: $peggingCount",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.colors.accentLight
                        )
                    }

                    // Pegging pile (compact inline)
                    if (peggingPile.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy((-20).dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            itemsIndexed(peggingPile) { _, card ->
                                GameCard(
                                    card = card,
                                    isRevealed = true,
                                    isClickable = false,
                                    cardSize = CardSize.Medium
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
                        onCardClick = onCardClick,
                        isPlayerTurn = isPlayerTurn  // Only clickable when it's player's turn
                    )
                }
            }

            GamePhase.HAND_COUNTING -> {
                // This will be handled by the existing HandCountingDisplay
                // No message shown during hand counting phase
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

        // Show "31!" banner overlay when count reaches 31
        if (show31Banner) {
            ThirtyOneBannerAnimation(
                onAnimationComplete = onBannerComplete,
                modifier = Modifier.align(Alignment.Center)
            )
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
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Label above cards
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Show remaining card count
            if (playedCards.isNotEmpty()) {
                Text(
                    text = "(${hand.size - playedCards.size} left)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Cards row with increased overlap to fit all cards on screen
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy((-45).dp),  // Increased from -30
            contentPadding = PaddingValues(horizontal = 12.dp)  // Reduced from 16
        ) {
            itemsIndexed(hand) { index, card ->
                GameCard(
                    card = card,
                    isRevealed = showCards,
                    isPlayed = playedCards.contains(index),
                    isClickable = false,
                    cardSize = CardSize.Large
                )
            }
        }
    }
}

@Composable
private fun PlayerHandCompact(
    hand: List<CribbageCard>,
    selectedCards: Set<Int>,
    playedCards: Set<Int>,
    onCardClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isPlayerTurn: Boolean = true
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy((-45).dp),  // Increased from -30
        contentPadding = PaddingValues(horizontal = 12.dp)  // Reduced from 16
    ) {
        itemsIndexed(hand) { index, card ->
            GameCard(
                card = card,
                isSelected = selectedCards.contains(index),
                isPlayed = playedCards.contains(index),
                isRevealed = true,
                isClickable = isPlayerTurn && !playedCards.contains(index),
                onClick = { onCardClick(index) },
                cardSize = CardSize.Large
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
    showGoButton: Boolean,
    gameOver: Boolean,
    selectedCardsCount: Int,
    isPlayerDealer: Boolean,
    onStartGame: () -> Unit,
    onEndGame: () -> Unit,
    onDeal: () -> Unit,
    onSelectCrib: () -> Unit,
    onCountHands: () -> Unit,
    onGo: () -> Unit,
    onReportBug: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTheme = LocalSeasonalTheme.current

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
                    Text(if (isPlayerDealer) "My Crib" else "Opponent's Crib")
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
                        containerColor = currentTheme.colors.primary
                    )
                ) {
                    Text("Count Hands")
                }
            }

            showGoButton -> {
                Button(
                    onClick = onGo,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Go")
                }
                OutlinedButton(
                    onClick = onReportBug,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Report Bug")
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

/**
 * Cut Card Display - Shows the cut card before pegging begins
 */
@Composable
fun CutCardDisplay(
    cutCard: CribbageCard,
    playerScore: Int,
    opponentScore: Int,
    isJack: Boolean,
    dealerGetsPoints: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTheme = LocalSeasonalTheme.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(
                containerColor = currentTheme.colors.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "Cut Card",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = currentTheme.colors.primary
                )

                // Cut card display
                GameCard(
                    card = cutCard,
                    isRevealed = true,
                    isClickable = false,
                    cardSize = CardSize.Large
                )

                // Card description
                Text(
                    text = cutCard.getSymbol(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // His Heels bonus if Jack
                if (isJack) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = currentTheme.colors.primary.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "His Heels!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = currentTheme.colors.primary
                            )
                            Text(
                                text = if (dealerGetsPoints) "Dealer gets 2 points" else "+2 Points",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Current scores
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "You",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$playerScore",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "—",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Opponent",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$opponentScore",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Continue button
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = currentTheme.colors.primary
                    )
                ) {
                    Text(
                        text = "Start Pegging",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Winner Modal - Shown when a game is won
 * Modal overlay that requires user acknowledgment via OK button
 * Uses scrollable middle section with fixed header and button (similar to HandCountingDisplay)
 */
@Composable
fun WinnerModal(
    playerWon: Boolean,
    playerScore: Int,
    opponentScore: Int,
    wasSkunk: Boolean,
    gamesWon: Int,
    gamesLost: Int,
    skunksFor: Int,
    skunksAgainst: Int,
    doubleSkunksFor: Int = 0,
    doubleSkunksAgainst: Int = 0,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                // Consume all touch events to prevent background interaction
                detectTapGestures { /* Do nothing - just consume the tap */ }
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // FIXED HEADER: Winner celebration icon and announcement
                Text(
                    text = if (playerWon) "\uD83C\uDFC6" else "\uD83D\uDE14",
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 64.sp
                )

                Text(
                    text = if (playerWon) "You Win!" else "Opponent Wins!",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (playerWon)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary
                )

                // Skunk indicator (if applicable)
                if (wasSkunk) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "\uD83E\uDDA8",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "SKUNK!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )

                // SCROLLABLE MIDDLE SECTION: Scores and stats
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Final scores
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "You",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = playerScore.toString(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "-",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Opponent",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = opponentScore.toString(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    item {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }

                    // Match statistics
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Match Record",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = "$gamesWon - $gamesLost",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold
                                )

                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outline
                                )

                                // Skunks row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Skunks",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$skunksFor - $skunksAgainst",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // Double Skunks row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Double Skunks",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$doubleSkunksFor - $doubleSkunksAgainst",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // FIXED BOTTOM: Dismiss button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "OK",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Welcome Home Screen - Shown before game starts
 */
@Composable
fun WelcomeHomeScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App icon/logo area
        Card(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🃏",
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 72.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // App title
        Text(
            text = "Cribbage",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Classic Card Game",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Welcome message
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Play cribbage against the computer. Be the first to reach 121 points!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Instruction hint
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "👇",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Tap \"Start New Game\" below to begin",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

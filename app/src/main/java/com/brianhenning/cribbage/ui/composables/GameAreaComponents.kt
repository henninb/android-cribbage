package com.brianhenning.cribbage.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brianhenning.cribbage.shared.domain.model.Card as CribbageCard
import com.brianhenning.cribbage.game.state.PendingResetState
import com.brianhenning.cribbage.ui.theme.LocalSeasonalTheme

/**
 * Calculate optimal card overlap based on number of cards, card size, and available width
 * Uses a hybrid approach: overlaps cards, and scales down if necessary
 *
 * @param cardCount Number of cards to display
 * @param cardWidth Width of a single card in dp
 * @param availableWidth Available screen width in dp
 * @param minOverlap Minimum overlap in dp (more overlap = cards closer together)
 * @param maxOverlap Maximum overlap in dp (cards can't overlap more than this)
 * @return Pair of (overlap in dp, scale factor)
 */
internal fun calculateCardOverlapAndScale(
    cardCount: Int,
    cardWidth: Float,
    availableWidth: Float,
    minOverlap: Float = 10f,
    maxOverlap: Float = 60f
): Pair<Float, Float> {
    if (cardCount <= 1) return Pair(0f, 1f)

    // Calculate needed total width with minimum overlap
    val minTotalWidth = cardWidth + (cardCount - 1) * (cardWidth - maxOverlap)

    // If we fit with max overlap, calculate exact overlap needed
    if (minTotalWidth <= availableWidth) {
        // Solve: cardWidth + (n-1) * (cardWidth - overlap) = availableWidth
        val neededOverlap = ((cardWidth * cardCount - availableWidth) / (cardCount - 1))
            .coerceIn(minOverlap, maxOverlap)
        return Pair(neededOverlap, 1f)
    }

    // If we don't fit even with max overlap, we need to scale down
    val scaleNeeded = availableWidth / minTotalWidth
    return Pair(maxOverlap, scaleNeeded.coerceIn(0.65f, 1f)) // Don't scale smaller than 65%
}

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

                // Calculate dynamic overlap based on pile size and screen width
                val configuration = LocalConfiguration.current
                val screenWidth = configuration.screenWidthDp.toFloat()
                val availableWidth = screenWidth - 64f // Account for card padding and modal padding
                val (minOverlapDp, maxOverlapDp) = when (pile.size) {
                    in 4..6 -> 30f to 45f // moderate overlap for 4–6 cards
                    else -> 10f to 65f
                }

                val (overlap, scale) = calculateCardOverlapAndScale(
                    cardCount = pile.size,
                    cardWidth = CardSize.Medium.width.value,
                    availableWidth = availableWidth,
                    minOverlap = minOverlapDp,
                    maxOverlap = maxOverlapDp
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy((-overlap).dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
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
    pendingReset: PendingResetState? = null,
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

                    // Pegging pile (compact inline with dynamic overlap)
                    if (peggingPile.isNotEmpty()) {
                        // Calculate dynamic overlap based on pile size and screen width
                        val configuration = LocalConfiguration.current
                        val screenWidth = configuration.screenWidthDp.toFloat()
                        val availableWidth = screenWidth - 32f // Account for card padding
                        val (overlap, scale) = calculateCardOverlapAndScale(
                            cardCount = peggingPile.size,
                            cardWidth = CardSize.Medium.width.value,
                            availableWidth = availableWidth,
                            minOverlap = 10f,
                            maxOverlap = 55f
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy((-overlap).dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
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

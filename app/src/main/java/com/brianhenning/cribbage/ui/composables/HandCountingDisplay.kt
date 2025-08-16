package com.brianhenning.cribbage.ui.composables

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brianhenning.cribbage.ui.screens.Card as CribbageCard

@Composable
fun HandCountingDisplay(
    playerHand: List<CribbageCard>,
    opponentHand: List<CribbageCard>,
    cribHand: List<CribbageCard>,
    starterCard: CribbageCard?,
    isPlayerDealer: Boolean,
    currentCountingPhase: CountingPhase,
    handScores: HandScores,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Hand Counting Phase",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        starterCard?.let { starter ->
            StarterCardDisplay(
                starterCard = starter,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        
        // Non-dealer hand counting
        HandCountingCard(
            title = if (isPlayerDealer) "Opponent's Hand (Non-Dealer)" else "Your Hand (Non-Dealer)",
            hand = if (isPlayerDealer) opponentHand else playerHand,
            starterCard = starterCard,
            isCurrentlyCounting = currentCountingPhase == CountingPhase.NON_DEALER,
            isCompleted = currentCountingPhase.ordinal > CountingPhase.NON_DEALER.ordinal,
            score = handScores.nonDealerScore,
            breakdown = handScores.nonDealerBreakdown,
            isHighlighted = !isPlayerDealer
        )
        
        // Dealer hand counting
        HandCountingCard(
            title = if (isPlayerDealer) "Your Hand (Dealer)" else "Opponent's Hand (Dealer)",
            hand = if (isPlayerDealer) playerHand else opponentHand,
            starterCard = starterCard,
            isCurrentlyCounting = currentCountingPhase == CountingPhase.DEALER,
            isCompleted = currentCountingPhase.ordinal > CountingPhase.DEALER.ordinal,
            score = handScores.dealerScore,
            breakdown = handScores.dealerBreakdown,
            isHighlighted = isPlayerDealer
        )
        
        // Crib counting
        CribCountingCard(
            title = if (isPlayerDealer) "Your Crib" else "Opponent's Crib",
            cribHand = cribHand,
            starterCard = starterCard,
            isCurrentlyCounting = currentCountingPhase == CountingPhase.CRIB,
            isCompleted = currentCountingPhase.ordinal > CountingPhase.CRIB.ordinal,
            score = handScores.cribScore,
            breakdown = handScores.cribBreakdown,
            isPlayerCrib = isPlayerDealer
        )
    }
}

@Composable
private fun HandCountingCard(
    title: String,
    hand: List<CribbageCard>,
    starterCard: CribbageCard?,
    isCurrentlyCounting: Boolean,
    isCompleted: Boolean,
    score: Int,
    breakdown: String,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (isCurrentlyCounting || isCompleted) 1.0f else 0.6f,
        animationSpec = tween(500),
        label = "hand_alpha"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isHighlighted && isCurrentlyCounting -> MaterialTheme.colorScheme.primaryContainer
                isCurrentlyCounting -> MaterialTheme.colorScheme.secondaryContainer
                isCompleted -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentlyCounting) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                AnimatedVisibility(
                    visible = isCurrentlyCounting,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Currently counting",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Hand cards with starter
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                itemsIndexed(hand) { index, card ->
                    GameCard(
                        card = card,
                        isRevealed = true,
                        isClickable = false,
                        cardSize = CardSize.Medium
                    )
                }
                
                // Add starter card with visual separator
                item {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(60.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        Divider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                starterCard?.let { starter ->
                    item {
                        GameCard(
                            card = starter,
                            isRevealed = true,
                            isClickable = false,
                            cardSize = CardSize.Medium
                        )
                    }
                }
            }
            
            // Score display
            AnimatedVisibility(
                visible = isCompleted,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Score:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = score.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        if (breakdown.isNotEmpty()) {
                            Text(
                                text = breakdown,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CribCountingCard(
    title: String,
    cribHand: List<CribbageCard>,
    starterCard: CribbageCard?,
    isCurrentlyCounting: Boolean,
    isCompleted: Boolean,
    score: Int,
    breakdown: String,
    isPlayerCrib: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (isCurrentlyCounting || isCompleted) 1.0f else 0.6f,
        animationSpec = tween(500),
        label = "crib_alpha"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isPlayerCrib && isCurrentlyCounting -> MaterialTheme.colorScheme.secondaryContainer
                isCurrentlyCounting -> MaterialTheme.colorScheme.tertiaryContainer
                isCompleted -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentlyCounting) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                AnimatedVisibility(
                    visible = isCurrentlyCounting,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Currently counting",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Crib cards
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                itemsIndexed(cribHand) { index, card ->
                    GameCard(
                        card = card,
                        isRevealed = true,
                        isClickable = false,
                        cardSize = CardSize.Medium
                    )
                }
                
                starterCard?.let { starter ->
                    item {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(60.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            Divider(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(2.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    item {
                        GameCard(
                            card = starter,
                            isRevealed = true,
                            isClickable = false,
                            cardSize = CardSize.Medium
                        )
                    }
                }
            }
            
            // Score display
            AnimatedVisibility(
                visible = isCompleted,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPlayerCrib) 
                            MaterialTheme.colorScheme.secondaryContainer 
                        else 
                            MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Crib Score:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = score.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        if (breakdown.isNotEmpty()) {
                            Text(
                                text = breakdown,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StarterCardDisplay(
    starterCard: CribbageCard,
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Starter Card",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            GameCard(
                card = starterCard,
                isRevealed = true,
                isClickable = false,
                cardSize = CardSize.Large
            )
        }
    }
}

enum class CountingPhase {
    NONE,
    NON_DEALER,
    DEALER,
    CRIB,
    COMPLETED
}

data class HandScores(
    val nonDealerScore: Int = 0,
    val nonDealerBreakdown: String = "",
    val dealerScore: Int = 0,
    val dealerBreakdown: String = "",
    val cribScore: Int = 0,
    val cribBreakdown: String = ""
)
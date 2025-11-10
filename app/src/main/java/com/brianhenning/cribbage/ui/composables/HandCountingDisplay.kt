package com.brianhenning.cribbage.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.brianhenning.cribbage.shared.domain.logic.DetailedScoreBreakdown
import com.brianhenning.cribbage.shared.domain.model.Card as CribbageCard

@Composable
fun HandCountingDisplay(
    playerHand: List<CribbageCard>,
    opponentHand: List<CribbageCard>,
    cribHand: List<CribbageCard>,
    starterCard: CribbageCard?,
    isPlayerDealer: Boolean,
    currentCountingPhase: CountingPhase,
    handScores: HandScores,
    onDialogDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf<ScoreDialogData?>(null) }

    // Show dialog when a hand is completed
    LaunchedEffect(currentCountingPhase, handScores) {
        when (currentCountingPhase) {
            CountingPhase.NON_DEALER -> {
                handScores.nonDealerBreakdown?.let { breakdown ->
                    showDialog = ScoreDialogData(
                        title = if (isPlayerDealer) "Opponent's Hand" else "Your Hand",
                        hand = if (isPlayerDealer) opponentHand else playerHand,
                        breakdown = breakdown
                    )
                }
            }
            CountingPhase.DEALER -> {
                handScores.dealerBreakdown?.let { breakdown ->
                    showDialog = ScoreDialogData(
                        title = if (isPlayerDealer) "Your Hand" else "Opponent's Hand",
                        hand = if (isPlayerDealer) playerHand else opponentHand,
                        breakdown = breakdown
                    )
                }
            }
            CountingPhase.CRIB -> {
                handScores.cribBreakdown?.let { breakdown ->
                    showDialog = ScoreDialogData(
                        title = if (isPlayerDealer) "Your Crib" else "Opponent's Crib",
                        hand = cribHand,
                        breakdown = breakdown
                    )
                }
            }
            else -> {}
        }
    }

    // Only show the dialog - no background display
    showDialog?.let { dialogData ->
        ScoreBreakdownDialog(
            title = dialogData.title,
            hand = dialogData.hand,
            starterCard = starterCard,
            breakdown = dialogData.breakdown,
            onDismiss = {
                showDialog = null
                onDialogDismissed()
            }
        )
    }
}

data class ScoreDialogData(
    val title: String,
    val hand: List<CribbageCard>,
    val breakdown: DetailedScoreBreakdown
)

enum class CountingPhase {
    NONE,
    NON_DEALER,
    DEALER,
    CRIB,
    COMPLETED
}

data class HandScores(
    val nonDealerScore: Int = 0,
    val nonDealerBreakdown: DetailedScoreBreakdown? = null,
    val dealerScore: Int = 0,
    val dealerBreakdown: DetailedScoreBreakdown? = null,
    val cribScore: Int = 0,
    val cribBreakdown: DetailedScoreBreakdown? = null
)

@Composable
fun ScoreBreakdownDialog(
    title: String,
    hand: List<CribbageCard>,
    starterCard: CribbageCard?,
    breakdown: DetailedScoreBreakdown,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header with title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )

                // Cards display
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Hand cards
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Hand",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy((-35).dp),  // Overlap cards
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            items(hand) { card ->
                                GameCard(
                                    card = card,
                                    isRevealed = true,
                                    isClickable = false,
                                    cardSize = CardSize.Medium
                                )
                            }
                        }
                    }

                    // Starter card (cut card) - clearly separated
                    starterCard?.let { starter ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Cut Card",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.tertiary
                            )

                            GameCard(
                                card = starter,
                                isRevealed = true,
                                isClickable = false,
                                cardSize = CardSize.Medium
                            )
                        }
                    }
                }

                // Score breakdown
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Table header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Cards",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1.3f)
                            )
                            Text(
                                text = "Type",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Points",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(0.7f)
                            )
                        }

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        // Score entries (scrollable)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(breakdown.entries) { entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = entry.cards.joinToString(" ") { it.getSymbol() },
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1.3f)
                                    )
                                    Text(
                                        text = entry.type,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = entry.points.toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(0.7f)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        // Total
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Points",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = breakdown.totalScore.toString(),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Accept button
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
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Accept",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
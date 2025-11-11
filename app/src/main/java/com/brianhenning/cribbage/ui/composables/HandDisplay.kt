package com.brianhenning.cribbage.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brianhenning.cribbage.shared.domain.model.Card as CribbageCard

@Composable
fun PlayerHandDisplay(
    hand: List<CribbageCard>,
    selectedCards: Set<Int>,
    playedCards: Set<Int>,
    onCardClick: (Int) -> Unit,
    isEnabled: Boolean = true,
    title: String = "Your Hand",
    modifier: Modifier = Modifier
) {
    HandDisplayBase(
        hand = hand,
        selectedCards = selectedCards,
        playedCards = playedCards,
        onCardClick = onCardClick,
        isEnabled = isEnabled,
        showCards = true,
        title = title,
        cardSize = CardSize.Large,
        modifier = modifier
    )
}

@Composable
fun OpponentHandDisplay(
    hand: List<CribbageCard>,
    playedCards: Set<Int>,
    showCards: Boolean = false,
    title: String = "Opponent's Hand",
    modifier: Modifier = Modifier
) {
    HandDisplayBase(
        hand = hand,
        selectedCards = emptySet(),
        playedCards = playedCards,
        onCardClick = { },
        isEnabled = false,
        showCards = showCards,
        title = title,
        cardSize = CardSize.Small,
        modifier = modifier
    )
}

@Composable
private fun HandDisplayBase(
    hand: List<CribbageCard>,
    selectedCards: Set<Int>,
    playedCards: Set<Int>,
    onCardClick: (Int) -> Unit,
    isEnabled: Boolean,
    showCards: Boolean,
    title: String,
    cardSize: CardSize,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        AnimatedVisibility(
            visible = hand.isNotEmpty(),
            enter = expandVertically(animationSpec = tween(300)),
            exit = shrinkVertically(animationSpec = tween(300))
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy((-20).dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(hand) { index, card ->
                    val selectedOffset by animateDpAsState(
                        targetValue = if (selectedCards.contains(index)) (-12).dp else 0.dp,
                        animationSpec = tween(200),
                        label = "selected_offset"
                    )
                    
                    GameCard(
                        card = card,
                        isSelected = selectedCards.contains(index),
                        isPlayed = playedCards.contains(index),
                        isRevealed = showCards,
                        isClickable = isEnabled && !playedCards.contains(index),
                        onClick = { onCardClick(index) },
                        cardSize = cardSize,
                        modifier = Modifier.offset(y = selectedOffset)
                    )
                }
            }
        }
    }
}

@Composable
fun CribDisplay(
    cribCards: List<CribbageCard>,
    showCards: Boolean = false,
    isPlayerCrib: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = cribCards.isNotEmpty(),
        enter = expandVertically(animationSpec = tween(400)),
        exit = shrinkVertically(animationSpec = tween(400)),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isPlayerCrib) 
                    MaterialTheme.colorScheme.secondaryContainer 
                else 
                    MaterialTheme.colorScheme.tertiaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${if (isPlayerCrib) "Your" else "Opponent's"} Crib",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    itemsIndexed(cribCards) { _, card ->
                        GameCard(
                            card = card,
                            isRevealed = showCards,
                            isClickable = false,
                            cardSize = CardSize.Small
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PeggingPileDisplay(
    peggingCards: List<CribbageCard>,
    peggingCount: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = peggingCards.isNotEmpty(),
        enter = expandVertically(animationSpec = tween(300)),
        exit = shrinkVertically(animationSpec = tween(300)),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Pegging Pile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Count: $peggingCount",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy((-15).dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    itemsIndexed(peggingCards) { index, card ->
                        GameCard(
                            card = card,
                            isRevealed = true,
                            isClickable = false,
                            cardSize = CardSize.Medium,
                            modifier = Modifier.offset(y = (index * 2).dp)
                        )
                    }
                }
            }
        }
    }
}
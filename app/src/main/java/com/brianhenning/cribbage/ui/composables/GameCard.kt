package com.brianhenning.cribbage.ui.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.brianhenning.cribbage.R
import com.brianhenning.cribbage.shared.domain.model.Card as CribbageCard
import com.brianhenning.cribbage.ui.utils.getCardResourceId
import com.brianhenning.cribbage.ui.theme.CardBackground
import com.brianhenning.cribbage.ui.theme.SelectedCard

@Composable
fun GameCard(
    card: CribbageCard,
    isSelected: Boolean = false,
    isPlayed: Boolean = false,
    isRevealed: Boolean = true,
    isClickable: Boolean = true,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    cardSize: CardSize = CardSize.Medium
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = tween(200),
        label = "card_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPlayed) 0.5f else 1.0f,
        animationSpec = tween(300),
        label = "card_alpha"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isSelected) 8f else if (isPlayed) 0f else 2f,
        animationSpec = tween(200),
        label = "card_elevation"
    )

    // Grayscale color matrix for played cards
    val grayscaleMatrix = ColorMatrix().apply {
        setToSaturation(0f)
    }

    val colorFilter = if (isPlayed) {
        ColorFilter.colorMatrix(grayscaleMatrix)
    } else {
        null
    }

    Card(
        modifier = modifier
            .size(cardSize.width, cardSize.height)
            .scale(scale)
            .alpha(alpha)
            .then(
                if (isPlayed) {
                    Modifier.border(2.dp, Color.Red.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            )
            .clickable(enabled = isClickable && !isPlayed) { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SelectedCard else CardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isRevealed) {
                Image(
                    painter = painterResource(id = getCardResourceId(card)),
                    contentDescription = "${card.rank} of ${card.suit}",
                    colorFilter = colorFilter,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.back_dark),
                    contentDescription = "Hidden card",
                    colorFilter = colorFilter,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun FlippableGameCard(
    card: CribbageCard,
    isRevealed: Boolean,
    isSelected: Boolean = false,
    isPlayed: Boolean = false,
    isClickable: Boolean = true,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    cardSize: CardSize = CardSize.Medium
) {
    var flipState by remember { mutableStateOf(isRevealed) }
    
    LaunchedEffect(isRevealed) {
        flipState = isRevealed
    }

    val rotation by animateFloatAsState(
        targetValue = if (flipState) 0f else 180f,
        animationSpec = tween(600),
        label = "card_flip"
    )

    Box(
        modifier = modifier
            .size(cardSize.width, cardSize.height)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12 * density
            }
    ) {
        if (rotation <= 90f) {
            GameCard(
                card = card,
                isSelected = isSelected,
                isPlayed = isPlayed,
                isRevealed = true,
                isClickable = isClickable,
                onClick = onClick,
                cardSize = cardSize,
                modifier = Modifier.graphicsLayer { rotationY = 180f }
            )
        } else {
            GameCard(
                card = card,
                isSelected = isSelected,
                isPlayed = isPlayed,
                isRevealed = false,
                isClickable = isClickable,
                onClick = onClick,
                cardSize = cardSize
            )
        }
    }
}

enum class CardSize(val width: Dp, val height: Dp) {
    Small(60.dp, 90.dp),
    Medium(80.dp, 120.dp),
    Large(100.dp, 150.dp),
    ExtraLarge(120.dp, 180.dp)
}
package com.brianhenning.cribbage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.brianhenning.cribbage.R
import com.brianhenning.cribbage.ui.theme.CardBackground
import com.brianhenning.cribbage.ui.theme.SelectedCard
import kotlin.random.Random

@Composable
fun FirstScreen(navController: NavController) {
    val context = LocalContext.current
    
    // Game state
    var gameStarted by remember { mutableStateOf(false) }
    var playerScore by remember { mutableStateOf(0) }
    var opponentScore by remember { mutableStateOf(0) }
    var isPlayerDealer by remember { mutableStateOf(false) }
    var playerHand by remember { mutableStateOf<List<Card>>(emptyList()) }
    var selectedCards by remember { mutableStateOf<Set<Int>>(emptySet()) }
    
    // Pegging state
    var isPeggingPhase by remember { mutableStateOf(false) }
    var isPlayerTurn by remember { mutableStateOf(false) }
    var peggingCount by remember { mutableStateOf(0) }
    var peggingPile by remember { mutableStateOf<List<Card>>(emptyList()) }
    var playerCardsPlayed by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var opponentCardsPlayed by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var consecutiveGoes by remember { mutableStateOf(0) }
    var starterCard by remember { mutableStateOf<Card?>(null) }
    var opponentHand by remember { mutableStateOf<List<Card>>(emptyList()) }
    
    // UI state
    var gameStatus by remember { mutableStateOf(context.getString(R.string.welcome_to_cribbage)) }
    var dealButtonEnabled by remember { mutableStateOf(false) }
    var selectCribButtonEnabled by remember { mutableStateOf(false) }
    var playCardButtonEnabled by remember { mutableStateOf(false) }
    var showPeggingCount by remember { mutableStateOf(false) }
    
    // Functions
    val startNewGame = {
        gameStarted = true
        playerScore = 0
        opponentScore = 0
        playerHand = emptyList()
        opponentHand = emptyList()
        selectedCards = emptySet()
        
        isPeggingPhase = false
        peggingCount = 0
        peggingPile = emptyList()
        playerCardsPlayed = emptySet()
        opponentCardsPlayed = emptySet()
        consecutiveGoes = 0
        starterCard = null
        
        isPlayerDealer = Random.nextBoolean()
        
        dealButtonEnabled = true
        selectCribButtonEnabled = false
        playCardButtonEnabled = false
        showPeggingCount = false
        
        gameStatus = context.getString(R.string.game_started)
    }
    
    val dealCards = {
        val deck = createDeck().shuffled().toMutableList()
        
        playerHand = List(6) { deck.removeAt(0) }
        opponentHand = List(6) { deck.removeAt(0) }
        
        dealButtonEnabled = false
        selectCribButtonEnabled = true
        gameStatus = context.getString(R.string.select_cards_for_crib)
    }
    
    val selectCardsForCrib = {
        if (selectedCards.size != 2) {
            gameStatus = context.getString(R.string.select_exactly_two)
        } else {
            // Get indices of selected cards
            val selectedIndices = selectedCards.toList().sortedDescending()
            
            // Randomly select two cards from opponent's hand for crib
            val opponentCribCards = opponentHand.shuffled().take(2)
            
            // Remove selected cards from player's hand
            val remainingPlayerCards = playerHand.filterIndexed { index, _ -> !selectedCards.contains(index) }
            playerHand = remainingPlayerCards
            
            // Remove selected cards from opponent's hand
            opponentHand = opponentHand.filter { !opponentCribCards.contains(it) }
            
            selectedCards = emptySet()
            selectCribButtonEnabled = false
            gameStatus = context.getString(R.string.crib_cards_selected)
            
            // Cut a card for the starter
            val newDeck = createDeck().shuffled()
            starterCard = newDeck.first()
            
            // Start pegging phase
            isPeggingPhase = true
            isPlayerTurn = !isPlayerDealer
            playCardButtonEnabled = true
            showPeggingCount = true
            peggingCount = 0
            
            if (isPlayerTurn) {
                gameStatus = context.getString(R.string.pegging_your_turn)
            } else {
                gameStatus = context.getString(R.string.pegging_opponent_turn)
                // Simulate opponent's turn in a real implementation
            }
        }
    }
    
    val toggleCardSelection = { cardIndex: Int ->
        selectedCards = if (isPeggingPhase) {
            // During pegging, player can only select one playable card at a time
            if (isPlayerTurn && !playerCardsPlayed.contains(cardIndex)) {
                // Check if card would exceed 31
                val cardValue = playerHand[cardIndex].getValue()
                if (peggingCount + cardValue <= 31) {
                    setOf(cardIndex)
                } else {
                    gameStatus = context.getString(R.string.pegging_go)
                    emptySet()
                }
            } else {
                selectedCards
            }
        } else {
            // Original behavior for crib selection
            if (selectedCards.contains(cardIndex)) {
                selectedCards - cardIndex
            } else if (selectedCards.size < 2) {
                selectedCards + cardIndex
            } else {
                selectedCards
            }
        }
    }
    
    val playSelectedCard = {
        if (isPeggingPhase && isPlayerTurn && selectedCards.isNotEmpty()) {
            val cardIndex = selectedCards.first()
            
            if (cardIndex < playerHand.size && !playerCardsPlayed.contains(cardIndex)) {
                val playedCard = playerHand[cardIndex]
                
                // Add card to pegging pile
                peggingPile = peggingPile + playedCard
                playerCardsPlayed = playerCardsPlayed + cardIndex
                
                // Update pegging count
                peggingCount += playedCard.getValue()
                
                // Clear selection
                selectedCards = emptySet()
                
                // In a real implementation, check for scoring events here
                
                // Check if count reached 31
                if (peggingCount == 31) {
                    peggingCount = 0
                    consecutiveGoes = 0
                }
                
                // Switch to opponent's turn
                isPlayerTurn = false
                gameStatus = context.getString(R.string.pegging_opponent_turn)
                
                // Simulate opponent's turn
                if (opponentHand.isNotEmpty()) {
                    // Add a short delay to simulate thinking
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        // Find a playable card
                        val playableCards = opponentHand.filter { card -> 
                            card.getValue() + peggingCount <= 31 && !opponentCardsPlayed.contains(opponentHand.indexOf(card))
                        }
                        
                        if (playableCards.isNotEmpty()) {
                            // Choose a card to play
                            val cardToPlay = playableCards.random()
                            val cardIndex = opponentHand.indexOf(cardToPlay)
                            
                            // Play the card
                            peggingPile = peggingPile + cardToPlay
                            opponentCardsPlayed = opponentCardsPlayed + cardIndex
                            peggingCount += cardToPlay.getValue()
                            
                            // Update UI
                            gameStatus = "Opponent played ${cardToPlay.getSymbol()}"
                            
                            // Switch back to player's turn
                            isPlayerTurn = true
                        } else {
                            // Opponent can't play
                            gameStatus = "Opponent says GO!"
                            isPlayerTurn = true
                        }
                    }, 1000)
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game header with scores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Your Score: $playerScore",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Opponent Score: $opponentScore",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        // Dealer info
        Text(
            text = if (isPlayerDealer) "Dealer: You" else "Dealer: Opponent",
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        
        // Game status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = gameStatus,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        // Pegging count (visible during pegging phase)
        if (showPeggingCount) {
            Text(
                text = "Count: $peggingCount",
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Player's hand
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 0 until 6) {
                val card = if (i < playerHand.size) playerHand[i] else null
                val isSelected = selectedCards.contains(i)
                val isPlayed = playerCardsPlayed.contains(i)
                
                Box(
                    modifier = Modifier
                        .size(60.dp, 90.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            color = if (isSelected) SelectedCard else CardBackground,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .alpha(if (isPlayed) 0.3f else 1.0f)
                        .clickable(
                            enabled = gameStarted && card != null && !isPlayed,
                            onClick = { toggleCardSelection(i) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = card?.getSymbol() ?: "🂠",
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Game control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { startNewGame() },
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(text = "Start Game")
            }
            
            Button(
                onClick = { dealCards() },
                enabled = dealButtonEnabled,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(text = "Deal Cards")
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { selectCardsForCrib() },
                enabled = selectCribButtonEnabled,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(text = "Select for Crib")
            }
            
            Button(
                onClick = { playSelectedCard() },
                enabled = playCardButtonEnabled && selectedCards.isNotEmpty(),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(text = "Play Card")
            }
        }
    }
}

// Card representation
enum class Suit { HEARTS, DIAMONDS, CLUBS, SPADES }
enum class Rank { ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING }

data class Card(val rank: Rank, val suit: Suit) {
    fun getValue(): Int {
        return when (rank) {
            Rank.ACE -> 1
            Rank.TWO -> 2
            Rank.THREE -> 3
            Rank.FOUR -> 4
            Rank.FIVE -> 5
            Rank.SIX -> 6
            Rank.SEVEN -> 7
            Rank.EIGHT -> 8
            Rank.NINE -> 9
            Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING -> 10
        }
    }
    
    fun getSymbol(): String {
        // Using card Unicode symbols
        val suitOffset = when (suit) {
            Suit.SPADES -> 0x1F0A0
            Suit.HEARTS -> 0x1F0B0
            Suit.DIAMONDS -> 0x1F0C0
            Suit.CLUBS -> 0x1F0D0
        }
        
        val rankOffset = when (rank) {
            Rank.ACE -> 1
            Rank.TWO -> 2
            Rank.THREE -> 3
            Rank.FOUR -> 4
            Rank.FIVE -> 5
            Rank.SIX -> 6
            Rank.SEVEN -> 7
            Rank.EIGHT -> 8
            Rank.NINE -> 9
            Rank.TEN -> 10
            Rank.JACK -> 11
            Rank.QUEEN -> 13
            Rank.KING -> 14
        }
        
        return String(Character.toChars(suitOffset + rankOffset))
    }
}

// Create a full deck of cards
fun createDeck(): List<Card> {
    val deck = mutableListOf<Card>()
    
    for (suit in Suit.entries) {
        for (rank in Rank.entries) {
            deck.add(Card(rank, suit))
        }
    }
    
    return deck
}
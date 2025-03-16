package com.brianhenning.cribbage.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
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
    
    // Log that the screen is being composed
    LaunchedEffect(Unit) {
        Log.i("CribbageGame", "FirstScreen composable is being rendered")
    }
    
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
        Log.i("CribbageGame", "Starting new game")
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
        Log.i("CribbageGame", "Dealer: ${if (isPlayerDealer) "Player" else "Opponent"}")
        
        dealButtonEnabled = true
        selectCribButtonEnabled = false
        playCardButtonEnabled = false
        showPeggingCount = false
        
        gameStatus = context.getString(R.string.game_started)
    }
    
    val dealCards = {
        Log.i("CribbageGame", "Dealing cards")
        val deck = createDeck().shuffled().toMutableList()
        
        playerHand = List(6) { deck.removeAt(0) }
        opponentHand = List(6) { deck.removeAt(0) }
        
        Log.i("CribbageGame", "Player hand: $playerHand")
        Log.i("CribbageGame", "Opponent hand: $opponentHand")
        
        dealButtonEnabled = false
        selectCribButtonEnabled = true
        gameStatus = context.getString(R.string.select_cards_for_crib)
    }
    
    val selectCardsForCrib = {
        Log.i("CribbageGame", "Select cards for crib called, selected cards: ${selectedCards.size}")
        if (selectedCards.size != 2) {
            gameStatus = context.getString(R.string.select_exactly_two)
            Log.i("CribbageGame", "Not enough cards selected for crib")
        } else {
            // Get indices of selected cards
            val selectedIndices = selectedCards.toList().sortedDescending()
            val selectedPlayerCards = selectedIndices.map { playerHand[it] }
            Log.i("CribbageGame", "Cards selected for crib: $selectedPlayerCards")
            
            // Randomly select two cards from opponent's hand for crib
            val opponentCribCards = opponentHand.shuffled().take(2)
            Log.i("CribbageGame", "Opponent cards for crib: $opponentCribCards")
            
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
            Log.i("CribbageGame", "Starter card: ${starterCard?.getSymbol()}")
            
            // Start pegging phase
            isPeggingPhase = true
            isPlayerTurn = !isPlayerDealer
            playCardButtonEnabled = true
            showPeggingCount = true
            peggingCount = 0
            
            Log.i("CribbageGame", "Starting pegging phase, player turn: $isPlayerTurn")
            if (isPlayerTurn) {
                gameStatus = context.getString(R.string.pegging_your_turn)
            } else {
                gameStatus = context.getString(R.string.pegging_opponent_turn)
                // Simulate opponent's turn in a real implementation
            }
        }
    }
    
    val toggleCardSelection = { cardIndex: Int ->
        Log.i("CribbageGame", "Card selection toggled: $cardIndex")
        selectedCards = if (isPeggingPhase) {
            // During pegging, player can only select one playable card at a time
            if (isPlayerTurn && !playerCardsPlayed.contains(cardIndex)) {
                // Check if card would exceed 31
                val cardValue = playerHand[cardIndex].getValue()
                if (peggingCount + cardValue <= 31) {
                    Log.i("CribbageGame", "Card at index $cardIndex selected for play")
                    setOf(cardIndex)
                } else {
                    Log.i("CribbageGame", "Card at index $cardIndex cannot be played, would exceed 31")
                    gameStatus = context.getString(R.string.pegging_go)
                    emptySet()
                }
            } else {
                selectedCards
            }
        } else {
            // Original behavior for crib selection
            if (selectedCards.contains(cardIndex)) {
                Log.i("CribbageGame", "Card at index $cardIndex deselected for crib")
                selectedCards - cardIndex
            } else if (selectedCards.size < 2) {
                Log.i("CribbageGame", "Card at index $cardIndex selected for crib")
                selectedCards + cardIndex
            } else {
                selectedCards
            }
        }
    }
    
    val playSelectedCard = {
        Log.i("CribbageGame", "Play selected card called")
        if (isPeggingPhase && isPlayerTurn && selectedCards.isNotEmpty()) {
            val cardIndex = selectedCards.first()
            Log.i("CribbageGame", "Playing card at index $cardIndex")
            
            if (cardIndex < playerHand.size && !playerCardsPlayed.contains(cardIndex)) {
                val playedCard = playerHand[cardIndex]
                Log.i("CribbageGame", "Player playing card: ${playedCard.getSymbol()}")
                
                // Add card to pegging pile
                peggingPile = peggingPile + playedCard
                playerCardsPlayed = playerCardsPlayed + cardIndex
                
                // Update pegging count
                peggingCount += playedCard.getValue()
                Log.i("CribbageGame", "New pegging count: $peggingCount")
                
                // Clear selection
                selectedCards = emptySet()
                
                // In a real implementation, check for scoring events here
                
                // Check if count reached 31
                if (peggingCount == 31) {
                    Log.i("CribbageGame", "Count reached 31; resetting to 0")
                    peggingCount = 0
                    consecutiveGoes = 0
                }
                
                // Switch to opponent's turn
                isPlayerTurn = false
                gameStatus = context.getString(R.string.pegging_opponent_turn)
                Log.i("CribbageGame", "Switching to opponent's turn")
                
                // Simulate opponent's turn
                if (opponentHand.isNotEmpty()) {
                    // Add a short delay to simulate thinking
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        // Find a playable card
                        val playableCards = opponentHand.filter { card -> 
                            card.getValue() + peggingCount <= 31 && !opponentCardsPlayed.contains(opponentHand.indexOf(card))
                        }
                        Log.i("CribbageGame", "Opponent has ${playableCards.size} playable cards")
                        
                        if (playableCards.isNotEmpty()) {
                            // Choose a card to play
                            val cardToPlay = playableCards.random()
                            val cardIndex = opponentHand.indexOf(cardToPlay)
                            Log.i("CribbageGame", "Opponent playing card: ${cardToPlay.getSymbol()}")
                            
                            // Play the card
                            peggingPile = peggingPile + cardToPlay
                            opponentCardsPlayed = opponentCardsPlayed + cardIndex
                            peggingCount += cardToPlay.getValue()
                            Log.i("CribbageGame", "New pegging count after opponent play: $peggingCount")
                            
                            // Update UI
                            gameStatus = "Opponent played ${cardToPlay.getSymbol()}"
                            
                            // Switch back to player's turn
                            isPlayerTurn = true
                            Log.i("CribbageGame", "Switching back to player's turn")
                        } else {
                            // Opponent can't play
                            Log.i("CribbageGame", "Opponent cannot play; says GO")
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
                    if (card != null) {
                        Image(
                            painter = painterResource(id = getCardResourceId(card)),
                            contentDescription = card.toString(),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.back_light),
                            contentDescription = "Card back",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
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
        val rankSymbol = when (rank) {
            Rank.ACE -> "A"
            Rank.TWO -> "2"
            Rank.THREE -> "3"
            Rank.FOUR -> "4"
            Rank.FIVE -> "5"
            Rank.SIX -> "6"
            Rank.SEVEN -> "7"
            Rank.EIGHT -> "8"
            Rank.NINE -> "9"
            Rank.TEN -> "10"
            Rank.JACK -> "J"
            Rank.QUEEN -> "Q"
            Rank.KING -> "K"
        }
        
        val suitSymbol = when (suit) {
            Suit.SPADES -> "♠"
            Suit.HEARTS -> "♥"
            Suit.DIAMONDS -> "♦"
            Suit.CLUBS -> "♣"
        }
        
        return "$rankSymbol$suitSymbol"
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

// Get the resource ID for a card
fun getCardResourceId(card: Card): Int {
    val suitName = when (card.suit) {
        Suit.HEARTS -> "hearts"
        Suit.DIAMONDS -> "diamonds"
        Suit.CLUBS -> "clubs"
        Suit.SPADES -> "spades"
    }
    
    val rankName = when (card.rank) {
        Rank.ACE -> "a"
        Rank.TWO -> "2"
        Rank.THREE -> "3"
        Rank.FOUR -> "4"
        Rank.FIVE -> "5"
        Rank.SIX -> "6"
        Rank.SEVEN -> "7"
        Rank.EIGHT -> "8"
        Rank.NINE -> "9"
        Rank.TEN -> "10"
        Rank.JACK -> "j"
        Rank.QUEEN -> "q"
        Rank.KING -> "k"
    }
    
    // Convert string resource name to actual resource ID
    val resourceName = "${suitName}_${rankName}"
    val resourceField = R.drawable::class.java.getField(resourceName)
    return resourceField.getInt(null)
}
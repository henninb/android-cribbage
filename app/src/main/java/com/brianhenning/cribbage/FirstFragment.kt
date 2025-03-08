package com.brianhenning.cribbage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.brianhenning.cribbage.databinding.FirstFragmentBinding
import com.brianhenning.cribbage.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class FirstFragment : Fragment() {

    private var _binding: FirstFragmentBinding? = null
    private val binding get() = _binding!!

    private var gameStarted = false
    private var playerScore = 0
    private var opponentScore = 0
    private var isPlayerDealer = false
    private val playerHand = mutableListOf<Card>()
    private val opponentHand = mutableListOf<Card>()
    private val selectedCards = mutableSetOf<Int>()
    private val cardViews = mutableListOf<TextView>()

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
            // Using card Unicode symbols (not perfect for all devices but works for demo)
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FirstFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Store card views for easy access
        cardViews.add(binding.textViewCard1)
        cardViews.add(binding.textViewCard2)
        cardViews.add(binding.textViewCard3)
        cardViews.add(binding.textViewCard4)
        cardViews.add(binding.textViewCard5)
        cardViews.add(binding.textViewCard6)
        
        // Set up click listeners for cards
        cardViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                if (gameStarted && playerHand.size > index) {
                    toggleCardSelection(index)
                }
            }
        }
        
        // Set up button listeners
        binding.buttonStartGame.setOnClickListener {
            startNewGame()
        }
        
        binding.buttonDealCards.setOnClickListener {
            dealCards()
        }
        
        binding.buttonSelectCrib.setOnClickListener {
            selectCardsForCrib()
        }
    }
    
    private fun startNewGame() {
        // Reset game state
        gameStarted = true
        playerScore = 0
        opponentScore = 0
        playerHand.clear()
        opponentHand.clear()
        selectedCards.clear()
        
        // Determine first dealer randomly
        isPlayerDealer = Random.nextBoolean()
        
        // Update UI
        binding.textViewPlayerScore.text = getString(R.string.your_score_0)
        binding.textViewOpponentScore.text = getString(R.string.opponent_score_0)
        
        if (isPlayerDealer) {
            binding.textViewDealer.text = getString(R.string.dealer_you)
        } else {
            binding.textViewDealer.text = getString(R.string.dealer_opponent)
        }
        
        // Enable deal button, disable others
        binding.buttonDealCards.isEnabled = true
        binding.buttonSelectCrib.isEnabled = false
        
        // Set all cards to back face
        cardViews.forEach { it.text = "🂠" }
        
        // Update status
        binding.textViewGameStatus.text = getString(R.string.game_started)
        
        Log.i("FirstFragment", "New cribbage game started")
    }
    
    private fun dealCards() {
        // Create and shuffle deck
        val deck = createDeck()
        
        // Deal 6 cards to each player
        playerHand.clear()
        opponentHand.clear()
        
        repeat(6) {
            playerHand.add(deck.removeAt(0))
            opponentHand.add(deck.removeAt(0))
        }
        
        // Display player's cards
        displayPlayerHand()
        
        // Update UI and game state
        binding.buttonDealCards.isEnabled = false
        binding.buttonSelectCrib.isEnabled = true
        binding.textViewGameStatus.text = getString(R.string.select_cards_for_crib)
        
        Log.i("FirstFragment", "Cards dealt. Player hand: $playerHand")
    }
    
    private fun createDeck(): MutableList<Card> {
        val deck = mutableListOf<Card>()
        
        for (suit in Suit.values()) {
            for (rank in Rank.values()) {
                deck.add(Card(rank, suit))
            }
        }
        
        // Shuffle the deck
        deck.shuffle()
        
        return deck
    }
    
    private fun displayPlayerHand() {
        playerHand.forEachIndexed { index, card ->
            if (index < cardViews.size) {
                cardViews[index].text = card.getSymbol()
            }
        }
        
        // Reset card selections
        selectedCards.clear()
        updateCardSelections()
    }
    
    private fun toggleCardSelection(cardIndex: Int) {
        if (selectedCards.contains(cardIndex)) {
            selectedCards.remove(cardIndex)
        } else {
            // Only allow selecting 2 cards for crib
            if (selectedCards.size < 2) {
                selectedCards.add(cardIndex)
            }
        }
        
        updateCardSelections()
    }
    
    private fun updateCardSelections() {
        // Apply visual indication for selected cards
        cardViews.forEachIndexed { index, textView ->
            if (selectedCards.contains(index)) {
                textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selected_card))
            } else {
                textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_background))
            }
        }
    }
    
    private fun selectCardsForCrib() {
        if (selectedCards.size != 2) {
            binding.textViewGameStatus.text = getString(R.string.select_exactly_two)
            return
        }
        
        // Get indices of selected cards (in descending order to avoid index issues when removing)
        val selectedIndices = selectedCards.toList().sortedDescending()
        
        // Log the cards being discarded to crib
        val cribCards = selectedIndices.map { playerHand[it] }
        Log.i("FirstFragment", "Cards selected for crib: $cribCards")
        
        // Randomly select two cards from opponent's hand for crib
        val opponentCribCards = opponentHand.shuffled().take(2)
        Log.i("FirstFragment", "Opponent's cards for crib: $opponentCribCards")
        
        // Remove the selected cards from player's hand
        selectedIndices.forEach { playerHand.removeAt(it) }
        
        // Remove the selected cards from opponent's hand
        opponentHand.removeAll(opponentCribCards)
        
        // Update the UI to show remaining cards
        displayRemainingCards()
        
        // Disable crib selection button and update game status
        binding.buttonSelectCrib.isEnabled = false
        binding.textViewGameStatus.text = getString(R.string.crib_cards_selected)
    }
    
    private fun displayRemainingCards() {
        // Reset all cards to back face first
        cardViews.forEach { it.text = "🂠" }
        
        // Display remaining 4 cards
        playerHand.forEachIndexed { index, card ->
            if (index < cardViews.size) {
                cardViews[index].text = card.getSymbol()
            }
        }
        
        // Clear selections
        selectedCards.clear()
        updateCardSelections()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
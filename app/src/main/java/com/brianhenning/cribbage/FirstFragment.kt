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

    // Pegging variables
    private var isPeggingPhase = false
    private var isPlayerTurn = false
    private var peggingCount = 0
    private val peggingPile = mutableListOf<Card>()
    private val playerCardsPlayed = mutableSetOf<Int>()
    private val opponentCardsPlayed = mutableSetOf<Int>()
    private var consecutiveGoes = 0
    private var starterCard: Card? = null
    // Track the last player who played a card ("player" or "opponent")
    private var lastPlayerWhoPlayed: String? = null

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

        binding.buttonPlayCard.setOnClickListener {
            playSelectedCard()
        }

        binding.buttonSayGo.setOnClickListener {
            sayGo()
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

        // Reset pegging variables
        isPeggingPhase = false
        peggingCount = 0
        peggingPile.clear()
        playerCardsPlayed.clear()
        opponentCardsPlayed.clear()
        consecutiveGoes = 0
        starterCard = null
        lastPlayerWhoPlayed = null

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
        binding.buttonPlayCard.isEnabled = false
        binding.buttonSayGo.isEnabled = false
        binding.buttonSayGo.visibility = View.GONE

        // Reset the pegging count and cut card displays
        binding.textViewPeggingCount.visibility = View.GONE
        binding.textViewCutCard.visibility = View.GONE

        // Set all cards to back face and reset opacity
        cardViews.forEach {
            it.text = "🂠"
            it.alpha = 1.0f
        }

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

        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
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

    // New helper function to update the player's pegging UI
    private fun updatePlayerPeggingUI() {
        if (isPlayerTurn) {
            if (checkPlayerCanPlayAnyCard()) {
                binding.buttonPlayCard.isEnabled = selectedCards.isNotEmpty()
                binding.buttonSayGo.visibility = View.GONE
            } else {
                binding.buttonPlayCard.isEnabled = false
                binding.buttonSayGo.visibility = View.VISIBLE
                binding.buttonSayGo.isEnabled = true
                binding.textViewGameStatus.text = getString(R.string.pegging_go)
            }
        } else {
            binding.buttonPlayCard.isEnabled = false
            binding.buttonSayGo.visibility = View.GONE
        }
    }

    private fun toggleCardSelection(cardIndex: Int) {
        // Handle differently based on game phase
        if (isPeggingPhase) {
            // During pegging, player can only select one playable card at a time
            if (isPlayerTurn && !playerCardsPlayed.contains(cardIndex)) {
                // Clear previous selections
                selectedCards.clear()

                // Check if the card would exceed 31
                val cardValue = playerHand[cardIndex].getValue()
                if (peggingCount + cardValue <= 31) {
                    selectedCards.add(cardIndex)
                } else {
                    binding.textViewGameStatus.text = getString(R.string.pegging_go)
                }
                updatePlayerPeggingUI()
            }
        } else {
            // Original behavior for crib selection
            if (selectedCards.contains(cardIndex)) {
                selectedCards.remove(cardIndex)
            } else {
                // Only allow selecting 2 cards for crib
                if (selectedCards.size < 2) {
                    selectedCards.add(cardIndex)
                }
            }
        }

        updateCardSelections()
    }

    private fun sayGo() {
        if (!isPeggingPhase || !isPlayerTurn) {
            return
        }

        // Player says "GO"
        binding.textViewGameStatus.text = "You say GO!"
        consecutiveGoes++

        // Check if both players have said GO or all cards are played
        if (consecutiveGoes == 2 || allCardsPlayed()) {
            // Last card point for the last player who played a card
            if (peggingPile.isNotEmpty()) {
                if (lastPlayerWhoPlayed == "player") {
                    playerScore += 1
                    binding.textViewGameStatus.text = getString(R.string.pegging_last_card) + " (You)"
                } else {
                    opponentScore += 1
                    binding.textViewGameStatus.text = getString(R.string.pegging_last_card) + " (Opponent)"
                }
                updateScores()
            }

            // Reset for next segment
            peggingCount = 0
            binding.textViewPeggingCount.text = getString(R.string.pegging_count_0)
            consecutiveGoes = 0
        }

        // Check if pegging is complete
        if (checkIfPeggingIsComplete()) {
            finishPeggingPhase()
            return
        }

        // Switch to opponent's turn
        isPlayerTurn = false
        binding.buttonPlayCard.isEnabled = false
        binding.buttonSayGo.visibility = View.GONE
        binding.textViewGameStatus.text = getString(R.string.pegging_opponent_turn)

        // Simulate opponent's turn after a short delay
        playOpponentCard()
    }

    private fun checkPlayerCanPlayAnyCard(): Boolean {
        return playerHand.filterIndexed { index, card ->
            !playerCardsPlayed.contains(index) && card.getValue() + peggingCount <= 31
        }.isNotEmpty()
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

        // Cut a card for the starter and show it
        val deck = createDeck()
        starterCard = deck.first()

        // Show the cut card visually before starting pegging
        binding.textViewCutCard.text = starterCard?.getSymbol()
        binding.textViewCutCard.visibility = View.VISIBLE
        binding.textViewGameStatus.text = "Cut card shown above. Ready for pegging."

        // Start the pegging phase
        startPeggingPhase()
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

    private fun startPeggingPhase() {
        // Initialize pegging variables
        isPeggingPhase = true
        peggingCount = 0
        peggingPile.clear()
        playerCardsPlayed.clear()
        opponentCardsPlayed.clear()
        consecutiveGoes = 0
        lastPlayerWhoPlayed = null

        // When it's my deal (i.e. I'm the dealer), the opponent leads off.
        if (isPlayerDealer) {
            isPlayerTurn = false
            binding.textViewGameStatus.text = getString(R.string.pegging_opponent_turn)
            binding.buttonPlayCard.isEnabled = false
            binding.buttonSayGo.visibility = View.GONE
            // Let the opponent play after a short delay (forcing the opponent turn)
            view?.postDelayed({ playOpponentCard(force = true) }, 1000)
        } else {
            // If I'm not the dealer, then I lead.
            isPlayerTurn = true
            binding.textViewGameStatus.text = getString(R.string.pegging_your_turn)
            updatePlayerPeggingUI()
        }

        binding.textViewPeggingCount.visibility = View.VISIBLE
        binding.textViewPeggingCount.text = getString(R.string.pegging_count_0)
    }

    private fun playSelectedCard() {
        if (!isPeggingPhase || !isPlayerTurn || selectedCards.isEmpty()) {
            return
        }

        val cardIndex = selectedCards.first()

        // Check if valid card index and not already played
        if (cardIndex >= playerHand.size || playerCardsPlayed.contains(cardIndex)) {
            return
        }

        val playedCard = playerHand[cardIndex]
        // Track that the player played this card
        lastPlayerWhoPlayed = "player"

        // Add card to pegging pile
        peggingPile.add(playedCard)
        playerCardsPlayed.add(cardIndex)

        // Update pegging count
        peggingCount += playedCard.getValue()

        // Check for scoring events
        val points = checkPeggingScore(playedCard)
        if (points > 0) {
            playerScore += points
            updateScores()
            Log.i("FirstFragment", "Player scored $points points. New score: $playerScore")
        }

        // Update UI
        binding.textViewPeggingCount.text = "Count: $peggingCount"

        // Mark card as played in UI
        cardViews[cardIndex].alpha = 0.3f
        selectedCards.clear()
        updateCardSelections()

        // Reset consecutiveGoes since a card was played
        consecutiveGoes = 0

        // Check if pegging round is complete
        if (checkIfPeggingIsComplete()) {
            finishPeggingPhase()
            return
        }

        // If count reached 31, reset for next segment
        if (peggingCount == 31) {
            peggingCount = 0
            binding.textViewPeggingCount.text = getString(R.string.pegging_count_0)
            consecutiveGoes = 0
        }

        // Switch to opponent's turn
        isPlayerTurn = false
        binding.buttonPlayCard.isEnabled = false
        binding.buttonSayGo.visibility = View.GONE
        binding.textViewGameStatus.text = getString(R.string.pegging_opponent_turn)

        view?.postDelayed({ playOpponentCard() }, 1000)
    }

    // Added an optional parameter "force" to ensure opponent's turn starts when required.
    private fun playOpponentCard(force: Boolean = false) {
        if (!force && isPlayerTurn) {
            return  // Only proceed if it's not the player's turn or if forced.
        }

        view?.postDelayed({
            // Find a valid card for opponent to play
            val playableCards = opponentHand.filterIndexed { index, card ->
                !opponentCardsPlayed.contains(index) && card.getValue() + peggingCount <= 31
            }

            if (playableCards.isEmpty()) {
                binding.textViewGameStatus.text = "Opponent says GO!"
                consecutiveGoes++

                if (consecutiveGoes == 2 || allCardsPlayed()) {
                    if (peggingPile.isNotEmpty()) {
                        if (lastPlayerWhoPlayed == "player") {
                            playerScore += 1
                            binding.textViewGameStatus.text = getString(R.string.pegging_last_card) + " (You)"
                        } else {
                            opponentScore += 1
                            binding.textViewGameStatus.text = getString(R.string.pegging_last_card) + " (Opponent)"
                        }
                        updateScores()
                    }

                    peggingCount = 0
                    binding.textViewPeggingCount.text = getString(R.string.pegging_count_0)
                    consecutiveGoes = 0
                }

                if (checkIfPeggingIsComplete()) {
                    finishPeggingPhase()
                    return@postDelayed
                }

                isPlayerTurn = true
                binding.textViewGameStatus.text = getString(R.string.pegging_your_turn)
                updatePlayerPeggingUI()

                val playerCanPlay = checkPlayerCanPlayAnyCard()

                if (!playerCanPlay) {
                    consecutiveGoes++

                    if (consecutiveGoes == 2 || allCardsPlayed()) {
                        if (peggingPile.isNotEmpty()) {
                            if (lastPlayerWhoPlayed == "player") {
                                playerScore += 1
                                binding.textViewGameStatus.text = getString(R.string.pegging_last_card) + " (You)"
                            } else {
                                opponentScore += 1
                                binding.textViewGameStatus.text = getString(R.string.pegging_last_card) + " (Opponent)"
                            }
                            updateScores()
                        }

                        peggingCount = 0
                        binding.textViewPeggingCount.text = getString(R.string.pegging_count_0)
                        consecutiveGoes = 0
                    }

                    if (checkIfPeggingIsComplete()) {
                        finishPeggingPhase()
                    } else {
                        isPlayerTurn = false
                        playOpponentCard()
                    }
                }

                return@postDelayed
            }

            val cardToPlay = playableCards.random()
            val cardIndex = opponentHand.indexOf(cardToPlay)

            lastPlayerWhoPlayed = "opponent"

            peggingPile.add(cardToPlay)
            opponentCardsPlayed.add(cardIndex)

            peggingCount += cardToPlay.getValue()

            val points = checkPeggingScore(cardToPlay)
            if (points > 0) {
                opponentScore += points
                updateScores()
                Log.i("FirstFragment", "Opponent scored $points points. New score: $opponentScore")
            }

            binding.textViewPeggingCount.text = "Count: $peggingCount"
            binding.textViewGameStatus.text = "Opponent played a card. Count: $peggingCount"

            consecutiveGoes = 0

            if (checkIfPeggingIsComplete()) {
                finishPeggingPhase()
                return@postDelayed
            }

            if (peggingCount == 31) {
                peggingCount = 0
                binding.textViewPeggingCount.text = getString(R.string.pegging_count_0)
                consecutiveGoes = 0
            }

            isPlayerTurn = true
            binding.textViewGameStatus.text = getString(R.string.pegging_your_turn)
            updatePlayerPeggingUI()

            val playerCanPlay = checkPlayerCanPlayAnyCard()

            if (playerCanPlay) {
                binding.buttonPlayCard.isEnabled = selectedCards.isNotEmpty()
                binding.buttonSayGo.visibility = View.GONE
            } else if (!allCardsPlayed()) {
                binding.buttonPlayCard.isEnabled = false
                binding.buttonSayGo.visibility = View.VISIBLE
                binding.buttonSayGo.isEnabled = true
                binding.textViewGameStatus.text = getString(R.string.pegging_go)
            }

        }, 1000)
    }

    private fun checkPeggingScore(playedCard: Card): Int {
        var score = 0
        val statusMessages = mutableListOf<String>()

        if (peggingCount == 15) {
            score += 2
            statusMessages.add(getString(R.string.pegging_fifteen))
            Log.i("FirstFragment", "Scored 2 points for fifteen. Count: $peggingCount")
        }

        if (peggingCount == 31) {
            score += 2
            statusMessages.add(getString(R.string.pegging_thirtyone))
            Log.i("FirstFragment", "Scored 2 points for thirty-one. Count: $peggingCount")
        }

        val consecutiveSameRankCards = peggingPile.reversed().takeWhile { it.rank == playedCard.rank }
        when (consecutiveSameRankCards.size) {
            2 -> {
                score += 2
                statusMessages.add(getString(R.string.pegging_pairs))
            }
            3 -> {
                score += 6
                statusMessages.add(getString(R.string.pegging_three_of_a_kind))
            }
            4 -> {
                score += 12
                statusMessages.add(getString(R.string.pegging_four_of_a_kind))
            }
        }

        for (runLength in 7 downTo 3) {
            if (peggingPile.size >= runLength) {
                val lastCards = peggingPile.takeLast(runLength)
                val ranks = lastCards.map { it.rank.ordinal }.sorted()
                var isRun = true
                for (i in 0 until ranks.size - 1) {
                    if (ranks[i + 1] - ranks[i] != 1) {
                        isRun = false
                        break
                    }
                }

                if (isRun) {
                    score += runLength
                    statusMessages.add(getString(R.string.pegging_run, runLength, runLength))
                    break
                }
            }
        }

        if (statusMessages.isNotEmpty()) {
            binding.textViewGameStatus.text = statusMessages.joinToString("\n")
        }

        return score
    }

    private fun updateScores() {
        binding.textViewPlayerScore.text = "Your Score: $playerScore"
        binding.textViewOpponentScore.text = "Opponent Score: $opponentScore"
    }

    private fun checkIfPeggingIsComplete(): Boolean {
        return playerCardsPlayed.size == playerHand.size && opponentCardsPlayed.size == opponentHand.size
    }

    private fun allCardsPlayed(): Boolean {
        return playerCardsPlayed.size == playerHand.size && opponentCardsPlayed.size == opponentHand.size
    }

    private fun finishPeggingPhase() {
        isPeggingPhase = false
        binding.textViewGameStatus.text = getString(R.string.pegging_complete)
        binding.buttonPlayCard.isEnabled = false
        binding.textViewPeggingCount.visibility = View.GONE

        Log.i("FirstFragment", "Pegging complete. Starter card: ${starterCard?.getSymbol()}")
        binding.textViewCutCard.text = starterCard?.getSymbol()
        binding.textViewCutCard.visibility = View.VISIBLE
        binding.textViewGameStatus.text = "Pegging complete. See cut card above."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

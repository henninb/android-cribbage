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
    // isPlayerTurn true means it is the human player's turn; false for opponent
    private var isPlayerTurn = false
    private var peggingCount = 0
    // All cards played in the current sub-round (for run/pair scoring)
    private val peggingPile = mutableListOf<Card>()
    // Track indices of cards already played (in the remaining hand)
    private val playerCardsPlayed = mutableSetOf<Int>()
    private val opponentCardsPlayed = mutableSetOf<Int>()
    // The last player to successfully play a card ("player" or "opponent")
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
            // Using card Unicode symbols (works for demo)
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
        Log.i("FirstFragment", "onCreateView called")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.i("FirstFragment", "onViewCreated called")
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
                Log.i("FirstFragment", "Card view $index clicked")
                if (gameStarted && playerHand.size > index) {
                    toggleCardSelection(index)
                }
            }
        }

        // Set up button listeners
        binding.buttonStartGame.setOnClickListener {
            Log.i("FirstFragment", "Start Game button clicked")
            startNewGame()
        }
        binding.buttonDealCards.setOnClickListener {
            Log.i("FirstFragment", "Deal Cards button clicked")
            dealCards()
        }
        binding.buttonSelectCrib.setOnClickListener {
            Log.i("FirstFragment", "Select Crib button clicked")
            selectCardsForCrib()
        }
        binding.buttonPlayCard.setOnClickListener {
            Log.i("FirstFragment", "Play Card button clicked")
            playSelectedCard()
        }
        binding.buttonSayGo.setOnClickListener {
            Log.i("FirstFragment", "Go button clicked")
            sayGo()
        }
    }

    private fun startNewGame() {
        Log.i("FirstFragment", "Starting new game")
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
        lastPlayerWhoPlayed = null

        // Determine first dealer randomly
        isPlayerDealer = Random.nextBoolean()
        Log.i("FirstFragment", "Dealer: ${if (isPlayerDealer) "Player" else "Opponent"}")

        // Update UI
        binding.textViewPlayerScore.text = getString(R.string.your_score_0)
        binding.textViewOpponentScore.text = getString(R.string.opponent_score_0)
        binding.textViewDealer.text = if (isPlayerDealer) getString(R.string.dealer_you) else getString(R.string.dealer_opponent)

        // Enable deal button, disable others
        binding.buttonDealCards.isEnabled = true
        binding.buttonSelectCrib.isEnabled = false
        binding.buttonPlayCard.isEnabled = false
        binding.buttonSayGo.isEnabled = false
        binding.buttonSayGo.visibility = View.GONE

        // Reset pegging and cut card displays
        binding.textViewPeggingCount.visibility = View.GONE
        binding.textViewCutCard.visibility = View.GONE

        // Set all card views to back face and full opacity
        cardViews.forEach {
            it.text = "🂠"
            it.alpha = 1.0f
        }

        binding.textViewGameStatus.text = getString(R.string.game_started)
        Log.i("FirstFragment", "New game started")
    }

    private fun dealCards() {
        Log.i("FirstFragment", "Dealing cards")
        // Create and shuffle deck
        val deck = createDeck()

        // Deal 6 cards to each player (later, crib selection reduces to 4)
        playerHand.clear()
        opponentHand.clear()

        repeat(6) {
            playerHand.add(deck.removeAt(0))
            opponentHand.add(deck.removeAt(0))
        }
        Log.i("FirstFragment", "Player hand: $playerHand")
        Log.i("FirstFragment", "Opponent hand: $opponentHand")

        displayPlayerHand()

        binding.buttonDealCards.isEnabled = false
        binding.buttonSelectCrib.isEnabled = true
        binding.textViewGameStatus.text = getString(R.string.select_cards_for_crib)
        Log.i("FirstFragment", "Cards dealt")
    }

    private fun createDeck(): MutableList<Card> {
        Log.i("FirstFragment", "Creating deck")
        val deck = mutableListOf<Card>()
        for (suit in Suit.entries) {
            for (rank in Rank.entries) {
                deck.add(Card(rank, suit))
            }
        }
        deck.shuffle()
        Log.i("FirstFragment", "Deck created and shuffled")
        return deck
    }

    private fun displayPlayerHand() {
        Log.i("FirstFragment", "Displaying player hand")
        playerHand.forEachIndexed { index, card ->
            if (index < cardViews.size) {
                cardViews[index].text = card.getSymbol()
            }
        }
        selectedCards.clear()
        updateCardSelections()
    }

    // Toggle selection in crib phase or for pegging if a card is playable
    private fun toggleCardSelection(cardIndex: Int) {
        Log.i("FirstFragment", "toggleCardSelection called for index: $cardIndex")
        if (isPeggingPhase) {
            // In pegging, allow selection only if it's your turn and the card won’t push total over 31.
            if (isPlayerTurn && !playerCardsPlayed.contains(cardIndex)) {
                selectedCards.clear()
                val cardValue = playerHand[cardIndex].getValue()
                if (peggingCount + cardValue <= 31) {
                    selectedCards.add(cardIndex)
                    Log.i("FirstFragment", "Card at index $cardIndex selected for play")
                } else {
                    binding.textViewGameStatus.text = getString(R.string.pegging_go)
                    Log.i("FirstFragment", "Card at index $cardIndex cannot be played, would exceed 31")
                }
                updatePlayerPeggingUI()
            }
        } else {
            // Crib selection: allow exactly 2 cards.
            if (selectedCards.contains(cardIndex)) {
                selectedCards.remove(cardIndex)
                Log.i("FirstFragment", "Card at index $cardIndex deselected for crib")
            } else if (selectedCards.size < 2) {
                selectedCards.add(cardIndex)
                Log.i("FirstFragment", "Card at index $cardIndex selected for crib")
            }
        }
        updateCardSelections()
    }

    // Update card view backgrounds based on selection
    private fun updateCardSelections() {
        cardViews.forEachIndexed { index, textView ->
            if (selectedCards.contains(index)) {
                textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selected_card))
            } else {
                textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_background))
            }
        }
    }

    private fun selectCardsForCrib() {
        Log.i("FirstFragment", "selectCardsForCrib called")
        if (selectedCards.size != 2) {
            binding.textViewGameStatus.text = getString(R.string.select_exactly_two)
            Log.i("FirstFragment", "Crib selection failed, exactly 2 cards not selected")
            return
        }
        // Remove selected cards (and pick 2 random opponent cards for the crib)
        val selectedIndices = selectedCards.toList().sortedDescending()
        val cribCards = selectedIndices.map { playerHand[it] }
        Log.i("FirstFragment", "Cards selected for crib: $cribCards")
        val opponentCribCards = opponentHand.shuffled().take(2)
        Log.i("FirstFragment", "Opponent's cards for crib: $opponentCribCards")

        selectedIndices.forEach { playerHand.removeAt(it) }
        opponentHand.removeAll(opponentCribCards)
        displayRemainingCards()

        binding.buttonSelectCrib.isEnabled = false

        // Cut a card for the starter and display it.
        val deck = createDeck()
        val starterCard = deck.first()
        binding.textViewCutCard.text = starterCard.getSymbol()
        binding.textViewCutCard.visibility = View.VISIBLE
        binding.textViewGameStatus.text = "Cut card shown. Ready for pegging."
        Log.i("FirstFragment", "Starter card: ${starterCard.getSymbol()}")

        // Start pegging phase.
        startPeggingPhase()
    }

    private fun displayRemainingCards() {
        Log.i("FirstFragment", "Displaying remaining cards after crib selection")
        cardViews.forEach { it.text = "🂠" }
        playerHand.forEachIndexed { index, card ->
            if (index < cardViews.size) {
                cardViews[index].text = card.getSymbol()
            }
        }
        selectedCards.clear()
        updateCardSelections()
    }

    // ==================== PEGGING PHASE FUNCTIONS ====================

    private fun startPeggingPhase() {
        Log.i("FirstFragment", "Starting pegging phase")
        isPeggingPhase = true
        peggingCount = 0
        peggingPile.clear()
        playerCardsPlayed.clear()
        opponentCardsPlayed.clear()
        lastPlayerWhoPlayed = null

        binding.textViewPeggingCount.visibility = View.VISIBLE
        binding.textViewPeggingCount.text = "Count: 0"

        // Who leads depends on dealer. If you are the dealer, opponent leads; otherwise you do.
        if (isPlayerDealer) {
            isPlayerTurn = false
            binding.textViewGameStatus.text = getString(R.string.pegging_opponent_turn)
            binding.buttonPlayCard.isEnabled = false
            binding.buttonSayGo.visibility = View.GONE
            Log.i("FirstFragment", "Player is dealer: Opponent will lead")
            // Delay opponent play slightly.
            view?.postDelayed({ playOpponentCard() }, 1000)
        } else {
            isPlayerTurn = true
            binding.textViewGameStatus.text = getString(R.string.pegging_your_turn)
            Log.i("FirstFragment", "Player leads pegging")
            updatePlayerPeggingUI()
        }
    }

    // Called when the human player presses the Play Card button.
    private fun playSelectedCard() {
        Log.i("FirstFragment", "playSelectedCard called")
        if (!isPeggingPhase || !isPlayerTurn || selectedCards.isEmpty()) {
            Log.i("FirstFragment", "Cannot play card: Not player's turn or no selection")
            return
        }
        val cardIndex = selectedCards.first()
        if (cardIndex >= playerHand.size || playerCardsPlayed.contains(cardIndex)) {
            Log.i("FirstFragment", "Invalid card index $cardIndex or card already played")
            return
        }

        // Process the play.
        processPlay(isPlayer = true, cardIndex = cardIndex)
        selectedCards.clear()
        updateCardSelections()
    }

    // Called when the human player presses the Go button.
    private fun sayGo() {
        Log.i("FirstFragment", "sayGo called by player")
        if (!isPeggingPhase || !isPlayerTurn) return
        binding.textViewGameStatus.text = "You say GO!"
        Log.i("FirstFragment", "Player says GO!")
        // Check if opponent can play.
        if (opponentCanPlay()) {
            isPlayerTurn = false
            Log.i("FirstFragment", "Opponent can play after GO; switching turn")
            // Give the opponent a chance to play.
            view?.postDelayed({ playOpponentCard() }, 1000)
        } else {
            Log.i("FirstFragment", "Neither player can play after GO; awarding point and ending sub-round")
            // Neither can play—award last card point and end the sub-round.
            awardGoPoint()
            endSubRound()
        }
    }

    // Process a card play (by human or opponent)
    private fun processPlay(isPlayer: Boolean, cardIndex: Int) {
        val playedCard = if (isPlayer) playerHand[cardIndex] else opponentHand[cardIndex]
        Log.i("FirstFragment", "${if (isPlayer) "Player" else "Opponent"} playing card ${playedCard.getSymbol()} at index $cardIndex")
        // Add card to pegging pile.
        peggingPile.add(playedCard)
        if (isPlayer) {
            playerCardsPlayed.add(cardIndex)
        } else {
            opponentCardsPlayed.add(cardIndex)
        }
        peggingCount += playedCard.getValue()
        lastPlayerWhoPlayed = if (isPlayer) "player" else "opponent"
        Log.i("FirstFragment", "New pegging count: $peggingCount; Pegging pile: $peggingPile")

        // Check for scoring events (15, 31, pairs, runs)
        val points = checkPeggingScore(playedCard)
        if (points > 0) {
            if (isPlayer) {
                playerScore += points
                Log.i("FirstFragment", "Player scored $points points for ${playedCard.getSymbol()}")
            } else {
                opponentScore += points
                Log.i("FirstFragment", "Opponent scored $points points for ${playedCard.getSymbol()}")
            }
            updateScores()
        }
        binding.textViewPeggingCount.text = "Count: $peggingCount"

        // If exactly 31 is reached, sub-round ends immediately.
        if (peggingCount == 31) {
            Log.i("FirstFragment", "Count reached 31; ending sub-round")
            endSubRound()
            return
        }
        // After a play, proceed to the next turn.
        nextTurn()
    }

    // Determines and initiates the next turn.
    private fun nextTurn() {
        Log.i("FirstFragment", "nextTurn called")
        if (allCardsPlayed()) {
            Log.i("FirstFragment", "All cards played; finishing pegging phase")
            finishPeggingPhase()
            return
        }
        if (isPlayerTurn) {
            // Human player's turn.
            if (checkPlayerCanPlayAnyCard()) {
                Log.i("FirstFragment", "Player has playable card; updating UI")
                updatePlayerPeggingUI()
            } else {
                Log.i("FirstFragment", "Player has no playable card; prompting Go")
                binding.buttonPlayCard.isEnabled = false
                binding.buttonSayGo.visibility = View.VISIBLE
                binding.buttonSayGo.isEnabled = true
                binding.textViewGameStatus.text = "No playable card. Press Go."
            }
        } else {
            // Opponent’s turn.
            Log.i("FirstFragment", "Switching turn to opponent")
            view?.postDelayed({ playOpponentCard() }, 1000)
        }
    }

    // Check if opponent has any playable card.
    private fun opponentCanPlay(): Boolean {
        val canPlay = opponentHand.filterIndexed { index, card ->
            !opponentCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
        }.isNotEmpty()
        Log.i("FirstFragment", "opponentCanPlay: $canPlay")
        return canPlay
    }

    // Opponent's turn.
    private fun playOpponentCard() {
        Log.i("FirstFragment", "Opponent's turn started")
        if (!isPeggingPhase) return
        // If opponent can play, choose a random playable card.
        if (opponentCanPlay()) {
            val playableCards = opponentHand.filterIndexed { index, card ->
                !opponentCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
            }
            if (playableCards.isNotEmpty()) {
                val cardToPlay = playableCards.random()
                val cardIndex = opponentHand.indexOf(cardToPlay)
                binding.textViewGameStatus.text = "Opponent plays ${cardToPlay.getSymbol()}"
                Log.i("FirstFragment", "Opponent selected card ${cardToPlay.getSymbol()} at index $cardIndex")
                processPlay(isPlayer = false, cardIndex = cardIndex)
            }
        } else {
            // Opponent cannot play. Simulate opponent saying GO.
            binding.textViewGameStatus.text = "Opponent says GO!"
            Log.i("FirstFragment", "Opponent cannot play; says GO")
            // Now check if the human player can play.
            if (checkPlayerCanPlayAnyCard()) {
                isPlayerTurn = true
                updatePlayerPeggingUI()
            } else {
                Log.i("FirstFragment", "Neither player can play; awarding point and ending sub-round")
                awardGoPoint()
                endSubRound()
            }
        }
    }

    // Award 1 point for a "Go" (if count is not exactly 31)
    private fun awardGoPoint() {
        if (peggingCount != 31 && peggingPile.isNotEmpty()) {
            if (lastPlayerWhoPlayed == "player") {
                playerScore += 1
                binding.textViewGameStatus.text = "You get 1 point for Go."
                Log.i("FirstFragment", "Player awarded 1 point for Go")
            } else {
                opponentScore += 1
                binding.textViewGameStatus.text = "Opponent gets 1 point for Go."
                Log.i("FirstFragment", "Opponent awarded 1 point for Go")
            }
            updateScores()
        }
    }

    // Ends the current sub-round of pegging (resetting the running total)
    private fun endSubRound() {
        Log.i("FirstFragment", "Ending sub-round")
        peggingCount = 0
        binding.textViewPeggingCount.text = "Count: 0"
        peggingPile.clear()
        // The next sub-round is led by the player who last played a card.
        isPlayerTurn = (lastPlayerWhoPlayed == "player")
        Log.i("FirstFragment", "Next sub-round lead: ${if (isPlayerTurn) "Player" else "Opponent"}")
        if (allCardsPlayed()) {
            finishPeggingPhase()
        } else {
            binding.textViewGameStatus.append("\nNew sub-round begins. " + if (isPlayerTurn) "Your turn." else "Opponent's turn.")
            nextTurn()
        }
    }

    // Update UI for the human player's pegging turn.
    private fun updatePlayerPeggingUI() {
        Log.i("FirstFragment", "updatePlayerPeggingUI called; isPlayerTurn = $isPlayerTurn")
        if (isPlayerTurn) {
            if (checkPlayerCanPlayAnyCard()) {
                binding.buttonPlayCard.isEnabled = selectedCards.isNotEmpty()
                binding.buttonSayGo.visibility = View.GONE
                Log.i("FirstFragment", "Player has playable cards; hiding Go button")
            } else {
                binding.buttonPlayCard.isEnabled = false
                binding.buttonSayGo.visibility = View.VISIBLE
                binding.buttonSayGo.isEnabled = true
                binding.textViewGameStatus.text = "No playable card. Press Go."
                Log.i("FirstFragment", "Player has no playable cards; showing Go button")
            }
        } else {
            binding.buttonPlayCard.isEnabled = false
            binding.buttonSayGo.visibility = View.GONE
        }
    }

    // Check if the human player has any card they can legally play.
    private fun checkPlayerCanPlayAnyCard(): Boolean {
        val canPlay = playerHand.filterIndexed { index, card ->
            !playerCardsPlayed.contains(index) && (peggingCount + card.getValue() <= 31)
        }.isNotEmpty()
        Log.i("FirstFragment", "checkPlayerCanPlayAnyCard: $canPlay")
        return canPlay
    }

    // Scoring function: evaluates the current play for 15/31, pairs, and runs.
    private fun checkPeggingScore(playedCard: Card): Int {
        var score = 0
        val statusMessages = mutableListOf<String>()

        // 15 or 31 rule
        if (peggingCount == 15) {
            score += 2
            statusMessages.add(getString(R.string.pegging_fifteen))
            Log.i("FirstFragment", "Scored 2 for fifteen. Count: $peggingCount")
        }
        if (peggingCount == 31) {
            score += 2
            statusMessages.add(getString(R.string.pegging_thirtyone))
            Log.i("FirstFragment", "Scored 2 for thirty-one. Count: $peggingCount")
        }

        // Pairs (and multiples)
        val consecutiveSameRankCards = peggingPile.reversed().takeWhile { it.rank == playedCard.rank }
        when (consecutiveSameRankCards.size) {
            2 -> {
                score += 2
                statusMessages.add(getString(R.string.pegging_pairs))
                Log.i("FirstFragment", "Scored 2 for a pair")
            }
            3 -> {
                score += 6
                statusMessages.add(getString(R.string.pegging_three_of_a_kind))
                Log.i("FirstFragment", "Scored 6 for three of a kind")
            }
            4 -> {
                score += 12
                statusMessages.add(getString(R.string.pegging_four_of_a_kind))
                Log.i("FirstFragment", "Scored 12 for four of a kind")
            }
        }

        // Runs: check the longest run in the tail of the pegging pile (minimum 3 cards)
        for (runLength in peggingPile.size downTo 3) {
            if (peggingPile.size >= runLength) {
                val lastCards = peggingPile.takeLast(runLength)
                // Use rank ordinal for run detection.
                val ordinals = lastCards.map { it.rank.ordinal }.sorted()
                val isRun = ordinals.zipWithNext().all { (a, b) -> b - a == 1 }
                if (isRun) {
                    score += runLength
                    statusMessages.add(getString(R.string.pegging_run, runLength, runLength))
                    Log.i("FirstFragment", "Scored $runLength for a run of length $runLength")
                    break  // only score the longest run
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
        Log.i("FirstFragment", "Scores updated: Player: $playerScore, Opponent: $opponentScore")
    }

    // Check if both players have played all their cards.
    private fun allCardsPlayed(): Boolean {
        val allPlayed = playerCardsPlayed.size == playerHand.size && opponentCardsPlayed.size == opponentHand.size
        Log.i("FirstFragment", "allCardsPlayed: $allPlayed")
        return allPlayed
    }

    private fun finishPeggingPhase() {
        Log.i("FirstFragment", "Finishing pegging phase")
        isPeggingPhase = false
        binding.textViewGameStatus.text = getString(R.string.pegging_complete)
        binding.buttonPlayCard.isEnabled = false
        binding.textViewPeggingCount.visibility = View.GONE

        Log.i("FirstFragment", "Pegging complete. Starter card: ${binding.textViewCutCard.text}")
        binding.textViewGameStatus.text = "Pegging complete. See cut card above."
    }

    override fun onDestroyView() {
        Log.i("FirstFragment", "onDestroyView called")
        super.onDestroyView()
        _binding = null
    }
}

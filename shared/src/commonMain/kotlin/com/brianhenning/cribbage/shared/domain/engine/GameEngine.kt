package com.brianhenning.cribbage.shared.domain.engine

import com.brianhenning.cribbage.shared.domain.logic.*
import com.brianhenning.cribbage.shared.domain.model.Card
import com.brianhenning.cribbage.shared.domain.model.createDeck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Platform-agnostic game engine that manages all Cribbage game logic and state.
 * Uses StateFlow for reactive state updates that both Android and iOS can observe.
 */
class GameEngine(
    private val persistence: GamePersistence? = null
) {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var peggingManager: PeggingRoundManager? = null
    private var drawDeck: List<Card> = emptyList()

    init {
        // Load persisted stats on initialization
        persistence?.let { prefs ->
            _state.value = _state.value.copy(
                gamesWon = prefs.getGamesWon(),
                gamesLost = prefs.getGamesLost(),
                skunksFor = prefs.getSkunksFor(),
                skunksAgainst = prefs.getSkunksAgainst()
            )

            // Load last cut cards if available
            val cutCards = prefs.getLastCutCards()
            if (cutCards != null) {
                _state.value = _state.value.copy(
                    cutPlayerCard = cutCards.first,
                    cutOpponentCard = cutCards.second
                )
            }
        }
    }

    /**
     * Start a new game
     */
    fun startNewGame() {
        // Reset game state but preserve stats
        val stats = _state.value
        _state.value = GameState(
            gameStarted = true,
            currentPhase = GamePhase.CUT_FOR_DEALER,
            gamesWon = stats.gamesWon,
            gamesLost = stats.gamesLost,
            skunksFor = stats.skunksFor,
            skunksAgainst = stats.skunksAgainst,
            gameStatus = "Cut cards to determine dealer."
        )
        peggingManager = null
        drawDeck = emptyList()
    }

    /**
     * Cut cards to determine dealer
     */
    fun cutForDealer(playerCutCard: Card, opponentCutCard: Card) {
        _state.value = _state.value.copy(
            cutPlayerCard = playerCutCard,
            cutOpponentCard = opponentCutCard,
            showCutForDealer = true
        )

        // Save cut cards to persistence
        persistence?.saveLastCutCards(playerCutCard, opponentCutCard)

        // Determine dealer
        val dealer = dealerFromCut(playerCutCard, opponentCutCard)
        when (dealer) {
            Player.PLAYER -> {
                _state.value = _state.value.copy(
                    isPlayerDealer = true,
                    currentPhase = GamePhase.DEALING,
                    gameStatus = "You are the dealer! Tap \"Deal Cards\" to continue."
                )
            }
            Player.OPPONENT -> {
                _state.value = _state.value.copy(
                    isPlayerDealer = false,
                    currentPhase = GamePhase.DEALING,
                    gameStatus = "Opponent is the dealer! Tap \"Deal Cards\" to continue."
                )
            }
            null -> {
                // Tie - cut again
                _state.value = _state.value.copy(
                    gameStatus = "Tie! Cut again to determine dealer."
                )
            }
        }
    }

    /**
     * Deal cards to both players
     */
    fun dealCards() {
        val deck = createDeck().toMutableList()
        deck.shuffle()

        val dealResult = dealSixToEach(deck, _state.value.isPlayerDealer)

        drawDeck = dealResult.remainingDeck

        _state.value = _state.value.copy(
            playerHand = dealResult.playerHand,
            opponentHand = dealResult.opponentHand,
            currentPhase = GamePhase.CRIB_SELECTION,
            selectedCards = emptySet(),
            gameStatus = if (_state.value.isPlayerDealer) {
                "You are the dealer. Select 2 cards for the crib."
            } else {
                "Opponent is the dealer. Select 2 cards for the crib."
            }
        )
    }

    /**
     * Toggle card selection for crib
     */
    fun toggleCardSelection(index: Int) {
        val currentSelected = _state.value.selectedCards
        val newSelected = if (index in currentSelected) {
            currentSelected - index
        } else if (currentSelected.size < 2) {
            currentSelected + index
        } else {
            currentSelected
        }
        _state.value = _state.value.copy(selectedCards = newSelected)
    }

    /**
     * Confirm crib selection
     */
    fun confirmCribSelection() {
        val selected = _state.value.selectedCards.toList().sorted()
        if (selected.size != 2) return

        // Move selected cards to crib
        val playerHand = _state.value.playerHand.toMutableList()
        val crib = mutableListOf(playerHand[selected[0]], playerHand[selected[1]])
        playerHand.removeAt(selected[1]) // Remove higher index first
        playerHand.removeAt(selected[0])

        // Opponent AI selects cards for crib
        val opponentHand = _state.value.opponentHand
        val opponentCribCards = OpponentAI.chooseCribCards(
            opponentHand,
            !_state.value.isPlayerDealer
        )
        val opponentHandFiltered = opponentHand.filter { it !in opponentCribCards }
        crib.addAll(opponentCribCards)

        // Cut starter card
        val starter = drawDeck.firstOrNull()
        drawDeck = drawDeck.drop(1)

        // Check for His Nobs (Jack of same suit as starter) during dealing
        var statusMessage = "Starter card: ${starter?.toString() ?: "?"}"
        var playerScoreUpdate = _state.value.playerScore
        var opponentScoreUpdate = _state.value.opponentScore

        if (starter != null && starter.rank.name == "JACK") {
            if (_state.value.isPlayerDealer) {
                playerScoreUpdate += 2
                statusMessage += "\nYou scored 2 for His Heels (Jack cut)!"
            } else {
                opponentScoreUpdate += 2
                statusMessage += "\nOpponent scored 2 for His Heels (Jack cut)!"
            }
        }

        // Start pegging phase
        peggingManager = PeggingRoundManager(
            startingPlayer = if (_state.value.isPlayerDealer) Player.OPPONENT else Player.PLAYER
        )

        _state.value = _state.value.copy(
            playerHand = playerHand,
            opponentHand = opponentHandFiltered,
            cribHand = crib,
            starterCard = starter,
            selectedCards = emptySet(),
            currentPhase = GamePhase.PEGGING,
            isPeggingPhase = true,
            isPlayerTurn = !_state.value.isPlayerDealer,
            peggingCount = 0,
            peggingPile = emptyList(),
            playerCardsPlayed = emptySet(),
            opponentCardsPlayed = emptySet(),
            playerScore = playerScoreUpdate,
            opponentScore = opponentScoreUpdate,
            gameStatus = statusMessage + "\n" +
                if (!_state.value.isPlayerDealer) "Your turn to play." else "Opponent's turn."
        )

        checkGameOver()
    }

    /**
     * Play a card during pegging
     */
    fun playCard(cardIndex: Int, isPlayer: Boolean): Boolean {
        if (_state.value.isOpponentActionInProgress) return false
        if (isPlayer && !_state.value.isPlayerTurn) return false

        val hand = if (isPlayer) _state.value.playerHand else _state.value.opponentHand
        val played = if (isPlayer) _state.value.playerCardsPlayed else _state.value.opponentCardsPlayed

        if (cardIndex !in hand.indices || cardIndex in played) return false

        val card = hand[cardIndex]
        val mgr = peggingManager ?: return false

        // Check if play is legal
        val newCount = mgr.peggingCount + card.getValue()
        if (newCount > 31) return false

        // Save pile and count before play for scoring
        val pileBeforePlay = mgr.peggingPile.toList()
        val countBeforePlay = mgr.peggingCount

        // Execute play
        val outcome = mgr.onPlay(card)

        // Calculate points
        val pileAfterPlay = mgr.peggingPile.toList()
        val countAfterPlay = mgr.peggingCount
        val pts = PeggingScorer.pointsForPile(pileAfterPlay, countAfterPlay)

        // Award points
        var statusUpdate = _state.value.gameStatus
        var playerScoreUpdate = _state.value.playerScore
        var opponentScoreUpdate = _state.value.opponentScore

        if (pts.total > 0) {
            if (isPlayer) {
                playerScoreUpdate += pts.total
                statusUpdate += "\nYou scored ${pts.total} points!"
            } else {
                opponentScoreUpdate += pts.total
                statusUpdate += "\nOpponent scored ${pts.total} points!"
            }
        }

        // Update state
        val newPlayed = if (isPlayer) {
            _state.value.playerCardsPlayed + cardIndex
        } else {
            _state.value.opponentCardsPlayed + cardIndex
        }

        _state.value = _state.value.copy(
            peggingCount = mgr.peggingCount,
            peggingPile = mgr.peggingPile,
            isPlayerTurn = mgr.isPlayerTurn == Player.PLAYER,
            playerCardsPlayed = if (isPlayer) newPlayed else _state.value.playerCardsPlayed,
            opponentCardsPlayed = if (!isPlayer) newPlayed else _state.value.opponentCardsPlayed,
            playerScore = playerScoreUpdate,
            opponentScore = opponentScoreUpdate,
            gameStatus = statusUpdate
        )

        // Handle reset after 31
        if (outcome.reset != null && outcome.reset.resetFor31) {
            _state.value = _state.value.copy(
                pendingReset = PendingResetState(
                    pile = pileAfterPlay,
                    finalCount = 31,
                    scoreAwarded = pts.total,
                    message = "31!"
                )
            )
        }

        // Check if pegging is complete
        checkPeggingComplete()
        checkGameOver()

        return true
    }

    /**
     * Handle "Go" during pegging
     */
    fun handleGo() {
        val mgr = peggingManager ?: return

        // Check if opponent has legal moves
        val opponentHand = _state.value.opponentHand
        val opponentPlayed = _state.value.opponentCardsPlayed
        val opponentHasLegalMove = opponentHand.indices.any { i ->
            i !in opponentPlayed && (mgr.peggingCount + opponentHand[i].getValue()) <= 31
        }

        val reset = mgr.onGo(opponentHasLegalMove)

        if (reset != null) {
            // Award Go point
            val goWinner = reset.goPointTo
            var playerScoreUpdate = _state.value.playerScore
            var opponentScoreUpdate = _state.value.opponentScore

            if (goWinner == Player.PLAYER) {
                playerScoreUpdate += 1
            } else if (goWinner == Player.OPPONENT) {
                opponentScoreUpdate += 1
            }

            _state.value = _state.value.copy(
                playerScore = playerScoreUpdate,
                opponentScore = opponentScoreUpdate,
                peggingCount = 0,
                peggingPile = emptyList(),
                gameStatus = _state.value.gameStatus + "\nGo! Pile reset."
            )
        }

        _state.value = _state.value.copy(
            isPlayerTurn = mgr.isPlayerTurn == Player.PLAYER
        )

        checkPeggingComplete()
        checkGameOver()
    }

    /**
     * Acknowledge pending reset (after 31 or Go)
     */
    fun acknowledgePendingReset() {
        _state.value = _state.value.copy(
            pendingReset = null,
            peggingCount = 0,
            peggingPile = emptyList()
        )
    }

    /**
     * Check if pegging phase is complete
     */
    private fun checkPeggingComplete() {
        if (_state.value.playerCardsPlayed.size == 4 && _state.value.opponentCardsPlayed.size == 4) {
            _state.value = _state.value.copy(
                isPeggingPhase = false,
                currentPhase = GamePhase.HAND_COUNTING,
                gameStatus = _state.value.gameStatus + "\nPegging complete. Count hands."
            )
        }
    }

    /**
     * Start hand counting phase
     */
    fun startHandCounting() {
        _state.value = _state.value.copy(
            isInHandCountingPhase = true,
            countingPhase = CountingPhase.NON_DEALER
        )
        proceedToNextCountingPhase()
    }

    /**
     * Proceed to next counting phase
     */
    fun proceedToNextCountingPhase() {
        val currentPhase = _state.value.countingPhase
        val starter = _state.value.starterCard ?: return

        when (currentPhase) {
            CountingPhase.NON_DEALER -> {
                // Score non-dealer's hand
                val hand = if (_state.value.isPlayerDealer) _state.value.opponentHand else _state.value.playerHand
                val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter, false)

                val updatedScores = _state.value.handScores.copy(
                    nonDealerScore = breakdown.totalScore,
                    nonDealerBreakdown = breakdown
                )

                val updatedPlayerScore = if (!_state.value.isPlayerDealer) {
                    _state.value.playerScore + breakdown.totalScore
                } else {
                    _state.value.playerScore
                }

                val updatedOpponentScore = if (_state.value.isPlayerDealer) {
                    _state.value.opponentScore + breakdown.totalScore
                } else {
                    _state.value.opponentScore
                }

                _state.value = _state.value.copy(
                    handScores = updatedScores,
                    playerScore = updatedPlayerScore,
                    opponentScore = updatedOpponentScore,
                    countingPhase = CountingPhase.DEALER
                )

                checkGameOver()
            }
            CountingPhase.DEALER -> {
                // Score dealer's hand
                val hand = if (_state.value.isPlayerDealer) _state.value.playerHand else _state.value.opponentHand
                val breakdown = CribbageScorer.scoreHandWithBreakdown(hand, starter, false)

                val updatedScores = _state.value.handScores.copy(
                    dealerScore = breakdown.totalScore,
                    dealerBreakdown = breakdown
                )

                val updatedPlayerScore = if (_state.value.isPlayerDealer) {
                    _state.value.playerScore + breakdown.totalScore
                } else {
                    _state.value.playerScore
                }

                val updatedOpponentScore = if (!_state.value.isPlayerDealer) {
                    _state.value.opponentScore + breakdown.totalScore
                } else {
                    _state.value.opponentScore
                }

                _state.value = _state.value.copy(
                    handScores = updatedScores,
                    playerScore = updatedPlayerScore,
                    opponentScore = updatedOpponentScore,
                    countingPhase = CountingPhase.CRIB
                )

                checkGameOver()
            }
            CountingPhase.CRIB -> {
                // Score crib
                val breakdown = CribbageScorer.scoreHandWithBreakdown(_state.value.cribHand, starter, true)

                val updatedScores = _state.value.handScores.copy(
                    cribScore = breakdown.totalScore,
                    cribBreakdown = breakdown
                )

                val updatedPlayerScore = if (_state.value.isPlayerDealer) {
                    _state.value.playerScore + breakdown.totalScore
                } else {
                    _state.value.playerScore
                }

                val updatedOpponentScore = if (!_state.value.isPlayerDealer) {
                    _state.value.opponentScore + breakdown.totalScore
                } else {
                    _state.value.opponentScore
                }

                _state.value = _state.value.copy(
                    handScores = updatedScores,
                    playerScore = updatedPlayerScore,
                    opponentScore = updatedOpponentScore,
                    countingPhase = CountingPhase.COMPLETED,
                    isInHandCountingPhase = false
                )

                checkGameOver()

                // If game not over, start new round
                if (!_state.value.gameOver) {
                    startNewRound()
                }
            }
            else -> {}
        }
    }

    /**
     * Start a new round (after hand counting)
     */
    private fun startNewRound() {
        _state.value = _state.value.copy(
            currentPhase = GamePhase.DEALING,
            isPlayerDealer = !_state.value.isPlayerDealer,
            gameStatus = if (_state.value.isPlayerDealer) {
                "Opponent is now the dealer."
            } else {
                "You are now the dealer."
            },
            countingPhase = CountingPhase.NONE,
            handScores = HandScores()
        )
    }

    /**
     * Check if game is over (score > 120)
     */
    private fun checkGameOver() {
        if (_state.value.playerScore > 120 || _state.value.opponentScore > 120) {
            val playerWins = _state.value.playerScore > _state.value.opponentScore
            val loserScore = if (playerWins) _state.value.opponentScore else _state.value.playerScore
            val skunked = loserScore < 91

            val newGamesWon = if (playerWins) _state.value.gamesWon + 1 else _state.value.gamesWon
            val newGamesLost = if (!playerWins) _state.value.gamesLost + 1 else _state.value.gamesLost
            val newSkunksFor = if (playerWins && skunked) _state.value.skunksFor + 1 else _state.value.skunksFor
            val newSkunksAgainst = if (!playerWins && skunked) _state.value.skunksAgainst + 1 else _state.value.skunksAgainst

            // Persist stats
            persistence?.saveGameStats(newGamesWon, newGamesLost, newSkunksFor, newSkunksAgainst)

            _state.value = _state.value.copy(
                gameOver = true,
                currentPhase = GamePhase.GAME_OVER,
                gamesWon = newGamesWon,
                gamesLost = newGamesLost,
                skunksFor = newSkunksFor,
                skunksAgainst = newSkunksAgainst,
                showWinnerModal = true,
                winnerModalData = WinnerModalData(
                    playerWon = playerWins,
                    playerScore = _state.value.playerScore,
                    opponentScore = _state.value.opponentScore,
                    wasSkunk = skunked,
                    gamesWon = newGamesWon,
                    gamesLost = newGamesLost,
                    skunksFor = newSkunksFor,
                    skunksAgainst = newSkunksAgainst
                ),
                gameStatus = if (playerWins) "You win!" else "Opponent wins!"
            )
        }
    }

    /**
     * Dismiss winner modal
     */
    fun dismissWinnerModal() {
        _state.value = _state.value.copy(showWinnerModal = false)
    }

    /**
     * Set opponent action in progress (to prevent race conditions)
     */
    fun setOpponentActionInProgress(inProgress: Boolean) {
        _state.value = _state.value.copy(isOpponentActionInProgress = inProgress)
    }
}

/**
 * Persistence interface for platform-specific storage
 */
interface GamePersistence {
    fun getGamesWon(): Int
    fun getGamesLost(): Int
    fun getSkunksFor(): Int
    fun getSkunksAgainst(): Int
    fun getLastCutCards(): Pair<Card, Card>?

    fun saveGameStats(gamesWon: Int, gamesLost: Int, skunksFor: Int, skunksAgainst: Int)
    fun saveLastCutCards(playerCard: Card, opponentCard: Card)
}

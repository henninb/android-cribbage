import SwiftUI
import shared

struct ContentView: View {
    @StateObject private var viewModel = GameViewModel()

    var body: some View {
        ZStack {
            Color(red: 0.1, green: 0.3, blue: 0.2)
                .ignoresSafeArea()

            VStack(spacing: 0) {
                // Zone 1: Compact Score Header (fixed at top)
                CompactScoreHeader(
                    playerScore: Int(viewModel.gameState.playerScore),
                    opponentScore: Int(viewModel.gameState.opponentScore),
                    isPlayerDealer: viewModel.gameState.isPlayerDealer,
                    starterCard: viewModel.gameState.starterCard
                )

                // Zone 2: Dynamic Game Area (flexible middle)
                GeometryReader { geo in
                    ScrollView {
                        VStack(spacing: 16) {
                            switch viewModel.gameState.currentPhase {
                            case .setup:
                                if !viewModel.gameState.gameStarted {
                                    WelcomeHomeScreen()
                                } else if viewModel.gameState.cutPlayerCard != nil && viewModel.gameState.cutOpponentCard != nil {
                                    CutForDealerDisplay(
                                        playerCard: viewModel.gameState.cutPlayerCard!,
                                        opponentCard: viewModel.gameState.cutOpponentCard!
                                    )
                                } else {
                                    StatusMessageView(message: viewModel.gameState.gameStatus)
                                }

                            case .dealing:
                                StatusMessageView(message: "Dealing cards...")

                            case .cribSelection:
                                CribSelectionView(
                                    gameState: viewModel.gameState,
                                    viewModel: viewModel
                                )

                            case .pegging:
                                PeggingView(
                                    gameState: viewModel.gameState,
                                    viewModel: viewModel
                                )

                            case .handCounting:
                                HandCountingView(
                                    gameState: viewModel.gameState,
                                    viewModel: viewModel
                                )

                            case .gameOver:
                                StatusMessageView(message: "Game Over!")

                            default:
                                StatusMessageView(message: viewModel.gameState.gameStatus)
                            }
                        }
                        .padding()
                        .frame(minHeight: geo.size.height)
                    }
                }

                // Zone 3: Action Bar (fixed at bottom)
                ActionBarView(
                    gameState: viewModel.gameState,
                    viewModel: viewModel
                )
            }
        }
    }
}

// MARK: - Zone 1: Compact Score Header

struct CompactScoreHeader: View {
    let playerScore: Int
    let opponentScore: Int
    let isPlayerDealer: Bool
    let starterCard: Card?

    var body: some View {
        ZStack(alignment: .topTrailing) {
            // Main content
            HStack(spacing: 0) {
                // Player score section
                ScoreSectionView(
                    label: "You",
                    score: playerScore,
                    isDealer: isPlayerDealer,
                    color: Color.blue
                )
                .frame(maxWidth: .infinity)

                // Divider
                Rectangle()
                    .fill(Color.gray.opacity(0.3))
                    .frame(width: 1, height: 40)
                    .padding(.horizontal, 8)

                // Opponent score section
                ScoreSectionView(
                    label: "Opponent",
                    score: opponentScore,
                    isDealer: !isPlayerDealer,
                    color: Color.orange
                )
                .frame(maxWidth: .infinity)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .padding(.trailing, starterCard != nil ? 60 : 0)
            .background(Color(UIColor.systemBackground).opacity(0.95))

            // Theme indicator in top-left
            HStack(spacing: 4) {
                Text("🃏")
                    .font(.system(size: 10))
                Text("Classic")
                    .font(.system(size: 10))
                    .foregroundColor(.secondary)
            }
            .padding(.leading, 4)
            .padding(.top, 4)
            .frame(maxWidth: .infinity, alignment: .leading)

            // Starter card in top-right corner
            if let starter = starterCard {
                CardView(
                    card: starter,
                    size: .small
                )
                .padding(.trailing, 4)
                .padding(.top, 4)
            }
        }
        .shadow(color: .black.opacity(0.1), radius: 4, y: 2)
    }
}

struct ScoreSectionView: View {
    let label: String
    let score: Int
    let isDealer: Bool
    let color: Color

    var body: some View {
        VStack(spacing: 4) {
            // Label + dealer indicator
            HStack(spacing: 4) {
                Text(label)
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(color)

                if isDealer {
                    Image(systemName: "die.face.5.fill")
                        .font(.caption2)
                        .foregroundColor(.orange)
                }
            }

            // Score
            Text("\(score)")
                .font(.system(size: 32, weight: .bold))
                .foregroundColor(color)

            // Progress bar (score out of 121)
            GeometryReader { geometry in
                ZStack(alignment: .leading) {
                    // Track
                    Rectangle()
                        .fill(Color.gray.opacity(0.2))
                        .frame(height: 6)
                        .cornerRadius(3)

                    // Progress
                    Rectangle()
                        .fill(color)
                        .frame(width: geometry.size.width * CGFloat(min(score, 121)) / 121.0, height: 6)
                        .cornerRadius(3)
                }
            }
            .frame(height: 6)
        }
    }
}

// MARK: - Zone 2: Game Area

struct GameAreaView: View {
    let gameState: GameState
    let viewModel: GameViewModel

    var body: some View {
        ZStack {
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 16) {
                    switch gameState.currentPhase {
                    case .setup:
                        if !gameState.gameStarted {
                            WelcomeHomeScreen()
                        } else if gameState.cutPlayerCard != nil && gameState.cutOpponentCard != nil {
                            CutForDealerDisplay(
                                playerCard: gameState.cutPlayerCard!,
                                opponentCard: gameState.cutOpponentCard!
                            )
                        } else {
                            StatusMessageView(message: gameState.gameStatus)
                        }

                    case .dealing:
                        StatusMessageView(message: "Dealing cards...")

                    case .cribSelection:
                        CribSelectionView(
                            gameState: gameState,
                            viewModel: viewModel
                        )

                    case .pegging:
                        PeggingView(
                            gameState: gameState,
                            viewModel: viewModel
                        )

                    case .handCounting:
                        HandCountingView(
                            gameState: gameState,
                            viewModel: viewModel
                        )

                    case .gameOver:
                        StatusMessageView(message: "Game Over!")

                    default:
                        StatusMessageView(message: gameState.gameStatus)
                    }
                }
                .padding()
            }

            // Winner Modal
            if gameState.showWinnerModal, let data = gameState.winnerModalData {
                WinnerModalView(data: data) {
                    viewModel.dismissWinnerModal()
                    viewModel.startNewGame()
                }
            }

            // Pending Reset
            if let pendingReset = gameState.pendingReset {
                PendingResetView(resetState: pendingReset) {
                    viewModel.acknowledgePendingReset()
                }
            }
        }
    }
}

// MARK: - Game Area Components

struct WelcomeHomeScreen: View {
    var body: some View {
        VStack(spacing: 32) {
            // Card icon in rounded square
            ZStack {
                RoundedRectangle(cornerRadius: 24)
                    .fill(Color.blue.opacity(0.2))
                    .frame(width: 120, height: 120)
                    .shadow(radius: 8)

                Text("🃏")
                    .font(.system(size: 72))
            }

            VStack(spacing: 8) {
                Text("Cribbage")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .foregroundColor(.white)

                Text("Classic Card Game")
                    .font(.title3)
                    .foregroundColor(.white.opacity(0.7))
            }

            // Welcome message card
            VStack(spacing: 12) {
                Text("Welcome!")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(.white)

                Text("Play cribbage against the computer. Be the first to reach 121 points!")
                    .font(.body)
                    .foregroundColor(.white.opacity(0.9))
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
            }
            .padding(24)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(Color.white.opacity(0.1))
            )

            // Instruction hint
            HStack(spacing: 8) {
                Text("👇")
                    .font(.title)
                Text("Tap \"Start New Game\" below to begin")
                    .font(.callout)
                    .foregroundColor(.white.opacity(0.8))
            }
        }
        .padding(32)
    }
}

struct CutForDealerDisplay: View {
    let playerCard: Card
    let opponentCard: Card

    var body: some View {
        VStack(spacing: 16) {
            Text("Cut for Dealer")
                .font(.title2)
                .fontWeight(.semibold)
                .foregroundColor(.white)

            HStack(spacing: 16) {
                VStack(spacing: 8) {
                    Text("You")
                        .font(.caption)
                        .foregroundColor(.white.opacity(0.7))
                    CardView(card: playerCard, size: .medium)
                }

                Text("vs")
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(.white)

                VStack(spacing: 8) {
                    Text("Opponent")
                        .font(.caption)
                        .foregroundColor(.white.opacity(0.7))
                    CardView(card: opponentCard, size: .medium)
                }
            }

            Text("Lower card deals first")
                .font(.caption)
                .foregroundColor(.white.opacity(0.7))
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.white.opacity(0.1))
        )
    }
}

struct StatusMessageView: View {
    let message: String

    var body: some View {
        Text(message)
            .font(.body)
            .foregroundColor(.white)
            .multilineTextAlignment(.center)
            .padding()
    }
}

struct CribSelectionView: View {
    let gameState: GameState
    let viewModel: GameViewModel

    var body: some View {
        VStack(spacing: 16) {
            // Opponent hand (face down)
            CompactHandDisplay(
                label: "Opponent:",
                hand: gameState.opponentHand,
                playedIndices: gameState.opponentCardsPlayed,
                selectedIndices: [],
                showCards: false
            )

            Text(gameState.gameStatus)
                .font(.body)
                .foregroundColor(.white)
                .multilineTextAlignment(.center)
                .padding(.vertical, 8)

            // Player hand (selectable)
            PlayerHandCompact(
                hand: gameState.playerHand,
                selectedIndices: gameState.selectedCards,
                playedIndices: gameState.playerCardsPlayed,
                onCardTap: { index in
                    viewModel.toggleCardSelection(index: index)
                }
            )
        }
    }
}

struct PeggingView: View {
    let gameState: GameState
    let viewModel: GameViewModel

    var body: some View {
        VStack(spacing: 8) {
            // Opponent hand
            CompactHandDisplay(
                label: "Opponent:",
                hand: gameState.opponentHand,
                playedIndices: gameState.opponentCardsPlayed,
                selectedIndices: [],
                showCards: false
            )

            // Pegging count (large and prominent)
            Text("Count: \(gameState.peggingCount)")
                .font(.title)
                .fontWeight(.bold)
                .foregroundColor(.yellow)
                .padding(.horizontal, 20)
                .padding(.vertical, 8)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Color.blue.opacity(0.3))
                )

            // Pegging pile
            if !gameState.peggingPile.isEmpty {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: -20) {
                        ForEach(Array(gameState.peggingPile.enumerated()), id: \.offset) { _, card in
                            CardView(card: card, size: .medium)
                        }
                    }
                    .padding(.horizontal, 8)
                }
                .frame(height: 120)
            }

            // Turn indicator
            Text(gameState.isPlayerTurn ? "Your turn" : "Opponent's turn")
                .font(.callout)
                .fontWeight(.semibold)
                .foregroundColor(gameState.isPlayerTurn ? .blue : .orange)

            // Player hand
            PlayerHandCompact(
                hand: gameState.playerHand,
                selectedIndices: [],
                playedIndices: gameState.playerCardsPlayed,
                isPlayerTurn: gameState.isPlayerTurn,
                onCardTap: { index in
                    if gameState.isPlayerTurn {
                        _ = viewModel.playCard(cardIndex: index, isPlayer: true)
                    }
                }
            )
        }
    }
}

struct HandCountingView: View {
    let gameState: GameState
    let viewModel: GameViewModel

    var body: some View {
        VStack(spacing: 16) {
            Text("Hand Counting Phase")
                .font(.title2)
                .fontWeight(.bold)
                .foregroundColor(.white)

            if let starter = gameState.starterCard {
                VStack {
                    Text("Starter Card")
                        .font(.caption)
                        .foregroundColor(.white.opacity(0.7))
                    CardView(card: starter, size: .medium)
                }
            }

            // Show hand scores
            if gameState.countingPhase != .none {
                VStack(spacing: 12) {
                    if gameState.handScores.nonDealerScore > 0 {
                        ScoreDisplayView(
                            label: gameState.isPlayerDealer ? "Opponent's Hand" : "Your Hand",
                            score: Int(gameState.handScores.nonDealerScore)
                        )
                    }

                    if gameState.handScores.dealerScore > 0 {
                        ScoreDisplayView(
                            label: gameState.isPlayerDealer ? "Your Hand" : "Opponent's Hand",
                            score: Int(gameState.handScores.dealerScore)
                        )
                    }

                    if gameState.handScores.cribScore > 0 {
                        ScoreDisplayView(
                            label: "Crib",
                            score: Int(gameState.handScores.cribScore)
                        )
                    }
                }
            }
        }
    }
}

struct ScoreDisplayView: View {
    let label: String
    let score: Int

    var body: some View {
        HStack {
            Text(label)
                .foregroundColor(.white)
            Spacer()
            Text("+\(score)")
                .fontWeight(.bold)
                .foregroundColor(.green)
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 8)
                .fill(Color.white.opacity(0.1))
        )
    }
}

struct CompactHandDisplay: View {
    let label: String
    let hand: [Card]
    let playedIndices: Set<KotlinInt>
    let selectedIndices: Set<KotlinInt>
    let showCards: Bool

    var body: some View {
        VStack(spacing: 8) {
            // Label with remaining card count
            HStack(spacing: 4) {
                Text(label)
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(.white.opacity(0.7))

                if !playedIndices.isEmpty {
                    Text("(\(hand.count - playedIndices.count) left)")
                        .font(.caption2)
                        .foregroundColor(.white.opacity(0.5))
                }
            }

            // Cards row with increased overlap (-45 like Android)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: -45) {
                    ForEach(Array(hand.enumerated()), id: \.offset) { index, card in
                        CardView(
                            card: card,
                            isSelected: selectedIndices.contains(KotlinInt(value: Int32(index))),
                            isPlayed: playedIndices.contains(KotlinInt(value: Int32(index))),
                            isRevealed: showCards,
                            size: .large
                        )
                    }
                }
                .padding(.horizontal, 12)
            }
            .frame(height: 140)
        }
    }
}

struct PlayerHandCompact: View {
    let hand: [Card]
    let selectedIndices: Set<KotlinInt>
    let playedIndices: Set<KotlinInt>
    var isPlayerTurn: Bool = true
    var onCardTap: ((Int) -> Void)? = nil

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: -45) {
                ForEach(Array(hand.enumerated()), id: \.offset) { index, card in
                    CardView(
                        card: card,
                        isSelected: selectedIndices.contains(KotlinInt(value: Int32(index))),
                        isPlayed: playedIndices.contains(KotlinInt(value: Int32(index))),
                        isRevealed: true,
                        size: .large,
                        onTap: (isPlayerTurn && !playedIndices.contains(KotlinInt(value: Int32(index))) && onCardTap != nil) ? {
                            onCardTap?(index)
                        } : nil
                    )
                }
            }
            .padding(.horizontal, 12)
        }
        .frame(height: 140)
    }
}

// MARK: - Zone 3: Action Bar

struct ActionBarView: View {
    let gameState: GameState
    let viewModel: GameViewModel

    var body: some View {
        VStack(spacing: 12) {
            // Context-sensitive buttons
            if !gameState.gameStarted {
                PrimaryButton(title: "Start New Game") {
                    viewModel.startNewGame()
                }
            } else if gameState.gameOver {
                PrimaryButton(title: "New Game") {
                    viewModel.startNewGame()
                }
            } else if gameState.currentPhase == .setup || gameState.currentPhase == .cutForDealer || gameState.currentPhase == .dealing {
                PrimaryButton(title: "Deal Cards") {
                    viewModel.dealCards()
                }
            } else if gameState.currentPhase == .cribSelection {
                PrimaryButton(
                    title: gameState.isPlayerDealer ? "My Crib" : "Opponent's Crib",
                    isEnabled: gameState.selectedCards.count == 2
                ) {
                    viewModel.confirmCribSelection()
                }
            } else if gameState.currentPhase == .pegging {
                if gameState.isPlayerTurn {
                    SecondaryButton(title: "Go") {
                        viewModel.handleGo()
                    }
                }
            } else if gameState.currentPhase == .handCounting {
                if !gameState.isInHandCountingPhase {
                    PrimaryButton(title: "Count Hands") {
                        viewModel.startHandCounting()
                    }
                } else if gameState.countingPhase != .completed {
                    PrimaryButton(title: "Next") {
                        viewModel.proceedToNextCountingPhase()
                    }
                }
            }

            // Stats display
            if gameState.gamesWon > 0 || gameState.gamesLost > 0 {
                HStack(spacing: 20) {
                    Text("Wins: \(gameState.gamesWon)")
                    Text("Losses: \(gameState.gamesLost)")
                }
                .font(.caption)
                .foregroundColor(.white.opacity(0.7))
            }
        }
        .padding()
        .padding(.bottom, 20) // Extra bottom padding for safe area
        .background(Color.black.opacity(0.7))
    }
}

// MARK: - Buttons

struct PrimaryButton: View {
    let title: String
    var isEnabled: Bool = true
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.headline)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding()
                .background(
                    RoundedRectangle(cornerRadius: 10)
                        .fill(isEnabled ? Color.blue : Color.gray)
                )
        }
        .disabled(!isEnabled)
    }
}

struct SecondaryButton: View {
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.headline)
                .foregroundColor(.white)
                .frame(minWidth: 150)
                .padding()
                .background(
                    RoundedRectangle(cornerRadius: 10)
                        .fill(Color.orange)
                )
        }
    }
}

// MARK: - Modals

struct WinnerModalView: View {
    let data: WinnerModalData
    let onDismiss: () -> Void

    var body: some View {
        ZStack {
            Color.black.opacity(0.7)
                .ignoresSafeArea()

            VStack(spacing: 20) {
                // Celebration icon
                Text(data.playerWon ? "🏆" : "😔")
                    .font(.system(size: 80))

                Text(data.playerWon ? "You Win!" : "Opponent Wins!")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .foregroundColor(data.playerWon ? .blue : .orange)

                if data.wasSkunk {
                    HStack {
                        Text("🦨")
                        Text("SKUNK!")
                            .fontWeight(.bold)
                            .foregroundColor(.orange)
                    }
                    .padding(12)
                    .background(
                        RoundedRectangle(cornerRadius: 8)
                            .fill(Color.orange.opacity(0.2))
                    )
                }

                Divider()

                // Final scores
                HStack(spacing: 40) {
                    VStack {
                        Text("You")
                            .foregroundColor(.secondary)
                        Text("\(data.playerScore)")
                            .font(.largeTitle)
                            .fontWeight(.bold)
                            .foregroundColor(.blue)
                    }

                    Text("-")
                        .font(.title)
                        .fontWeight(.bold)

                    VStack {
                        Text("Opponent")
                            .foregroundColor(.secondary)
                        Text("\(data.opponentScore)")
                            .font(.largeTitle)
                            .fontWeight(.bold)
                            .foregroundColor(.orange)
                    }
                }

                Divider()

                // Match statistics
                VStack(spacing: 8) {
                    Text("Match Record")
                        .font(.caption)
                        .fontWeight(.semibold)
                        .foregroundColor(.blue)

                    Text("\(data.gamesWon) - \(data.gamesLost)")
                        .font(.title2)
                        .fontWeight(.bold)

                    Text("Skunks: \(data.skunksFor) - \(data.skunksAgainst)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding()
                .frame(maxWidth: .infinity)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(Color.gray.opacity(0.1))
                )

                Button("OK") {
                    onDismiss()
                }
                .buttonStyle(PrimaryButtonStyle())
            }
            .padding(30)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color(UIColor.systemBackground))
            )
            .padding(40)
        }
    }
}

struct PendingResetView: View {
    let resetState: PendingResetState
    let onAcknowledge: () -> Void

    var body: some View {
        ZStack {
            Color.black.opacity(0.5)
                .ignoresSafeArea()

            VStack(spacing: 16) {
                Text(resetState.finalCount == 31 ? "31!" : "Go!")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .foregroundColor(.blue)

                Text("Count: \(resetState.finalCount)")
                    .font(.title2)
                    .fontWeight(.semibold)

                if resetState.scoreAwarded > 0 {
                    Text("+\(resetState.scoreAwarded)")
                        .font(.title)
                        .fontWeight(.bold)
                        .foregroundColor(.green)
                        .padding()
                        .background(
                            RoundedRectangle(cornerRadius: 8)
                                .fill(Color.green.opacity(0.2))
                        )
                }

                if !resetState.pile.isEmpty {
                    Text("Cards Played:")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: -20) {
                            ForEach(Array(resetState.pile.enumerated()), id: \.offset) { _, card in
                                CardView(card: card, size: .medium)
                            }
                        }
                    }
                    .frame(height: 100)
                }

                Button("Next Round") {
                    onAcknowledge()
                }
                .buttonStyle(PrimaryButtonStyle())
            }
            .padding(24)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color(UIColor.systemBackground))
            )
            .padding(40)
        }
    }
}

// MARK: - Button Styles

struct PrimaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .foregroundColor(.white)
            .padding()
            .frame(minWidth: 200)
            .background(
                RoundedRectangle(cornerRadius: 10)
                    .fill(Color.blue)
            )
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
    }
}

#Preview {
    ContentView()
}

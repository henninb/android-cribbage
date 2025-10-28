import SwiftUI
import shared

struct ContentView: View {
    @StateObject private var viewModel = GameViewModel()

    var body: some View {
        ZStack {
            // Background
            Color(red: 0.1, green: 0.3, blue: 0.2)
                .ignoresSafeArea()

            VStack(spacing: 16) {
                // Score display
                HStack(spacing: 40) {
                    ScoreView(
                        label: "You",
                        score: Int(viewModel.gameState.playerScore),
                        isDealer: viewModel.gameState.isPlayerDealer
                    )

                    ScoreView(
                        label: "Opponent",
                        score: Int(viewModel.gameState.opponentScore),
                        isDealer: !viewModel.gameState.isPlayerDealer
                    )
                }
                .padding(.top)

                // Game status
                Text(viewModel.gameState.gameStatus)
                    .font(.caption)
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
                    .frame(height: 60)
                    .padding(.horizontal)

                Spacer()

                // Opponent's hand
                HandView(
                    cards: viewModel.gameState.opponentHand,
                    playedIndices: viewModel.gameState.opponentCardsPlayed,
                    selectedIndices: [],
                    isRevealed: false,
                    label: "Opponent's Hand"
                )

                // Pegging pile (if in pegging phase)
                if viewModel.gameState.isPeggingPhase {
                    PeggingPileView(
                        cards: viewModel.gameState.peggingPile,
                        count: Int(viewModel.gameState.peggingCount)
                    )
                }

                // Starter card
                if let starter = viewModel.gameState.starterCard {
                    VStack {
                        Text("Starter")
                            .font(.caption)
                            .foregroundColor(.white)
                        CardView(card: starter, size: .medium)
                    }
                }

                // Crib (if visible)
                if !viewModel.gameState.cribHand.isEmpty {
                    VStack {
                        Text("Crib (\(viewModel.gameState.isPlayerDealer ? "Yours" : "Opponent's"))")
                            .font(.caption)
                            .foregroundColor(.white)
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: -30) {
                                ForEach(Array(viewModel.gameState.cribHand.enumerated()), id: \.offset) { _, card in
                                    CardView(
                                        card: card,
                                        isRevealed: viewModel.gameState.isInHandCountingPhase,
                                        size: .small
                                    )
                                }
                            }
                        }
                        .frame(height: 100)
                    }
                }

                Spacer()

                // Player's hand
                HandView(
                    cards: viewModel.gameState.playerHand,
                    playedIndices: viewModel.gameState.playerCardsPlayed,
                    selectedIndices: viewModel.gameState.selectedCards,
                    isRevealed: true,
                    label: "Your Hand",
                    onCardTap: { index in
                        if viewModel.gameState.currentPhase == .cribSelection {
                            viewModel.toggleCardSelection(index: index)
                        } else if viewModel.gameState.currentPhase == .pegging &&
                                  viewModel.gameState.isPlayerTurn {
                            _ = viewModel.playCard(cardIndex: index, isPlayer: true)
                        }
                    }
                )

                // Action buttons
                ActionButtonsView(viewModel: viewModel)
                    .padding(.bottom)
            }

            // Winner modal
            if viewModel.gameState.showWinnerModal, let data = viewModel.gameState.winnerModalData {
                WinnerModalView(data: data) {
                    viewModel.dismissWinnerModal()
                }
            }

            // Pending reset acknowledgment
            if let pendingReset = viewModel.gameState.pendingReset {
                PendingResetView(resetState: pendingReset) {
                    viewModel.acknowledgePendingReset()
                }
            }
        }
    }
}

// MARK: - Supporting Views

struct ScoreView: View {
    let label: String
    let score: Int
    let isDealer: Bool

    var body: some View {
        VStack(spacing: 4) {
            Text(label)
                .font(.caption)
                .foregroundColor(.white)
            if isDealer {
                Text("(Dealer)")
                    .font(.caption2)
                    .foregroundColor(.yellow)
            }
            Text("\(score)")
                .font(.system(size: 36, weight: .bold))
                .foregroundColor(.white)
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.black.opacity(0.3))
        )
    }
}

struct HandView: View {
    let cards: [Card]
    let playedIndices: Set<KotlinInt>
    let selectedIndices: Set<KotlinInt>
    let isRevealed: Bool
    let label: String
    var onCardTap: ((Int) -> Void)? = nil

    var body: some View {
        VStack {
            Text(label)
                .font(.caption)
                .foregroundColor(.white)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: -30) {
                    ForEach(Array(cards.enumerated()), id: \.offset) { index, card in
                        CardView(
                            card: card,
                            isSelected: selectedIndices.contains(KotlinInt(value: Int32(index))),
                            isPlayed: playedIndices.contains(KotlinInt(value: Int32(index))),
                            isRevealed: isRevealed,
                            size: .medium,
                            onTap: onCardTap != nil ? {
                                onCardTap?(index)
                            } : nil
                        )
                    }
                }
                .padding(.horizontal)
            }
            .frame(height: 140)
        }
    }
}

struct PeggingPileView: View {
    let cards: [Card]
    let count: Int

    var body: some View {
        VStack {
            Text("Pegging Pile")
                .font(.caption)
                .foregroundColor(.white)

            HStack {
                Text("Count: \(count)")
                    .font(.headline)
                    .foregroundColor(.white)
                    .padding(8)
                    .background(
                        RoundedRectangle(cornerRadius: 8)
                            .fill(count == 31 ? Color.green : Color.blue.opacity(0.6))
                    )
            }

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 4) {
                    ForEach(Array(cards.enumerated()), id: \.offset) { _, card in
                        CardView(card: card, size: .small)
                    }
                }
                .padding(.horizontal)
            }
            .frame(height: 100)
        }
    }
}

struct ActionButtonsView: View {
    @ObservedObject var viewModel: GameViewModel

    var body: some View {
        VStack(spacing: 12) {
            if !viewModel.gameState.gameStarted {
                Button("Start New Game") {
                    viewModel.startNewGame()
                }
                .buttonStyle(PrimaryButtonStyle())
            } else if viewModel.gameState.currentPhase == .dealing {
                Button("Deal Cards") {
                    viewModel.dealCards()
                }
                .buttonStyle(PrimaryButtonStyle())
            } else if viewModel.gameState.currentPhase == .cribSelection {
                Button("Confirm Crib Selection") {
                    viewModel.confirmCribSelection()
                }
                .buttonStyle(PrimaryButtonStyle())
                .disabled(viewModel.gameState.selectedCards.count != 2)
            } else if viewModel.gameState.currentPhase == .pegging {
                if viewModel.gameState.isPlayerTurn {
                    Button("Go") {
                        viewModel.handleGo()
                    }
                    .buttonStyle(SecondaryButtonStyle())
                }
            } else if viewModel.gameState.currentPhase == .handCounting {
                if !viewModel.gameState.isInHandCountingPhase {
                    Button("Start Hand Counting") {
                        viewModel.startHandCounting()
                    }
                    .buttonStyle(PrimaryButtonStyle())
                } else {
                    Button("Next") {
                        viewModel.proceedToNextCountingPhase()
                    }
                    .buttonStyle(PrimaryButtonStyle())
                }
            }

            // Stats display
            if viewModel.gameState.gamesWon > 0 || viewModel.gameState.gamesLost > 0 {
                HStack(spacing: 20) {
                    Text("Wins: \(viewModel.gameState.gamesWon)")
                    Text("Losses: \(viewModel.gameState.gamesLost)")
                }
                .font(.caption)
                .foregroundColor(.white)
            }
        }
    }
}

struct WinnerModalView: View {
    let data: WinnerModalData
    let onDismiss: () -> Void

    var body: some View {
        ZStack {
            Color.black.opacity(0.7)
                .ignoresSafeArea()

            VStack(spacing: 20) {
                Text(data.playerWon ? "You Win!" : "Opponent Wins!")
                    .font(.largeTitle)
                    .fontWeight(.bold)

                Text("Final Score: \(data.playerScore) - \(data.opponentScore)")
                    .font(.headline)

                if data.wasSkunk {
                    Text("Skunk!")
                        .font(.title2)
                        .foregroundColor(.orange)
                }

                VStack(spacing: 8) {
                    Text("Games Won: \(data.gamesWon)")
                    Text("Games Lost: \(data.gamesLost)")
                    if data.skunksFor > 0 || data.skunksAgainst > 0 {
                        Text("Skunks: \(data.skunksFor) for, \(data.skunksAgainst) against")
                    }
                }
                .font(.subheadline)

                Button("OK") {
                    onDismiss()
                }
                .buttonStyle(PrimaryButtonStyle())
            }
            .padding(30)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color.white)
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
                Text(resetState.message)
                    .font(.title)
                    .fontWeight(.bold)

                Text("Count: \(resetState.finalCount)")
                    .font(.headline)

                if resetState.scoreAwarded > 0 {
                    Text("Points: \(resetState.scoreAwarded)")
                        .font(.headline)
                        .foregroundColor(.green)
                }

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 4) {
                        ForEach(Array(resetState.pile.enumerated()), id: \.offset) { _, card in
                            CardView(card: card, size: .small)
                        }
                    }
                }
                .frame(height: 100)

                Button("Continue") {
                    onAcknowledge()
                }
                .buttonStyle(PrimaryButtonStyle())
            }
            .padding(24)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color.white)
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

struct SecondaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .foregroundColor(.white)
            .padding()
            .frame(minWidth: 150)
            .background(
                RoundedRectangle(cornerRadius: 10)
                    .fill(Color.orange)
            )
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
    }
}

#Preview {
    ContentView()
}

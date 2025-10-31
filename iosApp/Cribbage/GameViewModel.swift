import Foundation
import Combine
import shared

/**
 * ViewModel that bridges the shared GameEngine with SwiftUI
 * Observes Kotlin StateFlow and publishes updates to SwiftUI
 */
@MainActor
class GameViewModel: ObservableObject {
    @Published var gameState: GameState

    private let gameEngine: GameEngine
    private var stateCollectionTask: Task<Void, Never>?

    init() {
        let persistence = IOSGamePersistence()
        self.gameEngine = GameEngine(persistence: persistence)
        self.gameState = gameEngine.state.value as! GameState

        // Start collecting state updates from Kotlin StateFlow
        startObservingState()
    }

    deinit {
        stateCollectionTask?.cancel()
    }

    private func startObservingState() {
        stateCollectionTask = Task { [weak self] in
            guard let self = self else { return }

            // Poll StateFlow value periodically (simpler approach)
            // In production, you'd use proper Flow collection
            while !Task.isCancelled {
                let currentState = self.gameEngine.state.value as! GameState
                await MainActor.run {
                    self.gameState = currentState
                }
                try? await Task.sleep(nanoseconds: 100_000_000) // 0.1 seconds
            }
        }
    }

    // MARK: - Game Actions

    func startNewGame() {
        gameEngine.startNewGame()

        // Automatically cut for dealer by generating two random cards
        let deck = CardKt.createDeck().shuffled()
        if deck.count >= 2 {
            let playerCutCard = deck[0]
            let opponentCutCard = deck[1]
            gameEngine.cutForDealer(playerCutCard: playerCutCard, opponentCutCard: opponentCutCard)
        }
    }

    func cutForDealer(playerCard: Card, opponentCard: Card) {
        gameEngine.cutForDealer(playerCutCard: playerCard, opponentCutCard: opponentCard)
    }

    func dealCards() {
        gameEngine.dealCards()
    }

    func toggleCardSelection(index: Int) {
        gameEngine.toggleCardSelection(index: Int32(index))
    }

    func confirmCribSelection() {
        gameEngine.confirmCribSelection()
    }

    func playCard(cardIndex: Int, isPlayer: Bool) -> Bool {
        return gameEngine.playCard(cardIndex: Int32(cardIndex), isPlayer: isPlayer) as! Bool
    }

    func handleGo() {
        gameEngine.handleGo()
    }

    func acknowledgePendingReset() {
        gameEngine.acknowledgePendingReset()
    }

    func startHandCounting() {
        gameEngine.startHandCounting()
    }

    func proceedToNextCountingPhase() {
        gameEngine.proceedToNextCountingPhase()
    }

    func dismissWinnerModal() {
        gameEngine.dismissWinnerModal()
    }

    func setOpponentActionInProgress(_ inProgress: Bool) {
        gameEngine.setOpponentActionInProgress(inProgress: inProgress)
    }
}


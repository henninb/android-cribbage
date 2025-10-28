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
        self.gameState = gameEngine.state.value

        // Start collecting state updates from Kotlin StateFlow
        startObservingState()
    }

    deinit {
        stateCollectionTask?.cancel()
    }

    private func startObservingState() {
        stateCollectionTask = Task { [weak self] in
            guard let self = self else { return }

            // Collect from Kotlin StateFlow
            let stream = asyncStream(for: self.gameEngine.state)

            for await state in stream {
                self.gameState = state
            }
        }
    }

    // MARK: - Game Actions

    func startNewGame() {
        gameEngine.startNewGame()
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
        return gameEngine.playCard(cardIndex: Int32(cardIndex), isPlayer: isPlayer).boolValue
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

/**
 * Helper to convert Kotlin StateFlow to Swift AsyncSequence
 */
func asyncStream<T>(for stateFlow: StateFlow) -> AsyncStream<T> {
    AsyncStream { continuation in
        let job = stateFlow.subscribe(
            onCollect: { item in
                if let value = item as? T {
                    continuation.yield(value)
                }
                return nil
            }
        )

        continuation.onTermination = { _ in
            job.cancel(cause: nil)
        }
    }
}

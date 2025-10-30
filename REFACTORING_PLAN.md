# CribbageMainScreen.kt Refactoring Plan

## Overview

This document outlines the plan to refactor `CribbageMainScreen.kt` (1593 lines) into a maintainable, testable architecture following Android best practices with MVVM + Domain State Managers.

## Current Problems

### CribbageMainScreen.kt Issues

**File Size**: 1593 lines (target: 300-400 lines)

**Current Architecture Problems**:
- **54+ state variables** managed directly in composable function
- **Complex pegging logic** (~600 lines) using mutable state refs for mutual recursion
- **Hand counting coroutines** launched directly in composable
- **SharedPreferences I/O** in LaunchedEffect (blocks main thread)
- **Game lifecycle logic** scattered throughout
- **Zero testability** - all business logic trapped in @Composable
- **Difficult to maintain** - state mutations spread across 1000+ lines
- **No separation of concerns** - UI, business logic, data persistence all mixed

### Specific Code Smells

```kotlin
// Lines 54-144: 90+ mutable state variables in composable
var gameStarted by remember { mutableStateOf(false) }
var playerScore by remember { mutableIntStateOf(0) }
var opponentScore by remember { mutableIntStateOf(0) }
// ... 50+ more state variables

// Lines 244-248: Mutable state refs for mutual recursion
val autoHandleGoRef = remember { mutableStateOf({}) }
val playSelectedCardRef = remember { mutableStateOf({}) }
val handleNextRoundRef = remember { mutableStateOf({}) }

// Lines 74-94: SharedPreferences I/O in LaunchedEffect
LaunchedEffect(Unit) {
    val prefs = context.getSharedPreferences("cribbage_prefs", Context.MODE_PRIVATE)
    gamesWon = prefs.getInt("gamesWon", 0)
    // ...
}

// Lines 378-992: 600+ lines of pegging logic in lambdas
autoHandleGoRef.value = letUnit@{
    val mgr = peggingManager ?: return@letUnit
    // ... 100+ lines of complex logic
}
```

## Proposed Architecture

### Best Practice: MVVM + Domain-Specific State Managers

Following Google's recommended Android architecture:

```
UI Layer (Composables) → ViewModel → Domain Layer (State Managers) → Data Layer (Repository)
```

**Benefits**:
1. **Testability**: ViewModel and State Managers can be unit tested without UI
2. **Separation of Concerns**: Each layer has clear responsibility
3. **Configuration Change Survival**: ViewModel survives rotation/theme changes
4. **Reusability**: State managers can be shared across ViewModels if needed
5. **Maintainability**: Clear code organization, easier to locate bugs
6. **Scalability**: Easy to add new features without bloating existing files

## New Package Structure

```
com.brianhenning.cribbage/
├── ui/
│   ├── screens/
│   │   └── CribbageMainScreen.kt (300-400 lines - pure UI only)
│   ├── viewmodels/
│   │   └── CribbageGameViewModel.kt (coordinates game flow)
│   └── composables/ (existing, no changes)
│
├── game/
│   ├── state/
│   │   ├── GameState.kt (immutable data classes)
│   │   ├── PeggingStateManager.kt (pegging phase logic)
│   │   ├── HandCountingStateManager.kt (counting phase logic)
│   │   ├── GameLifecycleManager.kt (start/end/deal/round logic)
│   │   └── ScoreManager.kt (score tracking and game over)
│   │
│   ├── logic/ (existing - no changes needed)
│   │   ├── CribbageScorer.kt
│   │   ├── PeggingScorer.kt
│   │   ├── PeggingRoundManager.kt
│   │   ├── OpponentAI.kt
│   │   └── ...
│   │
│   └── repository/
│       └── PreferencesRepository.kt (SharedPreferences abstraction)
```

## Component Responsibilities

### 1. GameState.kt (Data Classes)

**Purpose**: Immutable data classes representing all game state

**Contents**:
```kotlin
data class GameUiState(
    val gameStarted: Boolean = false,
    val currentPhase: GamePhase = GamePhase.SETUP,
    val playerScore: Int = 0,
    val opponentScore: Int = 0,
    val isPlayerDealer: Boolean = false,
    val playerHand: List<Card> = emptyList(),
    val opponentHand: List<Card> = emptyList(),
    val cribHand: List<Card> = emptyList(),
    val starterCard: Card? = null,
    val cutPlayerCard: Card? = null,
    val cutOpponentCard: Card? = null,
    val showCutForDealer: Boolean = false,
    val gameStatus: String = "",
    val gameOver: Boolean = false,

    // Button states
    val dealButtonEnabled: Boolean = false,
    val selectCribButtonEnabled: Boolean = false,
    val playCardButtonEnabled: Boolean = false,
    val showHandCountingButton: Boolean = false,
    val showGoButton: Boolean = false,

    // Phase-specific state
    val peggingState: PeggingState? = null,
    val handCountingState: HandCountingState? = null,
    val selectedCards: Set<Int> = emptySet(),

    // Modals
    val showWinnerModal: Boolean = false,
    val winnerModalData: WinnerModalData? = null,
    val showCutCardDisplay: Boolean = false,

    // Animations
    val playerScoreAnimation: ScoreAnimationState? = null,
    val opponentScoreAnimation: ScoreAnimationState? = null,
    val show31Banner: Boolean = false,

    // Match stats
    val matchStats: MatchStats = MatchStats()
)

data class PeggingState(
    val isPeggingPhase: Boolean = false,
    val isPlayerTurn: Boolean = false,
    val peggingCount: Int = 0,
    val peggingPile: List<Card> = emptyList(),
    val peggingDisplayPile: List<Card> = emptyList(),
    val playerCardsPlayed: Set<Int> = emptySet(),
    val opponentCardsPlayed: Set<Int> = emptySet(),
    val consecutiveGoes: Int = 0,
    val lastPlayerWhoPlayed: String? = null,
    val pendingReset: PendingResetState? = null,
    val isOpponentActionInProgress: Boolean = false
)

data class HandCountingState(
    val isInHandCountingPhase: Boolean = false,
    val countingPhase: CountingPhase = CountingPhase.NONE,
    val handScores: HandScores = HandScores(),
    val waitingForDialogDismissal: Boolean = false
)

data class MatchStats(
    val gamesWon: Int = 0,
    val gamesLost: Int = 0,
    val skunksFor: Int = 0,
    val skunksAgainst: Int = 0,
    val doubleSkunksFor: Int = 0,
    val doubleSkunksAgainst: Int = 0
)
```

### 2. PeggingStateManager.kt (~300 lines)

**Purpose**: Manages all pegging phase logic and state transitions

**Extracted From**: Lines 378-992 of CribbageMainScreen.kt

**Responsibilities**:
- Turn management (player/opponent)
- Card play validation
- Pegging count tracking
- Go handling (automatic and manual)
- Sub-round reset detection
- PeggingRoundManager coordination
- Opponent AI card selection
- Score point calculations (delegates to PeggingScorer)

**Key Methods**:
```kotlin
class PeggingStateManager(
    private val scoreManager: ScoreManager,
    private val opponentAI: OpponentAI
) {
    fun startPegging(
        playerHand: List<Card>,
        opponentHand: List<Card>,
        isPlayerDealer: Boolean,
        starterCard: Card?
    ): PeggingState

    fun playCard(
        currentState: PeggingState,
        cardIndex: Int,
        playerHand: List<Card>
    ): PeggingResult

    fun handleGo(
        currentState: PeggingState,
        playerHand: List<Card>,
        opponentHand: List<Card>
    ): PeggingResult

    fun handleOpponentTurn(
        currentState: PeggingState,
        opponentHand: List<Card>
    ): PeggingResult

    fun acknowledgeReset(
        currentState: PeggingState,
        playerHand: List<Card>,
        opponentHand: List<Card>
    ): PeggingResult

    private fun checkPeggingComplete(state: PeggingState): Boolean
}

sealed class PeggingResult {
    data class Success(
        val newState: PeggingState,
        val pointsScored: PeggingPoints? = null,
        val statusMessage: String
    ) : PeggingResult()

    data class OpponentTurnNext(
        val newState: PeggingState,
        val delayMs: Long = 500
    ) : PeggingResult()

    data class PeggingComplete(
        val finalState: PeggingState
    ) : PeggingResult()

    data class Error(val message: String) : PeggingResult()
}
```

### 3. HandCountingStateManager.kt (~150 lines)

**Purpose**: Orchestrates hand counting phase with sequential dialogs

**Extracted From**: Lines 1038-1147 of CribbageMainScreen.kt

**Responsibilities**:
- Hand counting sequence coordination
- Dialog state management
- Score application (delegates to ScoreManager)
- Non-dealer → Dealer → Crib counting flow
- Waiting for user acknowledgment
- Round completion detection

**Key Methods**:
```kotlin
class HandCountingStateManager(
    private val scoreManager: ScoreManager,
    private val scorer: CribbageScorer
) {
    suspend fun startHandCounting(
        playerHand: List<Card>,
        opponentHand: List<Card>,
        cribHand: List<Card>,
        starterCard: Card,
        isPlayerDealer: Boolean
    ): HandCountingResult

    fun dismissDialog(
        currentState: HandCountingState,
        currentPhase: CountingPhase
    ): HandCountingState

    fun getCurrentHandToDisplay(
        state: HandCountingState,
        playerHand: List<Card>,
        opponentHand: List<Card>,
        cribHand: List<Card>
    ): List<Card>
}

sealed class HandCountingResult {
    data class ShowDialog(
        val state: HandCountingState,
        val phase: CountingPhase,
        val breakdown: DetailedScoreBreakdown
    ) : HandCountingResult()

    data class Complete(
        val totalPlayerPoints: Int,
        val totalOpponentPoints: Int
    ) : HandCountingResult()
}
```

### 4. GameLifecycleManager.kt (~200 lines)

**Purpose**: Handles game initialization, rounds, and transitions

**Extracted From**: Lines 628-787 of CribbageMainScreen.kt

**Responsibilities**:
- Game start/end
- Dealer determination (cut for dealer)
- Card dealing
- Crib selection (player + opponent AI)
- Starter card drawing
- Round transitions (toggle dealer)
- Phase transitions

**Key Methods**:
```kotlin
class GameLifecycleManager(
    private val preferencesRepository: PreferencesRepository,
    private val opponentAI: OpponentAI
) {
    fun startNewGame(): GameLifecycleResult

    fun dealCards(isPlayerDealer: Boolean): DealResult

    fun selectCardsForCrib(
        playerHand: List<Card>,
        opponentHand: List<Card>,
        selectedIndices: Set<Int>,
        isPlayerDealer: Boolean,
        remainingDeck: List<Card>
    ): CribSelectionResult

    fun startNextRound(isPlayerDealer: Boolean): RoundTransitionResult

    fun endGame(): Unit
}

sealed class GameLifecycleResult {
    data class NewGameStarted(
        val isPlayerDealer: Boolean,
        val cutPlayerCard: Card?,
        val cutOpponentCard: Card?,
        val showCutForDealer: Boolean,
        val statusMessage: String
    ) : GameLifecycleResult()
}

data class DealResult(
    val playerHand: List<Card>,
    val opponentHand: List<Card>,
    val remainingDeck: List<Card>,
    val statusMessage: String
)

data class CribSelectionResult(
    val updatedPlayerHand: List<Card>,
    val updatedOpponentHand: List<Card>,
    val cribHand: List<Card>,
    val starterCard: Card,
    val remainingDeck: List<Card>,
    val statusMessage: String
)
```

### 5. ScoreManager.kt (~150 lines)

**Purpose**: Centralized score tracking, game over detection, and match stats

**Extracted From**: Lines 159-239, 256-296 of CribbageMainScreen.kt

**Responsibilities**:
- Score updates (player/opponent)
- Game over detection (> 120 points)
- Skunk detection (< 91 = single, < 61 = double)
- Match statistics tracking
- Score animation state management
- Winner determination

**Key Methods**:
```kotlin
class ScoreManager(
    private val preferencesRepository: PreferencesRepository
) {
    fun addScore(
        currentPlayerScore: Int,
        currentOpponentScore: Int,
        pointsToAdd: Int,
        isForPlayer: Boolean,
        matchStats: MatchStats
    ): ScoreResult

    fun checkGameOver(
        playerScore: Int,
        opponentScore: Int,
        matchStats: MatchStats,
        isPlayerDealer: Boolean
    ): GameOverResult?

    fun createScoreAnimation(
        points: Int,
        isPlayer: Boolean
    ): ScoreAnimationState
}

sealed class ScoreResult {
    data class ScoreUpdated(
        val newPlayerScore: Int,
        val newOpponentScore: Int,
        val animation: ScoreAnimationState?
    ) : ScoreResult()

    data class GameOver(
        val winnerModalData: WinnerModalData,
        val matchStats: MatchStats
    ) : ScoreResult()
}
```

### 6. PreferencesRepository.kt (~100 lines)

**Purpose**: Abstraction over SharedPreferences for testability

**Extracted From**: Lines 74-94, 193-203, 688-721 of CribbageMainScreen.kt

**Responsibilities**:
- Load/save match statistics
- Load/save cut cards
- Load/save next dealer preference
- Clear preferences on demand

**Key Methods**:
```kotlin
class PreferencesRepository(private val context: Context) {
    fun getMatchStats(): MatchStats
    fun saveMatchStats(stats: MatchStats)

    fun getCutCards(): Pair<Card?, Card?>
    fun saveCutCards(playerCard: Card, opponentCard: Card)

    fun getNextDealerIsPlayer(): Boolean?
    fun setNextDealerIsPlayer(isPlayer: Boolean)
    fun clearNextDealer()

    fun clearAll()
}
```

### 7. CribbageGameViewModel.kt (~400 lines)

**Purpose**: Coordinates all state managers and exposes UI state

**Responsibilities**:
- Owns all state managers
- Exposes `StateFlow<GameUiState>` to UI
- Handles user actions via methods
- Manages coroutine scope for async operations
- Coordinates state transitions between managers

**Key Structure**:
```kotlin
class CribbageGameViewModel(
    private val lifecycleManager: GameLifecycleManager,
    private val peggingManager: PeggingStateManager,
    private val handCountingManager: HandCountingStateManager,
    private val scoreManager: ScoreManager,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // User Actions
    fun startNewGame()
    fun endGame()
    fun dealCards()
    fun selectCardsForCrib()
    fun toggleCardSelection(cardIndex: Int)
    fun playCard()
    fun handleGo()
    fun startHandCounting()
    fun dismissHandCountingDialog()
    fun acknowledgeReset()
    fun dismissWinnerModal()
    fun dismissCutCardDisplay()

    // Internal coordination
    private suspend fun handleOpponentTurn()
    private fun updateState(transform: (GameUiState) -> GameUiState)
    private fun checkGameOver()
}
```

### 8. CribbageMainScreen.kt (Refactored - ~300 lines)

**Purpose**: Pure UI rendering, no business logic

**Structure**:
```kotlin
@Composable
fun CribbageMainScreen(
    viewModel: CribbageGameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Zone 1: Compact Score Header
        CompactScoreHeader(
            playerScore = uiState.playerScore,
            opponentScore = uiState.opponentScore,
            // ...
            onTripleTap = { viewModel.showDebugDialog() }
        )

        // Zone 2: Dynamic Game Area
        Box(modifier = Modifier.weight(1f)) {
            GameAreaContent(
                currentPhase = uiState.currentPhase,
                // ... pass all display state
                onCardClick = { viewModel.toggleCardSelection(it) }
            )

            // Overlays (modals, dialogs)
            if (uiState.handCountingState?.isInHandCountingPhase == true) {
                HandCountingDisplay(/* ... */)
            }
            if (uiState.showWinnerModal) {
                WinnerModal(/* ... */)
            }
        }

        // Zone 3: Action Bar
        ActionBar(
            currentPhase = uiState.currentPhase,
            // ... button states
            onStartGame = { viewModel.startNewGame() },
            onDeal = { viewModel.dealCards() },
            // ...
        )

        // Zone 4: Cribbage Board
        CribbageBoard(
            playerScore = uiState.playerScore,
            opponentScore = uiState.opponentScore
        )
    }
}
```

## Task Breakdown

### Task 1: Create Package Structure
**Estimated Time**: 5 minutes

Create new packages:
- `com.brianhenning.cribbage.ui.viewmodels`
- `com.brianhenning.cribbage.game.state`
- `com.brianhenning.cribbage.game.repository`

### Task 2: Create GameState.kt
**Estimated Time**: 30 minutes

Create immutable data classes for:
- `GameUiState` (main state)
- `PeggingState`
- `HandCountingState`
- `MatchStats`
- Supporting types

### Task 3: Create CribbageGameViewModel.kt Skeleton
**Estimated Time**: 20 minutes

Create ViewModel with:
- StateFlow for UI state
- Empty method stubs for all user actions
- Constructor for state manager dependencies

### Task 4: Extract PeggingStateManager.kt
**Estimated Time**: 2-3 hours

Most complex extraction:
- Extract pegging logic from lines 378-992
- Convert mutable state refs to pure functions
- Return sealed class results instead of mutating state
- Add unit tests for pegging scenarios

### Task 5: Extract HandCountingStateManager.kt
**Estimated Time**: 1 hour

- Extract hand counting from lines 1038-1147
- Convert coroutine logic to suspended functions
- Add unit tests for counting sequence

### Task 6: Extract GameLifecycleManager.kt
**Estimated Time**: 1.5 hours

- Extract game start/end logic (lines 628-731)
- Extract deal cards (lines 733-754)
- Extract crib selection (lines 756-787)
- Add unit tests

### Task 7: Extract ScoreManager.kt
**Estimated Time**: 1 hour

- Extract score updates (lines 256-296)
- Extract game over detection (lines 159-239)
- Add unit tests for skunk detection

### Task 8: Create PreferencesRepository.kt
**Estimated Time**: 45 minutes

- Abstract SharedPreferences operations
- Create fake implementation for tests
- Add unit tests

### Task 9: Wire Up ViewModel
**Estimated Time**: 2 hours

- Implement all user action methods
- Coordinate between state managers
- Handle state transitions
- Add ViewModel unit tests

### Task 10: Refactor CribbageMainScreen.kt
**Estimated Time**: 2 hours

- Replace all state variables with ViewModel state collection
- Replace all lambdas with ViewModel method calls
- Remove all business logic
- Keep only UI rendering

### Task 11: Test Refactored Implementation
**Estimated Time**: 1-2 hours

- Manual testing of full game flow
- Verify all features work
- Test edge cases
- Check animations and timing

### Task 12: Clean Up and Verify
**Estimated Time**: 30 minutes

- Remove unused code
- Update imports
- Verify no regressions
- Update documentation

**Total Estimated Time**: 12-15 hours

## Migration Strategy Options

### Option A: Big Bang (Extract All, Then Integrate)
**Approach**: Extract all managers → Wire ViewModel → Refactor UI

**Pros**:
- Clean separation between old and new
- Can keep old code as backup
- Each extraction is independent

**Cons**:
- Won't see working results until very end
- Higher risk of integration issues

### Option B: Incremental (One Manager at a Time)
**Approach**: Extract one manager → Partial ViewModel → Partial UI per iteration

**Pros**:
- See progress faster
- Lower risk per iteration
- Can test each piece immediately

**Cons**:
- More complex migrations
- Temporary hybrid state

### Recommended: Option A
Cleaner architecture, easier to test, better for long-term maintenance.

## Decision Points

### 1. Testing Strategy
**Question**: Create unit tests for each manager during extraction?

**Recommendation**: YES
- State managers are pure business logic (easy to test)
- Tests ensure refactoring doesn't break behavior
- Tests serve as documentation

### 2. Dependency Injection
**Question**: Use Hilt/Koin or manual instantiation?

**Options**:
- **Manual**: Simple, no new dependencies, good for now
- **Hilt**: Industry standard, but adds complexity
- **Koin**: Lightweight, Kotlin-friendly

**Recommendation**: Start manual, migrate to Hilt later if needed

### 3. State Persistence
**Question**: Enhance PreferencesRepository to save in-progress game state?

**Benefits**: Survive app kill, restore game state

**Recommendation**: Add in future iteration (out of scope for initial refactor)

### 4. Backward Compatibility
**Question**: Keep old code in backup branch?

**Recommendation**: YES
- Create `feature/architecture-refactor` branch
- Keep main branch stable
- Merge after thorough testing

## Success Criteria

### Code Quality Metrics
- [ ] CribbageMainScreen.kt reduced to < 400 lines
- [ ] All business logic extracted to testable classes
- [ ] State managers have > 80% unit test coverage
- [ ] Zero mutable state refs in UI layer
- [ ] Zero LaunchedEffect with business logic

### Functional Requirements
- [ ] All existing features work identically
- [ ] No visual regressions
- [ ] Animation timing unchanged
- [ ] Game logic behavior identical
- [ ] SharedPreferences behavior unchanged

### Architectural Goals
- [ ] Clear separation: UI → ViewModel → Domain → Data
- [ ] Single responsibility per class
- [ ] Testable without Android framework
- [ ] Configuration change resilience (rotation works)
- [ ] No memory leaks

## Next Steps

1. **Review and Approve Plan**: Confirm approach and answer decision points
2. **Create Feature Branch**: `git checkout -b feature/architecture-refactor`
3. **Start Task 1**: Create package structure
4. **Iterate Through Tasks**: Complete each task with testing
5. **Integration Testing**: Full game flow validation
6. **Code Review**: Review extracted code quality
7. **Merge to Main**: After thorough testing

## References

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [ViewModel Overview](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [State and Jetpack Compose](https://developer.android.com/jetpack/compose/state)
- [Kotlin Flow Documentation](https://kotlinlang.org/docs/flow.html)

---

**Document Version**: 1.0
**Last Updated**: 2025-10-30
**Author**: Claude Code (AI Assistant)

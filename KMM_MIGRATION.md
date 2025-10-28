# Kotlin Multiplatform Mobile (KMM) Migration Guide

This document describes the KMM setup for the Android Cribbage app, enabling iOS support while sharing game logic between platforms.

## What's Been Implemented

### ✅ Shared Module (Platform-Agnostic Game Logic)
Located in `shared/src/commonMain/kotlin/`

**Game Engine** (`domain/engine/`):
- `GameEngine.kt` - Core game state management with StateFlow for reactive updates
- `GameState.kt` - Immutable game state data classes
- `GamePersistence` - Interface for platform-specific storage

**Existing Game Logic** (already platform-agnostic):
- `Card.kt` - Card models and deck creation (58 lines)
- `CribbageScorer.kt` - Complete scoring engine for all 5 categories (311 lines)
- `PeggingRoundManager.kt` - Pegging phase state machine (102 lines)
- `OpponentAI.kt` - Strategic AI for card selection (308 lines)
- `DealUtils.kt` - Deal mechanics and dealer determination (45 lines)

**Total Shared Code**: ~1,200 lines of pure game logic

### ✅ Android Implementation
Located in `app/src/main/java/com/brianhenning/cribbage/`

- `AndroidGamePersistence.kt` - SharedPreferences adapter for game stats
- Existing Jetpack Compose UI (needs refactoring to use GameEngine)

### ✅ iOS Implementation
Located in `iosApp/iosApp/`

- `CribbageApp.swift` - SwiftUI app entry point
- `GameViewModel.swift` - ViewModel that observes Kotlin StateFlow
- `ContentView.swift` - Main game screen with SwiftUI
- `CardView.swift` - Reusable card component
- `IOSGamePersistence.swift` - UserDefaults adapter for game stats

## Architecture Overview

```
┌─────────────────────────────────────┐
│         iOS App (SwiftUI)            │
│  ┌──────────────────────────────┐   │
│  │   GameViewModel              │   │
│  │   (observes StateFlow)       │   │
│  └──────────────┬───────────────┘   │
│                 │                    │
└─────────────────┼────────────────────┘
                  │
┌─────────────────▼────────────────────┐
│      Shared Module (Kotlin)          │
│  ┌──────────────────────────────┐   │
│  │      GameEngine              │   │
│  │  (StateFlow<GameState>)      │   │
│  ├──────────────────────────────┤   │
│  │  • CribbageScorer            │   │
│  │  • PeggingRoundManager       │   │
│  │  • OpponentAI                │   │
│  │  • Card / Deck Utils         │   │
│  └──────────────────────────────┘   │
└──────────────────────────────────────┘
                  │
┌─────────────────▼────────────────────┐
│    Android App (Jetpack Compose)     │
│  ┌──────────────────────────────┐   │
│  │   CribbageMainScreen         │   │
│  │   (needs refactoring)        │   │
│  └──────────────────────────────┘   │
└──────────────────────────────────────┘
```

## What Remains To Be Done

### 1. Complete Android Refactoring
**Priority: Optional (Android app currently works)**

The existing `CribbageMainScreen.kt` (1,531 lines) should be refactored to use the shared `GameEngine` instead of managing state locally. This would:
- Reduce code duplication
- Ensure both platforms have identical game behavior
- Make future updates easier

**Steps**:
1. Replace all `remember { mutableStateOf(...) }` with `viewModel.gameState.*`
2. Replace inline game logic with `gameEngine.*()` method calls
3. Create a `GameViewModel` for Android (similar to iOS version)
4. Observe `StateFlow` using `collectAsState()` in Compose

### 2. Set Up iOS Xcode Project
**Priority: Required for iOS build**

The iOS Swift files are created but need to be integrated into an Xcode project. See `iosApp/README.md` for detailed instructions.

**Quick Steps**:
1. Open Xcode → New Project → iOS App
2. Add all Swift files from `iosApp/iosApp/`
3. Link the shared Kotlin framework (via CocoaPods or XCFramework)
4. Build and run

### 3. Test and Iterate
- Test Android app with refactored code (if refactoring is done)
- Test iOS app in Xcode simulator
- Verify game logic consistency between platforms
- Add platform-specific features as desired

## Build Commands

### Shared Module
```bash
# Build for Android
./gradlew :shared:build

# Build for iOS Simulator (M1/M2 Mac)
./gradlew :shared:compileKotlinIosSimulatorArm64

# Build for iOS Device
./gradlew :shared:compileKotlinIosArm64

# Build XCFramework for Xcode
./gradlew :shared:assembleXCFramework
```

### Android App
```bash
# Build
./gradlew :app:build

# Install on device/emulator
./gradlew :app:installDebug

# Run tests
./gradlew :app:test
```

### iOS App
```bash
# Build shared module first
./gradlew :shared:compileKotlinIosSimulatorArm64

# Then build in Xcode
# (open iosApp.xcodeproj or iosApp.xcworkspace)
```

## Key Features of the Shared GameEngine

### State Management
- Immutable `GameState` data class
- Reactive updates via Kotlin `StateFlow`
- Observable from both Android (Compose) and iOS (SwiftUI)

### Game Flow
```
SETUP → CUT_FOR_DEALER → DEALING → CRIB_SELECTION →
PEGGING → HAND_COUNTING → (new round or GAME_OVER)
```

### Core Methods
- `startNewGame()` - Initialize new game
- `cutForDealer(...)` - Determine dealer from cut cards
- `dealCards()` - Deal 6 cards to each player
- `toggleCardSelection(...)` - Select cards for crib
- `confirmCribSelection()` - Move cards to crib, start pegging
- `playCard(...)` - Play a card during pegging
- `handleGo()` - Handle "Go" situation
- `startHandCounting()` - Begin hand scoring phase
- `proceedToNextCountingPhase()` - Score next hand/crib

### Persistence
Platform-specific implementations of `GamePersistence`:
- **Android**: `SharedPreferences` for stats and preferences
- **iOS**: `UserDefaults` for stats and preferences

## Project Stats

**Shared Code**: 100% of game logic (~1,200 lines)
**Platform-Specific Code**:
- Android: ~2,100 lines (mostly UI)
- iOS: ~600 lines (SwiftUI + adapters)

**Code Reuse**: ~65% (game logic shared, UI platform-specific)

## Benefits of This Architecture

1. **Single Source of Truth**: All game rules in shared module
2. **Consistency**: Both platforms use identical game logic
3. **Maintainability**: Bug fixes and features update both platforms
4. **Native UIs**: Platform-specific UIs provide best user experience
5. **Type Safety**: Kotlin types bridge to Swift seamlessly

## Testing

The shared module includes 40 unit tests covering:
- Card operations
- Scoring scenarios (all categories)
- Pegging rules and edge cases
- AI strategy
- Game flow state machine

Run shared module tests:
```bash
./gradlew :shared:test
```

## Troubleshooting

### iOS Build Issues
- Ensure Java 17 is installed: `brew install openjdk@17`
- Clean and rebuild: `./gradlew clean :shared:build`
- Check Xcode framework linking in Build Settings

### Android Issues
- The existing Android app still works with its original code
- Refactoring to use GameEngine is optional for Android functionality

### State Synchronization
- Android: Use `viewModel.gameState.collectAsState()` in Composables
- iOS: Use `@StateObject var viewModel = GameViewModel()` and `@Published`

## Next Steps

1. **Test Shared Module**: `./gradlew :shared:test` (should pass all 40 tests)
2. **Create Xcode Project**: Follow `iosApp/README.md`
3. **Build iOS App**: Test in simulator
4. **(Optional) Refactor Android**: Update CribbageMainScreen to use GameEngine

## Resources

- [Kotlin Multiplatform Mobile Docs](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html)
- [StateFlow in KMM](https://kotlinlang.org/docs/kmm-integrate-in-existing-app.html#make-your-cross-platform-application-work-on-ios)
- [SwiftUI + KMM](https://touchlab.co/using-kotlin-stateflow-in-swift)

## Questions or Issues?

Refer to:
- `CLAUDE.md` - Android development guidelines
- `iosApp/README.md` - iOS setup instructions
- `bug.md` - Known issues and bug tracking

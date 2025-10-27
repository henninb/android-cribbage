# Bug Report: Unable to Play Cards During Pegging Phase

## Issue Summary
Cards cannot be played during the pegging phase even when it shows as the player's turn and cards are available to play.

## Severity
**HIGH** - Breaks core game functionality

## Status
**OPEN**

## Affected Version
- App Version: 1.0.6
- Device: Google Pixel 8a (SDK 36)

---

## Bug Instance #1

### Symptoms
- Status shows "Your turn, choose a card to play"
- Play Card button is enabled
- Unable to click on any card to play
- UI state shows `Is Player Turn: true`
- PeggingManager shows `Manager Is Player Turn: PLAYER`

### Game State at Time of Bug

**Scores:**
- Player: 61
- Opponent: 48
- Dealer: Player

**Cards:**
- Starter: A♠
- Pegging Count: 2
- Pegging Pile: 2♦
- Player Hand: A♦, 4♦, 7♦, 7♣
- Player Cards Played: [0, 2, 3] (indices)
- Remaining Playable: Index 1 (4♦)
- Opponent Hand: 2♦, 2♠, 3♠, Q♠
- Opponent Cards Played: [0, 1, 2, 3] (all cards played)
- Crib: J♦, J♣, 4♠, J♠

**Debug State:**
```
Game Phase: PEGGING
Game Started: true
Game Over: false
Is Player Turn: true
Is Pegging Phase: true
Is In Hand Counting Phase: false

Card State:
- Selected Cards: []
- Player Cards Played: [0, 2, 3]
- Opponent Cards Played: [0, 1, 2, 3]
- Pegging Display Pile: 2♦

Button State:
- Deal Button Enabled: false
- Select Crib Button Enabled: false
- Play Card Button Enabled: true
- Show Hand Counting Button: false
- Show Go Button: false

Pegging Manager State:
- Manager Is Player Turn: PLAYER
- Manager Pegging Count: 2
- Manager Pegging Pile: 2♦
- Manager Consecutive Goes: 0
- Manager Last Player Who Played: OPPONENT
```

**Last Status:** "Opponent played 2♦" → "Your turn, choose a card to play"

**Match Record:** 1-1 (Skunks 0-0)

### Analysis
- Player should be able to play the 4♦ (count would be 2 + 4 = 6, well under 31)
- All opponent cards have been played
- Turn state appears correct (`isPlayerTurn: true`)
- But cards are not clickable

---

## Bug Instance #2

### Symptoms
- Status shows "Your turn, choose a card to play"
- Play Card button is enabled
- Unable to click on any card to play
- **CONTRADICTION**: UI state shows `Is Player Turn: false` but status says "Your turn"
- PeggingManager shows `Manager Is Player Turn: OPPONENT`

### Game State at Time of Bug

**Scores:**
- Player: 4
- Opponent: 0
- Dealer: Player

**Cards:**
- Starter: J♦
- Pegging Count: 11
- Pegging Pile: 6♣, 5♦
- Player Hand: A♥, 4♠, Q♣, Q♠
- Player Cards Played: [0, 1, 3] (indices)
- Remaining Playable: Index 2 (Q♣)
- Opponent Hand: 5♦, 6♥, 6♣, 10♣
- Opponent Cards Played: [0, 1, 2, 3] (all cards played)
- Crib: K♦, 10♥, 3♥, J♣

**Debug State:**
```
Game Phase: PEGGING
Game Started: true
Game Over: false
Is Player Turn: false  ⚠️ CONTRADICTION WITH STATUS MESSAGE
Is Pegging Phase: true
Is In Hand Counting Phase: false

Card State:
- Selected Cards: []
- Player Cards Played: [0, 1, 3]
- Opponent Cards Played: [0, 1, 2, 3]
- Pegging Display Pile: 6♣, 5♦

Button State:
- Deal Button Enabled: false
- Select Crib Button Enabled: false
- Play Card Button Enabled: true
- Show Hand Counting Button: false
- Show Go Button: false

Pegging Manager State:
- Manager Is Player Turn: OPPONENT  ⚠️ CONTRADICTION WITH STATUS MESSAGE
- Manager Pegging Count: 11
- Manager Pegging Pile: 6♣, 5♦
- Manager Consecutive Goes: 0
- Manager Last Player Who Played: PLAYER  ⚠️ CONTRADICTION - should be OPPONENT
```

**Last Status:** "Opponent played 5♦" → "Your turn, choose a card to play"

**Match Record:** 1-1 (Skunks 0-0)

### Analysis
- **Critical State Mismatch**: Status message says "Your turn" but internal state has `isPlayerTurn: false`
- PeggingManager thinks it's OPPONENT's turn
- Last player who played shows as PLAYER, but status says "Opponent played 5♦"
- Player's Q♣ cannot be played (11 + 10 = 21, legal move)
- This appears to be a state synchronization issue

---

## Root Cause Hypothesis

Based on the two instances:

### Theory 1: Turn State Desynchronization
The `isPlayerTurn` flag in the UI is not properly synchronized with the `PeggingRoundManager.isPlayerTurn` state. Instance #2 clearly shows:
- Status message indicates player's turn
- UI `isPlayerTurn: false`
- Manager `isPlayerTurn: OPPONENT`

This suggests the status message is generated from one source of truth, while the card clickability logic uses a different source.

### Theory 2: Missing State Update After Opponent Play
After the opponent plays a card, the turn should switch to the player, but the state update may not be propagating correctly to all parts of the UI:
1. Status message gets updated correctly
2. `PlayerHandCompact` component doesn't receive the updated `isPlayerTurn` prop
3. Cards remain unclickable because `isPlayerTurn && !playedCards.contains(index)` evaluates to false

### Theory 3: Event Handler Timing Issue
The opponent's card play may trigger multiple state updates in rapid succession, and the final state isn't correctly reflecting whose turn it is.

---

## Relevant Code Locations

### CribbageMainScreen.kt
- `isPlayerTurn` state variable (line ~73)
- `applyManagerStateToUi()` function that syncs manager state to UI
- `PlayerHandCompact` component call site with `isPlayerTurn` parameter

### ZoneComponents.kt
- `PlayerHandCompact` composable (receives `isPlayerTurn` parameter)
- Card clickability logic: `isClickable = isPlayerTurn && !playedCards.contains(index)`

### PeggingRoundManager.kt
- `isPlayerTurn: Player` property
- `onPlay()` function that switches turns
- Turn switching logic after plays

---

## Steps to Reproduce

**Unable to reliably reproduce**, but occurs during pegging phase when:
1. Opponent plays a card
2. Status correctly shows "Your turn, choose a card to play"
3. Player attempts to click on their remaining card(s)
4. Cards do not respond to clicks
5. Checking debug state reveals turn state mismatch

---

## Expected Behavior

1. After opponent plays a card, turn should switch to player
2. Status message should say "Your turn, choose a card to play"
3. `isPlayerTurn` should be `true`
4. `PeggingManager.isPlayerTurn` should be `PLAYER`
5. Cards should be clickable (if they haven't been played)
6. **All turn state indicators should be consistent**

---

## Actual Behavior

- Status message correctly indicates player's turn
- Internal turn state (`isPlayerTurn` flag) shows opponent's turn (Instance #2)
- PeggingManager thinks it's opponent's turn (Instance #2)
- Cards are not clickable
- **State desynchronization between status message and turn logic**

---

## Suggested Investigation Steps

1. **Add logging to track turn state changes:**
   - Log when `isPlayerTurn` changes in CribbageMainScreen
   - Log when `PeggingManager.isPlayerTurn` changes
   - Log when `applyManagerStateToUi()` is called
   - Compare these logs during normal play vs when bug occurs

2. **Review state synchronization:**
   - Verify `applyManagerStateToUi()` is called after every opponent play
   - Check if there are race conditions in state updates
   - Ensure Handler.postDelayed operations aren't interfering with turn updates

3. **Verify PlayerHandCompact receives correct props:**
   - Add logging to PlayerHandCompact to show `isPlayerTurn` value at render time
   - Check if recomposition is triggered after turn changes

4. **Test the fix from version 1.0.6:**
   - The fix in 1.0.6 added `isPlayerTurn` parameter to `PlayerHandCompact`
   - Verify this parameter is always passed with the current value of `isPlayerTurn`
   - Check if there's a timing issue where the component renders before state updates

---

## Workaround

**None known** - Game becomes unplayable when this occurs.

---

## Related Issues

- Previous bug (fixed in 1.0.6): Cards were unclickable because `PlayerHandCompact` only checked `!playedCards.contains(index)` without checking `isPlayerTurn`
- This appears to be a separate issue where the turn state itself is incorrect, not just the clickability logic

---

## Priority

**HIGH** - This is a game-breaking bug that prevents core gameplay. However, it appears to be intermittent and difficult to reproduce, which may indicate a race condition or timing issue.

---

## Next Steps

1. Instrument code with comprehensive logging of turn state changes
2. Play multiple games to try to reproduce the issue with logging enabled
3. Analyze logs to identify where state desynchronization occurs
4. Implement fix to ensure turn state consistency
5. Add automated tests to verify turn state synchronization after opponent plays

---

## Investigation: Handler.postDelayed() Usage

### Current Implementation

The codebase uses `Handler(Looper.getMainLooper()).postDelayed()` extensively for opponent AI moves and state transitions. All delays are set to **1000ms (1 second)**.

**Locations found (5 instances):**

1. **Line 286-335**: Opponent card play during active pegging turn
   - Delay: 1000ms
   - Purpose: Simulate opponent "thinking" before playing a card
   - Actions inside delay:
     - Calls `chooseSmartOpponentCard()`
     - Calls `mgr.onPlay(cardToPlay)`
     - Updates `opponentCardsPlayed`
     - Updates `peggingDisplayPile`
     - Calls `applyManagerStateToUi()`
     - Calls `applyManagerReset()` if reset occurred
     - Updates `gameStatus`
     - Determines if player should see "Go" button or can play

2. **Line 368-436**: GO handling continuation after turn transfer
   - Delay: 1000ms
   - Purpose: Give visual pause after GO is declared before next action
   - Actions inside delay:
     - Determines whose turn it is
     - Calculates legal moves for current player
     - If no legal moves, triggers another GO
     - If opponent's turn, plays opponent card
     - If player's turn, updates UI for player to play

3. **Line 631-695**: Starting pegging phase after crib selection
   - Delay: 1000ms (line 631)
   - Nested delay: 1000ms (line 644)
   - Purpose: Transition from crib selection to pegging phase
   - Actions inside first delay:
     - Sets `isPeggingPhase = true`
     - Sets initial `isPlayerTurn` based on dealer
     - Updates `gameStatus`
   - If opponent goes first, nested delay triggers opponent's first play

4. **Line 730-779**: Opponent plays after player plays (turn continuation)
   - Delay: 1000ms
   - Purpose: Opponent responds to player's card play
   - Actions inside delay:
     - Same logic as location #1 above

5. **Line 779**: Additional nested opponent play (appears to be in GO handling)
   - Delay: 1000ms
   - Part of complex GO/turn continuation logic

### Key Observation: applyManagerStateToUi() Timing

The function `applyManagerStateToUi()` is responsible for synchronizing the UI `isPlayerTurn` state with the `PeggingManager.isPlayerTurn` state:

```kotlin
fun applyManagerStateToUi() {
    val mgr = peggingManager ?: return
    peggingCount = mgr.peggingCount
    peggingPile = mgr.peggingPile.toList()
    val newPlayerTurn = (mgr.isPlayerTurn == Player.PLAYER)
    android.util.Log.d("CribbageDebug", "applyManagerStateToUi: isPlayerTurn changing from $isPlayerTurn to $newPlayerTurn, mgr.isPlayerTurn=${mgr.isPlayerTurn}")
    isPlayerTurn = newPlayerTurn
    consecutiveGoes = mgr.consecutiveGoes
    lastPlayerWhoPlayed = when (mgr.lastPlayerWhoPlayed) {
        Player.PLAYER -> "player"
        Player.OPPONENT -> "opponent"
        else -> null
    }
}
```

**Critical Finding**: `applyManagerStateToUi()` is called **inside the postDelayed() callbacks** (line 305, 361, etc.), meaning:
- The manager's state changes immediately when `mgr.onPlay()` is called
- BUT the UI state (`isPlayerTurn`, `peggingCount`, etc.) doesn't update until 1000ms later
- During this 1000ms window, there is a state mismatch between the manager and the UI

### Synchronous vs Asynchronous Execution

**Current behavior (asynchronous with postDelayed):**
1. Opponent needs to play → schedule action for 1000ms in the future
2. Continue executing code (UI may recompose)
3. 1000ms later → execute opponent play logic
4. Inside the delayed callback → call `applyManagerStateToUi()`

**Proposed synchronous alternative:**
1. Opponent needs to play → execute immediately (0ms delay)
2. Call `applyManagerStateToUi()` immediately after manager state changes
3. UI state stays synchronized with manager state at all times

### Why postDelayed() is Used

Based on code analysis, `postDelayed()` appears to serve these purposes:
1. **User Experience**: Creates artificial "thinking time" for the opponent (1 second delay)
2. **Visual Pacing**: Prevents rapid-fire card plays that would be hard to follow
3. **Animation Time**: May allow time for UI animations/transitions to complete

### Potential Issues with postDelayed()

1. **Race Conditions**: If user somehow triggers an action during the 1000ms delay window, state may be inconsistent
2. **State Desynchronization**: Manager state changes immediately via `mgr.onPlay()`, but UI state doesn't update until callback executes
3. **Multiple Delayed Callbacks**: Complex scenarios with GOs may have multiple nested `postDelayed()` calls executing in sequence
4. **Compose Recomposition**: If Compose recomposes during the delay window, it may render with stale state

### Feasibility of Removing postDelayed()

**Technical feasibility**: Yes, the delays could be removed or reduced to 0ms. The code would execute synchronously.

**User experience impact**:
- Opponent would play instantly with no "thinking" animation
- Game pace would be much faster
- May feel robotic or less natural

**Alternative approaches**:
1. Keep delays but ensure state is fully synchronized before and after
2. Use Compose's `LaunchedEffect` with `delay()` instead of Handler for better integration with Compose lifecycle
3. Reduce delay from 1000ms to a smaller value (e.g., 300-500ms)
4. Add explicit state barriers to prevent race conditions during delayed operations
5. Use a state machine that blocks user input during opponent's turn transition

### Questions for Further Investigation

1. Can the bug be reproduced if delays are set to 0ms?
2. Does the bug occur during the 1000ms delay window?
3. Are there race conditions between postDelayed callbacks and user interactions?
4. Is the `playCardButtonEnabled` flag properly synchronized with turn state?

### Recommendation

Before removing `postDelayed()`:
1. Add comprehensive logging to track exact timing of state changes
2. Log when postDelayed callbacks start/end
3. Log all state values before and after `applyManagerStateToUi()` calls
4. Try reproducing bug with reduced delays (e.g., 100ms) to narrow timing window
5. If bug disappears with 0ms delay, confirms it's a timing/race condition issue

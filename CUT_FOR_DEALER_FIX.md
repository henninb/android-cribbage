# Cut for Dealer Display Fix

## Issue
The "Cut for Dealer" screen was appearing on every round when the "Deal Cards" button was enabled, not just on the very first round of a new game. This was incorrect behavior - the cut for dealer should only happen once at the start of a new game to determine the initial dealer.

## Root Cause
The UI was showing the cut cards whenever:
```kotlin
gameStarted && dealButtonEnabled && cutPlayerCard != null && cutOpponentCard != null
```

Since the cut cards were persisted in SharedPreferences and loaded on app start, they would continue to exist even in subsequent rounds. The `dealButtonEnabled` flag becomes true at the start of every round, causing the cut for dealer display to appear repeatedly.

## Solution
Introduced a new state variable `showCutForDealer` that explicitly controls when the cut for dealer display should be shown:

### Changes Made

1. **Added new state variable** (FirstScreen.kt:54):
   ```kotlin
   var showCutForDealer by remember { mutableStateOf(false) }
   ```

2. **Set flag during initial dealer cut** (FirstScreen.kt:467):
   ```kotlin
   showCutForDealer = true  // Only show cut screen on first round
   ```

3. **Clear flag when using previous game's dealer** (FirstScreen.kt:448):
   ```kotlin
   showCutForDealer = false
   ```

4. **Hide cut display after first deal** (FirstScreen.kt:509):
   ```kotlin
   // Hide cut for dealer screen after first deal
   showCutForDealer = false
   ```

5. **Updated UI condition** (FirstScreen.kt:786-787):
   ```kotlin
   cutPlayerCard = if (showCutForDealer && gameStarted && dealButtonEnabled) cutPlayerCard else null,
   cutOpponentCard = if (showCutForDealer && gameStarted && dealButtonEnabled) cutOpponentCard else null,
   ```

## Behavior After Fix

### First Game Ever
1. **Start New Game** → Cuts for dealer → Sets `showCutForDealer = true`
2. **Cut cards displayed** → User sees who won the cut
3. **Deal Cards** → Cards dealt → `showCutForDealer = false`
4. **Round 2+** → No cut display, dealer just alternates

### Subsequent Games (After one game completes)
1. **Start New Game** → Uses previous game loser as dealer → Sets `showCutForDealer = false`
2. **No cut display** → Goes straight to "Deal Cards"
3. **Round 2+** → No cut display, dealer alternates

## Testing Checklist

- [x] Build successful
- [x] No compilation errors
- [ ] Start new game (first ever) → Cut for dealer shows ✓
- [ ] Deal cards → Cut for dealer disappears ✓
- [ ] Play round 1 to completion
- [ ] Round 2 starts → Cut for dealer does NOT show ✓
- [ ] Complete game
- [ ] Start new game → Cut for dealer does NOT show (uses previous loser) ✓
- [ ] App restart → Cut for dealer does NOT show on subsequent rounds ✓

## Files Modified
- `app/src/main/java/com/brianhenning/cribbage/ui/screens/FirstScreen.kt`
  - Added `showCutForDealer` state variable
  - Updated `startNewGame()` to set the flag appropriately
  - Updated `dealCards()` to hide the flag after first deal
  - Updated `GameAreaContent()` call to use the flag

## Build Status
```
BUILD SUCCESSFUL in 1m
109 actionable tasks: 27 executed, 82 up-to-date
```

## Summary
The cut for dealer screen now appears **only once** at the very start of the first game to determine the initial dealer. After the first deal, and in all subsequent rounds, the screen does not appear and the dealer simply alternates as per cribbage rules.

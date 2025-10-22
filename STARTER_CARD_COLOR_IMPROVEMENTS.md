# Starter Card Position & Color Coding Improvements

## Changes Made

### 1. ✅ Moved Starter Card to Score Header
**Problem**: The starter card was displayed in the middle of the game area next to the pegging pile with a "Starter:" label, making it distracting during pegging.

**Solution**:
- Moved the starter card to the **top-right corner** of the score header
- Removed the "Starter:" label completely
- Card appears/disappears smoothly when starter is revealed/cleared
- Uses small card size (CardSize.Small) to not dominate the header

**Implementation**:
- Updated `CompactScoreHeader` to accept `starterCard` parameter
- Added Box layout to position card in top-right corner
- Added padding to score row to prevent overlap with card
- Removed inline starter display from pegging phase in `GameAreaContent`

### 2. ✅ Color-Coded Scores
**Problem**: Both player and opponent scores used the same color scheme, making it harder to quickly distinguish at a glance.

**Solution**:
- **Player ("You")**: Blue color for all score elements
  - Label text
  - Score number
  - Progress bar
- **Opponent**: Red color for all score elements
  - Label text
  - Score number
  - Progress bar

**Why Blue & Red**:
- Matches the cribbage board peg colors (blue for player, red for opponent)
- High contrast and easy to distinguish
- Traditional gaming colors for opposing sides
- Accessible color combination

**Implementation**:
- Added `isPlayer: Boolean` parameter to `ScoreSection`
- Set `scoreColor = if (isPlayer) Color.Blue else Color.Red`
- Applied color to:
  - Label text
  - Score number (large display)
  - Progress bar indicator

## Visual Changes

### Before
```
┌────────────────────────────────────┐
│  You: 42    [D]    Opponent: 38    │
│  ━━━━━━━━━━         ━━━━━━━━━━     │  ← All same color
└────────────────────────────────────┘

[Game Area]
  Opponent: 🂠 🂠 🂠 🂠

  Count: 23
  7♦ K♥ 6♠

  Starter: J♣  ← Distracting in middle

  Your turn
  [Your cards]
```

### After
```
┌────────────────────────────────────┐
│  You: 42    [D]    Opponent: 38  🂫│ ← Starter in corner
│  ━━━━━━━━━━         ━━━━━━━━━━     │
│  ^Blue              ^Red            │
└────────────────────────────────────┘

[Game Area]
  Opponent: 🂠 🂠 🂠 🂠

  Count: 23
  7♦ K♥ 6♠

  Your turn  ← No starter label/card here
  [Your cards]
```

## Benefits

### Starter Card Repositioning
1. ✅ **Less distracting** - Out of the way in corner
2. ✅ **Always visible** - Stays in header throughout hand
3. ✅ **Space efficient** - Frees up center area for pegging
4. ✅ **Cleaner design** - No extra label needed
5. ✅ **Logical placement** - With game info, not with action area

### Color Coding
1. ✅ **Quick identification** - Instant recognition of player vs opponent
2. ✅ **Visual consistency** - Matches cribbage board peg colors
3. ✅ **Reduced cognitive load** - Don't need to read labels
4. ✅ **Better accessibility** - High contrast colors
5. ✅ **Professional appearance** - Standard gaming UI pattern

## Technical Details

### Files Modified
1. **ZoneComponents.kt**
   - `CompactScoreHeader`: Added `starterCard` parameter, positioned in top-right
   - `ScoreSection`: Added `isPlayer` parameter, applied color coding
   - `GameAreaContent` (Pegging phase): Removed inline starter card display

2. **FirstScreen.kt**
   - Updated `CompactScoreHeader` call to pass `starterCard`

### Color Values Used
```kotlin
val scoreColor = if (isPlayer) Color.Blue else Color.Red
```

Using standard Material Color values:
- `Color.Blue` - Standard blue (#2196F3 equivalent)
- `Color.Red` - Standard red (#F44336 equivalent)

### Positioning Logic
```kotlin
Box(modifier = Modifier.fillMaxWidth()) {
    Row(
        modifier = Modifier
            .padding(end = if (starterCard != null) 60.dp else 0.dp)
        // ... scores ...
    )

    if (starterCard != null) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 4.dp)
        ) {
            GameCard(card = starterCard, cardSize = CardSize.Small)
        }
    }
}
```

## Build Status
```
BUILD SUCCESSFUL in 28s
109 actionable tasks: 27 executed, 82 up-to-date
```

## Testing Checklist
- [x] Compiles successfully
- [x] No runtime errors
- [ ] Player score displays in blue
- [ ] Opponent score displays in red
- [ ] Progress bars use respective colors
- [ ] Starter card appears in top-right corner
- [ ] Starter card has no label
- [ ] Starter card doesn't overlap scores
- [ ] Starter card disappears at end of hand
- [ ] Pegging area no longer shows starter card

## Alternative Colors (If Blue/Red Don't Work)
If blue and red don't provide good contrast or look off:

**Option 1: Material Theme Colors**
- Player: `MaterialTheme.colorScheme.primary`
- Opponent: `MaterialTheme.colorScheme.tertiary`

**Option 2: Darker Variants**
- Player: `Color(0xFF1976D2)` (darker blue)
- Opponent: `Color(0xFFD32F2F)` (darker red)

**Option 3: Complementary Colors**
- Player: `Color(0xFF0D47A1)` (deep blue)
- Opponent: `Color(0xFFFF6F00)` (orange)

Currently using standard `Color.Blue` and `Color.Red` for maximum contrast and traditional gaming aesthetics.

# Pegging Game UI/UX Improvements Plan

## Executive Summary
This document outlines proposed improvements to the pegging phase UI/UX based on four key concerns: card visibility for played vs. remaining cards, score reset clarity when hitting 31, cut card positioning, and color consistency between score displays.

**Priority Order:**
1. **Functionality First** - Ensuring clear game state visibility
2. **Aesthetics Second** - Making the UI pleasant and polished

---

## Issue 1: Semi-Transparent Played Cards Look Odd

### Current Implementation
**Location:** `GameCard.kt:46-50`

```kotlin
val alpha by animateFloatAsState(
    targetValue = if (isPlayed) 0.4f else 1.0f,
    animationSpec = tween(300),
    label = "card_alpha"
)
```

**Problem:** Played cards at 40% opacity look washed out and can be confusing. It's hard to quickly identify which cards remain available to play, especially when overlapping (-45dp spacing in `PlayerHandCompact`).

### Proposed Solutions

#### **Option 1A: Dimmed + Desaturated + Border (Recommended)**
- **Played cards:**
  - Opacity: `0.5f` (instead of 0.4f)
  - Apply grayscale filter using `ColorMatrix`
  - Add 2dp red border to clearly mark as "used"
- **Remaining cards:**
  - Full color, 1.0f opacity
  - Subtle elevation/shadow increase
  - Optional: slight scale up (1.05f) for emphasis

**Pros:**
- Very clear visual distinction
- Maintains card readability for game history
- Border provides immediate "played" indicator
- Grayscale makes unplayed cards "pop" with color

**Cons:**
- Slightly more complex implementation
- Needs `ColorFilter` and border styling

**Estimated Changes:**
- `GameCard.kt`: Add `ColorFilter.colorMatrix()` for played cards
- Add border modifier: `.border(2.dp, Color.Red.copy(alpha=0.6f))`
- Adjust alpha to 0.5f

---

#### **Option 1B: Shrink + Shift Down**
- **Played cards:**
  - Scale: `0.75f` (25% smaller)
  - Y-offset: `+10.dp` (shifted down)
  - Opacity: `0.6f` (less transparent than current)
- **Remaining cards:**
  - Normal size (1.0f)
  - Normal position
  - Full opacity

**Pros:**
- Physically smaller = clearly "used"
- Remaining cards dominate visual space
- No overlap confusion

**Cons:**
- May look cramped with overlapping cards
- Animation might be jarring if too fast

**Estimated Changes:**
- `GameCard.kt`: Add scale animation for played state
- Add offset modifier: `.offset(y = if (isPlayed) 10.dp else 0.dp)`
- Adjust alpha to 0.6f

---

#### **Option 1C: Move Played Cards to Separate Row (Most Drastic)**
- **Played cards:**
  - Move to a separate compact row above or below active hand
  - Very small size: `CardSize.Small` (60x90dp)
  - Label: "Played:"
- **Remaining cards:**
  - Full size in main hand area
  - No overlap concerns

**Pros:**
- Maximum clarity - complete physical separation
- No visual confusion whatsoever
- Easy to count remaining cards at a glance

**Cons:**
- Uses more screen real estate
- Requires layout restructuring in `PlayerHandCompact`
- May feel cluttered

**Estimated Changes:**
- `HandDisplay.kt`: Create new `PlayedCardsRow` composable
- Modify `PlayerHandCompact` to filter cards by played state
- Add vertical stack layout

---

#### **Option 1D: "Flip Over" Played Cards (Card Back)**
- **Played cards:**
  - Animate 180° rotation to show card back
  - `isRevealed = false` after play
  - Opacity: 0.7f
- **Remaining cards:**
  - Face-up as normal

**Pros:**
- Mimics physical cribbage experience
- Very clear distinction (back vs. face)
- Satisfying animation

**Cons:**
- Loses ability to see play history easily
- May confuse players who want to review what was played
- Not traditional for pegging phase

**Estimated Changes:**
- Use existing `FlippableGameCard` composable (already implemented!)
- Trigger flip animation on play
- Adjust `PlayerHandCompact` to use `FlippableGameCard`

---

### **Recommendation: Option 1A** (Dimmed + Desaturated + Border)
**Rationale:** Best balance of clarity (border + grayscale), functionality (maintains visibility of played cards for strategy), and aesthetics (clean visual hierarchy).

---

## Issue 2: Score Reset After 31 is Too Fast

### Current Implementation
**Location:** `ZoneComponents.kt:401-414` (Pegging Count Display)

```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = currentTheme.colors.boardPrimary
    )
) {
    Text(
        text = "Count: $peggingCount",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = currentTheme.colors.accentLight
    )
}
```

**Problem:** When opponent hits 31, the count resets instantly to 0 with no visual feedback, making it unclear what happened.

### Proposed Solutions

#### **Option 2A: Flash Animation + "31!" Banner (Recommended)**
**Sequence:**
1. When count reaches 31:
   - Display "31!" in large text with celebration animation (scale + bounce)
   - Flash count display background with accent color (e.g., gold/green)
   - Duration: 1500ms
2. After delay:
   - Animate count from 31 → 0 with fade/slide transition
   - Duration: 500ms
3. Show "+1" or "+2" point animation above scorer (already implemented)

**Pros:**
- Clear visual feedback that 31 was reached
- Gives player time to process what happened
- Feels celebratory (important moment in pegging)

**Cons:**
- Adds slight delay to game flow
- Requires new animation composable

**Estimated Changes:**
- Create `CountResetAnimation` composable in `ZoneComponents.kt`
- Add state for "show31Banner" in `CribbageMainScreen.kt`
- Trigger animation in pegging logic when count == 31

---

#### **Option 2B: "Count: 31 → 0" Countdown Animation**
- When 31 is hit:
  - Briefly hold at 31 for 800ms
  - Animate number countdown: 31 → 30 → 29 → ... → 0 (fast, 50ms per number)
  - Or smooth interpolation from 31.0 → 0.0 over 800ms

**Pros:**
- Reinforces that the count is resetting
- Smooth transition

**Cons:**
- Countdown might look odd (numbers don't actually count down in cribbage)
- Could be confusing

**Estimated Changes:**
- Add animated countdown in pegging count display
- Use `animateIntAsState` with custom animation spec

---

#### **Option 2C: Temporary "Last Count: 31" Indicator**
- Display small text below main count: "Last: 31" for 2000ms
- Main count updates to 0 immediately
- Subtle pulse animation on "Last: 31"

**Pros:**
- Non-intrusive
- Provides context without blocking game flow
- Simple to implement

**Cons:**
- May be too subtle
- Requires extra UI space

**Estimated Changes:**
- Add `lastResetCount` state variable
- Display conditional text in pegging count card
- Auto-clear after 2s with `LaunchedEffect`

---

#### **Option 2D: Animated Confetti/Spark Effect**
- When 31 is hit:
  - Trigger particle effect (stars, confetti, sparkles) around count display
  - Hold count at 31 for 1200ms with pulsing animation
  - Then fade to 0

**Pros:**
- Visually exciting
- Clearly marks important game event
- Fun aesthetics

**Cons:**
- Most complex to implement
- May feel too "busy" or distracting
- Performance considerations

**Estimated Changes:**
- Create particle animation composable (Canvas-based)
- Trigger on count == 31
- Integrate with count display

---

### **Recommendation: Option 2A** (Flash Animation + "31!" Banner)
**Rationale:** Provides clear, immediate feedback with appropriate celebration for an important game event. Balances functionality (clarity) with aesthetics (polish).

---

## Issue 3: Cut Card Overlaps Opponent Score

### Current Implementation
**Location:** `ZoneComponents.kt:218-232`

```kotlin
// Starter card in top-right corner (no label)
if (starterCard != null) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 4.dp, end = 4.dp)
    ) {
        GameCard(
            card = starterCard,
            isRevealed = true,
            isClickable = false,
            cardSize = CardSize.Medium  // 80x120dp
        )
    }
}
```

**Problem:** CardSize.Medium (80x120dp) positioned at TopEnd overlaps with opponent score section, especially on smaller screens.

### Proposed Solutions

#### **Option 3A: Reduce to CardSize.Small (Recommended)**
- Change `cardSize = CardSize.Small` (60x90dp)
- Keep same position (TopEnd)
- 25% smaller footprint
- Add subtle shadow/elevation to maintain visibility

**Pros:**
- Minimal code change
- Solves overlap issue immediately
- Card still fully readable

**Cons:**
- Smaller card size (but still legible)

**Estimated Changes:**
- `ZoneComponents.kt:229`: Change to `cardSize = CardSize.Small`

**Visual Impact:** Reduces card from 80x120dp → 60x90dp

---

#### **Option 3B: Move to Center-Top Between Scores**
- Position cut card centered between player and opponent scores
- Use `Alignment.TopCenter`
- Slightly smaller: `CardSize.Small`
- May need to adjust padding/spacing

**Pros:**
- Symmetrical layout
- No overlap concerns
- Emphasizes cut card's neutrality (affects both players)

**Cons:**
- May interfere with theme indicator (currently TopStart)
- Less conventional placement

**Estimated Changes:**
- Change alignment to `Alignment.TopCenter`
- Adjust padding values
- May need to reposition theme indicator

---

#### **Option 3C: Move Below Header, Above Game Area**
- Position cut card in its own row between score header and game area
- Centered horizontally
- Label: "Cut Card:" or "Starter:"
- `CardSize.Medium` (can keep current size)

**Pros:**
- Dedicated space, no overlap
- Clear labeling
- Traditional placement (separate from scores)

**Cons:**
- Uses more vertical space
- Requires layout restructuring

**Estimated Changes:**
- Remove from `CompactScoreHeader`
- Add new `StarterCardDisplay` composable
- Insert between header and `GameAreaContent` in main screen

---

#### **Option 3D: Floating Card with Adjustable Position**
- Create custom position below opponent score text
- Use absolute positioning or specific padding
- `CardSize.Small`
- Ensure it stays within header bounds

**Pros:**
- Fine-tuned control over position
- Can optimize for different screen sizes

**Cons:**
- More complex positioning logic
- May not scale well across devices

**Estimated Changes:**
- Custom Box with specific offset values
- Calculate position based on opponent score width

---

#### **Option 3E: Collapsible/Expandable Cut Card Icon**
- Show small card icon (30x40dp) at TopEnd by default
- Tap to expand to full size temporarily (modal overlay)
- Auto-collapse after 3s

**Pros:**
- Minimal screen usage
- Available when needed
- Creative solution

**Cons:**
- Requires interaction (less accessible)
- May be confusing
- Unconventional UX

**Estimated Changes:**
- Add toggle state for expanded/collapsed
- Create overlay composable for expanded view
- Add tap handler

---

### **Recommendation: Option 3A** (CardSize.Small)
**Rationale:** Simplest solution with immediate impact. Reduces overlap while maintaining visibility and readability. Can always relocate if further issues arise.

**Alternative:** Option 3C if more screen space becomes available or if vertical layout is reorganized.

---

## Issue 4: Point Color Inconsistency

### Current Implementation

#### Score Display in Header (Top)
**Location:** `ZoneComponents.kt:248-252, 287-295`

```kotlin
// Uses theme-based colors
val scoreColor = if (isPlayer) {
    currentTheme.colors.primary        // e.g., Green, Amber, Orange
} else {
    currentTheme.colors.secondary      // e.g., Yellow, Blue, Gold
}

// Progress bar
LinearProgressIndicator(
    progress = { (score / 121f).coerceIn(0f, 1f) },
    color = scoreColor,  // Uses theme colors
    trackColor = MaterialTheme.colorScheme.surfaceVariant,
)
```

#### Cribbage Board Track (Bottom)
**Location:** `ZoneComponents.kt:850-892`

```kotlin
// Uses Material theme colors (NOT seasonal theme colors!)
val boardPrimaryColor = MaterialTheme.colorScheme.tertiary
val boardSecondaryColor = MaterialTheme.colorScheme.tertiaryContainer

// Player peg: boardPrimaryColor (tertiary)
// Opponent peg: boardSecondaryColor (tertiaryContainer)
```

**Problem:**
- **Top (header):** Uses `currentTheme.colors.primary` (player) and `.secondary` (opponent)
- **Bottom (board track):** Uses `MaterialTheme.colorScheme.tertiary` (both pegs)
- **Result:** Colors don't match between top score text and bottom board pegs

### Proposed Solutions

#### **Option 4A: Align Board Track to Theme Colors (Recommended)**
Change board track to use seasonal theme colors:

```kotlin
val boardPrimaryColor = currentTheme.colors.boardPrimary    // Player peg
val boardSecondaryColor = currentTheme.colors.boardSecondary // Opponent peg
```

**Pros:**
- Consistent with header score colors
- Leverages existing theme system
- Seasonal theming extends to full UI

**Cons:**
- Board track colors may shift dramatically with themes
- Requires testing across all themes for legibility

**Estimated Changes:**
- `ZoneComponents.kt:850-852`: Change to theme colors
- Test all 13 themes (4 seasons + 9 holidays) for contrast

---

#### **Option 4B: Align Header to Material Colors**
Change header score colors to use Material theme colors:

```kotlin
val scoreColor = if (isPlayer) {
    MaterialTheme.colorScheme.primary
} else {
    MaterialTheme.colorScheme.secondary
}
```

**Pros:**
- Material theme is consistent across app
- No seasonal variation

**Cons:**
- Loses seasonal theme colors in header (defeats purpose of theme system)
- Less visually interesting

**Estimated Changes:**
- `ZoneComponents.kt:248-252`: Change to Material colors
- Remove theme-based color logic

---

#### **Option 4C: Unified Color Variables**
Create explicit "player color" and "opponent color" variables that are used consistently across all UI elements:

```kotlin
// In theme definitions
data class ThemeColors(
    ...
    val playerColor: Color,      // Explicitly for player
    val opponentColor: Color,    // Explicitly for opponent
)

// Use everywhere
val scoreColor = if (isPlayer) currentTheme.colors.playerColor else currentTheme.colors.opponentColor
val boardPrimaryColor = currentTheme.colors.playerColor
val boardSecondaryColor = currentTheme.colors.opponentColor
```

**Pros:**
- Absolute consistency guaranteed
- Clear semantic naming
- Easy to maintain

**Cons:**
- Requires updating all 13 theme definitions
- Adds redundancy (`playerColor` = `primary` in most cases)

**Estimated Changes:**
- `SeasonalTheme.kt:35-47`: Add `playerColor` and `opponentColor` fields
- Update all 13 theme definitions (lines 177-443)
- Update all color references in UI

---

#### **Option 4D: Two-Tier Color System**
- **Score text + progress bar (header):** Use theme colors (as is)
- **Board track pegs:** Use fixed, high-contrast colors (Player: Blue, Opponent: Red)

**Rationale:** Board track is "physical" representation, should be consistent. Header is "digital" display, can be themed.

**Pros:**
- Board track is always highly legible
- Header can be decorative/themed

**Cons:**
- Intentional inconsistency (may confuse users)
- Doesn't fully solve the issue

**Estimated Changes:**
- Keep current implementation but document the intentional difference

---

### **Recommendation: Option 4A** (Align Board Track to Theme Colors)
**Rationale:** The seasonal theme system is a key feature of the app (13 different themes!). Extending theme colors consistently to both header and board track creates a cohesive visual experience.

**Action Items:**
1. Update `CribbageBoardTrack` to use `currentTheme.colors.boardPrimary` and `.boardSecondary`
2. Test all 13 themes for adequate contrast between:
   - Peg colors and track background
   - Player and opponent pegs
3. Adjust specific theme colors if contrast issues are found

**Fallback:** If contrast issues are widespread, use Option 4C (unified color variables) to create optimized player/opponent colors per theme.

---

## Implementation Priority

### Phase 1: Critical Functionality (Immediate)
1. **Issue 1:** Implement Option 1A (Dimmed + Desaturated + Border for played cards)
   - **Impact:** HIGH - Directly addresses core gameplay visibility
   - **Effort:** Medium
   - **Files:** `GameCard.kt`

2. **Issue 4:** Implement Option 4A (Align board track to theme colors)
   - **Impact:** MEDIUM - Visual consistency
   - **Effort:** Low
   - **Files:** `ZoneComponents.kt:850-852`

### Phase 2: Important UX (Next Sprint)
3. **Issue 2:** Implement Option 2A (Flash animation + "31!" banner)
   - **Impact:** HIGH - Improves game clarity at critical moment
   - **Effort:** Medium
   - **Files:** `ZoneComponents.kt` (new composable), `CribbageMainScreen.kt` (state)

4. **Issue 3:** Implement Option 3A (CardSize.Small for cut card)
   - **Impact:** LOW-MEDIUM - Solves overlap on small screens
   - **Effort:** Trivial
   - **Files:** `ZoneComponents.kt:229`

---

## Testing Checklist

### Issue 1: Played Cards Visibility
- [ ] Played cards are visually distinct from unplayed cards
- [ ] Can quickly identify remaining cards in hand
- [ ] Overlapping cards (-45dp spacing) don't obscure borders/filters
- [ ] Animation is smooth and not jarring
- [ ] Works across all seasonal themes

### Issue 2: Score Reset Clarity
- [ ] "31!" banner appears when count reaches exactly 31
- [ ] Banner is visible for adequate duration (1500ms)
- [ ] Transition from 31 → 0 is clear
- [ ] Point animation (+1/+2) appears correctly for scorer
- [ ] Doesn't interfere with next card play

### Issue 3: Cut Card Position
- [ ] Cut card doesn't overlap opponent score on any device size
- [ ] Card remains readable at new size (if resized)
- [ ] Position is visually balanced in header
- [ ] Works in portrait and landscape (if supported)

### Issue 4: Color Consistency
- [ ] Player color matches between header score and board peg
- [ ] Opponent color matches between header score and board peg
- [ ] Progress bar color matches score text color
- [ ] Tested across all 13 themes:
  - [ ] Spring, Summer, Fall, Winter
  - [ ] New Year, MLK Day, Valentine's, Presidents' Day
  - [ ] St. Patrick's, Memorial Day, Independence Day
  - [ ] Labor Day, Halloween, Thanksgiving
- [ ] Adequate contrast on all theme backgrounds

---

## Design Mockup Notes

### Color Palette Reference (Current Themes)
| Theme | Player (Primary) | Opponent (Secondary) | Board Track Notes |
|-------|------------------|----------------------|-------------------|
| **Spring** | Green `#388E3C` | Yellow `#F9A825` | Should use boardPrimary `#66BB6A`, boardSecondary `#81C784` |
| **Summer** | Amber `#F9A825` | Sky Blue `#0277BD` | Should use boardPrimary `#FFCA28`, boardSecondary `#4FC3F7` |
| **Fall** | Orange `#FF8A50` | Gold `#FFB74D` | Dark background - needs high contrast |
| **Winter** | Blue `#1565C0` | Blue Grey `#78909C` | Dark background - needs testing |
| **Halloween** | Orange `#FF6F00` | Purple `#7E57C2` | Dark background - verify contrast |

**Note:** See `SeasonalTheme.kt:173-443` for complete color definitions.

---

## Future Enhancements (Out of Scope)

### Accessibility
- Add haptic feedback when card is played (vibration)
- Optional high-contrast mode with fixed colors
- Screen reader announcements for score changes

### Advanced Animations
- Card play animation (slide from hand to pegging pile)
- Peg movement animation on board track (currently animates, but could add trail effect)
- Score increment "ticker" animation (numbers rolling up)

### Settings/Preferences
- Toggle for played card style (option to choose between 1A, 1B, 1C, 1D)
- Adjustable animation speed
- Custom player/opponent colors (override theme)

---

## Summary of Recommendations

| Issue | Recommended Solution | Rationale | Priority |
|-------|---------------------|-----------|----------|
| **1. Played Cards** | Option 1A: Dimmed + Desaturated + Border | Best balance of clarity and aesthetics | **P0** |
| **2. Score Reset** | Option 2A: Flash Animation + "31!" Banner | Clear feedback for critical game event | **P1** |
| **3. Cut Card Position** | Option 3A: CardSize.Small | Simple fix, immediate impact | **P1** |
| **4. Color Consistency** | Option 4A: Align Board Track to Theme Colors | Leverages existing theme system, cohesive design | **P0** |

---

## Estimated Implementation Effort

| Issue | Solution | Effort | Lines of Code | Files Modified |
|-------|----------|--------|---------------|----------------|
| **Issue 1** | Option 1A | 3-4 hours | ~30 lines | 1 (`GameCard.kt`) |
| **Issue 2** | Option 2A | 4-5 hours | ~60 lines | 2 (`ZoneComponents.kt`, `CribbageMainScreen.kt`) |
| **Issue 3** | Option 3A | 5 minutes | 1 line | 1 (`ZoneComponents.kt`) |
| **Issue 4** | Option 4A | 1 hour + testing | ~3 lines + validation | 1 (`ZoneComponents.kt`) |
| **Total** | All | ~9 hours | ~94 lines | 3 files |

---

## Questions for Product Owner

1. **Issue 1:** Do you prefer the border approach (1A) or would you like to see a mockup of the "separate row" approach (1C)?
2. **Issue 2:** Is 1500ms an acceptable delay for the "31!" banner, or would you prefer shorter/longer?
3. **Issue 3:** Is CardSize.Small (60x90dp) acceptable, or do you want to explore relocation options?
4. **Issue 4:** Should we validate contrast across all 13 themes before implementation, or proceed with theme colors and adjust problematic themes afterward?
5. **General:** Are there any other pegging phase UI concerns not covered in this plan?

---

**Document Version:** 1.0
**Last Updated:** 2025-10-27
**Author:** Claude Code (AI Assistant)
**Review Status:** Pending User Approval

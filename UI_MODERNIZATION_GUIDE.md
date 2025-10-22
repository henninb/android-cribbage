# Android Cribbage - UI/UX Modernization Guide (Revised)

## Overview
Complete redesign inspired by successful cribbage apps (like Cribbage Pro) to create a streamlined, single-screen experience that eliminates scrolling and maximizes gameplay clarity.

## Core Design Philosophy

### 1. **Everything Fits on One Screen**
- **Zero scrolling during gameplay**
- **Smart element visibility**: Show only what's relevant to the current game phase
- **Fixed layout zones**: Each screen area has a clear purpose
- **Adaptive content**: Elements appear/disappear based on game state

### 2. **Visual Hierarchy**
Priority from top to bottom:
1. **Scores** (always visible, compact)
2. **Active game area** (cards, pegging, dealer indicator)
3. **Action buttons** (context-sensitive, single row)
4. **Visual cribbage board** (persistent scoring reference)

### 3. **Progressive Disclosure**
- Show information when needed, hide when irrelevant
- Reduce cognitive load by removing unnecessary choices
- Guide users through game phases with clear visual cues

---

## Detailed Layout Design

### Zone 1: Score Header (Top, Always Visible)
**Fixed Height: ~60-80dp**

```
┌─────────────────────────────────────────┐
│  ●You: 42        [D]        Opp: 38 ●   │
│  ━━━━━━━━━━        ━━━━━━━━━━          │
└─────────────────────────────────────────┘
```

**Components:**
- **Player scores**: Large, readable numbers
- **Progress bars**: Visual representation of score (out of 121)
- **Dealer indicator**: Small [D] badge or dealer chip icon next to current dealer's name
- **Compact design**: Single line, minimal padding (8-12dp)
- **Color coding**: Subtle player/opponent color differentiation

**Removed from header:**
- Match record (moved to menu or post-game summary)
- Cut cards display (shown only during cut phase, then disappears)
- Verbose text labels (icons + numbers only)

---

### Zone 2: Active Game Area (Center, Dynamic)
**Flexible Height: Uses remaining space between header and bottom**

This zone changes based on game phase:

#### **Phase: Setup / Cut for Dealer**
```
┌─────────────────────────────────────────┐
│        Cut for First Dealer             │
│                                         │
│      [Your Card]    [Opp Card]         │
│         A♠            K♥                │
│                                         │
│     Lower card deals first              │
└─────────────────────────────────────────┘
```
- Shows only during initial dealer determination
- Disappears immediately after dealer is established
- Auto-transitions to deal phase

#### **Phase: Crib Selection**
```
┌─────────────────────────────────────────┐
│                                         │
│    [Opponent's face-down cards]         │
│        🂠  🂠  🂠  🂠                    │
│                                         │
│           Starter: (empty)              │
│                                         │
│      Select 2 cards for crib            │
│                                         │
│    [Your hand - 6 cards, selectable]    │
│     A♠  5♥  7♦  9♣  J♥  K♠             │
│                                         │
└─────────────────────────────────────────┘
```

#### **Phase: Pegging**
```
┌─────────────────────────────────────────┐
│    Opponent: 🂠  ✓   ✓   🂠  (2 left)   │
│                                         │
│        Count: 23                        │
│     ┌────────────────┐                  │
│     │ 7♦  K♥  6♠     │ ← Pegging pile  │
│     └────────────────┘                  │
│                                         │
│           Starter: J♣                   │
│                                         │
│      Your turn to play                  │
│                                         │
│    [Your hand with played cards grayed] │
│     A♠  ✓   7♦  ✓                      │
│                                         │
└─────────────────────────────────────────┘
```
- **Count prominently displayed** (large, bold text)
- **Pegging pile** shown in compact card row
- **Opponent cards**: Show count + placeholders (face-down/checkmarks for played)
- **Starter card**: Visible but not emphasized
- **Turn indicator**: Clear text "Your turn" or "Opponent's turn"

#### **Phase: Hand Counting**
```
┌─────────────────────────────────────────┐
│      Counting Non-Dealer Hand           │
│                                         │
│    Opponent: A♠  5♥  7♦  9♣             │
│    Starter:  J♣                         │
│                                         │
│    15s: 4 pts  |  Pairs: 2 pts          │
│    Run: 0 pts  |  Flush: 0 pts          │
│                                         │
│         Total: 6 points                 │
│                                         │
│    [Auto-advances to next hand]         │
└─────────────────────────────────────────┘
```
- **Reveals opponent cards** during counting
- **Shows scoring breakdown** with animation
- **Auto-progresses** through non-dealer → dealer → crib
- **Brief pause** between each hand (2-3 seconds)

---

### Zone 3: Action Bar (Bottom, Above Board)
**Fixed Height: ~56dp**

**Single row of context-sensitive buttons** (never more than 3 visible at once):

| Game Phase | Buttons Shown |
|------------|---------------|
| Pre-game | `[Start New Game]` |
| Ready to deal | `[Deal Cards]` `[⚙️ Menu]` |
| Crib selection | `[Discard to Crib]` (enabled when 2 selected) |
| Pegging | `[Go]` (if no legal plays) `[⚙️ Menu]` |
| Hand counting | (no buttons - auto-progresses) |
| Game over | `[New Game]` `[View Match Stats]` |

**Button Design:**
- **Filled button** for primary action (e.g., "Deal Cards", "Discard to Crib")
- **Outlined button** for secondary actions (e.g., "Menu", "Report Bug")
- **Full width or equally weighted** in row (use Modifier.weight(1f))
- **Minimum 48dp touch target**
- **No stacked button rows** - keep it simple!

---

### Zone 4: Cribbage Board (Bottom, Always Visible)
**Fixed Height: ~120-140dp**

```
┌─────────────────────────────────────────┐
│  [Visual cribbage board representation] │
│                                         │
│  ●━━━━━━━━━━━━━━━━━━━━━━━━━━━━━○      │ ← Player track
│  ○━━━━━━━━━━━━━━━━━━━━━━━━━━━━━●      │ ← Opponent track
│                                         │
│  0    15    30    45    60    75    90  │
│              105   121                  │
└─────────────────────────────────────────┘
```

**Purpose:**
- **Visual scoring reference**: Traditional cribbage board aesthetic
- **Peg positions**: Animated peg movement when scores change
- **Always visible**: Provides game context without taking much space
- **Compact design**: Simplified 2-track board (not full 4-peg design)

**Implementation Note:**
- Can be a simple custom Canvas drawing or SVG
- Pegs move smoothly with animation when scores update
- Color-coded for player (e.g., blue) vs opponent (e.g., red)

---

## Key Improvements Over Current Design

### ✅ Eliminated Scrolling
- **Old**: Vertical scroll with many stacked elements
- **New**: Fixed layout with smart visibility toggling

### ✅ Cleaner Score Display
- **Old**: Large card-based score display with verbose labels
- **Old**: Collapsible match record always visible
- **New**: Compact header with progress bars
- **New**: Match stats moved to menu/end screen

### ✅ Transient Elements
- **Old**: Cut cards visible long after cut
- **Old**: Starter card in separate prominent card container
- **New**: Cut cards shown only during cut phase
- **New**: Starter shown inline, small and unobtrusive

### ✅ Context-Sensitive Actions
- **Old**: Multiple button rows visible simultaneously
- **Old**: Buttons for actions not available in current phase
- **New**: Single action bar with max 2-3 relevant buttons
- **New**: Buttons appear/disappear based on game state

### ✅ Pegging Clarity
- **Old**: No count display during pegging
- **Old**: Pegging pile in separate container
- **New**: Large, prominent count display
- **New**: Compact inline pegging pile

### ✅ Visual Cribbage Board
- **Old**: No board, only numeric scores
- **New**: Traditional board visualization for better game feel

---

## State-Based Visibility Matrix

| Element | Setup | Cut | Deal Ready | Crib Select | Pegging | Counting | Game Over |
|---------|-------|-----|------------|-------------|---------|----------|-----------|
| Score Header | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Cut Cards | ✗ | ✓ | ✗ | ✗ | ✗ | ✗ | ✗ |
| Opponent Hand | ✗ | ✗ | ✗ | ✓ (back) | ✓ (back) | ✓ (face) | ✗ |
| Starter Card | ✗ | ✗ | ✗ | ✓ | ✓ | ✓ | ✗ |
| Pegging Count | ✗ | ✗ | ✗ | ✗ | ✓ | ✗ | ✗ |
| Pegging Pile | ✗ | ✗ | ✗ | ✗ | ✓ | ✗ | ✗ |
| Player Hand | ✗ | ✗ | ✗ | ✓ | ✓ | ✓ | ✗ |
| Crib Indicator | ✗ | ✗ | ✗ | ✓ (back) | ✓ (back) | ✓ (face) | ✗ |
| Status Text | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Action Buttons | 1 | 0 | 1-2 | 1 | 0-1 | 0 | 1-2 |
| Cribbage Board | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |

---

## Implementation Strategy

### Phase 1: Layout Restructure
1. **Create new screen zones** (Composables for each zone)
   - `CompactScoreHeader()`
   - `GameAreaContent()` (dynamic based on phase)
   - `ActionBar()`
   - `CribbageBoard()`
2. **Remove vertical scroll** - use fixed Column layout
3. **Implement state-based visibility** for each zone

### Phase 2: Smart Component Design
1. **Compact card displays**
   - Smaller card sizes (adjust CardSize enum)
   - Horizontal card rows instead of grid layouts
   - Opponent cards: simple placeholders/backs
2. **Pegging count component**
   - Large, centered text
   - Animated updates
   - Clear visual prominence
3. **Simplified status messaging**
   - Single line of text
   - Context-aware (e.g., "Your turn" vs. "Opponent's turn")
   - No verbose logs (save for debug/bug reports)

### Phase 3: Visual Polish
1. **Cribbage board implementation**
   - Custom Canvas or SVG rendering
   - Animated peg movement
   - Proper scaling for different screen sizes
2. **Smooth transitions**
   - Fade in/out for appearing/disappearing elements
   - Card flip animations for reveals
   - Score increment animations
3. **Color & theming**
   - Consistent color palette (Material 3)
   - Felt green background option (or user choice)
   - High contrast for accessibility

### Phase 4: Responsive Design
1. **Test on various screen sizes**
   - Small phones (5" screens)
   - Large phones (6.5"+ screens)
   - Tablets (adjust board size)
2. **Landscape orientation**
   - Rearrange zones horizontally
   - Board on right side
   - Cards in center
3. **Dynamic text sizing**
   - Use sp units appropriately
   - Scale based on screen density

---

## Design Inspirations from Cribbage Pro

### What to Adopt:
1. ✅ **Single-screen, no-scroll design**
2. ✅ **Compact score display with visual pegs**
3. ✅ **Dealer indicator badge/token**
4. ✅ **Large, clear pegging count**
5. ✅ **Context-sensitive single action button**
6. ✅ **Cribbage board at bottom for visual scoring**
7. ✅ **Clean, uncluttered layout**
8. ✅ **Traditional card game aesthetic (green felt)**

### What to Keep Original:
1. ✓ **Material Design 3 components** (modern Android look)
2. ✓ **Detailed scoring breakdowns** during hand counting
3. ✓ **Bug report functionality** (keep in menu)
4. ✓ **Match statistics tracking** (show in menu or post-game)

### What NOT to Copy:
- ❌ Exact visual style (avoid copyright issues)
- ❌ Paid features / ads / monetization patterns
- ❌ Specific icon designs or artwork

---

## Success Metrics

### User Experience Goals
- [x] Zero scrolling during active gameplay
- [x] All essential game info visible at appropriate times
- [x] Clear visual indication of game state (phase, turn, dealer)
- [x] Smooth, seamless transitions between phases
- [x] Intuitive, minimal button layout

### Technical Goals
- [x] Fixed layout that works on minSdk 24+ devices
- [x] Proper state management for dynamic visibility
- [x] Smooth animations (60fps)
- [x] Accessible to users with disabilities (TalkBack, contrast)
- [x] Landscape orientation support

---

## Next Steps

1. ✅ **Review and approve this design plan**
2. **Create mockups/wireframes** (optional but recommended)
3. **Implement Zone-based layout structure**
   - CompactScoreHeader
   - GameAreaContent (with phase-based switching)
   - ActionBar (context-sensitive)
   - CribbageBoard (visual component)
4. **Migrate existing logic** into new layout structure
5. **Test on physical devices** (various screen sizes)
6. **Polish animations and transitions**
7. **User testing** with fresh users

---

## Open Questions / Decisions Needed

1. **Cribbage board style**:
   - Traditional wooden board aesthetic?
   - Modern/minimalist line design?
   - User preference setting?

2. **Background**:
   - Green felt (traditional)?
   - Material Design background (modern)?
   - User choice?

3. **Match statistics**:
   - Show in hamburger menu?
   - Post-game dialog only?
   - Subtle indicator in score header?

4. **Card size**:
   - How small can we go while maintaining readability?
   - Test on 5" screen to find minimum

5. **Animations**:
   - How much animation is too much?
   - Preference for reduced motion (accessibility)?

---

## Conclusion

This revised design takes clear inspiration from successful cribbage apps like Cribbage Pro while maintaining our app's unique features (detailed scoring, Material Design, modern Android patterns). The focus is on **eliminating scrolling**, **showing only relevant information**, and **creating a seamless, intuitive single-screen experience**.

The key is **progressive disclosure**: each game phase shows exactly what the player needs, no more, no less. This reduces cognitive load and creates a cleaner, more enjoyable gameplay experience.

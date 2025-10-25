# Cribbage App Icon - Creation Summary

## What Was Created

### 1. Main Icon Design
**File**: `app_icon.svg` (512x512)
- Circular background with green felt gradient (cribbage table theme)
- Wooden cribbage board with peg holes
- Two playing cards: 5 of Hearts and Jack of Spades (represents "15-2" scoring)
- Four colorful pegs (red, blue, green, yellow)
- "15-2" text at bottom (classic cribbage scoring reference)

### 2. Play Store Asset
**File**: `app_icon_512.png` (512x512 PNG)
- High-resolution PNG for Google Play Store listing
- ✓ Ready to upload to Play Console

### 3. Adaptive Icon Components

#### Vector Drawables (Scalable)
- **`app/src/main/res/drawable/ic_launcher_background.xml`**
  - Green felt gradient background (#1a5f3e to #0d4028)
  - Represents the cribbage table

- **`app/src/main/res/drawable/ic_launcher_foreground.xml`**
  - Simplified vector design with:
    - Wooden cribbage board section
    - Two rows of peg holes
    - Two white playing cards
    - Four colored pegs

#### Bitmap Assets (All Densities)
Regular launcher icons generated for:
- `mipmap-mdpi/ic_launcher.png` (48x48)
- `mipmap-hdpi/ic_launcher.png` (72x72)
- `mipmap-xhdpi/ic_launcher.png` (96x96)
- `mipmap-xxhdpi/ic_launcher.png` (144x144)
- `mipmap-xxxhdpi/ic_launcher.png` (192x192)

Round launcher icons:
- `mipmap-mdpi/ic_launcher_round.png` (48x48)
- `mipmap-hdpi/ic_launcher_round.png` (72x72)
- `mipmap-xhdpi/ic_launcher_round.png` (96x96)
- `mipmap-xxhdpi/ic_launcher_round.png` (144x144)
- `mipmap-xxxhdpi/ic_launcher_round.png` (192x192)

Adaptive icon foreground layers:
- `mipmap-mdpi/ic_launcher_foreground.png` (108x108)
- `mipmap-hdpi/ic_launcher_foreground.png` (162x162)
- `mipmap-xhdpi/ic_launcher_foreground.png` (216x216)
- `mipmap-xxhdpi/ic_launcher_foreground.png` (324x324)
- `mipmap-xxxhdpi/ic_launcher_foreground.png` (432x432)

## Icon Features

### Design Elements
1. **Cribbage Board**: Brown wooden texture with realistic peg holes
2. **Playing Cards**: Clean white cards showing "5♥" and "J♠"
3. **Pegs**: Four distinct colors for multi-player gameplay
4. **Background**: Green felt (classic card table)
5. **Scoring Reference**: "15-2" (most common cribbage score)

### Color Palette
- **Green Felt**: #1a5f3e → #0d4028 (gradient)
- **Wood Board**: #8B4513 → #654321 (gradient)
- **Peg Holes**: #2a1810 (dark brown)
- **Red Peg**: #d32f2f
- **Blue Peg**: #1976d2
- **Green Peg**: #388e3c
- **Yellow Peg**: #f57c00
- **Card Background**: #FFFFFF (white)
- **Text**: #ffd700 (gold) with black stroke

## How to Test the Icon

### 1. Build and Install
```bash
./gradlew installDebug
```

### 2. View on Device
- Check your device's app drawer
- The icon should show:
  - **Android 8.0+**: Adaptive icon (background + foreground layers)
  - **Older Android**: Static circular icon

### 3. Preview in Android Studio
- Navigate to: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- Android Studio will show preview in editor
- Right-click → "Create App Icons" to see all variations

## Customization Options

If you want to modify the icon design:

### Edit the SVG
```bash
# Use any SVG editor (Inkscape, Illustrator, Figma)
# File: app_icon.svg or app_icon_foreground.svg
```

### Regenerate All Sizes
```bash
# After editing SVG, regenerate all densities:

# Play Store asset
rsvg-convert -w 512 -h 512 app_icon.svg -o app_icon_512.png

# Regular launcher icons
rsvg-convert -w 192 -h 192 app_icon.svg -o app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
rsvg-convert -w 144 -h 144 app_icon.svg -o app/src/main/res/mipmap-xxhdpi/ic_launcher.png
rsvg-convert -w 96 -h 96 app_icon.svg -o app/src/main/res/mipmap-xhdpi/ic_launcher.png
rsvg-convert -w 72 -h 72 app_icon.svg -o app/src/main/res/mipmap-hdpi/ic_launcher.png
rsvg-convert -w 48 -h 48 app_icon.svg -o app/src/main/res/mipmap-mdpi/ic_launcher.png

# Round launcher icons (same sizes)
# ... (same commands with ic_launcher_round.png)

# Adaptive icon foreground layers
rsvg-convert -w 432 -h 432 app_icon_foreground.svg -o app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png
# ... (162, 216, 324 sizes)
```

### Change Colors
Edit the vector drawables:
- `app/src/main/res/drawable/ic_launcher_background.xml` - Change gradient colors
- `app/src/main/res/drawable/ic_launcher_foreground.xml` - Change element colors

## Play Store Upload

When uploading to Play Store:

1. **Navigate to**: Play Console → Store listing → App icon
2. **Upload**: `app_icon_512.png`
3. **Requirements**:
   - ✓ 512x512 pixels
   - ✓ 32-bit PNG
   - ✓ No transparency (has solid background)
   - ✓ Max 1024KB file size

## Verification Checklist

- [x] Icon builds without errors
- [x] All density variants created
- [x] Adaptive icon configured (Android 8.0+)
- [x] Legacy icon configured (Android 7.1 and below)
- [x] Round icon variant available
- [x] Play Store asset ready (512x512)
- [x] Icon is recognizable at small sizes
- [x] Icon reflects cribbage theme

## Next Steps for Play Store

This icon is ready for submission! See `PLAY_STORE_PUBLISHING_GUIDE.md` for:
- Creating app signing key
- Building release AAB
- Completing Play Store listing
- Uploading screenshots and graphics
- Submitting for review

## Design Rationale

The icon was designed to be:
1. **Instantly Recognizable**: Cards, board, and pegs clearly indicate cribbage
2. **Scalable**: Clean lines and simple shapes work at any size
3. **Thematic**: Green felt and wood evoke traditional card games
4. **Colorful**: Four peg colors add visual interest
5. **Professional**: Suitable for Play Store publication

---

**Files to Keep**:
- `app_icon.svg` - Source file for future edits
- `app_icon_foreground.svg` - Source for foreground layer
- `app_icon_512.png` - Play Store asset

**Files Safe to Delete** (after verifying icon looks good):
- None - All generated files are needed by the app

**DO NOT Delete**:
- Any files in `app/src/main/res/mipmap-*/` directories
- Any files in `app/src/main/res/drawable/` directories

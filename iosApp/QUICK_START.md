# iOS App Quick Start Guide

Follow these simple steps to get the iOS Cribbage app running.

## Prerequisites

- ✅ macOS with Xcode 15+ installed
- ✅ CocoaPods installed (`sudo gem install cocoapods`)
- ✅ Project already has shared Kotlin framework built

## Step-by-Step Setup (5 minutes)

### Step 1: Create Xcode Project

1. Open Terminal and navigate to the iosApp directory:
   ```bash
   cd /Users/brianhenning/projects/android-cribbage/iosApp
   ```

2. Run the automated setup (this will open Xcode for you):
   ```bash
   ./setup_xcode.sh
   ```

3. In Xcode, create a new project with these exact settings:
   - **Template**: iOS → App
   - **Product Name**: `Cribbage`
   - **Organization Identifier**: `com.brianhenning`
   - **Interface**: SwiftUI
   - **Language**: Swift
   - **Storage**: None
   - **Include Tests**: Optional
   - **Location**: `/Users/brianhenning/projects/android-cribbage/iosApp`

4. Click "Create"

### Step 2: Install Dependencies

1. Close the Xcode project if it's open

2. In Terminal (from the iosApp directory), run:
   ```bash
   pod install
   ```

3. This will:
   - Install CocoaPods dependencies
   - Create `Cribbage.xcworkspace`
   - Link the shared Kotlin framework

### Step 3: Add Swift Files

1. Open the workspace (NOT the project):
   ```bash
   open Cribbage.xcworkspace
   ```

2. In Xcode, delete the default `ContentView.swift` and `CribbageApp.swift` files that were generated

3. Add the iOS app files:
   - Right-click on the `Cribbage` folder in the project navigator
   - Select "Add Files to Cribbage..."
   - Navigate to `iosApp/iosApp/` folder
   - Select ALL `.swift` files:
     - `CribbageApp.swift`
     - `ContentView.swift`
     - `GameViewModel.swift`
     - `CardView.swift`
     - `IOSGamePersistence.swift`
   - Make sure "Copy items if needed" is UNCHECKED
   - Make sure "Cribbage" target is CHECKED
   - Click "Add"

### Step 4: Build and Run

1. Select a simulator (e.g., iPhone 15 Pro) from the device dropdown

2. Press `⌘R` (or click the Play button) to build and run

3. The app should launch in the simulator!

## Troubleshooting

### Error: "Module 'shared' not found"

**Solution**: Build the shared framework first:
```bash
cd /Users/brianhenning/projects/android-cribbage
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Then clean and rebuild in Xcode (`⌘⇧K` then `⌘B`)

### Error: "No such module 'shared'"

**Solution**: Check that you opened the `.xcworkspace` file, NOT the `.xcodeproj` file. Close Xcode and run:
```bash
open Cribbage.xcworkspace
```

### Build fails with "Could not find shared.framework"

**Solution**:
1. Make sure CocoaPods installed successfully
2. Check that `Pods/` directory exists in `iosApp/`
3. Run `pod install` again

### Swift files not compiling

**Solution**: Make sure the files are added to the Cribbage target:
1. Select each Swift file in the project navigator
2. Check the "Target Membership" in the File Inspector (right panel)
3. Ensure "Cribbage" is checked

## Alternative: Manual Framework Linking (Without CocoaPods)

If you prefer not to use CocoaPods:

1. In Xcode, select your project in the navigator
2. Select the "Cribbage" target
3. Go to "General" tab
4. Under "Frameworks, Libraries, and Embedded Content", click "+"
5. Click "Add Other..." → "Add Files..."
6. Navigate to: `../shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework`
7. Select "shared.framework" and click "Open"
8. Change "Embed" to "Embed & Sign"

Then in Build Settings:
- Search for "Framework Search Paths"
- Add: `$(PROJECT_DIR)/../shared/build/bin/iosSimulatorArm64/debugFramework`

## Next Steps

Once the app is running:

- **Test the game**: Play a few rounds to verify functionality
- **Customize**: Modify colors, themes, or UI in the Swift files
- **Add features**: Implement iOS-specific features like haptics or widgets

## Need Help?

- Check `README.md` for detailed architecture information
- See `../KMM_MIGRATION.md` for the complete KMM setup guide
- Review `../CLAUDE.md` for development guidelines

---

**Quick Command Reference**:

```bash
# Build shared framework
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Install pods
cd iosApp && pod install

# Open workspace
open Cribbage.xcworkspace

# Or run the automated setup
./setup_xcode.sh
```

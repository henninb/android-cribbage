# iOS Cribbage App

This is the iOS version of the Cribbage app, built using Kotlin Multiplatform Mobile (KMM) to share game logic with the Android version.

## Setup Instructions

### Prerequisites
- macOS with Xcode 15+ installed
- Kotlin Multiplatform Mobile plugin configured
- CocoaPods or Swift Package Manager

### Creating the Xcode Project

1. Open Xcode and create a new iOS App project:
   - File → New → Project
   - Choose "iOS App" template
   - Product Name: `Cribbage`
   - Organization Identifier: `com.brianhenning`
   - Interface: SwiftUI
   - Language: Swift
   - Save location: Use this `iosApp` directory

2. Add the Swift files to your project:
   - Add all `.swift` files from `iosApp/iosApp/` to your Xcode project
   - Ensure they're added to the target

3. Link the shared Kotlin module:

   **Option A: Using CocoaPods**
   - In the project root, create or update `Podfile`:
   ```ruby
   platform :ios, '14.0'

   target 'iosApp' do
     use_frameworks!
     pod 'shared', :path => '../shared'
   end
   ```
   - Run `pod install` from the project root
   - Open the generated `.xcworkspace` file

   **Option B: Using Gradle Task**
   - Run `./gradlew :shared:assembleXCFramework` from project root
   - In Xcode, add the generated XCFramework:
     - Target → General → Frameworks, Libraries, and Embedded Content
     - Add `shared/build/XCFrameworks/debug/shared.xcframework`

4. Configure Build Settings:
   - Set minimum iOS deployment target to 14.0
   - Ensure "Enable Bitcode" is set to "No"

### Running the App

1. Build the shared Kotlin module:
   ```bash
   ./gradlew :shared:compileKotlinIosSimulatorArm64
   ```

2. Open the Xcode project and run on simulator or device

## Architecture

The iOS app uses:
- **SwiftUI** for the UI layer
- **Shared Kotlin module** for all game logic (scoring, AI, game flow)
- **MVVM pattern** with `GameViewModel` observing Kotlin `StateFlow`
- **UserDefaults** for persistence (via `IOSGamePersistence`)

## Project Structure

```
iosApp/
├── iosApp/
│   ├── CribbageApp.swift       # App entry point
│   ├── ContentView.swift       # Main game screen
│   ├── GameViewModel.swift     # ViewModel bridging shared logic
│   ├── CardView.swift          # Card component
│   └── IOSGamePersistence.swift # Persistence adapter
└── README.md
```

## Shared Game Logic

All game rules, scoring, and AI are implemented in the shared Kotlin module:
- Card models and deck creation
- Cribbage scoring (fifteens, pairs, runs, flushes, nobs)
- Pegging phase management
- Opponent AI with strategic card selection
- Game state management with `GameEngine`

## Next Steps

1. Create the Xcode project following the instructions above
2. Test the app on iOS Simulator
3. Customize the UI theme and styling as desired
4. Add any iOS-specific features (haptics, notifications, etc.)

## Troubleshooting

### "Module 'shared' not found"
- Ensure the shared Kotlin module is built for iOS
- Check that the framework is properly linked in Xcode
- Verify the framework search paths in Build Settings

### State updates not reflecting in UI
- Ensure `GameViewModel` is using `@StateObject` in ContentView
- Check that the StateFlow collection task is running
- Verify the `asyncStream` helper is working correctly

### Build errors related to Kotlin types
- Clean and rebuild both the shared module and iOS project
- Check that iOS deployment target matches in both Gradle and Xcode
- Ensure all Kotlin enums are properly bridged to Swift

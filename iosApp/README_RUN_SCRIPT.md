# iOS Run Script

## Quick Start

To build and run the iOS Cribbage app in the simulator:

```bash
cd /Users/brianhenning/projects/android-cribbage/iosApp
./run_simulator.sh
```

## What the Script Does

The `run_simulator.sh` script automates the entire build and launch process:

1. ✅ **Builds Shared Framework**: Compiles the Kotlin Multiplatform shared module
2. ✅ **Builds iOS App**: Compiles the iOS app using xcodebuild
3. ✅ **Finds Simulator**: Locates the configured simulator (iPhone 16 Pro by default)
4. ✅ **Boots Simulator**: Starts the simulator if not already running
5. ✅ **Installs App**: Installs the built app on the simulator
6. ✅ **Launches App**: Opens the Cribbage app automatically

## Configuration

You can customize the script by editing these variables at the top of `run_simulator.sh`:

```bash
SIMULATOR_NAME="iPhone 16 Pro"    # Change to any available simulator
BUNDLE_ID="com.brianhenning.Cribbage"
WORKSPACE="Cribbage.xcworkspace"
SCHEME="Cribbage"
SDK="iphonesimulator"
```

## Available Simulators

To see all available simulators:

```bash
xcrun simctl list devices
```

Common options:
- `iPhone 16 Pro`
- `iPhone 16`
- `iPhone 15 Pro`
- `iPhone SE (3rd generation)`
- `iPad Pro (12.9-inch)`

## Logs and Debugging

Build logs are saved to:
- `/tmp/gradle_build.log` - Gradle/Kotlin build output
- `/tmp/xcode_build.log` - Xcode build output

To view real-time app logs:

```bash
# Get simulator UDID first
xcrun simctl list devices | grep "iPhone 16 Pro"

# Stream logs (replace UDID)
xcrun simctl spawn <UDID> log stream --predicate 'processImagePath contains "Cribbage"'
```

Or use the shortcut the script provides at the end.

## Manual Steps (Alternative)

If you prefer to run steps manually:

```bash
# 1. Build shared framework
cd /Users/brianhenning/projects/android-cribbage
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# 2. Build iOS app
cd iosApp
xcodebuild -workspace Cribbage.xcworkspace \
  -scheme Cribbage \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 16 Pro' \
  build

# 3. Launch simulator
open -a Simulator

# 4. Install and run (replace UDID)
xcrun simctl install <UDID> ~/Library/Developer/Xcode/DerivedData/Cribbage-*/Build/Products/Debug-iphonesimulator/Cribbage.app
xcrun simctl launch <UDID> com.brianhenning.Cribbage
```

## Xcode Alternative

You can also run directly from Xcode:

```bash
cd /Users/brianhenning/projects/android-cribbage/iosApp
open Cribbage.xcworkspace
```

Then press `⌘R` to build and run.

## Troubleshooting

### "Could not find simulator"
- Run `xcrun simctl list devices` to see available simulators
- Update `SIMULATOR_NAME` in the script to match an available device

### "Failed to build shared framework"
- Check `/tmp/gradle_build.log` for errors
- Ensure Java 17 is installed: `java -version`
- Try cleaning: `./gradlew clean`

### "Failed to build iOS app"
- Check `/tmp/xcode_build.log` for errors
- Try cleaning Xcode build: `xcodebuild clean -workspace Cribbage.xcworkspace -scheme Cribbage`
- Delete DerivedData: `rm -rf ~/Library/Developer/Xcode/DerivedData/Cribbage-*`

### "App crashes on launch"
- View logs: `xcrun simctl spawn <UDID> log stream`
- Check if shared framework is properly linked
- Rebuild shared framework: `cd .. && ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`

## Build Times

Typical build times:
- **First build**: 30-60 seconds (compiles everything)
- **Incremental builds**: 10-20 seconds (only changed files)
- **Clean build**: 45-90 seconds

## Requirements

- macOS with Xcode installed
- Xcode Command Line Tools: `xcode-select --install`
- CocoaPods: `gem install cocoapods`
- Java 17: `brew install openjdk@17`
- Gradle 8.14.3+ (included in project)

---

**Note**: This script is designed for development/testing. For production builds, use Xcode's Archive feature or CI/CD pipelines.

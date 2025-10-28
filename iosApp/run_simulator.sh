#!/bin/bash

# iOS Cribbage - Build and Run Script
# This script builds the shared framework, builds the iOS app, and launches it in the simulator

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
SIMULATOR_NAME="iPhone 16 Pro"
BUNDLE_ID="com.brianhenning.Cribbage"
WORKSPACE="Cribbage.xcworkspace"
SCHEME="Cribbage"
SDK="iphonesimulator"

# Function to print colored status messages
print_status() {
    echo -e "${BLUE}==>${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Get script directory and project root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
echo -e "${GREEN}  iOS Cribbage - Build and Run${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
echo ""

# Step 1: Build shared Kotlin framework
print_status "Building shared Kotlin framework..."
cd "$PROJECT_ROOT"
if ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 > /tmp/gradle_build.log 2>&1; then
    print_success "Shared framework built successfully"
else
    print_error "Failed to build shared framework. Check /tmp/gradle_build.log for details"
    exit 1
fi

# Step 2: Build iOS app
print_status "Building iOS app..."
cd "$SCRIPT_DIR"
if xcodebuild -workspace "$WORKSPACE" \
    -scheme "$SCHEME" \
    -sdk "$SDK" \
    -destination "platform=iOS Simulator,name=$SIMULATOR_NAME" \
    -configuration Debug \
    build > /tmp/xcode_build.log 2>&1; then
    print_success "iOS app built successfully"
else
    print_error "Failed to build iOS app. Check /tmp/xcode_build.log for details"
    tail -50 /tmp/xcode_build.log
    exit 1
fi

# Step 3: Get simulator UDID
print_status "Finding simulator..."
SIMULATOR_UDID=$(xcrun simctl list devices | grep "$SIMULATOR_NAME" | grep -v "unavailable" | head -1 | sed -E 's/.*\(([A-F0-9-]+)\).*/\1/')

if [ -z "$SIMULATOR_UDID" ]; then
    print_error "Could not find simulator: $SIMULATOR_NAME"
    print_warning "Available simulators:"
    xcrun simctl list devices | grep "iPhone"
    exit 1
fi

print_success "Found simulator: $SIMULATOR_NAME ($SIMULATOR_UDID)"

# Step 4: Boot simulator if not running
SIMULATOR_STATE=$(xcrun simctl list devices | grep "$SIMULATOR_UDID" | sed -E 's/.*\(([A-Za-z]+)\).*/\1/')
if [ "$SIMULATOR_STATE" != "Booted" ]; then
    print_status "Booting simulator..."
    xcrun simctl boot "$SIMULATOR_UDID"
    print_success "Simulator booted"
    # Give it a moment to fully boot
    sleep 2
else
    print_success "Simulator already running"
fi

# Step 5: Open Simulator app (bring to front)
print_status "Opening Simulator app..."
open -a Simulator

# Step 6: Install app
print_status "Installing app on simulator..."
APP_PATH="$HOME/Library/Developer/Xcode/DerivedData/Cribbage-*/Build/Products/Debug-iphonesimulator/Cribbage.app"
APP_PATH=$(echo $APP_PATH)  # Expand glob

if [ ! -d "$APP_PATH" ]; then
    print_error "Could not find built app at: $APP_PATH"
    print_warning "Looking for app in DerivedData..."
    find "$HOME/Library/Developer/Xcode/DerivedData" -name "Cribbage.app" -type d 2>/dev/null | head -1
    exit 1
fi

if xcrun simctl install "$SIMULATOR_UDID" "$APP_PATH"; then
    print_success "App installed successfully"
else
    print_error "Failed to install app"
    exit 1
fi

# Step 7: Launch app
print_status "Launching Cribbage app..."
if xcrun simctl launch "$SIMULATOR_UDID" "$BUNDLE_ID"; then
    print_success "App launched successfully!"
else
    print_error "Failed to launch app"
    exit 1
fi

echo ""
echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
echo -e "${GREEN}  ✓ Build and launch completed successfully!${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════${NC}"
echo ""
echo "App is now running on: $SIMULATOR_NAME"
echo "Bundle ID: $BUNDLE_ID"
echo ""
echo "To view logs, run:"
echo "  xcrun simctl spawn $SIMULATOR_UDID log stream --predicate 'processImagePath contains \"Cribbage\"'"
echo ""

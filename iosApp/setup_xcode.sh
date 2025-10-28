#!/bin/bash

# Setup script for iOS Cribbage App
# This script helps set up the Xcode project for the iOS app

set -e

echo "🎮 Cribbage iOS Setup Script"
echo "=============================="
echo ""

# Check if CocoaPods is installed
if ! command -v pod &> /dev/null; then
    echo "❌ CocoaPods is not installed."
    echo "   Install it with: sudo gem install cocoapods"
    exit 1
fi

echo "✅ CocoaPods is installed"
echo ""

# Check if Xcode is installed
if ! command -v xcodebuild &> /dev/null; then
    echo "❌ Xcode is not installed."
    echo "   Install it from the Mac App Store"
    exit 1
fi

echo "✅ Xcode is installed ($(xcodebuild -version | head -1))"
echo ""

# Check if the Xcode project exists
if [ ! -d "Cribbage.xcodeproj" ]; then
    echo "📱 Xcode project not found. Creating it now..."
    echo ""
    echo "Please follow these steps in Xcode:"
    echo "1. Open Xcode"
    echo "2. Select 'Create a new Xcode project'"
    echo "3. Choose 'iOS' → 'App'"
    echo "4. Fill in the details:"
    echo "   - Product Name: Cribbage"
    echo "   - Team: (select your team)"
    echo "   - Organization Identifier: com.brianhenning"
    echo "   - Interface: SwiftUI"
    echo "   - Language: Swift"
    echo "5. Save in: $(pwd)"
    echo ""
    echo "After creating the project, run this script again."
    echo ""

    # Open Xcode
    open -a Xcode

    exit 0
fi

echo "✅ Found Xcode project: Cribbage.xcodeproj"
echo ""

# Build the shared Kotlin framework
echo "🔨 Building shared Kotlin framework..."
cd ..
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

if [ $? -ne 0 ]; then
    echo "❌ Failed to build shared framework"
    exit 1
fi

echo "✅ Shared framework built successfully"
echo ""

# Return to iosApp directory
cd iosApp

# Install CocoaPods dependencies
echo "📦 Installing CocoaPods dependencies..."
pod install

if [ $? -ne 0 ]; then
    echo "❌ CocoaPods installation failed"
    exit 1
fi

echo "✅ CocoaPods dependencies installed"
echo ""

# Copy Swift files to the project (if not already there)
echo "📝 Checking Swift files..."

PROJECT_DIR="Cribbage"
if [ -d "$PROJECT_DIR" ]; then
    SWIFT_FILES=(
        "CribbageApp.swift"
        "ContentView.swift"
        "GameViewModel.swift"
        "CardView.swift"
        "IOSGamePersistence.swift"
    )

    for file in "${SWIFT_FILES[@]}"; do
        if [ -f "iosApp/$file" ] && [ ! -f "$PROJECT_DIR/$file" ]; then
            cp "iosApp/$file" "$PROJECT_DIR/"
            echo "  ✅ Copied $file"
        fi
    done
else
    echo "⚠️  Project directory not found. Swift files are in iosApp/iosApp/"
fi

echo ""
echo "🎉 Setup complete!"
echo ""
echo "Next steps:"
echo "1. Open Cribbage.xcworkspace (NOT .xcodeproj):"
echo "   open Cribbage.xcworkspace"
echo ""
echo "2. In Xcode, add the Swift files to your target:"
echo "   - Right-click on the 'Cribbage' folder"
echo "   - Select 'Add Files to Cribbage...'"
echo "   - Select all .swift files from iosApp/iosApp/"
echo ""
echo "3. Build and run the app (⌘R)"
echo ""
echo "If you encounter issues, see iosApp/README.md for troubleshooting."

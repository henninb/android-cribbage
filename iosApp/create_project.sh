#!/bin/bash

# Interactive script to guide through Xcode project creation
# for the Cribbage iOS app

set -e

echo "🎮 Cribbage iOS - Xcode Project Creator"
echo "========================================"
echo ""

# Check if project already exists
if [ -d "Cribbage.xcodeproj" ] || [ -d "Cribbage.xcworkspace" ]; then
    echo "✅ Xcode project already exists!"
    echo ""
    echo "Opening workspace..."
    if [ -f "Cribbage.xcworkspace" ]; then
        open Cribbage.xcworkspace
    else
        open Cribbage.xcodeproj
    fi
    echo ""
    echo "See QUICK_START.md for next steps if you haven't added the Swift files yet."
    exit 0
fi

# Build the framework first
echo "Step 1: Building shared Kotlin framework..."
echo ""
./link_framework.sh

echo ""
echo "✅ Framework is ready!"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 2: Create Xcode Project"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Xcode will open shortly. Please follow these steps:"
echo ""
echo "1. Click 'Create a new Xcode project'"
echo ""
echo "2. Select template:"
echo "   ├─ Platform: iOS"
echo "   └─ Template: App"
echo ""
echo "3. Fill in project details:"
echo "   ├─ Product Name: Cribbage"
echo "   ├─ Team: (select your development team)"
echo "   ├─ Organization Identifier: com.brianhenning"
echo "   ├─ Bundle Identifier: com.brianhenning.Cribbage"
echo "   ├─ Interface: SwiftUI"
echo "   ├─ Language: Swift"
echo "   ├─ Storage: None"
echo "   └─ Include Tests: (your choice)"
echo ""
echo "4. Save location:"
echo "   ⚠️  IMPORTANT: Save in this directory:"
echo "   $(pwd)"
echo ""
echo "5. After creating the project:"
echo "   - Close Xcode"
echo "   - Run this script again to complete setup"
echo ""
echo "Press ENTER to open Xcode..."
read -r

# Open Xcode
open -a Xcode

echo ""
echo "Waiting for you to create the project..."
echo "(This script will check every 5 seconds)"
echo ""

# Wait for project to be created
for i in {1..60}; do
    if [ -d "Cribbage.xcodeproj" ]; then
        echo ""
        echo "✅ Project detected!"
        sleep 2
        break
    fi
    sleep 5
    echo -n "."
done

if [ ! -d "Cribbage.xcodeproj" ]; then
    echo ""
    echo "⏱️  Timeout waiting for project creation."
    echo ""
    echo "If you've created the project, run this script again to continue."
    echo "If not, create the project in Xcode and run: ./create_project.sh"
    exit 0
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 3: Adding Swift Files to Project"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Opening Xcode project..."
open Cribbage.xcodeproj
sleep 3

echo ""
echo "Now in Xcode, please:"
echo ""
echo "1. Delete the default files (select and press Delete):"
echo "   - ContentView.swift"
echo "   - CribbageApp.swift"
echo "   (Choose 'Move to Trash')"
echo ""
echo "2. Add the iOS source files:"
echo "   a. Right-click on 'Cribbage' folder"
echo "   b. Select 'Add Files to Cribbage...'"
echo "   c. Navigate to: iosApp/iosApp/"
echo "   d. Select ALL .swift files (⌘A):"
echo "      • CribbageApp.swift"
echo "      • ContentView.swift"
echo "      • GameViewModel.swift"
echo "      • CardView.swift"
echo "      • IOSGamePersistence.swift"
echo "   e. ⚠️  UNCHECK 'Copy items if needed'"
echo "   f. ✅  CHECK 'Cribbage' target"
echo "   g. Click 'Add'"
echo ""
echo "3. Add the shared framework:"
echo "   a. Select 'Cribbage' project in navigator"
echo "   b. Select 'Cribbage' target"
echo "   c. Go to 'General' tab"
echo "   d. Under 'Frameworks, Libraries, and Embedded Content'"
echo "   e. Click '+' button"
echo "   f. Click 'Add Other...' → 'Add Files...'"
echo "   g. Navigate to:"
echo "      ../shared/build/bin/iosSimulatorArm64/debugFramework/"
echo "   h. Select 'shared.framework'"
echo "   i. Click 'Open'"
echo "   j. Change to 'Embed & Sign'"
echo ""
echo "4. Configure Framework Search Path:"
echo "   a. Select 'Cribbage' project"
echo "   b. Select 'Cribbage' target"
echo "   c. Go to 'Build Settings' tab"
echo "   d. Search for 'Framework Search Paths'"
echo "   e. Double-click to edit"
echo "   f. Click '+' and add:"
echo "      \$(PROJECT_DIR)/../shared/build/bin/iosSimulatorArm64/debugFramework"
echo ""
echo "5. Build and Run (⌘R)"
echo ""
echo "Press ENTER when you've completed these steps..."
read -r

echo ""
echo "🎉 Setup Complete!"
echo ""
echo "Your iOS Cribbage app should now be building in Xcode."
echo ""
echo "If you encounter any issues:"
echo "  - See QUICK_START.md for detailed troubleshooting"
echo "  - Check SETUP_COMPLETE.md for common solutions"
echo "  - Review README.md for architecture details"
echo ""
echo "Happy coding! 🎮"

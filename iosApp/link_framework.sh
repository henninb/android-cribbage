#!/bin/bash

# Script to build and prepare the shared framework for Xcode
# Run this before building in Xcode

set -e

echo "🔨 Building Shared Kotlin Framework for iOS"
echo "==========================================="
echo ""

# Navigate to project root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/.."

# Build the framework for simulator
echo "Building for iOS Simulator (Apple Silicon)..."
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

if [ $? -eq 0 ]; then
    echo "✅ Framework built successfully!"
    echo ""
    echo "Framework location:"
    echo "  shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework"
    echo ""
else
    echo "❌ Framework build failed"
    exit 1
fi

# Build for device (optional)
echo "Would you like to build for iOS device as well? (y/n)"
read -r response
if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
    echo "Building for iOS Device..."
    ./gradlew :shared:linkDebugFrameworkIosArm64
    echo "✅ Device framework built!"
    echo "   Location: shared/build/bin/iosArm64/debugFramework/shared.framework"
fi

echo ""
echo "🎉 Setup complete!"
echo ""
echo "Next steps:"
echo "1. Create Xcode project (if not already done)"
echo "2. Add shared.framework to your project:"
echo "   - In Xcode: Target → General → Frameworks, Libraries, and Embedded Content"
echo "   - Click '+' → Add Other → Add Files"
echo "   - Select: shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework"
echo "   - Choose 'Embed & Sign'"
echo ""
echo "3. Add Framework Search Path in Build Settings:"
echo "   - Search for 'Framework Search Paths'"
echo "   - Add: \$(PROJECT_DIR)/../shared/build/bin/iosSimulatorArm64/debugFramework"
echo ""
echo "4. Add Swift files to your project from: iosApp/iosApp/"
echo ""
echo "5. Build and Run (⌘R)"

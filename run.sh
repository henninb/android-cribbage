#!/bin/sh

set -e

AVD_NAME="Pixel-8a-API-35-x86"
PROXY="192.168.10.40:8081"

# Check if emulator is already running
if ! adb devices | grep -q "emulator"; then
    echo "========================================"
    echo "No emulator detected!"
    echo "========================================"
    echo ""

    # Run diagnostics
    echo "Running diagnostics..."
    echo ""

    # Check KVM
    echo "1. Checking KVM (hardware acceleration):"
    if [ -c /dev/kvm ]; then
        echo "   ✓ /dev/kvm exists"
        if [ -r /dev/kvm ] && [ -w /dev/kvm ]; then
            echo "   ✓ You have read/write access to /dev/kvm"
        else
            echo "   ✗ Permission denied on /dev/kvm"
            echo "   → Run: sudo usermod -aG kvm $USER"
            echo "   → Then log out and log back in"
        fi
    else
        echo "   ✗ /dev/kvm not found"
        echo "   → Check if KVM is available: lsmod | grep kvm"
    fi
    echo ""

    # Check user in kvm group
    echo "2. Checking kvm group membership:"
    if groups | grep -q kvm; then
        echo "   ✓ User is in kvm group"
    else
        echo "   ✗ User is NOT in kvm group"
        echo "   → Run: sudo usermod -aG kvm $USER"
        echo "   → Then log out and log back in"
    fi
    echo ""

    # List available AVDs
    echo "3. Available AVDs:"
    if command -v emulator >/dev/null 2>&1; then
        AVDS=$(emulator -list-avds 2>/dev/null)
        if [ -n "$AVDS" ]; then
            echo "$AVDS" | sed 's/^/   /'
            if echo "$AVDS" | grep -q "$AVD_NAME"; then
                echo "   ✓ $AVD_NAME is available"
            else
                echo "   ✗ $AVD_NAME NOT found"
                echo "   → Create it using Android Studio AVD Manager"
            fi
        else
            echo "   ✗ No AVDs found"
            echo "   → Create an AVD using Android Studio AVD Manager"
        fi
    else
        echo "   ✗ emulator command not found"
        echo "   → Ensure Android SDK tools are in your PATH"
    fi
    echo ""

    echo "========================================"
    echo "To start the emulator:"
    echo "========================================"
    echo ""
    echo "  emulator -avd $AVD_NAME -http-proxy $PROXY"
    echo ""
    echo "Or with writable system (if needed):"
    echo ""
    echo "  emulator -avd $AVD_NAME -writable-system -http-proxy $PROXY"
    echo ""
    echo "Then run this script again."
    echo ""
    exit 1
fi

echo "Emulator detected, waiting for device to be ready..."
adb wait-for-device

echo "Waiting for emulator to fully boot..."
while [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
    echo "  Still booting..."
    sleep 2
done

echo "Emulator is ready!"
sleep 2

echo "Building and installing app..."
./gradlew assembleDebug
./gradlew installDebug

echo "Launching app..."
adb shell am start -n com.brianhenning.cribbage/.MainActivity

echo "App launched successfully!"
exit 0

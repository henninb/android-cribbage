#!/bin/sh

# Script to manually start the lighter emulator with performance optimizations

AVD_NAME="Pixel_4_API_34"
PROXY="192.168.10.40:8081"

echo "========================================"
echo "Starting lighter emulator..."
echo "========================================"
echo ""
echo "AVD: $AVD_NAME"
echo "RAM: 1536 MB"
echo "Cores: 2"
echo "GPU: Software rendering"
echo ""

# Start emulator in background
emulator -avd "$AVD_NAME" \
    -http-proxy "$PROXY" \
    -memory 1536 \
    -cores 2 \
    -gpu swiftshader_indirect \
    -no-snapshot-save \
    > /tmp/emulator.log 2>&1 &

EMULATOR_PID=$!

echo "Emulator starting (PID: $EMULATOR_PID)"
echo "Logs: /tmp/emulator.log"
echo ""
echo "Waiting for emulator to appear..."

# Wait for emulator to appear in adb devices
for i in 1 2 3 4 5 6 7 8 9 10; do
    if adb devices | grep -q "emulator"; then
        echo "✓ Emulator detected!"
        break
    fi
    echo "  Waiting... ($i/10)"
    sleep 2
done

if ! adb devices | grep -q "emulator"; then
    echo "✗ Emulator did not start"
    echo "Check logs: tail -f /tmp/emulator.log"
    exit 1
fi

echo ""
echo "✓ Emulator started successfully!"
echo ""
echo "You can now run: ./run-new.sh"
echo ""

#!/bin/sh

echo emulator -avd Pixel-8a-API-35-x86 -writable-system -http-proxy 192.168.10.40:8081
./gradlew installDebug
adb shell am start -n com.brianhenning.cribbage/.MainActivity

exit 0

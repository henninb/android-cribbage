#!/bin/sh

# Fast bundle build - uses incremental compilation (no clean)
./gradlew bundleRelease --console=plain

exit 0

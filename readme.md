# Cribbage (Android)

An Android Cribbage game built with Kotlin and Jetpack Compose. It includes the
core gameplay loop (deal, select crib, pegging, hand counting) with scoring for
pairs, runs, fifteens, thirty‑one, and last card. The app uses Material 3, Navig
ation, and Compose previews, and ships with unit and instrumented UI tests.

## Features

- Core gameplay: deal, crib selection, pegging phase, and hand scoring.
- Scoring: fifteens, thirty‑one, pairs/three/four of a kind, and runs.
- Modern UI: Jetpack Compose + Material 3 with a bottom navigation scaffold.
- Navigation: type‑safe screen routes via a sealed `Screen` model.
- Testing: JUnit unit tests and AndroidX + Compose instrumented tests.
- Networking scaffolding: Retrofit/OkHttp services for future integrations.

## Tech Stack

- Language: Kotlin (JDK 17, Gradle JVM toolchain 17)
- UI: Jetpack Compose, Material 3, Navigation
- Build: Gradle, Android Gradle Plugin
- Tests: JUnit4, AndroidX Test, Espresso, Compose UI Test
- Networking: Retrofit, OkHttp

## Requirements

- Android Studio Koala or newer
- Java 17 (toolchain configured via Gradle)
- Android SDK: `compileSdk 34`, `targetSdk 33`, `minSdk 24`
- Emulator or device (e.g., Pixel 8a, API 35)

## Quick Start

1) Build all variants (compiles + unit tests + lint):
```
./gradlew clean build
```

If you hit Kotlin metadata checks, add:
```
./gradlew clean build -P android.suppressKotlinVersionCompatibilityCheck=true
```

2) Assemble and install debug build:
```
./gradlew assembleDebug
./gradlew installDebug
```

3) Run on a device/emulator (one‑shot helper):
```
./run.sh
```
This script assembles, installs, and starts `com.brianhenning.cribbage/.MainActi
vity`. Ensure an emulator/device is running (example AVD: Pixel‑8a‑API‑35‑x86).

## Project Structure

```
app/
  src/
    main/
      java/com/brianhenning/cribbage/
        CribbageApplication.kt
        MainActivity.kt
        ui/
          composables/        # Reusable Compose components (e.g., BottomNavBar)
          navigation/         # Sealed Screen routes
          screens/            # First/Second/Third screens (game UI lives here)
          theme/              # Theme, colors, typography
      res/                    # Drawables, strings, themes, XML
      AndroidManifest.xml
    test/java/com/brianhenning/cribbage/         # Unit tests
    androidTest/java/com/brianhenning/cribbage/  # Instrumented/Compose tests
build.gradle (root), settings.gradle, app/build.gradle
```

## Development

- Package: `com.brianhenning.cribbage`
- Style: Kotlin, 4‑space indent, ≤ 120 chars; Android Studio formatter.
- Compose: Material 3 with a `Scaffold` and bottom navigation. Entry point is `M
ainActivity` → `MainScreen()`.
- Logging: tagged with `CribbageGame` for lifecycle and gameplay events.

### Key Files

- `app/src/main/java/com/brianhenning/cribbage/MainActivity.kt`: Hosts Compose c
ontent and navigation.
- `app/src/main/java/com/brianhenning/cribbage/ui/screens/FirstScreen.kt`: Cribb
age gameplay UI and scoring logic.
- `app/src/main/java/com/brianhenning/cribbage/ui/navigation/Screen.kt`: Sealed
routes for bottom navigation.
- `app/src/main/java/com/brianhenning/cribbage/ui/composables/BottomNavBar.kt`:
Bottom navigation component.
- `app/src/main/res/values/strings.xml`: UI strings including cribbage messages.

## Testing

- Unit tests:
```
./gradlew testDebugUnitTest
```

- Instrumented/Compose UI tests (requires a running emulator/device):
```
./gradlew connectedDebugAndroidTest
```

Notable tests:
- `app/src/test/java/com/brianhenning/cribbage/CribbageHandTest.kt`
- `app/src/test/java/com/brianhenning/cribbage/FirstScreenTest.kt`
- `app/src/test/java/com/brianhenning/cribbage/PeggingScorerTest.kt`
- `app/src/test/java/com/brianhenning/cribbage/PeggingRoundManagerTest.kt`
- `app/src/androidTest/java/com/brianhenning/cribbage/FirstScreenComposeTest.kt`

### Pegging Logic and GO Flow

- Core pegging scoring lives in `app/src/main/java/com/brianhenning/cribbage/logic/PeggingScorer.kt` and is unit-tested by `PeggingScorerTest.kt`.
- GO/sub-round resets are modeled by `app/src/main/java/com/brianhenning/cribbage/logic/PeggingRoundManager.kt` with tests in `PeggingRoundManagerTest.kt`.
- `FirstScreen` delegates pegging flow to `PeggingRoundManager` and mirrors state using small helpers.
- Instrumented test `FirstScreenComposeTest.kt` includes a small harness to validate GO→reset behavior.

## Build & Dependency Notes

- Compose compiler: `kotlinCompilerExtensionVersion = 1.5.8`.
- Java 17 toolchain is configured via Gradle.
- If you see Kotlin metadata version issues, the build includes `-Xskip-metadata
-version-check`; you can also add the Gradle property flag shown above.
- PerimeterX SDK is included with Kotlin stdlib excludes to avoid conflicts.

## Troubleshooting

- Kotlin metadata/version mismatch:
  - Try: `./gradlew clean build -P android.suppressKotlinVersionCompatibilityChe
ck=true`
- No devices found / tests hang:
  - Start an emulator (e.g., Pixel 8a API 35) or plug in a device.
- Lint failures blocking release builds:
  - Debug builds run lint but don’t fail the build; fix issues before PRs.

## Contributing

- Follow the repo’s coding style and package conventions.
- Add or update unit/Compose tests for new logic or UI flows.
- Keep PRs focused, include rationale, test results, and screenshots/GIFs for UI
 changes.

## Security

- Do not commit secrets or signing keys.
- Keep local SDK paths in `local.properties`.

## License

This project’s license is not specified in this repository. If you plan to publi
sh or share, add a `LICENSE` file.

# Repository Guidelines

## Project Structure & Module Organization
- App module: `app/` (Android application).
- Source code: `app/src/main/java/com/brianhenning/cribbage`.
- UI/resources: `app/src/main/res` (drawables, strings, themes, XML).
- Unit tests: `app/src/test/java/com/brianhenning/cribbage`.
- Instrumented/Compose tests: `app/src/androidTest/java/com/brianhenning/cribbage`.
- Gradle config: root `build.gradle`, `settings.gradle`, module `app/build.gradle`.

## Build, Test, and Development Commands
- Build all variants: `./gradlew clean build` — compiles, runs unit tests, and lints.
- Assemble debug APK: `./gradlew assembleDebug` — outputs `app/build/outputs/apk/debug/`.
- Install on device/emulator: `./gradlew installDebug`.
- Run unit tests: `./gradlew testDebugUnitTest`.
- Run instrumented tests: `./gradlew connectedDebugAndroidTest` (requires a running emulator/device).
- One‑shot build/install/run: `./run.sh` (installs and starts `MainActivity`).
- If you hit Kotlin metadata checks, add: `./gradlew clean build -P android.suppressKotlinVersionCompatibilityCheck=true`.

## Coding Style & Naming Conventions
- Language: Kotlin (JDK 17, Compose enabled). Use Android Studio formatter.
- Indentation: 4 spaces, no tabs; keep lines ≤ 120 chars.
- Packages: `com.brianhenning.cribbage`. Files end with `.kt` (e.g., `LoginService.kt`).
- Classes/objects: PascalCase; functions/vars: lowerCamelCase; constants: UPPER_SNAKE_CASE.
- Resources: follow existing patterns (e.g., `res/drawable/spades_a.png`, `res/values/strings.xml`).

## Testing Guidelines
- Frameworks: JUnit4 (unit), AndroidX Test + Espresso + Compose UI tests (androidTest).
- Naming: place tests under matching package; files end with `*Test.kt`; use `@Test` methods with clear names.
- Scope: prioritize core game logic (scoring, pegging) and service integrations; add Compose tests for UI behavior.
- Run before PR: `./gradlew testDebugUnitTest connectedDebugAndroidTest` (ensure emulator running).

## Commit & Pull Request Guidelines
- Messages: imperative mood, concise subject (≤ 72 chars), include scope when helpful (e.g., "scoring:"), reference issues (e.g., `#123`). Avoid `wip` on main.
- PRs: describe changes, rationale, and testing; link issues; attach screenshots/GIFs for UI updates; note any API changes.
- Quality gate: build green, tests pass, no new critical lint warnings.

## Security & Configuration Tips
- Do not commit secrets or signing keys. Keep local SDK paths in `local.properties`.
- Requires Android SDK 34+ and Java 17. Emulator example in `run.sh` (Pixel 8a, API 35).

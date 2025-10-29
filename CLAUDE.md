# Android Cribbage App - Development Guidelines

## Build & Run Commands
- Build: `./gradlew build`
- Run/Install: `./gradlew installDebug`
- Clean: `./gradlew clean`
- Lint: `./gradlew lint` (configured with abortOnError = false)
- Unit tests: `./gradlew test`
- Single test: `./gradlew test --tests "com.brianhenning.cribbage.ExampleUnitTest"`
- Instrumented tests: `./gradlew connectedAndroidTest`

## Code Style Guidelines
- **Naming**: camelCase for variables/methods, PascalCase for classes/composables
- **Compose**: Use composable functions with preview when possible
- **Architecture**: MVVM pattern with Compose and Navigation
- **Imports**: Group by Android, Compose, Kotlin (alphabetize within groups)
- **Error Handling**: Use try-catch with specific exceptions, provide fallbacks
- **Line Length**: Maximum 100 characters
- **Indentation**: 4 spaces, no tabs
- **Navigation**: Use sealed classes for routes (see Screen.kt)
- **State Management**: Use remember/mutableStateOf for UI state

## Project Structure
- Package: `com.brianhenning.cribbage`
- Subpackages:
  - `ui.composables`: Reusable UI components
  - `ui.navigation`: Navigation-related classes
  - `ui.screens`: Screen implementations
  - `ui.theme`: Theme definitions

## Current Tech Stack
- **Java Version**: 17 (JVM Toolchain)
- **Gradle**: 8.14.3
- **Android Gradle Plugin**: 8.13.0 (latest stable, supports compileSdk 36)
- **Kotlin**: 2.2.21 with Compose Compiler Plugin
- **compileSdk**: 36
- **minSdk**: 24
- **targetSdk**: 34

## Key Dependencies (Latest Compatible Versions)
- **Jetpack Compose**: BOM 2025.01.00 (Kotlin 2.2+ compatible)
- **AndroidX Core**: 1.17.0 (requires AGP 8.9.1+, compileSdk 36)
- **AndroidX AppCompat**: 1.7.1
- **AndroidX Navigation**: 2.8.6 (fragment-ktx, ui-ktx, compose)
- **Activity Compose**: 1.10.0 (requires compileSdk 35+)
- **Lifecycle ViewModel Compose**: 2.8.7
- **Retrofit**: 2.11.0 (latest 2.x stable)
- **OkHttp**: 4.12.0 (latest 4.x stable)
- **JUnit**: 4.13.2
- **Espresso**: 3.6.1
- **AndroidX Test JUnit**: 1.2.1

## Upgrade Notes
- **AGP 8.13.0 compatibility**: Compatible with Gradle 8.14.3, supports compileSdk 36
- **Java 17 limitation**: Android Gradle Plugin does not support Java 19+; Java 17 is the maximum supported version
- **Kotlin 2.2.21 compatibility**: Fully compatible with Gradle 9.0 (minimum Kotlin 2.0.0 required for Gradle 9.0+)
- **Gradle 9.0 ready**: No deprecation warnings with current configuration
- **Test library resolution**: Uses resolutionStrategy to force latest test library versions (1.2.1 for junit, 3.6.1 for espresso)
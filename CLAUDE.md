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

## Key Dependencies
- Jetpack Compose (BOM 2023.10.01) for modern UI
- AndroidX Navigation for fragment/compose navigation
- Retrofit/OkHttp for network requests
- JUnit for testing
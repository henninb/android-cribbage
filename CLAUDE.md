# Android Project Commands & Guidelines

## Build & Run Commands
- Build: `./gradlew build`
- Run: `./gradlew installDebug`
- Clean: `./gradlew clean`
- Lint: `./gradlew lint`
- Unit tests: `./gradlew test`
- Single test: `./gradlew test --tests "com.bhenning.example.ExampleUnitTest"`
- Instrumented tests: `./gradlew connectedAndroidTest`

## Code Style Guidelines
- **Naming**: camelCase for variables/methods, PascalCase for classes
- **Kotlin**: Use modern Kotlin idioms (trailing lambdas, scope functions)
- **Imports**: Group and organize by type (Android, Kotlin, Java)
- **Error Handling**: Try-catch with specific exceptions
- **Indentation**: 4 spaces, no tabs
- **Line Length**: 100 characters maximum
- **Architecture**: Follow MVVM pattern where possible
- **Logging**: Use Log.i/d/e tags with class names

## Dependencies
- Retrofit/OkHttp for network requests
- AndroidX Navigation for fragment management
- JUnit for testing
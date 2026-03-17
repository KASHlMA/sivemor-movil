# Sivemore Mobile

Greenfield Android app scaffolded in Kotlin with Jetpack Compose, Hilt, Navigation Compose, mocked repositories, unit tests, UI flow tests, and Roborazzi screenshot tests.

## What is implemented

- Native Android app only. No Spring Boot code is included in this phase.
- Single `app` module with package-based feature structure.
- Mocked auth, home, and profile flows wired through repository interfaces.
- Shared design-system layer with theme tokens, reusable cards/buttons, and previews.
- ViewModel unit tests, an instrumentation navigation test, and JVM screenshot regression tests.

## Current limitation

The exact Figma implementation is still blocked by design access:

- The configured Figma MCP entry in this session is unauthenticated.
- Direct access to the provided Figma link returns `403`.
- The app UI in this repo is therefore a production-ready mocked baseline, not a claimed pixel-perfect translation of the Figma file yet.

## Local setup

Install the Android toolchain first:

1. Install JDK 17.
2. Install Android Studio and the Android SDK for API 36.
3. If needed, set `ANDROID_HOME` or `ANDROID_SDK_ROOT`.

Then use the Gradle wrapper:

```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
./gradlew recordRoborazziDebug
```

## Project shape

- `app/src/main/java/com/sivemore/mobile/app` for theme, navigation, and DI.
- `app/src/main/java/com/sivemore/mobile/feature` for feature-owned UI state, view models, and screens.
- `app/src/main/java/com/sivemore/mobile/domain` for models and repository contracts.
- `app/src/main/java/com/sivemore/mobile/data` for fake implementations and typed fixtures.

## To finish the Figma build

1. Enable authenticated Figma MCP access in Codex.
2. Restart Codex so the MCP tools are available.
3. Pull `get_metadata`, `get_design_context`, `get_variable_defs`, and `get_screenshot` for the file `o34HWz5ADzK63ASPGSbpUn`, root node `1-2`.
4. Replace the provisional mocked screens with the real screen inventory and Figma assets.

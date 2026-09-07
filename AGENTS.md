# Agent Guide: Álcool ou Gasolina Android App

This document provides essential information for LLM agents working on the Álcool ou Gasolina Android application codebase.

## 1. Project Overview

Android app that helps users calculate and decide between Ethanol and Gasoline based on price and fuel efficiency, storing calculation history and receiving push notifications.

**Key Technologies:**

*   **Language:** Kotlin (Java 17/21 source/target)
*   **Build:** AGP, Gradle Kotlin DSL, Version Catalog (`gradle/libs.versions.toml`); `compileSdk`/`targetSdk` 37, `minSdk` 23
*   **UI:** Jetpack Compose (BOM), Material 3, Navigation Compose
*   **Architecture:** MVVM with Unidirectional Data Flow (UDF): `StateFlow` for state + `SharedFlow` for one-shot events
*   **Local Storage:** Room (KSP, Flow/Coroutines) + SharedPreferences (`PreferencesRepository` in `data/repository/PreferencesRepository.kt`)
*   **Dependency Injection:** Koin
*   **Async:** Kotlin Coroutines & Flow
*   **Push:** Firebase Cloud Messaging (`MyFirebaseMessagingService`)
*   **Analytics/Crash:** Firebase Analytics, Firebase Crashlytics
*   **Ads:** AdMob (Banner, Interstitial, App Open Ads)
*   **Testing:** JUnit4, MockK, Robolectric, Turbine
*   **Code Quality:** ktlint (via `org.jlleitschuh.gradle.ktlint`)

## 2. Project Structure

Single-Activity app. `MainActivity.setContent { MainScreen(...) }` (or `AppNavHost(...)`).

Source structure under `app/src/main/java/br/com/gazoza/alcoolougasolina/`:
*   **`application/`**: `CustomApplication.kt` (Koin startup, Firebase & AdMob init).
*   **`data/`**: Room database, DAO (`ComparisonDao.kt`).
*   **`data/repository/`**: Repositories (`PreferencesRepository`, `HistoryRepository`).
*   **`domain/`**: Entity models (`Comparison.kt`).
*   **`di/`**: Koin modules (`AppModule`, `LocalModule`, `RepositoryModule`, `ViewModelModule`).
*   **`features/`**: Feature-based packages:
    *   `start/`: Splash / Start screen (`StartViewModel`, `StartScreen`).
    *   `main/`: Calculator screen (`MainViewModel`, `MainScreen`, `MainState`).
    *   `history/`: Calculation history (`HistoryViewModel`, `HistoryScreen`, `HistoryState`).
    *   `notifications/`: Notifications list & detail (`NotificationsViewModel`, `NotificationsScreen`).
*   **`navigation/`**: `AppNavHost.kt`, routes definition (`Routes`).
*   **`ui/components/`**: Reusable Compose UI components (`BannerAd`, `TopBar`, etc.).
*   **`ui/theme/`**: Jetpack Compose theme (`Color.kt`, `Theme.kt`, `Type.kt`).
*   **`service/`**: `MyFirebaseMessagingService.kt`.
*   **`util/`**: `Utils`, `MaskMoney`, `Constants`, `AppOpenManager`.

## 3. Creating a New Feature / State Management Pattern

Features live under `app/src/main/java/br/com/gazoza/alcoolougasolina/features/<feature>/`:

*   **`<Feature>ViewModel.kt`**: Holds state (`MutableStateFlow<XxxUiState>` exposed as `StateFlow`), one-shot events (`MutableSharedFlow<XxxEvent>`), and user action functions.
*   **`<Feature>State.kt`**: `data class XxxUiState(...)` + `sealed class XxxEvent`.
*   **`<Feature>Screen.kt`**: The main `@Composable`; collects `uiState`/`events` and renders UI.
*   **`components/` (optional)**: Feature-specific Composables.

**State Management Conventions:**
- UI state lives in ViewModel as a single `StateFlow<XxxUiState>`.
- One-shot side-effects (navigation, toast/snackbar) emitted via `SharedFlow<XxxEvent>`.
- State mutation with `_uiState.update { it.copy(...) }`.

## 4. Dependency Injection (Koin)

Modules live in `app/src/main/java/br/com/gazoza/alcoolougasolina/di/`:

*   **`AppModule.kt`**: App-wide dependencies.
*   **`LocalModule.kt`**: Room database, DAOs.
*   **`RepositoryModule.kt`**: `PreferencesRepository`, `HistoryRepository`.
*   **`ViewModelModule.kt`**: `viewModelOf(::XxxViewModel)` declarations.

## 5. Build & Test Commands

Run with `.\gradlew.bat` on Windows / `./gradlew` elsewhere:

```bash
# Compile Kotlin code
./gradlew compileDebugKotlin

# Run unit tests
./gradlew testDebugUnitTest

# Force re-run unit tests
./gradlew testDebugUnitTest --rerun-tasks

# Check code style with ktlint
./gradlew ktlintCheck

# Auto-format code with ktlint
./gradlew ktlintFormat

# Assemble debug APK
./gradlew :app:assembleDebug

# Bundle release
./gradlew :app:bundleRelease
```

## 6. Always-Rules

*   *Dependencies:* All dependencies are managed in `gradle/libs.versions.toml`. Use aliases in `build.gradle.kts`.
*   *Compile verification:* After changes, verify with `./gradlew compileDebugKotlin`.
*   *Code Style:* Run `./gradlew ktlintCheck` (and `ktlintFormat` if needed) before submitting.
*   *Commits:* Follow Conventional Commits specification.

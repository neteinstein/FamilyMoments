# Family Moments – Copilot Agent Instructions

## Project Overview

**Family Moments** is an Android app that helps families trigger meaningful conversations by presenting randomised questions at the table or in other family settings.

**Package:** `org.neteinstein.family`
**Platform:** Android (minSdk 30, targetSdk 35)
**Language:** Kotlin

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM + Clean Architecture |
| DI | Koin 4.x |
| Async | Kotlin Coroutines + Flow |
| Build | Kotlin DSL Gradle + Version Catalog (`gradle/libs.versions.toml`) |
| Navigation | Jetpack Navigation Compose |
| Testing | JUnit 4, MockK, kotlinx-coroutines-test |
| CI/CD | GitHub Actions |

---

## Module Structure

```
FamilyMoments/
├── app/                   # Entry point, DI wiring, Navigation, MainActivity
├── core/
│   ├── domain/            # Pure Kotlin: models, repository interfaces, use cases
│   ├── data/              # Repository implementations, data sources
│   └── ui/                # Shared Compose theme (colors, typography, Theme.kt)
└── feature/
    ├── splash/            # Splash screen with animated logo
    ├── home/              # Question card with swipe gesture + HomeViewModel
    └── settings/          # Settings screen (language, about)
```

### Dependency rules (strict)
- `feature/*` → `core:domain`, `core:ui` (never cross-feature imports)
- `core:data` → `core:domain`
- `core:ui` → Compose / Material3 only
- `app` → all modules (wires everything together)

---

## Architecture Principles

### Clean Architecture layers
1. **Domain** – business logic only, zero Android dependencies (`core:domain`)
2. **Data** – implements domain interfaces, provides data (`core:data`)
3. **Presentation** – ViewModels + Compose screens (`feature/*`)

### MVVM in features
- Every screen has a corresponding `ViewModel`
- ViewModels expose `StateFlow<UiState>` – sealed class or data class
- Screens collect state with `collectAsState()` / `collectAsStateWithLifecycle()`
- No business logic in Composables

### Use Cases
- One responsibility per use case
- Named as verb + noun: `GetRandomQuestionUseCase`, `GetQuestionsUseCase`
- Injected into ViewModels via Koin

---

## Code Style & Conventions

- **Kotlin** idiomatic style (no Java-style code)
- `data class` for models and UI states
- `sealed class` or `sealed interface` for navigation routes and UI events
- No `lateinit var` in ViewModels – use `StateFlow` / `SharedFlow`
- `@Composable` functions start with an uppercase noun (e.g. `HomeScreen`, `QuestionCard`)
- Private composable helpers use `private fun` and lowercase starting letter is fine for lambdas
- String resources go in `res/values/strings.xml` – no hardcoded UI strings

---

## Testing Expectations

### Unit tests (required)
- All **ViewModels** must have unit tests covering happy path, edge cases, and error states
- All **Use Cases** must have unit tests
- All **Repository implementations** must have unit tests
- Use `StandardTestDispatcher` and `runTest` for coroutine tests
- Use `MockK` (`mockk()`, `coEvery`, `coVerify`) for mocking

### Coverage targets
- Minimum **80% line coverage** for `core:domain` and `core:data`
- Minimum **70% line coverage** for `feature:home`
- CI runs `jacocoTestReport` and uploads results to Codecov

### Test naming convention
```kotlin
@Test
fun `methodName does something when condition`() { ... }
```

---

## CI/CD

Three parallel jobs on every PR targeting `main` or `develop`:

| Job | Command | Artifact |
|---|---|---|
| **Compile** | `./gradlew assembleDebug` | – |
| **Unit Tests** | `./gradlew testDebugUnitTest` | XML test results |
| **Coverage** | `./gradlew testDebugUnitTest jacocoTestReport` | HTML + XML reports |

All jobs must pass before merging.

---

## Theming

- Light and dark schemes defined in `core/ui` (`Theme.kt`, `Color.kt`)
- Material3 dynamic colour enabled on Android 12+ (API 31+)
- Static fallback colour scheme for API 30
- `FamilyMomentsTheme` wraps entire app in `MainActivity`

---

## Animations

- Splash: scale + fade-in logo, then text fade-in
- Home card: `AnimatedContent` with slide + fade transitions on swipe
- Card rotation follows drag offset (`animateFloatAsState`)
- Progress dots resize with `animateFloatAsState`

---

## Adding a New Feature

1. Create a new module under `feature/` following existing module structure
2. Add `build.gradle.kts` following `feature/home/build.gradle.kts` as template
3. Register it in `settings.gradle.kts` and `app/build.gradle.kts`
4. Add a Koin module and include it in `AppModule.kt`
5. Add a new `Screen` in `navigation/Screen.kt` and a composable in `AppNavigation.kt`
6. Write unit tests before opening a PR

---

## Common Pitfalls to Avoid

- Do **not** call `rememberCoroutineScope()` in ViewModels – use `viewModelScope`
- Do **not** pass `Context` into ViewModels – use `AndroidViewModel` only if strictly necessary
- Do **not** collect Flows inside `LaunchedEffect` – prefer `collectAsStateWithLifecycle()`
- Do **not** hardcode colours in Composables – always use `MaterialTheme.colorScheme.*`
- Do **not** import cross-feature dependencies (e.g. `feature:home` must not import `feature:settings`)
- Always run `./gradlew testDebugUnitTest` locally before pushing

---

## Custom Agents

This repo also defines four specialized custom agents — Developer, QA, Architect, Security Manager — in `.github/agents/*.agent.md`, mirrored for Claude Code in `.claude/agents/`. Architect and Security Manager can delegate to Developer and QA via the `agent` tool / `handoffs`. See [`docs/agents/README.md`](../docs/agents/README.md) for the full model.

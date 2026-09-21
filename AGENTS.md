# AGENTS.md

This file provides guidance to AI coding agents (Claude Code and others) when working with code in this repository.

## Project Overview

Family Moments is an Android app that helps families spark meaningful conversations by presenting randomized, swipeable conversation-starter questions in categories like Ice Breakers, Memories, Values, Future Dreams, and Daily Life.

- **Package:** `org.neteinstein.family`
- **Language:** Kotlin, UI in Jetpack Compose + Material3
- **minSdk 32 / targetSdk 37 / compileSdk 37** (see `app/build.gradle.kts` and per-module `build.gradle.kts`)

## Commands

```bash
./gradlew assembleDebug                  # compile (mirrors CI "Compile" job)
./gradlew testGithubDebugUnitTest testPlaystoreDebugUnitTest testAndroidHostTest  # run all unit tests (mirrors CI "Unit Tests" job)
./gradlew createGithubDebugUnitTestCoverageReport testAndroidHostTest koverXmlReport  # coverage: AGP for androidApp, Kover for every KMP module (mirrors CI "Code Coverage" job)
./gradlew assembleRelease && ./scripts/verify-obfuscation.sh  # build + check the minified/obfuscated release (mirrors CI "Minified Release" job)
```

> **KMP migration note:** modules already converted to Kotlin Multiplatform (`core:domain`,
> `core:data`, `core:ui`, `feature:splash`, `feature:settings`, `feature:home`, `app`) run their
> JVM/Android unit tests (the `androidHostTest` source set) under `testAndroidHostTest`. `androidApp`
> (the one module not yet converted to KMP) builds two product flavors (see Releases below), so its
> unit test/coverage tasks are flavor-scoped - `testGithubDebugUnitTest`/`testPlaystoreDebugUnitTest`
> and `createGithubDebugUnitTestCoverageReport`/`createPlaystoreDebugUnitTestCoverageReport` - the
> plain `testDebugUnitTest`/`createDebugUnitTestCoverageReport` names are ambiguous now that no
> unflavored module exposes them directly (Gradle's task-name abbreviation matching treats the
> plain name as matching both flavor-scoped tasks instead of resolving one exactly). CI pins to
> `github` for coverage. `enableUnitTestCoverage = true` and `MainActivityViewModelTest.kt` were
> added to `androidApp` specifically so `createGithubDebugUnitTestCoverageReport` has both a task to
> run and real coverage data to report on (AGP hard-fails that task, rather than producing an empty
> report, if a module enables coverage but has zero unit tests).
>
> **Kover for the KMP modules:** AGP's classic `enableUnitTestCoverage` has no equivalent for the
> KMP android-library plugin (`com.android.kotlin.multiplatform.library`) that `core:domain`,
> `core:data`, `core:ui`, `feature:splash`, `feature:home`, `feature:settings`, and `app` all use -
> confirmed against the plugin's actual DSL, not assumed. `kotlinx-kover` (`libs.plugins.kover`,
> applied to each of those seven modules plus the root project) covers them instead: the root
> `build.gradle.kts` applies the plugin for real (not `apply false` like every other root-declared
> plugin) since it's the Kover "merging module" - its `dependencies { kover(project(...)) }` block
> lists all seven, and `./gradlew koverXmlReport` produces one aggregated XML report at
> `build/reports/kover/report.xml`. `androidApp` and `webApp` (the latter has no tests yet) aren't
> included - `androidApp` keeps its own separate AGP-native mechanism above rather than running two
> coverage tools for one module. CI runs `testAndroidHostTest` explicitly alongside
> `koverXmlReport` rather than relying on the report task's own dependencies, to sidestep a known
> AGP 9.x issue where Kover's report tasks can end up with no real dependency on the test tasks
> that generate their input (Kotlin/kotlinx-kover#785).

Run tests for a single module:
```bash
./gradlew :androidApp:testGithubDebugUnitTest  # not yet KMP-converted
./gradlew :feature:home:testAndroidHostTest    # KMP-converted
```

Run a single test class or method (`--tests` works with any of the module targets above):
```bash
./gradlew :feature:home:testAndroidHostTest --tests "org.neteinstein.family.feature.home.HomeViewModelTest"
./gradlew :feature:home:testAndroidHostTest --tests "*.HomeViewModelTest.nextQuestion advances to next question"
```

`./gradlew ktlintCheck` runs the [ktlint Gradle plugin](https://github.com/JLLeitschuh/ktlint-gradle) (applied per-module, configured in each module's `build.gradle.kts` + root `.editorconfig`) — the formatting/style gate CI relies on. Run `./gradlew ktlintFormat` to auto-fix violations.

CI (`.github/workflows/pr.yml`) runs seven independent jobs on every PR into `main`/`develop`: `ktlint`, `assembleDebug`, `assembleRelease` + `scripts/verify-obfuscation.sh` (see [Obfuscation and shrinking](#obfuscation-and-shrinking) — `assembleDebug` never runs R8, so this is what catches a broken keep rule), `testGithubDebugUnitTest testPlaystoreDebugUnitTest testAndroidHostTest`, `createGithubDebugUnitTestCoverageReport testAndroidHostTest koverXmlReport` (both reports uploaded to Codecov in the same job), `:webApp:wasmJsBrowserDistribution` (Compile Web), and `iosSimulatorArm64Test` on a `macos-latest` runner (Compile iOS — needs the Kotlin/Native/Xcode toolchain; run unscoped so Gradle resolves it against every KMP module, which compiles each one's `commonMain`/`iosMain` for the simulator target even though most existing tests are MockK-based and can't run there). Coverage is two separate mechanisms, not one: AGP's built-in `enableUnitTestCoverage = true` (`androidApp` only, a classic android-application module) and `kotlinx-kover` (every `core:*`/`feature:*`/`app` module, all KMP android-library modules that have no `enableUnitTestCoverage` equivalent) — see the KMP migration note above for why they can't share one mechanism.

## Releases

`androidApp` builds two product flavors under the `distribution` flavor dimension (`androidApp/build.gradle.kts`): **`github`**, the direct-APK build distributed via GitHub Releases, and **`playstore`**, submitted to the Play Store. `assembleRelease`/`assembleDebug` are aggregate tasks that build both flavors; `testDebugUnitTest`/`createDebugUnitTestCoverageReport` are ambiguous for `androidApp` for that same reason (see the KMP migration note above) - use `assembleGithubRelease`/`assemblePlaystoreRelease`/`testGithubDebugUnitTest`/etc. to target one flavor, or both flavor-scoped task names together to cover both.

Pushing to `main` (or a manual `workflow_dispatch`) triggers `.github/workflows/release.yml`, which runs `ktlintCheck` + `testGithubDebugUnitTest testPlaystoreDebugUnitTest testAndroidHostTest`, builds signed `assembleRelease` APKs for both flavors plus the `playstore` flavor's `bundlePlaystoreRelease` App Bundle, and publishes all three as assets on a single GitHub Release tagged `v1.0.<run number>`. Signing requires four repo secrets: `KEYSTORE_BASE64` (base64-encoded `.jks`/`.keystore` file), `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. The workflow fails fast if any are missing. `app/build.gradle.kts`'s `versionCode`/`versionName` and `signingConfigs["release"]` read `APP_VERSION_CODE`/`APP_VERSION_NAME`/`KEYSTORE_FILE`/`KEYSTORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD` env vars set by the workflow, falling back to debug signing and static defaults for local builds. The `playstore` APK is uploaded to the GitHub Release alongside the `github` one purely for archival; the `playstore` AAB is the one that actually matters for Play Console, whether uploaded there by hand or by the automated step below.

After the GitHub Release step, the workflow also runs `publishPlaystoreReleaseBundle` (from the [Gradle Play Publisher](https://github.com/Triple-T/gradle-play-publisher) plugin, `com.github.triplet.play` — applied in `app/build.gradle.kts`) to upload the `playstore` flavor's signed release App Bundle straight to the Play Console via the Google Play Developer API. This step only runs if the `ANDROID_PUBLISHER_CREDENTIALS` repo secret is set (the raw JSON contents of a Play Console service account key with "Release to production, exclude devices, and use Play App Signing" — or at least test-track — permission for this app); if it's unset, the workflow logs a notice and skips the step instead of failing, so forks/clones without Play Console access still get a GitHub Release. Publishes go to the `internal` track by default, overridable via the `PLAY_TRACK` repo variable, so a release never reaches production without an explicit promotion in the Play Console. `app/build.gradle.kts`'s `play { }` block is disabled (`enabled.set(false)`) by default and re-enabled only for the `playstore` flavor via `playConfigs` — the `github` flavor shares the same `applicationId` and must never be uploaded. Note: the Play Developer API can only publish updates to an app that already has at least one release uploaded manually through the Play Console; do that first before this automation will work — download the `.aab` asset from the GitHub Release (built regardless of whether `ANDROID_PUBLISHER_CREDENTIALS` is set) and upload it by hand under Play Console → your app → Production/Testing → Create release for that one-time first upload; every release after that is handled automatically by this step.

**Troubleshooting a `403 PERMISSION_DENIED` from `publishPlaystoreReleaseBundle`:** the request that fails is `POST .../applications/<applicationId>/edits` (the very first call Gradle Play Publisher makes, before it touches any track), so the cause is always the service account's standing in the Play Console, not this repo's Gradle config. Check, in order: (1) the service account (the `client_email` inside the `ANDROID_PUBLISHER_CREDENTIALS` JSON) has been invited as a user under Play Console → Users and permissions, with access to *this specific app* (not just "all apps" from a different app list) and the "Release to production, exclude devices, and use Play App Signing" permission (or at least a testing-track release permission); (2) a first release for `applicationId` (`app/build.gradle.kts`) has been uploaded manually through the Play Console — the API can only publish updates, never the initial listing; (3) the Google Play Android Developer API is enabled on the Google Cloud project the service account key belongs to; (4) a newly-granted permission can take a few hours to propagate on Google's side, so a re-run after a short wait can resolve it with no config change at all.

### Store listing metadata

`fastlane/metadata/android/<locale>/` holds the Play Store listing copy and graphics (`title.txt`,
`short_description.txt`, `full_description.txt`, `changelogs/<versionCode>.txt`,
`images/icon.png`, `images/featureGraphic.png`, `images/phoneScreenshots/`) in the standard
[fastlane `supply` metadata layout](https://docs.fastlane.tools/actions/supply/#images-and-metadata),
covering `en-US`, `pt-BR`, `es-ES`, `fr-FR`, and `de-DE`. This is edited by hand today; the
Gradle Play Publisher plugin above reads its own, differently-structured `play/` metadata
directory (see [its docs](https://github.com/Triple-T/gradle-play-publisher#directory-structure)),
so populating `app/src/playstoreRelease/play/` from this folder — by script or by hand — is a
prerequisite before listing changes here reach the Play Console automatically.
`scripts/generate_store_assets.py` regenerates `images/icon.png` and `images/featureGraphic.png`
straight from the launcher icon's vector paths and brand colors (`app/src/main/res/drawable/
ic_launcher_foreground.xml`, `values/colors.xml`) — re-run it after either changes. The Play
Console's required Privacy Policy URL should point at [`PRIVACY_POLICY.md`](PRIVACY_POLICY.md) in
this repo; a contact email still needs to be added under Play Console → Store settings, since none
exists in this repo to source one from.

The `github` flavor keeps an in-app self-update flow: the Settings screen's "Update to latest" button (`feature:settings`'s `SettingsViewModel`/`SettingsScreen`) checks `https://api.github.com/repos/neteinstein/FamilyMoments/releases/latest` (`core:data`'s `GitHubUpdateRepositoryImpl`), compares the tag against the installed `versionName` (`core:domain`'s `isNewerVersion`), and downloads/installs the APK asset via `AppUpdateInstallerImpl` (a `FileProvider`-backed install flow gated by the `REQUEST_INSTALL_PACKAGES` permission, the `FileProvider`, and `UpdateApkCleanupReceiver` — all declared only in `app/src/github/AndroidManifest.xml`, merged in for that flavor only). The `playstore` flavor has this feature stripped entirely: `app/build.gradle.kts` sets `BuildConfig.UPDATES_ENABLED = false` for it, wired via Koin as a named `"updatesEnabled"` boolean (`app/.../di/AppModule.kt` → `feature/settings/.../di/SettingsModule.kt`) into `SettingsViewModel`, which skips the update check entirely and drives `SettingsUiState.updatesEnabled = false` so `SettingsScreen` never renders the "Updates" section; the Play Store flavor's manifest carries none of the permission/provider/receiver above since they're only declared in the `github` source set.

### Obfuscation and shrinking

The `release` build type (`app/build.gradle.kts`) is minified, obfuscated **and** resource-shrunk for both flavors — `isMinifyEnabled = true` + `isShrinkResources = true`, with R8 in full mode (`android.enableR8.fullMode=true` in `gradle.properties`). `debug` is untouched, so `assembleDebug` and every unit test run against unshrunk code; nothing about R8 is exercised by the debug pipeline.

`app/proguard-rules.pro` deliberately keeps almost nothing. Everything this app resolves through ordinary Kotlin calls — Koin's `single { }`/`get()`/`by inject()` DSL (which captures `KClass` literals at compile time), use cases, ViewModels, domain models, Compose UI — is renamed consistently by R8 at both the definition and the call site, so keeping it only inflates the APK and weakens the obfuscation. **Only add a `-keep` for something reached by name at runtime**, and say in a comment what resolves it:

- **Room** is the one such case in this codebase. `Room.databaseBuilder(...)` (`core/data/.../data/di/DataModule.kt`) resolves the KSP-generated `FamilyMomentsDatabase_Impl` with `Class.forName` on the runtime name of the class it is handed + `"_Impl"`, so `-keep class * extends androidx.room.RoomDatabase { <init>(); }` pins both halves. Renaming either independently is a runtime crash, not a build error.
- **Manifest-declared components** (`MainActivity`, `FamilyMomentsApp`, `UpdateApkCleanupReceiver`, the `FileProvider`) need no rule here — AGP generates keep rules from the merged manifest.
- **Resource shrinking** is safe only because nothing looks a resource up dynamically; every reference is a static `R.*` one. Adding a `Resources.getIdentifier` call anywhere means either a `tools:keep` entry or turning `isShrinkResources` back off.

`scripts/verify-obfuscation.sh` reads R8's `mapping.txt` for each release variant and fails if no `org.neteinstein.family.*` class was renamed (i.e. obfuscation silently stopped happening) or if either Room name above lost its identity. The "Minified Release" PR job and the release workflow both run it — an over-broad `-keep` or a flipped `isMinifyEnabled` therefore fails CI rather than shipping.

Obfuscated stack traces need the matching mapping file, and R8 emits a different one per build. `-keepattributes SourceFile,LineNumberTable` + `-renamesourcefileattribute SourceFile` keep traces line-accurate without shipping the original `.kt` filenames, and `.github/workflows/release.yml` attaches `*-mapping.txt` for both flavors to every GitHub Release (the `.aab` needs no asset — AGP embeds its mapping in the bundle, so the Play Console de-obfuscates that flavor itself). Run a trace through R8's `retrace` with the mapping from the exact release the crash came from.

`.github/dependabot.yml` runs weekly `gradle` and `github-actions` update checks.

## Firebase Analytics

Firebase Analytics runs on **Android (both flavors) and Web**; iOS binds a no-op. The seam is
`core:domain`'s `AnalyticsTracker` interface, implemented per platform in `core:data` and bound
through `expect val platformAnalyticsModule` - the same shape `platformUpdateModule` already uses
for the Android-only self-update feature.

```
core:domain   AnalyticsTracker, AnalyticsEvents/Params/Screens/UserProperties (the name catalog),
              AnalyticsPreferenceRepository, Get/Set/ObserveAnalyticsEnabledUseCase
core:data     AnalyticsPreferenceRepositoryImpl (KeyValueStore-backed, defaults true)
              ConsentAwareAnalyticsTracker  <- what Koin binds as AnalyticsTracker
              platformAnalyticsModule: androidMain -> FirebaseAnalyticsTracker
                                       wasmJsMain  -> FirebaseWebAnalyticsTracker
                                       iosMain     -> NoOpAnalyticsTracker
```

**Adding an event** means adding its name to `AnalyticsEvents` (and any new parameter to
`AnalyticsParams`) and calling `analyticsTracker.logEvent(...)` from a ViewModel. `core:domain`'s
`AnalyticsEventsTest` enforces Firebase's naming rules over that catalog - at most 40 characters of
`[a-z0-9_]`, starting with a letter, no `firebase_`/`google_`/`ga_` prefix - so a name Firebase
would silently drop fails the build instead. Treat the constants as a published schema: renaming
one starts a new series in the console and orphans the old one.

Never call the tracker from a composable when a ViewModel can carry the call. Shuffle, the
grid/swipe toggle and opening a card full screen are pure `HomeScreen` state, and are still routed
through `HomeViewModel.onShuffleClicked`/`onViewModeToggled`/`onQuestionExpanded` so they stay
unit-testable. The two exceptions have no ViewModel seam: `core:ui`'s `InstallAppBanner` (which
also can't import the catalog - `core:ui` depends on nothing but Compose) reports through an
`onBannerAction: (String) -> Unit` callback that `App()` forwards to the tracker.

Screen views come from one place: `App.kt` collects `navController.currentBackStackEntryFlow` and
maps the three routes in `Screen.kt`. The grid and the full-screen card aren't destinations, so
they report themselves via `HomeViewModel` (`AnalyticsScreens.HOME_GRID`/`QUESTION_FULLSCREEN`).

**No user identifier exists anywhere.** There is no account, and the app deliberately never calls
`setUserId` and never generates or stores an install ID - per-user metrics come from Firebase's own
app-instance ID. `AnalyticsUserProperties` carries coarse dimensions only, and
`hidden_cards_bucket` is bucketed (`0`/`1_10`/`11_50`/`51_plus`) rather than a raw count precisely
so it can't become a near-unique per-device value.

**Consent is opt-out.** `ConsentAwareAnalyticsTracker` wraps the platform tracker: it drops calls
while the user is opted out *and* pushes `setCollectionEnabled` down to the SDK, which is the half
that actually stops network traffic (dropping calls upstream wouldn't stop Firebase's own
automatic `session_start`/`user_engagement`/`first_open`). It also emits the opt-in/opt-out events
itself, because their ordering around the switch is what makes them recordable at all - an
opt-*out* event logged after collection stops never leaves the device. Settings' "Share usage data"
switch drives it via `SettingsViewModel.onAnalyticsEnabledChanged`.

### Configuration, and what it actually protects

| Artifact | Local dev | CI |
|---|---|---|
| `androidApp/google-services.json` | drop it in by hand from the Firebase console | `GOOGLE_SERVICES_JSON` secret, base64, decoded by the workflow |
| `webApp/firebase-web-config.json` | the console's `firebaseConfig` object saved as JSON | `FIREBASE_WEB_CONFIG` secret, raw JSON, read from the env by Gradle |

Both are git-ignored and **both are optional at build time**. Missing Android config means
`androidApp/build.gradle.kts` never applies the google-services plugin (it hard-fails on a missing
file, which would break every fork), no `google_app_id` resource is generated, and
`FirebaseAnalyticsTracker` no-ops. Missing web config means `generateFirebaseWebInit` emits a stub
that leaves `window.__familyMomentsAnalytics` null, and `FirebaseWebAnalyticsTracker` no-ops.
`pr.yml` therefore treats the secret as optional - a fork PR can't read secrets at all - while
`release.yml` requires it in its fail-fast list, because a release that silently ships without
analytics looks healthy and reports nothing for the life of that version.

These values are **client identifiers, not secrets**: they ship inside every APK and inside the JS
bundle, and anyone can extract them. Keeping them out of git avoids secret-scanner noise and keeps
forks out of this Firebase project, but it buys hygiene, not confidentiality. What actually
protects the project is restricting both API keys in Google Cloud Console → Credentials - the
Android key to `org.neteinstein.family` + the release SHA-1, the browser key to HTTP referrers on
the deployed domains - plus Firebase App Check if a backend product (Firestore/Storage/Functions)
is ever added. A service-account key must never go anywhere near client code.

### The Web bridge

Kotlin/Wasm has no `@JsModule` support and this repo has no npm/webpack setup, so the Firebase JS
SDK is loaded as an ES module from the gstatic CDN by a generated `firebase-init.js`
(`webApp/firebase-init.js.template` → `generateFirebaseWebInit` → a `wasmJsMain` resources srcDir),
which publishes a small `window.__familyMomentsAnalytics` object that
`FirebaseWebAnalyticsTracker` calls through `js(...)`. **That bridge buffers on purpose:** a
`type="module"` script is deferred, so its `import()` is still in flight while the wasm app is
already reporting its first `screen_view`. The stub queues calls and replays them once the SDK
resolves; without it every startup event is dropped silently. The template's two placeholder
tokens must each appear exactly once and never inside a comment - the config JSON is multi-line, so
a second occurrence would spill real JSON onto uncommented lines and break the script.

### Permissions, and what this changed

`INTERNET` and `ACCESS_NETWORK_STATE` now live in `androidApp/src/main/AndroidManifest.xml`, not
just the `github` flavor's. The `playstore` flavor previously declared no permissions at all and
made no network requests - `firebase-analytics` merges those permissions into every flavor
regardless, so they are declared explicitly to keep the shipped permission set visible in the
source manifest rather than only in a generated one. `com.google.android.gms.permission.AD_ID` and
the two `ACCESS_ADSERVICES_*` Privacy Sandbox permissions are stripped with `tools:node="remove"`:
this app has no ads, and declaring them would oblige an "Advertising ID" entry in the Play Data
Safety form for no benefit. Verify with:

```bash
./gradlew :androidApp:processPlaystoreDebugMainManifest
grep -E 'uses-permission' \
  androidApp/build/intermediates/merged_manifest/playstoreDebug/*/AndroidManifest.xml
```

**Because this changed what the app does, four documents had to change with it** -
`PRIVACY_POLICY.md`, `README.md`, and `docs/android/<locale>/full_description.txt` in all five
locales all previously promised that no data ever leaves the device. A fifth change is manual and
outside this repo: the **Play Console Data Safety form** must declare "App activity / App
interactions" and "Device or other IDs" as collected, not shared, not linked to identity, and
optional (the user can turn it off). Keep all of these in step with any future change to what is
collected.

### Verifying events

Android:
```bash
adb shell setprop debug.firebase.analytics.app org.neteinstein.family
adb logcat -s FA FA-SVC
```
Web: DevTools → Network, filtered to `google-analytics.com/g/collect`. Either way, Firebase
Console → Analytics → DebugView shows events within seconds instead of hours.

## Architecture

> **KMP migration complete** (merged to `main`, Phases 1-8/8 done): this repo has been migrated to
> Kotlin Multiplatform + Compose Multiplatform, targeting Android + iOS + Web (wasmJs), following
> the pattern in `neteinstein/loopgain`. `core:domain`, `core:data`, `core:ui`, `feature:splash`,
> `feature:settings`, `feature:home`, and `app` (the real KMP aggregator - shared `App()`,
> navigation, and DI) are converted/wired up; `androidApp`'s own main code depends on nothing but
> `app` (Phase 7) - it's just the Android application shell (manifest, flavors, signing, ProGuard)
> plus one test-only `core:domain` dependency for its coverage-stopgap test (see
> `MainActivityViewModelTest.kt`'s comment). CI (Phase 8) builds and verifies all three platforms
> on every PR - `Compile iOS` and `Compile Web` jobs alongside the Android ones - and every KMP
> module has real coverage reporting via `kotlinx-kover`, not just `androidApp`'s own stopgap. One
> functional gap remains outside this migration's original scope, not yet addressed: iOS and
> wasmJs both persist question/card data in memory only (no Room/browser-storage-backed
> implementation - see `QuestionLocalDataSourceImpl`'s doc comment on each platform).
>
> **`build-logic` convention plugins:** the repeated `kotlin { jvmToolchain(17); android { compileSdk
> = 37; minSdk = 32 }; iosArm64(); iosSimulatorArm64(); wasmJs { browser() } }` shape every
> `core:*`/`feature:*`/`app` module needed, plus the identical `ktlint { reporters { ... } }` block
> every module (including `androidApp`/`webApp`) needed, now live in `build-logic` (an included
> build, wired up via `pluginManagement { includeBuild("build-logic") }` in the root
> `settings.gradle.kts`) as precompiled script plugins under `build-logic/src/main/kotlin/`:
> `familymoments.ktlint` (ktlint reporter config alone - applied by every module),
> `familymoments.kmp.library` (adds the Kotlin Multiplatform/Android-KMP-library/Kover plugins plus
> the shared target shape - applied by `core:domain`/`core:data`), and
> `familymoments.kmp.compose.library` (adds Compose Multiplatform on top - applied by
> `core:ui`/`feature:splash`/`feature:home`/`feature:settings`/`app`). Each consuming module's own
> `build.gradle.kts` reopens `kotlin { android { ... } }` only for what's actually specific to it -
> `namespace`, `withHostTestBuilder`/`androidResources` config, and its `sourceSets { }`
> dependencies. `webApp` (wasmJs-only, no Android/iOS target) and `androidApp` (the one classic,
> non-KMP module) apply only `familymoments.ktlint`, since their target shapes aren't shared with
> the seven KMP modules.

Gradle multi-module project, wired via `settings.gradle.kts`:

```
app                 # KMP aggregator module - commonMain holds App() (theme + NavHost), the
                     # composed Koin appModule/doInitKoin, MainActivityViewModel, and the shared
                     # navigation graph (AppNavigation.kt/Screen.kt); iosMain holds mainViewController().
androidApp          # thin Android entry point: MainActivity/FamilyMomentsApp call straight into
                     # `app`'s App()/appModule/setAndroidAppContext; owns the manifest, flavors,
                     # signing and ProGuard config. Depends only on `app` for main code (Phase 7) -
                     # a test-only core:domain dependency remains for its coverage-stopgap test.
iosApp              # Xcode project wrapper (no Gradle build file); iOSApp.swift calls
                     # InitKoinKt.doInitKoin(), ContentView.swift renders MainViewControllerKt.mainViewController()
webApp              # wasmJs entry point; Main.kt calls doInitKoin() then ComposeViewport { App() }
core/domain         # pure Kotlin: models, repository interfaces, use cases — no Android/Compose deps
core/data           # repository implementations + data sources, depends on core:domain
core/ui             # shared Compose theme (Color/Theme/Typography), exposes Compose libs via `api`
feature/splash      # splash screen (animated logo, auto-navigates after a delay)
feature/home        # main question-card screen + HomeViewModel (the only feature with a ViewModel so far)
feature/settings    # settings screen (language shortcut to system settings, about section)
build-logic         # included build hosting the familymoments.* convention plugins (see the
                     # "build-logic convention plugins" note above) - not part of the app itself
```

**Dependency rules (enforced by module graph, not lint):**
- `feature/*` depends only on `core:domain` and `core:ui` — never on another `feature/*` module.
- `core:data` depends on `core:domain` and implements its repository interfaces.
- `core:ui` depends only on Compose/Material3 (no domain/data deps).
- `app` depends on every module and is the only place they're wired together (Koin modules, `NavHost`).

### Data flow (question retrieval)

`QuestionSeedData` (object in `core/data`, `core/data/src/main/kotlin/.../data/source/QuestionSeedData.kt`) holds hardcoded question lists per language (`en`, `pt`, `es`, `fr`, `de`) as the single content source, plus a `VERSION` constant. `QuestionRepositoryImpl` persists that content into a local Room database (`FamilyMomentsDatabase`/`CardDao`, `core/data/.../local/`) and implements `QuestionRepository` (the `core:domain` interface); there is no network layer. On first use each process, it compares `QuestionSeedData.VERSION` against what's stored in the single-row `seed_metadata` table (`SeedMetadataDao`) and, on any mismatch, fully deletes and reinserts every card — not just an additive insert — so a question added, edited, *or removed* from `QuestionSeedData` reaches already-installed devices; cards already hidden (see `UsedQuestionsRepositoryImpl`) are re-marked hidden by id after the replace so a version bump doesn't silently un-hide them. **Bump `QuestionSeedData.VERSION` any time you change its content**, and bump `FamilyMomentsDatabase`'s `@Database(version = ...)` (with an added `Migration`, never `fallbackToDestructiveMigration`) any time you change its schema. `GetQuestionsUseCase`/`GetRandomQuestionUseCase` sit on top of the repository interface and are what ViewModels actually call.

### Localization and language selection

Supported languages live in one place, `core:domain`'s `AppLanguage` enum (`en`, `pt`, `es`, `fr`, `de`) — adding one means adding its `QuestionSeedData` content, a `composeResources/values-<code>/strings.xml` in every module that has strings (`core:ui`, `feature:home`, `feature:settings`, `feature:splash`), an entry in `androidApp/src/main/res/xml/locale_config.xml`, and the enum entry itself.

Two separate things resolve a language at runtime, and both go through `GetContentLanguageUseCase`'s inputs:

- **Question content** — `GetContentLanguageUseCase` (the in-app override if set, else the OS/browser locale via `LocaleProvider`, else English) is what `HomeViewModel`/`SettingsViewModel` pass to `GetQuestionsUseCase`.
- **The app's own UI strings** — Compose Multiplatform resolves `stringResource` against the OS/browser locale only, so the in-app override has to be pushed into its resource lookups explicitly: `core:ui`'s `ProvideAppLanguage` wraps the whole UI in `app`'s `App()`, fed by `MainActivityViewModel.languageOverride` (backed by `ObserveLanguageOverrideUseCase`, so a pick in Settings re-resolves every string immediately). CMP exposes no public API for this, so that one file reaches into compose-resources' internal `LocalComposeEnvironment` — see its header comment, and `core:ui`'s `ProvideAppLanguageTest`, which fails if a CMP upgrade breaks the seam.

How the language gets *picked* is platform-specific (see `feature:settings`'s `rememberOpenLanguageSettingsAction`): Android deep-links into the OS's own per-app language settings (which changes `LocaleProvider` itself, so no override is ever stored there), while iOS/Web — with no such settings page — get the in-app picker that writes `LanguagePreferenceRepository`'s override.

### DI wiring (Koin)

Each module that needs DI defines its own Koin module (`dataModule`, `homeModule`, …); `app/.../di/AppModule.kt` composes them into `fun appModule(updatesEnabled: Boolean): Module`. It takes `updatesEnabled` as a parameter rather than reading it from a local `BuildConfig` because `app` (unlike `androidApp`) has no build flavors of its own — only `androidApp`'s "github"/"playstore" flavors know that value. `androidApp`'s `FamilyMomentsApp` calls `startKoin` directly with `appModule(BuildConfig.UPDATES_ENABLED)` (plus `androidContext()`/`androidLogger()`); iOS and Web instead call `app/.../di/InitKoin.kt`'s `doInitKoin()`, a zero-argument entry point (named `doInitKoin`, not `initKoin`, since Kotlin/Native's Objective-C exporter renames a top-level `init`-prefixed function unpredictably) that always passes `updatesEnabled = false`, since the GitHub self-update feature has no iOS/Web equivalent. When adding a feature module with a ViewModel, add its own `*Module.kt` under `feature/<name>/.../di/` and `include` it from `app`'s `AppModule.kt`.

### Navigation

Single `NavHost` in `app/.../navigation/AppNavigation.kt`, routes defined as a `sealed class Screen` in `Screen.kt` (`Splash`, `Home`, `Settings`). Splash pops itself off the back stack (`popUpTo(inclusive = true)`) once it navigates to Home. Shared across all three platforms via `app`'s `commonMain`, which depends on `org.jetbrains.androidx.navigation:navigation-compose` (`libs.navigation.compose.multiplatform`), **not** the mainline `androidx.navigation:navigation-compose` (`libs.navigation.compose`, still used directly by `androidApp` alone, where it's Android-only and therefore fine): the mainline artifact publishes Android/JVM/native variants but no wasmJs one (confirmed by a real Gradle resolution failure, not assumed) — the JetBrains fork exists specifically to cover that gap and publishes under the same `androidx.navigation.*` package, so no source-level changes are needed, only the Gradle coordinate.

### MVVM conventions

- Each screen that needs state has a `ViewModel` (see `HomeViewModel`) exposing a single `StateFlow<UiState>` (e.g. `HomeUiState`), updated via `MutableStateFlow.update { }`.
- Screens read state with `collectAsStateWithLifecycle()`, not `collectAsState()`.
- ViewModels use `viewModelScope`, never `rememberCoroutineScope()`; no `Context` is passed into ViewModels.
- Use cases are single-purpose, named verb+noun (`GetRandomQuestionUseCase`), and injected into ViewModels through Koin `factory { }`.

### Theming

`FamilyMomentsTheme` (in `core/ui`) picks between Material3 dynamic color (Android 12+/API 31+, via `dynamicLightColorScheme`/`dynamicDarkColorScheme`) and a static `lightColorScheme`/`darkColorScheme` fallback for older devices, based on `isSystemInDarkTheme()`. Never hardcode colors in Composables — always reference `MaterialTheme.colorScheme.*`. All user-facing strings belong in `res/values/strings.xml`.

## Adding a new feature module

1. Create `feature/<name>/` mirroring `feature/home`'s `build.gradle.kts` (depends on `core:domain` + `core:ui` only).
2. Register it in `settings.gradle.kts` and add it as a dependency in `app/build.gradle.kts`.
3. If it needs a ViewModel, add a Koin module under `feature/<name>/.../di/` and `include` it in `app/.../di/AppModule.kt`.
4. Add a route to `Screen.kt` and a `composable(...)` entry in `AppNavigation.kt`.
5. Add unit tests (ViewModel/use case/repository) before opening a PR — see Testing conventions below.

## Testing conventions

- Unit tests use JUnit 4 + MockK (`mockk()`, `coEvery`, `coVerify`) + `kotlinx-coroutines-test`.
- Coroutine-driven tests use `StandardTestDispatcher`, set via `Dispatchers.setMain()`/`resetMain()` in `@Before`/`@After`, and `runTest { }` with `testDispatcher.scheduler.advanceUntilIdle()` to drive pending coroutines.
- Test method names are backtick-quoted sentences: `` fun `nextQuestion advances to next question`() ``.
- Existing tests to use as templates: `core/domain/.../GetRandomQuestionUseCaseTest.kt` (use case + mocked repository) and `feature/home/.../HomeViewModelTest.kt` (ViewModel + mocked use case + coroutine dispatcher setup).

## Agent orchestration

This repo defines five agent roles — Developer, QA, Architect, Security Manager, Product Manager — as Claude Code subagents (`.claude/agents/`). Architect, Security Manager, and Product Manager delegate to Developer and QA rather than implementing everything themselves. See [`docs/agents/README.md`](docs/agents/README.md) for the full orchestration model, and `docs/agents/<role>.md` for each role's canonical instructions.

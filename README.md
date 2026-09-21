# Family Moments

Spark deeper conversations with the people you love.

Family Moments is an Android app that hands you one thoughtful question at a time — swipe through
a deck of conversation starters across five categories (Ice Breakers, Memories, Values, Future
Dreams, Daily Life), pick a card at random, or browse the whole deck as a grid. Your content
stays on-device: there's no account and no backend of its own. The app reports anonymous usage
analytics to Firebase (which screens and actions get used, never any text you read or type), and
the GitHub build can check this repo's own Releases for an update. Both are described in
[`PRIVACY_POLICY.md`](PRIVACY_POLICY.md), and analytics can be turned off in Settings.

## Features

- **Swipeable question deck** — swipe left/right for the next/previous question, swipe up to
  focus a card full screen, swipe down to hide a card you've already used or don't want (it stays
  out of rotation until you reset it).
- **Categories** — filter the deck by Ice Breakers, Memories, Values, Future Dreams, or Daily
  Life, or leave it on "All".
- **Shuffle** — jump straight to a random question from the current filter.
- **Grid view** — toggle between the one-at-a-time swipe deck and a scrollable grid of every
  question in the current filter, tapping any card to open it full screen.
- **Multi-language content** — question text ships in English, Portuguese, Spanish, French, and
  German (`en`/`pt` are currently exposed as selectable app languages via Android's per-app
  language picker; see `app/src/main/res/xml/locale_config.xml`).
- **Reset cards** — Settings has a one-tap reset that brings every hidden card back into rotation.
- **Usage analytics, opt-out** — Firebase Analytics records which screens and actions get used so
  the deck can be improved; Settings' "Share usage data" switch turns it off. No user ID, no
  advertising ID, no question text — see [`PRIVACY_POLICY.md`](PRIVACY_POLICY.md).
- **Self-updating** — Settings' "Update to latest" button checks this repo's GitHub Releases and
  installs a newer APK directly, no Play Store required.

## Requirements

- Android 12+ (API 32) device or emulator.
- No accounts or build-time secrets required — a clean checkout compiles and runs as-is, with
  analytics simply disabled (see Setup below).

## Setup

```bash
git clone https://github.com/neteinstein/FamilyMoments.git
cd FamilyMoments
./gradlew assembleDebug
./gradlew installDebug   # with a device/emulator connected
```

Or open the project directly in Android Studio and run it from there.

### Firebase Analytics (optional)

A clean checkout builds and runs with analytics disabled — the Firebase config files are
deliberately git-ignored, and both the Android and Web builds fall back to a no-op tracker when
they're absent. To build against a real Firebase project, drop in your own config:

| File | Where to get it |
|---|---|
| `androidApp/google-services.json` | Firebase console → Project settings → your Android app → `google-services.json` |
| `webApp/firebase-web-config.json` | Firebase console → Project settings → your Web app → the `firebaseConfig` object, saved as JSON |

Both are listed in `.gitignore`; CI supplies them through the `GOOGLE_SERVICES_JSON` (base64) and
`FIREBASE_WEB_CONFIG` (raw JSON) repository secrets. Note these values are *client identifiers*,
not secrets — they ship inside the APK and the JS bundle either way — so what actually protects a
Firebase project is restricting each API key in the Google Cloud console (Android key pinned to the
package name + signing SHA-1, browser key pinned to HTTP referrers). See
[`AGENTS.md`](AGENTS.md#firebase-analytics) for the full setup.

## Usage

1. Open the app — you land on the swipe deck with a random question already showing.
2. Swipe **left**/**right** for the next/previous question, **up** to focus it full screen, or
   **down** to hide it (confirmed with a dialog) if you've already used it or don't want it again.
3. Use the category dropdown (bottom left) to filter the deck, or the shuffle button (top right)
   to jump to a random card from the current filter.
4. Tap the grid icon (bottom right) to switch to a scrollable grid of every question in the
   current filter — tap any card to open it full screen.
5. Open Settings (top right) to change the app's language, reset hidden cards, or check for an
   app update.

## Architecture

Kotlin, Jetpack Compose + Material3, MVVM, Koin DI, one Gradle module per layer/feature:

```
app                 # entry point: MainActivity, Application class, Koin DI wiring, navigation graph
core/domain         # pure Kotlin: models, repository interfaces, use cases
core/data           # repository implementations + local (Room) data source
core/ui             # shared Compose theme, exposes Compose libs via `api`
feature/splash      # splash screen
feature/home        # main question-card / grid screen + HomeViewModel
feature/settings    # settings screen (language, reset cards, update)
```

See [`AGENTS.md`](AGENTS.md) for the full module dependency rules, data flow, DI/navigation
conventions, and testing standards.

## Testing & CI

```bash
./gradlew testDebugUnitTest                   # all unit tests
./gradlew :feature:home:testDebugUnitTest     # a single module
./gradlew ktlintCheck                         # style/formatting gate (ktlintFormat to auto-fix)
./gradlew createDebugUnitTestCoverageReport   # tests + coverage report
```

Every PR into `main`/`develop` runs four independent GitHub Actions jobs
(`.github/workflows/pr.yml`): `ktlint`, `assembleDebug`, `testDebugUnitTest`, and a
coverage report uploaded to Codecov.

## Releases

Every push to `main` triggers `.github/workflows/release.yml`: it re-validates the commit
(`ktlintCheck`, `testDebugUnitTest`), builds a signed release APK, and publishes it as a GitHub
Release tagged `v1.0.<run number>`. See [`AGENTS.md`](AGENTS.md#releases) for the required signing
secrets and the in-app update flow that consumes these releases.

## Contributing

Issues and pull requests are welcome. See [`AGENTS.md`](AGENTS.md) for coding standards, module
boundaries, and testing conventions, and the PR template for what to include.

### Agent orchestration

This repo defines five specialized agent roles — Developer, QA, Architect, Security Manager, and
Product Manager — usable as Claude Code subagents and GitHub Copilot custom agents alike. See
[`docs/agents/README.md`](docs/agents/README.md) for the full orchestration model.

## License

Copyright (c) 2026 Pedro Vicente. All rights reserved. This is source-available, not open-source:
the code is public for viewing and reference, but using, copying, modifying, or redistributing it
(commercially or otherwise) requires the copyright holder's prior written permission. See
[`LICENSE`](LICENSE) for the full terms.

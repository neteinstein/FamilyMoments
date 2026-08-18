# Google Play Submission Guide — Family Moments

This is the complete checklist and reference for submitting Family Moments to the Google Play
Console, plus all the store-listing assets generated for it. Store text lives in
[`fastlane/metadata/android/en-US/`](../../fastlane/metadata/android/en-US/) using the
[fastlane supply](https://docs.fastlane.tools/actions/supply/) layout, so it can be uploaded by
hand through Play Console or automated later with `fastlane supply` if desired.

## ⚠️ Blocker: the self-update feature conflicts with Play policy

**Read this first — it will get the app rejected (or suspended after the fact) if not
addressed.**

Family Moments' Settings screen has an "Update to latest" button
(`core/data/.../GitHubUpdateRepositoryImpl.kt`, `AppUpdateInstallerImpl.kt`) that downloads an
APK from this repo's GitHub Releases and installs it directly, using the
`REQUEST_INSTALL_PACKAGES` permission declared in `app/src/main/AndroidManifest.xml`.

Google Play's **Device and Network Abuse** policy states that an app distributed via Google Play
"may not modify, replace, or update itself using any method other than Google Play's own update
mechanism," and separately prohibits apps that download and install executable code (APKs) from
a source other than Google Play. A self-updating APK is one of the most common — and most
mechanically detected — reasons for a rejected review or a post-launch suspension.

**Before submitting, do one of the following:**

1. **Recommended:** Ship a separate Play build flavor/variant that removes the update feature
   entirely — strip the `REQUEST_INSTALL_PACKAGES` permission, the Settings "Updates" section,
   and the GitHub-releases update check for that variant, while keeping direct-APK/GitHub-Releases
   distribution (this repo's existing `.github/workflows/release.yml` flow) fully intact for
   users who install outside Play. Play builds get updates through Play's own mechanism instead.
2. Alternatively, if only ever distributing outside Google Play, this is a non-issue — just don't
   submit to Play with this feature present.

This guide assumes option 1 will happen before the Play Console submission; the Data Safety and
Permissions sections below describe the Play-build behavior (i.e., without self-update).

## Store listing text

Located in `fastlane/metadata/android/en-US/`:

| File | Content | Limit | Status |
|---|---|---|---|
| `title.txt` | Family Moments | 30 chars | ✅ 15 chars |
| `short_description.txt` | Spark deeper conversations with the people you love. | 80 chars | ✅ 52 chars |
| `full_description.txt` | Full marketing copy (features, categories, privacy note) | 4000 chars | ✅ ~1,750 chars |

Edit these directly — they're plain text, no special formatting needed. Full description was
written to lead with the privacy/no-accounts angle, which is a genuine differentiator worth
keeping front and center in the listing.

## Graphic assets (generated)

All generated at `fastlane/metadata/android/en-US/images/`:

| Asset | File | Size | Play requirement |
|---|---|---|---|
| Hi-res icon | `images/icon.png` | 512×512 | 512×512, 32-bit PNG ✅ |
| Feature graphic | `images/featureGraphic.png` | 1024×500 | 1024×500, JPG/24-bit PNG, no alpha ✅ |
| Phone screenshot 1 | `phoneScreenshots/1_swipe_deck.png` | 1080×1920 | 16:9–9:16 ratio, 320–3840px ✅ |
| Phone screenshot 2 | `phoneScreenshots/2_full_screen_question.png` | 1080×1920 | ✅ |
| Phone screenshot 3 | `phoneScreenshots/3_grid_view.png` | 1080×1920 | ✅ |
| Phone screenshot 4 | `phoneScreenshots/4_category_filter.png` | 1080×1920 | ✅ |
| Phone screenshot 5 | `phoneScreenshots/5_settings.png` | 1080×1920 | ✅ |

The icon and feature graphic are pixel-accurate renders of the app's real mark (the campfire
vector in `app/src/main/res/drawable/ic_launcher_foreground.xml`) and real color tokens
(`core/ui/.../Color.kt`). Play applies its own mask/shape to the hi-res icon, so it's supplied as
a full-bleed square, matching how Android composites the adaptive icon's background + foreground
layers.

The five phone screenshots are **high-fidelity mockups**, not device/emulator captures — this
container has no Android SDK, emulator, or connected device available, so they were built as
pixel-precise HTML/CSS recreations of the actual Compose screens, using the app's real strings
(`strings.xml`), real color tokens, real layout proportions, and real sample question text from
`QuestionSeedData.kt`. They accurately represent what the app looks like. **Before submitting,
swap these for real captures from a device or emulator** (`./gradlew installDebug` + a
screenshot, or Android Studio's device screenshot tool) — same shot list below works as the
composition guide:

1. Swipe deck / home screen with a question card showing
2. A question opened full screen (swipe-up state)
3. Grid view of the question deck
4. Category filter dropdown open
5. Settings screen

Optional additions if you want to fill out the full 8-screenshot allowance: a 7" and 10" tablet
set (Play no longer requires these but they help if the app is later made adaptive/foldable-aware),
and/or a short (≤30s) promo video uploaded as a YouTube link in Play Console — neither exists yet.

The HTML/CSS mockup generator used to produce these was a scratch tool and isn't committed here.
If you want different mockups, the fastest path is a real device/emulator screenshot rather than
rebuilding that pipeline.

## App details for the Play Console form

| Field | Recommended value |
|---|---|
| App name | Family Moments |
| Default language | English (United States) |
| App or game | App |
| Free or paid | Free |
| Category | Lifestyle (alternative: Communication or Parenting — see note below) |
| Tags | Family, Communication, Conversation starters |
| Contact email | **[fill in — required, shown to users]** |
| Contact website | `https://github.com/neteinstein/FamilyMoments` (or a dedicated site if you have one) |
| Contact phone | Optional |
| Privacy policy URL | **[fill in — see below]** |

**Category note:** "Lifestyle" is the closest fit and least likely to trigger extra review
friction. "Parenting" is more specific to the family-conversation use case but implies
child-directed content, which this app is not (it's for the whole family, adults included) — avoid
it unless deliberately targeting the Designed for Families program (see Target audience below).

## Privacy policy

Play Console requires a **hosted, publicly reachable URL** — a file in this repo isn't enough by
itself. A draft is provided at [`PRIVACY_POLICY.md`](./PRIVACY_POLICY.md); before submitting:

1. Fill in the `[FILL IN ...]` placeholders (date, support email).
2. Host it somewhere public and stable. Cheapest option for this repo: enable **GitHub Pages**
   (Settings → Pages → deploy from `main` / `docs`) and it becomes reachable at
   `https://neteinstein.github.io/FamilyMoments/play-store/PRIVACY_POLICY` (rendered as raw
   Markdown by GitHub Pages' Jekyll, or convert to `.html` for a cleaner render). Any static host
   works equally well.
3. Paste the final URL into Play Console → Policy → App content → Privacy policy, **and** into
   the Data safety section (it's requested in both places).
4. Update the privacy policy again if the self-update feature is stripped for the Play build (the
   draft has a note marking exactly which paragraph to revisit).

## Content rating questionnaire

Play Console will ask a IARC questionnaire. Based on the app's actual content (open-ended
conversation-starter text, no images/video/audio, no user-generated content, no user-to-user
interaction, no violence/sexual content/gambling references):

- Violence: None
- Sexuality: None
- Language: None
- Controlled substances: None
- Gambling: None
- User-generated content: None (question text is fixed, shipped with the app)
- User interaction / communication: None (no chat, no multiplayer, no data shared between users)
- Shares location: No
- Digital purchases: No

Expected result: **Everyone / PEGI 3** across all rating boards. Answer the actual questionnaire
honestly — this list is a preview, not a substitute for it.

## Data safety section

This is the section most likely to be filled in incorrectly by habit (copy-pasted from another
app). For Family Moments as described in this guide (Play build, self-update removed):

- **Does your app collect or share any user data?** No.
- **Is data encrypted in transit?** N/A — no data leaves the device.
- **Can users request data deletion?** N/A — nothing is collected; uninstalling the app clears
  all local state.
- **Data types collected:** None.

If the self-update feature is *not* removed and network calls to GitHub remain, re-answer this
section to reflect that the app makes network requests (device/app info sent as part of any
HTTPS request such as IP address) even though no personal data is deliberately collected — under
"Data collection," most reviewers still mark this "No data collected" since nothing beyond
standard network metadata is transmitted, but be accurate to what actually ships.

## Target audience & content guidelines

- **Target age group:** Suggest 13+ / general audience, not "Designed for Families" (that
  program has stricter technical and content requirements — COPPA compliance docs, no mixed
  audience ads, additional review — and isn't needed for an app aimed at the whole family rather
  than specifically at children).
- **Ads:** App contains no ads or ad SDKs — answer "No" to the ads declaration.
- **News app:** No.
- **COVID-19 contact tracing / status app:** No.

## Permissions justification (for Play's permissions declaration form)

| Permission | Play-build status | Justification if kept |
|---|---|---|
| `INTERNET` | Remove for Play build (see blocker above) | N/A once removed |
| `REQUEST_INSTALL_PACKAGES` | Remove for Play build | N/A once removed |

If both are removed, the Play build declares **zero** runtime/special permissions, which is a
strong signal to reviewers and simplifies both the content-rating and data-safety flows above.

## Pricing & distribution

- Free, no in-app purchases, no subscriptions.
- Countries: All countries/regions (no reason to restrict given no data collection or
  region-specific content).
- Contains ads: No.
- Device categories: Phone (the UI is phone-oriented; tablet/Chromebook/Wear OS support isn't
  built — leave those unchecked in Play Console's device catalog unless later verified).

## Release signing

`app/build.gradle.kts` already wires a release `signingConfig` from CI secrets
(`KEYSTORE_FILE`/`KEYSTORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD`) via `.github/workflows/release.yml`,
falling back to debug signing locally. For Play:

- Enroll in **Play App Signing** (default for new apps) — upload a release build signed with your
  own upload key; Google re-signs it for distribution with an app signing key it manages. This is
  independent of, and doesn't require changing, the existing GitHub Actions signing setup used for
  the direct-APK release channel.
- Build an **Android App Bundle** (`.aab`) for the Play upload — Play Console requires AAB for new
  apps, not APK. Add a `bundleRelease` task invocation (`./gradlew bundleRelease`) alongside the
  existing `assembleRelease` used for GitHub Releases; both can be produced from the same signing
  config.
- `versionCode`/`versionName` are already CI-driven (`APP_VERSION_CODE`/`APP_VERSION_NAME` env
  vars from the Actions run number) — reuse that scheme for Play uploads so version codes never
  collide between the two distribution channels.

## Suggested rollout

1. Internal testing track first (instant, no review) — sanity-check the listing, screenshots, and
   an actual installed build.
2. Closed testing (a small opt-in group) for at least a few days if you want real feedback before
   going public — also satisfies Play's requirement (for new developer accounts) of 20+ testers
   for 14+ days before a production release becomes available on some account tiers.
3. Production release once the self-update blocker is resolved and the mock screenshots are
   swapped for real device captures.

## Pre-submission checklist

- [ ] Self-update feature removed/gated for the Play build variant (see blocker section)
- [ ] Privacy policy hosted publicly; placeholders filled in; URL added to Play Console
- [ ] Contact email decided and added to Play Console
- [ ] Screenshots replaced with real device/emulator captures (mockups are placeholders)
- [ ] Content rating questionnaire completed in Play Console
- [ ] Data safety section completed in Play Console
- [ ] Release AAB built and signed (Play App Signing enrolled)
- [ ] Internal testing track verified before wider rollout

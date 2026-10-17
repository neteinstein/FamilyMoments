# Privacy Policy — Family Moments

**Last updated: 2026-09-21**

Family Moments (`org.neteinstein.family`) keeps all of your content on your device. This policy
covers both distribution channels: the version installed from Google Play and the version
installed directly from this repository's GitHub Releases.

## Data collection

There is still no account, no sign-in, and no advertising SDK anywhere in the app, and Family
Moments collects no personal data: no name, no email address, no contacts, no location, no
advertising ID, and nothing you type.

Everything the app remembers — which cards you've hidden, your chosen theme, your selected app
language, the question deck itself — is stored locally on your device and is never sent anywhere.

**What did change:** as of 2026-09-21 the app includes Google Firebase Analytics, to answer
questions like which question categories people actually use and which questions get hidden most
often, so the deck can be improved. You can turn this off — see "Your choice" below.

### What analytics collects

- **Which screens you open** and which in-app actions you take: swiping to the next card, hiding a
  card (recorded as the card's number, never its text), changing the category filter, shuffling,
  switching to grid view, changing the theme or language, resetting hidden cards, checking for an
  update.
- **Coarse settings** used to group the numbers: your content language, theme, platform
  (Android/Web) and install channel, and a *bucketed* count of hidden cards (`0`, `1–10`, `11–50`,
  `51+`) rather than an exact one.
- **What Firebase collects automatically:** a random app-instance identifier, app version, device
  model, operating system version, and an approximate country derived from your IP address.

### What it does not collect

No identifier that points at you as a person. The app deliberately never sets a user ID, never
generates or stores an installation ID of its own, and never asks Firebase for the advertising ID
— the Android permission that would allow it is explicitly removed from the app.

## Your choice

Settings → **Share usage data** turns analytics off. It is on by default; switching it off stops
collection at the SDK level, so nothing further is recorded or sent, on that device, until you
switch it back on. The choice is remembered.

## Network access

- **Both versions:** send analytics events to Google's Firebase servers, unless you have turned
  "Share usage data" off. (Before 2026-09-21 the Play Store version made no network requests at
  all; that is no longer true.)
- **GitHub-distributed version** (downloaded directly from
  [this repo's Releases](https://github.com/neteinstein/FamilyMoments/releases)): additionally
  checks `api.github.com` for a newer release when you tap "Update to latest" in Settings, and
  downloads the new APK from GitHub if you confirm. That request only reaches GitHub's servers —
  Family Moments has no server of its own, and sends nothing beyond a standard HTTP request (no
  account identifiers, no usage data).

## Third parties

Analytics data is processed by **Google** as the provider of Firebase Analytics, under
[Google's privacy policy](https://policies.google.com/privacy) and the
[Firebase data-processing terms](https://firebase.google.com/support/privacy). It is not sold, and
not shared with anyone else. Family Moments has no server of its own and no other third-party SDK.

The GitHub-distributed version's update check is a request to GitHub, described above, and is your
device talking to GitHub directly rather than to us.

## Children's privacy

The app collects no personal data from anyone, including children. The analytics described above
records how the app is used, never who is using it, and can be turned off entirely in Settings.

## Changes to this policy

If this policy changes, the update will be committed to this file in
[this repository](https://github.com/neteinstein/FamilyMoments), with the "Last updated" date
above reflecting the change.

## Contact

Questions about this policy can be raised as an issue on
[this repository's GitHub Issues](https://github.com/neteinstein/FamilyMoments/issues), or via
[the developer's website](https://www.pedrovicente.pt).

# Privacy Policy — Family Moments

**Last updated: 2026-09-17**

Family Moments (`org.neteinstein.family`) is built to work entirely on your device. This policy
covers both distribution channels: the version installed from Google Play and the version
installed directly from this repository's GitHub Releases.

## Data collection

Family Moments does not collect, store, or transmit any personal data. There is no account, no
sign-in, no analytics SDK, and no advertising SDK anywhere in the app.

Everything the app remembers — which cards you've hidden, your chosen theme, your selected app
language, the question deck itself — is stored locally on your device (via a local Room
database) and is never sent anywhere.

## Network access

- **Play Store version:** makes no network requests at all.
- **GitHub-distributed version** (downloaded directly from
  [this repo's Releases](https://github.com/neteinstein/FamilyMoments/releases)): additionally
  checks `api.github.com` for a newer release when you tap "Update to latest" in Settings, and
  downloads the new APK from GitHub if you confirm. That request only reaches GitHub's servers —
  Family Moments has no server of its own, and sends nothing beyond a standard HTTP request (no
  account identifiers, no usage data).

## Third parties

Family Moments does not share data with third parties, because it does not collect any data to
share. The GitHub-distributed version's update check is a request to GitHub, described above, and
is your device talking to GitHub directly rather than to us.

## Children's privacy

The app does not knowingly collect data from anyone, including children, because it does not
collect data from anyone at all.

## Changes to this policy

If this policy changes, the update will be committed to this file in
[this repository](https://github.com/neteinstein/FamilyMoments), with the "Last updated" date
above reflecting the change.

## Contact

Questions about this policy can be raised as an issue on
[this repository's GitHub Issues](https://github.com/neteinstein/FamilyMoments/issues), or via
[the developer's website](https://www.pedrovicente.pt).

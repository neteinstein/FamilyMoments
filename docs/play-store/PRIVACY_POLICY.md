# Privacy Policy for Family Moments

**Last updated: [FILL IN DATE BEFORE PUBLISHING]**

This privacy policy describes how Family Moments ("the app", "we", "us") handles information.
Google Play requires every app's Store Listing to link to a policy like this one — see the
[Submission Guide](./SUBMISSION_GUIDE.md) for where to host it.

## Summary

Family Moments does not collect, store, or transmit any personal data. The app works entirely
on your device.

## Information We Collect

**None.** Family Moments has no user accounts, no analytics SDK, no advertising SDK, and no
crash-reporting SDK. The app does not ask for your name, email, location, contacts, photos, or
any other personal information.

## Data Stored On Your Device

The app stores the following data locally, using Android's private app storage (Room/SQLite),
and it never leaves your device:

- Which question cards you've marked as "hidden" (used or not wanted), so they can be excluded
  from rotation until you reset them in Settings.
- Your selected question-category filter and view preference (swipe deck vs. grid), so the app
  remembers your last state between sessions.

None of this data is linked to an identity, none of it is shared with us or with any third
party, and uninstalling the app deletes it permanently.

## Network Access

The app requests the `INTERNET` permission for exactly one purpose: [Settings → "Update to
latest"] checks this app's own GitHub Releases page
(`https://api.github.com/repos/neteinstein/FamilyMoments/releases`) for a newer version. No
personal or device-identifying data is sent with this request beyond what any standard HTTPS
request includes (e.g., IP address, handled by GitHub, not by us).

> **If this app is distributed on Google Play, the self-update feature described above must be
> removed or disabled for the Play build** — see the "Self-updating APK" section of the
> [Submission Guide](./SUBMISSION_GUIDE.md) for why, and update this paragraph (or delete it)
> once that's done so the policy stays accurate to what actually ships.

## Permissions

| Permission | Why the app requests it |
|---|---|
| `INTERNET` | Checking for and downloading app updates from GitHub Releases (see above; not used by the Play Store build if the self-update feature is removed). |
| `REQUEST_INSTALL_PACKAGES` | Lets the app ask the OS for permission to install an update APK it downloaded (see above; not used by the Play Store build if the self-update feature is removed). |

The app requests no other permissions — no camera, microphone, contacts, location, storage, or
notifications access.

## Children's Privacy

Family Moments does not knowingly collect any personal information from anyone, including
children under 13. The app contains no chat, social, or user-generated-content features that
would expose a child to other users.

## Changes to This Policy

If this policy changes, the "Last updated" date above will change accordingly. Material changes
will be reflected in the app's Play Store listing.

## Contact

Questions about this policy can be sent to: **[FILL IN SUPPORT EMAIL]**

---

*This document is a starting draft, not legal advice. Review it (ideally with counsel if this
app will be published commercially or handle any future data collection) before hosting it
publicly and linking it from the Play Console.*

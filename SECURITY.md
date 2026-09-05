# Security Policy

AkashBoard is a keyboard. It sees everything you type, holds the API keys you give it, and —
if you switch it on — can read and write text in other apps. That is a lot of trust, so
private vulnerability reporting matters more here than it does for most apps.

## Reporting a vulnerability

**Do not open a public issue for anything that could be used against someone's device or data.**
Use GitHub's private reporting instead:

> **[➕ Report a vulnerability privately](https://github.com/AkashPriyadarshii/AkashBoard/security/advisories/new)**

Only you and the maintainer can see the report. It becomes a published advisory once a fix ships —
with credit, unless you'd rather not be named.

Useful in a report: what you did, what happened, Android version, device model, and the AkashBoard
version from *Settings → About*. A proof of concept is welcome but not required.

### What to expect

AkashBoard is maintained by one person. You'll get an acknowledgement within a few days.
If things go quiet longer than that, ping the thread — you won't be ignored.

Fixes ship in the next release. Only the latest release is supported; no backporting.

## In scope

The Android app. Specifically:

- Anything that exposes a stored provider API key to another app or to someone on the network.
- Anything that lets another app read what is typed or dictated, or drive the floating button's
  accessibility service.
- Anything that sends audio or text to a destination other than the provider you configured.
- A crafted `.flex` that escapes its directory, overwrites files outside it, or executes code.
- Anything that weakens or bypasses TLS beyond the switches documented in settings.

## Not vulnerabilities

These are how the app is built:

- **Your audio and text go to the AI provider you configured.** That is what the app does; the
  [privacy policy](PRIVACY_POLICY.md) describes exactly what leaves the device and when. The
  on-device engine is how you avoid it entirely.
- **Your API key is stored on the device**, in AkashBoard's private storage. Root or physical
  access to an unlocked phone can read it — that is the Android storage model, not a flaw.
- **Plain HTTP is permitted** so a speech server on your own network can be reached.
- **"Trust user certificates" exists**, off by default, for self-hosters running their own CA.
- **The accessibility service is opt-in**, used only by the floating button, and revocable anytime
  in Android settings.
- Scanner output with no explanation of how the finding is reachable in AkashBoard.

## Third-party components

AkashBoard is a fork of [FlorisBoard](https://github.com/florisboard/florisboard) and ships
upstream libraries and on-device models. Vulnerabilities in those are best reported to the
upstream project — but notify this repo too so the dependency can be updated.

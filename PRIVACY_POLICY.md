# Privacy Policy for AkashBoard

**Last updated:** September 5, 2026

AkashBoard is an open-source, privacy-first Android keyboard built with Kotlin and Jetpack Compose.
This policy describes what data the keyboard handles, how permissions are used, and our commitment to your privacy.

---

## 1. Core Privacy Principles

- **No telemetry, no tracking, no ads.** AkashBoard does not collect, record, or track what you type.
- **Offline-by-default.** On-device speech recognition transcribes your voice directly on your device without sending any audio over the internet.
- **Your keys, your control.** If you choose to use third-party cloud AI providers (such as OpenAI, Gemini, or Groq), your requests are sent directly to that provider using your own API key. We run no intermediary servers and never see, intercept, or store your text or audio.

---

## 2. Permissions and Why They Are Used

| Permission | Purpose |
| --- | --- |
| `RECORD_AUDIO` | Required exclusively to capture voice input when you deliberately press the microphone button. Audio is processed only while recording is active. |
| `INTERNET` | Used solely if you configure a cloud AI provider with your own API key or self-hosted endpoint. If on-device offline models are selected, audio never leaves your phone. |
| `MODIFY_AUDIO_SETTINGS` / `BLUETOOTH` | Used to route microphone recording through Bluetooth headsets or external mics. |
| `VIBRATE` | Provides optional haptic feedback when typing. |
| `POST_NOTIFICATIONS` | Displays transcription progress and status alerts on Android 13+. |

---

## 3. Data Handling and Storage

- **Keystrokes:** AkashBoard never logs, records, transmits, or sells your keystrokes. Password fields and incognito sessions are completely excluded from history.
- **Audio recordings:** When using offline on-device models, audio is transcribed in memory and discarded immediately. When using a cloud provider, audio is sent directly to your configured endpoint. Temporary recording buffers are erased immediately after transcription completes.
- **API Keys & Settings:** Stored securely on your device in Android's private, sandboxed app storage. They are never transmitted to any third party except the respective provider you configured.
- **Personal Dictionary & Clipboard:** Stored locally in your device's private database. You can inspect, clear, or export this data at any time in Settings.

---

## 4. Third-Party AI Providers (Bring Your Own Key)

If you configure an external speech-to-text or rewording provider (e.g., OpenAI, Google Gemini, Groq, Mistral, Anthropic, or Ollama):
- Your requests go directly from your device to that provider.
- Data handling is governed by that provider's terms and privacy policy.
- You can switch back to on-device offline models or delete your API keys at any time in Settings.

---

## 5. Open Source Transparency

AkashBoard is 100% free and open-source under the Apache License 2.0. The complete source code is public and auditable:

**Repository:** <https://github.com/AkashPriyadarshii/AkashBoard>

---

## 6. Contact

For questions or security concerns regarding this Privacy Policy:

- **Author:** Akash Priyadarshi (Patna, Bihar, India)
- **Repository:** <https://github.com/AkashPriyadarshii/AkashBoard>
- **Website:** <https://akashpriyadarshi.vercel.app>
- **Security:** <https://github.com/AkashPriyadarshii/AkashBoard/security/advisories/new>

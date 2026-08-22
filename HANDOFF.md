# HANDOFF.md — Cross-Agent Coordination Log

Append an entry before ending your session. Read the last 3 entries for your
branch and `main` before starting work. Newest first.

Format:

```
## YYYY-MM-DD — <agent> @ <branch>
- **Did:** <what landed, commit hashes>
- **In-flight:** <uncommitted / planned next>
- **Don't touch:** <files or areas another agent should leave alone>
```

---

## 2026-08-22 — Claude Android agent @ claude-android
- **Did:**
  - Full Android-side audit (settings wiring, lifecycle, threading, security,
    manifest). Fixed and committed as `0dde84e`:
    - ALL settings now wired at `onStartInputView` (was only 3 of ~20)
    - "Clear clipboard history" actually deletes unpinned items (was toast-only)
    - `clipboard_max_items` respected in clipboard panel load
    - `PredictorBridge` flags `@Volatile`
    - `VoiceInput.destroy()` called in IME `onDestroy`
    - Removed dead `network_access` setting + `PrivacyDashboard.NETWORK`
      (no INTERNET permission by design)
  - Created worktree `../AkashBoard-rs` for the claude-rs agent.
- **In-flight:** nothing uncommitted on this branch.
- **Don't touch:** `engine/src/**`, `engine/tests/**`, jniLibs `.so`,
  branch `claude-rs` itself — engine agent's territory. Also: original Rust
  WIP sits in stash `pre-claude-android rust+android WIP` (OnboardingActivity,
  SettingsActivity, learner.rs, predictor.rs) — restore from there, don't
  re-invent.

---

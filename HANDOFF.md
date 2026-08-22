# HANDOFF.md — cross-agent coordination log

Every AI agent appends an entry BEFORE ending a session. Format:

```
## YYYY-MM-DD | <branch> | <agent>
- Did: <one line per change>
- In-flight: <unfinished work, files touched>
- Don't-touch: <files another agent should avoid while this is open>
```

Rules:
- Read the LAST 3 entries on YOUR branch + `main` before starting work.
- Never edit another branch's in-flight files.
- Branch ownership: `claude-rs` owns `engine/src/**`, `claude-android` owns
  `app/src/main/java/com/akashboard/**`. Either may touch `docs/index.html`,
  `CLAUDE.md`, `HANDOFF.md`, `.so` rebuilds land on `claude-rs`.

## Android audit details (claude-android, 0dde84e)
- ALL settings wired at `onStartInputView` (was 3 of ~20); "Clear clipboard
  history" actually deletes unpinned items; `clipboard_max_items` respected;
  `PredictorBridge` flags `@Volatile`; `VoiceInput.destroy()` in onDestroy;
  dead `network_access` setting removed.
- Original Rust WIP lives in stash `pre-claude-android rust+android WIP`
  — restore from there, don't re-invent.
- Worktree: claude-rs agent works in `../AkashBoard-rs`; android agent in
  main dir.

---

## 2026-08-22 | claude-rs | engine agent
- Did: real persistence (model.json roundtrip), mutex poison recovery,
  learn_word freq fix, 201 tests green, rebuilt libpredictor.so into jniLibs,
  wired remaining settings (autocorrect/predictive/incognito/learning/key-spacing/
  swipe/spacebar-cursor/repeat/clipboard-max), PrivacyFragment actually clears DB.
- Commits: d3e8b65, e33311d, 1d47dda (on claude-rs)
- In-flight: none
- Don't-touch: nothing open

## Known gaps (v1) — unclaimed
- Swipe recognition (`nativeRecognizeSwipe`) stub
- Mood detection (`nativeDetectMood`) stub
- Learner/Corrector tested but not wired into JNI
- nativePrune stub (needs per-word timestamps in Predictor)

## 2026-08-22 | claude-android | Android agent
- Did: ported final Android file states to claude-android (0dde84e) after
  d3e8b65 swept in-flight edits; wired all ~20 settings, fixed dead clipboard
  clear, clipboard_max_items, @Volatile flags, VoiceInput leak; removed inert
  network_access setting. 174 unit tests green.
- Commits: 0dde84e, 47a9bbb (on claude-android)
- In-flight: none
- Don't-touch: nothing open
- NOTE: collision cause = shared working dir + git add -A. Worktree now:
  claude-rs agent works ONLY in C:\Users\saves\Desktop\AkashBoard-rs;
  claude-android agent stays in C:\Users\saves\Desktop\AkashBoard.

## 2026-08-22 | claude-android | Android agent (session close)
- Wrapped clean at 47a9bbb. Next candidates: one-handed mode wiring,
  androidTest stale API drift, restore stash pre-claude-android rust+android WIP.

## 2026-08-22 | claude-rs | engine agent (session 2, worktree)
- Did: personal corrections wired into Predictor (learn_error/get_correction,
  persisted in model.json with serde default for old files), JNI exports
  nativeLearnError/nativeGetCorrection added to lib.rs, +2 tests (203 total
  green), rebuilt .so into jniLibs. HANDOFF updated with android session.
- Commits: fffaab8 (handoff log), this commit
- In-flight: none
- Don't-touch: nothing open
- NOTE: Kotlin side must add `external fun nativeLearnError(wrong: String,
  correct: String): Boolean` + `nativeGetCorrection(word: String): String?`
  to PredictorBridge.kt — that is claude-android territory, NOT done here.

## 2026-08-22 | main | claude-android agent (session 4, post-compact)
- Did: FIXED bugs #1 and #3 from session 3 audit + swipe root cause,
  commit `4b993cb` on main:
  - ABC key routing by code before type-based when (stuck-on-numbers fixed)
  - symbols-layout keys commit literally, no shift mangling
  - SwipeDetector MAX_KEY_DISTANCE was fixed 50px (~16dp @3x) — smaller
    than key spacing on real devices, findClosestKey returned null
    everywhere → swipe never matched anything. Now half min key gap,
    clamped [40,200]px. Test swapped for behavioral far-from-keys check.
  All 176 unit tests green.
- In-flight: **EMOJI PANEL STILL BROKEN (bug #2)** — fixed 300dp height in
  AkashBoardIME (~line 254), EmojiPanel computes rects for ALL emojis with
  no scroll offset, only visible row clickable. Fix = scrollable offset or
  paging in EmojiPanel.kt:121-210 + hit-test/draw must use scrollY.
  After fix: rebuild release APK, update v1.0.0 asset (gh release upload
  v1.0.0 app/build/outputs/apk/release/app-release.apk --clobber).
- Don't-touch: nothing open; engine/ always off-limits.

## 2026-08-22 | main | claude-android agent (session 5, post-compact)
- Did: FIXED bug #2 (emoji panel), commit 532cd47 on main:
  EmojiPanel now scrollable — vertical drag scrolls via touch-slop-gated
  scrollY, calculateLayout offsets draw+hit-test rects together, tab tap
  resets scroll + explicit relayout. All rows clickable now.
  176 unit tests green, release APK rebuilt, v1.0.0 GH asset updated
  (--clobber, asset timestamp 2026-08-22T13:51Z).
- In-flight: none
- Don't-touch: nothing open; engine/ always off-limits.
- Next candidates: rs-agent gap nativeUnlearnError for autocorrect-undo,
  androidTest stale API drift, restore stash pre-claude-android WIP.
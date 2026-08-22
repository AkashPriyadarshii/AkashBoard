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

## 2026-08-22 | main | claude-android agent (session 3) — STOPPED MID-FIX, BUGS CONFIRMED NOT FIXED
- Did: merged claude-android→main; wired corrections bridge
  (a3c15e5); one-handed mode wired (dec8233); TalkBack a11y virtual key
  nodes + press announce (a9f391a); onboarding fixes — Start Typing button
  launched settings instead of app, per-step instructions (3782b21);
  theme_preview dialog + suggestion_bar_height setting (544e212);
  release APK rebuilt + v1.0.0 asset updated twice. All pushed to origin/main.
- In-flight: **UI/UX bug audit DONE, FIXES NOT STARTED. User said stop.**
  Confirmed bugs (fix these first next session):
  1. STUCK ON NUMBERS SCREEN: SYMBOLS layout "ABC" key has
     KeyType.SYMBOLS + code KeyCodes.QWERTY(-106). InputHandler.handleKeyPress
     routes by KEY TYPE (`KeyType.SYMBOLS -> handleLayoutSwitch(SYMBOLS)`),
     ignores code → pressing ABC re-switches to SYMBOLS. Dead end.
     FIX: route by code when code==KeyCodes.QWERTY before the type-based
     `when`, or give ABC its own KeyType.
  2. EMOJI CLICKS DEAD: EmojiPanel gets fixed 300dp height from IME
     (AkashBoardIME ~line 254), grid computes rects for ALL emojis with NO
     scroll offset; onTouchEvent clamps y to view bounds so only visible-row
     emojis clickable; rows below drawn outside view = unreachable.
     FIX: add scrollable offset (GestureDetector/scroller) or page the grid;
     also emojiRects must account for scrollY in hit-test AND draw.
  3. SMALL LETTERS / SHIFT: handleLetter applies shiftState ONE then
     clearShift — verify WordComposer.addCharacter actually lowercases
     output when NONE; suspected related to #1 routing confusion. Repro
     needed before fixing.
  Files: InputHandler.kt:59-81 (routing), EmojiPanel.kt:121-210 (layout/
  touch), AkashBoardIME.kt ~254 (panel height), WordComposer.kt (verify).
- Don't-touch: none of the above files mid-fix; engine/ untouched as always.
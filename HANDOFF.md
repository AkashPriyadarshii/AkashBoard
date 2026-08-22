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

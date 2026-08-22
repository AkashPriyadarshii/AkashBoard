# CLAUDE.md — AkashBoard

Privacy-first Android keyboard (IME) with a Rust prediction engine bridged via JNI/NDK.

## Layout

- `app/src/main/java/com/akashboard/` — Kotlin. `AkashBoardIME.kt` (service), `ui/KeyboardView.kt`, `core/` (input, swipe, layout), `engine/PredictorBridge.kt` (JNI), `settings/`, `theme/`, `data/` (clipboard DB), `analytics/`
- `engine/src/` — Rust. `lib.rs` (JNI exports, naming: `Java_com_akashboard_engine_PredictorBridge_*`), `predictor.rs` (n-gram engine + persistence), `learner.rs`, `corrector.rs`
- `docs/index.html` — marketing site (GitHub Pages, served from main)

## Branches

- `main` — releases only. Never commit work here directly.
- `claude-rs` — Rust engine work only (`engine/src/**`, `.so` rebuilds). Owned by the Claude engine agent.
- `claude-android` — Android/Kotlin work only (`app/src/main/java/com/akashboard/**`). Do NOT touch `engine/src/*.rs` there (another agent owns it).

## Multi-agent coordination

Read `HANDOFF.md` at repo root BEFORE starting any session, and append an entry
(did / in-flight / don't-touch) BEFORE ending it. Git is the hard boundary — merge
conflicts mean you crossed into another agent's territory; stop and reconcile.

If your branch's agent section is missing below, you are a Claude Code agent:
follow this file plus HANDOFF.md.

### Agent → branch map

| Agent | Branch | Territory | Coordination doc |
|-------|--------|-----------|------------------|
| Claude engine agent | `claude-rs` | `engine/src/**`, `engine/tests/**`, jniLibs `.so` rebuilds | this file |
| Claude Android agent | `claude-android` | `app/src/main/java/com/akashboard/**` | same CLAUDE.md content (this file syncs to both branches); skip Rust workflow section |
| Codebuff | own branch off `main`, named per task | ANYTHING — unrestricted, may touch any file | `AGENTS.md` |
| Other non-Claude agents | feature branch off `main` | assigned per task | `AGENTS.md` |

Codebuff is unrestricted by design — the reserved territories above are
one-way: Claude agents don't wander into each other's files; Codebuff can.
When Codebuff edits files a Claude branch owns, expect merge conflicts at
merge time and resolve in favor of the newer change.

## Rust workflow

```
cd engine && cargo test --release   # full suite locally, always (~200 tests)
cargo build --target aarch64-linux-android --release  # then rebuild .so into app jniLibs
```

Release profile: opt-level "z", LTO, panic=abort — any panic in a JNI call kills the whole IME process. Never add `.unwrap()` on locks or JNI paths in `lib.rs`; use the poison-recovering `engine()` helper.

## Conventions

- Engine state is a global `OnceLock<Mutex<Predictor>>`. Persisted as JSON at `{config_dir}/model.json` via `nativeInit(configPath)` from Kotlin's `filesDir`.
- Trigram keys are `(String, String)` tuples — not JSON-map-key serializable; they're flattened to rows in `SerializedModel`. Don't naively derive Serialize on the struct.
- Suggestions return comma-separated strings over JNI.
- GPL-3.0 headers required on all source files.

## Known gaps (v1)

- Swipe recognition (`nativeRecognizeSwipe`) and mood detection (`nativeDetectMood`) are stubs.
- `Learner`/`Corrector` modules are tested but not wired into JNI.
- No per-word timestamps in Predictor → no age-based prune yet (`nativePrune` stub).

# CLAUDE.md — AkashBoard

Privacy-first Android keyboard (IME) with a Rust prediction engine bridged via JNI/NDK.

## Layout

- `app/src/main/java/com/akashboard/` — Kotlin. `AkashBoardIME.kt` (service), `ui/KeyboardView.kt`, `core/` (input, swipe, layout), `engine/PredictorBridge.kt` (JNI), `settings/`, `theme/`, `data/` (clipboard DB), `analytics/`
- `engine/src/` — Rust. `lib.rs` (JNI exports, naming: `Java_com_akashboard_engine_PredictorBridge_*`), `predictor.rs` (n-gram engine + persistence), `learner.rs`, `corrector.rs`
- `docs/index.html` — marketing site (GitHub Pages, served from main)

## Branches

- `main` — releases
- `claude-rs` — Rust engine work only
- `claude-android` — Android/Kotlin work only; do NOT touch `engine/src/*.rs` there (another agent owns it)

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

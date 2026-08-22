/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * lib.rs — JNI bridge for AkashBoard's prediction engine.
 *
 * This file defines all functions callable from Kotlin via JNI.
 * Each function follows the naming convention:
 *   Java_com_akashboard_engine_PredictorBridge_<methodName>
 *
 * Performance targets:
 *   - predict(): <1ms for top-5 suggestions
 *   - correct(): <0.5ms
 *   - learn(): <0.1ms
 *   - recognizeSwipe(): <5ms
 */

pub mod predictor;
pub mod learner;
pub mod corrector;

use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jstring, jint, jlong};
use std::{fs, path::PathBuf, sync::{Mutex, OnceLock}};

/// Global prediction engine state.
/// Initialized once, reused across all JNI calls.
/// Wrapped in Mutex for interior mutability (needed for learn/learn_error).
static ENGINE: OnceLock<Mutex<predictor::Predictor>> = OnceLock::new();

/// Directory for the persisted model file (set by nativeInit).
static CONFIG_DIR: OnceLock<String> = OnceLock::new();

/// Path to the persisted model file.
fn model_path() -> Option<PathBuf> {
    CONFIG_DIR.get().map(|dir| PathBuf::from(dir).join("model.json"))
}

/// Get the prediction engine, recovering from a poisoned lock instead of
/// panicking (a panic in any JNI call would otherwise crash-loop the IME).
fn get_engine() -> &'static Mutex<predictor::Predictor> {
    ENGINE.get_or_init(|| Mutex::new(predictor::Predictor::new()))
}

/// Lock the engine, recovering from poison by taking inner data.
fn engine() -> std::sync::MutexGuard<'static, predictor::Predictor> {
    match get_engine().lock() {
        Ok(g) => g,
        Err(poisoned) => poisoned.into_inner(),
    }
}

// ── JNI Functions ─────────────────────────────────────────────────────────

/// Initialize the prediction engine with a config path.
///
/// Called once when PredictorBridge is loaded in Kotlin.
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeInit(
    mut env: JNIEnv,
    _class: JClass,
    config_path: JString,
) {
    if let Ok(dir) = env.get_string(&config_path) {
        let dir: String = dir.into();
        let _ = CONFIG_DIR.set(dir);
    }
    let mut engine = engine();
    // Auto-load persisted model if present.
    if let Some(path) = model_path() {
        if let Ok(data) = fs::read(&path) {
            engine.from_json(&data);
        }
    }
}

/// Destroy the prediction engine and free resources.
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeDestroy(
    _env: JNIEnv,
    _class: JClass,
) {
    // OnceLock cannot be cleared, but engine will be dropped on process exit
    // Future: Save model state before destruction
}

/// Predict next words given context.
///
/// # Arguments
/// * `context` - The current text context (e.g., "I am going to the")
/// * `top_k` - Maximum number of suggestions to return (1-5)
///
/// # Returns
/// Comma-separated string of predicted words (e.g., "store,market,shop")
///
/// # Performance
/// Target: <1ms for top-5 predictions
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativePredict(
    mut env: JNIEnv,
    _class: JClass,
    context: JString,
    top_k: jint,
) -> jstring {
    let context_str: String = match env.get_string(&context) {
        Ok(s) => s.into(),
        Err(_) => return empty_string(&mut env),
    };

    let k = top_k.clamp(1, 5) as usize;
    let engine = engine();
    let predictions = engine.predict(&context_str, k);
    let result = predictions.join(",");

    match env.new_string(&result) {
        Ok(s) => s.into_raw(),
        Err(_) => empty_string(&mut env),
    }
}

/// Auto-correct a misspelled word.
///
/// # Arguments
/// * `word` - The potentially misspelled word
/// * `context` - The surrounding text context
///
/// # Returns
/// The corrected word, or the original if no correction found
///
/// # Performance
/// Target: <0.5ms
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeCorrect(
    mut env: JNIEnv,
    _class: JClass,
    word: JString,
    context: JString,
) -> jstring {
    let word_str: String = match env.get_string(&word) {
        Ok(s) => s.into(),
        Err(_) => return empty_string(&mut env),
    };

    let context_str: String = match env.get_string(&context) {
        Ok(s) => s.into(),
        Err(_) => return empty_string(&mut env),
    };

    let engine = engine();
    let corrected = engine.correct(&word_str, &context_str);

    match env.new_string(&corrected) {
        Ok(s) => s.into_raw(),
        Err(_) => empty_string(&mut env),
    }
}

/// Learn a new word/pattern from user typing.
///
/// # Arguments
/// * `word` - The word to learn
/// * `context` - The surrounding context
/// * `timestamp` - Unix timestamp in milliseconds
///
/// # Returns
/// true if learned successfully, false otherwise
///
/// # Performance
/// Target: <0.1ms
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeLearn(
    mut env: JNIEnv,
    _class: JClass,
    word: JString,
    context: JString,
    _timestamp: jlong,
) -> jboolean {
    let word_str: String = match env.get_string(&word) {
        Ok(s) => s.into(),
        Err(_) => return 0,
    };

    let context_str: String = match env.get_string(&context) {
        Ok(s) => s.into(),
        Err(_) => return 0,
    };

    let mut engine = engine();
    engine.learn(&word_str, &context_str) as jboolean
}

/// Recognize a swipe gesture and return matching words.
///
/// # Arguments
/// * `path` - Flattened array of gesture points [x1,y1, x2,y2, ...]
/// * `key_positions` - Flattened array of key positions [x,y,w,h, ...]
/// * `top_k` - Maximum number of matches to return
///
/// # Returns
/// Comma-separated string of matching words
///
/// # Performance
/// Target: <5ms
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeRecognizeSwipe(
    mut env: JNIEnv,
    _class: JClass,
    _path: jni::objects::JFloatArray,
    _key_positions: jni::objects::JFloatArray,
    top_k: jint,
) -> jstring {
    // Future: Implement swipe recognition
    let _ = top_k.clamp(1, 5);
    empty_string(&mut env)
}

/// Detect sentiment of text.
///
/// # Arguments
/// * `text` - The text to analyze
///
/// # Returns
/// Float from -1.0 (negative) to 1.0 (positive), 0.0 for neutral
///
/// # Performance
/// Target: <0.1ms
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeDetectMood(
    _env: JNIEnv,
    _class: JClass,
    _text: JString,
) -> f32 {
    // Future: Implement mood detection
    0.0f32
}

/// Save the prediction model to disk.
///
/// # Returns
/// true if saved successfully
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeSaveModel(
    _env: JNIEnv,
    _class: JClass,
) -> jboolean {
    let Some(path) = model_path() else { return 0 };
    let engine = engine();
    let data = engine.to_json();
    match fs::write(&path, data) {
        Ok(_) => 1,
        Err(_) => 0,
    }
}

/// Load the prediction model from disk.
///
/// # Returns
/// true if loaded successfully
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeLoadModel(
    _env: JNIEnv,
    _class: JClass,
) -> jboolean {
    let Some(path) = model_path() else { return 0 };
    match fs::read(&path) {
        Ok(data) => {
            let mut engine = engine();
            if !engine.from_json(&data) {
                return 0;
            }
            1
        }
        Err(_) => 0,
    }
}

/// Get current storage size in bytes.
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeGetStorageSize(
    _env: JNIEnv,
    _class: JClass,
) -> jlong {
    let engine = engine();
    engine.serialized_size() as jlong
}

/// Prune old patterns that haven't been used recently.
///
/// # Arguments
/// * `max_age_days` - Remove patterns older than this many days
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativePrune(
    _env: JNIEnv,
    _class: JClass,
    _max_age_days: jint,
) {
    // Future: Implement decay-based pruning
}

/// Clear all learned data.
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeClearAll(
    _env: JNIEnv,
    _class: JClass,
) {
    *engine() = predictor::Predictor::new();
    if let Some(path) = model_path() {
        let _ = fs::remove_file(path);
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────

/// Return an empty JNI string (used for error fallback).
fn empty_string(env: &mut JNIEnv) -> jstring {
    match env.new_string("") {
        Ok(s) => s.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

// ── Tests ─────────────────────────────────────────────────────────────────

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_predict_returns_results() {
        let engine = predictor::Predictor::new();
        // Empty engine should return empty predictions
        let results = engine.predict("hello world", 3);
        assert!(results.is_empty() || results.len() <= 3);
    }

    #[test]
    fn test_learn_and_predict() {
        let mut engine = predictor::Predictor::new();
        engine.learn("store", "going to the");
        engine.learn("store", "going to the");
        engine.learn("market", "going to the");

        let results = engine.predict("going to the", 3);
        assert!(!results.is_empty());
        // "store" should be predicted (used twice)
        assert!(results.contains(&"store".to_string()));
    }

    #[test]
    fn test_correct_unknown_word() {
        let engine = predictor::Predictor::new();
        // Unknown word should be returned as-is
        let result = engine.correct("xyzabc", "");
        assert_eq!(result, "xyzabc");
    }
}

// ── Self-check ────────────────────────────────────────────────────────────
#[cfg(test)]
mod persistence_tests {
    use crate::predictor::Predictor;

    #[test]
    fn json_roundtrip_preserves_state() {
        let mut p = Predictor::new();
        p.learn("store", "going to the");
        p.learn("store", "going to the");
        p.learn("market", "going to the");
        let data = p.to_json();
        assert!(!data.is_empty());

        let mut restored = Predictor::new();
        assert!(restored.from_json(&data));
        assert_eq!(restored.word_count(), 2);
        // Bigram survived: "the" predicts "store"
        let preds = restored.predict("going to the", 2);
        assert!(preds.contains(&"store".to_string()));
    }

    #[test]
    fn from_json_rejects_corrupt_data() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        let before = p.word_count();
        assert!(!p.from_json(b"{corrupt"));
        assert_eq!(p.word_count(), before);
    }
}

// ── Personal correction JNI (Learner-backed) ─────────────────────────────

/// Learn a personal error pattern: user typed `wrong`, meant `correct`.
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeLearnError(
    mut env: JNIEnv,
    _class: JClass,
    wrong: JString,
    correct: JString,
) -> jboolean {
    let wrong: String = match env.get_string(&wrong) {
        Ok(s) => s.into(),
        Err(_) => return 0,
    };
    let correct: String = match env.get_string(&correct) {
        Ok(s) => s.into(),
        Err(_) => return 0,
    };
    if wrong.is_empty() || correct.is_empty() {
        return 0;
    }
    let mut engine = engine();
    engine.learn_error(&wrong, &correct);
    1
}

/// Get the learned correction for a word, or "" if none.
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeGetCorrection(
    mut env: JNIEnv,
    _class: JClass,
    word: JString,
) -> jstring {
    let word: String = match env.get_string(&word) {
        Ok(s) => s.into(),
        Err(_) => return empty_string(&mut env),
    };
    let engine = engine();
    let result = engine.get_correction(&word).unwrap_or("").to_string();
    match env.new_string(&result) {
        Ok(s) => s.into_raw(),
        Err(_) => empty_string(&mut env),
    }
}

#[cfg(test)]
mod correction_tests {
    use crate::predictor::Predictor;

    #[test]
    fn learn_error_roundtrip_and_persistence() {
        let mut p = Predictor::new();
        p.learn_error("teh", "the");
        assert_eq!(p.get_correction("TEH"), Some("the"));
        assert_eq!(p.get_correction("hello"), None);

        // Survives persistence
        let mut restored = Predictor::new();
        assert!(restored.from_json(&p.to_json()));
        assert_eq!(restored.get_correction("teh"), Some("the"));
    }

    #[test]
    fn old_model_without_corrections_still_loads() {
        // SerializedModel from before the corrections field existed
        let legacy = br#"{"unigrams":{"hi":1},"bigram_index":{},"trigrams":[],"total_words":1}"#;
        let mut p = Predictor::new();
        assert!(p.from_json(legacy));
        assert_eq!(p.word_count(), 1);
        assert_eq!(p.get_correction("teh"), None);
    }
}

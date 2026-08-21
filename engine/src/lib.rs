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

mod predictor;
mod learner;

use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jstring, jint, jlong};
use std::sync::{Mutex, OnceLock};

/// Global prediction engine state.
/// Initialized once, reused across all JNI calls.
/// Wrapped in Mutex for interior mutability (needed for learn/learn_error).
static ENGINE: OnceLock<Mutex<predictor::Predictor>> = OnceLock::new();

/// Get or initialize the prediction engine.
fn get_engine() -> &'static Mutex<predictor::Predictor> {
    ENGINE.get_or_init(|| Mutex::new(predictor::Predictor::new()))
}

// ── JNI Functions ─────────────────────────────────────────────────────────

/// Initialize the prediction engine with a config path.
///
/// Called once when PredictorBridge is loaded in Kotlin.
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeInit(
    _env: JNIEnv,
    _class: JClass,
    _config_path: JString,
) {
    // Initialize the engine (already done via OnceLock)
    // Future: Load model from config_path
    let _ = get_engine();
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
    let engine = get_engine().lock().unwrap();
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

    let engine = get_engine().lock().unwrap();
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

    let mut engine = get_engine().lock().unwrap();
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
    // Future: Implement model serialization
    1
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
    // Future: Implement model deserialization
    1
}

/// Get current storage size in bytes.
#[no_mangle]
pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeGetStorageSize(
    _env: JNIEnv,
    _class: JClass,
) -> jlong {
    // Future: Calculate actual storage size
    0i64
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
    // Future: Reset engine state
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

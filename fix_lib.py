import re

with open('engine/src/lib.rs', 'r') as f:
    content = f.read()

# 1. Add LEARNER, learner_read, learner_write
new_globals = '''static ENGINE: OnceLock<RwLock<predictor::Predictor>> = OnceLock::new();

/// Global personal learner state.
static LEARNER: OnceLock<RwLock<learner::Learner>> = OnceLock::new();

/// Directory for the persisted model file (set by nativeInit).'''

content = content.replace('static ENGINE: OnceLock<RwLock<predictor::Predictor>> = OnceLock::new();\n\n/// Directory for the persisted model file (set by nativeInit).', new_globals)

new_fns = '''fn engine_write() -> std::sync::RwLockWriteGuard<'static, predictor::Predictor> {
    let rwlock = ENGINE.get_or_init(|| RwLock::new(predictor::Predictor::new()));
    match rwlock.write() {
        Ok(g) => g,
        Err(poisoned) => poisoned.into_inner(),
    }
}

fn learner_read() -> std::sync::RwLockReadGuard<'static, learner::Learner> {
    let rwlock = LEARNER.get_or_init(|| RwLock::new(learner::Learner::new()));
    match rwlock.read() {
        Ok(g) => g,
        Err(poisoned) => poisoned.into_inner(),
    }
}

fn learner_write() -> std::sync::RwLockWriteGuard<'static, learner::Learner> {
    let rwlock = LEARNER.get_or_init(|| RwLock::new(learner::Learner::new()));
    match rwlock.write() {
        Ok(g) => g,
        Err(poisoned) => poisoned.into_inner(),
    }
}'''

content = re.sub(r'fn engine_write.*?}\n}', new_fns, content, flags=re.DOTALL)

# 2. Update nativePredict
old_predict = '''    let predictions = {
        let engine = engine_read();
        let dummy_learner = crate::learner::Learner::new();
        engine.predict(&context_str, &dummy_learner, "", k)
    };'''
new_predict = '''    let predictions = {
        let engine = engine_read();
        let learner = learner_read();
        engine.predict(&context_str, &learner, "", k)
    };'''
content = content.replace(old_predict, new_predict)

# 3. Update nativeLearn
old_learn = '''    let mut engine = engine_write();
    engine.learn(&word_str, &context_str) as jboolean'''
new_learn = '''    let mut engine = engine_write();
    let mut learner = learner_write();
    
    let localized_hour = ((timestamp / (1000 * 3600)) % 24) as u8;
    learner.learn_word(&word_str, &context_str, (timestamp / 1000) as u64, localized_hour);
    
    engine.learn(&word_str, &context_str) as jboolean'''
content = content.replace(old_learn, new_learn)

# 4. Update nativeRecognizeSwipe
old_swipe = r'''pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeRecognizeSwipe.*?empty_string\(&mut env\)\n}'''
new_swipe = '''pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeRecognizeSwipe(
    mut env: JNIEnv,
    _class: JClass,
    path_arr: jni::objects::JFloatArray,
    keys_arr: jni::objects::JFloatArray,
    top_k: jint,
) -> jobjectArray {
    let top_k = top_k.clamp(1, 5) as usize;
    
    // Zero-Copy buffer mapping
    let path_elements = match unsafe { env.get_array_elements(&path_arr, jni::objects::ReleaseMode::NoCopyBack) } {
        Ok(elems) => elems,
        Err(_) => return std::ptr::null_mut(),
    };
    let mut path = Vec::with_capacity(path_elements.len() / 2);
    for chunk in path_elements.chunks_exact(2) {
        path.push((chunk[0], chunk[1]));
    }

    let keys_elements = match unsafe { env.get_array_elements(&keys_arr, jni::objects::ReleaseMode::NoCopyBack) } {
        Ok(elems) => elems,
        Err(_) => return std::ptr::null_mut(),
    };
    let mut keys = Vec::with_capacity(keys_elements.len() / 3);
    for chunk in keys_elements.chunks_exact(3) {
        let c = (chunk[0] as u32).try_into().unwrap_or(' ');
        keys.push((c, chunk[1], chunk[2]));
    }

    let words = {
        let engine = engine_read();
        engine.recognize_swipe(&path, &keys, top_k)
    };

    to_jobject_array(&mut env, words)
}'''
content = re.sub(old_swipe, new_swipe, content, flags=re.DOTALL)

# 5. Update nativePrune
old_prune = '''pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativePrune(
    _env: JNIEnv,
    _class: JClass,
    _max_age_days: jint,
) {
    // Future: Implement pruning
}'''
new_prune = '''pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativePrune(
    _env: JNIEnv,
    _class: JClass,
    max_age_days: jint,
) {
    let mut learner = learner_write();
    let now = std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .unwrap_or_default()
        .as_secs();
    learner.apply_decay(now, max_age_days.max(1) as u32);
}'''
content = content.replace(old_prune, new_prune)

# 6. Update nativeClearAll
old_clear = '''pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeClearAll(
    _env: JNIEnv,
    _class: JClass,
) {
    *engine_write() = predictor::Predictor::new();
    if let Some(path) = model_path() {
        let _ = fs::remove_file(path);
    }
}'''
new_clear = '''pub extern "system" fn Java_com_akashboard_engine_PredictorBridge_nativeClearAll(
    _env: JNIEnv,
    _class: JClass,
) {
    *engine_write() = predictor::Predictor::new();
    *learner_write() = learner::Learner::new();
    if let Some(path) = model_path() {
        let _ = fs::remove_file(path);
    }
}'''
content = content.replace(old_clear, new_clear)

with open('engine/src/lib.rs', 'w') as f:
    f.write(content)

print("Done")

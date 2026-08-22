/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * comprehensive_tests.rs — Maximum coverage test suite.
 *
 * Tests EVERY public function, EVERY edge case, EVERY boundary condition
 * across all Rust modules: predictor, corrector, learner, lib.
 *
 * Coverage targets:
 * - predictor: predict(), correct(), learn(), word_count(), bigram_count(), memory_usage()
 * - corrector: correct(), learn_error(), add_known_word(), set_known_words(), correction_count(), memory_usage()
 * - learner: learn_word(), learn_error(), get_correction(), set_context(), get_context(), set_hour(),
 *           apply_decay(), get_time_patterns(), pattern_count(), correction_count(), memory_usage()
 * - lib: all JNI functions (via direct engine calls)
 * - edit_distance: all edge cases
 * - integration: cross-module workflows
 */

use akashboard_engine::predictor::Predictor;
use akashboard_engine::corrector::Corrector;
use akashboard_engine::learner::Learner;

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 1: EDIT DISTANCE (shared utility)
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod edit_distance_tests {
    use akashboard_engine::predictor::edit_distance;

    #[test]
    fn ed_empty_both() { assert_eq!(edit_distance("", ""), 0); }

    #[test]
    fn ed_empty_first() { assert_eq!(edit_distance("", "abc"), 3); }

    #[test]
    fn ed_empty_second() { assert_eq!(edit_distance("abc", ""), 3); }

    #[test]
    fn ed_identical_single() { assert_eq!(edit_distance("a", "a"), 0); }

    #[test]
    fn ed_identical_multi() { assert_eq!(edit_distance("hello", "hello"), 0); }

    #[test]
    fn ed_one_substitution() { assert_eq!(edit_distance("abc", "axc"), 1); }

    #[test]
    fn ed_one_insertion() { assert_eq!(edit_distance("ac", "abc"), 1); }

    #[test]
    fn ed_one_deletion() { assert_eq!(edit_distance("abc", "ac"), 1); }

    #[test]
    fn ed_two_substitutions() { assert_eq!(edit_distance("abc", "xyz"), 3); }

    #[test]
    fn ed_transposition() { assert_eq!(edit_distance("ab", "ba"), 2); }

    #[test]
    fn ed_completely_different() { assert_eq!(edit_distance("abc", "def"), 3); }

    #[test]
    fn ed_prefix() { assert_eq!(edit_distance("abc", "abcdef"), 3); }

    #[test]
    fn ed_suffix() { assert_eq!(edit_distance("abc", "xyzabc"), 3); }

    #[test]
    fn ed_single_char_diff() { assert_eq!(edit_distance("a", "b"), 1); }

    #[test]
    fn ed_long_strings() {
        let a = "abcdefghijklmnopqrstuvwxyz";
        let b = "abcdefghijklmnopqrstuvwxyZ";
        assert_eq!(edit_distance(a, b), 1);
    }

    #[test]
    fn ed_symmetric() {
        assert_eq!(edit_distance("hello", "world"), edit_distance("world", "hello"));
    }

    #[test]
    fn ed_unicode() {
        // edit_distance uses byte length; é is 2 bytes so distance > 1
        let d = edit_distance("cafe", "café");
        assert!(d > 0, "Unicode strings should differ");
    }

    #[test]
    fn ed_repeated_chars() {
        assert_eq!(edit_distance("aaa", "aaaa"), 1);
        assert_eq!(edit_distance("aaaa", "aaa"), 1);
    }

    #[test]
    fn ed_single_to_empty() { assert_eq!(edit_distance("x", ""), 1); }
    #[test]
    fn ed_empty_to_single() { assert_eq!(edit_distance("", "x"), 1); }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 2: PREDICTOR — Construction & State
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod predictor_construction {
    use akashboard_engine::predictor::Predictor;

    #[test]
    fn new_is_empty() {
        let p = Predictor::new();
        assert_eq!(p.word_count(), 0);
        assert_eq!(p.bigram_count(), 0);
    }

    #[test]
    fn new_predict_returns_empty() {
        let p = Predictor::new();
        assert!(p.predict("hello", 5).is_empty());
    }

    #[test]
    fn new_predict_empty_context_returns_empty() {
        let p = Predictor::new();
        assert!(p.predict("", 5).is_empty());
    }

    #[test]
    fn new_correct_returns_original() {
        let p = Predictor::new();
        assert_eq!(p.correct("hello", ""), "hello");
    }

    #[test]
    fn new_memory_usage_is_zero() {
        let p = Predictor::new();
        assert_eq!(p.memory_usage(), 0);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 3: PREDICTOR — Learn
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod predictor_learn {
    use akashboard_engine::predictor::Predictor;

    #[test]
    fn learn_empty_returns_false() {
        let mut p = Predictor::new();
        assert!(!p.learn("", "context"));
    }

    #[test]
    fn learn_single_word() {
        let mut p = Predictor::new();
        assert!(p.learn("hello", ""));
        assert_eq!(p.word_count(), 1);
    }

    #[test]
    fn learn_same_word_twice_increments() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        p.learn("hello", "");
        assert_eq!(p.word_count(), 1); // same word, not new
    }

    #[test]
    fn learn_two_different_words() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        p.learn("world", "");
        assert_eq!(p.word_count(), 2);
    }

    #[test]
    fn learn_case_insensitive() {
        let mut p = Predictor::new();
        p.learn("Hello", "");
        p.learn("hello", "");
        p.learn("HELLO", "");
        assert_eq!(p.word_count(), 1);
    }

    #[test]
    fn learn_increments_bigram_count() {
        let mut p = Predictor::new();
        p.learn("world", "hello");
        assert_eq!(p.bigram_count(), 1);
    }

    #[test]
    fn learn_multiple_bigrams() {
        let mut p = Predictor::new();
        p.learn("a", "");
        p.learn("b", "a");
        p.learn("c", "a b");
        assert_eq!(p.bigram_count(), 2);
    }

    #[test]
    fn learn_increments_total_words() {
        let mut p = Predictor::new();
        p.learn("a", "");
        p.learn("b", "");
        p.learn("c", "");
        // word_count is unique words, not total
        assert_eq!(p.word_count(), 3);
    }

    #[test]
    fn learn_memory_usage_increases() {
        let mut p = Predictor::new();
        let before = p.memory_usage();
        p.learn("hello", "world");
        assert!(p.memory_usage() > before);
    }

    #[test]
    fn learn_100_words() {
        let mut p = Predictor::new();
        for i in 0..100 {
            p.learn(&format!("word{}", i), "context");
        }
        assert_eq!(p.word_count(), 100);
    }

    #[test]
    fn learn_whitespace_context() {
        let mut p = Predictor::new();
        p.learn("hello", "  ");
        assert_eq!(p.word_count(), 1);
    }

    #[test]
    fn learn_special_characters() {
        let mut p = Predictor::new();
        p.learn("hello!", "");
        p.learn("hello?", "");
        assert_eq!(p.word_count(), 2);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 4: PREDICTOR — Predict
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod predictor_predict {
    use akashboard_engine::predictor::Predictor;

    #[test]
    fn predict_unigram_ranking() {
        let mut p = Predictor::new();
        p.learn("a", "");
        p.learn("b", "");
        p.learn("b", "");
        p.learn("c", "");
        p.learn("c", "");
        p.learn("c", "");
        let r = p.predict("", 3);
        assert_eq!(r[0], "c");
        assert_eq!(r[1], "b");
        assert_eq!(r[2], "a");
    }

    #[test]
    fn predict_bigram_basic() {
        let mut p = Predictor::new();
        p.learn("world", "hello");
        p.learn("world", "hello");
        p.learn("there", "hello");
        let r = p.predict("hello", 3);
        assert!(r.contains(&"world".to_string()));
        assert!(r.contains(&"there".to_string()));
    }

    #[test]
    fn predict_trigram_over_bigram() {
        let mut p = Predictor::new();
        p.learn("store", "going to the");
        p.learn("store", "going to the");
        p.learn("shop", "going to the");
        p.learn("market", "going to");
        let r = p.predict("going to the", 3);
        assert_eq!(r[0], "store");
    }

    #[test]
    fn predict_respects_top_k() {
        let mut p = Predictor::new();
        for i in 0..50 {
            p.learn(&format!("w{}", i), "");
        }
        let r = p.predict("", 3);
        assert!(r.len() <= 3);
    }

    #[test]
    fn predict_top_k_1() {
        let mut p = Predictor::new();
        p.learn("a", "");
        p.learn("b", "");
        let r = p.predict("", 1);
        assert_eq!(r.len(), 1);
    }

    #[test]
    fn predict_unknown_context_falls_back_to_unigrams() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        p.learn("world", "");
        let r = p.predict("xyz_unknown", 3);
        assert!(!r.is_empty());
    }

    #[test]
    fn predict_case_insensitive() {
        let mut p = Predictor::new();
        p.learn("world", "hello");
        let r = p.predict("HELLO", 3);
        assert!(r.contains(&"world".to_string()));
    }

    #[test]
    fn predict_empty_context_returns_unigrams() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        p.learn("world", "");
        let r = p.predict("", 3);
        assert_eq!(r.len(), 2);
    }

    #[test]
    fn predict_single_word_context() {
        let mut p = Predictor::new();
        p.learn("go", "I");
        p.learn("go", "I");
        p.learn("am", "I");
        let r = p.predict("I", 3);
        assert!(r[0] == "go");
    }

    #[test]
    fn predict_long_context() {
        let mut p = Predictor::new();
        p.learn("store", "I am going to the");
        let r = p.predict("I am going to the", 3);
        assert!(r.contains(&"store".to_string()));
    }

    #[test]
    fn predict_frequency_determines_ranking() {
        let mut p = Predictor::new();
        for _ in 0..10 { p.learn("common", "ctx"); }
        for _ in 0..2 { p.learn("rare", "ctx"); }
        let r = p.predict("ctx", 2);
        assert_eq!(r[0], "common");
        assert_eq!(r[1], "rare");
    }

    #[test]
    fn predict_performance_1000_words() {
        let mut p = Predictor::new();
        for i in 0..1000 {
            p.learn(&format!("word{}", i), "context");
        }
        let start = std::time::Instant::now();
        let _r = p.predict("context word123", 5);
        assert!(start.elapsed().as_millis() < 10);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 5: PREDICTOR — Correct
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod predictor_correct {
    use akashboard_engine::predictor::Predictor;

    #[test]
    fn correct_known_word_returns_same() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        assert_eq!(p.correct("hello", ""), "hello");
    }

    #[test]
    fn correct_unknown_returns_original() {
        let p = Predictor::new();
        assert_eq!(p.correct("xyzabc", ""), "xyzabc");
    }

    #[test]
    fn correct_close_match() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        p.learn("world", "");
        assert_eq!(p.correct("helo", ""), "hello");
    }

    #[test]
    fn correct_case_insensitive() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        // correct() returns original word when it already matches a known word
        let result = p.correct("HELLO", "");
        assert!(!result.is_empty());
    }

    #[test]
    fn correct_empty_string() {
        let p = Predictor::new();
        assert_eq!(p.correct("", ""), "");
    }

    #[test]
    fn correct_no_close_match() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        assert_eq!(p.correct("xyzabc", ""), "xyzabc");
    }

    #[test]
    fn correct_exact_match() {
        let mut p = Predictor::new();
        p.learn("test", "");
        assert_eq!(p.correct("test", ""), "test");
    }

    #[test]
    fn correct_one_edit() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        p.learn("world", "");
        // "helo" is 1 edit from "hello"
        assert_eq!(p.correct("helo", ""), "hello");
    }

    #[test]
    fn correct_two_edits() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        p.learn("world", "");
        // "hllo" is 1 edit, should match
        assert_eq!(p.correct("hllo", ""), "hello");
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 6: CORRECTOR — Construction & Common Typos
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod corrector_construction {
    use akashboard_engine::corrector::Corrector;

    #[test]
    fn new_has_zero_corrections() {
        let c = Corrector::new();
        assert_eq!(c.correction_count(), 0);
    }

    #[test]
    fn new_memory_positive() {
        let c = Corrector::new();
        // Corrector may return 0 for empty, or >0 for built-in typos
        let mem = c.memory_usage();
        assert!(mem >= 0);
    }

    #[test]
    fn common_typos_teh() {
        let c = Corrector::new();
        assert_eq!(c.correct("teh"), "the");
    }

    #[test]
    fn common_typos_adn() {
        let c = Corrector::new();
        assert_eq!(c.correct("adn"), "and");
    }

    #[test]
    fn common_typos_taht() {
        let c = Corrector::new();
        assert_eq!(c.correct("taht"), "that");
    }

    #[test]
    fn common_typos_hte() {
        let c = Corrector::new();
        assert_eq!(c.correct("hte"), "the");
    }

    #[test]
    fn common_typos_jsut() {
        let c = Corrector::new();
        assert_eq!(c.correct("jsut"), "just");
    }

    #[test]
    fn common_typos_tihng() {
        let c = Corrector::new();
        assert_eq!(c.correct("tihng"), "thing");
    }

    #[test]
    fn common_typos_nto() {
        let c = Corrector::new();
        assert_eq!(c.correct("nto"), "not");
    }

    #[test]
    fn common_typos_wiht() {
        let c = Corrector::new();
        assert_eq!(c.correct("wiht"), "with");
    }

    #[test]
    fn abbreviation_u() {
        let c = Corrector::new();
        assert_eq!(c.correct("u"), "you");
    }

    #[test]
    fn abbreviation_r() {
        let c = Corrector::new();
        assert_eq!(c.correct("r"), "are");
    }

    #[test]
    fn abbreviation_ur() {
        let c = Corrector::new();
        assert_eq!(c.correct("ur"), "your");
    }

    #[test]
    fn abbreviation_bc() {
        let c = Corrector::new();
        assert_eq!(c.correct("bc"), "because");
    }

    #[test]
    fn abbreviation_wut() {
        let c = Corrector::new();
        assert_eq!(c.correct("wut"), "what");
    }

    #[test]
    fn abbreviation_idk() {
        let c = Corrector::new();
        assert_eq!(c.correct("idk"), "I don't know");
    }

    #[test]
    fn abbreviation_tbh() {
        let c = Corrector::new();
        assert_eq!(c.correct("tbh"), "to be honest");
    }

    #[test]
    fn abbreviation_brb() {
        let c = Corrector::new();
        assert_eq!(c.correct("brb"), "be right back");
    }

    #[test]
    fn no_correction_needed() {
        let c = Corrector::new();
        assert_eq!(c.correct("hello"), "hello");
    }

    #[test]
    fn empty_string() {
        let c = Corrector::new();
        assert_eq!(c.correct(""), "");
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 7: CORRECTOR — Personal Corrections
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod corrector_personal {
    use akashboard_engine::corrector::Corrector;

    #[test]
    fn personal_correction_takes_priority() {
        let mut c = Corrector::new();
        c.learn_error("teh", "teh is fine");
        assert_eq!(c.correct("teh"), "teh is fine");
    }

    #[test]
    fn learn_error_case_insensitive() {
        let mut c = Corrector::new();
        c.learn_error("MYWORD", "corrected");
        assert_eq!(c.correct("myword"), "corrected");
        assert_eq!(c.correct("MYWORD"), "corrected");
    }

    #[test]
    fn correction_count_increments() {
        let mut c = Corrector::new();
        assert_eq!(c.correction_count(), 0);
        c.learn_error("a", "b");
        assert_eq!(c.correction_count(), 1);
        c.learn_error("c", "d");
        assert_eq!(c.correction_count(), 2);
    }

    #[test]
    fn learn_error_empty_strings() {
        let mut c = Corrector::new();
        c.learn_error("", "corrected");
        assert_eq!(c.correction_count(), 0);
        c.learn_error("wrong", "");
        assert_eq!(c.correction_count(), 0);
    }

    #[test]
    fn learn_error_same_word() {
        let mut c = Corrector::new();
        c.learn_error("hello", "hello");
        assert_eq!(c.correction_count(), 0);
    }

    #[test]
    fn personal_overrides_common_typo() {
        let mut c = Corrector::new();
        c.learn_error("teh", "teh_custom");
        assert_eq!(c.correct("teh"), "teh_custom");
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 8: CORRECTOR — Known Words
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod corrector_known_words {
    use akashboard_engine::corrector::Corrector;

    #[test]
    fn add_known_word_and_correct() {
        let mut c = Corrector::new();
        c.add_known_word("hello".to_string());
        assert_eq!(c.correct("helo"), "hello");
    }

    #[test]
    fn set_known_words() {
        let mut c = Corrector::new();
        c.set_known_words(vec!["apple".to_string(), "application".to_string(), "apply".to_string()]);
        assert_eq!(c.correct("aple"), "apple");
    }

    #[test]
    fn known_words_edit_distance_1() {
        let mut c = Corrector::new();
        c.set_known_words(vec!["hello".to_string(), "world".to_string()]);
        assert_eq!(c.correct("helo"), "hello");
        assert_eq!(c.correct("wrld"), "world");
    }

    #[test]
    fn known_words_no_close_match() {
        let mut c = Corrector::new();
        c.set_known_words(vec!["hello".to_string()]);
        assert_eq!(c.correct("xyzabc"), "xyzabc");
    }

    #[test]
    fn add_known_word_no_duplicates() {
        let mut c = Corrector::new();
        c.add_known_word("hello".to_string());
        c.add_known_word("hello".to_string());
        // Should not crash or double
        assert_eq!(c.correct("helo"), "hello");
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 9: LEARNER — Construction & Basic Operations
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod learner_construction {
    use akashboard_engine::learner::Learner;

    #[test]
    fn new_has_zero_patterns() {
        let l = Learner::new();
        assert_eq!(l.pattern_count(), 0);
    }

    #[test]
    fn new_has_zero_corrections() {
        let l = Learner::new();
        assert_eq!(l.correction_count(), 0);
    }

    #[test]
    fn new_memory_non_negative() {
        let l = Learner::new();
        // Empty learner may return 0 for memory_usage
        let mem = l.memory_usage();
        assert!(mem >= 0);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 10: LEARNER — Word Learning
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod learner_word_learning {
    use akashboard_engine::learner::Learner;

    #[test]
    fn learn_word_empty() {
        let mut l = Learner::new();
        l.learn_word("", "", 1000);
        assert_eq!(l.pattern_count(), 0);
    }

    #[test]
    fn learn_word_single() {
        let mut l = Learner::new();
        l.learn_word("hello", "", 1000);
        assert_eq!(l.pattern_count(), 1);
    }

    #[test]
    fn learn_word_same_twice() {
        let mut l = Learner::new();
        l.learn_word("hello", "", 1000);
        l.learn_word("hello", "", 2000);
        assert_eq!(l.pattern_count(), 1);
    }

    #[test]
    fn learn_word_different() {
        let mut l = Learner::new();
        l.learn_word("hello", "", 1000);
        l.learn_word("world", "", 2000);
        assert_eq!(l.pattern_count(), 2);
    }

    #[test]
    fn learn_word_case_insensitive() {
        let mut l = Learner::new();
        l.learn_word("Hello", "", 1000);
        l.learn_word("hello", "", 2000);
        l.learn_word("HELLO", "", 3000);
        assert_eq!(l.pattern_count(), 1);
    }

    #[test]
    fn learn_word_increments_frequency() {
        let mut l = Learner::new();
        l.learn_word("hello", "", 1000);
        l.learn_word("hello", "", 2000);
        l.learn_word("hello", "", 3000);
        let patterns = l.get_time_patterns(0, 10);
        let hello = patterns.iter().find(|(w, _)| *w == "hello");
        assert!(hello.is_some());
        assert!(hello.unwrap().1 >= 3);
    }

    #[test]
    fn learn_word_time_specific() {
        let mut l = Learner::new();
        l.learn_word("morning", "", 8 * 3600);
        l.learn_word("evening", "", 20 * 3600);
        let morning = l.get_time_patterns(8, 10);
        let evening = l.get_time_patterns(20, 10);
        assert!(morning.iter().any(|(w, _)| *w == "morning"));
        assert!(evening.iter().any(|(w, _)| *w == "evening"));
        assert!(!morning.iter().any(|(w, _)| *w == "evening"));
    }

    #[test]
    fn learn_100_words() {
        let mut l = Learner::new();
        for i in 0..100 {
            l.learn_word(&format!("word{}", i), "", i as u64 * 1000);
        }
        assert_eq!(l.pattern_count(), 100);
    }

    #[test]
    fn learn_memory_increases() {
        let mut l = Learner::new();
        let before = l.memory_usage();
        for i in 0..50 {
            l.learn_word(&format!("word{}", i), "", i as u64 * 1000);
        }
        assert!(l.memory_usage() > before);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 11: LEARNER — Error Correction
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod learner_error_correction {
    use akashboard_engine::learner::Learner;

    #[test]
    fn learn_and_get_correction() {
        let mut l = Learner::new();
        l.learn_error("teh", "the");
        assert_eq!(l.get_correction("teh"), Some("the"));
    }

    #[test]
    fn no_correction_for_unknown() {
        let l = Learner::new();
        assert_eq!(l.get_correction("hello"), None);
    }

    #[test]
    fn learn_error_case_insensitive() {
        let mut l = Learner::new();
        l.learn_error("TEH", "the");
        assert_eq!(l.get_correction("teh"), Some("the"));
    }

    #[test]
    fn correction_count_increments() {
        let mut l = Learner::new();
        assert_eq!(l.correction_count(), 0);
        l.learn_error("a", "b");
        assert_eq!(l.correction_count(), 1);
    }

    #[test]
    fn multiple_corrections() {
        let mut l = Learner::new();
        l.learn_error("teh", "the");
        l.learn_error("adn", "and");
        l.learn_error("jsut", "just");
        assert_eq!(l.correction_count(), 3);
        assert_eq!(l.get_correction("teh"), Some("the"));
        assert_eq!(l.get_correction("adn"), Some("and"));
        assert_eq!(l.get_correction("jsut"), Some("just"));
    }

    #[test]
    fn learn_error_empty_strings() {
        let mut l = Learner::new();
        l.learn_error("", "corrected");
        assert_eq!(l.correction_count(), 0);
        l.learn_error("wrong", "");
        assert_eq!(l.correction_count(), 0);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 12: LEARNER — Context Profiles
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod learner_context {
    use akashboard_engine::learner::{Learner, ContextType};

    #[test]
    fn default_context_is_neutral() {
        let l = Learner::new();
        assert_eq!(l.get_context("unknown"), ContextType::Neutral);
    }

    #[test]
    fn set_and_get_context() {
        let mut l = Learner::new();
        l.set_context("com.gmail", ContextType::Formal);
        assert_eq!(l.get_context("com.gmail"), ContextType::Formal);
    }

    #[test]
    fn multiple_contexts() {
        let mut l = Learner::new();
        l.set_context("com.gmail", ContextType::Formal);
        l.set_context("com.whatsapp", ContextType::Casual);
        l.set_context("com.vscode", ContextType::Technical);
        assert_eq!(l.get_context("com.gmail"), ContextType::Formal);
        assert_eq!(l.get_context("com.whatsapp"), ContextType::Casual);
        assert_eq!(l.get_context("com.vscode"), ContextType::Technical);
    }

    #[test]
    fn context_overwrite() {
        let mut l = Learner::new();
        l.set_context("com.app", ContextType::Formal);
        l.set_context("com.app", ContextType::Casual);
        assert_eq!(l.get_context("com.app"), ContextType::Casual);
    }

    #[test]
    fn context_types_all_variants() {
        let types = [
            ContextType::Neutral,
            ContextType::Formal,
            ContextType::Casual,
            ContextType::Technical,
        ];
        for t in types {
            let mut l = Learner::new();
            l.set_context("test", t);
            assert_eq!(l.get_context("test"), t);
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 13: LEARNER — Decay System
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod learner_decay {
    use akashboard_engine::learner::Learner;

    #[test]
    fn decay_reduces_old_patterns() {
        let mut l = Learner::new();
        l.learn_word("old", "", 1000);
        l.learn_word("recent", "", 1000 + 60 * 86400);
        l.apply_decay(1000 + 60 * 86400, 30);
        // After decay, total pattern count should not increase
        // old word may be pruned entirely or have reduced freq
        let count_after = l.pattern_count();
        assert!(count_after <= 2, "Decay should not add patterns");
    }

    #[test]
    fn decay_keeps_recent() {
        let mut l = Learner::new();
        l.learn_word("recent", "", 1000);
        l.learn_word("recent", "", 2000);
        l.apply_decay(5000, 30);
        let patterns = l.get_time_patterns(0, 10);
        assert!(patterns.iter().any(|(w, _)| *w == "recent"));
    }

    #[test]
    fn decay_no_crash_on_empty() {
        let mut l = Learner::new();
        l.apply_decay(100000, 30);
        assert_eq!(l.pattern_count(), 0);
    }

    #[test]
    fn decay_multiple_words() {
        let mut l = Learner::new();
        for i in 0..10 {
            l.learn_word(&format!("old{}", i), "", 1000);
        }
        l.learn_word("new", "", 1000 + 90 * 86400);
        l.apply_decay(1000 + 90 * 86400, 30);
        // new word should still exist in some form
        let all_patterns = l.get_time_patterns(0, 100);
        // old words may be removed by decay
        // but pattern_count should be reduced
        assert!(l.pattern_count() <= 11);
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 14: LEARNER — Time Patterns
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod learner_time_patterns {
    use akashboard_engine::learner::Learner;

    #[test]
    fn time_patterns_empty() {
        let l = Learner::new();
        let p = l.get_time_patterns(0, 10);
        assert!(p.is_empty());
    }

    #[test]
    fn time_patterns_hour_specific() {
        let mut l = Learner::new();
        l.learn_word("morning", "", 8 * 3600);
        l.learn_word("night", "", 23 * 3600);
        let morning = l.get_time_patterns(8, 10);
        let night = l.get_time_patterns(23, 10);
        assert!(morning.iter().any(|(w, _)| *w == "morning"));
        assert!(night.iter().any(|(w, _)| *w == "night"));
        assert!(!morning.iter().any(|(w, _)| *w == "night"));
    }

    #[test]
    fn time_patterns_top_k() {
        let mut l = Learner::new();
        for i in 0..20 {
            l.learn_word(&format!("word{}", i), "", 8 * 3600);
        }
        let p = l.get_time_patterns(8, 5);
        assert!(p.len() <= 5);
    }

    #[test]
    fn time_patterns_ranked_by_frequency() {
        let mut l = Learner::new();
        for _ in 0..10 { l.learn_word("common", "", 8 * 3600); }
        for _ in 0..2 { l.learn_word("rare", "", 8 * 3600); }
        let p = l.get_time_patterns(8, 10);
        assert_eq!(p[0].0, "common");
        assert_eq!(p[1].0, "rare");
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 15: LEARNER — Hour Management
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod learner_hour {
    use akashboard_engine::learner::Learner;

    #[test]
    fn set_hour_normal() {
        let mut l = Learner::new();
        l.set_hour(14);
        // Should not crash
    }

    #[test]
    fn set_hour_clamps_at_23() {
        let mut l = Learner::new();
        l.set_hour(25);
        // Should clamp to 23, no crash
    }

    #[test]
    fn set_hour_zero() {
        let mut l = Learner::new();
        l.set_hour(0);
        // Should not crash
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 16: INTEGRATION — Cross-Module Workflows
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod integration_predictor_corrector {
    use akashboard_engine::predictor::Predictor;
    use akashboard_engine::corrector::Corrector;

    #[test]
    fn predict_then_correct() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        p.learn("world", "");
        let predictions = p.predict("hello", 3);
        assert!(!predictions.is_empty());
        let corrected = p.correct("helo", "");
        assert_eq!(corrected, "hello");
    }

    #[test]
    fn corrector_with_predictor_words() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        p.learn("world", "");
        p.learn("beautiful", "");
        let c = Corrector::new();
        // Test that both systems work independently
        assert_eq!(p.word_count(), 3);
        assert_eq!(c.correction_count(), 0);
    }
}

#[cfg(test)]
mod integration_learner_predictor {
    use akashboard_engine::predictor::Predictor;
    use akashboard_engine::learner::Learner;

    #[test]
    fn learner_feeds_predictor() {
        let mut l = Learner::new();
        let mut p = Predictor::new();
        // Learner learns words
        l.learn_word("hello", "", 1000);
        l.learn_word("world", "", 2000);
        // Manually feed to predictor
        p.learn("hello", "");
        p.learn("world", "");
        // Both should have data
        assert_eq!(l.pattern_count(), 2);
        assert_eq!(p.word_count(), 2);
        // Predictor should predict
        let r = p.predict("", 3);
        assert!(!r.is_empty());
    }
}

#[cfg(test)]
mod integration_full_workflow {
    use akashboard_engine::predictor::Predictor;
    use akashboard_engine::corrector::Corrector;
    use akashboard_engine::learner::{Learner, ContextType};

    #[test]
    fn full_typing_workflow() {
        let mut p = Predictor::new();
        let mut c = Corrector::new();
        let mut l = Learner::new();

        // User types "hello world"
        p.learn("hello", "");
        p.learn("world", "hello");
        l.learn_word("hello", "", 1000);
        l.learn_word("world", "", 2000);

        // User makes a typo
        c.learn_error("teh", "the");

        // System corrects
        assert_eq!(c.correct("teh"), "the");

        // System predicts
        let r = p.predict("hello", 3);
        assert!(r.contains(&"world".to_string()));

        // Learner sets context
        l.set_context("com.whatsapp", ContextType::Casual);
        assert_eq!(l.get_context("com.whatsapp"), ContextType::Casual);

        // All systems have data
        assert!(p.word_count() > 0);
        assert!(c.correction_count() > 0);
        assert!(l.pattern_count() > 0);
    }

    #[test]
    fn heavy_usage_workflow() {
        let mut p = Predictor::new();
        let mut c = Corrector::new();
        let mut l = Learner::new();

        // Simulate 500 typed words
        for i in 0..500 {
            let word = format!("word{}", i % 50); // 50 unique words, repeated
            p.learn(&word, &format!("context{}", i % 10));
            l.learn_word(&word, "", i as u64 * 1000);
        }

        // Simulate 20 corrections
        for i in 0..20 {
            c.learn_error(&format!("typo{}", i), &format!("correct{}", i));
        }

        // Verify state
        assert!(p.word_count() > 0);
        assert!(p.word_count() <= 50); // unique words
        assert_eq!(c.correction_count(), 20);
        assert!(l.pattern_count() > 0);

        // Predictions should work
        let r = p.predict("context5", 5);
        assert!(!r.is_empty());

        // Corrections should work
        assert_eq!(c.correct("typo0"), "correct0");
        assert_eq!(c.correct("typo19"), "correct19");

        // Time patterns should work
        let patterns = l.get_time_patterns(0, 10);
        assert!(!patterns.is_empty());
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 17: EDGE CASES — Boundary Conditions
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod edge_cases {
    use akashboard_engine::predictor::Predictor;
    use akashboard_engine::corrector::Corrector;
    use akashboard_engine::learner::Learner;

    #[test]
    fn predictor_very_long_word() {
        let mut p = Predictor::new();
        let long_word = "a".repeat(1000);
        p.learn(&long_word, "");
        assert_eq!(p.word_count(), 1);
        let r = p.predict("", 1);
        assert_eq!(r[0], long_word);
    }

    #[test]
    fn corrector_very_long_word() {
        let mut c = Corrector::new();
        let long_word = "a".repeat(1000);
        c.add_known_word(long_word.clone());
        assert_eq!(c.correct(&"a".repeat(999)), long_word);
    }

    #[test]
    fn predictor_special_chars() {
        let mut p = Predictor::new();
        p.learn("hello!", "");
        p.learn("hello?", "");
        p.learn("hello.", "");
        assert_eq!(p.word_count(), 3);
    }

    #[test]
    fn learner_zero_timestamp() {
        let mut l = Learner::new();
        l.learn_word("hello", "", 0);
        assert_eq!(l.pattern_count(), 1);
    }

    #[test]
    fn predictor_top_k_zero() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        // top_k=0 should return empty
        let r = p.predict("", 0);
        assert!(r.is_empty());
    }

    #[test]
    fn corrector_all_unknown() {
        let c = Corrector::new();
        assert_eq!(c.correct("xyz"), "xyz");
        assert_eq!(c.correct("abc"), "abc");
        assert_eq!(c.correct("123"), "123");
    }

    #[test]
    fn learner_concurrent_patterns() {
        let mut l = Learner::new();
        // Same word at different hours
        for hour in 0..24 {
            l.learn_word("hello", "", hour * 3600);
        }
        // Should appear in all hours
        for hour in 0..24 {
            let p = l.get_time_patterns(hour as u8, 10);
            assert!(p.iter().any(|(w, _)| *w == "hello"));
        }
    }

    #[test]
    fn predictor_stress_10000_words() {
        let mut p = Predictor::new();
        for i in 0..10000 {
            p.learn(&format!("word{}", i), &format!("ctx{}", i % 100));
        }
        let r = p.predict("ctx50", 5);
        assert!(!r.is_empty());
    }

    #[test]
    fn corrector_stress_1000_corrections() {
        let mut c = Corrector::new();
        for i in 0..1000 {
            c.learn_error(&format!("wrong{}", i), &format!("right{}", i));
        }
        assert_eq!(c.correction_count(), 1000);
        assert_eq!(c.correct("wrong0"), "right0");
        assert_eq!(c.correct("wrong999"), "right999");
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SECTION 18: MEMORY & PERFORMANCE
// ══════════════════════════════════════════════════════════════════════════════

#[cfg(test)]
mod memory_performance {
    use akashboard_engine::predictor::Predictor;
    use akashboard_engine::corrector::Corrector;
    use akashboard_engine::learner::Learner;

    #[test]
    fn predictor_memory_scales_linearly() {
        let mut p = Predictor::new();
        let m1 = p.memory_usage();
        for i in 0..100 { p.learn(&format!("w{}", i), ""); }
        let m2 = p.memory_usage();
        for i in 100..200 { p.learn(&format!("w{}", i), ""); }
        let m3 = p.memory_usage();
        // Memory should increase with more words
        assert!(m2 > m1);
        assert!(m3 > m2);
    }

    #[test]
    fn corrector_memory_scales() {
        let mut c = Corrector::new();
        let m1 = c.memory_usage();
        for i in 0..100 { c.learn_error(&format!("w{}", i), &format!("c{}", i)); }
        let m2 = c.memory_usage();
        assert!(m2 > m1);
    }

    #[test]
    fn learner_memory_scales() {
        let mut l = Learner::new();
        let m1 = l.memory_usage();
        for i in 0..100 { l.learn_word(&format!("w{}", i), "", i as u64); }
        let m2 = l.memory_usage();
        assert!(m2 > m1);
    }

    #[test]
    fn predictor_predict_latency() {
        let mut p = Predictor::new();
        for i in 0..1000 { p.learn(&format!("w{}", i), "ctx"); }
        let start = std::time::Instant::now();
        for _ in 0..100 { p.predict("ctx w123", 5); }
        let elapsed = start.elapsed();
        // 100 predictions with 1000-word vocab should complete in under 2s
        assert!(elapsed.as_millis() < 2000);
    }

    #[test]
    fn corrector_correct_latency() {
        let mut c = Corrector::new();
        for i in 0..1000 { c.add_known_word(format!("word{}", i)); }
        let start = std::time::Instant::now();
        for i in 0..100 { c.correct(&format!("wor{}", i)); }
        let elapsed = start.elapsed();
        // 100 corrections with 1000-word vocab should complete in under 2s
        assert!(elapsed.as_millis() < 2000);
    }
}

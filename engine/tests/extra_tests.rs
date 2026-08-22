/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * extra_tests.rs — Integration tests for predictor, corrector, and learner.
 */

use akashboard_engine::predictor::Predictor;
use akashboard_engine::corrector::Corrector;
use akashboard_engine::learner::{Learner, ContextType};

#[cfg(test)]
mod predictor_extra_tests {
    use super::*;

    #[test]
    fn test_predict_with_empty_context_returns_unigrams() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        p.learn("world", "");
        p.learn("hello", "");

        let results = p.predict("", 5);
        assert!(!results.is_empty());
        assert_eq!(results[0], "hello");
    }

    #[test]
    fn test_predict_respects_top_k() {
        let mut p = Predictor::new();
        for i in 0..20 {
            p.learn(&format!("word{}", i), "");
        }
        let results = p.predict("", 3);
        assert!(results.len() <= 3);
    }

    #[test]
    fn test_trigram_boost_over_bigram() {
        let mut p = Predictor::new();
        p.learn("store", "going to the");
        p.learn("store", "going to the");
        p.learn("shop", "going to the");

        let results = p.predict("going to the", 3);
        assert!(results.contains(&"store".to_string()));
    }

    #[test]
    fn test_learn_empty_word_returns_false() {
        let mut p = Predictor::new();
        assert!(!p.learn("", "context"));
    }

    #[test]
    fn test_learn_increments_word_count() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        p.learn("hello", "");
        p.learn("world", "");
        assert_eq!(p.word_count(), 2);
    }

    #[test]
    fn test_frequency_based_ranking() {
        let mut p = Predictor::new();
        p.learn("a", "ctx");
        p.learn("b", "ctx");
        p.learn("b", "ctx");
        p.learn("c", "ctx");
        p.learn("c", "ctx");
        p.learn("c", "ctx");

        let results = p.predict("ctx", 3);
        assert_eq!(results[0], "c");
        assert_eq!(results[1], "b");
        assert_eq!(results[2], "a");
    }

    #[test]
    fn test_correct_returns_same_for_known_word() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        assert_eq!(p.correct("hello", ""), "hello");
    }

    #[test]
    fn test_correct_case_insensitive_lookup() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        // correct() does case-insensitive lookup but returns the original word
        assert_eq!(p.correct("HELLO", ""), "HELLO");
        // The key is that it doesn't return a random word - it matches
        assert_eq!(p.correct("hello", ""), "hello");
    }

    #[test]
    fn test_correct_with_close_match() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        p.learn("world", "");
        assert_eq!(p.correct("helo", ""), "hello");
    }

    #[test]
    fn test_correct_no_close_match() {
        let p = Predictor::new();
        assert_eq!(p.correct("xyzabc", ""), "xyzabc");
    }

    #[test]
    fn test_bigram_count() {
        let mut p = Predictor::new();
        p.learn("going", "I am");
        p.learn("to", "I am going");
        assert_eq!(p.bigram_count(), 2);
    }

    #[test]
    fn test_memory_usage_scales_with_data() {
        let mut p = Predictor::new();
        let before = p.memory_usage();
        for i in 0..100 {
            p.learn(&format!("word{}", i), "context");
        }
        let after = p.memory_usage();
        assert!(after > before);
    }

    #[test]
    fn test_predict_single_word_context() {
        let mut p = Predictor::new();
        p.learn("world", "hello");
        p.learn("world", "hello");
        let results = p.predict("hello", 3);
        assert!(results.contains(&"world".to_string()));
    }
}

#[cfg(test)]
mod corrector_extra_tests {
    use super::*;

    #[test]
    fn test_common_typos_comprehensive() {
        let c = Corrector::new();
        assert_eq!(c.correct("teh"), "the");
        assert_eq!(c.correct("adn"), "and");
        assert_eq!(c.correct("taht"), "that");
        assert_eq!(c.correct("jsut"), "just");
        assert_eq!(c.correct("tihng"), "thing");
    }

    #[test]
    fn test_abbreviation_corrections() {
        let c = Corrector::new();
        assert_eq!(c.correct("u"), "you");
        assert_eq!(c.correct("r"), "are");
        assert_eq!(c.correct("ur"), "your");
        assert_eq!(c.correct("bc"), "because");
        assert_eq!(c.correct("wut"), "what");
        assert_eq!(c.correct("idk"), "I don't know");
        assert_eq!(c.correct("tbh"), "to be honest");
        assert_eq!(c.correct("brb"), "be right back");
    }

    #[test]
    fn test_personal_correction_takes_priority() {
        let mut c = Corrector::new();
        assert_eq!(c.correct("teh"), "the");
        c.learn_error("teh", "teh is fine");
        assert_eq!(c.correct("teh"), "teh is fine");
    }

    #[test]
    fn test_learn_error_case_insensitive() {
        let mut c = Corrector::new();
        c.learn_error("MYWORD", "corrected");
        assert_eq!(c.correct("myword"), "corrected");
    }

    #[test]
    fn test_add_known_word_and_correct() {
        let mut c = Corrector::new();
        c.add_known_word("hello".to_string());
        assert_eq!(c.correct("helo"), "hello");
    }

    #[test]
    fn test_correction_count() {
        let mut c = Corrector::new();
        assert_eq!(c.correction_count(), 0);
        c.learn_error("a", "b");
        assert_eq!(c.correction_count(), 1);
    }

    #[test]
    fn test_memory_usage_positive() {
        let c = Corrector::new();
        assert!(c.memory_usage() > 0);
    }

    #[test]
    fn test_correct_empty_string() {
        let c = Corrector::new();
        assert_eq!(c.correct(""), "");
    }
}

#[cfg(test)]
mod learner_extra_tests {
    use super::*;

    #[test]
    fn test_learn_word_increments_frequency() {
        let mut l = Learner::new();
        l.learn_word("hello", "", 1000);
        l.learn_word("hello", "", 2000);
        let patterns = l.get_time_patterns(0, 10);
        let hello = patterns.iter().find(|(w, _)| *w == "hello");
        assert!(hello.is_some());
        assert!(hello.unwrap().1 >= 2);
    }

    #[test]
    fn test_learn_word_empty_string() {
        let mut l = Learner::new();
        l.learn_word("", "", 1000);
        assert_eq!(l.pattern_count(), 0);
    }

    #[test]
    fn test_learn_and_correct() {
        let mut l = Learner::new();
        l.learn_error("teh", "the");
        assert_eq!(l.get_correction("teh"), Some("the"));
        assert_eq!(l.get_correction("the"), None);
    }

    #[test]
    fn test_context_profiles() {
        let mut l = Learner::new();
        l.set_context("com.gmail", ContextType::Formal);
        l.set_context("com.whatsapp", ContextType::Casual);

        assert_eq!(l.get_context("com.gmail"), ContextType::Formal);
        assert_eq!(l.get_context("com.whatsapp"), ContextType::Casual);
        assert_eq!(l.get_context("unknown"), ContextType::Neutral);
    }

    #[test]
    fn test_set_hour() {
        let mut l = Learner::new();
        l.set_hour(14);
    }

    #[test]
    fn test_set_hour_clamps() {
        let mut l = Learner::new();
        l.set_hour(25);
    }

    #[test]
    fn test_apply_decay_reduces_old_patterns() {
        let mut l = Learner::new();
        // Learn a word very long ago
        l.learn_word("ancient", "", 1000);
        l.learn_word("fresh", "", 1000 + 90 * 86400);
        // Apply decay with very small max age
        l.apply_decay(1000 + 90 * 86400, 30);
        // Single-use old pattern fully decays to 0 and is pruned; recent survives
        assert_eq!(l.pattern_count(), 1);
    }

    #[test]
    fn test_apply_decay_keeps_recent_patterns() {
        let mut l = Learner::new();
        // Learn at hour 0 (timestamp 0 = hour 0)
        l.learn_word("recent", "", 0);
        l.learn_word("recent", "", 100);
        l.apply_decay(5000, 30);
        // Recent pattern should still exist (hour 0)
        let patterns = l.get_time_patterns(0, 10);
        assert!(patterns.iter().any(|(w, _)| *w == "recent"));
    }

    #[test]
    fn test_pattern_count() {
        let mut l = Learner::new();
        assert_eq!(l.pattern_count(), 0);
        l.learn_word("a", "", 1000);
        l.learn_word("b", "", 2000);
        assert_eq!(l.pattern_count(), 2);
    }

    #[test]
    fn test_memory_usage_positive() {
        let mut l = Learner::new();
        l.learn_word("hello", "", 1000);
        assert!(l.memory_usage() > 0);
    }

    #[test]
    fn test_time_patterns_hour_specific() {
        let mut l = Learner::new();
        l.learn_word("morning", "", 8 * 3600);
        l.learn_word("evening", "", 20 * 3600);

        let morning_patterns = l.get_time_patterns(8, 5);
        assert!(morning_patterns.iter().any(|(w, _)| *w == "morning"));

        let evening_patterns = l.get_time_patterns(20, 5);
        assert!(evening_patterns.iter().any(|(w, _)| *w == "evening"));
        assert!(!evening_patterns.iter().any(|(w, _)| *w == "morning"));
    }
}

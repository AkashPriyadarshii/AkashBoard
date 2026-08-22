/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * corrector.rs — Error correction engine for AkashBoard.
 *
 * Corrects misspelled words using:
 *   1. User's personal error patterns (learned from corrections)
 *   2. Edit distance (Levenshtein) against known words
 *   3. Common typo patterns (e.g., "teh" → "the")
 *
 * Performance targets:
 *   - correct(): <0.5ms
 *   - learn_error(): <0.1ms
 *
 * Optimizations:
 *   - Length pre-filter: skip words differing by >max_distance chars
 *   - Early-exit edit_distance: abort row when distance exceeds best
 *   - Known words sorted by length for binary search on length range
 */

use std::collections::HashMap;

/// Error correction engine.
pub struct Corrector {
    /// Personal error patterns: wrong → correct
    personal_corrections: HashMap<String, String>,

    /// Common typos (built-in)
    common_typos: HashMap<String, String>,

    /// Known words for edit distance matching, sorted by length
    known_words: Vec<String>,

    /// Known words sorted by length for fast length-range queries
    known_by_length: Vec<(usize, usize)>, // (length, index into known_words)
}

impl Corrector {
    /// Create a new corrector with built-in common typos.
    pub fn new() -> Self {
        let mut common_typos = HashMap::new();

        // Common English typos
        common_typos.insert("teh".to_string(), "the".to_string());
        common_typos.insert("adn".to_string(), "and".to_string());
        common_typos.insert("taht".to_string(), "that".to_string());
        common_typos.insert("hte".to_string(), "the".to_string());
        common_typos.insert("fo".to_string(), "of".to_string());
        common_typos.insert("thn".to_string(), "than".to_string());
        common_typos.insert("jsut".to_string(), "just".to_string());
        common_typos.insert("tihng".to_string(), "thing".to_string());
        common_typos.insert("nto".to_string(), "not".to_string());
        common_typos.insert("wiht".to_string(), "with".to_string());
        common_typos.insert("nt".to_string(), "not".to_string());
        common_typos.insert("bc".to_string(), "because".to_string());
        common_typos.insert("wut".to_string(), "what".to_string());
        common_typos.insert("u".to_string(), "you".to_string());
        common_typos.insert("r".to_string(), "are".to_string());
        common_typos.insert("ur".to_string(), "your".to_string());
        common_typos.insert("nah".to_string(), "no".to_string());
        common_typos.insert("idk".to_string(), "I don't know".to_string());
        common_typos.insert("imo".to_string(), "in my opinion".to_string());
        common_typos.insert("tbh".to_string(), "to be honest".to_string());
        common_typos.insert("omw".to_string(), "on my way".to_string());
        common_typos.insert("brb".to_string(), "be right back".to_string());

        Self {
            personal_corrections: HashMap::new(),
            common_typos,
            known_words: Vec::new(),
            known_by_length: Vec::new(),
        }
    }

    /// Correct a potentially misspelled word.
    ///
    /// Priority:
    ///   1. Personal corrections (user-specific)
    ///   2. Common typos (built-in)
    ///   3. Edit distance against known words
    ///
    /// # Performance
    /// Target: <0.5ms
    pub fn correct(&self, word: &str) -> String {
        let lower = word.to_lowercase();

        // 1. Check personal corrections — O(1) HashMap lookup
        if let Some(correction) = self.personal_corrections.get(&lower) {
            return correction.clone();
        }

        // 2. Check common typos — O(1) HashMap lookup
        if let Some(correction) = self.common_typos.get(&lower) {
            return correction.clone();
        }

        // 3. Edit distance against known words with optimizations
        if !self.known_words.is_empty() {
            let word_len = lower.len();
            let max_distance = if word_len <= 4 { 1 } else { 2 };

            // Length pre-filter: skip words that differ by more than max_distance
            // Binary search for the range of valid lengths
            let min_len = if word_len > max_distance { word_len - max_distance } else { 0 };
            let max_len = word_len + max_distance;

            let mut best_distance = usize::MAX;
            let mut best_match: Option<&str> = None;

            for known in &self.known_words {
                let known_len = known.len();

                // Skip words outside length range — O(1) per word
                if known_len < min_len || known_len > max_len {
                    continue;
                }

                // Use early-exit edit distance
                let distance = edit_distance_early_exit(&lower, known, best_distance);
                if distance < best_distance {
                    best_distance = distance;
                    best_match = Some(known.as_str());
                }
            }

            if let Some(closest) = best_match {
                if best_distance <= max_distance {
                    return closest.to_string();
                }
            }
        }

        // No correction found — return original
        word.to_string()
    }

    /// Learn a new error pattern.
    ///
    /// Called when user manually corrects an autocorrect mistake.
    ///
    /// # Performance
    /// Target: <0.1ms
    pub fn learn_error(&mut self, wrong: &str, correct: &str) {
        let wrong_lower = wrong.to_lowercase();
        let correct_lower = correct.to_lowercase();

        if !wrong_lower.is_empty() && !correct_lower.is_empty() && wrong_lower != correct_lower {
            self.personal_corrections.insert(wrong_lower, correct_lower);
        }
    }

    /// Add a word to the known words list.
    pub fn add_known_word(&mut self, word: String) {
        let lower = word.to_lowercase();
        if !self.known_words.contains(&lower) {
            self.known_words.push(lower);
            self.rebuild_length_index();
        }
    }

    /// Set the known words list (from dictionary).
    pub fn set_known_words(&mut self, words: Vec<String>) {
        self.known_words = words.into_iter().map(|w| w.to_lowercase()).collect();
        self.rebuild_length_index();
    }

    /// Rebuild the length-sorted index for fast filtering.
    fn rebuild_length_index(&mut self) {
        self.known_by_length.clear();
        for (i, word) in self.known_words.iter().enumerate() {
            self.known_by_length.push((word.len(), i));
        }
        self.known_by_length.sort_by_key(|&(len, _)| len);
    }

    /// Find the closest known word by edit distance.
    fn find_closest_word(&self, word: &str) -> Option<String> {
        let mut best_match = None;
        let mut best_distance = usize::MAX;

        for known in &self.known_words {
            let distance = edit_distance(word, known);
            if distance < best_distance {
                best_distance = distance;
                best_match = Some(known.clone());
            }
        }

        best_match
    }

    /// Get the number of personal corrections.
    pub fn correction_count(&self) -> usize {
        self.personal_corrections.len()
    }

    /// Get memory usage estimate in bytes.
    pub fn memory_usage(&self) -> usize {
        let personal: usize = self
            .personal_corrections
            .iter()
            .map(|(k, v)| k.len() + v.len() + 32)
            .sum();
        let common: usize = self
            .common_typos
            .iter()
            .map(|(k, v)| k.len() + v.len() + 32)
            .sum();
        let words: usize = self.known_words.iter().map(|w| w.len() + 16).sum();
        personal + common + words
    }
}

/// Levenshtein edit distance.
pub fn edit_distance(a: &str, b: &str) -> usize {
    let a_chars: Vec<char> = a.chars().collect();
    let b_chars: Vec<char> = b.chars().collect();
    let a_len = a_chars.len();
    let b_len = b_chars.len();

    if a_len == 0 { return b_len; }
    if b_len == 0 { return a_len; }

    let mut prev = vec![0usize; b_len + 1];
    let mut curr = vec![0usize; b_len + 1];

    for j in 0..=b_len { prev[j] = j; }

    for i in 1..=a_len {
        curr[0] = i;
        for j in 1..=b_len {
            let cost = if a_chars[i - 1] == b_chars[j - 1] { 0 } else { 1 };
            curr[j] = (prev[j] + 1).min(curr[j - 1] + 1).min(prev[j - 1] + cost);
        }
        std::mem::swap(&mut prev, &mut curr);
    }

    prev[b_len]
}

/// Edit distance with early exit — aborts if distance exceeds `max_dist`.
///
/// This is critical for correct() performance: instead of computing the
/// full matrix, we stop as soon as we know this word can't be the best match.
fn edit_distance_early_exit(a: &str, b: &str, max_dist: usize) -> usize {
    let a_chars: Vec<char> = a.chars().collect();
    let b_chars: Vec<char> = b.chars().collect();
    let a_len = a_chars.len();
    let b_len = b_chars.len();

    if a_len == 0 { return b_len; }
    if b_len == 0 { return a_len; }

    // Quick length check
    if (a_len as isize - b_len as isize).unsigned_abs() > max_dist {
        return max_dist + 1;
    }

    let mut prev = vec![0usize; b_len + 1];
    let mut curr = vec![0usize; b_len + 1];

    for j in 0..=b_len { prev[j] = j; }

    for i in 1..=a_len {
        curr[0] = i;
        let mut row_min = curr[0];

        for j in 1..=b_len {
            let cost = if a_chars[i - 1] == b_chars[j - 1] { 0 } else { 1 };
            curr[j] = (prev[j] + 1).min(curr[j - 1] + 1).min(prev[j - 1] + cost);
            if curr[j] < row_min {
                row_min = curr[j];
            }
        }

        // Early exit: if the minimum value in this row exceeds max_dist,
        // the final distance will definitely exceed it
        if row_min > max_dist {
            return max_dist + 1;
        }

        std::mem::swap(&mut prev, &mut curr);
    }

    prev[b_len]
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_common_typo_correction() {
        let c = Corrector::new();
        assert_eq!(c.correct("teh"), "the");
        assert_eq!(c.correct("adn"), "and");
        assert_eq!(c.correct("jsut"), "just");
    }

    #[test]
    fn test_personal_correction() {
        let mut c = Corrector::new();
        c.learn_error("palce", "place");
        assert_eq!(c.correct("palce"), "place");
    }

    #[test]
    fn test_edit_distance_correction() {
        let mut c = Corrector::new();
        c.set_known_words(vec!["hello".to_string(), "world".to_string()]);
        // "helo" is 1 edit away from "hello"
        assert_eq!(c.correct("helo"), "hello");
    }

    #[test]
    fn test_no_correction_needed() {
        let c = Corrector::new();
        assert_eq!(c.correct("hello"), "hello");
    }

    #[test]
    fn test_edit_distance() {
        assert_eq!(edit_distance("", ""), 0);
        assert_eq!(edit_distance("abc", "abc"), 0);
        assert_eq!(edit_distance("abc", "ab"), 1);
        assert_eq!(edit_distance("abc", "def"), 3);
    }

    #[test]
    fn test_early_exit_same_as_full() {
        // Verify early-exit produces same results as full edit_distance
        let pairs = [
            ("hello", "helo"),
            ("keyboard", "keybaord"),
            ("short", "hort"),
            ("abc", "def"),
            ("a", "b"),
            ("", "test"),
            ("test", ""),
        ];
        for (a, b) in &pairs {
            let full = edit_distance(a, b);
            let early = edit_distance_early_exit(a, b, 10);
            assert_eq!(full, early, "Mismatch for ({}, {})", a, b);
        }
    }
}

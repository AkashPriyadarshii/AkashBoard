/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * predictor.rs — N-gram prediction engine for AkashBoard.
 *
 * Uses unigram, bigram, and trigram models for next-word prediction.
 * All data is stored in-memory for <1ms prediction times.
 *
 * Performance targets:
 *   - predict(): <1ms for top-5
 *   - correct(): <0.5ms
 *   - learn(): <0.1ms
 *
 * Optimizations:
 *   - O(1) bigram lookup via HashMap<String, Vec<(String, u32)>>
 *   - O(1) trigram lookup via HashMap<(String, String), Vec<(String, u32)>>
 *   - Partial sort for candidate selection (O(n log k) instead of O(n log n))
 *   - Length pre-filter + early-exit edit_distance for correct()
 */

use std::collections::HashMap;

#[derive(Default)]
pub struct TrieNode {
    pub is_word: bool,
    pub children: HashMap<char, Box<TrieNode>>,
}

impl TrieNode {
    pub fn insert(&mut self, word: &str) {
        let mut curr = self;
        for c in word.chars() {
            curr = curr.children.entry(c).or_insert_with(|| Box::new(TrieNode::default()));
        }
        curr.is_word = true;
    }
}

/// Owned persistence shape (deserialize target).
#[derive(serde::Serialize, serde::Deserialize)]
struct TrigramRow {
    p1: String,
    p2: String,
    w: String,
    c: u32,
}

/// JSON persistence shape. Trigram tuple keys are flattened to rows because
/// JSON map keys must be strings.
#[derive(serde::Serialize, serde::Deserialize)]
struct SerializedModel {
    unigrams: HashMap<String, u32>,
    bigram_index: HashMap<String, Vec<(String, u32)>>,
    trigrams: Vec<TrigramRow>,
    total_words: u64,
    /// Personal error patterns; default keeps old model.json files loadable.
    #[serde(default)]
    corrections: HashMap<String, String>,
}

/// N-gram prediction engine.
pub struct Predictor {
    /// Unigram frequencies: word → count
    unigrams: HashMap<String, u32>,

    /// Bigram index: prev_word → [(next_word, count), ...]
    /// This enables O(1) lookup instead of O(n) iteration
    bigram_index: HashMap<String, Vec<(String, u32)>>,

    /// Trigram index: (prev_prev, prev) → [(next_word, count), ...]
    /// This enables O(1) lookup instead of O(n) iteration
    trigram_index: HashMap<(String, String), Vec<(String, u32)>>,

    /// Total word count for frequency normalization
    total_words: u64,

    /// Personal error patterns learned from user corrections: wrong → right
    corrections: HashMap<String, String>,

}

impl Predictor {
    /// Create a new empty predictor.
    pub fn new() -> Self {
        Self {
            unigrams: HashMap::new(),
            bigram_index: HashMap::new(),
            trigram_index: HashMap::new(),
            total_words: 0,
            corrections: HashMap::new(),

        }
    }

    /// Predict the next words given a context string.
    ///
    /// # Performance
    /// Target: <1ms for top-5 on a typical vocabulary
    pub fn predict(&self, context: &str, top_k: usize) -> Vec<String> {
        let words: Vec<&str> = context.split_whitespace().collect();

        if words.is_empty() {
            return self.top_unigrams(top_k);
        }

        let mut candidates: Vec<(String, f64)> = Vec::with_capacity(top_k * 3);

        // Trigram matching — O(1) lookup
        if words.len() >= 2 {
            let prev_prev = words[words.len() - 2].to_lowercase();
            let prev = words[words.len() - 1].to_lowercase();

            if let Some(entries) = self.trigram_index.get(&(prev_prev, prev)) {
                for (curr, freq) in entries {
                    let score = *freq as f64 * 1.5;
                    candidates.push((curr.clone(), score));
                }
            }
        }

        // Bigram matching — O(1) lookup
        let last_word = words.last().unwrap().to_lowercase();
        if let Some(entries) = self.bigram_index.get(&last_word) {
            for (curr, freq) in entries {
                let score = *freq as f64;
                if !candidates.iter().any(|(w, _)| w == curr) {
                    candidates.push((curr.clone(), score));
                }
            }
        }

        // If no bigram/trigram matches, fall back to unigrams
        if candidates.is_empty() {
            return self.top_unigrams(top_k);
        }

        if top_k == 0 {
            return Vec::new();
        }

        // Partial sort — O(n log k) instead of O(n log n)
        if candidates.len() > top_k {
            candidates.select_nth_unstable_by(top_k - 1, |a, b| {
                b.1.partial_cmp(&a.1).unwrap_or(std::cmp::Ordering::Equal)
            });
            candidates.truncate(top_k);
        } else {
            candidates.sort_by(|a, b| b.1.partial_cmp(&a.1).unwrap_or(std::cmp::Ordering::Equal));
        }

        candidates.into_iter().map(|(word, _)| word).collect()
    }

    /// Auto-correct a potentially misspelled word.
    ///
    /// # Performance
    /// Target: <0.5ms
    pub fn correct(&self, word: &str, _context: &str) -> String {
        let lower = word.to_lowercase();

        // O(1) check if already known
        if self.unigrams.contains_key(&lower) {
            return word.to_string();
        }

        let word_len = lower.len();
        let max_distance = if word_len <= 4 { 1 } else { 2 };

        let mut best_match = None;
        let mut best_distance = usize::MAX;

        for known_word in self.unigrams.keys() {
            // Length pre-filter — O(1) skip
            let known_len = known_word.len();
            if (word_len as isize - known_len as isize).unsigned_abs() > max_distance {
                continue;
            }

            let distance = edit_distance_early_exit(&lower, known_word, best_distance);
            if distance < best_distance && distance <= max_distance {
                best_distance = distance;
                best_match = Some(known_word.clone());
            }
        }

        best_match.unwrap_or_else(|| word.to_string())
    }

    /// Learn a new word/pattern from user typing.
    ///
    /// # Performance
    /// Target: <0.1ms
    pub fn learn(&mut self, word: &str, context: &str) -> bool {
        let lower_word = word.to_lowercase();
        if lower_word.is_empty() {
            return false;
        }

        // Update unigram
        *self.unigrams.entry(lower_word.clone()).or_insert(0) += 1;
        self.total_words += 1;

        // Update bigram index
        let context_words: Vec<&str> = context.split_whitespace().collect();
        if let Some(last) = context_words.last() {
            let prev = last.to_lowercase();
            let entries = self.bigram_index.entry(prev).or_insert_with(Vec::new);
            // Check if this (prev, curr) pair already exists
            if let Some(entry) = entries.iter_mut().find(|(w, _)| *w == lower_word) {
                entry.1 += 1;
            } else {
                entries.push((lower_word.clone(), 1));
            }
        }

        // Update trigram index
        if context_words.len() >= 2 {
            let prev_prev = context_words[context_words.len() - 2].to_lowercase();
            let prev = context_words[context_words.len() - 1].to_lowercase();
            let key = (prev_prev, prev);
            let entries = self.trigram_index.entry(key).or_insert_with(Vec::new);
            if let Some(entry) = entries.iter_mut().find(|(w, _)| *w == lower_word) {
                entry.1 += 1;
            } else {
                entries.push((lower_word.clone(), 1));
            }
        }

        true
    }

    /// Get the top N most frequent unigrams.
    fn top_unigrams(&self, k: usize) -> Vec<String> {
        if k == 0 {
            return Vec::new();
        }
        let mut words: Vec<_> = self.unigrams.iter().collect();
        if words.len() > k * 10 {
            words.select_nth_unstable_by(k - 1, |a, b| b.1.cmp(a.1));
            words.truncate(k);
        } else {
            words.sort_by(|a, b| b.1.cmp(a.1));
            words.truncate(k);
        }
        words.into_iter().map(|(word, _)| word.clone()).collect()
    }

    /// Get the total number of learned words.
    pub fn word_count(&self) -> usize {
        self.unigrams.len()
    }

    /// Get the total number of bigrams.
    pub fn bigram_count(&self) -> usize {
        self.bigram_index.values().map(|v| v.len()).sum()
    }

    /// Learn a personal error pattern (user typed X, meant Y).
    pub fn learn_error(&mut self, wrong: &str, correct: &str) {
        let (wrong, correct) = (wrong.to_lowercase(), correct.to_lowercase());
        if !wrong.is_empty() && !correct.is_empty() {
            self.corrections.insert(wrong, correct);
        }
    }

    /// Unlearn a personal error pattern.
    pub fn unlearn_error(&mut self, wrong: &str) {
        self.corrections.remove(&wrong.to_lowercase());
    }

    /// Get the learned correction for a word.
    pub fn get_correction(&self, word: &str) -> Option<&str> {
        self.corrections.get(&word.to_lowercase()).map(|s| s.as_str())
    }

    /// Serialize engine state to JSON bytes (for persistence).
    /// ponytail: trigram tuple-keys can't be JSON map keys, so they're
    /// flattened to [prev_prev, prev, next, count] arrays. Upgrade path:
    /// bincode/flatbuffers if JSON size becomes a problem.
    pub fn to_json(&self) -> Vec<u8> {
        let trigrams: Vec<TrigramRow> = self
            .trigram_index
            .iter()
            .flat_map(|((p1, p2), entries)| {
                entries.iter().map(move |(w, c)| TrigramRow {
                    p1: p1.clone(),
                    p2: p2.clone(),
                    w: w.clone(),
                    c: *c,
                })
            })
            .collect();
        let payload = SerializedModel {
            unigrams: self.unigrams.clone(),
            bigram_index: self.bigram_index.clone(),
            trigrams,
            total_words: self.total_words,
            corrections: self.corrections.clone(),
        };
        serde_json::to_vec(&payload).unwrap_or_default()
    }

    /// Restore engine state from JSON bytes.
    /// Returns false on corrupt data (engine left unchanged).
    pub fn from_json(&mut self, data: &[u8]) -> bool {
        match serde_json::from_slice::<SerializedModel>(data) {
            Ok(m) => {
                let mut trigram_index = HashMap::new();
                for row in m.trigrams {
                    trigram_index
                        .entry((row.p1, row.p2))
                        .or_insert_with(Vec::new)
                        .push((row.w, row.c));
                }
                self.unigrams = m.unigrams;
                self.bigram_index = m.bigram_index;
                self.trigram_index = trigram_index;
                self.total_words = m.total_words;
                self.corrections = m.corrections;
                true
            }
            Err(_) => false,
        }
    }

    /// Approximate serialized size in bytes.
    pub fn serialized_size(&self) -> usize {
        self.to_json().len()
    }

    /// Get memory usage estimate in bytes.
    pub fn memory_usage(&self) -> usize {
        let unigram_size: usize = self
            .unigrams
            .iter()
            .map(|(k, _v)| k.len() + std::mem::size_of::<u32>() + 32)
            .sum();
        let bigram_size: usize = self
            .bigram_index
            .iter()
            .map(|(k, v)| k.len() + v.iter().map(|(w, _)| w.len() + 8).sum::<usize>() + 48)
            .sum();
        let trigram_size: usize = self
            .trigram_index
            .iter()
            .map(|((k1, k2), v)| k1.len() + k2.len() + v.iter().map(|(w, _)| w.len() + 8).sum::<usize>() + 64)
            .sum();
        unigram_size + bigram_size + trigram_size
    }
}

/// Calculate the Levenshtein edit distance between two strings.
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
fn edit_distance_early_exit(a: &str, b: &str, max_dist: usize) -> usize {
    let a_chars: Vec<char> = a.chars().collect();
    let b_chars: Vec<char> = b.chars().collect();
    let a_len = a_chars.len();
    let b_len = b_chars.len();

    if a_len == 0 { return b_len; }
    if b_len == 0 { return a_len; }

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

        if row_min > max_dist {
            return max_dist + 1;
        }

        std::mem::swap(&mut prev, &mut curr);
    }

    prev[b_len]
}

// ── Tests ─────────────────────────────────────────────────────────────────

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_empty_predictor() {
        let p = Predictor::new();
        assert!(p.predict("hello", 3).is_empty());
        assert_eq!(p.word_count(), 0);
    }

    #[test]
    fn test_unigram_prediction() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        p.learn("world", "");
        p.learn("hello", "");

        let results = p.predict("", 3);
        assert!(results.contains(&"hello".to_string()));
    }

    #[test]
    fn test_bigram_prediction() {
        let mut p = Predictor::new();
        p.learn("going", "I am");
        p.learn("to", "I am going");
        p.learn("the", "I am going to");
        p.learn("store", "I am going to the");

        let results = p.predict("I am going to the", 3);
        assert!(results.contains(&"store".to_string()));
    }

    #[test]
    fn test_trigram_prediction() {
        let mut p = Predictor::new();
        p.learn("the", "I am going to");
        p.learn("store", "I am going to the");
        p.learn("store", "I am going to the");

        let results = p.predict("I am going to the", 3);
        // Trigram should boost "store"
        assert!(results.contains(&"store".to_string()));
    }

    #[test]
    fn test_correct_known_word() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        assert_eq!(p.correct("hello", ""), "hello");
    }

    #[test]
    fn test_correct_misspelling() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        p.learn("world", "");
        assert_eq!(p.correct("helo", ""), "hello");
    }

    #[test]
    fn test_correct_unknown_word() {
        let p = Predictor::new();
        assert_eq!(p.correct("xyzabc", ""), "xyzabc");
    }

    #[test]
    fn test_edit_distance() {
        assert_eq!(edit_distance("", ""), 0);
        assert_eq!(edit_distance("abc", "abc"), 0);
        assert_eq!(edit_distance("abc", "ab"), 1);
        assert_eq!(edit_distance("abc", "abcd"), 1);
        assert_eq!(edit_distance("abc", "def"), 3);
    }

    #[test]
    fn test_memory_usage() {
        let mut p = Predictor::new();
        p.learn("hello", "");
        p.learn("world", "");
        let usage = p.memory_usage();
        assert!(usage > 0);
    }

    #[test]
    fn test_early_exit_consistency() {
        let pairs = [
            ("hello", "helo"),
            ("keyboard", "keybaord"),
            ("abc", "def"),
        ];
        for (a, b) in &pairs {
            let full = edit_distance(a, b);
            let early = edit_distance_early_exit(a, b, 10);
            assert_eq!(full, early, "Mismatch for ({}, {})", a, b);
        }
    }

    #[test]
    fn test_bigram_count() {
        let mut p = Predictor::new();
        p.learn("hello", "hi");
        p.learn("world", "hello");
        assert!(p.bigram_count() >= 2);
    }
}




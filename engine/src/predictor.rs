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
 */

use std::collections::HashMap;

/// N-gram prediction engine.
///
/// Stores word frequencies and bigram/trigram patterns
/// for fast next-word prediction.
pub struct Predictor {
    /// Unigram frequencies: word → count
    unigrams: HashMap<String, u32>,

    /// Bigram frequencies: (prev_word, curr_word) → count
    bigrams: HashMap<(String, String), u32>,

    /// Trigram frequencies: (prev_prev, prev, curr) → count
    trigrams: HashMap<(String, String, String), u32>,

    /// Total word count for frequency normalization
    total_words: u64,
}

impl Predictor {
    /// Create a new empty predictor.
    pub fn new() -> Self {
        Self {
            unigrams: HashMap::new(),
            bigrams: HashMap::new(),
            trigrams: HashMap::new(),
            total_words: 0,
        }
    }

    /// Predict the next words given a context string.
    ///
    /// # Arguments
    /// * `context` - The current text (e.g., "I am going to the")
    /// * `top_k` - Maximum number of suggestions
    ///
    /// # Returns
    /// Vector of predicted words, ordered by probability (highest first)
    ///
    /// # Performance
    /// Target: <1ms for top-5 on a typical vocabulary
    pub fn predict(&self, context: &str, top_k: usize) -> Vec<String> {
        let words: Vec<&str> = context.split_whitespace().collect();

        if words.is_empty() {
            return self.top_unigrams(top_k);
        }

        // Try trigrams first (most specific), then bigrams, then unigrams
        let mut candidates: Vec<(String, f64)> = Vec::new();

        if words.len() >= 2 {
            let prev_prev = words[words.len() - 2].to_lowercase();
            let prev = words[words.len() - 1].to_lowercase();

            // Trigram matching
            for ((pp, p, curr), freq) in &self.trigrams {
                if pp == &prev_prev && p == &prev {
                    let score = *freq as f64 * 1.5; // Trigrams get 1.5x boost
                    candidates.push((curr.clone(), score));
                }
            }
        }

        // Bigram matching
        let last_word = words.last().unwrap().to_lowercase();
        for ((prev, curr), freq) in &self.bigrams {
            if prev == &last_word {
                let score = *freq as f64;
                // Only add if not already present from trigrams
                if !candidates.iter().any(|(w, _)| w == curr) {
                    candidates.push((curr.clone(), score));
                }
            }
        }

        // If no bigram/trigram matches, fall back to unigrams
        if candidates.is_empty() {
            return self.top_unigrams(top_k);
        }

        // Sort by score (highest first) and take top_k
        candidates.sort_by(|a, b| b.1.partial_cmp(&a.1).unwrap_or(std::cmp::Ordering::Equal));
        candidates.truncate(top_k);

        candidates.into_iter().map(|(word, _)| word).collect()
    }

    /// Auto-correct a potentially misspelled word.
    ///
    /// Uses edit distance to find the closest known word.
    ///
    /// # Performance
    /// Target: <0.5ms
    pub fn correct(&self, word: &str, _context: &str) -> String {
        let lower = word.to_lowercase();

        // If the word is already known, return it as-is
        if self.unigrams.contains_key(&lower) {
            return word.to_string();
        }

        // Find the closest word by edit distance
        let mut best_match = None;
        let mut best_distance = usize::MAX;

        for known_word in self.unigrams.keys() {
            let distance = edit_distance(&lower, known_word);
            if distance < best_distance && distance <= 2 {
                // Only correct if edit distance is reasonable
                best_distance = distance;
                best_match = Some(known_word.clone());
            }
        }

        best_match.unwrap_or_else(|| word.to_string())
    }

    /// Learn a new word/pattern from user typing.
    ///
    /// # Arguments
    /// * `word` - The word to learn
    /// * `context` - The surrounding context
    ///
    /// # Returns
    /// true if the word was successfully learned
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

        // Update bigram
        let context_words: Vec<&str> = context.split_whitespace().collect();
        if let Some(last) = context_words.last() {
            let prev = last.to_lowercase();
            *self.bigrams.entry((prev, lower_word.clone())).or_insert(0) += 1;
        }

        // Update trigram
        if context_words.len() >= 2 {
            let prev_prev = context_words[context_words.len() - 2].to_lowercase();
            let prev = context_words[context_words.len() - 1].to_lowercase();
            *self.trigrams
                .entry((prev_prev, prev, lower_word))
                .or_insert(0) += 1;
        }

        true
    }

    /// Get the top N most frequent unigrams.
    fn top_unigrams(&self, k: usize) -> Vec<String> {
        let mut words: Vec<_> = self.unigrams.iter().collect();
        words.sort_by(|a, b| b.1.cmp(a.1));
        words
            .into_iter()
            .take(k)
            .map(|(word, _)| word.clone())
            .collect()
    }

    /// Get the total number of learned words.
    pub fn word_count(&self) -> usize {
        self.unigrams.len()
    }

    /// Get the total number of bigrams.
    pub fn bigram_count(&self) -> usize {
        self.bigrams.len()
    }

    /// Get memory usage estimate in bytes.
    pub fn memory_usage(&self) -> usize {
        let unigram_size: usize = self
            .unigrams
            .iter()
            .map(|(k, _v)| k.len() + std::mem::size_of::<u32>() + 32) // HashMap overhead
            .sum();
        let bigram_size: usize = self
            .bigrams
            .iter()
            .map(|((k1, k2), _v)| k1.len() + k2.len() + std::mem::size_of::<u32>() + 48)
            .sum();
        let trigram_size: usize = self
            .trigrams
            .iter()
            .map(|((k1, k2, k3), _v)| k1.len() + k2.len() + k3.len() + std::mem::size_of::<u32>() + 64)
            .sum();
        unigram_size + bigram_size + trigram_size
    }
}

/// Calculate the Levenshtein edit distance between two strings.
///
/// Used for auto-correction to find the closest known word.
pub fn edit_distance(a: &str, b: &str) -> usize {
    let a_chars: Vec<char> = a.chars().collect();
    let b_chars: Vec<char> = b.chars().collect();
    let a_len = a_chars.len();
    let b_len = b_chars.len();

    if a_len == 0 {
        return b_len;
    }
    if b_len == 0 {
        return a_len;
    }

    // Use a flat array for better cache performance
    let mut prev = vec![0usize; b_len + 1];
    let mut curr = vec![0usize; b_len + 1];

    for j in 0..=b_len {
        prev[j] = j;
    }

    for i in 1..=a_len {
        curr[0] = i;
        for j in 1..=b_len {
            let cost = if a_chars[i - 1] == b_chars[j - 1] {
                0
            } else {
                1
            };
            curr[j] = (prev[j] + 1)
                .min(curr[j - 1] + 1)
                .min(prev[j - 1] + cost);
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
        // "helo" should correct to "hello" (edit distance 1)
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
        assert!(usage < 1024); // Should be less than 1KB for 2 words
    }
}

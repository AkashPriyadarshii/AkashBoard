/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * learner.rs — Personal pattern learning engine for AkashBoard.
 *
 * Tracks user-specific typing patterns:
 *   - Time-of-day patterns (what you type in morning vs night)
 *   - Context profiles (formal vs casual per app)
 *   - Error patterns (your common typos)
 *   - Decay system (old patterns lose weight)
 *
 * All learning happens locally. Zero cloud dependency.
 */

use std::collections::HashMap;

/// Context profile types.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
pub enum ContextType {
    /// Neutral/unknown context
    Neutral,
    /// Formal context (email, documents)
    Formal,
    /// Casual context (chat, social)
    Casual,
    /// Technical context (code, technical writing)
    Technical,
}

/// A personal pattern entry with decay tracking.
#[derive(Debug, Clone)]
pub struct PatternEntry {
    /// Usage count
    pub frequency: u32,
    /// Last used timestamp (Unix seconds)
    pub last_used: u64,
    /// Time-of-day usage (hour 0-23 → count)
    pub time_pattern: [u32; 24],
    /// Context type
    pub context: ContextType,
}

impl PatternEntry {
    pub fn new(timestamp: u64) -> Self {
        Self {
            frequency: 1,
            last_used: timestamp,
            time_pattern: [0; 24],
            context: ContextType::Neutral,
        }
    }
}

/// Personal pattern learning engine.
///
/// Learns user-specific patterns without cloud dependency.
pub struct Learner {
    /// Word patterns with decay tracking
    patterns: HashMap<String, PatternEntry>,

    /// Error correction patterns: wrong → correct
    corrections: HashMap<String, String>,

    /// Context profiles per app/package name
    context_profiles: HashMap<String, ContextType>,

    /// Current hour (0-23) for time-based learning
    current_hour: u8,
}

impl Learner {
    /// Create a new learner.
    pub fn new() -> Self {
        Self {
            patterns: HashMap::new(),
            corrections: HashMap::new(),
            context_profiles: HashMap::new(),
            current_hour: 0,
        }
    }

    /// Learn a new word pattern.
    ///
    /// # Arguments
    /// * `word` - The word to learn
    /// * `context` - Surrounding text
    /// * `timestamp` - Unix timestamp in seconds
    pub fn learn_word(&mut self, word: &str, _context: &str, timestamp: u64) {
        let lower = word.to_lowercase();
        if lower.is_empty() {
            return;
        }

        let hour = ((timestamp / 3600) % 24) as usize;

        let entry = self.patterns.entry(lower).or_insert_with(|| PatternEntry {
            frequency: 0,
            last_used: timestamp,
            time_pattern: [0; 24],
            context: ContextType::Neutral,
        });

        entry.frequency += 1;
        entry.last_used = timestamp;
        entry.time_pattern[hour] += 1;
    }

    /// Learn an error pattern (user typed X, meant Y).
    pub fn learn_error(&mut self, wrong: &str, correct: &str) {
        let wrong_lower = wrong.to_lowercase();
        let correct_lower = correct.to_lowercase();

        if !wrong_lower.is_empty() && !correct_lower.is_empty() {
            self.corrections.insert(wrong_lower, correct_lower);
        }
    }

    /// Get correction for a potentially misspelled word.
    pub fn get_correction(&self, word: &str) -> Option<&str> {
        self.corrections.get(&word.to_lowercase()).map(|s| s.as_str())
    }

    /// Set context profile for an app.
    pub fn set_context(&mut self, package_name: &str, context: ContextType) {
        self.context_profiles.insert(package_name.to_string(), context);
    }

    /// Get context profile for an app.
    pub fn get_context(&self, package_name: &str) -> ContextType {
        self.context_profiles
            .get(package_name)
            .copied()
            .unwrap_or(ContextType::Neutral)
    }

    /// Update the current hour (call when hour changes).
    pub fn set_hour(&mut self, hour: u8) {
        self.current_hour = hour.min(23);
    }

    /// Apply decay to all patterns.
    ///
    /// Patterns unused for `max_age_days` lose weight.
    /// Called periodically (e.g., once per day).
    pub fn apply_decay(&mut self, current_timestamp: u64, max_age_days: u32) {
        let max_age_seconds = max_age_days as u64 * 86400;
        let now = current_timestamp;

        for entry in self.patterns.values_mut() {
            let age = now.saturating_sub(entry.last_used);
            if age > max_age_seconds {
                // Reduce frequency by 10% per month unused
                let months_old = (age / (30 * 86400)).max(1);
                let decay_factor = 0.9f32.powi(months_old as i32);
                entry.frequency = (entry.frequency as f32 * decay_factor) as u32;
            }
        }

        // Remove entries with zero frequency
        self.patterns.retain(|_, entry| entry.frequency > 0);
    }

    /// Get top patterns for a specific hour.
    pub fn get_time_patterns(&self, hour: u8, top_k: usize) -> Vec<(&str, u32)> {
        let mut patterns: Vec<_> = self
            .patterns
            .iter()
            .filter(|(_, entry)| entry.time_pattern[hour as usize] > 0)
            .map(|(word, entry)| (word.as_str(), entry.time_pattern[hour as usize]))
            .collect();

        patterns.sort_by(|a, b| b.1.cmp(&a.1));
        patterns.truncate(top_k);
        patterns
    }

    /// Get total pattern count.
    pub fn pattern_count(&self) -> usize {
        self.patterns.len()
    }

    /// Get total correction count.
    pub fn correction_count(&self) -> usize {
        self.corrections.len()
    }

    /// Get memory usage estimate in bytes.
    pub fn memory_usage(&self) -> usize {
        let pattern_size: usize = self
            .patterns
            .iter()
            .map(|(k, _v)| k.len() + std::mem::size_of::<PatternEntry>() + 32)
            .sum();
        let correction_size: usize = self
            .corrections
            .iter()
            .map(|(k, v)| k.len() + v.len() + 32)
            .sum();
        pattern_size + correction_size
    }
}

// ── Tests ─────────────────────────────────────────────────────────────────

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_learn_word() {
        let mut l = Learner::new();
        l.learn_word("hello", "hi there", 1000000);
        assert_eq!(l.pattern_count(), 1);
    }

    #[test]
    fn test_learn_error() {
        let mut l = Learner::new();
        l.learn_error("teh", "the");
        assert_eq!(l.get_correction("teh"), Some("the"));
        assert_eq!(l.get_correction("the"), None);
    }

    #[test]
    fn test_time_patterns() {
        let mut l = Learner::new();
        // Learn "morning" at hour 8
        l.learn_word("morning", "", 8 * 3600);
        l.learn_word("morning", "", 8 * 3600 + 100);

        let patterns = l.get_time_patterns(8, 5);
        assert!(patterns.iter().any(|(w, _)| *w == "morning"));

        // Should not appear at hour 20
        let patterns_night = l.get_time_patterns(20, 5);
        assert!(!patterns_night.iter().any(|(w, _)| *w == "morning"));
    }

    #[test]
    fn test_context_profiles() {
        let mut l = Learner::new();
        l.set_context("com.google.android.gm", ContextType::Formal);
        l.set_context("com.whatsapp", ContextType::Casual);

        assert_eq!(l.get_context("com.google.android.gm"), ContextType::Formal);
        assert_eq!(l.get_context("com.whatsapp"), ContextType::Casual);
        assert_eq!(l.get_context("unknown"), ContextType::Neutral);
    }

    #[test]
    fn test_decay() {
        let mut l = Learner::new();
        l.learn_word("old_word", "", 1000000);
        l.learn_word("new_word", "", 1000000 + 90 * 86400); // 90 days later

        // Apply decay (max 30 days)
        l.apply_decay(1000000 + 90 * 86400, 30);

        // Single-use word fully decays to 0 and gets pruned
        assert!(!l.patterns.contains_key("old_word"));
    }
}

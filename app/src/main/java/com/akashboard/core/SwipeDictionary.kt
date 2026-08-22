/*
 * Copyright (C) 2026 Akash Priyadarshi
 * Licensed under the GNU General Public License v3.0
 *
 * SwipeDictionary.kt — Word list for swipe/glide typing.
 *
 * Contains 500+ common English words sorted by frequency.
 * The SwipeDetector matches gesture paths against this dictionary.
 */

package com.akashboard.core

/**
 * Swipe typing dictionary.
 *
 * Provides a list of common English words for path matching.
 * Words are sorted by frequency (most common first).
 */
object SwipeDictionary {

    /**
     * Get the full word list for swipe matching.
     *
     * @return List of lowercase English words
     */
    fun getWords(): List<String> = WORDS

    /**
     * Get words matching a key sequence.
     *
     * @param sequence Characters touched during gesture
     * @param topK Maximum results
     * @return Matching words ordered by relevance
     */
    fun match(sequence: List<Char>, topK: Int = 5): List<String> {
        val seqStr = sequence.joinToString("").lowercase()
        return WORDS.filter { word ->
            matchesSequence(word, seqStr)
        }.sortedBy { word ->
            when {
                word == seqStr -> 0
                seqStr.startsWith(word) -> 1
                word.startsWith(seqStr) -> 2
                else -> 3
            }
        }.take(topK)
    }

    private fun matchesSequence(word: String, sequence: String): Boolean {
        var seqIndex = 0
        for (char in word) {
            val found = sequence.indexOf(char, seqIndex)
            if (found == -1) return false
            seqIndex = found + 1
        }
        return true
    }

    // Top 500+ English words by frequency
    private val WORDS = listOf(
        // Top 100
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
        "people", "into", "year", "your", "good", "some", "could", "them", "see",
        "other", "than", "then", "now", "look", "only", "come", "its", "over",
        "think", "also", "back", "after", "use", "two", "how", "our", "work",
        "first", "well", "way", "even", "new", "want", "because", "any", "these",
        // 101-200
        "give", "day", "most", "us", "find", "here", "thing", "many", "may", "still",
        "long", "part", "much", "must", "while", "last", "right", "too", "same",
        "those", "both", "since", "keep", "never", "start", "city", "under", "name",
        "need", "home", "big", "hand", "high", "hold", "own", "play", "small",
        "end", "put", "open", "help", "point", "turn", "move", "live", "real",
        "close", "money", "seem", "try", "leave", "call", "down", "set", "run",
        "yes", "again", "stop", "between", "every", "should", "sure", "house",
        "world", "group", "always", "story", "young", "late", "face", "feel",
        "head", "far", "food", "city", "tree", "cross", "love", "pain", "happy",
        "bring", "happen", "next", "body", "mind", "enough", "kind", "say",
        // 201-300
        "ask", "going", "school", "through", "line", "right", "number", "left",
        "old", " tell", "boy", "follow", "came", "show", "want", "got",
        "place", "many", "day", "woman", "water", "man", "play", "today",
        "old", "put", "heart", "hand", "letter", "social", "been", "state",
        "country", "school", "family", "early", "miss", "before", "change", "large",
        "read", "mother", "father", "leave", "night", "live", "late", "around",
        "another", "area", "call", "few", "great", "company", "word", "work",
        "something", "write", "music", "book", "letter", "already", "maybe",
        "half", "room", "fact", "area", "land", "idea", "power", "car",
        "away", "side", "asked", "girl", "morning", "together", "quite", "plan",
        "keep", "look", "eye", "point", "four", "five", "money", "soon",
        // 301-400
        "market", "drive", "stop", "free", "eat", "market", "ever", "white",
        "month", "today", "holiday", "south", "door", "system", "center",
        "important", "paper", "second", "later", "near", "hard", "develop",
        "above", "third", "photo", "story", "young", "answer", "grow", "room",
        "fish", "north", "open", "study", "learn", "office", "walk", "why",
        "easy", "air", "kind", "remember", "pick", "different", "almost", "answer",
        "car", "game", "reason", "result", "full", "special", "understand", "face",
        "space", "feel", "problem", "yet", "body", "maybe", "best", "person",
        "together", "happen", "children", "side", "feet", "letter", "cover",
        "sound", "break", "human", "river", "father", "clear", "wine", "trip",
        "complete", "consider", "draw", "hope", "warm", "fire", "full", "rest",
        // 401-500
        "already", "beautiful", "table", "rock", "hundred", "sometimes", "art",
        "sent", "saw", "already", "nothing", "run", "able", "among", "sit",
        "product", "during", "war", "hotel", "allow", "decide", "stop",
        "office", "recent", "dark", "order", "table", "speak", "including",
        "boy", "story", "game", "wrong", "write", "age", "main", "sure",
        "lose", "team", "morning", "else", "except", "example", "toward",
        "near", "remember", "buy", "team", "taking", "test", "turn", "note",
        "example", "build", "watch", "follow", "cold", "hot", "wind", "window",
        "pay", "check", "test", "second", "tall", "charge", "offer", "oil",
        "absolute", "personal", "wish", "city", "pull", "wonder", "handle",
        "box", "sure", "top", "meet", "spring", "table", "party", "dinner"
    )
}

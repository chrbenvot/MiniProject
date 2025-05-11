package com.info2.miniprojet.comparison.impl;

import com.info2.miniprojet.comparison.StringComparator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExactMatchComparatorTest {

    private final StringComparator comparator = new ExactMatchComparator();

    @Test
    void getNameShouldReturnCorrectName() {
        assertEquals("EXACT_STRING", comparator.getName()); // Or your chosen name
    }

    @Test
    void isScoreDistanceShouldReturnFalse() {
        assertFalse(comparator.isScoreDistance(), "Exact match score should be a similarity (not distance)");
    }

    @Test
    void calculateScoreShouldReturnOneForIdenticalStrings() {
        assertEquals(1.0, comparator.calculateScore("hello", "hello"), 0.001);
        assertEquals(1.0, comparator.calculateScore("", ""), 0.001); // Empty strings are identical
        assertEquals(1.0, comparator.calculateScore("Test123#", "Test123#"), 0.001);
    }

    @Test
    void calculateScoreShouldReturnZeroForDifferentStrings() {
        assertEquals(0.0, comparator.calculateScore("hello", "world"), 0.001);
        assertEquals(0.0, comparator.calculateScore("Hello", "hello"), 0.001); // Case-sensitive
        assertEquals(0.0, comparator.calculateScore("test", " test"), 0.001); // Leading space
        assertEquals(0.0, comparator.calculateScore("apple", ""), 0.001);
    }

    @Test
    void calculateScoreShouldHandleNullsGracefully() {
        // Based on common ExactMatch implementation:
        // null vs null = 1.0 (identical)
        // null vs non-null = 0.0 (different)
        assertEquals(1.0, comparator.calculateScore(null, null), 0.001, "Null vs Null should be perfect match");
        assertEquals(0.0, comparator.calculateScore("text", null), 0.001, "Text vs Null should be no match");
        assertEquals(0.0, comparator.calculateScore(null, "text"), 0.001, "Null vs Text should be no match");
    }
}
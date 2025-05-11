package com.info2.miniprojet.comparison.impl;

import com.info2.miniprojet.comparison.StringComparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Assuming JaroWinklerComparator uses Apache Commons Text or a similar reliable implementation.
// Ensure commons-text.jar is in your test libraries if using it.

class JaroWinklerComparatorTest {

    private StringComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new JarowinklerComparator(); // Assumes this class is implemented
    }

    @Test
    void getNameShouldReturnCorrectName() {
        assertEquals("JARO_WINKLER", comparator.getName());
    }

    @Test
    void isScoreDistanceShouldReturnFalse() {
        assertFalse(comparator.isScoreDistance());
    }

    @Test
    void calculateScoreForIdenticalStringsShouldBeOne() {
        assertEquals(1.0, comparator.calculateScore("martha", "martha"), 0.0001);
        assertEquals(1.0, comparator.calculateScore("", ""), 0.0001);
    }

    @Test
    void calculateScoreStandardExamples() {
        assertEquals(0.9611, comparator.calculateScore("MARTHA", "MARHTA"), 0.0001);
        assertEquals(0.840, comparator.calculateScore("DWAYNE", "DUANE"), 0.0001); // Values may vary slightly if not using common prefix boost by default
        assertEquals(0.8133, comparator.calculateScore("DIXON", "DICKSONX"), 0.0001); // Jaro part is lower, Winkler boosts for prefix
        assertEquals(0.92, comparator.calculateScore("apple", "apply"), 0.0001); // With Winkler boost
    }

    @Test
    void calculateScoreWithEmptyString() {
        assertEquals(0.0, comparator.calculateScore("hello", ""), 0.0001);
        assertEquals(0.0, comparator.calculateScore("", "world"), 0.0001);
    }

    @Test
    void calculateScoreWithNullsShouldBeHandled() {
        // Apache Commons Text JaroWinklerSimilarity throws IllegalArgumentException for nulls.
        // Test for that or for how your specific implementation handles it (e.g., returns 0.0).
        // This example assumes it's robust and returns 0.0 for safety if one is null.
        assertThrows(IllegalArgumentException.class, () -> comparator.calculateScore("text", null), "Expected IAE for null input");
        assertThrows(IllegalArgumentException.class, () -> comparator.calculateScore(null, "text"), "Expected IAE for null input");
        assertThrows(IllegalArgumentException.class, () -> comparator.calculateScore(null, null), "Expected IAE for null input");

        // If you implement it to return 0.0 for nulls:
        // assertEquals(0.0, comparator.calculateScore("text", null), 0.0001);
        // assertEquals(0.0, comparator.calculateScore(null, "text"), 0.0001);
        // assertEquals(0.0, comparator.calculateScore(null, null), 0.0001); // Or 1.0 if defined as such
    }
}
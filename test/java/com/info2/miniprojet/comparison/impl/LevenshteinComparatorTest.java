package com.info2.miniprojet.comparison.impl;

import com.info2.miniprojet.comparison.StringComparator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LevenshteinComparatorTest {

    private final StringComparator comparator = new LevenshteinComparator();

    @Test
    void getNameShouldReturnCorrectName() {
        assertEquals("LEVENSHTEIN", comparator.getName()); // Or your chosen name
    }

    @Test
    void isScoreDistanceShouldReturnTrue() {
        assertTrue(comparator.isScoreDistance(), "Levenshtein score is a distance");
    }

    @Test
    void calculateScoreForIdenticalStringsShouldBeZero() {
        assertEquals(0.0, comparator.calculateScore("hello", "hello"), 0.001);
        assertEquals(0.0, comparator.calculateScore("", ""), 0.001);
    }

    @Test
    void calculateScoreForSingleEditStrings() {
        assertEquals(1.0, comparator.calculateScore("kitten", "sitten"), 0.001); // Substitution
        assertEquals(1.0, comparator.calculateScore("apple", "aple"), 0.001);   // Deletion
        assertEquals(1.0, comparator.calculateScore("aple", "apple"), 0.001);   // Insertion
    }

    @Test
    void calculateScoreForMultipleEdits() {
        assertEquals(3.0, comparator.calculateScore("saturday", "sunday"), 0.001); // sat -> sun (1), ur -> NUL (2) = 3
        assertEquals(2.0, comparator.calculateScore("flaw", "lawn"), 0.001); // f->l (1), add n (1)
    }

    @Test
    void calculateScoreWithDifferentCases() {
        // Your current Levenshtein impl converts to lowercase internally.
        // If it didn't, "Hello" vs "hello" would be 1.
        // Assuming it does convert to lowercase as per your provided code:
        assertEquals(0.0, comparator.calculateScore("Hello", "hello"), 0.001, "Should be case-insensitive if internal toLowerCase is used");
        assertEquals(1.0, comparator.calculateScore("Apple", "apply"), 0.001); // A->a (0 if case-insensitive), e->y (1)
    }

    @Test
    void calculateScoreWithEmptyString() {
        assertEquals(5.0, comparator.calculateScore("hello", ""), 0.001); // 5 deletions
        assertEquals(5.0, comparator.calculateScore("", "hello"), 0.001); // 5 insertions
    }

    @Test
    void calculateScoreWithNullsShouldReturnLengthOfOtherString() {
        // Based on your LevenshteinComparator's null handling
        assertEquals(0.0, comparator.calculateScore(null, null), 0.001);
        assertEquals(5.0, comparator.calculateScore("hello", null), 0.001);
        assertEquals(5.0, comparator.calculateScore(null, "world"), 0.001);
    }
}
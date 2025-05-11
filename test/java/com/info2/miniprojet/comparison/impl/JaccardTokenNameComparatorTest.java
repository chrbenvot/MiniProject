package com.info2.miniprojet.comparison.impl;

import com.info2.miniprojet.comparison.NameComparator;
import com.info2.miniprojet.core.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;

class JaccardTokenNameComparatorTest {

    private NameComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new JaccardTokenNameComparator();
    }

    @Test
    void getNameShouldReturnCorrectName() {
        assertEquals("JACCARD_TOKEN_SET", comparator.getName());
    }

    @Test
    void isScoreDistanceShouldReturnFalse() {
        assertFalse(comparator.isScoreDistance());
    }

    @Test
    void calculateScoreForIdenticalTokenSetsShouldBeOne() {
        Name name1 = new Name("id1", "John Michael Smith", Arrays.asList("john", "michael", "smith"));
        assertEquals(1.0, comparator.calculateScore(name1, name1), 0.0001);
    }

    @Test
    void calculateScoreForCompletelyDifferentTokenSetsShouldBeZero() {
        Name name1 = new Name("id1", "Apple Banana", Arrays.asList("apple", "banana"));
        Name name2 = new Name("id2", "Orange Kiwi", Arrays.asList("orange", "kiwi"));
        assertEquals(0.0, comparator.calculateScore(name1, name2), 0.0001);
    }

    @Test
    void calculateScoreWithPartialOverlap() {
        Name name1 = new Name("id1", "John Michael Smith", Arrays.asList("john", "michael", "smith"));
        Name name2 = new Name("id2", "John David Wilson", Arrays.asList("john", "david", "wilson"));
        // Intersection: {"john"} (size 1)
        // Union: {"john", "michael", "smith", "david", "wilson"} (size 5)
        // Score: 1.0 / 5.0 = 0.2
        assertEquals(0.2, comparator.calculateScore(name1, name2), 0.0001);
    }

    @Test
    void calculateScoreWithMoreOverlap() {
        Name name1 = new Name("id1", "Alpha Beta Gamma", Arrays.asList("alpha", "beta", "gamma"));
        Name name2 = new Name("id2", "Alpha Beta Delta", Arrays.asList("alpha", "beta", "delta"));
        // Intersection: {"alpha", "beta"} (size 2)
        // Union: {"alpha", "beta", "gamma", "delta"} (size 4)
        // Score: 2.0 / 4.0 = 0.5
        assertEquals(0.5, comparator.calculateScore(name1, name2), 0.0001);
    }

    @Test
    void calculateScoreWithDuplicateTokensInInputShouldBehaveAsSets() {
        Name name1 = new Name("id1", "apple apple pear", Arrays.asList("apple", "apple", "pear")); // Set: {"apple", "pear"}
        Name name2 = new Name("id2", "apple banana", Arrays.asList("apple", "banana"));       // Set: {"apple", "banana"}
        // Intersection: {"apple"} (size 1)
        // Union: {"apple", "pear", "banana"} (size 3)
        // Score: 1.0 / 3.0
        assertEquals(1.0 / 3.0, comparator.calculateScore(name1, name2), 0.0001);
    }

    @Test
    void calculateScoreWithOneListEmptyShouldBeZero() {
        Name name1 = new Name("id1", "Not Empty", Arrays.asList("not", "empty"));
        Name name2 = new Name("id2", "", Collections.emptyList());
        assertEquals(0.0, comparator.calculateScore(name1, name2), 0.0001);
        assertEquals(0.0, comparator.calculateScore(name2, name1), 0.0001);
    }

    @Test
    void calculateScoreWithBothListsEmptyShouldBeOne() {
        Name name1 = new Name("id1", "", Collections.emptyList());
        Name name2 = new Name("id2", "", Collections.emptyList());
        assertEquals(1.0, comparator.calculateScore(name1, name2), 0.0001, "Both empty should be perfect match by Jaccard definition (0/0 can be 1)");
    }

    @Test
    void calculateScoreWithNullNamesShouldReturnZero() {
        Name name1 = new Name("id1", "Valid", Arrays.asList("valid"));
        assertEquals(0.0, comparator.calculateScore(null, name1), 0.0001);
        assertEquals(0.0, comparator.calculateScore(name1, null), 0.0001);
        assertEquals(0.0, comparator.calculateScore(null, null), 0.0001); // Or 1.0, consistent with both empty
    }
}
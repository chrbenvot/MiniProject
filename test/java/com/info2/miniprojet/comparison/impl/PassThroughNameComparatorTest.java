package com.info2.miniprojet.comparison.impl;

import com.info2.miniprojet.comparison.NameComparator;
import com.info2.miniprojet.comparison.StringComparator;
import com.info2.miniprojet.core.Name;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;

class PassThroughNameComparatorTest {

    @Test
    void getNameShouldReturnCorrectName() {
        // Test with a real StringComparator implementation
        StringComparator exactMatcher = new ExactMatchComparator();
        NameComparator comparator = new PassThroughNameComparator(exactMatcher);
        // The PassThroughNameComparator.getName() might be hardcoded
        // or could try to incorporate the name of the internal comparator.
        // Adjust assertion based on your actual PassThroughNameComparator.getName() implementation.
        assertEquals("PASS_THROUGH_NAME", comparator.getName());
        // If it includes internal name: assertTrue(comparator.getName().contains(exactMatcher.getName()));
    }

    @Test
    void isScoreDistanceShouldReflectInternalComparator() {
        // Test with a real StringComparator known to be a distance
        StringComparator levenshtein = new LevenshteinComparator(); // Assumes implemented
        NameComparator comparatorDist = new PassThroughNameComparator(levenshtein);
        assertTrue(comparatorDist.isScoreDistance());

        // Test with a real StringComparator known to be a similarity
        StringComparator exactMatch = new ExactMatchComparator();
        NameComparator comparatorSim = new PassThroughNameComparator(exactMatch);
        assertFalse(comparatorSim.isScoreDistance());
    }

    @Test
    void calculateScoreShouldDelegateAndUseJoinedTokens() {
        // Use a real StringComparator where you know the expected outcome
        StringComparator testStringComp = new StringComparator() {
            @Override
            public double calculateScore(String s1, String s2) {
                if ("john smith".equals(s1) && "jon smyth".equals(s2)) return 0.8;
                if ("mary sue".equals(s1) && "mary sue".equals(s2)) return 1.0;
                return 0.0; // Default for other pairs
            }
            @Override public boolean isScoreDistance() { return false; }
            @Override public String getName() { return "TEST_SC"; }
        };

        NameComparator comparator = new PassThroughNameComparator(testStringComp);

        Name name1 = new Name("id1", "John Smith", Arrays.asList("john", "smith"));
        Name name2 = new Name("id2", "Jon Smyth", Arrays.asList("jon", "smyth"));
        Name name3 = new Name("id3", "Mary Sue", Arrays.asList("mary", "sue"));

        assertEquals(0.8, comparator.calculateScore(name1, name2), 0.001);
        assertEquals(1.0, comparator.calculateScore(name3, name3), 0.001);
    }

    @Test
    void calculateScoreWithEmptyTokensShouldHandleCorrectly() {
        StringComparator exactComp = new ExactMatchComparator();
        NameComparator comparator = new PassThroughNameComparator(exactComp);

        Name name1 = new Name("id1", " ", List.of());
        Name name2 = new Name("id2", "Test", Arrays.asList("test"));
        Name name3 = new Name("id3", "", List.of());

        assertEquals(0.0, comparator.calculateScore(name1, name2), 0.001);
        assertEquals(1.0, comparator.calculateScore(name1, name3), 0.001);
    }

    @Test
    void calculateScoreWithNullNamesShouldReturnZero() {
        NameComparator comparator = new PassThroughNameComparator(new ExactMatchComparator());
        Name name1 = new Name("id1", "Valid Name", Arrays.asList("valid", "name"));
        assertEquals(0.0, comparator.calculateScore(name1, null), 0.001);
        assertEquals(0.0, comparator.calculateScore(null, name1), 0.001);
        assertEquals(0.0, comparator.calculateScore(null, null), 0.001);
    }
}
package com.info2.miniprojet.preprocessing.impl;

import com.info2.miniprojet.preprocessing.Preprocessor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

class LowercaseNormalizerTest {

    private final Preprocessor normalizer = new LowercaseNormalizer();

    @Test
    void getNameShouldReturnCorrectName() {
        assertEquals("LOWERCASE", normalizer.getName());
    }

    @Test
    void preprocessShouldConvertAllTokensToLowercase() {
        List<String> input = Arrays.asList("John", "Fitzgerald", "SMITH");
        List<String> expected = Arrays.asList("john", "fitzgerald", "smith");
        List<String> actual = normalizer.preprocess(input);
        assertEquals(expected, actual);
    }

    @Test
    void preprocessWithAlreadyLowercaseShouldRemainLowercase() {
        List<String> input = Arrays.asList("mary", "anne");
        List<String> expected = Arrays.asList("mary", "anne");
        List<String> actual = normalizer.preprocess(input);
        assertEquals(expected, actual);
    }

    @Test
    void preprocessWithMixedCaseShouldConvertToAllLowercase() {
        List<String> input = Arrays.asList("MiXeD", "CaSe");
        List<String> expected = Arrays.asList("mixed", "case");
        List<String> actual = normalizer.preprocess(input);
        assertEquals(expected, actual);
    }

    @Test
    void preprocessWithNumbersAndSymbolsShouldLeaveThemUnchanged() {
        List<String> input = Arrays.asList("Name123", "#Tag", "Word-With-Hyphen");
        List<String> expected = Arrays.asList("name123", "#tag", "word-with-hyphen");
        List<String> actual = normalizer.preprocess(input);
        assertEquals(expected, actual);
    }

    @Test
    void preprocessEmptyListShouldReturnEmptyList() {
        List<String> actual = normalizer.preprocess(Collections.emptyList());
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    void preprocessNullListShouldReturnEmptyList() {
        List<String> actual = normalizer.preprocess(null);
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    void preprocessListWithNullStringShouldHandleGracefully() {
        List<String> input = new ArrayList<>();
        input.add("Hello");
        input.add(null); // Assuming LowercaseNormalizer skips nulls or converts to "null" if not handled
        input.add("World");

        // If LowercaseNormalizer.preprocess skips nulls:
        List<String> expected = Arrays.asList("hello", "world");
        // If it processes nulls into "null" strings (less ideal):
        // List<String> expected = Arrays.asList("hello", "null", "world");

        List<String> actual = normalizer.preprocess(input);
        // Adjust assertion based on how your LowercaseNormalizer handles nulls in the list
        assertEquals(expected, actual, "Should handle nulls in list gracefully (e.g., by skipping them or as defined)");
    }
}
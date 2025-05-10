package com.info2.miniprojet.preprocessing.impl;

import com.info2.miniprojet.preprocessing.Preprocessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
class NoOpPreprocessorTest {

    @Test
    void getNameShouldReturnCorrectName() {
        Preprocessor preprocessor = new NoOpPreprocessor();
        assertEquals("NOOP", preprocessor.getName(), "getName() should return 'NOOP'");
    }

    @Test
    void preprocessShouldReturnSameListContentForNonNullInput() {
        Preprocessor preprocessor = new NoOpPreprocessor();
        List<String> input = Arrays.asList("Test", "Input");
        List<String> expected = Arrays.asList("Test", "Input"); // NoOp should not change content
        List<String> actual = preprocessor.preprocess(input);

        assertNotNull(actual, "Processed list should not be null");
        assertEquals(expected.size(), actual.size(), "List sizes should be equal");
        assertEquals(expected, actual, "List content should be identical");
        // Ensure it returns a new list instance, not the same one
        assertNotSame(input, actual, "Should return a new list instance, not modify input in place");
    }

    @Test
    void preprocessShouldReturnEmptyListForEmptyInput() {
        Preprocessor preprocessor = new NoOpPreprocessor();
        List<String> input = new ArrayList<>();
        List<String> actual = preprocessor.preprocess(input);

        assertNotNull(actual, "Processed list should not be null for empty input");
        assertTrue(actual.isEmpty(), "Processed list should be empty for empty input");
    }

    @Test
    void preprocessShouldReturnEmptyListForNullInput() {
        Preprocessor preprocessor = new NoOpPreprocessor();
        List<String> actual = preprocessor.preprocess(null);

        assertNotNull(actual, "Processed list should not be null for null input");
        assertTrue(actual.isEmpty(), "Processed list should be empty for null input");
    }
}
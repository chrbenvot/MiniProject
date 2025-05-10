package com.info2.miniprojet.preprocessing.impl;

import com.info2.miniprojet.preprocessing.Preprocessor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;
import java.util.Collections; // For Collections.emptyList()
import java.util.ArrayList;


class SimpleTokenizerTest {

    private final Preprocessor tokenizer = new SimpleTokenizer();

    @Test
    void getNameShouldReturnCorrectName() {
        assertEquals("TOKENIZE", tokenizer.getName());
    }

    @Test
    void preprocessSingleStringInListShouldTokenizeBySpace() {
        List<String> input = List.of("John Fitzgerald Smith"); // Input is List<String>
        List<String> expected = Arrays.asList("John", "Fitzgerald", "Smith");
        List<String> actual = tokenizer.preprocess(input);
        assertEquals(expected, actual);
    }

    @Test
    void preprocessMultipleStringsInListShouldTokenizeAll() {
        List<String> input = Arrays.asList("Mary Anne", "O'Malley");
        List<String> expected = Arrays.asList("Mary", "Anne", "O'Malley"); // O'Malley is one token by space
        List<String> actual = tokenizer.preprocess(input);
        assertEquals(expected, actual);
    }

    @Test
    void preprocessStringWithMultipleSpacesShouldHandleCorrectly() {
        List<String> input = List.of("Jane  Doe  "); // Multiple spaces, trailing space
        List<String> expected = Arrays.asList("Jane", "Doe"); // .trim().split("\\s+") handles this
        List<String> actual = tokenizer.preprocess(input);
        assertEquals(expected, actual);
    }

    @Test
    void preprocessEmptyStringInListShouldResultInEmptyTokenListOrBeIgnored() {
        List<String> input = List.of("");
        // Your SimpleTokenizer currently adds tokens from splitting.
        // An empty string split by "\\s+" results in an array with one empty string: `[""]`
        // If `trim()` is used before split, it becomes empty.
        // Let's assume after trim().split(), it might ignore truly empty results.
        // The current corrected SimpleTokenizer would filter this out if it was '  '.trim().
        // If it was just "", .split() yields [""]. If that's undesirable, SimpleTokenizer needs adjustment.
        // For now, assuming current behavior of your SimpleTokenizer.
        // My corrected SimpleTokenizer does: token.trim().split("\\s+"), so "" -> [""], "  " -> []
        // If input is List.of(""), expected is List.of("")
        // If input is List.of("  "), expected is Collections.emptyList()
        List<String> actualEmpty = tokenizer.preprocess(List.of(""));
        assertEquals(List.of(""), actualEmpty, "Empty string token should produce one empty string token or be empty list, depends on split impl");

        List<String> actualSpaces = tokenizer.preprocess(List.of("   "));
        assertTrue(actualSpaces.isEmpty(), "String with only spaces should result in empty list if trimmed before split");
    }

    @Test
    void preprocessListWithEmptyAndNonEmptyStrings() {
        List<String> input = Arrays.asList("First", "", "Last", "  ");
        List<String> expected = Arrays.asList("First", "", "Last"); // Assuming "" becomes a token, "  " is ignored
        List<String> actual = tokenizer.preprocess(input);
        assertEquals(expected, actual);
    }


    @Test
    void preprocessNullInputListShouldReturnEmptyList() {
        List<String> actual = tokenizer.preprocess(null);
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    void preprocessEmptyInputListShouldReturnEmptyList() {
        List<String> actual = tokenizer.preprocess(new ArrayList<>());
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }
}
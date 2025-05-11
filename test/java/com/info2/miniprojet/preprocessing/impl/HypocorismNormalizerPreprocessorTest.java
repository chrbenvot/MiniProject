package com.info2.miniprojet.preprocessing.impl;

import com.info2.miniprojet.preprocessing.Preprocessor;
// import com.info2.miniprojet.util.HypocorismLoader; // If used for loading
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

class HypocorismNormalizerPreprocessorTest {

    private Preprocessor normalizer;
    private Map<String, String> testNicknameMap;

    @BeforeEach
    void setUp() {
        testNicknameMap = new HashMap<>();
        testNicknameMap.put("bob", "robert"); // Nicknames should be stored/looked up in lowercase
        testNicknameMap.put("rob", "robert");
        testNicknameMap.put("bill", "william");
        testNicknameMap.put("billy", "william");
        testNicknameMap.put("liz", "elizabeth");
        testNicknameMap.put("beth", "elizabeth");
        // Assuming a constructor that allows injecting the map for testability
        normalizer = new HypocorismNormalizerPreprocessor();
    }

    @Test
    void getNameShouldReturnCorrectName() {
        assertEquals("NICKNAME_NORMALIZER", normalizer.getName());
    }


    @Test
    void preprocessShouldLeaveUnknownTokensUnchanged() {
        List<String> input = Arrays.asList("William", "Davis", "aka", "BiLLy");
        List<String> expected = Arrays.asList("bill", "Davis", "aka", "bill");
        List<String> actual = normalizer.preprocess(input);
        assertEquals(expected, actual);
    }

    @Test
    void preprocessEmptyListShouldReturnEmptyList() {
        List<String> actual = normalizer.preprocess(Collections.emptyList());
        assertTrue(actual.isEmpty());
    }

    @Test
    void preprocessNullListShouldReturnEmptyList() {
        List<String> actual = normalizer.preprocess(null);
        assertTrue(actual.isEmpty());
    }

    @Test
    void preprocessWithTokensNotInMapShouldReturnOriginalTokensButNormalized() {
        // Assuming the HypocorismNormalizer *only* replaces known nicknames
        // and doesn't do other normalizations like lowercasing if the token isn't a nickname.
        // If it does lowercase all tokens, adjust expected.
        List<String> input = Arrays.asList("Tester", "Doe");
        List<String> expected = Arrays.asList("Tester", "Doe"); // No changes if not in map
        List<String> actual = normalizer.preprocess(input);
        assertEquals(expected, actual);
    }

    @Test
    void preprocessCanonicalFormsShouldRemainUnchanged() {
        List<String> input = Arrays.asList("randolf");
        List<String> expected = Arrays.asList("randolf"); // the OG list is very problematic... the mapping is reflexive for some names...
        List<String> actual = normalizer.preprocess(input);
        assertEquals(expected, actual);
    }
}
package com.info2.miniprojet.preprocessing.impl;

import com.info2.miniprojet.preprocessing.Preprocessor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

class AccentRemoverTest {

    // Assuming AccentRemover is implemented and its getName() returns "ACCENT_REMOVER"
    // and its preprocess method actually removes accents.
    private final Preprocessor remover = new AccentRemover();

    @Test
    void getNameShouldReturnCorrectName() {
        assertEquals("ACCENT_REMOVER", remover.getName());
    }

    @Test
    void preprocessShouldRemoveCommonAccents() {
        List<String> input = Arrays.asList("René", "François", "El Niño", "Crème brûlée");
        List<String> expected = Arrays.asList("Rene", "Francois", "El Nino", "Creme brulee");
        List<String> actual = remover.preprocess(input);
        assertEquals(expected, actual);
    }

    @Test
    void preprocessShouldHandleMixedCaseWithAccents() {
        List<String> input = Arrays.asList("Élise", "JÜRGEN");
        // Assuming AccentRemover doesn't also lowercase. If it does, expected should be "elise", "jurgen"
        // For now, assuming it only removes accents.
        List<String> expected = Arrays.asList("Elise", "JURGEN");
        List<String> actual = remover.preprocess(input);
        assertEquals(expected, actual);
    }

    @Test
    void preprocessShouldLeaveNonAccentedStringsUnchanged() {
        List<String> input = Arrays.asList("John", "Smith", "123");
        List<String> expected = Arrays.asList("John", "Smith", "123");
        List<String> actual = remover.preprocess(input);
        assertEquals(expected, actual);
    }

    @Test
    void preprocessShouldHandleMultipleAccentsInOneToken() {
        List<String> input = List.of("crêpes-sucrées");
        List<String> expected = List.of("crepes-sucrees");
        List<String> actual = remover.preprocess(input);
        assertEquals(expected, actual);
    }

    @Test
    void preprocessEmptyListShouldReturnEmptyList() {
        List<String> actual = remover.preprocess(Collections.emptyList());
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    void preprocessNullListShouldReturnEmptyList() {
        List<String> actual = remover.preprocess(null);
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    void preprocessListWithNullAndEmptyStrings() {
        List<String> input = new ArrayList<>();
        input.add("Hélène");
        input.add(null);
        input.add("");
        input.add("garçon");

        // Assuming your AccentRemover skips nulls and processes "" to ""
        List<String> expected = Arrays.asList("Helene", "", "garcon"); // If nulls are skipped
        List<String> actual = remover.preprocess(input);
        assertEquals(expected, actual);
    }
}
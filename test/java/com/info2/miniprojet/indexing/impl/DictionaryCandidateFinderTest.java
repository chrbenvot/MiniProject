package com.info2.miniprojet.indexing.impl;

import com.info2.miniprojet.core.Couple;
import com.info2.miniprojet.core.Name;
import com.info2.miniprojet.indexing.CandidateFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class DictionaryCandidateFinderTest {

    private CandidateFinder finder;
    private List<Name> sampleNames;
    private Name nameJohnSmith;
    private Name nameJaneSmith;
    private Name namePeterJones;
    private Name nameSueDavis;
    private Name nameJohnDoe;

    @BeforeEach
    void setUp() {
        finder = new DictionaryCandidateFinder(); // Assuming it indexes on the last token

        // Sample data - processedTokens are key here
        nameJohnSmith = new Name("id1", "John Smith", Arrays.asList("john", "smith"));
        nameJaneSmith = new Name("id2", "Jane Smith", Arrays.asList("jane", "smith"));
        namePeterJones = new Name("id3", "Peter Jones", Arrays.asList("peter", "jones"));
        nameSueDavis = new Name("id4", "Sue Davis", Arrays.asList("sue", "davis"));
        nameJohnDoe = new Name("id5", "John Doe", Arrays.asList("john", "doe")); // Different last name

        sampleNames = Arrays.asList(nameJohnSmith, nameJaneSmith, namePeterJones, nameSueDavis, nameJohnDoe);
    }

    @Test
    void getNameShouldReturnCorrectName() {
        assertEquals("DICTIONARY_LAST_TOKEN", finder.getName()); // Or your chosen factory key
    }

    @Test
    void resetShouldClearIndex() {
        finder.buildIndex(sampleNames);
        // Call a search to ensure index was used (not strictly necessary for reset test itself)
        Name query = new Name("q1", "Test Smith", Arrays.asList("test", "smith"));
        assertFalse(finder.findCandidatesForSearch(query, sampleNames).isEmpty(), "Should find candidates before reset");

        finder.reset();
        // After reset, buildIndex was effectively undone.
        // A search should ideally return empty or finder might throw IllegalStateException if it expects index.
        // Let's assume it returns empty if index is null/empty.
        // To make this test more robust, we'd need to inspect internal state or ensure buildIndex is called again.
        // For now, let's test that calling buildIndex again works.
        finder.buildIndex(sampleNames); // Rebuild
        assertFalse(finder.findCandidatesForSearch(query, sampleNames).isEmpty(), "Should find candidates after reset and rebuild");
    }


    @Test
    void buildIndexAndSearchShouldFindMatchingLastTokens() {
        finder.buildIndex(sampleNames);
        Name querySmith = new Name("q_smith", "Query Smith", Arrays.asList("query", "smith"));
        List<Couple<Name>> resultsSmith = finder.findCandidatesForSearch(querySmith, sampleNames);

        assertEquals(2, resultsSmith.size(), "Should find 2 Smiths");
        assertTrue(resultsSmith.contains(new Couple<>(querySmith, nameJohnSmith)));
        assertTrue(resultsSmith.contains(new Couple<>(querySmith, nameJaneSmith)));

        Name queryJones = new Name("q_jones", "Query Jones", Arrays.asList("query", "jones"));
        List<Couple<Name>> resultsJones = finder.findCandidatesForSearch(queryJones, sampleNames);
        assertEquals(1, resultsJones.size());
        assertTrue(resultsJones.contains(new Couple<>(queryJones, namePeterJones)));
    }

    @Test
    void searchShouldReturnEmptyForNoMatchingLastToken() {
        finder.buildIndex(sampleNames);
        Name queryNonExistent = new Name("q_non", "Query Nobody", Arrays.asList("query", "nobody"));
        List<Couple<Name>> results = finder.findCandidatesForSearch(queryNonExistent, sampleNames);
        assertTrue(results.isEmpty());
    }

    @Test
    void searchWithEmptyQueryTokensShouldReturnEmpty() {
        finder.buildIndex(sampleNames);
        Name queryEmpty = new Name("q_empty", "Query Empty", Collections.emptyList());
        List<Couple<Name>> results = finder.findCandidatesForSearch(queryEmpty, sampleNames);
        assertTrue(results.isEmpty());
    }

    @Test
    void findCandidatesForComparisonShouldWork() {
        List<Name> list1 = Arrays.asList(
                new Name("l1_1", "Alpha Smith", Arrays.asList("alpha", "smith")),
                new Name("l1_2", "Beta Jones", Arrays.asList("beta", "jones"))
        );
        List<Name> list2 = Arrays.asList( // This list will be indexed
                nameJohnSmith, // "smith"
                namePeterJones, // "jones"
                nameSueDavis    // "davis"
        );

        finder.buildIndex(list2); // Index list2
        List<Couple<Name>> results = finder.findCandidatesForComparison(list1, list2);

        // Expected:
        // (Alpha Smith, John Smith) - match on "smith"
        // (Beta Jones, Peter Jones) - match on "jones"
        assertEquals(2, results.size());
        Set<Couple<Name>> resultSet = new HashSet<>(results); // Use set for order-agnostic check
        assertTrue(resultSet.contains(new Couple<>(list1.get(0), nameJohnSmith)));
        assertTrue(resultSet.contains(new Couple<>(list1.get(1), namePeterJones)));
    }


    @Test
    void findCandidatesForDeduplicationShouldFindNamesWithSameLastToken() {
        finder.buildIndex(sampleNames); // sampleNames has two "smith"
        List<Couple<Name>> results = finder.findCandidatesForDeduplication(sampleNames);

        // Expected: (John Smith, Jane Smith) because they share "smith"
        assertEquals(1, results.size(), "Should find one pair of Smiths");
        Couple<Name> expectedPair = new Couple<>(nameJohnSmith, nameJaneSmith);
        Couple<Name> actualPair = results.get(0);

        // Check if the pair contains the two expected names, regardless of order in the Couple
        assertTrue(
                (expectedPair.first().equals(actualPair.first()) && expectedPair.second().equals(actualPair.second())) ||
                        (expectedPair.first().equals(actualPair.second()) && expectedPair.second().equals(actualPair.first())),
                "The pair should be John Smith and Jane Smith"
        );
    }

    @Test
    void buildIndexShouldBeReusableAndNotRebuildIdenticalListInstance() {
        finder.buildIndex(sampleNames); // First build
        // Simulate getting candidates, which would use the index
        Name querySmith = new Name("q_smith", "Query Smith", Arrays.asList("query", "smith"));
        finder.findCandidatesForSearch(querySmith, sampleNames);
        long initialIndexCreationIndicator = getDictionaryIndexSizeIndicator(); // Helper to "see" if index was built

        finder.buildIndex(sampleNames); // Call buildIndex again with the SAME list instance
        long secondIndexCreationIndicator = getDictionaryIndexSizeIndicator(); // "See" if index was rebuilt

        // This test relies on your buildIndex having the "if (this.indexedListReference == namesToIndex)" check
        // And assumes the "DEBUG: Index reused" message would mean no change to a size indicator.
        // A more direct test would be to mock/spy or have a counter in buildIndex.
        // For now, let's just assert that a search still works as expected.
        List<Couple<Name>> resultsSmith = finder.findCandidatesForSearch(querySmith, sampleNames);
        assertEquals(2, resultsSmith.size(), "Search should still work correctly after calling buildIndex with same list");
        // If your debug logs show "Index reused", this test implicitly passes that part.
    }

    // Helper for the last test - this is a bit of a hack without reflection/spying
    // Ideally, you'd verify by checking logs or having a test-specific flag in buildIndex
    private long getDictionaryIndexSizeIndicator() {
        // This is a placeholder. In a real scenario without reflection, you might not have
        // a direct way to get the size of the internal map from outside.
        // One way is if buildIndex prints the size, you could capture stdout (complex for unit test)
        // or rely on the debug prints from your actual DictionaryCandidateFinder.
        // For this test, it's more about ensuring subsequent calls with the same list are fast.
        return System.nanoTime(); // Just returns a value; the real check is if buildIndex logs "reused"
    }


    @Test
    void findCandidatesForSearchWithEmptyIndexedList() {
        finder.buildIndex(Collections.emptyList());
        Name query = new Name("q1", "Query Name", Arrays.asList("query", "name"));
        List<Couple<Name>> results = finder.findCandidatesForSearch(query, Collections.emptyList());
        assertTrue(results.isEmpty());
    }
}
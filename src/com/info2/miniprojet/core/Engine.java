package com.info2.miniprojet.core;

import com.info2.miniprojet.config.Configuration;
import com.info2.miniprojet.preprocessing.*;
import com.info2.miniprojet.preprocessing.impl.*;
import com.info2.miniprojet.indexing.*;
import com.info2.miniprojet.indexing.impl.*;
import com.info2.miniprojet.comparison.*;
import com.info2.miniprojet.comparison.impl.*;
import com.info2.miniprojet.factory.StrategyFactory;

import java.util.ArrayList;
import java.util.List;

public class Engine {


    public List<ComparisonResult> performSearch(Name queryName, List<Name> namesList, Configuration config) {
        System.out.println("Engine: Starting Search for '" + queryName + "'...");

        // 1. Instantiate strategies based on config
        // Note: Error handling for invalid choices could be added here or in create methods
        Preprocessor preprocessor = createPreprocessor(config.getPreprocessorChoice());
        IndexBuilder indexBuilder = createIndexBuilder(config.getIndexBuilderChoice());
        CandidateFinder candidateFinder = createCandidateFinder(config.getCandidateFinderChoice());
        NameComparator nameComparator = createNameComparator(config.getNameComparatorChoice());

        // --- Skeleton Workflow ---
        try {
            // Preprocess the query
            // For skeleton, assume simple tokenization if needed or that preprocessor handles it
            System.out.println("Engine: Preprocessing query...");
            List<String> queryTokens = preprocessor.preprocess(List.of(queryName.orignalName())); // Wrap query in list

            // Preprocess the data list
            System.out.println("Engine: Preprocessing data list (" + namesList.size() + " entries)...");
            List<List<String>> processedNameTokensList = new ArrayList<>(namesList.size());
            for (Name name : namesList) {
                processedNameTokensList.add(preprocessor.preprocess(List.of(name.orignalName()))); // TODO: Wrap each name in a list?
            }

            // Build the index
            System.out.println("Engine: Building index...");
            Object index = indexBuilder.buildIndex(processedNameTokensList); // Build index with token lists

            // Find candidates
            System.out.println("Engine: Finding candidates...");
            // Pass index Object, query tokens. CandidateFinder might need original list size.
            // Adjust ReturnAllCandidateFinder if it needs the size.
            List<Integer> candidateIndices = candidateFinder.findCandidates(queryName,queryTokens, index);
            System.out.println("Engine: Found " + candidateIndices.size() + " candidate indices.");

            // Compare candidates
            System.out.println("Engine: Comparing candidates...");
            List<ComparisonResult> results = new ArrayList<>();
            for (int indexPos : candidateIndices) {
                if (indexPos >= 0 && indexPos < namesList.size()) {
                    List<String> candidateTokens = processedNameTokensList.get(indexPos);
                    double score = nameComparator.calculateScore(queryTokens, candidateTokens);
                    // Use original query and candidate names for the result DTO
                    results.add(new ComparisonResult(queryName, namesList.get(indexPos), score, nameComparator.getName()));
                } else {
                    System.err.println("Engine Warning: Candidate finder returned invalid index: " + indexPos);
                }
            }

            // Filter/Sort (Placeholder for now)
            System.out.println("Engine: Filtering/Sorting results (Not implemented in skeleton)...");
            List<ComparisonResult> finalResults = filterAndSortResults(results, config, nameComparator.isScoreDistance());

            System.out.println("Engine: Search complete. Returning " + finalResults.size() + " results.");
            return finalResults;

        } catch (Exception e) {
            System.err.println("Engine Error during search: " + e.getMessage());
            e.printStackTrace(); // For detailed debugging
            return new ArrayList<>(); // Return empty list on error
        }
    }

    public List<ComparisonResult> performComparison(List<Name> list1, List<Name> list2, Configuration config) {
        System.out.println("Engine: Starting Comparison...");
        // TODO: Implement skeleton logic similar to performSearch
        //       - Instantiate strategies
        //       - Preprocess both lists
        //       - Compare items (e.g., N*M comparison for skeleton)
        //       - Filter/Sort
        System.out.println("Engine: Comparison not fully implemented in skeleton.");
        return new ArrayList<>(); // Return empty list for now
    }

    public List<ComparisonResult> performDeduplication(List<String> namesList, Configuration config) {
        System.out.println("Engine: Starting Deduplication...");
        // TODO: Implement skeleton logic similar to performSearch
        //       - Instantiate strategies
        //       - Preprocess list
        //       - Compare items within the list (N*(N-1)/2 comparisons for skeleton)
        //       - Filter/Sort
        System.out.println("Engine: Deduplication not fully implemented in skeleton.");
        return new ArrayList<>(); // Return empty list for now
    }

    // --- Strategy Instantiation Logic (Private helper methods) ---
    // These return the "lazy" implementations for the skeleton

    private Preprocessor createPreprocessor(String choice) {
        System.out.println("Engine DEBUG: Creating Preprocessor (Choice: " + choice + ")");
        // For skeleton, ignore choice and return the NoOp version
        // TODO: Later, add if/else or switch based on 'choice' string
        return new NoOpPreprocessor(); // Assumes this class exists
    }

    private IndexBuilder createIndexBuilder(String choice) {
        System.out.println("Engine DEBUG: Creating IndexBuilder (Choice: " + choice + ")");
        // TODO: Later, add if/else or switch
        return new NoOpIndexBuilder(); // Assumes this class exists
    }

    private CandidateFinder createCandidateFinder(String choice) {
        System.out.println("Engine DEBUG: Creating CandidateFinder (Choice: " + choice + ")");
        // TODO: Later, add if/else or switch
        // ReturnAllCandidateFinder might need the list size, how to pass it?
        // For now, let's assume it can work without it or gets it implicitly later.
        return new ReturnAllCandidateFinder(); // Assumes this class exists
    }

    private NameComparator createNameComparator(String choice) {
        System.out.println("Engine DEBUG: Creating NameComparator (Choice: " + choice + ")");
        // TODO: Later, add if/else or switch
        // Lazy name comparator uses lazy string comparator
        StringComparator lazyStringComp = new ExactMatchComparator(); // Assumes this class exists
        return new PassThroughNameComparator(lazyStringComp); // Assumes this class exists
    }

    // --- Filtering Logic ---
    private List<ComparisonResult> filterAndSortResults(List<ComparisonResult> potentialMatches, Configuration config, boolean isDistance) {
        // Simple placeholder: Just return the list as is for the skeleton
        System.out.println("Engine DEBUG: filterAndSortResults called with " + potentialMatches.size() + " potential matches.");
        // TODO: Implement actual filtering/sorting based on config.isThresholdMode(),
        //       config.getResultThreshold(), config.getMaxResults(), and isDistance.
        return potentialMatches; // Return unfiltered/unsorted list for skeleton
    }
}
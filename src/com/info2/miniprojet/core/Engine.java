package com.info2.miniprojet.core;

import com.info2.miniprojet.config.Configuration;
import com.info2.miniprojet.factory.StrategyFactory;
import com.info2.miniprojet.preprocessing.Preprocessor;
import com.info2.miniprojet.indexing.CandidateFinder;
import com.info2.miniprojet.comparison.NameComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects; // For checking list equality reference

public class Engine {

    // --- Stateful Strategy Management for CandidateFinder ---
    private CandidateFinder currentCandidateFinder;
    private String lastCandidateFinderChoice;
    // The CandidateFinder itself will also store a reference to the list it indexed


    public Engine() {
        this.currentCandidateFinder = null;
        this.lastCandidateFinderChoice = null;
    }

    // --- Helper to ensure the correct CandidateFinder is active ---
    private void ensureCandidateFinder(String choice, List<Name> listToPotentiallyIndex) {
        if (this.currentCandidateFinder == null || !Objects.equals(choice, this.lastCandidateFinderChoice)) { //Objects.equals is used here for Null-safety
            System.out.println("Engine: CandidateFinder choice changed or not initialized. Creating new: " + choice);
            this.currentCandidateFinder = StrategyFactory.createCandidateFinder(choice);
            this.lastCandidateFinderChoice = choice;
            this.currentCandidateFinder.reset(); // Reset new finder
        }

        // Now, ask the current finder to build/update its index IF NECESSARY
        this.currentCandidateFinder.buildIndex(listToPotentiallyIndex);
    }



    public List<ComparisonResult> performSearch(String rawQueryName, List<Name> namesList, Configuration config) {
        System.out.println("Engine: Starting Search for '" + rawQueryName + "' with " + namesList.size() + " names.");

        //LOGGING statement for performance TODO: actually remove this
        long startTime = System.currentTimeMillis();

        Preprocessor preprocessor = StrategyFactory.createPreprocessor(config.getPreprocessorChoice());
        NameComparator nameComparator = StrategyFactory.createNameComparator(config.getNameComparatorChoice(), config.getStringComparatorForNameCompChoice());

        // Ensure the right CandidateFinder is active and its index is prepared for namesList
        ensureCandidateFinder(config.getCandidateFinderChoice(), namesList);

        // Preprocess the raw query string to create a Name object
        List<String> queryTokens = preprocessor.preprocess(List.of(rawQueryName)); // Wrap query
        Name queryNameObject = new Name("QUERY_" + rawQueryName, rawQueryName, queryTokens); // ID for query

        List<Couple<Name>> candidatePairs;
        try {
            candidatePairs = this.currentCandidateFinder.findCandidatesForSearch(queryNameObject, namesList);
        } catch (IllegalStateException e) {
            System.err.println("Engine Error (Search): " + e.getMessage() + " Did you call buildIndex first on the CandidateFinder?");
            return new ArrayList<>();
        }
        System.out.println("Engine: Found " + candidatePairs.size() + " candidate pairs for search.");

        List<ComparisonResult> comparisonResults = new ArrayList<>();
        for (Couple<Name> pair : candidatePairs) {
            if (pair.first() == null || pair.second() == null) continue; // Robustness
            double score = nameComparator.calculateScore(pair.first(), pair.second());
            comparisonResults.add(new ComparisonResult(
                    pair.first().originalName(),
                    pair.second().originalName(),
                    score,
                    nameComparator.getName()
            ));
        }
        boolean isDistance=nameComparator.isScoreDistance();

        comparisonResults.sort((r1, r2) -> {
            if (isDistance) {
                return Double.compare(r1.score(), r2.score()); // Ascending for distance
            } else {
                return Double.compare(r2.score(), r1.score()); // Descending for similarity
            }
        }); // This is why comparisonResults implemented the Comparable interface
        //LOGGING statement for performance TODO: actually remove this
        long endTime = System.currentTimeMillis();
        System.out.println("Engine: Search completed in " + (endTime - startTime) + " ms.");
        return filterAndSortResults(comparisonResults, config, nameComparator.isScoreDistance());
    }

    public List<ComparisonResult> performComparison(List<Name> list1, List<Name> list2, Configuration config) {
        System.out.println("Engine: Starting Comparison between list1 (" + list1.size() + ") and list2 (" + list2.size() + ").");

        //LOGGING statement for performance TODO: actually remove this
        long startTime = System.currentTimeMillis();
        NameComparator nameComparator = StrategyFactory.createNameComparator(config.getNameComparatorChoice(), config.getStringComparatorForNameCompChoice());

        // Ensure the right CandidateFinder is active.
        // For comparison, we choose to index list2 and iterate through list1.
        ensureCandidateFinder(config.getCandidateFinderChoice(), list2); // Index list2

        List<Couple<Name>> candidatePairs;
        try {
            // The finder's comparison method iterates list1 and uses its internal index (of list2)
            candidatePairs = this.currentCandidateFinder.findCandidatesForComparison(list1, list2);
        } catch (IllegalStateException e) {
            System.err.println("Engine Error (Compare): " + e.getMessage() + " Did you call buildIndex on the CandidateFinder for list2?");
            return new ArrayList<>();
        }
        System.out.println("Engine: Found " + candidatePairs.size() + " candidate pairs for comparison.");

        List<ComparisonResult> comparisonResults = new ArrayList<>();
        for (Couple<Name> pair : candidatePairs) {
            if (pair.first() == null || pair.second() == null) continue;
            double score = nameComparator.calculateScore(pair.first(), pair.second());
            comparisonResults.add(new ComparisonResult(
                    pair.first().originalName(),
                    pair.second().originalName(),
                    score,
                    nameComparator.getName()
            ));
        }

        boolean isDistance=nameComparator.isScoreDistance();

        comparisonResults.sort((r1, r2) -> {
            if (isDistance) {
                return Double.compare(r1.score(), r2.score()); // Ascending for distance
            } else {
                return Double.compare(r2.score(), r1.score()); // Descending for similarity
            }
        });
        //LOGGING statement for performance TODO: actually remove this
        long endTime = System.currentTimeMillis();
        System.out.println("Engine: Search completed in " + (endTime - startTime) + " ms.");
        return filterAndSortResults(comparisonResults, config, nameComparator.isScoreDistance());
    }

    public List<ComparisonResult> performDeduplication(List<Name> namesList, Configuration config) {
        System.out.println("Engine: Starting Deduplication for list of " + namesList.size() + " names.");

        //LOGGING statement for performance TODO: actually remove this
        long startTime = System.currentTimeMillis();
        NameComparator nameComparator = StrategyFactory.createNameComparator(config.getNameComparatorChoice(), config.getStringComparatorForNameCompChoice());

        // Ensure the right CandidateFinder is active and its index is prepared for namesList
        ensureCandidateFinder(config.getCandidateFinderChoice(), namesList);

        List<Couple<Name>> candidatePairs;
        try {
            candidatePairs = this.currentCandidateFinder.findCandidatesForDeduplication(namesList);
        } catch (IllegalStateException e) {
            System.err.println("Engine Error (Dedupe): " + e.getMessage() + " Did you call buildIndex first on the CandidateFinder?");
            return new ArrayList<>();
        }
        System.out.println("Engine: Found " + candidatePairs.size() + " candidate pairs for deduplication.");

        List<ComparisonResult> comparisonResults = new ArrayList<>();
        for (Couple<Name> pair : candidatePairs) {
            if (pair.first() == null || pair.second() == null) continue;
            double score = nameComparator.calculateScore(pair.first(), pair.second());
            comparisonResults.add(new ComparisonResult(
                    pair.first().originalName(),
                    pair.second().originalName(),
                    score,
                    nameComparator.getName()
            ));
        }

        boolean isDistance=nameComparator.isScoreDistance();

        comparisonResults.sort((r1, r2) -> {
            if (isDistance) {
                return Double.compare(r1.score(), r2.score()); // Ascending for distance
            } else {
                return Double.compare(r2.score(), r1.score()); // Descending for similarity
            }
        });
        //LOGGING statement for performance TODO: actually remove this
        long endTime = System.currentTimeMillis();
        System.out.println("Engine: Search completed in " + (endTime - startTime) + " ms.");
        return filterAndSortResults(comparisonResults, config, nameComparator.isScoreDistance());
    }

    // --- Filtering Logic ---
    private List<ComparisonResult> filterAndSortResults(List<ComparisonResult> sortedMatches, Configuration config, boolean isDistance) {
        // This method assumes sortedMatches is ALREADY sorted appropriately
        // (e.g., higher score better if not distance, lower score better if distance)
        // For Comparable in ComparisonResult: if it sorts "higher score better", and you have a distance, you'd reverse the list.
        // Let's assume ComparisonResult.compareTo sorts higher score = "better" (comes first).
        // If isDistance is true, we need to reverse if default Comparable sorts by higher is better.
        // This part needs careful alignment with ComparisonResult.compareTo()

        List<ComparisonResult> filteredResults = new ArrayList<>();
        if (config.isThresholdMode()) {
            double threshold = config.getResultThreshold();
            for (ComparisonResult res : sortedMatches) {
                if (isDistance) {
                    if (res.score() <= threshold) filteredResults.add(res);
                } else {
                    if (res.score() >= threshold) filteredResults.add(res);
                }
            }
        } else { // Max results mode
            int max = config.getMaxResults();
            if (max <= 0) { // Sentinel value for "show all" (assuming 0 or negative means all)
                System.out.println("Engine: Max results set to show all. Returning " + sortedMatches.size() + " matches.");
                return new ArrayList<>(sortedMatches); // Return all sorted matches (new list for safety)
            } else {
                for (int i = 0; i < Math.min(max, sortedMatches.size()); i++) {
                    filteredResults.add(sortedMatches.get(i));
                }
            }
        }
        System.out.println("Engine: Filtered results down to " + filteredResults.size());
        return filteredResults;
    }
}
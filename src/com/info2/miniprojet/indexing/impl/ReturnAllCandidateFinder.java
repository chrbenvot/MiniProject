package com.info2.miniprojet.indexing.impl;

import com.info2.miniprojet.core.Couple;
import com.info2.miniprojet.core.Name;
import com.info2.miniprojet.indexing.CandidateFinder; // Import the interface

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReturnAllCandidateFinder implements CandidateFinder {

    /**
     * Lazy implementation for Search: Returns pairs of the queryName with every name in namesList.
     * Ignores indexStructure.
     */
    @Override
    public List<Couple<Name>> findCandidates(Name queryName, List<Name> namesList, Object indexStructure) {
        System.out.println("DEBUG: ReturnAllCandidateFinder.findCandidates (Search overload) called.");
        if (queryName == null || namesList == null || namesList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Couple<Name>> candidatePairs = new ArrayList<>(namesList.size());
        for (Name candidateName : namesList) {
            // Create a pair of the original query object and the candidate object
            candidatePairs.add(new Couple<>(queryName, candidateName));
        }
        System.out.println("DEBUG: ReturnAllCandidateFinder (Search) returning " + candidatePairs.size() + " pairs.");
        return candidatePairs;
    }

    /**
     * Lazy implementation for Compare: Returns the full Cartesian product of list1 and list2.
     * Ignores indexStructures.
     */
    @Override
    public List<Couple<Name>> findCandidates(List<Name> list1, List<Name> list2, Object indexStructure1, Object indexStructure2) {
        System.out.println("DEBUG: ReturnAllCandidateFinder.findCandidates (Compare overload) called.");
        if (list1 == null || list2 == null || list1.isEmpty() || list2.isEmpty()) {
            return Collections.emptyList();
        }

        List<Couple<Name>> candidatePairs = new ArrayList<>(list1.size() * list2.size());
        for (Name name1 : list1) {
            for (Name name2 : list2) {
                candidatePairs.add(new Couple<>(name1, name2));
            }
        }
        System.out.println("DEBUG: ReturnAllCandidateFinder (Compare) returning " + candidatePairs.size() + " pairs (Cartesian Product).");
        return candidatePairs;
    }

    /**
     * Lazy implementation for Deduplication: Returns all unique pairs (i < j) from the list.
     * Ignores indexStructure.
     */
    @Override
    public List<Couple<Name>> findCandidates(List<Name> list, Object indexStructure) {
        System.out.println("DEBUG: ReturnAllCandidateFinder.findCandidates (Deduplication overload) called.");
        if (list == null || list.size() < 2) { // Need at least 2 names to form a pair
            return Collections.emptyList();
        }

        List<Couple<Name>> candidatePairs = new ArrayList<>();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) { // j starts from i + 1
                candidatePairs.add(new Couple<>(list.get(i), list.get(j)));
            }
        }
        System.out.println("DEBUG: ReturnAllCandidateFinder (Dedupe) returning " + candidatePairs.size() + " pairs.");
        return candidatePairs;
    }

    @Override
    public String getName() {
        // This identifier can be used in the Configuration DTO and StrategyFactory
        return "FIND_ALL";
    }
}
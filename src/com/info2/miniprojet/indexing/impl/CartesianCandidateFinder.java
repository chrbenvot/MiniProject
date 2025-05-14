package com.info2.miniprojet.indexing.impl;

import com.info2.miniprojet.core.Couple;
import com.info2.miniprojet.core.Name;
import com.info2.miniprojet.indexing.CandidateFinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CartesianCandidateFinder implements CandidateFinder {

    // Stores the single list that was "indexed"
    private List<Name> indexedList;
    // Optional: Store a hash or reference to check if the list passed to buildIndex is new
    private List<Name> listRefForCurrentIndex;


    public CartesianCandidateFinder() {
        reset();
    }

    @Override
    public void buildIndex(List<Name> namesToIndex) {
        // For Cartesian, "building an index" means storing the list.
        // We can add a check to see if the list is the same as already "indexed"
        // to avoid recreating the list copy if not necessary, though for Cartesian it's minor.
        if (this.listRefForCurrentIndex == namesToIndex && this.indexedList != null) {
            System.out.println("DEBUG: CartesianCandidateFinder.buildIndex called with the same list. Index reused.");
            return;
        }

        System.out.println("DEBUG: CartesianCandidateFinder.buildIndex called. Storing new list reference.");
        this.indexedList = (namesToIndex == null) ? new ArrayList<>() : new ArrayList<>(namesToIndex); // Store a copy
        this.listRefForCurrentIndex = namesToIndex; // Store reference to the original list used for indexing
    }

    @Override
    public List<Couple<Name>> findCandidatesForSearch(Name queryName, List<Name> originalNamesList) {
        if (queryName == null || this.indexedList == null || this.indexedList.isEmpty()) {
            if (this.indexedList == null) System.err.println("Search Error: Index not built. Call buildIndex() first.");
            return Collections.emptyList();
        }
        // Assertion: originalNamesList should be the same as this.listRefForCurrentIndex
        if (originalNamesList != this.listRefForCurrentIndex) {
            System.err.println("Warning (Search): originalNamesList differs from indexed list. Results might be inconsistent.");
            // For Cartesian, we use this.indexedList which is a copy of what was passed to buildIndex.
        }

        List<Couple<Name>> candidatePairs = new ArrayList<>(this.indexedList.size());
        for (Name candidateName : this.indexedList) {
            candidatePairs.add(new Couple<>(queryName, candidateName));
        }
        System.out.println("DEBUG: CartesianCandidateFinder (Search) returning " + candidatePairs.size() + " pairs.");
        return candidatePairs;
    }

    @Override
    public List<Couple<Name>> findCandidatesForComparison(List<Name> listToIterate, List<Name> indexedOriginalList) {
        // Assumes 'indexedOriginalList' IS THE LIST that 'buildIndex' was called with.
        // 'listToIterate' is the "other" list.
        if (listToIterate == null || this.indexedList == null || listToIterate.isEmpty() || this.indexedList.isEmpty()) {
            if (this.indexedList == null) System.err.println("Compare Error: Index not built on the second list. Call buildIndex() first.");
            return Collections.emptyList();
        }
        // Assertion: indexedOriginalList should be the same as this.listRefForCurrentIndex
        if (indexedOriginalList != this.listRefForCurrentIndex) {
            System.err.println("Warning (Compare): indexedOriginalList differs from the list this finder indexed. Results might be inconsistent.");
            // For Cartesian, it will still produce a result by using this.indexedList.
        }

        List<Couple<Name>> candidatePairs = new ArrayList<>(listToIterate.size() * this.indexedList.size());
        for (Name nameFromIteratedList : listToIterate) {
            for (Name nameFromIndexedList : this.indexedList) { // Compare against our "indexed" list
                candidatePairs.add(new Couple<>(nameFromIteratedList, nameFromIndexedList));
            }
        }
        System.out.println("DEBUG: CartesianCandidateFinder (Compare) returning " + candidatePairs.size() + " pairs (Cartesian Product).");
        return candidatePairs;
    }

    @Override
    public List<Couple<Name>> findCandidatesForDeduplication(List<Name> originalNamesList) {
        // Assumes originalNamesList is the list that buildIndex was called with
        if (this.indexedList == null || this.indexedList.size() < 2) {
            if (this.indexedList == null) System.err.println("Dedupe Error: Index not built. Call buildIndex() first.");
            return Collections.emptyList();
        }
        // Assertion: originalNamesList should be the same as this.listRefForCurrentIndex
        if (originalNamesList != this.listRefForCurrentIndex) {
            System.err.println("Warning (Dedupe): originalNamesList differs from indexed list. Results might be inconsistent.");
        }

        List<Couple<Name>> candidatePairs = new ArrayList<>();
        int size = this.indexedList.size();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                candidatePairs.add(new Couple<>(this.indexedList.get(i), this.indexedList.get(j)));
            }
        }
        System.out.println("DEBUG: CartesianCandidateFinder (Dedupe) returning " + candidatePairs.size() + " pairs.");
        return candidatePairs;
    }

    @Override
    public String getName() {
        return "CARTESIAN_FIND_ALL";
    }

    @Override
    public void reset() {
        this.indexedList = null;
        this.listRefForCurrentIndex = null;
    }
}
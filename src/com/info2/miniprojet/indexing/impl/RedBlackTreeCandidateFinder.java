package com.info2.miniprojet.indexing.impl;

import com.info2.miniprojet.core.Couple;
import com.info2.miniprojet.core.Name;
import com.info2.miniprojet.indexing.CandidateFinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap; // Red-Black Tree implementation

public class RedBlackTreeCandidateFinder implements CandidateFinder {

    private TreeMap<String, List<Integer>> indexMap; // Key: Processed name string, Value: list of original indices
    private List<Name> indexedListReference;

    public RedBlackTreeCandidateFinder() {
        reset();
    }

    // Helper to get the key for the TreeMap
    private String getIndexKeyFromName(Name name) {
        if (name == null || name.processedTokens() == null || name.processedTokens().isEmpty()) {
            return null;
        }
        // Join all processed tokens and lowercase to form the key
        return String.join(" ", name.processedTokens()).toLowerCase();
    }

    @Override
    public void buildIndex(List<Name> namesToIndex) {
        if (namesToIndex == null || namesToIndex.isEmpty()) {
            System.out.println("DEBUG: RedBlackTreeCandidateFinder.buildIndex called with empty or null list. Resetting.");
            reset();
            return;
        }

        if (this.indexedListReference != null && this.indexedListReference == namesToIndex && this.indexMap != null && !this.indexMap.isEmpty()) {
            System.out.println("DEBUG: RedBlackTreeCandidateFinder.buildIndex called with the same list instance. Index reused.");
            return;
        }

        System.out.println("DEBUG: RedBlackTreeCandidateFinder.buildIndex: New or different list. Building TreeMap index for " + namesToIndex.size() + " names.");
        reset();
        this.indexedListReference = namesToIndex;
        this.indexMap = new TreeMap<>(); // Initialize the TreeMap

        for (int i = 0; i < namesToIndex.size(); i++) {
            Name name = namesToIndex.get(i);
            String key = getIndexKeyFromName(name);
            if (key != null) {
                this.indexMap.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
            }
        }
        System.out.println("DEBUG: RedBlackTreeCandidateFinder.buildIndex complete. Index size: " + this.indexMap.size() + " unique keys.");
    }

    @Override
    public List<Couple<Name>> findCandidatesForSearch(Name queryName, List<Name> originalNamesList) {
        if (queryName == null || this.indexMap == null || this.indexMap.isEmpty() || this.indexedListReference == null) {
            if (this.indexMap == null) System.err.println("Search Error (RBTree): Index not built. Call buildIndex() first.");
            return Collections.emptyList();
        }
        if (originalNamesList != this.indexedListReference) {
            System.err.println("Warning (Search - RBTree): originalNamesList differs from indexed list. Using internally stored list ref.");
        }

        List<Couple<Name>> candidatePairs = new ArrayList<>();
        String queryKey = getIndexKeyFromName(queryName);

        if (queryKey != null) {
            List<Integer> matchingIndices = this.indexMap.get(queryKey); // O(log N) lookup
            if (matchingIndices != null) {
                System.out.println("DEBUG: RBTree Search: Found " + matchingIndices.size() + " candidates for key '" + queryKey + "'.");
                for (int index : matchingIndices) {
                    if (index >= 0 && index < this.indexedListReference.size()) {
                        candidatePairs.add(new Couple<>(queryName, this.indexedListReference.get(index)));
                    }
                }
            } else {
                System.out.println("DEBUG: RBTree Search: No candidates found for key '" + queryKey + "'.");
            }
        } else {
            System.out.println("DEBUG: RBTree Search: Query key is null.");
        }
        return candidatePairs;
    }

    @Override
    public List<Couple<Name>> findCandidatesForComparison(List<Name> listToIterate, List<Name> indexedOriginalList) {
        if (listToIterate == null || this.indexMap == null || this.indexMap.isEmpty() || this.indexedListReference == null) {
            if (this.indexMap == null) System.err.println("Compare Error (RBTree): Index not built (for indexedOriginalList). Call buildIndex() first.");
            return Collections.emptyList();
        }
        if (indexedOriginalList != this.indexedListReference) {
            System.err.println("Warning (Compare - RBTree): indexedOriginalList differs from the list this finder indexed. Using internally stored list ref.");
        }

        List<Couple<Name>> candidatePairs = new ArrayList<>();
        for (Name nameFromIteratedList : listToIterate) {
            String key = getIndexKeyFromName(nameFromIteratedList);
            if (key != null) {
                List<Integer> matchingIndices = this.indexMap.get(key); // O(log N) lookup in index built from indexedOriginalList
                if (matchingIndices != null) {
                    for (int index : matchingIndices) {
                        if (index >= 0 && index < this.indexedListReference.size()) {
                            candidatePairs.add(new Couple<>(nameFromIteratedList, this.indexedListReference.get(index)));
                        }
                    }
                }
            }
        }
        System.out.println("DEBUG: RBTree Compare: Generated " + candidatePairs.size() + " candidate pairs.");
        return candidatePairs;
    }

    @Override
    public List<Couple<Name>> findCandidatesForDeduplication(List<Name> originalNamesList) {
        if (this.indexMap == null || this.indexMap.isEmpty() || this.indexedListReference == null) {
            if (this.indexMap == null) System.err.println("Dedupe Error (RBTree): Index not built. Call buildIndex() first.");
            return Collections.emptyList();
        }
        if (originalNamesList != this.indexedListReference) {
            System.err.println("Warning (Dedupe - RBTree): originalNamesList differs from indexed list. Using internally stored list ref.");
        }

        List<Couple<Name>> candidatePairs = new ArrayList<>();
        // Iterate through the index. For any key that has multiple names (list of indices > 1), form pairs.
        for (Map.Entry<String, List<Integer>> entry : this.indexMap.entrySet()) {
            List<Integer> indicesWithSameKey = entry.getValue();
            if (indicesWithSameKey != null && indicesWithSameKey.size() > 1) {
                for (int i = 0; i < indicesWithSameKey.size(); i++) {
                    for (int j = i + 1; j < indicesWithSameKey.size(); j++) {
                        int index1 = indicesWithSameKey.get(i);
                        int index2 = indicesWithSameKey.get(j);

                        if (index1 >= 0 && index1 < this.indexedListReference.size() &&
                                index2 >= 0 && index2 < this.indexedListReference.size()) {
                            candidatePairs.add(new Couple<>(this.indexedListReference.get(index1), this.indexedListReference.get(index2)));
                        }
                    }
                }
            }
        }
        System.out.println("DEBUG: RBTree Dedupe: Generated " + candidatePairs.size() + " candidate pairs.");
        return candidatePairs;
    }

    @Override
    public String getName() {
        return "REDBLACKTREE_FINDER";
    }

    @Override
    public void reset() {
        this.indexMap = new TreeMap<>(); // Initialize new empty TreeMap
        this.indexedListReference = null;
    }
}
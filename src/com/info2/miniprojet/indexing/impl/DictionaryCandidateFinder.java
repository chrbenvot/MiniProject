package com.info2.miniprojet.indexing.impl;

import com.info2.miniprojet.core.Couple;
import com.info2.miniprojet.core.Name;
import com.info2.miniprojet.indexing.CandidateFinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DictionaryCandidateFinder implements CandidateFinder {

    private Map<String, List<Integer>> lastNameIndex; // Key: last token, Value: list of original indices
    private List<Name> indexedListReference; // Reference to the list that was indexed

    public DictionaryCandidateFinder() {

        reset();
    }

    private String getKeyFromTokens(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return null; // Or a special placeholder for empty/no last name
        }
        // Use the last token as the key
        String lastToken = tokens.get(tokens.size() - 1);
        return (lastToken == null || lastToken.trim().isEmpty()) ? null : lastToken.trim();
    }

    @Override
    public void buildIndex(List<Name> namesToIndex) {
        if (namesToIndex == null || namesToIndex.isEmpty()) {
            System.out.println("DEBUG: DictionaryCandidateFinder.buildIndex called with empty or null list.");
            return;
        }
        if(this.indexedListReference != null && namesToIndex==this.indexedListReference && this.lastNameIndex != null && !this.lastNameIndex.isEmpty()) {
            System.out.println("DEBUG: DictionaryCandidateFinder.buildIndex called with the same list. Index reused.");
            return;
        }
        reset();

        this.indexedListReference = namesToIndex; // Store direct reference
        this.lastNameIndex = new HashMap<>();

        System.out.println("DEBUG: DictionaryCandidateFinder.buildIndex starting for " + namesToIndex.size() + " names.");
        for (int i = 0; i < namesToIndex.size(); i++) {
            Name name = namesToIndex.get(i);
            if (name != null && name.processedTokens() != null) {
                String key = getKeyFromTokens(name.processedTokens());
                if (key != null) {
                    // If the key doesn't exist, create a new list for it
                    // If it exists, add the current index to the existing list
                    this.lastNameIndex.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
                }
            }
        }
        System.out.println("DEBUG: DictionaryCandidateFinder.buildIndex complete. Index size: " + this.lastNameIndex.size() + " unique last names.");
    }

    @Override
    public List<Couple<Name>> findCandidatesForSearch(Name queryName, List<Name> originalNamesList) {
        System.out.println("DEBUG: DictionaryCandidateFinder.findCandidatesForSearch for query: " + queryName.originalName());
        if (queryName == null || this.lastNameIndex == null || this.lastNameIndex.isEmpty()) {
            if (this.lastNameIndex == null) System.err.println("Search Error (Dictionary): Index not built. Call buildIndex() first.");
            return Collections.emptyList();
        }
        // Assertion/Check for consistency
        if (originalNamesList != this.indexedListReference) {
            System.err.println("Warning (Search - Dictionary): originalNamesList differs from indexed list. Using internally stored list reference for candidates.");
            // For safety, this implementation will use its internally stored indexedListReference
            // to fetch candidates, assuming it's the one the index was built upon.
        }


        List<Couple<Name>> candidatePairs = new ArrayList<>();
        String queryKey = getKeyFromTokens(queryName.processedTokens());

        if (queryKey != null) {
            List<Integer> matchingIndices = this.lastNameIndex.get(queryKey);
            if (matchingIndices != null) {
                System.out.println("DEBUG: Dictionary Search: Found " + matchingIndices.size() + " potential candidates for key '" + queryKey + "'.");
                for (int index : matchingIndices) {
                    // Ensure index is valid for the list used to build the index
                    if (index >= 0 && index < this.indexedListReference.size()) {
                        candidatePairs.add(new Couple<>(queryName, this.indexedListReference.get(index)));
                    }
                }
            } else {
                System.out.println("DEBUG: Dictionary Search: No candidates found for key '" + queryKey + "'.");
            }
        } else {
            System.out.println("DEBUG: Dictionary Search: Query key is null, cannot find candidates.");
        }
        return candidatePairs;
    }

    @Override
    public List<Couple<Name>> findCandidatesForComparison(List<Name> listToIterate, List<Name> indexedOriginalList) {
        // Assumes 'indexedOriginalList' is the list that 'buildIndex' was called with (and its index is in this.lastNameIndex).
        // 'listToIterate' is the "other" list whose items will be used as queries.
        System.out.println("DEBUG: DictionaryCandidateFinder.findCandidatesForComparison called.");
        if (listToIterate == null || this.lastNameIndex == null || this.lastNameIndex.isEmpty() ) {
            if (this.lastNameIndex == null) System.err.println("Compare Error (Dictionary): Index not built on the second list. Call buildIndex() first.");
            return Collections.emptyList();
        }
        if (indexedOriginalList != this.indexedListReference) {
            System.err.println("Warning (Compare - Dictionary): indexedOriginalList differs from the list this finder indexed. Using internally stored list reference for candidates.");
        }


        List<Couple<Name>> candidatePairs = new ArrayList<>();
        for (Name nameFromIteratedList : listToIterate) {
            if (nameFromIteratedList != null && nameFromIteratedList.processedTokens() != null) {
                String key = getKeyFromTokens(nameFromIteratedList.processedTokens());
                if (key != null) {
                    List<Integer> matchingIndices = this.lastNameIndex.get(key);
                    if (matchingIndices != null) {
                        for (int index : matchingIndices) {
                            if (index >= 0 && index < this.indexedListReference.size()) {
                                candidatePairs.add(new Couple<>(nameFromIteratedList, this.indexedListReference.get(index)));
                            }
                        }
                    }
                }
            }
        }
        System.out.println("DEBUG: Dictionary Compare: Generated " + candidatePairs.size() + " candidate pairs.");
        return candidatePairs;
    }

    @Override
    public List<Couple<Name>> findCandidatesForDeduplication(List<Name> originalNamesList) {
        System.out.println("DEBUG: DictionaryCandidateFinder.findCandidatesForDeduplication called.");
        if (this.lastNameIndex == null || this.lastNameIndex.isEmpty()) {
            if (this.lastNameIndex == null) System.err.println("Dedupe Error (Dictionary): Index not built. Call buildIndex() first.");
            return Collections.emptyList();
        }
        if (originalNamesList != this.indexedListReference) {
            System.err.println("Warning (Dedupe - Dictionary): originalNamesList differs from indexed list. Using internally stored list reference for candidates.");
        }


        List<Couple<Name>> candidatePairs = new ArrayList<>();
        // Iterate through the index. For any key that has multiple names, form pairs.
        for (List<Integer> indicesWithSameKey : this.lastNameIndex.values()) {
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
        System.out.println("DEBUG: Dictionary Dedupe: Generated " + candidatePairs.size() + " candidate pairs.");
        return candidatePairs;
    }

    @Override
    public String getName() {
        return "DICTIONARY_LAST_TOKEN"; // Or more general "DICTIONARY_FINDER" if keying logic is complex/configurable
    }

    @Override
    public void reset() {
        System.out.println("DEBUG: DictionaryCandidateFinder.reset() called.");
        this.lastNameIndex = null;
        this.indexedListReference = null;
    }
}
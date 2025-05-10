package com.info2.miniprojet.indexing.impl;

import com.info2.miniprojet.core.Couple;
import com.info2.miniprojet.core.Name;
import com.info2.miniprojet.indexing.CandidateFinder;
import com.info2.miniprojet.util.TrieNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrieCandidateFinder implements CandidateFinder {

    private TrieNode root;
    private List<Name> indexedListReference; // Reference to the list used to build the index

    public TrieCandidateFinder() {
        reset();
    }

    // --- Helper method to get the string key for the Trie from a Name object ---
    // For simplicity, we'll join all processed tokens.
    // You could also choose to index only the first token, last token, etc.
    private String getIndexKeyFromName(Name name) {
        if (name == null || name.processedTokens() == null || name.processedTokens().isEmpty()) {
            return null;
        }
        return String.join(" ", name.processedTokens()).toLowerCase(); // Join and lowercase
    }

    // --- Helper method to insert a word and its original index into the Trie ---
    private void insertInTrie(String word, int originalIndex) {
        if (word == null || word.isEmpty()) return;
        TrieNode current = root;
        for (char ch : word.toCharArray()) {
            current = current.children.computeIfAbsent(ch, c -> new TrieNode());
        }
        current.isEndOfWord = true;
        current.originalIndices.add(originalIndex);
    }

    @Override
    public void buildIndex(List<Name> namesToIndex) {
        if (namesToIndex == null || namesToIndex.isEmpty()) {
            System.out.println("DEBUG: TrieCandidateFinder.buildIndex called with empty or null list. Resetting.");
            reset();
            return;
        }

        // Check if the new list is the same instance as the one already indexed.
        if (this.indexedListReference != null && this.indexedListReference == namesToIndex && this.root.children.size() > 0) {
            System.out.println("DEBUG: TrieCandidateFinder.buildIndex called with the same list instance. Index reused.");
            return;
        }

        System.out.println("DEBUG: TrieCandidateFinder.buildIndex: New or different list. Building Trie index for " + namesToIndex.size() + " names.");
        reset(); // Reset for a new index
        this.indexedListReference = namesToIndex;

        for (int i = 0; i < namesToIndex.size(); i++) {
            Name name = namesToIndex.get(i);
            String key = getIndexKeyFromName(name);
            if (key != null) {
                insertInTrie(key, i);
            }
        }
        System.out.println("DEBUG: TrieCandidateFinder.buildIndex complete.");
    }

    // Helper to search the Trie for a prefix/word and get all indices
    private Set<Integer> searchPrefixInTrie(String prefixOrWord) {
        if (prefixOrWord == null || prefixOrWord.isEmpty() || root.children.isEmpty()) {
            return Collections.emptySet();
        }
        TrieNode current = root;
        for (char ch : prefixOrWord.toCharArray()) {
            TrieNode node = current.children.get(ch);
            if (node == null) {
                return Collections.emptySet(); // Prefix not found
            }
            current = node;
        }
        // Now 'current' is the node for the last char of prefixOrWord.
        // We need to collect all indices from this node and its children (if searching for prefixes)
        // or just this node's indices if searching for exact word matches.
        // For candidate generation, collecting from all children (prefix match) is often better.
        return collectAllIndicesFromNode(current);
    }

    // Helper to recursively collect all indices from a node and its descendants
    private Set<Integer> collectAllIndicesFromNode(TrieNode node) {
        Set<Integer> indices = new HashSet<>();
        if (node == null) return indices;

        if (node.isEndOfWord) {
            indices.addAll(node.originalIndices);
        }
        for (TrieNode child : node.children.values()) {
            indices.addAll(collectAllIndicesFromNode(child));
        }
        return indices;
    }


    @Override
    public List<Couple<Name>> findCandidatesForSearch(Name queryName, List<Name> originalNamesList) {
        System.out.println("DEBUG: TrieCandidateFinder.findCandidatesForSearch for query: " + queryName.originalName());
        if (queryName == null || this.root.children.isEmpty() || this.indexedListReference == null) {
            if (this.root.children.isEmpty()) System.err.println("Search Error (Trie): Index not built or empty. Call buildIndex() first.");
            return Collections.emptyList();
        }
        if (originalNamesList != this.indexedListReference) {
            System.err.println("Warning (Search - Trie): originalNamesList differs from indexed list. Results might be inconsistent.");
        }


        List<Couple<Name>> candidatePairs = new ArrayList<>();
        String queryKey = getIndexKeyFromName(queryName);
        Set<Integer> matchingIndices = searchPrefixInTrie(queryKey); // Find matches based on prefix

        System.out.println("DEBUG: Trie Search: Found " + matchingIndices.size() + " potential candidate indices for key '" + queryKey + "'.");
        for (int index : matchingIndices) {
            if (index >= 0 && index < this.indexedListReference.size()) {
                candidatePairs.add(new Couple<>(queryName, this.indexedListReference.get(index)));
            }
        }
        return candidatePairs;
    }

    @Override
    public List<Couple<Name>> findCandidatesForComparison(List<Name> listToIterate, List<Name> indexedOriginalList) {
        // Assumes 'indexedOriginalList' is the list that 'buildIndex' was called with (and its Trie is in this.root).
        // 'listToIterate' is the "other" list whose items will be used as queries.
        System.out.println("DEBUG: TrieCandidateFinder.findCandidatesForComparison called.");
        if (listToIterate == null || this.root.children.isEmpty() || this.indexedListReference == null) {
            if (this.root.children.isEmpty()) System.err.println("Compare Error (Trie): Index not built on the second list. Call buildIndex() first.");
            return Collections.emptyList();
        }
        if (indexedOriginalList != this.indexedListReference) {
            System.err.println("Warning (Compare - Trie): indexedOriginalList differs from the list this finder indexed. Results might be inconsistent.");
        }

        List<Couple<Name>> candidatePairs = new ArrayList<>();
        for (Name nameFromIteratedList : listToIterate) {
            String key = getIndexKeyFromName(nameFromIteratedList);
            Set<Integer> matchingIndices = searchPrefixInTrie(key); // Search the Trie (built from indexedOriginalList)
            for (int index : matchingIndices) {
                if (index >= 0 && index < this.indexedListReference.size()) {
                    candidatePairs.add(new Couple<>(nameFromIteratedList, this.indexedListReference.get(index)));
                }
            }
        }
        System.out.println("DEBUG: Trie Compare: Generated " + candidatePairs.size() + " candidate pairs.");
        return candidatePairs;
    }

    @Override
    public List<Couple<Name>> findCandidatesForDeduplication(List<Name> originalNamesList) {
        System.out.println("DEBUG: TrieCandidateFinder.findCandidatesForDeduplication called.");
        if (this.root.children.isEmpty() || this.indexedListReference == null) {
            if (this.root.children.isEmpty()) System.err.println("Dedupe Error (Trie): Index not built. Call buildIndex() first.");
            return Collections.emptyList();
        }
        if (originalNamesList != this.indexedListReference) {
            System.err.println("Warning (Dedupe - Trie): originalNamesList differs from indexed list. Results might be inconsistent.");
        }

        List<Couple<Name>> candidatePairs = new ArrayList<>();
        // To find duplicates using a Trie: iterate all words in the Trie.
        // If a word (TrieNode marked as isEndOfWord) has multiple originalIndices,
        // then those original names are candidates for being duplicates (as they map to the same Trie path).
        collectDuplicateCandidatesFromTrie(this.root, candidatePairs, this.indexedListReference);

        System.out.println("DEBUG: Trie Dedupe: Generated " + candidatePairs.size() + " candidate pairs.");
        return candidatePairs;
    }

    // Recursive helper for deduplication
    private void collectDuplicateCandidatesFromTrie(TrieNode node, List<Couple<Name>> pairs, List<Name> originalList) {
        if (node == null) return;

        if (node.isEndOfWord && node.originalIndices.size() > 1) {
            // This word is shared by multiple original names. Form pairs.
            List<Integer> indices = new ArrayList<>(node.originalIndices); // Convert set to list for indexed access
            for (int i = 0; i < indices.size(); i++) {
                for (int j = i + 1; j < indices.size(); j++) {
                    int index1 = indices.get(i);
                    int index2 = indices.get(j);
                    if (index1 >= 0 && index1 < originalList.size() &&
                            index2 >= 0 && index2 < originalList.size()) {
                        pairs.add(new Couple<>(originalList.get(index1), originalList.get(index2)));
                    }
                }
            }
        }

        for (TrieNode child : node.children.values()) {
            collectDuplicateCandidatesFromTrie(child, pairs, originalList);
        }
    }


    @Override
    public String getName() {
        return "TRIE_FINDER";
    }

    @Override
    public void reset() {
        System.out.println("DEBUG: TrieCandidateFinder.reset() called.");
        this.root = new TrieNode(); // Create a new empty Trie root
        this.indexedListReference = null;
    }
}
package com.info2.miniprojet.util;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class TrieNode {
    public Map<Character, TrieNode> children;
    public Set<Integer> originalIndices; // Store indices of Names ending at this node
    public boolean isEndOfWord;

    public TrieNode() {
        children = new HashMap<>();
        originalIndices = new HashSet<>(); // Use Set to avoid duplicate indices for same word
        isEndOfWord = false;
    }
}

//A lot of these are public when they shouldn't be ideally,too lazy to refactor code cuz i originally had this in
// indexing.impl and then moved it to util because it's not really an implementation
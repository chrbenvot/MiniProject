package com.info2.miniprojet.preprocessing.impl;

import com.info2.miniprojet.preprocessing.Preprocessor;

import java.util.List;
import java.util.ArrayList; // For returning a new list

//TODO: actually implement
public class AccentRemover implements Preprocessor {

    @Override
    public List<String> preprocess(List<String> inputTokens) {
        System.out.println("DEBUG: AccentRemover.preprocess called (current: no-op). Input: " + inputTokens);
        if (inputTokens == null) {
            return new ArrayList<>(); // Return empty list for null input
        }
        // For the "do-nothing" skeleton, just return a copy of the input list.
        // A real implementation would iterate and remove accents from each token.
        return new ArrayList<>(inputTokens);
    }

    @Override
    public String getName() {
        return "ACCENT_REMOVER"; // Matches the key used in StrategyFactory
    }
}
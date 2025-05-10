package com.info2.miniprojet.preprocessing.impl;

import com.info2.miniprojet.preprocessing.Preprocessor;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors; // For a more concise way to process

public class LowercaseNormalizer implements Preprocessor {

    @Override
    public List<String> preprocess(List<String> inputTokens) {
        if (inputTokens == null) {
            // System.out.println("DEBUG: LowercaseNormalizer received null input, returning empty list.");
            return new ArrayList<>();
        }

        // System.out.println("DEBUG: LowercaseNormalizer.preprocess called. Input: " + inputTokens);
        List<String> lowercasedTokens = new ArrayList<>(inputTokens.size());
        for (String token : inputTokens) {
            if (token != null) {
                lowercasedTokens.add(token.toLowerCase());
            } else {
                continue; // Or skip, or add empty string depending on desired handling
            }
        }
        // System.out.println("DEBUG: LowercaseNormalizer output: " + lowercasedTokens);
        return lowercasedTokens;

        // Alternative using streams (more concise but same effect):
        // return inputTokens.stream()
        //                   .map(token -> token == null ? null : token.toLowerCase())
        //                   .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "LOWERCASE"; // Matches the key used in StrategyFactory
    }
}
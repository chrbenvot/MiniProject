package com.info2.miniprojet.preprocessing.impl;

import com.info2.miniprojet.preprocessing.Preprocessor;
import java.text.Normalizer; // For Unicode normalization
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern; // For pattern compilation (efficiency)
import java.util.stream.Collectors;

public class AccentRemover implements Preprocessor {

    // Pre-compile the pattern for efficiency if this method is called frequently
    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    @Override
    public List<String> preprocess(List<String> inputTokens) {
        if (inputTokens == null) {
            // System.out.println("DEBUG: AccentRemover received null input, returning empty list.");
            return new ArrayList<>();
        }

        // System.out.println("DEBUG: AccentRemover.preprocess called. Input: " + inputTokens);
        List<String> tokensWithoutAccents = new ArrayList<>(inputTokens.size());
        for (String token : inputTokens) {
            if (token != null && !token.isEmpty()) {
                // 1. Normalize to NFD (Canonical Decomposition)
                //    This separates base characters from their diacritical marks.
                //    For example, "é" becomes "e" + "´" (combining acute accent).
                String normalized = Normalizer.normalize(token, Normalizer.Form.NFD);

                // 2. Remove the diacritical marks using a regular expression.
                //    \\p{InCombiningDiacriticalMarks} is a Unicode property that matches combining marks.
                String accentRemoved = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
                tokensWithoutAccents.add(accentRemoved);
            } else if (token != null) { // Handle empty strings
                tokensWithoutAccents.add("");
            } else {
                tokensWithoutAccents.add(null); // Preserve nulls if present
            }
        }
        // System.out.println("DEBUG: AccentRemover output: " + tokensWithoutAccents);
        return tokensWithoutAccents;

        // Alternative using streams:
        // return inputTokens.stream()
        //                   .map(token -> {
        //                       if (token == null || token.isEmpty()) return token; // or handle differently
        //                       String normalized = Normalizer.normalize(token, Normalizer.Form.NFD);
        //                       return DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
        //                   })
        //                   .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "ACCENT_REMOVER"; // Matches the key used in StrategyFactory
    }
}
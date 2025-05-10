package com.info2.miniprojet.preprocessing.impl;

import com.info2.miniprojet.preprocessing.Preprocessor;

import java.util.List;
import java.util.ArrayList; // For returning a new list

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class AccentRemover implements Preprocessor {

    @Override
    public List<String> preprocess(List<String> inputTokens) {
        if (inputTokens == null) {
            return new ArrayList<>();
        }
        
        List<String> processedTokens = new ArrayList<>(inputTokens.size());
        for (String token : inputTokens) {
            processedTokens.add(removeAccents(token));
        }
        return processedTokens;
    }

    private String removeAccents(String input) {
        if (input == null) {
            return "";
        }
        
        // Normalize to decomposed form (separate characters and diacritical marks)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        // Remove diacritical marks (Unicode characters between \u0300 and \u036F)
        String accentRemoved = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        // Return in composed form for better compatibility
        return Normalizer.normalize(accentRemoved, Normalizer.Form.NFC);
    }

    @Override
    public String getName() {
        return "ACCENT_REMOVER";
    }
}
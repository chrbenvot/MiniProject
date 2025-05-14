package com.info2.miniprojet.preprocessing.impl;

import com.info2.miniprojet.preprocessing.Preprocessor;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors; // For a more concise way to process

public class LowercaseNormalizer implements Preprocessor {

    @Override
    public List<String> preprocess(List<String> inputTokens) {
        if (inputTokens == null) {
            return new ArrayList<>();
        }

        List<String> lowercasedTokens = new ArrayList<>(inputTokens.size());
        for (String token : inputTokens) {
            if (token != null) {
                lowercasedTokens.add(token.toLowerCase());
            } else {
                continue; // Or add empty string depending on desired handling
            }
        }
        return lowercasedTokens;
    }

    @Override
    public String getName() {
        return "LOWERCASE";
    }
}
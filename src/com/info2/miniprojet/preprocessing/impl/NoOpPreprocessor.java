package com.info2.miniprojet.preprocessing.impl;

import com.info2.miniprojet.preprocessing.Preprocessor;
import java.util.List;
import java.util.ArrayList;

public class NoOpPreprocessor implements Preprocessor {

    @Override
    public List<String> preprocess(List<String> inputTokens) {
        if(inputTokens == null) {
            return new ArrayList<>(); // Return an empty list if input is null
        }
        return new ArrayList<>(inputTokens);
    }

    @Override
    public String getName() {
        return "NOOP"; // Matches default config choice
    }
}
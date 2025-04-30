package com.info2.miniprojet.preprocessing.impl;

import com.info2.miniprojet.preprocessing.Preprocessor;
import java.util.List;
import java.util.ArrayList;

public class NoOpPreprocessor implements Preprocessor {

    @Override
    public List<String> preprocess(List<String> inputTokens) {
        // Does nothing
        return new ArrayList<>(inputTokens);
    }

    @Override
    public String getName() {
        return "NOOP"; // Matches default config choice
    }
}
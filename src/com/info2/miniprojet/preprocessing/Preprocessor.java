package com.info2.miniprojet.preprocessing;

import java.util.List;

public interface Preprocessor {
    /**
     * Processes a list of tokens representing a name.
     * The first step might be tokenizing the raw string before calling this.
     * @param inputTokens The input list of tokens (potentially just one item if raw string is wrapped).
     * @return The processed list of tokens.
     */
    List<String> preprocess(List<String> inputTokens);

    /**
     * Gets a user-friendly name for this strategy.
     * @return The name identifier.
     */
    String getName();
}
package com.info2.miniprojet.comparison;

import java.util.List;

public interface NameComparator {
    /**
     * Calculates the similarity or distance score between two names represented as lists of processed tokens.
     * @param processedTokens1 Tokens for the first name.
     * @param processedTokens2 Tokens for the second name.
     * @return The calculated score.
     */
    double calculateScore(List<String> processedTokens1, List<String> processedTokens2);

    /**
     * Indicates if the score represents distance (lower is better) or similarity (higher is better).
     * @return true if the score is a distance, false if it's a similarity.
     */
    boolean isScoreDistance();

    /**
     * Gets a user-friendly name for this strategy.
     * @return The name identifier.
     */
    String getName();
}
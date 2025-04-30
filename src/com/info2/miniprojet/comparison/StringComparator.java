package com.info2.miniprojet.comparison;

public interface StringComparator {
    /**
     * Calculates the similarity or distance score between two strings.
     * @param string1 The first string.
     * @param string2 The second string.
     * @return The calculated score.
     */
    double calculateScore(String string1, String string2);

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
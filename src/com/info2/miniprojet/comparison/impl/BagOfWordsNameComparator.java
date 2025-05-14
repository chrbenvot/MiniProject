package com.info2.miniprojet.comparison.impl;

import com.info2.miniprojet.comparison.NameComparator;
import com.info2.miniprojet.comparison.StringComparator;
import com.info2.miniprojet.core.Name;

import java.util.List;
import java.util.ArrayList;


public class BagOfWordsNameComparator implements NameComparator {
    private final StringComparator stringComparator;
    private final double matchThreshold; // Threshold to consider a token "matched"

    public BagOfWordsNameComparator(StringComparator stringComparator, double tokenMatchThreshold) {
        this.stringComparator = stringComparator;
        this.matchThreshold = tokenMatchThreshold;
    }

    @Override
    public double calculateScore(Name name1, Name name2) {
        if (name1 == null || name2 == null) return 0.0;

        List<String> tokens1 = name1.processedTokens();
        List<String> tokens2 = name2.processedTokens();

        if (tokens1 == null || tokens1.isEmpty() || tokens2 == null || tokens2.isEmpty()) {
            return (tokens1 == null || tokens1.isEmpty()) && (tokens2 == null || tokens2.isEmpty()) ? 1.0 : 0.0;
        }

        // Use copies to mark matched tokens (simple approach)
        List<String> t1Copy = new ArrayList<>(tokens1);
        List<String> t2Copy = new ArrayList<>(tokens2);
        int matches = 0;

        for (int i = 0; i < t1Copy.size(); i++) {
            String token1 = t1Copy.get(i);
            if (token1 == null) continue; // Already matched or was null

            double bestScoreForToken1 = stringComparator.isScoreDistance() ? Double.MAX_VALUE : -1.0;
            int bestMatchIndexInT2 = -1;

            for (int j = 0; j < t2Copy.size(); j++) {
                String token2 = t2Copy.get(j);
                if (token2 == null) continue; // Already matched or was null

                double currentScore = stringComparator.calculateScore(token1, token2);
                if (stringComparator.isScoreDistance()) {
                    if (currentScore < bestScoreForToken1) {
                        bestScoreForToken1 = currentScore;
                        bestMatchIndexInT2 = j;
                    }
                } else { // Similarity
                    if (currentScore > bestScoreForToken1) {
                        bestScoreForToken1 = currentScore;
                        bestMatchIndexInT2 = j;
                    }
                }
            }

            // Check if the best match meets the threshold
            boolean thresholdMet;
            if (stringComparator.isScoreDistance()) {
                // Normalize distance to similarity for threshold check, or use distance threshold
                int maxLength = Math.max(token1.length(), (bestMatchIndexInT2 != -1 ? t2Copy.get(bestMatchIndexInT2).length() : 0));
                double similarity = maxLength == 0 ? 1.0 : Math.max(0, 1.0 - (bestScoreForToken1 / maxLength));
                thresholdMet = similarity >= matchThreshold;
            } else {
                thresholdMet = bestScoreForToken1 >= matchThreshold;
            }

            if (bestMatchIndexInT2 != -1 && thresholdMet) {
                matches++;
                t1Copy.set(i, null); // Mark as used
                t2Copy.set(bestMatchIndexInT2, null); // Mark as used
            }
        }

        // Simple score: proportion of matched tokens (Jaccard-like on tokens)
        if (tokens1.size() + tokens2.size() == 0) return 1.0; // Both empty
        return (2.0 * matches) / (tokens1.size() + tokens2.size());
    }

    @Override
    public boolean isScoreDistance() {
        return false; // Produces a similarity score (proportion of matches)
    }

    @Override
    public String getName() {
        return "BAG_OF_WORDS";
    }
}
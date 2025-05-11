package com.info2.miniprojet.comparison.impl;

import com.info2.miniprojet.comparison.NameComparator;
import com.info2.miniprojet.core.Name;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class JaccardTokenNameComparator implements NameComparator {

    public JaccardTokenNameComparator() {
        // No StringComparator needed
    }

    @Override
    public double calculateScore(Name name1, Name name2) {
        if (name1 == null || name2 == null) return 0.0;

        List<String> tokens1 = name1.processedTokens();
        List<String> tokens2 = name2.processedTokens();

        if (tokens1 == null || tokens2 == null) {
            return (tokens1 == null && tokens2 == null) ? 1.0 : 0.0; // Both null/empty vs one null/empty
        }
        if (tokens1.isEmpty() && tokens2.isEmpty()) {
            return 1.0; // Two empty token lists are perfectly similar
        }
        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0.0; // One empty, one not means no similarity by Jaccard
        }


        Set<String> set1 = new HashSet<>(tokens1);
        Set<String> set2 = new HashSet<>(tokens2);

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) {
            return 1.0; // If both original lists were effectively empty leading to empty union (e.g. list of empty strings)
        }

        return (double) intersection.size() / union.size();
    }

    @Override
    public boolean isScoreDistance() {
        return false; // Jaccard index is a similarity measure (0 to 1)
    }

    @Override
    public String getName() {
        return "JACCARD_TOKEN_SET";
    }
}
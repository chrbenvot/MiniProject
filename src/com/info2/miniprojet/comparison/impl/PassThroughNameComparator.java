package com.info2.miniprojet.comparison.impl;

import com.info2.miniprojet.comparison.NameComparator;
import com.info2.miniprojet.comparison.StringComparator;

import java.util.List;

public class PassThroughNameComparator implements NameComparator {

    private final StringComparator internalStringComparator;

    // Constructor requires a StringComparator to be injected
    public PassThroughNameComparator(StringComparator stringComparator) {
        if (stringComparator == null) {
            throw new IllegalArgumentException("Internal StringComparator cannot be null");
        }
        this.internalStringComparator = stringComparator;
    }

    @Override
    public double calculateScore(List<String> processedTokens1, List<String> processedTokens2) {
        // Simple lazy implementation: join tokens back into strings and compare.
        String joined1 = String.join(" ", processedTokens1).trim();
        String joined2 = String.join(" ", processedTokens2).trim();

        return internalStringComparator.calculateScore(joined1, joined2);
    }

    @Override
    public boolean isScoreDistance() {
        return internalStringComparator.isScoreDistance();
    }

    @Override
    public String getName() {
        return "PASS_THROUGH_NAME";
    }
}
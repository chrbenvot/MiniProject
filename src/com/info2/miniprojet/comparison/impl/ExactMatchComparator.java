package com.info2.miniprojet.comparison.impl;

import com.info2.miniprojet.comparison.StringComparator;
import java.util.Objects;

public class ExactMatchComparator implements StringComparator {

    @Override
    public double calculateScore(String string1, String string2) {
        return Objects.equals(string1, string2) ? 1.0 : 0.0;
    }

    @Override
    public boolean isScoreDistance() {
        // Is a similarity score,not a distance
        return false;
    }

    @Override
    public String getName() {
        return "EXACT_STRING"; // Simple identifier
    }
}
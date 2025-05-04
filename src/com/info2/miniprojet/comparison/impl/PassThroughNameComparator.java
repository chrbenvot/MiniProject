package com.info2.miniprojet.comparison.impl;

import com.info2.miniprojet.comparison.NameComparator;
import com.info2.miniprojet.comparison.StringComparator;
import com.info2.miniprojet.core.Name;

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
    public double calculateScore(Name name1, Name name2 ) {
        // Simple lazy implementation: join tokens back into strings and compare.
        return internalStringComparator.calculateScore(name1.orignalName(), name2.orignalName());
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
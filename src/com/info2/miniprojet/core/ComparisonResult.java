package com.info2.miniprojet.core;

/**
 * @param measureType e.g., name of the NameComparator used
 */
public record ComparisonResult(String name1, String name2, double score, String measureType) {         //name

    @Override
    public String toString() { //may not need the first name when using the search service!!
        // Simple representation for display
        return String.format("Match: ('%s', '%s'), Score: %.4f (%s)",
                name1, name2, score, measureType);
    }
}
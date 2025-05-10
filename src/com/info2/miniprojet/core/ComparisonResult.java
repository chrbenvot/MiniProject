package com.info2.miniprojet.core;

/**
 * @param measureType e.g., name of the NameComparator used
 */
public record ComparisonResult(String name1, String name2, double score, String measureType) implements Comparable<ComparisonResult> {         //name

    @Override
    public String toString() { //may not need the first name when using the search service!!
        // Simple representation for display
        return String.format("Match: ('%s', '%s'), Score: %.4f (%s)",
                name1, name2, score, measureType);
    }
    @Override
    public int compareTo(ComparisonResult o) {
        return Double.compare(o.score, score);
    }
}
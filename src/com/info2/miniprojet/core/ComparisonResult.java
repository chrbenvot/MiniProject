package com.info2.miniprojet.core;

public class ComparisonResult {         //name
    private final String name1;
    private final String name2;
    private final double score;
    private final String measureType; // e.g., name of the NameComparator used

    public ComparisonResult(String name1, String name2, double score, String measureType) {
        this.name1 = name1;
        this.name2 = name2;
        this.score = score;
        this.measureType = measureType;
    }

    public String getName1() {
        return name1;
    }

    public String getName2() {
        return name2;
    }

    public double getScore() {
        return score;
    }

    public String getMeasureType() {
        return measureType;
    }

    @Override
    public String toString() { //may not need the first name when using the search service!!
        // Simple representation for display
        return String.format("Match: ('%s', '%s'), Score: %.4f (%s)",
                name1, name2, score, measureType);
    }
}
package com.info2.miniprojet.core;

/**
 * @param measureType e.g., name of the NameComparator used
 */
public record ComparisonResult(String id1,String name1,String id2, String name2, double score, String measureType) implements Comparable<ComparisonResult> {         //name

    @Override
    public String toString() { //may not need the first name when using the search service!!
        // Simple representation for display
        String name1Display;
        if (id1 != null && id1.startsWith("QUERY_")) { // Special handling for query name
            name1Display = "'" + name1 + "' (Query)";
        } else if (id1 != null && !id1.isBlank() && !id1.startsWith("L_")) { // Display only "real" IDs
            name1Display = id1 + ":'" + name1 + "'";
        } else {
            name1Display = "'" + name1 + "'"; // Just the name if ID is null, blank, or generated line number
        }

        String name2Display;
        if (id2 != null && !id2.isBlank() && !id2.startsWith("L_")) { // Display only "real" IDs
            name2Display = id2 + ":'" + name2 + "'";
        } else {
            name2Display = "'" + name2 + "'"; // Just the name if ID is null, blank, or generated line number
        }

        return String.format("Match: (%s, %s), Score: %.4f (%s)",
                name1Display, name2Display, score, measureType);
    }
    @Override
    public int compareTo(ComparisonResult o) {
        return Double.compare(o.score, score);
    }
}
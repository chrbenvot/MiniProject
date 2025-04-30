package com.info2.miniprojet.indexing;

import java.util.List;

public interface CandidateFinder {
    /**
     * Finds potential candidate indices from the original list based on the query and a pre-built index.
     * @param processedQueryTokens The processed tokens of the query name.
     * @param indexStructure The opaque index object created by a compatible IndexBuilder.
     * @return A list of integer indices corresponding to potential matches in the original list.
     */
    List<Integer> findCandidates(List<String> processedQueryTokens, Object indexStructure); // IS INDEX NECESSARY??,also is it always better
                                                                                            // to only give indexes instead of list of names
    /**
     * Gets a user-friendly name for this strategy.
     * @return The name identifier.
     */
    String getName();
}
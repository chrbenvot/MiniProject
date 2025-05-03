package com.info2.miniprojet.indexing;

import com.info2.miniprojet.core.Couple;
import com.info2.miniprojet.core.Name;

import java.util.List;

public interface CandidateFinder {
    /**
     * Finds potential candidate indices from the original list based on the query and a pre-built index.
     * @param queryName the query name.
     * @param namesList the original list of names.
     * @param indexStructure The opaque index object created by a compatible IndexBuilder.
     * @return A list of integer indices corresponding to potential matches in the original list.
     */
    List<Couple<Name>> findCandidates(Name queryName, List<Name> namesList, Object indexStructure); // IS INDEX NECESSARY??,null check?

    /**
     * Gets a user-friendly name for this strategy.
     * @return The name identifier.
     */
    String getName();
}
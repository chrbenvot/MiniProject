package com.info2.miniprojet.indexing;

import com.info2.miniprojet.core.Couple;
import com.info2.miniprojet.core.Name;
import java.util.List;

public interface CandidateFinder {

    /**
     * Finds candidate pairs for a search query against a list.
     * Output pairs: (queryName, candidateName)
     */
    List<Couple<Name>> findCandidates(Name queryName, List<Name> namesList, Object indexStructure);

    /**
     * Generates candidate pairs for comparing two lists.
     * Output pairs: (nameFromList1, nameFromList2)
     */
    List<Couple<Name>> findCandidates(List<Name> list1, List<Name> list2, Object indexStructure1, Object indexStructure2);

    /**
     * Generates candidate pairs for deduplicating a single list.
     * Output pairs: (name_i, name_j) where i < j
     */
    List<Couple<Name>> findCandidates(List<Name> list, Object indexStructure);

    /**
     * Gets a user-friendly name for this strategy.
     */
    String getName();
}
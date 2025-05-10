package com.info2.miniprojet.indexing; // Or your chosen package for indexing interfaces

import com.info2.miniprojet.core.Couple;
import com.info2.miniprojet.core.Name;

import java.util.List;

public interface CandidateFinder {

    /**
     * Builds and stores an internal index for the given list of names.
     * This method should be "smart" enough to potentially avoid re-indexing
     * if called multiple times with the exact same list instance or equivalent content
     * (implementation dependent: e.g., by storing a reference to the indexed list
     * or a hash of its content).
     *
     * @param namesToIndex The list of Name objects to build an index from.
     */
    void buildIndex(List<Name> namesToIndex);

    /**
     * Finds candidate pairs for a search query against the internally indexed list.
     * Requires buildIndex to have been called first with the relevant namesList.
     *
     * @param queryName The Name object representing the search query (already preprocessed).
     * @param originalNamesList The original list of names from which the index was built.
     *                          This is needed to construct the full Name objects for the pairs.
     * @return A list of Couple<Name> where the first element is the queryName
     *         and the second is a candidate Name from originalNamesList.
     * @throws IllegalStateException if buildIndex has not been called or if the provided
     *         originalNamesList does not match the list used for building the current index.
     */
    List<Couple<Name>> findCandidatesForSearch(Name queryName, List<Name> originalNamesList);

    /**
     * Generates candidate pairs by comparing items from listToIterate against
     * the internally indexed list (which was set by the last call to buildIndex).
     * Requires buildIndex to have been called first with the list that will serve as the "indexed side".
     *
     * @param listToIterate The list of names to use as "queries" against the indexed list.
     * @param indexedOriginalList The original list of names from which the internal index was built.
     *                            This is the list that items from listToIterate are compared against.
     * @return A list of Couple<Name> where the first element is a Name from listToIterate
     *         and the second is a candidate Name from indexedOriginalList.
     * @throws IllegalStateException if buildIndex has not been called or if the provided
     *         indexedOriginalList does not match the list used for building the current index.
     */
    List<Couple<Name>> findCandidatesForComparison(List<Name> listToIterate, List<Name> indexedOriginalList);

    /**
     * Generates candidate pairs for deduplicating the internally indexed list.
     * Requires buildIndex to have been called first with the list to be deduplicated.
     *
     * @param originalNamesList The original list of names from which the index was built
     *                          and which is being deduplicated.
     * @return A list of Couple<Name> where both elements are from originalNamesList (name_i, name_j with i < j).
     * @throws IllegalStateException if buildIndex has not been called or if the provided
     *         originalNamesList does not match the list used for building the current index.
     */
    List<Couple<Name>> findCandidatesForDeduplication(List<Name> originalNamesList);

    /**
     * Gets a user-friendly name or identifier for this candidate finding strategy.
     *
     * @return The name of the strategy.
     */
    String getName();

    /**
     * Clears any internal index state (e.g., the built index, reference to the indexed list).
     * This makes the instance ready to index a new list.
     */
    void reset();
}
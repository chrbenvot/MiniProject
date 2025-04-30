package com.info2.miniprojet.indexing;

import java.util.List;

public interface IndexBuilder {
    /**
     * Builds an index structure from a list of processed name token lists.
     * @param processedTokenLists A list where each inner list represents the tokens of a single name.
     * @return An object representing the built index (implementation specific).
     */
    Object buildIndex(List<List<String>> processedTokenLists);

    /**
     * Gets a user-friendly name for this strategy.
     * @return The name identifier.
     */
    String getName();
}
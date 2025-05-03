package com.info2.miniprojet.indexing;

import java.util.List;
import com.info2.miniprojet.core.Name;

public interface IndexBuilder {
    /**
     * Builds an index structure from a list of processed name token lists.
     * @param names list of names to index.
     * @return An object representing the built index (implementation specific).
     */
    Object buildIndex(List<Name> names); // Object because no current best supertype for the potential return types (exp: TrieNode for trie indexing, and hashmaps)

    /**
     * Gets a user-friendly name for this strategy.
     * @return The name identifier.
     */
    String getName();
}
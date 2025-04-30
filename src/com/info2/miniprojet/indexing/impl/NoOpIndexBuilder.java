package com.info2.miniprojet.indexing.impl;

import com.info2.miniprojet.indexing.IndexBuilder;
import java.util.List;

public class NoOpIndexBuilder implements IndexBuilder {

    @Override
    public Object buildIndex(List<List<String>> processedTokenLists) { //OBJECT: THIS IS A SKELETOOOOOON
        // Skeleton implementation: Does no actual indexing,
        // but returns the SIZE of the input list.
        // This allows the lazy CandidateFinder to know how many indices to return.
        int size = (processedTokenLists == null) ? 0 : processedTokenLists.size();
        System.out.println("DEBUG: NoOpIndexBuilder.buildIndex called (returning size: " + size + ").");
        return size; // Return the number of items processed
    }

    @Override
    public String getName() {
        return "NOOP_BUILDER"; // Matches default config choice
    }
}
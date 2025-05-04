package com.info2.miniprojet.indexing.impl;

import com.info2.miniprojet.core.Name;
import java.util.List;

public class NoOpIndexBuilder  {


    public Object buildIndex(List<Name> names) { //OBJECT: THIS IS A SKELETOOOOOON(actually maybe not lmao)
        // Skeleton implementation: Does no actual indexing,
        // but returns the SIZE of the input list.
        // This allows the lazy CandidateFinder to know how many indices to return.
        int size = (names == null) ? 0 : names.size();
        System.out.println("DEBUG: NoOpIndexBuilder.buildIndex called (returning size: " + size + ").");
        return size; // Return the number of items processed
    }


    public String getName() {
        return "NOOP_BUILDER"; // Matches default config choice
    }
}
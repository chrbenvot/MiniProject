package com.info2.miniprojet.indexing.impl;

import com.info2.miniprojet.indexing.CandidateFinder;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections; // Added for Collections.emptyList()

public class ReturnAllCandidateFinder implements CandidateFinder {

    @Override
    public List<Integer> findCandidates(List<String> processedQueryTokens, Object indexStructure) {
        // Skeleton implementation: Ignores the query.
        System.out.println("DEBUG: ReturnAllCandidateFinder.findCandidates called.");

        if (!(indexStructure instanceof Integer)) {
            System.err.println("Warning: ReturnAllCandidateFinder expected indexStructure to be Integer (size), but got " + (indexStructure == null ? "null" : indexStructure.getClass().getName()) + ". Returning empty list.");
            return Collections.emptyList(); // Return empty list if index is not the expected size
        }

        int size = (Integer) indexStructure;

        if (size <= 0) {
            System.out.println("DEBUG: ReturnAllCandidateFinder received size 0 or less, returning empty list.");
            return Collections.emptyList(); // No indices if size is zero or negative
        }

        // Generate indices from 0 to size-1
        List<Integer> allIndices = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            allIndices.add(i);
        }
        System.out.println("DEBUG: ReturnAllCandidateFinder returning " + size + " indices.");
        return allIndices;
    }

    @Override
    public String getName() {
        return "FIND_ALL"; // Matches default config choice
    }
}
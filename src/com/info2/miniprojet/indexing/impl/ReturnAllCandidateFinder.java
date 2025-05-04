package com.info2.miniprojet.indexing.impl;

import com.info2.miniprojet.core.Couple;
import com.info2.miniprojet.core.Name;
import com.info2.miniprojet.indexing.CandidateFinder;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections; // Added for Collections.emptyList()

public class ReturnAllCandidateFinder implements CandidateFinder {

    @Override
    public List<Couple<Name>> findCandidates(Name queryName, List<Name> namesList, Object indexStructure) {
        // Skeleton implementation: Ignores the query.
        System.out.println("DEBUG: ReturnAllCandidateFinder.findCandidates called.");

        if (!(indexStructure instanceof Integer)) {
            System.err.println("Warning: ReturnAllCandidateFinder expected indexStructure to be Integer (size), but got " + (indexStructure == null ? "null" : indexStructure.getClass().getName()) + ". Returning empty list.");
            return Collections.emptyList(); // Return empty list if index is not the expected size
        }

        int expectedSize = (Integer) indexStructure;
        int actualSize = namesList.size();

        // Check if provided size matches actual list size
        if (expectedSize != actualSize) {
            System.err.println("Warning: ReturnAllCandidateFinder index size (" + expectedSize + ") does not match namesList size (" + actualSize + "). Using actual size.");
            // Decide how to handle mismatch - use actualSize for safety in loop
            // Or return empty list if it implies an inconsistency:
            // return Collections.emptyList();
        }

        if (actualSize <= 0) {
            System.out.println("DEBUG: ReturnAllCandidateFinder received size 0 or less, returning empty list.");
            return Collections.emptyList(); // No indices if size is zero or negative
        }

        // Generate indices from 0 to size-1
        List<Couple<Name>> candidatePairs = new ArrayList<>(actualSize);
        for (Name candidateName: namesList) {
            candidatePairs.add(new Couple<>(queryName,candidateName));
        }
        System.out.println("DEBUG: ReturnAllCandidateFinder returning " + candidatePairs.size() + " candidate pairs.");
        return candidatePairs;
    }

    @Override
    public String getName() {
        return "FIND_ALL"; // Matches default config choice
    }
}
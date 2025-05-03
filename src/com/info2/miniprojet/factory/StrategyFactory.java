package com.info2.miniprojet.factory;

import com.info2.miniprojet.comparison.NameComparator;
import com.info2.miniprojet.comparison.StringComparator;
import com.info2.miniprojet.indexing.CandidateFinder;
import com.info2.miniprojet.indexing.IndexBuilder;
import com.info2.miniprojet.preprocessing.Preprocessor;

public class StrategyFactory {

    public static Preprocessor createPreprocessor(String preprocessorName) {
        //TODO: add switch logic
    }
    public static IndexBuilder createIndexBuilder(String indexBuilderName) {}
    public static CandidateFinder createCandidateFinder(String candidateFinderName) {}
    public static NameComparator createNameComparator(String nameComparatorName){}
    public static StringComparator createStringComparator(String stringComparatorName){}
}

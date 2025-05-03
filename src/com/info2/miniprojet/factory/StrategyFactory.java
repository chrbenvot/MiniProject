package com.info2.miniprojet.factory;

import com.info2.miniprojet.comparison.NameComparator;
import com.info2.miniprojet.comparison.StringComparator;
import com.info2.miniprojet.indexing.CandidateFinder;
import com.info2.miniprojet.indexing.IndexBuilder;
import com.info2.miniprojet.preprocessing.Preprocessor;

import java.util.List;

public class StrategyFactory {
    public static final List<String> PREPROCESSOR_CHOICES=List.of(); // immutable list of processors
    public static final List<String> INDEX_BUILDER_CHOICES=List.of();
    public static final List<String> CANDIDATE_FINDER_CHOICES=List.of();
    public static final List<String> NAME_COMPARATOR_CHOICES=List.of();
    public static final List<String> STRING_COMPARATOR_CHOICES=List.of();
    public static Preprocessor createPreprocessor(String preprocessorName) {
        //TODO: add switch logic
    }
    public static IndexBuilder createIndexBuilder(String indexBuilderName) {}
    public static CandidateFinder createCandidateFinder(String candidateFinderName) {}
    public static NameComparator createNameComparator(String nameComparatorName){}
    public static StringComparator createStringComparator(String stringComparatorName){}
    public static List<String> getPreprocessorChoices() {return PREPROCESSOR_CHOICES;}
    public static List<String> getIndexBuilderChoices() {return INDEX_BUILDER_CHOICES;}
    public static List<String> getCandidateFinderChoices() {return CANDIDATE_FINDER_CHOICES;}
    public static List<String> getNameComparatorChoices() {return NAME_COMPARATOR_CHOICES;}
    public static List<String> getStringComparatorChoices() {return STRING_COMPARATOR_CHOICES;}
}

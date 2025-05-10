package com.info2.miniprojet.factory;

// Preprocessing
import com.info2.miniprojet.preprocessing.Preprocessor;
import com.info2.miniprojet.preprocessing.impl.*;
import com.info2.miniprojet.encoding.Encoder;
import com.info2.miniprojet.encoding.impl.*;

// Indexing (CandidateFinder now handles indexing internally)
import com.info2.miniprojet.indexing.CandidateFinder;
import com.info2.miniprojet.indexing.impl.*;

// Comparison
import com.info2.miniprojet.comparison.NameComparator;
import com.info2.miniprojet.comparison.StringComparator;
import com.info2.miniprojet.comparison.impl.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StrategyFactory {

    private static final String PIPELINE_PREFIX = "PIPELINE:";
    private static final String PIPELINE_SEPARATOR = ",";

    // --- Lists of Available Choices for CLI ---
    public static final List<String> PREPROCESSOR_CHOICES = Collections.unmodifiableList(Arrays.asList(
            "NOOP",
            "TOKENIZE",
            "LOWERCASE",
            "ACCENT_REMOVER",
            "PIPELINE:TOKENIZE,LOWERCASE", // Example pipeline choice
            "PIPELINE:TOKENIZE,LOWERCASE,ACCENT_REMOVER", // Another example
            //TODO: configure this for better UI,mainly clarifying pipeline usage
            "SOUNDEX_PREPROCESS",
            "METAPHONE_PREPROCESS",
            "NICKNAME_NORMALIZER"
    ));

    public static final List<String> CANDIDATE_FINDER_CHOICES = Collections.unmodifiableList(Arrays.asList(
            "CARTESIAN_FIND_ALL", // Default/Lazy
            "DICTIONARY_LAST_TOKEN",
            "TRIE_FINDER",
            "REDBLACKTREE_FINDER"
    ));

    public static final List<String> NAME_COMPARATOR_CHOICES = Collections.unmodifiableList(Arrays.asList(
            "PASS_THROUGH_NAME", // Default, uses ExactMatchStringComparator internally
            "POSITIONAL_WEIGHTED",
            "BAG_OF_WORDS",
            "JACCARD_TOKEN_SET"
    ));

    // This list might be used if a NameComparator allows choosing its internal StringComparator
    // or if user could choose a StringComparator directly for some debug/simple mode.
    public static final List<String> STRING_COMPARATOR_CHOICES = Collections.unmodifiableList(Arrays.asList(
            "EXACT_STRING",
            "LEVENSHTEIN",
            "JARO_WINKLER"
    ));

    // --- Getter Methods for Choices (used by CLI) ---
    public static List<String> getAvailablePreprocessorChoices() { return PREPROCESSOR_CHOICES; }
    public static List<String> getAvailableCandidateFinderChoices() { return CANDIDATE_FINDER_CHOICES; }
    public static List<String> getAvailableNameComparatorChoices() { return NAME_COMPARATOR_CHOICES; }
    public static List<String> getAvailableStringComparatorChoices() { return STRING_COMPARATOR_CHOICES; }

    // --- Creation Methods ---

    public static Preprocessor createPreprocessor(String choice) {
        if (choice == null || choice.trim().isEmpty()) {
            choice = "NOOP"; // Default
        }
        String upperChoice = choice.toUpperCase().trim();

        System.out.println("StrategyFactory: Creating Preprocessor for choice: " + upperChoice);

        if (upperChoice.startsWith(PIPELINE_PREFIX)) {
            String stagesString = upperChoice.substring(PIPELINE_PREFIX.length());
            String[] stageNames = stagesString.split(PIPELINE_SEPARATOR);
            List<Preprocessor> pipelineStages = new ArrayList<>();
            for (String stageName : stageNames) {
                String trimmedStageName = stageName.trim();
                if (trimmedStageName.isEmpty()) continue;

                Preprocessor stage = createPreprocessor(trimmedStageName); // Recursive call for individual stages
                // Avoid adding NoOp if it was a result of an unknown stage name, unless "NOOP" was explicit
                if (!(stage instanceof NoOpPreprocessor && !trimmedStageName.equalsIgnoreCase("NOOP"))) {
                    pipelineStages.add(stage);
                } else if (!trimmedStageName.equalsIgnoreCase("NOOP")) { // Only log warning if it wasn't explicitly "NOOP"
                    System.err.println("Warning: Unknown stage '" + stageName + "' in pipeline definition '" + upperChoice + "' skipped.");
                }
            }
            if (pipelineStages.isEmpty()) {
                System.err.println("Warning: Pipeline '" + upperChoice + "' is empty or had all unknown stages. Using NOOP.");
                return new NoOpPreprocessor();
            }
            return new PipelinePreprocessor(pipelineStages);
        }

        switch (upperChoice) {
            case "NOOP":
                return new NoOpPreprocessor();
            case "TOKENIZE":
                return new SimpleTokenizer();
            case "LOWERCASE":
                return new LowercaseNormalizer();
            case "ACCENT_REMOVER":
                return new AccentRemover();
            // Add cases for other single preprocessors here...
            case "SOUNDEX_PREPROCESS":
                return new PhoneticEncodingPreprocessor(new SoundexEncoder());
            case "METAPHONE_PREPROCESS":
                return new PhoneticEncodingPreprocessor(new MetaphoneEncoder());
            case "NICKNAME_NORMALIZER":
                return new HypocorismNormalizerPreprocessor();
            default:
                System.err.println("Warning: Unknown Preprocessor choice '" + upperChoice + "', using NOOP.");
                return new NoOpPreprocessor();
        }
    }

    public static CandidateFinder createCandidateFinder(String choice) {
        if (choice == null || choice.trim().isEmpty()) {
            choice = "CARTESIAN_FIND_ALL"; // Default
        }
        String upperChoice = choice.toUpperCase().trim();
        System.out.println("StrategyFactory: Creating CandidateFinder for choice: " + upperChoice);

        switch (upperChoice) {
            case "CARTESIAN_FIND_ALL":
                return new CartesianCandidateFinder();
            case "DICTIONARY_LAST_TOKEN":
                return new DictionaryCandidateFinder();
            case "TRIE_FINDER":
                 return new TrieCandidateFinder();
            case "REDBLACKTREE_FINDER":
                return new RedBlackTreeCandidateFinder();
            // Add other CandidateFinder implementations here
            default:
                System.err.println("Warning: Unknown CandidateFinder choice '" + upperChoice + "', using Cartesian.");
                return new CartesianCandidateFinder();
        }
    }

    public static StringComparator createStringComparator(String choice) {
        if (choice == null || choice.trim().isEmpty()) {
            choice = "EXACT_STRING"; // Default
        }
        String upperChoice = choice.toUpperCase().trim();
        System.out.println("StrategyFactory: Creating StringComparator for choice: " + upperChoice);

        switch (upperChoice) {
            case "EXACT_STRING":
                return new ExactMatchComparator();
            case "LEVENSHTEIN":
                return new LevenshteinComparator();
            case "JARO_WINKLER":
                return new JarowinklerComparator();
            // Add other StringComparator implementations here
            default:
                System.err.println("Warning: Unknown StringComparator choice '" + upperChoice + "', using ExactMatch.");
                return new ExactMatchComparator();
        }
    }

    public static NameComparator createNameComparator(String choice,String stringComparatorChoiceForInjection) {
        if (choice == null || choice.trim().isEmpty()) {
            choice = "PASS_THROUGH_NAME"; // Default
        }
        if (stringComparatorChoiceForInjection == null || stringComparatorChoiceForInjection.trim().isEmpty()){
            stringComparatorChoiceForInjection = "EXACT_STRING"; // Default if needed
        }
        String upperChoice = choice.toUpperCase().trim();
        System.out.println("StrategyFactory: Creating NameComparator for choice: " + upperChoice+"' (possibly using StringComparator '" + stringComparatorChoiceForInjection+"')");
        StringComparator injectedStringComp;
        switch (upperChoice) {
            case "PASS_THROUGH_NAME":
                // PassThrough uses a specific StringComparator, e.g., ExactMatch by default
                return new PassThroughNameComparator(createStringComparator("EXACT_STRING"));

            case "POSITIONAL_WEIGHTED":
                 injectedStringComp = createStringComparator(stringComparatorChoiceForInjection);
                 double fnWeight = 0.4; double lnWeight = 0.5; double mnWeight = 0.1;
                 return new PositionalWeightedNameComparator(injectedStringComp, fnWeight, lnWeight, mnWeight);
            case "BAG_OF_WORDS":
                 injectedStringComp = createStringComparator(stringComparatorChoiceForInjection);
                 switch(stringComparatorChoiceForInjection){
                     case "EXACT_STRING":
                         return new BagOfWordsNameComparator(injectedStringComp,1);
                     case "LEVENSHTEIN":
                         return new BagOfWordsNameComparator(injectedStringComp,0.8);
                     case "JARO_WINKLER":
                         return new BagOfWordsNameComparator(injectedStringComp,0.85);
                     default:
                         System.err.println("Warning: Unknown StringComparator choice '" + stringComparatorChoiceForInjection + "', using ExactMatch.");
                         return new BagOfWordsNameComparator(new ExactMatchComparator(),1);
                 }

            case "JACCARD_TOKEN_SET":
                return new JaccardTokenNameComparator();


            // Add other NameComparator implementations here
            default:
                System.err.println("Warning: Unknown NameComparator choice '" + upperChoice + "', using PassThrough with ExactMatch.");
                return new PassThroughNameComparator(createStringComparator("EXACT_STRING"));
        }
    }
}
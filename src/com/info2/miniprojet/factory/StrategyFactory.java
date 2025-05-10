package com.info2.miniprojet.factory;

// Preprocessing
import com.info2.miniprojet.comparison.impl.AiNameComparator;
import com.info2.miniprojet.preprocessing.Preprocessor;
import com.info2.miniprojet.preprocessing.impl.NoOpPreprocessor;
import com.info2.miniprojet.preprocessing.impl.SimpleTokenizer;
import com.info2.miniprojet.preprocessing.impl.LowercaseNormalizer;
import com.info2.miniprojet.preprocessing.impl.AccentRemover;
import com.info2.miniprojet.preprocessing.impl.PipelinePreprocessor;

// Indexing (CandidateFinder now handles indexing internally)
import com.info2.miniprojet.indexing.CandidateFinder;
import com.info2.miniprojet.indexing.impl.CartesianCandidateFinder;
// import com.info2.miniprojet.indexing.impl.PhoneticCandidateFinder; // Example for future

// Comparison
import com.info2.miniprojet.comparison.NameComparator;
import com.info2.miniprojet.comparison.StringComparator;
import com.info2.miniprojet.comparison.impl.ExactMatchComparator;
import com.info2.miniprojet.comparison.impl.PassThroughNameComparator;
// import com.info2.miniprojet.comparison.impl.LevenshteinComparator;
// import com.info2.miniprojet.comparison.impl.StructuredNameComparator;

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
            "ACCENT_REMOVER"
    ));

    public static final List<String> CANDIDATE_FINDER_CHOICES = Collections.unmodifiableList(Arrays.asList(
            "CARTESIAN_FIND_ALL" // Default/Lazy
            // "PHONETIC_FINDER" // Add when PhoneticCandidateFinder is implemented
    ));

    public static final List<String> NAME_COMPARATOR_CHOICES = Collections.unmodifiableList(Arrays.asList(
            "PASS_THROUGH_NAME", // Default, uses ExactMatchStringComparator internally
            "AI_CROSS_ENCODER"
            // "STRUCTURED_LEVENSHTEIN" // Add when StructuredNameComparator with Levenshtein is ready
    ));

    // This list might be used if a NameComparator allows choosing its internal StringComparator
    // or if user could choose a StringComparator directly for some debug/simple mode.
    public static final List<String> STRING_COMPARATOR_CHOICES = Collections.unmodifiableList(Arrays.asList(
            "EXACT_STRING"
            // "LEVENSHTEIN",
            // "JARO_WINKLER"
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
            // case "PHONETIC_FINDER":
            //     return new PhoneticCandidateFinder();
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
            // case "LEVENSHTEIN":
            //     return new LevenshteinComparator();
            // case "JARO_WINKLER":
            //     return new JaroWinklerComparator();
            // Add other StringComparator implementations here
            default:
                System.err.println("Warning: Unknown StringComparator choice '" + upperChoice + "', using ExactMatch.");
                return new ExactMatchComparator();
        }
    }

    public static NameComparator createNameComparator(String choice) {
        if (choice == null || choice.trim().isEmpty()) {
            choice = "PASS_THROUGH_NAME"; // Default
        }
        String upperChoice = choice.toUpperCase().trim();
        System.out.println("StrategyFactory: Creating NameComparator for choice: " + upperChoice);

        switch (upperChoice) {
            case "PASS_THROUGH_NAME":
                // PassThrough uses a specific StringComparator, e.g., ExactMatch by default
                return new PassThroughNameComparator(createStringComparator("EXACT_STRING"));
            // case "STRUCTURED_LEVENSHTEIN":
            //     // Example: Structured comparator that uses Levenshtein
            //     return new StructuredNameComparator(createStringComparator("LEVENSHTEIN"), 0.6, 0.4); // Example weights
            // Add other NameComparator implementations here
            case "AI_CROSS_ENCODER":
                // You might want to make the API URL configurable via Configuration DTO
                // and pass it here from the Engine, or have a default in AiNameComparator.
                return new AiNameComparator("http://127.0.0.1:5000/similarity");
            default:
                System.err.println("Warning: Unknown NameComparator choice '" + upperChoice + "', using PassThrough with ExactMatch.");
                return new PassThroughNameComparator(createStringComparator("EXACT_STRING"));
        }
    }
}
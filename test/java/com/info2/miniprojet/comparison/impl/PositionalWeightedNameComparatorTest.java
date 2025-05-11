package com.info2.miniprojet.comparison.impl;

import com.info2.miniprojet.comparison.NameComparator;
import com.info2.miniprojet.comparison.StringComparator;
import com.info2.miniprojet.core.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;

//NB: Ideally I'd use Mockito (so now this is sort of an integration test) but i dont want to make things too complex
class PositionalWeightedNameComparatorTest {

    private NameComparator weightedComparatorWithExactMatch;
    private NameComparator weightedComparatorWithCustomSim;

    // Define default weights for most tests
    private final double FN_WEIGHT = 0.4;
    private final double LN_WEIGHT = 0.5;
    private final double MN_WEIGHT = 0.1;

    // A simple stub StringComparator that returns 1.0 for specific pairs, 0.0 otherwise
    private static class StubSimilarityComparator implements StringComparator {
        private final String s1, s2;
        private final double scoreToReturn;
        private final boolean isDistance;

        public StubSimilarityComparator(String s1, String s2, double score, boolean isDist) {
            this.s1 = s1; this.s2 = s2; this.scoreToReturn = score; this.isDistance = isDist;
        }
        public StubSimilarityComparator(boolean isDist, double defaultScore){ // For multiple comparisons
            this.s1 = null; this.s2=null; this.scoreToReturn = defaultScore; this.isDistance = isDist;
        }


        @Override
        public double calculateScore(String str1, String str2) {
            if (this.s1 != null && ((this.s1.equals(str1) && this.s2.equals(str2)) || (this.s1.equals(str2) && this.s2.equals(str1)))) {
                return scoreToReturn;
            }
            // If s1 is null, it's a multi-comparison stub
            if(this.s1 == null) return scoreToReturn;

            return isDistance ? Double.MAX_VALUE : 0.0; // Default if not the specified pair
        }
        @Override public boolean isScoreDistance() { return isDistance; }
        @Override public String getName() { return "STUB_SC"; }
    }


    @BeforeEach
    void setUp() {
        weightedComparatorWithExactMatch = new PositionalWeightedNameComparator(new ExactMatchComparator(), FN_WEIGHT, LN_WEIGHT, MN_WEIGHT);
    }

    @Test
    void getNameShouldReturnFixedStrategyName() { // Renamed for clarity
        // The PositionalWeightedNameComparator's getName() should return its own fixed identifier,
        // not one dynamically built from the injected StringComparator's name.
        // This name should match the key used in StrategyFactory.
        assertEquals("POSITIONAL_WEIGHTED", weightedComparatorWithExactMatch.getName());
    }

    @Test
    void isScoreDistanceShouldBeFalse() {
        assertFalse(weightedComparatorWithExactMatch.isScoreDistance());
    }

    @Test
    void calculateScoreIdenticalNamesPerfectStringMatch() {
        Name name1 = new Name("id1", "John Michael Smith", Arrays.asList("john", "michael", "smith"));
        // ExactMatchComparator will return 1.0 for identical tokens
        assertEquals(1.0, weightedComparatorWithExactMatch.calculateScore(name1, name1), 0.0001);
    }

    @Test
    void calculateScoreDifferentFirstNameOnlyWithExactMatch() {
        Name name1 = new Name("id1", "John Michael Smith", Arrays.asList("john", "michael", "smith"));
        Name name2 = new Name("id2", "Peter Michael Smith", Arrays.asList("peter", "michael", "smith"));
        // "john" vs "peter" = 0.0 with ExactMatch
        // "michael" vs "michael" = 1.0
        // "smith" vs "smith" = 1.0
        // Expected = (0.0 * 0.4) + (1.0 * 0.1) + (1.0 * 0.5) = 0.0 + 0.1 + 0.5 = 0.6
        // Corrected based on your positional logic: (0.0 * FN_WEIGHT) + (1.0 * MN_WEIGHT) + (1.0 * LN_WEIGHT)
        // / (FN_WEIGHT + MN_WEIGHT + LN_WEIGHT if total weight not 1)
        // Assuming total weight for normalization is FN_WEIGHT + LN_WEIGHT + MN_WEIGHT = 1.0
        assertEquals(0.6, weightedComparatorWithExactMatch.calculateScore(name1, name2), 0.0001);
    }


    @Test
    void calculateScoreUsingCustomSimilarityStub() {
        // Create a StringComparator stub that returns specific values for specific pairs
        StringComparator customSim = new StringComparator() {
            @Override public double calculateScore(String s1, String s2) {
                if (("john".equals(s1) && "joan".equals(s2)) || ("joan".equals(s1) && "john".equals(s2))) return 0.75; // fnSim
                if (("michael".equals(s1) && "michelle".equals(s2)) || ("michelle".equals(s1) && "michael".equals(s2))) return 0.70; // mnSim
                if (("smith".equals(s1) && "smith".equals(s2))) return 1.0; // lnSim
                return 0.0; // Default for any other pair
            }
            @Override public boolean isScoreDistance() { return false; } // It's a similarity
            @Override public String getName() { return "CUSTOM_SIM"; }
        };

        NameComparator distWeightedComp = new PositionalWeightedNameComparator(customSim, FN_WEIGHT, LN_WEIGHT, MN_WEIGHT);

        Name name1 = new Name("id1", "John Michael Smith", Arrays.asList("john", "michael", "smith"));
        Name name2 = new Name("id2", "Joan Michelle Smith", Arrays.asList("joan", "michelle", "smith"));

        // Expected combined = (0.75 * 0.4) + (0.70 * 0.1) + (1.0 * 0.5)
        //                   = 0.30      + 0.07       + 0.50      = 0.870
        // Note: The previous calculation for this case (0.875) was based on a slightly different
        //       distance-to-similarity conversion. This one uses direct similarity scores from the stub.
        assertEquals(0.870, distWeightedComp.calculateScore(name1, name2), 0.0001);
    }

    // ... (add other tests for single tokens, empty lists, nulls as before, using new ExactMatchComparator() or stubs) ...
    @Test
    void calculateScoreSingleTokenVsSingleToken() {
        Name name1 = new Name("id1", "Smith", List.of("smith"));
        Name name2 = new Name("id2", "Smyth", List.of("smyth")); // ExactMatch returns 0
        assertEquals(0.0, weightedComparatorWithExactMatch.calculateScore(name1, name2), 0.0001);

        // Test with a custom similarity for this specific pair
        StringComparator customSinglePair = new StringComparator() {
            @Override public double calculateScore(String s1, String s2) { return ("smith".equals(s1) && "smyth".equals(s2)) ? 0.85 : 0.0; }
            @Override public boolean isScoreDistance() { return false; }
            @Override public String getName() { return "CUSTOM_SINGLE"; }
        };
        NameComparator wcCustom = new PositionalWeightedNameComparator(customSinglePair, 1.0, 0.0, 0.0); // Full weight to this single comparison
        assertEquals(0.85, wcCustom.calculateScore(name1, name2), 0.0001);
    }

}
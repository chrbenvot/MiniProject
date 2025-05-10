package com.info2.miniprojet.comparison.impl;

import com.info2.miniprojet.comparison.NameComparator;
import com.info2.miniprojet.comparison.StringComparator;
import com.info2.miniprojet.core.Name;

import java.util.List;

public class PositionalWeightedNameComparator implements NameComparator {
    private final StringComparator stringComparator;
    private final double firstNameWeight;
    private final double lastNameWeight;
    private final double middleNameWeight;

    public PositionalWeightedNameComparator(StringComparator stringComparator, double firstNameWeight, double lastNameWeight, double middleNameWeight){
        if (stringComparator == null) {
            throw new IllegalArgumentException("StringComparator cannot be null.");
        }
        this.stringComparator = stringComparator;
        this.firstNameWeight = firstNameWeight;
        this.lastNameWeight = lastNameWeight;
        this.middleNameWeight = middleNameWeight;
    }

    @Override
    public double calculateScore(Name name1, Name name2){
        if (name1 == null || name2 == null) return 0.0;

        List<String> tokens1 = name1.processedTokens();
        List<String> tokens2 = name2.processedTokens();

        if (tokens1 == null || tokens1.isEmpty() || tokens2 == null || tokens2.isEmpty()) {
            return 0.0; // nothing here
        }

        // --- Case 1: Single token name (treat as whole name comparison) ---
        if (tokens1.size() == 1 && tokens2.size() == 1) {
            double score = stringComparator.calculateScore(tokens1.get(0), tokens2.get(0));
            if (stringComparator.isScoreDistance()) {
                // Normalize distance to similarity (0-1).
                // Max length for normalization.
                int maxLength = Math.max(tokens1.get(0).length(), tokens2.get(0).length());
                return maxLength == 0 ? 1.0 : Math.max(0, 1.0 - (score / maxLength));
            }
            return score; // Assume already similarity
        }
        // If one has 1 token and other has more, it's a partial match at best.
        // This heuristic might treat it as a low score or compare the single token against likely first/last.
        // For simplicity, if structures differ significantly (1 token vs 3+), we might score low or
        // try to match the single token against both first and last of the other.
        // Let's go with a more structured approach below.

        // --- Component Identification ---
        String fn1 = "", ln1 = "", mn1_str = "";
        String fn2 = "", ln2 = "", mn2_str = "";

        // Name 1 components
        if (!tokens1.isEmpty()) {
            fn1 = tokens1.get(0);
            if (tokens1.size() > 1) {
                ln1 = tokens1.get(tokens1.size() - 1);
                if (tokens1.size() > 2) {
                    mn1_str = String.join(" ", tokens1.subList(1, tokens1.size() - 1)); // what's between first and last token is middle name
                }
            } else { // Single token, use it as both first and last for comparison logic
                ln1 = fn1;
            }
        }

        // Name 2 components
        if (!tokens2.isEmpty()) {
            fn2 = tokens2.get(0);
            if (tokens2.size() > 1) {
                ln2 = tokens2.get(tokens2.size() - 1);
                if (tokens2.size() > 2) {
                    mn2_str = String.join(" ", tokens2.subList(1, tokens2.size() - 1));
                }
            } else { // Single token
                ln2 = fn2;
            }
        }

        // --- Component Comparison ---
        double fnSim = getSimilarityScore(fn1, fn2);
        double lnSim = getSimilarityScore(ln1, ln2);
        double mnSim = 0.0;

        // Weighted score based on number of tokens
        double totalWeight = 0;
        double combinedScore = 0;

        if (tokens1.size() == 1 || tokens2.size() == 1) {
            // If one is a single token, compare it against both first and last of the other, take best
            // Or, if both are single token, this was handled above, but as a fallback:
            if (tokens1.size() == 1 && tokens2.size() == 1) {
                combinedScore = fnSim; // fnSim is already the comparison of the single tokens
                totalWeight = 1.0; // Full weight
            } else if (tokens1.size() == 1) { // name1 is single token, name2 is multi-token
                double simToFn2 = getSimilarityScore(tokens1.get(0), fn2);
                double simToLn2 = getSimilarityScore(tokens1.get(0), ln2);
                combinedScore = Math.max(simToFn2, simToLn2); // Take the best match
                totalWeight = 1.0; // Full weight to this single comparison
            } else { // name2 is single token, name1 is multi-token
                double simToFn1 = getSimilarityScore(fn1, tokens2.get(0));
                double simToLn1 = getSimilarityScore(ln1, tokens2.get(0));
                combinedScore = Math.max(simToFn1, simToLn1);
                totalWeight = 1.0;
            }
        } else if (tokens1.size() == 2 && tokens2.size() == 2) { // Both have First and Last
            combinedScore = (fnSim * 0.5) + (lnSim * 0.5); // 50/50 weight
            totalWeight = 1.0;
        } else if (tokens1.size() >= 2 && tokens2.size() >= 2) { // Both have at least First and Last
            combinedScore = (fnSim * firstNameWeight) + (lnSim * lastNameWeight);
            totalWeight = firstNameWeight + lastNameWeight;
            if (!mn1_str.isEmpty() && !mn2_str.isEmpty()) {
                mnSim = getSimilarityScore(mn1_str, mn2_str);
                combinedScore += (mnSim * middleNameWeight);
                totalWeight += middleNameWeight;
            } else if (mn1_str.isEmpty() && mn2_str.isEmpty()) {
                // No middle names to compare, weights are already correct
            } else {
                // One has middle name, one doesn't - penalize slightly or give 0 for mnSim
                // For simplicity, we add 0 for mnSim if one is empty, totalWeight isn't increased
            }
        } else {
            // Fallback for other mixed cases (e.g. 1 token vs 2 tokens not fully handled above)
            // Could compare the single token of one name against the joined tokens of the other.
            // For now, let's rely on the specific handling above.
            // If this path is reached, it means one list might be empty if initial checks fail.
            return 0.0; // Low similarity for completely different structures not caught
        }


        // Normalize score if totalWeight used is not 1.0 (and weights aren't pre-normalized)
        // Or ensure your weights (firstNameWeight, lastNameWeight, middleNameWeight) always sum to 1.0
        // For this example, assuming weights are proportions.
        return (totalWeight > 0) ? Math.max(0, Math.min(combinedScore / totalWeight, 1.0)) : 0.0;
    }

    // Helper to get similarity (0-1) from the stringComparator
    private double getSimilarityScore(String s1, String s2) {
        if (s1.isEmpty() && s2.isEmpty()) return 1.0; // Two empty strings are perfectly similar
        if (s1.isEmpty() || s2.isEmpty()) return 0.0; // One empty, one not is 0 similarity

        double score = stringComparator.calculateScore(s1, s2);
        if (stringComparator.isScoreDistance()) {
            int maxLength = Math.max(s1.length(), s2.length());
            return maxLength == 0 ? 1.0 : Math.max(0, 1.0 - (score / maxLength));
        }
        return score; // Assume it's already a similarity score
    }
    @Override
    public boolean isScoreDistance(){
        return false;
    }
    @Override
    public String getName(){
        return "POSITIONAL_WEIGHTED";
    }
}



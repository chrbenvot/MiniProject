package com.info2.miniprojet.comparison.impl;

import com.info2.miniprojet.comparison.StringComparator;
import java.util.HashMap;
import java.util.Map;

public class KeyboardDistanceComparator implements StringComparator {

    // Simplified QWERTY layout representation (could be more detailed)
    // We'll represent it as a map of char -> (row, col)
    private static final Map<Character, int[]> KEY_POSITIONS = new HashMap<>();

    static {
        // Row 0
        KEY_POSITIONS.put('q', new int[]{0, 0}); KEY_POSITIONS.put('w', new int[]{0, 1});
        KEY_POSITIONS.put('e', new int[]{0, 2}); KEY_POSITIONS.put('r', new int[]{0, 3});
        KEY_POSITIONS.put('t', new int[]{0, 4}); KEY_POSITIONS.put('y', new int[]{0, 5});
        KEY_POSITIONS.put('u', new int[]{0, 6}); KEY_POSITIONS.put('i', new int[]{0, 7});
        KEY_POSITIONS.put('o', new int[]{0, 8}); KEY_POSITIONS.put('p', new int[]{0, 9});
        // Row 1
        KEY_POSITIONS.put('a', new int[]{1, 0}); KEY_POSITIONS.put('s', new int[]{1, 1});
        KEY_POSITIONS.put('d', new int[]{1, 2}); KEY_POSITIONS.put('f', new int[]{1, 3});
        KEY_POSITIONS.put('g', new int[]{1, 4}); KEY_POSITIONS.put('h', new int[]{1, 5});
        KEY_POSITIONS.put('j', new int[]{1, 6}); KEY_POSITIONS.put('k', new int[]{1, 7});
        KEY_POSITIONS.put('l', new int[]{1, 8});
        // Row 2
        KEY_POSITIONS.put('z', new int[]{2, 0}); KEY_POSITIONS.put('x', new int[]{2, 1});
        KEY_POSITIONS.put('c', new int[]{2, 2}); KEY_POSITIONS.put('v', new int[]{2, 3});
        KEY_POSITIONS.put('b', new int[]{2, 4}); KEY_POSITIONS.put('n', new int[]{2, 5});
        KEY_POSITIONS.put('m', new int[]{2, 6});
        // Add numbers and other symbols if desired, adjust rows/cols
    }

    private final double ADJACENT_HORIZONTAL_VERTICAL_COST = 0.5; // Lower cost for adjacent keys
    private final double ADJACENT_DIAGONAL_COST = 0.75;          // Slightly higher for diagonal
    private final double STANDARD_SUBSTITUTION_COST = 1.0;      // For non-letters or distant letters
    private final double INSERTION_DELETION_COST = 1.0;

    @Override
    public double calculateScore(String s1, String s2) {
        if (s1 == null && s2 == null) return 0.0;
        if (s1 == null) return s2.length() * INSERTION_DELETION_COST;
        if (s2 == null) return s1.length() * INSERTION_DELETION_COST;

        // For this comparator, it's easier to work with lowercase
        String str1 = s1.toLowerCase();
        String str2 = s2.toLowerCase();

        int m = str1.length();
        int n = str2.length();
        double[][] dp = new double[m + 1][n + 1];

        for (int i = 0; i <= m; i++) dp[i][0] = i * INSERTION_DELETION_COST;
        for (int j = 0; j <= n; j++) dp[0][j] = j * INSERTION_DELETION_COST;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                char char1 = str1.charAt(i - 1);
                char char2 = str2.charAt(j - 1);
                double substitutionCost;

                if (char1 == char2) {
                    substitutionCost = 0.0;
                } else {
                    substitutionCost = getKeyboardSubstitutionCost(char1, char2);
                }

                dp[i][j] = Math.min(
                        dp[i - 1][j] + INSERTION_DELETION_COST,      // Deletion from s1
                        Math.min(
                                dp[i][j - 1] + INSERTION_DELETION_COST,    // Insertion into s1
                                dp[i - 1][j - 1] + substitutionCost  // Substitution/Match
                        )
                );
            }
        }
        return dp[m][n];
    }

    private double getKeyboardSubstitutionCost(char c1, char c2) {
        int[] pos1 = KEY_POSITIONS.get(c1);
        int[] pos2 = KEY_POSITIONS.get(c2);

        // If one or both characters are not on our defined keyboard layout (e.g., symbols, uppercase)
        if (pos1 == null || pos2 == null) {
            return STANDARD_SUBSTITUTION_COST; // Default cost
        }

        int rowDiff = Math.abs(pos1[0] - pos2[0]);
        int colDiff = Math.abs(pos1[1] - pos2[1]);

        // Simple Manhattan-like distance for keyboard
        if (rowDiff == 0 && colDiff == 1) return ADJACENT_HORIZONTAL_VERTICAL_COST; // Horizontal neighbor
        if (rowDiff == 1 && colDiff == 0) return ADJACENT_HORIZONTAL_VERTICAL_COST; // Vertical neighbor
        if (rowDiff == 1 && colDiff == 1) return ADJACENT_DIAGONAL_COST;        // Diagonal neighbor
        

        return STANDARD_SUBSTITUTION_COST; // If not immediately adjacent by this simple rule
    }


    @Override
    public boolean isScoreDistance() {
        return true; // It calculates a distance
    }

    @Override
    public String getName() {
        return "KEYBOARD_DISTANCE";
    }
}
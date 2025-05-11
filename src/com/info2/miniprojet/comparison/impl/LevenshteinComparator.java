package com.info2.miniprojet.comparison.impl;

import com.info2.miniprojet.comparison.StringComparator;

public class LevenshteinComparator implements StringComparator {

	@Override
	public double calculateScore(String string1, String string2) {
		// 1. Handle null inputs
		if (string1 == null && string2 == null) {
			return 0.0; // Distance between two nulls is 0
		}
		if (string1 == null) {
			// Distance is the number of insertions needed, which is length of string2
			return string2.length();
		}
		if (string2 == null) {
			// Distance is the number of deletions needed, which is length of string1
			return string1.length();
		}

		// 2. Call the private helper with non-null strings
		return levenshteinDistance(string1, string2);
	}

	@Override
	public boolean isScoreDistance() {
		return true;
	}

	@Override
	public String getName() {
		return "LEVENSHTEIN"; // Consistent with factory key
	}

	private int levenshteinDistance(String s1, String s2) {
		// Ensure strings are not null here (handled by public method)
		// For case-insensitivity, convert both strings to lowercase before processing
		String str1 = s1.toLowerCase();
		String str2 = s2.toLowerCase();

		int m = str1.length();
		int n = str2.length();
		int[][] dp = new int[m + 1][n + 1];

		// Initialize DP table
		// Cost of deleting all chars of str1 to get an empty string
		for (int i = 0; i <= m; i++) {
			dp[i][0] = i;
		}
		// Cost of inserting all chars of str2 into an empty string
		for (int j = 0; j <= n; j++) {
			dp[0][j] = j;
		}

		// Fill DP table
		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				int cost;
				// Compare characters from the (now lowercased) strings
				if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
					cost = 0; // Characters are the same, no cost
				} else {
					cost = 1; // Characters are different, substitution cost is 1
				}

				dp[i][j] = Math.min(
						dp[i - 1][j] + 1,      // Deletion (cost of deleting char from str1)
						Math.min(
								dp[i][j - 1] + 1,    // Insertion (cost of inserting char into str1)
								dp[i - 1][j - 1] + cost // Substitution or Match
						)
				);
			}
		}
		return dp[m][n]; // Return the Levenshtein distance
	}
}
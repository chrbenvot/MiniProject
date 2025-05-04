package com.info2.miniprojet.comparison.impl;
import com.info2.miniprojet.comparison.StringComparator;

public class LevenshteinComparator implements StringComparator {
	@Override
	public double calculateScore(String string1, String string2) {
		return levenshteinDistance(string1, string2);
	}

	@Override
	public boolean isScoreDistance() {
		return true;
	}
	@Override
	public String getName() {
		return "Levenshtein";
	}

	private int levenshteinDistance(String s1, String s2) {
		
		    int m = s1.length();
		    int n = s2.length();
		    int[][] dp = new int[m+1][n+1];

		    for (int i = 0; i <= m; i++) dp[i][0] = i;
		    for (int j = 0; j <= n; j++) dp[0][j] = j;
		    for (int i = 1; i <= m; i++) {
		        for (int j = 1; j <= n; j++) {
		        	int cost;
		        	if (s1.charAt(i-1) == s2.charAt(j-1)) {
		        	    cost = 0;
		        	} else {
		        	    cost = 1;
		        	}
		            
		            dp[i][j] = Math.min(
		                dp[i-1][j] + 1,      // Deletion
		                Math.min(
		                    dp[i][j-1] + 1,    // Insertion
		                    dp[i-1][j-1] + cost // Substitution
		                )
		            );
		        }
		    }
		    return dp[m][n];
		}
	}


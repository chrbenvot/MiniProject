package com.info2.miniprojet.preprocessing.impl;
import com.info2.miniprojet.preprocessing.Preprocessor;
import java.util.List;
import java.util.ArrayList;

public class SimpleTokenizer implements Preprocessor {
	@Override
	public List<String> preprocess(List<String> inputTokens) {
		if(inputTokens == null || inputTokens.isEmpty()) {
			return new ArrayList<>();
		}
		List<String> outputTokens = new ArrayList<>();
		for (String rawString : inputTokens) {
			if (rawString == null || rawString.trim().isEmpty()) {
				outputTokens.add("");
				continue;
			}

			// Regex to split by one or more occurrences of:
			// \\s : whitespace
			// -   : hyphen
			// '   : apostrophe
			// The [] creates a character class.
			// The + means one or more occurrences of any character in the class.
			String[] subTokens = rawString.trim().split("[\\s'-]+");

			for (String subToken : subTokens) {
				if (subToken != null && !subToken.isEmpty()) { // Avoid adding empty strings resulting from multiple delimiters
					outputTokens.add(subToken);
				}
			}
		}
		return outputTokens;
	}

	@Override
	public String getName() {
		return "TOKENIZE";
	}

}

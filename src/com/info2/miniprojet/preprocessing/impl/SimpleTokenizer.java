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
		List<String> copiedtokens = new ArrayList<>(inputTokens);
		List<String> output = new ArrayList<>();
		for ( int i = 0 ; i < inputTokens.size() ; i++ ) {
			String token = copiedtokens.get(i);
			if(token == null || token.isEmpty()) {
				output.add("") ;// Skip null or empty tokens
				continue;
			}
			// Example tokenization : split by spaces
			String[] subTokens = token.split("\\s+");
			for (String subToken : subTokens) {
				output.add(subToken);
				
			}
		}
		return output; 
	}

	@Override
	public String getName() {
		return "TOKENIZE";
	}

}

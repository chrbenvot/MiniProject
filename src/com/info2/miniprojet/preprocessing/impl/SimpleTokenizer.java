package com.info2.miniprojet.preprocessing.impl;
import com.info2.miniprojet.preprocessing.Preprocessor;
import java.util.List;
import java.util.ArrayList;

public class SimpleTokenizer implements Preprocessor {
	@Override
	public List<String> preprocess(List<String> inputTokens) {
		List<String> copiedtokens = new ArrayList<>(inputTokens);
		list<String> output = new ArrayList<>();
		for ( int i = 0 ; i < inputTokens.size() ; i++ ) {
			String token = copiedtokens.get(i);
			// Example tokenization : split by spaces
			String[] subTokens = token.split("\\s+");
			for (String subToken : subTokens) {
				output.add(subToken);
				
			}
		}
		return new ArrayList<>(inputTokens); 
	}

	@Override
	public String getName() {
		return "Tokenization"; 
	}

}

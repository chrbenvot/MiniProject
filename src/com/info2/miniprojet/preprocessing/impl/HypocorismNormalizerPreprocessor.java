package com.info2.miniprojet.preprocessing.impl;

import com.info2.miniprojet.preprocessing.Preprocessor;
import com.info2.miniprojet.util.HypocorismLoader;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class HypocorismNormalizerPreprocessor implements Preprocessor {
    private final Map<String, String> nicknameToCanonicalMap;

    // Constructor: Loads the map when the preprocessor is created
    public HypocorismNormalizerPreprocessor() {
        String csvFilePath = "/home/chrbenvot/IdeaProjects/MiniProjet/nicknames.csv"; // hard coded cause too lazy...
        this.nicknameToCanonicalMap = HypocorismLoader.loadNicknameToCanonicalMap(csvFilePath);
        System.out.println("DEBUG: HypocorismNormalizer loaded " + this.nicknameToCanonicalMap.size() + " nickname mappings.");
    }
    @Override
    public List<String> preprocess(List<String> inputTokens) {
        if (inputTokens == null) return new ArrayList<>();
        List<String> outputTokens = new ArrayList<>(inputTokens.size());

        for (String token : inputTokens) {
            if (token == null) {
                outputTokens.add(null);
                continue;
            }
            // Look up the lowercase version of the token in the map,cause original names are lower case
            String canonicalName = nicknameToCanonicalMap.get(token.toLowerCase());
            if (canonicalName != null) {
                outputTokens.add(canonicalName);
                System.out.println(canonicalName);
            } else {
                // If not a known nickname, keep the original token
                outputTokens.add(token);
            }
        }
        return outputTokens;
    }
    @Override
    public String getName() {
        return "NICKNAME_NORMALIZER";
    }
}

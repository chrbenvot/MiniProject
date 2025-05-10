package com.info2.miniprojet.preprocessing.impl;
import com.info2.miniprojet.preprocessing.Preprocessor;
import com.info2.miniprojet.encoding.impl.MetaphoneEncoder;
import com.info2.miniprojet.encoding.impl.SoundexEncoder;
import com.info2.miniprojet.encoding.Encoder;

import java.util.ArrayList;
import java.util.List;

public class PhoneticEncodingPreprocessor implements Preprocessor{
    private final Encoder encoder;

    public PhoneticEncodingPreprocessor(Encoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public List<String> preprocess(List<String> inputTokens) {
        System.out.println("DEBUG:"+this.getName()+".preprocess called . Input: " + inputTokens);
        if (inputTokens == null) {
            return new ArrayList<>(); // Return empty list for null input
        }
        List<String> encodedTokens = new ArrayList<>();
        for (String token : inputTokens) {
            encodedTokens.add(encoder.encode(token));
        }
        return encodedTokens;
    }
    @Override
    public String getName() {
        return encoder.getName()+"_PREPROCESS";
    }


}

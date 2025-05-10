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
            try{encodedTokens.add(encoder.encode(token));}catch (IllegalArgumentException e){
                System.out.println("This encoder is unfortunately racist \uD83D\uDE14  and probably doesn't encode your language,assumedly Burmese here");
                System.out.println("Or arabic, or any other language that doesn't use the latin alphabet");
                System.out.println("I'm just going to count this token as empty");
                encodedTokens.add(""); // Add empty string for unencodable tokens
        }}
        return encodedTokens;
    }
    @Override
    public String getName() {
        return encoder.getName()+"_PREPROCESS";
    }


}

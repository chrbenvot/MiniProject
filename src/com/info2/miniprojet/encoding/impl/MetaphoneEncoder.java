package com.info2.miniprojet.encoding.impl;

import com.info2.miniprojet.encoding.Encoder;

import org.apache.commons.codec.language.Metaphone;

public class MetaphoneEncoder implements Encoder {
    @Override
    public String encode(String input) {
        Metaphone metaphone= new Metaphone();
        metaphone.setMaxCodeLen(20); // THIS IS EXTREMELY IMPORTANT(if you don't tokenize at least),else everything will stop at 4 coded letters
        return metaphone.encode(input); // but then again if you don't tokenize you're really shooting yourself in the foot lul
    }
    @Override
    public String getName() {
        return "METAPHONE";
    }
}
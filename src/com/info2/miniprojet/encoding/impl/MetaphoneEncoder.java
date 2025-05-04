package com.info2.miniprojet.encoding.impl;

import com.info2.miniprojet.encoding.Encoder;

import org.apache.commons.codec.language.Metaphone;

public class MetaphoneEncoder implements Encoder {
    //TODO: actually implement
    @Override
    public String encode(String input) {
        Metaphone metaphone= new Metaphone();
        return metaphone.encode(input);
    }
    @Override
    public String getName() {
        return "METAPHONE";
    }
}
package com.info2.miniprojet.encoding.impl;

import com.info2.miniprojet.encoding.Encoder;

import org.apache.commons.codec.language.Soundex;

public class SoundexEncoder implements Encoder {
    //TODO: actually implement
    @Override
    public String encode(String input) {
        Soundex soundex= new Soundex();
        return soundex.encode(input);
    }
    @Override
    public String getName() {
        return "SOUNDEX";
    }
}

package com.info2.miniprojet.encoding.impl;

import com.info2.miniprojet.encoding.Encoder;

import org.apache.commons.codec.language.Soundex;

public class SoundexEncoder implements Encoder {
    @Override
    public String encode(String input) {
        Soundex soundex= new Soundex(); // HAS 4 CHARACTER LIMIT BY INTRINSIC IMPLEMENTATION,YOU MUST TOKENIZE HERE
        return soundex.encode(input);   // else any names with similarly sounding PREFIXES will count as the same
    }
    @Override
    public String getName() {
        return "SOUNDEX";
    }
}

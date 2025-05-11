package com.info2.miniprojet.comparison.impl;

import com.info2.miniprojet.comparison.StringComparator;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

public class JarowinklerComparator implements StringComparator {
    @Override
    public double calculateScore(String string1, String string2){
        JaroWinklerSimilarity jaro=new JaroWinklerSimilarity();
        return jaro.apply(string1,string2);
    }
    @Override
    public boolean isScoreDistance(){
        return false;
    }
    @Override
    public String getName(){
        return "JARO_WINKLER";
    }
}

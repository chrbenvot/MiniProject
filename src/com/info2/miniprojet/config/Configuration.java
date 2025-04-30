package com.info2.miniprojet.config;

public class Configuration {
    private String preprocessorChoice;
    private String indexBuilderChoice;
    private String candidateFinderChoice;
    private String nameComparatorChoice;
    private double resultThreshold;
    private int maxResults;
    private boolean isThresholdMode;

    // --- Getters ---
    public String getPreprocessorChoice() {
        return preprocessorChoice;
    }
    public String getIndexBuilderChoice() {
        return indexBuilderChoice;
    }
    public String getCandidateFinderChoice() {
        return candidateFinderChoice;
    }
    public String getNameComparatorChoice() {
        return nameComparatorChoice;
    }
    public double getResultThreshold() {
        return resultThreshold;
    }
    public int getMaxResults() {
        return maxResults;
    }
    public boolean isThresholdMode() {
        return isThresholdMode;
    }

    // --- Setters ---
    public void setPreprocessorChoice(String preprocessorChoice) {
        this.preprocessorChoice = preprocessorChoice;
    }
    public void setIndexBuilderChoice(String indexBuilderChoice) {
        this.indexBuilderChoice = indexBuilderChoice;
    }
    public void setCandidateFinderChoice(String candidateFinderChoice) {
        this.candidateFinderChoice = candidateFinderChoice;
    }
    public void setNameComparatorChoice(String nameComparatorChoice) {
        this.nameComparatorChoice = nameComparatorChoice;
    }
    public void setResultThreshold(double resultThreshold) {
        this.resultThreshold = resultThreshold;
    }
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
    public void setThresholdMode(boolean isThresholdMode) {
        this.isThresholdMode = isThresholdMode;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "preprocessor='" + preprocessorChoice + '\'' +
                ", indexBuilder='" + indexBuilderChoice + '\'' +
                ", candidateFinder='" + candidateFinderChoice + '\'' +
                ", nameComparator='" + nameComparatorChoice + '\'' +
                ", threshold=" + resultThreshold +
                ", maxResults=" + maxResults +
                ", thresholdMode=" + isThresholdMode +
                '}';
    }
}
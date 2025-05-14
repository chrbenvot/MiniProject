package com.info2.miniprojet;

import java.io.IOException;
import java.util.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.info2.miniprojet.cli.CliHandler;
import com.info2.miniprojet.config.Configuration;
import com.info2.miniprojet.core.Engine;
import com.info2.miniprojet.core.Name;
import com.info2.miniprojet.factory.StrategyFactory; // Static methods will be used
import com.info2.miniprojet.preprocessing.Preprocessor;
import com.info2.miniprojet.data.*;
import com.info2.miniprojet.data.impl.*;


public class MiniProject {
    private CliHandler cliHandler;
    private Engine engine;
    private Configuration currentConfig;
    private final String configFilePath;
    private Map<String, List<Name>> dataCache;

    public MiniProject() {
        this.configFilePath = "app_config.properties";
        this.currentConfig = loadConfig();
        this.engine = new Engine();
        this.cliHandler = new CliHandler(engine, this);
        this.dataCache = new HashMap<>();
    }


    public void start() {
        cliHandler.startMainMenuLoop();
    }

    // --- Configuration Management Methods (Instance Methods) ---

    public void setPreprocessorChoice(String preprocessorChoice) {
        if (this.currentConfig.getPreprocessorChoice() == null ||
                !this.currentConfig.getPreprocessorChoice().equals(preprocessorChoice)) {
            System.out.println("MiniProject: Preprocessor choice changed. Clearing data cache.");
            this.dataCache.clear();
        }
        this.currentConfig.setPreprocessorChoice(preprocessorChoice);
        saveConfig(); // Save after modification
    }

    public void setCandidateFinderChoice(String candidateFinderChoice) {
        this.currentConfig.setCandidateFinderChoice(candidateFinderChoice);
        saveConfig();
    }

    public void setStringComparatorForNameCompChoice(String choice){
        this.currentConfig.setStringComparatorForNameCompChoice(choice);
        saveConfig();
    }
    public void setNameComparatorChoice(String nameComparatorChoice) {
        this.currentConfig.setNameComparatorChoice(nameComparatorChoice);
        saveConfig();
    }

    public void setResultMode(boolean isThresholdMode) {
        this.currentConfig.setThresholdMode(isThresholdMode);
        saveConfig();
    }

    public void setResultThreshold(double threshold) {
        this.currentConfig.setResultThreshold(threshold);
        this.currentConfig.setThresholdMode(true); // Assume setting threshold means threshold mode
        saveConfig();
    }

    public void setMaxResults(int maxResults) {
        this.currentConfig.setMaxResults(maxResults);
        this.currentConfig.setThresholdMode(false); // Assume setting max results means !threshold mode
        saveConfig();
    }

    public Configuration getCurrentConfig() {
        return this.currentConfig;
    }


    public List<Name> loadAndPreprocessData(DataProvider dataProvider) throws IOException, InterruptedException {
        String cacheKey = null;

        // Determine cache key based on DataProvider type
        if (dataProvider instanceof LocalFileProvider) {
            cacheKey = ((LocalFileProvider) dataProvider).getFilePath();
        } else if (dataProvider instanceof UrlDataProvider) {
            cacheKey = ((UrlDataProvider) dataProvider).getUrlString();
        }
        // We will NOT cache CliInputProvider data as it's unique each time.

        // --- Cache Check ---
        if (cacheKey != null && this.dataCache.containsKey(cacheKey)) {
            System.out.println("MiniProject: Returning cached and preprocessed data for: " + cacheKey);
            return this.dataCache.get(cacheKey);
        }

        System.out.println("MiniProject: Starting fresh data load and preprocess using: " + dataProvider.getClass().getSimpleName() + (cacheKey != null ? " (" + cacheKey + ")" : " (Manual Input)"));

        String preprocessorChoice = this.currentConfig.getPreprocessorChoice();
        Preprocessor preprocessor = StrategyFactory.createPreprocessor(preprocessorChoice);
        System.out.println("MiniProject: Preprocessing with " + preprocessor.getName());

        List<String> rawNames = dataProvider.loadRawLines();
        List<Name> processedNames = new ArrayList<>(rawNames.size());
        int lineNumber = 0;

        for (String line : rawNames) {
            lineNumber++;
            if (line == null || line.isBlank()) continue;

            String parsedId = null;
            String originalNameForRecord;
            String nameToProcess;
            String[] parts = line.split(",", 2);
            if (parts.length == 1) {
                nameToProcess = parts[0].trim();
                originalNameForRecord = nameToProcess;
            } else {
                String potentialId = parts[0].trim();
                nameToProcess = parts[1].trim();
                originalNameForRecord = nameToProcess;
                if (!potentialId.isBlank()) parsedId = potentialId;
            }
            if (nameToProcess.isEmpty()) continue;

            String finalId = (parsedId != null && !parsedId.isBlank()) ? parsedId : "L_" + lineNumber;
            List<String> processedTokens = preprocessor.preprocess(List.of(nameToProcess));
            Name nameObject = new Name(finalId, originalNameForRecord, processedTokens);
            processedNames.add(nameObject);
        }
        System.out.println("MiniProject: Finished loading and preprocessing. Created " + processedNames.size() + " Name objects.");

        // --- Store in Cache if a key was generated (i.e., not CliInputProvider) ---
        if (cacheKey != null) {
            this.dataCache.put(cacheKey, processedNames);
            System.out.println("MiniProject: Data for " + cacheKey + " stored in cache.");
        }

        return processedNames;
    }

    // --- Configuration Persistence (Instance Methods) ---

    private Configuration loadConfig() {
        Properties props = new Properties();
        Configuration config = createDefaultConfig(); // Start with defaults
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            props.load(fis);
            System.out.println("MiniProject: Loaded configuration from " + configFilePath);

            config.setPreprocessorChoice(props.getProperty("preprocessor", config.getPreprocessorChoice()));
            config.setCandidateFinderChoice(props.getProperty("candidateFinder", config.getCandidateFinderChoice()));
            config.setStringComparatorForNameCompChoice(props.getProperty("stringComparatorForNameComp", config.getStringComparatorForNameCompChoice()));
            config.setNameComparatorChoice(props.getProperty("nameComparator", config.getNameComparatorChoice()));
            config.setResultThreshold(Double.parseDouble(props.getProperty("resultThreshold", String.valueOf(config.getResultThreshold()))));
            config.setMaxResults(Integer.parseInt(props.getProperty("maxResults", String.valueOf(config.getMaxResults()))));
            config.setThresholdMode(Boolean.parseBoolean(props.getProperty("isThresholdMode", String.valueOf(config.isThresholdMode()))));

        } catch (IOException e) {
            System.out.println("MiniProject: Configuration file not found or error reading. Using default configuration.");
        } catch (NumberFormatException e) {
            System.err.println("Warning: Error parsing numeric value in config file. Using defaults for affected values.");
        }
        return config;
    }

    private void saveConfig() {
        Properties props = new Properties();
        props.setProperty("preprocessor", currentConfig.getPreprocessorChoice());
        props.setProperty("candidateFinder", currentConfig.getCandidateFinderChoice());
        props.setProperty("stringComparatorForNameComp", currentConfig.getStringComparatorForNameCompChoice());
        props.setProperty("nameComparator", currentConfig.getNameComparatorChoice());
        props.setProperty("resultThreshold", String.valueOf(currentConfig.getResultThreshold()));
        props.setProperty("maxResults", String.valueOf(currentConfig.getMaxResults()));
        props.setProperty("isThresholdMode", String.valueOf(currentConfig.isThresholdMode()));

        try (FileOutputStream fos = new FileOutputStream(configFilePath)) {
            props.store(fos, "Name Matcher Configuration");
            System.out.println("MiniProject: Configuration saved to " + configFilePath);
        } catch (IOException e) {
            System.err.println("Error saving configuration to " + configFilePath + ": " + e.getMessage());
        }
    }

    private Configuration createDefaultConfig() {
        Configuration config = new Configuration();
        config.setPreprocessorChoice("NOOP");
        config.setCandidateFinderChoice("CARTESIAN_FIND_ALL");
        config.setStringComparatorForNameCompChoice("EXACT_STRING");
        config.setNameComparatorChoice("PASS_THROUGH_NAME");
        config.setResultThreshold(0.85);
        config.setMaxResults(20);
        config.setThresholdMode(false);
        return config;
    }
    // --- MiniProject Entry Point ---
    public static void main(String[] args) {
        System.out.println("Application starting...");
        MiniProject app = new MiniProject();
        app.start();
        System.out.println("Application finished.");
    }

    public Engine getEngine() {
        return this.engine;
    }

}
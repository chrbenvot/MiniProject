package com.info2.miniprojet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.info2.miniprojet.cli.CliHandler;
import com.info2.miniprojet.config.Configuration;
import com.info2.miniprojet.core.Engine;


public class MiniProject {
    private CliHandler cliHandler;
    private Engine engine;
    private Configuration currentConfig; // Non-static
    private final String configFilePath; // Final after constructor

    // Constructor: Initializes components and loads config
    public MiniProject() {
        this.configFilePath = "app_config.properties"; // Example file name
        this.currentConfig = loadConfig(); // Load from file or defaults
        this.engine = new Engine();
        // Pass the Engine instance and THIS MiniProject instance (for config access) to CliHandler
        this.cliHandler = new CliHandler(engine, this);
    }

    // Starts the application loop
    public void start() {
        cliHandler.startMainMenuLoop();
    }

    // --- Configuration Management Methods (Instance Methods) ---

    public void setPreprocessorChoice(String preprocessorChoice) {
        this.currentConfig.setPreprocessorChoice(preprocessorChoice);
        saveConfig(); // Save after modification
    }

    public void setIndexBuilderChoice(String indexBuilderChoice) {
        this.currentConfig.setIndexBuilderChoice(indexBuilderChoice);
        saveConfig();
    }

    public void setCandidateFinderChoice(String candidateFinderChoice) {
        this.currentConfig.setCandidateFinderChoice(candidateFinderChoice);
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

    public List<Name> loadAndPreprocessData(String pathOrUrl) throws IOException, InterruptedException{ //TODO: should this even be heere and not in engine

    }
    // --- Data Loading Method (Instance Method) ---

    // Include InterruptedException since we'll probably use HttpClient
    public List<String> loadRawData(String pathOrUrl) throws IOException , InterruptedException {
        System.out.println("MiniProject: Loading data from " + pathOrUrl); // Debugging line
        if (pathOrUrl == null || pathOrUrl.trim().isEmpty()) {
            throw new IOException("Path or URL cannot be empty.");
        }

        if (pathOrUrl.toLowerCase().startsWith("http://") || pathOrUrl.toLowerCase().startsWith("https://")) {
            // Keep InterruptedException if loadFromUrl signature requires it,
            // otherwise remove it from this method's throws clause too.
            return loadFromUrl(pathOrUrl);
        } else {
            return loadFromFile(pathOrUrl);
        }
    }

    private List<String> loadFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath); // Use Paths.get
        try {
            System.out.println("MiniProject: Reading from file " + path.toAbsolutePath());
            List<String> lines = Files.readAllLines(path); // Assumes default charset (UTF-8 usually)
            System.out.println("MiniProject: Successfully read " + lines.size() + " lines from file.");
            return lines;
        } catch (NoSuchFileException e) {
            System.err.println("Error: File not found at " + path.toAbsolutePath());
            throw e; // Re-throw specific exception
        } catch (IOException e) {
            System.err.println("Error reading file " + path.toAbsolutePath() + ": " + e.getMessage());
            throw e; // Re-throw general IO exception
        }
    }

    // Emptied URL loading method
    // Remove InterruptedException if it's not expected to be thrown by future implementations
    private List<String> loadFromUrl(String urlString) throws IOException /*, InterruptedException */ {
        System.out.println("MiniProject: URL loading requested for: " + urlString);
        // Implementation removed - Placeholder
        throw new UnsupportedOperationException("URL loading not yet implemented.");
        // return new ArrayList<>(); // Alternative: return empty list instead of throwing
    }


    // --- Configuration Persistence (Instance Methods) ---

    private Configuration loadConfig() {
        Properties props = new Properties();
        Configuration config = createDefaultConfig(); // Start with defaults
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            props.load(fis);
            System.out.println("MiniProject: Loaded configuration from " + configFilePath);

            // Load values, falling back to defaults if property is missing
            config.setPreprocessorChoice(props.getProperty("preprocessor", config.getPreprocessorChoice()));
            config.setIndexBuilderChoice(props.getProperty("indexBuilder", config.getIndexBuilderChoice()));
            config.setCandidateFinderChoice(props.getProperty("candidateFinder", config.getCandidateFinderChoice()));
            config.setNameComparatorChoice(props.getProperty("nameComparator", config.getNameComparatorChoice()));
            config.setResultThreshold(Double.parseDouble(props.getProperty("resultThreshold", String.valueOf(config.getResultThreshold()))));
            config.setMaxResults(Integer.parseInt(props.getProperty("maxResults", String.valueOf(config.getMaxResults()))));
            config.setThresholdMode(Boolean.parseBoolean(props.getProperty("isThresholdMode", String.valueOf(config.isThresholdMode()))));

        } catch (IOException e) {
            System.out.println("MiniProject: Configuration file not found or error reading. Using default configuration.");
            // File not found is expected on first run, just use defaults.
        } catch (NumberFormatException e) {
            System.err.println("Warning: Error parsing numeric value in config file. Using defaults for affected values.");
            // If parsing fails, defaults are already set
        }
        return config;
    }

    private void saveConfig() {
        Properties props = new Properties();
        // Store current config values into properties
        props.setProperty("preprocessor", currentConfig.getPreprocessorChoice());
        props.setProperty("indexBuilder", currentConfig.getIndexBuilderChoice());
        props.setProperty("candidateFinder", currentConfig.getCandidateFinderChoice());
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

    // Helper to create default configuration object
    private Configuration createDefaultConfig() {
        Configuration config = new Configuration();
        // Set initial defaults - use keys that match your lazy implementations
        config.setPreprocessorChoice("NOOP");
        config.setIndexBuilderChoice("NOOP_BUILDER");
        config.setCandidateFinderChoice("FIND_ALL");
        config.setNameComparatorChoice("PASS_THROUGH_NAME"); //
        config.setResultThreshold(0.85); // Default threshold
        config.setMaxResults(20); // Default max results
        config.setThresholdMode(false); // Default to Max Results mode
        return config;
    }

    // --- MiniProject Entry Point ---
    public static void main(String[] args) {
        System.out.println("Application starting...");
        MiniProject app = new MiniProject(); // Create instance
        app.start(); // Call instance method
        System.out.println("Application finished."); // Will only be reached if startMainMenuLoop exits normally
    }
    public Engine getEngine() {
        return this.engine;
    }
}
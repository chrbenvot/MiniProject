package com.info2.miniprojet.cli;

import com.info2.miniprojet.MiniProject;
import com.info2.miniprojet.core.Engine;
import com.info2.miniprojet.core.ComparisonResult; // Need this DTO

import java.util.Scanner;
import java.util.List;
import java.io.IOException; // Needed for loadData exception

public class CliHandler {
    private Scanner scanner;
    private Engine engine;
    private MiniProject app; // Reference to MiniProject to access config methods and loadData

    public CliHandler(Engine engine, MiniProject app) {
        this.scanner = new Scanner(System.in);
        this.engine = engine;
        this.app = app;
    }

    public void startMainMenuLoop() {
        while (true) {
            displayMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleSearch();
                    break;
                case "2":
                    handleCompare();
                    break;
                case "3":
                    handleDeduplicate();
                    break;
                case "4":
                    handleConfiguration();
                    break;
                case "5":
                    System.out.println("Exiting...");
                    scanner.close(); // Close scanner on exit
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void displayMainMenu() {
        System.out.println("\n=== Name Matcher Application ===");
        System.out.println("1. Search names");
        System.out.println("2. Compare two lists");
        System.out.println("3. Deduplicate a list");
        System.out.println("4. Configuration");
        System.out.println("5. Exit");
        System.out.print("Enter your choice: ");
    }

    private void handleSearch() {
        System.out.println("\n=== Name Search ===");
        String name = getInput("Enter name to search: ");
        String filePath = getFilePathOrUrlInput("Enter file path or URL to search in: ");
        try {
            // Load data using MiniProject's loadData method
            List<String> namesList = loadData(filePath);
            if (namesList != null) {
                System.out.println("CLI: Data loaded. Calling engine...");
                // Call engine with loaded data and current config from MiniProject
                List<ComparisonResult> results = engine.performSearch(name, namesList, app.getCurrentConfig());
                displayResults(results); // Display results
            }
        } catch (IOException | InterruptedException e) { // Catch potential exceptions from loadData
            System.err.println("Error loading data for search from " + filePath + ": " + e.getMessage());
        } catch (Exception e) { // Catch unexpected errors from engine etc.
            System.err.println("An unexpected error occurred during search: " + e.getMessage());
        }
    }

    private void handleCompare() {
        System.out.println("\n=== Name List Comparison ===");
        String filePath1 = getFilePathOrUrlInput("Enter first file path or URL: ");
        String filePath2 = getFilePathOrUrlInput("Enter second file path or URL: ");
        try {
            List<String> list1 = loadData(filePath1);
            List<String> list2 = loadData(filePath2);
            if (list1 != null && list2 != null) {
                System.out.println("CLI: Data loaded. Calling engine...");
                List<ComparisonResult> results = engine.performComparison(list1, list2, app.getCurrentConfig());
                displayResults(results);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error loading data for comparison: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during comparison: " + e.getMessage());
        }
    }

    private void handleDeduplicate() {
        System.out.println("\n=== Name List Deduplication ===");
        String filePath = getFilePathOrUrlInput("Enter file path or URL to deduplicate: ");
        try {
            List<String> namesList = loadData(filePath);
            if (namesList != null) {
                System.out.println("CLI: Data loaded. Calling engine...");
                List<ComparisonResult> results = engine.performDeduplication(namesList, app.getCurrentConfig());
                displayResults(results);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error loading data for deduplication from " + filePath + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during deduplication: " + e.getMessage());
        }
    }

    private void handleConfiguration() {
        System.out.println("\n=== Configuration ===");
        // Display current config first
        System.out.println("Current Config: " + app.getCurrentConfig()); // !! Configuration has toString()

        System.out.println("1. Choose Preprocessor");
        System.out.println("2. Choose Index Builder");
        System.out.println("3. Choose Candidate Finder");
        System.out.println("4. Choose Name Comparator");
        System.out.println("5. Set Result Filter (Threshold/Max Count)");
        System.out.println("6. Back to MiniProject Menu");
        System.out.print("Enter your choice: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                // TODO: list choices
                String preprocessorChoice = getInput("Enter Preprocessor identifier (e.g., NOOP): "); //so,could be changed to a number instead
                app.setPreprocessorChoice(preprocessorChoice); // Use MiniProject's setter
                System.out.println("Preprocessor choice set to: " + preprocessorChoice);
                break;
            case "2":
                String indexBuilderChoice = getInput("Enter Index Builder identifier (e.g., NOOP_BUILDER): ");
                app.setIndexBuilderChoice(indexBuilderChoice);
                System.out.println("Index Builder choice set to: " + indexBuilderChoice);
                break;
            case "3":
                String candidateFinderChoice = getInput("Enter Candidate Finder identifier (e.g., FIND_ALL): ");
                app.setCandidateFinderChoice(candidateFinderChoice);
                System.out.println("Candidate Finder choice set to: " + candidateFinderChoice);
                break;
            case "4":
                String nameComparatorChoice = getInput("Enter Name Comparator identifier (e.g., PASS_THROUGH_NAME): ");
                app.setNameComparatorChoice(nameComparatorChoice);
                System.out.println("Name Comparator choice set to: " + nameComparatorChoice);
                break;
            case "5":
                configureResultFilter();
                break;
            case "6":
                System.out.println("Returning to main menu...");
                return; // Go back
            default:
                System.out.println("Invalid configuration choice.");
        }
        // handleConfiguration(); // IF we want the menu to be recursive(set multiple options THEN quit with 6)
    }

    // Helper method for Case 5 in handleConfiguration
    private void configureResultFilter() {
        System.out.println("\n--- Set Result Filter ---");
        System.out.println("Filter results by: (1) Threshold or (2) Max Count?");
        String modeChoice = getInput("Enter choice (1 or 2): ");

        if ("1".equals(modeChoice)) {
            try {
                double threshold = Double.parseDouble(getInput("Enter threshold value (e.g., 0.85): "));
                app.setResultThreshold(threshold); // Sets value and mode via MiniProject's setter
                System.out.println("Result filtering set to threshold: " + threshold);
            } catch (NumberFormatException e) {
                System.err.println("Invalid number format for threshold.");
            }
        } else if ("2".equals(modeChoice)) {
            try {
                int maxResults = Integer.parseInt(getInput("Enter max number of results: "));
                app.setMaxResults(maxResults); // Sets value and mode via MiniProject's setter
                System.out.println("Result filtering set to max results: " + maxResults);
            } catch (NumberFormatException e) {
                System.err.println("Invalid number format for max results.");
            }
        } else {
            System.out.println("Invalid mode choice.");
        }
    }

    private void displayResults(List<ComparisonResult> results) {
        if (results == null || results.isEmpty()) {
            System.out.println("\n--- No matches found or operation yielded no results. ---");
            return;
        }
        System.out.println("\n=== Results (" + results.size() + " found) ===");
        int count = 0;
        final int MAX_DISPLAY = 50; // Limit display count(we're working on a CLI with a buffer(for now))

        for (ComparisonResult result : results) {
            System.out.println(result); // Uses ComparisonResult.toString()
            count++;
            if (count >= MAX_DISPLAY) {
                System.out.println("... (display limited to first " + MAX_DISPLAY + " results)");
                break;
            }
        }
        System.out.println("--- End of Results ---");
    }

    private String getFilePathOrUrlInput(String prompt) {
        // TODO:validation
        return getInput(prompt);
    }

    private String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    // TODO: change this when we actually add url class
    private List<String> loadData(String pathOrUrl) throws IOException, InterruptedException {
        // Delegate loading to the MiniProject instance
        return app.loadData(pathOrUrl);
    }
}
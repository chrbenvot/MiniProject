package com.info2.miniprojet.cli;

import com.info2.miniprojet.MiniProject; // Your main application class
import com.info2.miniprojet.config.Configuration; // To get config for engine
import com.info2.miniprojet.core.Engine;
import com.info2.miniprojet.core.ComparisonResult;
import com.info2.miniprojet.core.Name;
import com.info2.miniprojet.data.DataProvider; // DataProvider interface
import com.info2.miniprojet.factory.DataProviderFactory; // To create DataProvider instances
import com.info2.miniprojet.factory.StrategyFactory;   // To get available strategy choices

import java.util.Scanner;
import java.util.List;
import java.io.IOException;
import java.util.Arrays; // For checking if a choice needs a string comparator

public class CliHandler {
    private Scanner scanner;
    private final Engine engine;
    private final MiniProject app; // Reference to MiniProject

    // Helper list of NameComparator types that require an internal StringComparator
    private static final List<String> NAME_COMPS_REQUIRING_STRING_COMP = Arrays.asList(
            "PASS_THROUGH_NAME",
            "POSITIONAL_WEIGHTED",
            "BAG_OF_WORDS"
            // Add other NameComparator factory keys here if they use a StringComparator
    );


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
                    System.out.println("Exiting application...");
                    scanner.close();
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
        String queryName = getInput("Enter name to search: ");
        if (queryName.isEmpty()) {
            System.err.println("Error: Query name cannot be empty.");
            return;
        }

        DataProvider listProvider = getDataProvider("Enter data source for list (File path/URL or 'MANUAL'): ");
        if (listProvider == null) return;

        try {
            System.out.println("CLI: Loading and preprocessing data for search...");
            List<Name> namesList = app.loadAndPreprocessData(listProvider);
            if (namesList != null) {
                System.out.println("CLI: Data loaded ("+ namesList.size() + " names). Calling engine...");
                List<ComparisonResult> results = engine.performSearch(queryName, namesList, app.getCurrentConfig());
                displayResults(results);
            } else {
                System.err.println("CLI: Failed to load or preprocess data.");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during search data processing: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleCompare() {
        System.out.println("\n=== Name List Comparison ===");
        System.out.println("Enter data source for first list:");
        DataProvider provider1 = getDataProvider("Data source 1 (File path/URL or 'MANUAL'): ");
        if (provider1 == null) return;

        System.out.println("\nEnter data source for second list:");
        DataProvider provider2 = getDataProvider("Data source 2 (File path/URL or 'MANUAL'): ");
        if (provider2 == null) return;

        try {
            System.out.println("CLI: Loading and preprocessing list 1...");
            List<Name> list1 = app.loadAndPreprocessData(provider1);
            System.out.println("CLI: Loading and preprocessing list 2...");
            List<Name> list2 = app.loadAndPreprocessData(provider2);

            if (list1 != null && list2 != null) {
                System.out.println("CLI: Data loaded. Calling engine for comparison...");
                List<ComparisonResult> results = engine.performComparison(list1, list2, app.getCurrentConfig());
                displayResults(results);
            } else {
                System.err.println("CLI: Failed to load or preprocess one or both lists.");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during comparison data processing: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during comparison: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeduplicate() {
        System.out.println("\n=== Name List Deduplication ===");
        DataProvider listProvider = getDataProvider("Enter data source for list to deduplicate (File path/URL or 'MANUAL'): ");
        if (listProvider == null) return;

        try {
            System.out.println("CLI: Loading and preprocessing data for deduplication...");
            List<Name> namesList = app.loadAndPreprocessData(listProvider);
            if (namesList != null) {
                System.out.println("CLI: Data loaded. Calling engine for deduplication...");
                List<ComparisonResult> results = engine.performDeduplication(namesList, app.getCurrentConfig());
                displayResults(results);
            } else {
                System.err.println("CLI: Failed to load or preprocess data.");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error during deduplication data processing: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during deduplication: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleConfiguration() {
        boolean stayInConfigMenu = true;
        while(stayInConfigMenu) {
            System.out.println("\n=== Configuration Menu ===");
            System.out.println("Current Config:\n" + app.getCurrentConfig().toString()); // Display current settings
            System.out.println("1. Choose Preprocessor");
            System.out.println("2. Choose Candidate Finder");
            System.out.println("3. Choose Name Comparator (and its internal String Comparator if applicable)");
            System.out.println("4. Set Result Filter (Threshold/Max Count)");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    listAndSetStrategy("Preprocessor", null); // No secondary choice needed
                    break;
                case "2":
                    listAndSetStrategy("CandidateFinder", null); // No secondary choice needed
                    break;
                case "3":
                    // For NameComparator, we first set the NameComparator type,
                    // then potentially set the StringComparator it uses internally.
                    listAndSetStrategy("NameComparator", "StringComparatorForNameComp");
                    break;
                case "4":
                    configureResultFilter();
                    break;
                case "5":
                    System.out.println("Returning to main menu...");
                    stayInConfigMenu = false;
                    break;
                default:
                    System.out.println("Invalid configuration choice. Please try again.");
            }
        }
    }

    private void listAndSetStrategy(String strategyType, String secondaryStrategyType) {
        List<String> choices;
        String currentPrimaryChoice = "";
        Configuration currentFullConfig = app.getCurrentConfig();

        System.out.println("\n--- Choose " + strategyType + " ---");

        switch (strategyType) {
            case "Preprocessor":
                choices = StrategyFactory.getAvailablePreprocessorChoices();
                currentPrimaryChoice = currentFullConfig.getPreprocessorChoice();
                break;
            case "CandidateFinder":
                choices = StrategyFactory.getAvailableCandidateFinderChoices();
                currentPrimaryChoice = currentFullConfig.getCandidateFinderChoice();
                break;
            case "NameComparator":
                choices = StrategyFactory.getAvailableNameComparatorChoices();
                currentPrimaryChoice = currentFullConfig.getNameComparatorChoice();
                break;
            default:
                System.out.println("Invalid primary strategy type for listing: " + strategyType);
                return;
        }

        System.out.println("Currently selected " + strategyType + ": " + currentPrimaryChoice);
        for (int i = 0; i < choices.size(); i++) {
            System.out.println((i + 1) + ". " + choices.get(i));
        }
        System.out.println((choices.size() + 1) + ". Cancel");

        try {
            int choiceNum = Integer.parseInt(getInput("Enter choice number for " + strategyType + ": "));
            if (choiceNum > 0 && choiceNum <= choices.size()) {
                String selectedPrimaryChoice = choices.get(choiceNum - 1);
                // Set the primary strategy choice
                switch (strategyType) {
                    case "Preprocessor": app.setPreprocessorChoice(selectedPrimaryChoice); break;
                    case "CandidateFinder": app.setCandidateFinderChoice(selectedPrimaryChoice); break;
                    case "NameComparator": app.setNameComparatorChoice(selectedPrimaryChoice); break;
                }
                System.out.println(strategyType + " choice set to: " + selectedPrimaryChoice);

                // If a secondary strategy type is specified AND the primary choice needs it
                if (secondaryStrategyType != null && strategyType.equals("NameComparator") &&
                        NAME_COMPS_REQUIRING_STRING_COMP.contains(selectedPrimaryChoice.toUpperCase())) {

                    System.out.println("\n--- This Name Comparator uses an internal String Comparator ---");
                    List<String> secondaryChoices = StrategyFactory.getAvailableStringComparatorChoices();
                    String currentSecondaryChoice = currentFullConfig.getStringComparatorForNameCompChoice();

                    System.out.println("Currently selected internal " + secondaryStrategyType + ": " + currentSecondaryChoice);
                    for (int i = 0; i < secondaryChoices.size(); i++) {
                        System.out.println((i + 1) + ". " + secondaryChoices.get(i));
                    }
                    System.out.println((secondaryChoices.size() + 1) + ". Cancel / Keep Current");

                    try {
                        int secondaryChoiceNum = Integer.parseInt(getInput("Select internal " + secondaryStrategyType + " (number): "));
                        if (secondaryChoiceNum > 0 && secondaryChoiceNum <= secondaryChoices.size()) {
                            String selectedSecondaryChoice = secondaryChoices.get(secondaryChoiceNum - 1);
                            // Set the secondary strategy choice (e.g., StringComparatorForNameComp)
                            app.setStringComparatorForNameCompChoice(selectedSecondaryChoice);
                            System.out.println("Internal " + secondaryStrategyType + " set to: " + selectedSecondaryChoice);
                        } else if (secondaryChoiceNum == secondaryChoices.size() + 1) {
                            System.out.println("Internal " + secondaryStrategyType + " selection cancelled/kept current.");
                        }else {
                            System.out.println("Invalid number for internal " + secondaryStrategyType + ".");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input for internal " + secondaryStrategyType + ". Please enter a number.");
                    }
                }

            } else if (choiceNum == choices.size() + 1) {
                System.out.println(strategyType + " selection cancelled.");
            } else {
                System.out.println("Invalid number selected for " + strategyType + ".");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input for " + strategyType + ". Please enter a number.");
        }
    }


    private void configureResultFilter() {
        // ... (this method is already good) ...
        System.out.println("\n--- Set Result Filter ---");
        System.out.println("Filter results by: (1) Threshold or (2) Max Count?");
        String modeChoice = getInput("Enter choice (1 or 2): ");

        if ("1".equals(modeChoice)) {
            try {
                double threshold = Double.parseDouble(getInput("Enter threshold value (e.g., 0.85): "));
                app.setResultThreshold(threshold);
                System.out.println("Result filtering set to threshold: " + threshold);
            } catch (NumberFormatException e) {
                System.err.println("Invalid number format for threshold.");
            }
        } else if ("2".equals(modeChoice)) {
            try {
                int maxResults = Integer.parseInt(getInput("Enter max number of results: "));
                app.setMaxResults(maxResults);
                System.out.println("Result filtering set to max results: " + maxResults);
            } catch (NumberFormatException e) {
                System.err.println("Invalid number format for max results.");
            }
        } else {
            System.out.println("Invalid mode choice.");
        }
    }

    private void displayResults(List<ComparisonResult> results) {
        // ... (this method is already good) ...
        if (results == null || results.isEmpty()) {
            System.out.println("\n--- No matches found or operation yielded no results. ---");
            return;
        }
        System.out.println("\n=== Results (" + results.size() + " found) ===");
        int count = 0;
        final int MAX_DISPLAY = 50;

        for (ComparisonResult result : results) {
            System.out.println(result);
            count++;
            if (count >= MAX_DISPLAY && results.size() > MAX_DISPLAY) {
                System.out.println("... (display limited to first " + MAX_DISPLAY + " results of " + results.size() + ")");
                break;
            }
        }
        System.out.println("--- End of Results ---");
    }

    private DataProvider getDataProvider(String prompt) {
        String input = getInput(prompt);
        return DataProviderFactory.createDataProvider(input, this.scanner);
    }

    private String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
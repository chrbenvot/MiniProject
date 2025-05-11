package com.info2.miniprojet.gui;

import com.info2.miniprojet.MiniProject;
import com.info2.miniprojet.config.Configuration;
import com.info2.miniprojet.core.ComparisonResult;
import com.info2.miniprojet.core.Name; // Assuming Name is used by backendApp

import com.info2.miniprojet.factory.DataProviderFactory;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button; // Added
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox; // Added for progressBox
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MainViewController {

    @FXML private TextField queryNameField;
    @FXML private TextField filePathField;
    @FXML private Button browseButton;
    @FXML private Button searchButton;
    @FXML private ListView<String> resultsListView;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator activityIndicator;
    @FXML private Label progressLabel;
    @FXML private HBox progressBox;
    // Add these to your existing @FXML declarations
    @FXML private TextField compareFilePath1Field;
    @FXML private Button browseButtonCompare1;
    @FXML private TextField compareFilePath2Field;
    @FXML private Button browseButtonCompare2;
    @FXML private Button compareButton;

    @FXML private TextField dedupeFilePathField;
    @FXML private Button browseButtonDedupe;
    @FXML private Button deduplicateButton;

    @FXML private Button configureButton;


    private MiniProject backendApp;

    public void setBackendApp(MiniProject backendApp) {
        this.backendApp = backendApp;
        if (this.backendApp != null) {
            updateStatus("Status: Backend connected. Ready.");
        } else {
            updateStatus("Status: Error - Backend not connected.");
        }
    }

    @FXML
    public void initialize() {
        // Hide progress bar initially
        if (progressBox != null) {
            progressBox.setVisible(false);
        }
        System.out.println("GUI: MainViewController initialized.");
    }


    @FXML
    private void browseForFileAction() { // Renamed to match FXML
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Data File");
        Stage stage = (Stage) filePathField.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
            updateStatus("Status: File selected: " + selectedFile.getName());
        } else {
            updateStatus("Status: File selection cancelled.");
        }
    }

    @FXML
    private void performSearchAction() {
        if (backendApp == null) {
            updateStatus("Error: Backend not available.");
            return;
        }

        final String queryName = queryNameField.getText();
        final String filePath = filePathField.getText();

        if (queryName.trim().isEmpty()) {
            updateStatus("Error: Query name cannot be empty.");
            return;
        }
        if (filePath.trim().isEmpty()) {
            updateStatus("Error: File path cannot be empty.");
            return;
        }

        resultsListView.getItems().clear();
        if (progressBox != null) progressBox.setVisible(true);
        updateStatus("Status: Starting search for '" + queryName + "' in " + filePath + "...");
        searchButton.setDisable(true); // Disable button during search

        Task<List<ComparisonResult>> searchTask = new Task<>() {
            @Override
            protected List<ComparisonResult> call() throws Exception {
                // This code runs on a background thread
                Platform.runLater(() -> updateStatus("Status: Loading data... (" + filePath + ")"));
                List<Name> namesList = backendApp.loadAndPreprocessData(DataProviderFactory.createDataProvider(filePath, null)); // Pass null for scanner if not CLI

                Configuration config = backendApp.getCurrentConfig();

                Platform.runLater(() -> updateStatus("Status: Data loaded (" + (namesList != null ? namesList.size() : 0) + " names). Searching..."));
                if (namesList == null) { // Handle case where loadAndPreprocessData might return null
                    throw new IOException("Failed to load or preprocess data list.");
                }
                List<ComparisonResult> results = backendApp.getEngine().performSearch(queryName, namesList, config);
                return results;
            }
        };

        searchTask.setOnSucceeded(event -> {
            List<ComparisonResult> results = searchTask.getValue();
            displayResults(results);
            updateStatus("Status: Search complete. Found " + (results != null ? results.size() : 0) + " results.");
            if (progressBox != null) progressBox.setVisible(false);
            searchButton.setDisable(false);
        });

        searchTask.setOnFailed(event -> {
            Throwable exception = searchTask.getException();
            updateStatus("Error during search: " + (exception != null ? exception.getMessage() : "Unknown error"));
            System.err.println("GUI: Background task failed:");
            if (exception != null) exception.printStackTrace();
            resultsListView.getItems().clear();
            if (progressBox != null) progressBox.setVisible(false);
            searchButton.setDisable(false);
        });

        Thread backgroundThread = new Thread(searchTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void displayResults(List<ComparisonResult> results) {
        resultsListView.getItems().clear();
        if (results == null || results.isEmpty()) {
            resultsListView.getItems().add("No matches found or operation failed.");
        } else {
            final int MAX_DISPLAY = 1000; // Or some other sensible limit for ListView
            int count = 0;
            for (ComparisonResult result : results) {
                resultsListView.getItems().add(result.toString());
                count++;
                if (count >= MAX_DISPLAY && results.size() > MAX_DISPLAY) {
                    resultsListView.getItems().add("... (display limited to first " + MAX_DISPLAY + " results of " + results.size() + ")");
                    break;
                }
            }
        }
    }

    private void updateStatus(String message) {
        if (Platform.isFxApplicationThread()) {
            statusLabel.setText(message);
        } else {
            Platform.runLater(() -> statusLabel.setText(message));
        }
        System.out.println("GUI Status: " + message);
    }
    // ... existing methods ...

    // --- Browse Button Handlers ---
    @FXML
    private void browseForFileSearchAction() {
        selectFileForField(filePathField, "Select Data File for Search");
    }

    @FXML
    private void browseForCompareFile1Action() {
        selectFileForField(compareFilePath1Field, "Select First File for Compare");
    }

    @FXML
    private void browseForCompareFile2Action() {
        selectFileForField(compareFilePath2Field, "Select Second File for Compare");
    }

    @FXML
    private void browseForDedupeFileAction() {
        selectFileForField(dedupeFilePathField, "Select File for Deduplication");
    }

    // Helper for browse buttons
    private void selectFileForField(TextField targetField, String dialogTitle) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(dialogTitle);
        Stage stage = (Stage) targetField.getScene().getWindow(); // Get stage from any node
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            targetField.setText(selectedFile.getAbsolutePath());
            updateStatus("Status: File selected: " + selectedFile.getName());
        } else {
            updateStatus("Status: File selection cancelled.");
        }
    }


    // --- Action Handlers for Compare, Deduplicate, Configure ---
    @FXML
    private void performCompareAction() {
        if (backendApp == null) {
            updateStatus("Error: Backend not available.");
            return;
        }

        final String filePath1 = compareFilePath1Field.getText();
        final String filePath2 = compareFilePath2Field.getText();

        if (filePath1.trim().isEmpty() || filePath2.trim().isEmpty()) {
            updateStatus("Error: Both file paths for comparison must be provided.");
            return;
        }

        resultsListView.getItems().clear();
        if (progressBox != null) progressBox.setVisible(true);
        updateStatus("Status: Starting comparison between " + filePath1 + " and " + filePath2 + "...");
        compareButton.setDisable(true);
        deduplicateButton.setDisable(true); // Disable other major action buttons
        searchButton.setDisable(true);


        Task<List<ComparisonResult>> compareTask = new Task<>() {
            @Override
            protected List<ComparisonResult> call() throws Exception {
                Platform.runLater(() -> updateStatus("Status: Loading and preprocessing list 1..."));
                List<Name> list1 = backendApp.loadAndPreprocessData(DataProviderFactory.createDataProvider(filePath1, null));

                Platform.runLater(() -> updateStatus("Status: Loading and preprocessing list 2..."));
                List<Name> list2 = backendApp.loadAndPreprocessData(DataProviderFactory.createDataProvider(filePath2, null));

                Configuration config = backendApp.getCurrentConfig();

                Platform.runLater(() -> updateStatus("Status: Data loaded. Performing comparison..."));
                if (list1 == null || list2 == null) {
                    throw new IOException("Failed to load or preprocess one or both lists for comparison.");
                }
                return backendApp.getEngine().performComparison(list1, list2, config);
            }
        };

        compareTask.setOnSucceeded(event -> {
            List<ComparisonResult> results = compareTask.getValue();
            displayResults(results);
            updateStatus("Status: Comparison complete. Found " + (results != null ? results.size() : 0) + " results.");
            if (progressBox != null) progressBox.setVisible(false);
            enableActionButtons();
        });

        compareTask.setOnFailed(event -> {
            Throwable exception = compareTask.getException();
            updateStatus("Error during comparison: " + (exception != null ? exception.getMessage() : "Unknown error"));
            if (exception != null) exception.printStackTrace();
            resultsListView.getItems().clear();
            if (progressBox != null) progressBox.setVisible(false);
            enableActionButtons();
        });

        new Thread(compareTask).start();
    }

    @FXML
    private void performDeduplicateAction() {
        if (backendApp == null) {
            updateStatus("Error: Backend not available.");
            return;
        }
        final String filePath = dedupeFilePathField.getText();
        if (filePath.trim().isEmpty()) {
            updateStatus("Error: File path for deduplication must be provided.");
            return;
        }

        resultsListView.getItems().clear();
        if (progressBox != null) progressBox.setVisible(true);
        updateStatus("Status: Starting deduplication for " + filePath + "...");
        deduplicateButton.setDisable(true);
        compareButton.setDisable(true);
        searchButton.setDisable(true);


        Task<List<ComparisonResult>> dedupeTask = new Task<>() {
            @Override
            protected List<ComparisonResult> call() throws Exception {
                Platform.runLater(() -> updateStatus("Status: Loading and preprocessing list for deduplication..."));
                List<Name> namesList = backendApp.loadAndPreprocessData(DataProviderFactory.createDataProvider(filePath, null));

                Configuration config = backendApp.getCurrentConfig();
                Platform.runLater(() -> updateStatus("Status: Data loaded. Performing deduplication..."));
                if (namesList == null) {
                    throw new IOException("Failed to load or preprocess data list for deduplication.");
                }
                return backendApp.getEngine().performDeduplication(namesList, config);
            }
        };

        dedupeTask.setOnSucceeded(event -> {
            List<ComparisonResult> results = dedupeTask.getValue();
            displayResults(results);
            updateStatus("Status: Deduplication complete. Found " + (results != null ? results.size() : 0) + " potential duplicate pairs.");
            if (progressBox != null) progressBox.setVisible(false);
            enableActionButtons();
        });

        dedupeTask.setOnFailed(event -> {
            Throwable exception = dedupeTask.getException();
            updateStatus("Error during deduplication: " + (exception != null ? exception.getMessage() : "Unknown error"));
            if (exception != null) exception.printStackTrace();
            resultsListView.getItems().clear();
            if (progressBox != null) progressBox.setVisible(false);
            enableActionButtons();
        });

        new Thread(dedupeTask).start();
    }

    private void enableActionButtons() {
        searchButton.setDisable(false);
        compareButton.setDisable(false);
        deduplicateButton.setDisable(false);
    }


    @FXML
    private void openConfigurationWindowAction() {
        if (backendApp == null) {
            updateStatus("Error: Backend not available to configure.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("config-view.fxml")); // Ensure FXML is in same package
            Parent configRoot = loader.load();

            ConfigViewController configController = loader.getController();
            if (configController != null) {
                configController.setBackendApp(this.backendApp); // Pass backend to config controller
            } else {
                System.err.println("GUI Error: ConfigViewController instance not found.");
                updateStatus("Error: Could not load configuration controller.");
                return;
            }

            Stage configStage = new Stage();
            configStage.setTitle("Configuration");
            configStage.initModality(Modality.APPLICATION_MODAL); // Block main window until this one is closed
            configStage.initOwner((Stage) statusLabel.getScene().getWindow()); // Set owner to main window

            Scene scene = new Scene(configRoot);
            configStage.setScene(scene);

            updateStatus("Status: Configuration window opened.");
            configStage.showAndWait(); // Show and wait for it to be closed

            updateStatus("Status: Configuration window closed. Settings might have changed.");
            // Optionally, you might want to refresh or indicate that config has changed
            // For example, if the main view displays current config choices.

        } catch (IOException e) {
            e.printStackTrace();
            updateStatus("Error: Could not open configuration window. " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            updateStatus("Error: Unexpected issue opening configuration window. " + e.getMessage());
        }
    }
}
package com.info2.miniprojet.gui;

import com.info2.miniprojet.MiniProject;
import com.info2.miniprojet.config.Configuration;
import com.info2.miniprojet.core.ComparisonResult;
import com.info2.miniprojet.core.Name; // Assuming Name is used by backendApp

import com.info2.miniprojet.factory.DataProviderFactory;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button; // Added
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox; // Added for progressBox
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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
}
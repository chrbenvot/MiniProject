package com.info2.miniprojet.gui;

import com.info2.miniprojet.MiniProject; // Your backend access class
import com.info2.miniprojet.config.Configuration; // To get config for engine
import com.info2.miniprojet.core.ComparisonResult; // DTO for results

import javafx.application.Platform; // Needed for runLater
import javafx.concurrent.Task; // Needed for background tasks
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator; // Optional: for visual feedback
import javafx.scene.control.TextField;
// import javafx.scene.control.TableView; // Uncomment if using TableView
// import javafx.scene.control.TableColumn; // Uncomment if using TableView
// import javafx.scene.control.cell.PropertyValueFactory; // Uncomment if using TableView
import javafx.stage.FileChooser;
import javafx.stage.Stage; // To get the window for FileChooser

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MainViewController {

    // Inject UI elements defined in FXML
    @FXML private TextField queryNameField;
    @FXML private TextField filePathField;
    @FXML private ListView<String> resultsListView; // Simple display using toString()
    @FXML private Label statusLabel;
    // Optional: Add a ProgressIndicator to your FXML and inject it
    // @FXML private ProgressIndicator activityIndicator;

    /* Uncomment if using TableView instead of ListView
    @FXML private TableView<ComparisonResult> resultsTableView;
    @FXML private TableColumn<ComparisonResult, String> colName1;
    @FXML private TableColumn<ComparisonResult, String> colName2;
    @FXML private TableColumn<ComparisonResult, Double> colScore;
    @FXML private TableColumn<ComparisonResult, String> colMeasure;
    */

    private MiniProject backendApp; // Reference to the backend logic

    // Method to inject the backend application instance
    public void setBackendApp(MiniProject backendApp) {
        this.backendApp = backendApp;
        if (this.backendApp != null) {
            updateStatus("Status: Backend connected. Ready.");
        } else {
            updateStatus("Status: Error - Backend not connected.");
        }
        // Initialize TableView columns if using TableView
        // initializeTableView(); // Uncomment if using TableView
    }

    // --- FXML Event Handlers ---

    @FXML
    private void browseForFile() {
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

        final String queryName = queryNameField.getText(); // Use final for lambda access
        final String filePath = filePathField.getText(); // Use final for lambda access

        if (queryName.trim().isEmpty()) {
            updateStatus("Error: Query name cannot be empty.");
            return;
        }
        if (filePath.trim().isEmpty()) {
            updateStatus("Error: File path cannot be empty.");
            return;
        }

        // Clear previous results and indicate activity
        resultsListView.getItems().clear();
        // if (activityIndicator != null) activityIndicator.setVisible(true); // Optional progress
        updateStatus("Status: Starting search for '" + queryName + "' in " + filePath + "...");

        // --- Create and Run Background Task ---
        Task<List<ComparisonResult>> searchTask = new Task<>() {
            @Override
            protected List<ComparisonResult> call() throws Exception {
                // This code runs on a background thread

                // 1. Load data (can throw IOException/InterruptedException)
                // Update progress/status from background thread (use Platform.runLater for UI updates)
                Platform.runLater(() -> updateStatus("Status: Loading data..."));
                List<String> namesList = backendApp.loadData(filePath); // This might block

                // 2. Get current config
                Configuration config = backendApp.getCurrentConfig();

                // Update status before potentially long engine call
                Platform.runLater(() -> updateStatus("Status: Data loaded (" + namesList.size() + " lines). Searching..."));

                // 3. Perform search (can throw Exception)
                // This is the main long-running operation
                List<ComparisonResult> results = backendApp.getEngine().performSearch(queryName, namesList, config);

                // Return the result to the onSucceeded method
                return results;
            }
        };

        // --- Handle Task Completion (on JavaFX Application Thread) ---

        searchTask.setOnSucceeded(event -> {
            List<ComparisonResult> results = searchTask.getValue();
            displayResults(results); // Update UI with results
            updateStatus("Status: Search complete. Found " + (results != null ? results.size() : 0) + " results.");
            // if (activityIndicator != null) activityIndicator.setVisible(false); // Hide progress
        });

        searchTask.setOnFailed(event -> {
            Throwable exception = searchTask.getException();
            updateStatus("Error during search: " + exception.getMessage());
            System.err.println("Background task failed:");
            exception.printStackTrace(); // Log the full error
            resultsListView.getItems().clear(); // Clear results on failure
            // if (activityIndicator != null) activityIndicator.setVisible(false); // Hide progress
        });

        // Optional: Bind progress indicator to task progress
        // if (activityIndicator != null) activityIndicator.progressProperty().bind(searchTask.progressProperty());

        // Start the background task
        Thread backgroundThread = new Thread(searchTask);
        backgroundThread.setDaemon(true); // Allows application to exit if only daemon threads are running
        backgroundThread.start();
    }

    // --- Helper Methods ---

    private void displayResults(List<ComparisonResult> results) {
        resultsListView.getItems().clear(); // Ensure list is clear before adding
        if (results == null || results.isEmpty()) {
            resultsListView.getItems().add("No matches found.");
        } else {
            for (ComparisonResult result : results) {
                resultsListView.getItems().add(result.toString());
            }
        }
        // Handle TableView update if using it
    }

    private void updateStatus(String message) {
        // This method might be called from background task via Platform.runLater
        // or directly from event handlers (already on FX thread)
        if (Platform.isFxApplicationThread()) {
            statusLabel.setText(message);
            System.out.println("GUI Status: " + message); // Also print to console
        } else {
            Platform.runLater(() -> {
                statusLabel.setText(message);
                System.out.println("GUI Status (from background): " + message);
            });
        }
    }

    // --- Methods for TableView (if used) ---
    /*
    private void initializeTableView() { ... }
    */

    // --- Optional FXML initialize method ---
    // @FXML
    // public void initialize() { ... }
}
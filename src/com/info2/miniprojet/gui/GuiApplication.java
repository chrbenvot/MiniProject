package com.info2.miniprojet.gui;

import com.info2.miniprojet.MiniProject; // Your backend main class
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class GuiApplication extends Application {

    private MiniProject backendApp; // Instance of your original MiniProject (Main) class

    @Override
    public void init() throws Exception {
        // This method is called before start().
        // Initialize the backend application logic here.
        // This ensures config is loaded, etc., before the UI attempts to use it.
        super.init(); // Good practice to call super.init()
        try {
            backendApp = new MiniProject();
            System.out.println("GUI: Backend MiniProject initialized in init().");
        } catch (Exception e) {
            System.err.println("GUI Error: Failed to initialize backend MiniProject in init(): " + e.getMessage());
            e.printStackTrace();
            // Optionally re-throw or handle critical failure to prevent UI from launching
            throw e;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        if (backendApp == null) {
            System.err.println("GUI Error: Backend was not initialized. Cannot start UI.");
            // Optionally show an error alert to the user
            return;
        }

        try {
            // Construct the path to the FXML file within the resources structure
            // If main-view.fxml is directly in com.info2.miniprojet.gui
            URL fxmlUrl = getClass().getResource("main-view.fxml"); // Relative to this class's package

            if (fxmlUrl == null) {
                // Fallback if it's at the root of resources (less common for package structure)
                // fxmlUrl = getClass().getClassLoader().getResource("main-view.fxml");
                // If still null, then the path is wrong or file missing.
                System.err.println("Cannot find FXML file: main-view.fxml. Please check path.");
                System.err.println("Attempted path relative to GuiApplication.class: " + getClass().getResource("main-view.fxml"));
                System.err.println("Attempted path relative to classpath root: " + getClass().getClassLoader().getResource("main-view.fxml"));
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Get the controller instance and pass the backend reference
            MainViewController controller = loader.getController();
            if (controller != null) {
                controller.setBackendApp(backendApp); // Inject backend dependency
                System.out.println("GUI: Backend reference passed to MainViewController.");
            } else {
                System.err.println("GUI Error: MainViewController instance not found after loading FXML. Check fx:controller in FXML.");
            }

            Scene scene = new Scene(root, 800, 650); // Adjusted height slightly
            primaryStage.setTitle("Name Matcher GUI");
            primaryStage.setScene(scene);
            primaryStage.show();
            System.out.println("GUI: Stage should be showing.");

        } catch (IOException e) {
            System.err.println("GUI Error: Failed to load FXML or start application: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) { // Catch any other unexpected errors during startup
            System.err.println("GUI Error: An unexpected error occurred during UI startup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        // Optional: Perform any cleanup when the application closes
        super.stop();
        System.out.println("GUI: Application stopping.");
    }

    public static void main(String[] args) {
        // This is the entry point for the JavaFX application
        System.out.println("GUI: Launching GuiApplication...");
        launch(args);
    }
}
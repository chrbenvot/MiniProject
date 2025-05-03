package com.info2.miniprojet.gui;

import com.info2.miniprojet.MiniProject;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;

public class GuiApplication extends Application {

    private MiniProject backendApp; // Instance of your original Main class

    @Override
    public void init() throws Exception {
        // Initialize the backend application logic here
        // This ensures config is loaded etc. before UI starts
        backendApp = new MiniProject();
        System.out.println("Backend initialized.");
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML file
            // Using getResource ensures it finds the file relative to the classpath
            URL fxmlUrl = getClass().getResource("/com/info2/miniprojet/gui/main-view.fxml");
            if (fxmlUrl == null) {
                System.err.println("Cannot find FXML file. Check path.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Get the controller instance and pass the backend reference
            MainViewController controller = loader.getController();
            if (controller != null) {
                controller.setBackendApp(backendApp); // Inject backend dependency
                System.out.println("Backend reference passed to controller.");
            } else {
                System.err.println("Controller instance not found after loading FXML.");
            }


            // Set up the scene and stage
            Scene scene = new Scene(root, 800, 600); // Adjust size as needed
            primaryStage.setTitle("Name Matcher GUI");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Failed to load FXML or start application:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during startup:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Entry point for the JavaFX application
        launch(args);
    }
}
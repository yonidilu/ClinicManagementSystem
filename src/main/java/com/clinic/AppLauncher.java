package com.clinic;

import com.clinic.model.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppLauncher extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Main Bootstrap Sequence: Initializing database infrastructure structures...");
            DatabaseManager.initializeDatabase();

            System.out.println("Main Bootstrap Sequence: Parsing landing resource layouts...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/choice-view.fxml"));
            Parent root = loader.load();

            Scene mainPortalScene = new Scene(root);
            primaryStage.setScene(mainPortalScene);
            primaryStage.setTitle("Clinic Portal Ecosystem - Core Subsystem Registry");

            // FIXED LIFECYCLE ORDER: Show the stage first, then force the OS window manager to maximize
            primaryStage.show();
            primaryStage.setMaximized(true);
            System.out.println("Main Bootstrap Sequence: Clinical workflow module deployed successfully.");

        } catch (Exception e) {
            System.err.println("FATAL ARCHITECTURE CRASH: System loader failed to initialize core runtime panels.");
            e.printStackTrace();
        }
    }
    /**
     * Unified Entry Core Hub Launcher
     */
    public static void main(String[] args) {
        // Direct execution routing wrapper to decouple JavaFX modules during runtime
        Application.launch(AppLauncher.class, args);
    }
}
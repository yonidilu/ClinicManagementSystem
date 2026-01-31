package com.clinic.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import com.clinic.model.DatabaseManager; // Make sure this matches your folder path

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Label roleLabel;

    private String targetRole = "Staff";

    public void setTargetRole(String role) {
        this.targetRole = role;
        if (roleLabel != null) roleLabel.setText(role + " Login");
    }

    // Inside your LoginController method that opens the main view

    @FXML
    private void handleLogin(ActionEvent event) {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        // 1. Ask the Database what role this user has
        String role = DatabaseManager.verifyUserRole(user, pass);

        // Inside LoginController.java handleLogin method

        if ("HR".equalsIgnoreCase(role)) {
            // Send HR users to their dashboard
            switchToScene(event, "/hr-dashboard-view.fxml", "HR Dashboard");
        } else if (role != null && role.equalsIgnoreCase("DOCTOR")) {
            // Send Doctors to the patient records (Main View)
            switchToDashboard(event, role);
        } else {
            // If DatabaseManager.verifyUserRole returns null, show error
            errorLabel.setText("Invalid credentials!");
        }
    }
    // A helper method to keep your code clean and dry
    private void switchToScene(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void switchToDashboard(ActionEvent event, String role) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-view.fxml"));
            Parent root = loader.load();

            // Get the controller for the main view and set the role
            MainController mainController = loader.getController();
            mainController.setRole(role);

            // Switch the stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Clinic Management System - " + role);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading main-view.fxml. Check your file path!");
            e.printStackTrace();
        }
    }

    @FXML // Fixes error in image_0801d4.png
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/choice-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 400));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadDashboard(ActionEvent event, boolean isAdmin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-view.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setAccessLevel(isAdmin);

            // Inside your login logic...
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));

        // FORCE MAXIMIZE AGAIN
            stage.setMaximized(true);

            stage.show();
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to load dashboard! Check method names.");
            e.printStackTrace();
        }
    }
    private void openDashboard() {
        try {
            // 1. Load the Main Dashboard FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-dashboard.fxml"));
            Parent root = loader.load();

            // 2. Get the current "Stage" (the window)
            Stage stage = (Stage) usernameField.getScene().getWindow();

            // 3. Set the new scene (the Dashboard)
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Clinic Management System - Dashboard");
            stage.setMaximized(true); // Optional: Make it full screen
            stage.show();

        } catch (IOException e) {
            System.err.println("Could not find the dashboard FXML file!");
            e.printStackTrace();
        }
    }
}
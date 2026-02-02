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
import com.clinic.model.DatabaseManager;

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

    @FXML
    private void handleLogin(ActionEvent event) {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        //Asks the Database what role this user has
        String role = DatabaseManager.verifyUserRole(user, pass);

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

            MainController mainController = loader.getController();
            mainController.setRole(role);

            // Switch the stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Clinic Management System - " + role);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading main-view.fxml. Check your file path!");
            e.printStackTrace();
        }
    }

    @FXML
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

            // Inside login logic...
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));

        // FORCE MAXIMIZE
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to load dashboard! Check method names.");
            e.printStackTrace();
        }
    }
    private void openDashboard() {
        try {
            //Load the Main Dashboard FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-dashboard.fxml"));
            Parent root = loader.load();

            //Get the current "Stage" (the window)
            Stage stage = (Stage) usernameField.getScene().getWindow();

            //Set the new scene (the Dashboard)
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Clinic Management System - Dashboard");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("Could not find the dashboard FXML file!");
            e.printStackTrace();
        }
    }
}
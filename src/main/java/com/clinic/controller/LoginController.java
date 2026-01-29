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
        // 1. Get the text from your UI fields
        String user = usernameField.getText();
        String pass = passwordField.getText();

        // 2. Ask the database if these are correct
        if (DatabaseManager.verifyLogin(user, pass)) {
            try {
                // ONLY if login is true, we run the transition code
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-view.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);

                javafx.application.Platform.runLater(() -> {
                    stage.setMaximized(true);
                });

                stage.show();
                System.out.println("Login Successful for: " + user);

            } catch (IOException e) {
                System.err.println("CRITICAL: Failed to load dashboard!");
                e.printStackTrace();
            }
        } else {
            // 3. If wrong, show an error and stay on the login page
            errorLabel.setText("Invalid Username or Password!");
            errorLabel.setVisible(true);
            System.out.println("Login Failed for: " + user);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/clinic/view/main-dashboard.fxml"));
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
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // This tells Java: "Finish loading everything first, THEN make it big."
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(false); // Reset it for a split second
                stage.setMaximized(true);  // Force it to fill the screen
            });

            stage.show();
        } catch (IOException e) {
            System.err.println("CRITICAL: Failed to load dashboard!");
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
}
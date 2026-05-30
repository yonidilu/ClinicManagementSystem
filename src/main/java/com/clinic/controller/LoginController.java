package com.clinic.controller;

import com.clinic.model.DatabaseManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private Label roleHeaderLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private String anticipatedRoleBoundary;

    @FXML
    public void initialize() {
        // Read which role context was targeted during choice window selection
        this.anticipatedRoleBoundary = DatabaseManager.getCurrentUserRole();

        if (anticipatedRoleBoundary != null && !anticipatedRoleBoundary.isEmpty()) {
            roleHeaderLabel.setText("Authentication Gate: " + anticipatedRoleBoundary + " Subsystem");
        } else {
            roleHeaderLabel.setText("System Security Access Gate");
        }
    }

    @FXML
    private void handleLoginAction(ActionEvent event) {
        String enteredUser = usernameField.getText().trim();
        String enteredPass = passwordField.getText().trim();

        if (enteredUser.isEmpty() || enteredPass.isEmpty()) {
            displaySecurityNotice("Input Missing", "Please enter valid credential tokens to clear security parameters.");
            return;
        }

        // Validate the credentials directly against our persistent user directory
        String validatedRole = DatabaseManager.verifyUserRole(enteredUser, enteredPass);

        // Security check: Verify that user exists and their assigned database role matches their current selection intent
        if (validatedRole != null && validatedRole.equalsIgnoreCase(anticipatedRoleBoundary)) {

            // Lock in the global user session parameters
            DatabaseManager.setCurrentSession(enteredUser, validatedRole);

            // Route to the appropriate operational console dashboard
            routeUserToWorkspace(event, validatedRole);
        } else {
            displaySecurityNotice("Clearance Denied", "Invalid username, password, or role alignment parameters.");
        }
    }

    private void routeUserToWorkspace(ActionEvent event, String role) {
        String fxmlPath = "LAB_UNIT".equalsIgnoreCase(role) ? "/lab-view.fxml" : "/main-view.fxml";
        String titleString = "LAB_UNIT".equalsIgnoreCase(role) ?
                "Clinical Laboratory Subsystem Operational Registry" : "Clinic Hub Management System - Active Workspace: " + role;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // FIXED: Reuse the existing scene framework structure
            Scene existingScene = stage.getScene();
            if (existingScene != null) {
                existingScene.setRoot(root);
            } else {
                stage.setScene(new Scene(root));
            }

            stage.setTitle(titleString);
            stage.setMaximized(true);
        } catch (IOException e) {
            displaySecurityNotice("System Frame Error", "Could not initialize selected environment framework panel layout.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        try {
            DatabaseManager.setCurrentSession("", "");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/choice-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // FIXED: Reuse the existing scene framework structure
            Scene existingScene = stage.getScene();
            if (existingScene != null) {
                existingScene.setRoot(root);
            } else {
                stage.setScene(new Scene(root));
            }

            stage.setTitle("Clinic Management System - Welcome Portal");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displaySecurityNotice(String headline, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(headline);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
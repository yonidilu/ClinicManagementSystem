package com.clinic.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

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

        // Check against your specific credentials
        if ((user.equals("doc123") && pass.equals("admin")) ||
                (user.equals("staff") && pass.equals("123")) ||
                (user.equals("patient1") && pass.equals("123"))) {
            loadDashboard(event, !user.equals("patient1"));
        } else {
            errorLabel.setText("Invalid credentials!");
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

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to load dashboard! Check method names.");
            e.printStackTrace();
        }
    }
}
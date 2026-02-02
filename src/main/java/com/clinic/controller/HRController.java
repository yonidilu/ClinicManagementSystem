package com.clinic.controller;

import com.clinic.model.DatabaseManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class HRController {

    @FXML private TextField newDoctorUser;
    @FXML private PasswordField newDoctorPass;
    @FXML private Label statusLabel;

    @FXML
    private void handleHireDoctor() {
        String username = newDoctorUser.getText();
        String password = newDoctorPass.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Error: Please fill in all fields.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        boolean success = DatabaseManager.registerUser(username, password, "DOCTOR");

        if (success) {
            statusLabel.setText("Successfully hired Doctor: " + username);
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
            newDoctorUser.clear();
            newDoctorPass.clear();
        } else {
            statusLabel.setText("Error: Username already exists.");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    private void handleGoBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/choice-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Clinic Management System");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
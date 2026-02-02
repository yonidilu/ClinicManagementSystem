package com.clinic.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class ChoiceController {

    private void goToLogin(ActionEvent event, String title) {
        switchToScene(event, "/login-view.fxml", title);
    }

    @FXML
    private void handleDoctorSelection(ActionEvent event) {
        goToLogin(event, "Doctor Login");
    }

    @FXML
    private void handleHRSelection(ActionEvent event) {
        goToLogin(event, "HR Login");
    }

    @FXML
    private void onStaffSelected(ActionEvent event) {
        goToLogin(event, "Staff Login");
    }

    // "Universal Mover" method
    private void switchToScene(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(true); // Keeps your window full screen
            stage.show();
        } catch (IOException e) {
            System.err.println("Could not find: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
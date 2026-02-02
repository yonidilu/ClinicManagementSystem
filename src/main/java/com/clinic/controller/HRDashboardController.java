package com.clinic.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HRDashboardController {

    @FXML
    private void handleOpenHiringForm(ActionEvent event) {
        switchToScene(event, "/hr-mgmt-view.fxml", "Hire New Staff");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        switchToScene(event, "/choice-view.fxml", "Clinic Management System");
    }

    private void switchToScene(ActionEvent event, String path, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error: Could not find FXML at " + path);
            e.printStackTrace();
        }
    }
}
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

    @FXML private void onDoctorSelected(ActionEvent event) { openLogin(event, "Doctor"); }
    @FXML private void onStaffSelected(ActionEvent event) { openLogin(event, "Staff"); }
    @FXML
    private void handleDoctorRole(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));

            // ADD THIS TO PREVENT THE SHRINK
            stage.setMaximized(true);

            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading login view!");
        }
    }

    private void openLogin(ActionEvent event, String role) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 1. Set the scene
            stage.setScene(new Scene(root));

            // 2. FORCE the maximize immediately after setting the scene
            stage.setMaximized(true);

            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading login view!");
        }
    }

}
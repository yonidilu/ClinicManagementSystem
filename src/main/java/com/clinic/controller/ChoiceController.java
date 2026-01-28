package com.clinic.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChoiceController {

    @FXML private void onDoctorSelected(ActionEvent event) { openLogin(event, "Doctor"); }
    @FXML private void onStaffSelected(ActionEvent event) { openLogin(event, "Staff"); }
    @FXML private void onPatientSelected(ActionEvent event) { openLogin(event, "Patient"); }

    private void openLogin(ActionEvent event, String role) {
        try {
            // Note the leading forward slash in the path
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login-view.fxml"));
            Parent root = loader.load();

            LoginController loginController = loader.getController();
            loginController.setTargetRole(role);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 350, 400));
            stage.setTitle(role + " Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
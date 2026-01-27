package com.clinic.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() throws Exception {
        // You can set these to whatever you want!
        if (usernameField.getText().equals("Yonathan Dilnesa") && passwordField.getText().equals("admin")) {

            // 1. Close the Login Window
            Stage loginStage = (Stage) usernameField.getScene().getWindow();
            loginStage.close();

            // 2. Open the Main Dashboard
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 500);
            Stage mainStage = new Stage();
            mainStage.setTitle("Clinic Management System");
            mainStage.setScene(scene);
            mainStage.show();

        } else {
            errorLabel.setText("Invalid Doctor ID or Password!");
        }
    }
}
package com.clinic.controller;

import com.clinic.model.DatabaseManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class ChoiceController {

    @FXML private Button doctorPortalBtn;
    @FXML private Button receptionPortalBtn;
    @FXML private Button laboratoryPortalBtn;
    @FXML private Button hrPortalBtn;

    @FXML
    private void handleRoleSelection(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonText = clickedButton.getText().toLowerCase();
        String contextRole = "";

        // Bulletproof text checking ignores instance mismatches
        if (buttonText.contains("doctor")) {
            contextRole = "DOCTOR";
        } else if (buttonText.contains("reception") || buttonText.contains("billing")) {
            contextRole = "RECEPTION";
        } else if (buttonText.contains("laboratory") || buttonText.contains("pathology")) {
            contextRole = "LAB_UNIT";
        } else if (buttonText.contains("human") || buttonText.contains("hr")) {
            contextRole = "HR";
        }

        // Securely bind the session parameters before scene transition
        DatabaseManager.setCurrentSession("", contextRole);
        navigateToLoginView(event, contextRole);
    }

    private void navigateToLoginView(ActionEvent event, String roleContext) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene existingScene = stage.getScene();
            if (existingScene != null) {
                existingScene.setRoot(root);
            } else {
                stage.setScene(new Scene(root));
            }

            stage.setTitle("Security Clearance Center - Authentication Segment: " + roleContext);
            stage.setMaximized(true);
        } catch (IOException e) {
            System.err.println("Navigation Framework Crash: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
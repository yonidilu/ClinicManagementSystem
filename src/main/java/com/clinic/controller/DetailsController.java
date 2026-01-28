package com.clinic.controller;

import com.clinic.model.Patient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DetailsController {

    @FXML
    private Label nameLabel;
    @FXML
    private Label ailmentLabel;
    @FXML
    private Label faydaLabel;

    // This is the "hand" that catches the patient data
    public void setPatientData(Patient patient) {
        if (patient != null) {
            nameLabel.setText("Name: " + patient.getName());
            ailmentLabel.setText("Ailment: " + patient.getAilment());
            faydaLabel.setText("Fayda ID: " + patient.getFayda());
        }
    }

    // FIXES the LoadException: Error resolving onAction='#onCloseClick'
    @FXML
    private void onCloseClick(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
}
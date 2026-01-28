package com.clinic.controller;

import com.clinic.model.Patient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DetailsController {
        @FXML private Label nameLabel;
        @FXML private Label ailmentLabel;
        @FXML private Label faydaLabel;
        @FXML private Label appointmentLabel;
        @FXML private Label  paymentLabel;
        @FXML private Label doctorLabel;
        @FXML private Label treatmentLabel; // MUST BE ADDED
        @FXML private Label diagnosisLabel;
        // MUST BE ADDED
        // ... add any others like treatmentLabel ...

    // This is the "hand" that catches the patient data
    public void setPatientData(Patient patient) {

        nameLabel.setText(patient.getName()); // No more "Name: " prefix
        ailmentLabel.setText(patient.getAilment());
        faydaLabel.setText(patient.getFayda());
        doctorLabel.setText(patient.getDoctorName()); // No more "Doctor: " prefix
        diagnosisLabel.setText(patient.getDiagnosis());
        appointmentLabel.setText(patient.getApptDate()); // New!
        paymentLabel.setText(patient.getPayment());      // New!
        treatmentLabel.setText(patient.getTreatment());


    }

    // FIXES the LoadException: Error resolving onAction='#onCloseClick'
    @FXML
    private void onCloseClick(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
}
package com.clinic.controller;

import com.clinic.model.Patient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DetailsController {
    @FXML private Label detailsLabel;

    public void setPatientData(Patient patient) {
        String info = "Name: " + patient.getName() + "\n" +
                "Age: " + patient.getAge() + "\n" +
                "Diagnosis: " + patient.getAilment() + "\n\n" +
                "Status: Admitted\n" +
                "Record ID: " + System.currentTimeMillis() % 10000;
        detailsLabel.setText(info);
    }

    @FXML
    private void onCloseClick() {
        Stage stage = (Stage) detailsLabel.getScene().getWindow();
        stage.close();
    }
}
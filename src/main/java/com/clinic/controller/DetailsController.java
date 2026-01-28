package com.clinic.controller;

import com.clinic.model.Patient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DetailsController {
    @FXML private Label nameLabel, ailmentLabel, faydaLabel;

    public void setPatient(Patient p) {
        if (p != null) {
            nameLabel.setText("Name: " + p.getName());
            ailmentLabel.setText("Ailment: " + p.getAilment());
            faydaLabel.setText("Fayda: " + p.getFayda());
        }
    }

    @FXML
    private void onCloseClick() {
        ((Stage) nameLabel.getScene().getWindow()).close();
    }
}
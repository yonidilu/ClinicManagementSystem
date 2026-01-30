package com.clinic.controller;

import com.clinic.model.Patient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class DetailsController {
    // These MUST be declared here with @FXML
    @FXML private Label fullNameLabel;
    @FXML private Label dobLabel;
    @FXML private Label idLabel;
    @FXML private Label doctorLabel;
    @FXML private Label diagnosisLabel;
    @FXML private Label treatmentLabel;
    @FXML private Label appointmentLabel;
    @FXML private Label paymentLabel;

    private Patient currentPatient;

    public void setPatientData(Patient patient) {
        this.currentPatient = patient;

        // 2. This check prevents crashes if no patient is selected
        if (patient != null) {
            // These will work now because the labels are properly declared!
            fullNameLabel.setText(patient.getName());
            dobLabel.setText(patient.getDob());
            idLabel.setText(patient.getFayda());
            doctorLabel.setText(patient.getAssignedDoctor());
            diagnosisLabel.setText(patient.getDiagnosis());
            treatmentLabel.setText(patient.getTreatment());
            appointmentLabel.setText(patient.getAppointmentDate());
            paymentLabel.setText(patient.getPaymentStatus());
        }
    }

    @FXML
    private void onOpenLabResults(ActionEvent event) {
        // 3. Now 'currentPatient' will resolve because it's a class variable!
        if (currentPatient != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/lab-view.fxml"));
                Parent root = loader.load();

                LabController controller = loader.getController();
                controller.setPatient(currentPatient);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Lab Results - " + currentPatient.getName());
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void onClose(ActionEvent event) {
        // This finds the window the button is in and closes it
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
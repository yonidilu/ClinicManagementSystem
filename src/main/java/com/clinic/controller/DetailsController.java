package com.clinic.controller;

import com.clinic.model.DatabaseManager;
import com.clinic.model.Patient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;

public class DetailsController {

    // FXML Graphical Layout Mappings
    @FXML private Label fullNameLabel;
    @FXML private Label dobLabel;
    @FXML private Label idLabel;
    @FXML private Label doctorLabel;
    @FXML private Label diagnosisLabel;
    @FXML private Label treatmentLabel;
    @FXML private Label appointmentLabel;
    @FXML private Label paymentLabel;

    // NEW: Injected components from your updated FXML
    @FXML private HBox actionBox;
    @FXML private Button labResultsButton;

    private Patient currentPatientContext;

    /**
     * Initializes the controller and applies security constraints.
     */
    @FXML
    public void initialize() {
        // SECURITY: Role-based access control for the lab button
        String role = DatabaseManager.getCurrentUserRole();
        if (!"DOCTOR".equals(role) && !"LAB_UNIT".equals(role)) {
            labResultsButton.setVisible(false);
            labResultsButton.setManaged(false); // Removes the button from the HBox layout
        }
    }

    public void populatePatientDetails(Patient patient) {
        if (patient == null) return;
        this.currentPatientContext = patient;

        fullNameLabel.setText(patient.getName());
        dobLabel.setText(patient.getDob());
        idLabel.setText(patient.getFayda());
        doctorLabel.setText(patient.getAssignedDoctor() != null ? patient.getAssignedDoctor() : "Unassigned");
        diagnosisLabel.setText(patient.getDiagnosis() != null ? patient.getDiagnosis() : "No active diagnosis filed.");
        treatmentLabel.setText(patient.getTreatment() != null ? patient.getTreatment() : "No therapy plan assigned.");
        appointmentLabel.setText(patient.getAppointmentDate() != null ? patient.getAppointmentDate() : "None scheduled.");
        paymentLabel.setText(patient.getPaymentStatus() != null ? patient.getPaymentStatus() : "Pending");
    }

    @FXML
    private void onOpenLabResults(ActionEvent event) {
        if (currentPatientContext == null) {
            displayAlert("System Error", "No patient record loaded in active window view.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lab-view.fxml"));
            Parent root = loader.load();

            LabController labController = loader.getController();
            labController.initializePatientContext(currentPatientContext);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Clinic Laboratory Workspace - " + currentPatientContext.getName());
            stage.show();

        } catch (IOException e) {
            displayAlert("Navigation Crash", "Could not load laboratory interface.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void displayAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(header);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
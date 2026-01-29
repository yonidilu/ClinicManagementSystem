package com.clinic.controller;

import com.clinic.model.Patient;
import com.clinic.model.LabResult;
import com.clinic.model.DatabaseManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class EHRController {
    @FXML private Label nameLabel, faydaLabel, dobLabel, diagnosisLabel;
    @FXML private TextArea treatmentArea, prescriptionArea;
    @FXML private TableView<LabResult> labTable;
    @FXML private TableColumn<LabResult, String> testCol, resultCol, dateCol;

    @FXML
    public void initialize() {
        // Set up the columns here so they are ready BEFORE data arrives
        testCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTestName()));
        resultCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getResultValue()));
        dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTestDate()));
    }


    public void loadPatientData(Patient patient) {
        if (patient != null) {

            // 1. Fill in Basic Info
            nameLabel.setText(patient.getName());
            faydaLabel.setText(patient.getFayda());
            dobLabel.setText(patient.getDob());
            diagnosisLabel.setText(patient.getDiagnosis());
            treatmentArea.setText(patient.getTreatment());
            prescriptionArea.setText(patient.getPrescription());

            // 2. Setup Lab Table
            testCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTestName()));
            resultCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getResultValue()));
            dateCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTestDate()));

            // 3. Load the data from the database
            labTable.setItems(DatabaseManager.getPatientLabs(patient.getFayda()));
        }

    }
    @FXML
    private void handleSaveEHR() {
        String faydaId = faydaLabel.getText();

        // Create a patient object with the updated text from the areas
        Patient p = new Patient();
        p.setFayda(faydaId);
        p.setDiagnosis(diagnosisLabel.getText());
        p.setTreatment(treatmentArea.getText());
        p.setPrescription(prescriptionArea.getText());

        // Call the manager to force the save to C:/ClinicData/clinic.db
        DatabaseManager.updatePatient(p);

        System.out.println("DEBUG: EHR Changes committed for " + faydaId);
    }
}
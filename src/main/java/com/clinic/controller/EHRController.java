package com.clinic.controller;

import com.clinic.model.DatabaseManager;
import com.clinic.model.LabResult;
import com.clinic.model.Patient;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;

public class EHRController {

    // Identity Bindings
    @FXML private Label nameLabel;
    @FXML private Label idLabel;
    @FXML private Label dobLabel;
    @FXML private Label genderLabel;

    // Interactive Clinical Editing Blocks
    @FXML private TextField diagnosisInput;
    @FXML private TextArea treatmentArea;
    @FXML private TextArea prescriptionArea;

    // Laboratory Action Framework Hooks
    @FXML private ComboBox<String> labTestComboBox;
    @FXML private TableView<LabResult> labTable;
    @FXML private TableColumn<LabResult, String> testCol;
    @FXML private TableColumn<LabResult, String> resultCol;
    @FXML private TableColumn<LabResult, String> dateCol;

    private Patient activePatientContext;

    @FXML
    public void initialize() {
        // Initialize table properties to sync with model class fields
        testCol.setCellValueFactory(new PropertyValueFactory<>("testName"));
        resultCol.setCellValueFactory(new PropertyValueFactory<>("resultValue"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("testDate"));

        // Load targeted panel definitions into menu options
        labTestComboBox.setItems(javafx.collections.FXCollections.observableArrayList(
                "Complete Blood Count (CBC)",
                "Basic Metabolic Panel (BMP)",
                "Lipid Panel",
                "Thyroid Function Panel (TSH)",
                "Hemoglobin A1C (HbA1c)",
                "Urinalysis Profile"
        ));

        // Pull the selected context from our shared static workspace pipeline
        findAndBindActivePatientContext();
    }

    private void findAndBindActivePatientContext() {
        Patient selected = MainController.selectedPatientSessionContext;

        if (selected != null) {
            // CRITICAL: Fetch the latest state from the database using the unique Fayda ID
            // This bypasses any stale information in the 'selected' cache object
            Patient freshPatient = DatabaseManager.getPatientByFayda(selected.getFayda());

            if (freshPatient != null) {
                loadPatientDataContext(freshPatient);
            } else {
                // Fallback if DB fetch fails
                loadPatientDataContext(selected);
            }
        } else {
            // Fallback for registry directory
            java.util.List<Patient> directory = DatabaseManager.getAllPatients();
            if (!directory.isEmpty()) {
                loadPatientDataContext(directory.get(0));
            }
        }
    }

    public void loadPatientDataContext(Patient patient) {
        if (patient == null) {
            System.err.println("CRITICAL: Received null patient context!");
            return;
        }
        this.activePatientContext = patient;

        // Use null-coalescing to prevent display of "null" strings
        nameLabel.setText(patient.getName() != null ? patient.getName() : "Unknown");
        idLabel.setText(patient.getFayda() != null ? patient.getFayda() : "N/A");
        dobLabel.setText(patient.getDob() != null ? patient.getDob() : "N/A");
        genderLabel.setText(patient.getGender() != null ? patient.getGender() : "N/A");

        // Set textual contents for active diagnostic properties
        diagnosisInput.setText(patient.getDiagnosis() != null ? patient.getDiagnosis() : "");
        treatmentArea.setText(patient.getTreatment() != null ? patient.getTreatment() : "");
        prescriptionArea.setText(patient.getPrescription() != null ? patient.getPrescription() : "");

        // Sync lab parameters to database rows
        refreshLabReportDisplay();
    }

    private void refreshLabReportDisplay() {
        if (activePatientContext != null) {
            System.out.println("Refreshing labs for: " + activePatientContext.getFayda());
            ObservableList<LabResult> diagnosticHistory = DatabaseManager.getPatientLabs(activePatientContext.getFayda());
            labTable.setItems(diagnosticHistory);
        }
    }
    public void initData(Patient patient) {
        if (patient != null) {
            loadPatientDataContext(patient); // This now maps your labels!
        }
    }

    @FXML
    private void handleViewHistory(ActionEvent event) {
        System.out.println("DEBUG: Button Clicked! Attempting to open history...");

        if (activePatientContext == null) {
            System.err.println("DEBUG: activePatientContext is null!");
            triggerNotificationAlert("Context Requirement", "Highlight a patient record file to view clinical charts.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/encounter-history.fxml"));
            Parent root = loader.load();

            // 3. Initialize the controller with the patient's ID
            HistoryController controller = loader.getController();
            controller.initData(activePatientContext.getFayda());

            // 4. Configure the new stage
            Stage stage = new Stage();
            stage.setTitle("Clinical History - " + activePatientContext.getName());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            triggerNotificationAlert("Navigation Error", "Could not render the historical encounter logs.");
        }
    }

    @FXML
    private void handleSaveMedicalRecord(ActionEvent event) {
        if (activePatientContext == null) return;

        String diag = diagnosisInput.getText().trim();
        String treat = treatmentArea.getText().trim();
        String presc = prescriptionArea.getText().trim();

        // 1. Update the main patient record (Existing logic)
        activePatientContext.setDiagnosis(diag);
        activePatientContext.setTreatment(treat);
        activePatientContext.setPrescription(presc);
        DatabaseManager.updatePatient(activePatientContext);

        // 2. NEW: Insert a historical record into the encounters table
        DatabaseManager.addEncounter(activePatientContext.getFayda(), diag, treat, presc);

        triggerNotificationAlert("Commit Success", "Clinical encounter logged to history.");
    }

    @FXML
    private void handlePlaceLabOrder(ActionEvent event) {
        String chosenTest = labTestComboBox.getValue();
        if (chosenTest == null || chosenTest.isEmpty()) {
            triggerNotificationAlert("Input Validation Failure", "Please select a specific diagnostic lab profile test to request.");
            return;
        }
        if (activePatientContext == null) {
            triggerNotificationAlert("System State Fault", "Patient file must be initialized to process clinical lab requests.");
            return;
        }

        String orderingPhysician = DatabaseManager.getCurrentUserRole();

        // Send request cleanly down to the database tier
        DatabaseManager.orderLabTest(activePatientContext.getFayda(), chosenTest, orderingPhysician);

        // Refresh UI table immediately to show structural changes
        refreshLabReportDisplay();
        labTestComboBox.setValue(null);
        triggerNotificationAlert("Lab Ordered Successfully", chosenTest + " request sent to Laboratory Information System.");
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Clinic Registry Hub - Operations Suite");

            // Enforce structural scaling rules uniformly to avoid minimization bugs
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            triggerNotificationAlert("Navigation Failure", "Could not route back to operations main dashboard view.");
            e.printStackTrace();
        }
    }

    private void triggerNotificationAlert(String title, String textMessage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(textMessage);
        alert.showAndWait();
    }

    public void initData() {
    }


}
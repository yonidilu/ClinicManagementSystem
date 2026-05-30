package com.clinic.controller;

import com.clinic.model.DatabaseManager;
import com.clinic.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainController {

    // Global Communication Slot for Context Swapping
    public static Patient selectedPatientSessionContext = null;

    // Receptionist UI Panels - Bound for Dynamic Role-Masking Controls
    @FXML private VBox receptionFormSection;
    @FXML private Button registerButton;
    @FXML private Button removeSelectedButton;
    @FXML private Button editSelectedButton;
    @FXML private Button billingButton;

    // Doctor Specific UI Control Hooks
    @FXML private Button viewHistoryBtn;

    // Form Entry Fields
    @FXML private TextField nameField;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private DatePicker dobDatePicker;
    @FXML private TextField contactField;
    @FXML private TextField statusField;
    @FXML private DatePicker apptDatePicker;
    @FXML private TextField faydaField;
    @FXML private TextField paidAmountField;
    @FXML private ComboBox<String> doctorComboBox;

    // Search and Master Directives
    @FXML private TextField searchField;
    @FXML private Label countLabel;

    // Patient Directory Table Elements
    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, String> faydaColumn;
    @FXML private TableColumn<Patient, String> nameColumn;
    @FXML private TableColumn<Patient, String> dobColumn;
    @FXML private TableColumn<Patient, String> genderColumn;
    @FXML private TableColumn<Patient, String> contactColumn;
    @FXML private TableColumn<Patient, String> doctorColumn;
    @FXML private TableColumn<Patient, String> statusColumn;
    @FXML private TableColumn<Patient, String> regDateColumn;

    private ObservableList<Patient> masterPatientList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Map data properties cleanly to patient model properties
        faydaColumn.setCellValueFactory(new PropertyValueFactory<>("fayda"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dob"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contact"));
        doctorColumn.setCellValueFactory(new PropertyValueFactory<>("assignedDoctor"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        regDateColumn.setCellValueFactory(new PropertyValueFactory<>("registeredDate"));

        // Load a mock list of operational doctors for triage assignment
        if (doctorComboBox != null) {
            doctorComboBox.setItems(FXCollections.observableArrayList("doc123", "dr_jones", "dr_smith"));
        }

        // Apply Dynamic Access Control Boundaries based on Session Role
        applyAccessControlRules();

        // Populate records into active dashboard view
        refreshPatientTable();

        // Attach listener for real-time live filtering
        searchField.textProperty().addListener((observable, oldValue, newValue) -> handleLiveSearch(newValue));
    }

    private void applyAccessControlRules() {
        String activeRole = DatabaseManager.getCurrentUserRole();

        if ("DOCTOR".equalsIgnoreCase(activeRole)) {
            // Rule 1: Hide registration layout forms and structural modifying blocks completely
            receptionFormSection.setVisible(false);
            receptionFormSection.setManaged(false);

            registerButton.setVisible(false);
            registerButton.setManaged(false);

            removeSelectedButton.setVisible(false);
            removeSelectedButton.setManaged(false);

            editSelectedButton.setVisible(false);
            editSelectedButton.setManaged(false);

            billingButton.setVisible(false);
            billingButton.setManaged(false);

            // Ensure clinical buttons stay highly visible
            viewHistoryBtn.setVisible(true);
            viewHistoryBtn.setManaged(true);

        } else if ("RECEPTION".equalsIgnoreCase(activeRole)) {
            // Rule 2: Receptionists handle registration entirely, but cannot view clinical histories
            receptionFormSection.setVisible(true);
            receptionFormSection.setManaged(true);

            registerButton.setVisible(true);
            registerButton.setManaged(true);

            removeSelectedButton.setVisible(true);
            removeSelectedButton.setManaged(true);

            editSelectedButton.setVisible(true);
            editSelectedButton.setManaged(true);

            billingButton.setVisible(true);
            billingButton.setManaged(true);

            // Block clinical diagnostics buttons from receptionist access
            viewHistoryBtn.setVisible(false);
            viewHistoryBtn.setManaged(false);
        }
    }

    public void refreshPatientTable() {
        masterPatientList.clear();
        List<Patient> list = DatabaseManager.getAllPatients();
        masterPatientList.addAll(list);
        patientTable.setItems(masterPatientList);
        countLabel.setText("Total Active Records: " + masterPatientList.size());
    }

    @FXML
    private void handleAddPatient(ActionEvent event) {
        if (faydaField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty()) {
            showAlert("Validation Warning", "Patient National ID (Fayda) and Name are required variables.");
            return;
        }

        Patient newPatient = new Patient();
        newPatient.setFayda(faydaField.getText().trim());
        newPatient.setName(nameField.getText().trim());

        // 1. Convert LocalDate safely to String format for your DB model
        LocalDate dobDate = dobDatePicker.getValue();
        if (dobDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            newPatient.setDob(dobDate.format(formatter)); // Pass clean String mapping
        } else {
            newPatient.setDob(""); // Fallback if no date selected
        }

        // 2. Add structural null protection for ComboBox selections before trimming
        String genderValue = genderComboBox.getValue();
        newPatient.setGender(genderValue != null ? genderValue.trim() : "Unspecified");

        newPatient.setContact(contactField.getText().trim());
        newPatient.setPaymentStatus(statusField.getText().trim());
        newPatient.setAssignedDoctor(doctorComboBox.getValue() != null ? doctorComboBox.getValue() : "Unassigned");
        newPatient.setRegisteredDate(java.time.LocalDate.now().toString());

        if (DatabaseManager.savePatient(newPatient)) {
            clearInputFormFields();
            refreshPatientTable();
        } else {
            showAlert("Database Error", "Failed to preserve patient file. Unique ID may already exist.");
        }
    }

    @FXML
    private void handleRemoveSelected(ActionEvent event) {
        Patient target = patientTable.getSelectionModel().getSelectedItem();
        if (target == null) {
            showAlert("Selection Error", "Please select a specific patient record to remove.");
            return;
        }

        String sql = "DELETE FROM patients WHERE fayda = ?";
        try (java.sql.Connection conn = DatabaseManager.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, target.getFayda());
            pstmt.executeUpdate();
            refreshPatientTable();
        } catch (java.sql.SQLException e) {
            showAlert("Execution Exception", "Database failed to delete entry structural map: " + e.getMessage());
        }
    }

    @FXML
    private void onEditButtonClick(ActionEvent event) {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Select a patient profile to update.");
            return;
        }

        faydaField.setText(selected.getFayda());
        nameField.setText(selected.getName());

        // 3. Parse your database text String back into a LocalDate object for the picker
        String dobStr = selected.getDob();
        if (dobStr != null && !dobStr.trim().isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate parsedDate = LocalDate.parse(dobStr.trim(), formatter);
                dobDatePicker.setValue(parsedDate); // Sets calendar smoothly
            } catch (Exception e) {
                System.err.println("Could not parse saved DOB string: " + e.getMessage());
                dobDatePicker.setValue(null); // Reset safely if string pattern breaks
            }
        } else {
            dobDatePicker.setValue(null);
        }

        genderComboBox.setValue(selected.getGender());
        contactField.setText(selected.getContact());
        statusField.setText(selected.getPaymentStatus());
        doctorComboBox.setValue(selected.getAssignedDoctor());
    }

    @FXML
    private void onShowDetails(ActionEvent event) {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please highlight a patient profile file to view metadata.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/details-view.fxml"));
            Parent root = loader.load();

            DetailsController detailsController = loader.getController();
            detailsController.populatePatientDetails(selected);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Detailed Meta Record Summary - " + selected.getName());
            popupStage.showAndWait();

            refreshPatientTable();
        } catch (IOException e) {
            showAlert("Navigation Crash", "Could not render details overlay window.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewHistory(ActionEvent event) {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Context Requirement", "Highlight a patient record file to view clinical charts.");
            return;
        }

        // CRITICAL CACHE STEP: Store the patient instance for the EHRController to pull down
        selectedPatientSessionContext = selected;

        // Route into the specialized diagnostic chart layout
        openSpecializedView(event, "/ehr-view.fxml", "Electronic Health Chart Summary - " + selected.getName());
    }

    /**
     * DISPATCHED LAB TEST TRIGGER: Captures active patient selection and locks the requesting
     * doctor context token before generating a new record in the persistent laboratory tables.
     */
    @FXML
    private void handleOrderLabTest(ActionEvent event) {
        // 1. Find who is highlighted in the grid row right now
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();

        if (selectedPatient == null) {
            showAlert("Selection Missing", "Please choose a patient from the active table registry grid first before ordering lab panels.");
            return;
        }

        // 2. Extract the logged-in doctor identifier from the active login session tokens
        String activeOrderingDoctor = DatabaseManager.getCurrentUserSession();

        // 3. Fallback check to avoid empty text spaces if session state tracking is clear
        if (activeOrderingDoctor == null || activeOrderingDoctor.trim().isEmpty()) {
            activeOrderingDoctor = "Dr. System Default";
        }

        // 4. Send a standard default blood or diagnostic query context through down to the database row inserter
        String defaultTestProfile = "Complete Blood Count (CBC)";

        boolean isSuccess = DatabaseManager.orderLabTest(
                selectedPatient.getFayda(),
                defaultTestProfile,
                activeOrderingDoctor
        );

        if (isSuccess) {
            showAlert("Diagnostics Dispatched", "Lab orders for [" + selectedPatient.getName() + "] signed and transmitted successfully by " + activeOrderingDoctor + ".");
        } else {
            showAlert("Transmission Fault", "Failed to communicate with database engine infrastructure. Verify network links.");
        }
    }

    @FXML
    private void onBillingButtonClick(ActionEvent event) {
        // 1. Get the currently selected patient from the main table grid
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();

        if (selectedPatient == null) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setHeaderText("Selection Failure");
            warning.setContentText("Highlight a specific active patient file profile line to examine billing statements.");
            warning.showAndWait();
            return;
        }

        try {
            // 2. Instantiate a clean staging frame window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/billing-view.fxml"));
            Parent root = loader.load();

            // 3. Extract the active controller handler reference instance context mapping
            BillingController billingCtrl = loader.getController();

            // 4. Safely pipe the selected identity down to the operational matrix tracker
            billingCtrl.loadPatientBillingProfile(selectedPatient.getFayda(), selectedPatient.getName());

            // 5. Present the independent modal screen cleanly to the receptionist window stack
            Stage billingStage = new Stage();
            billingStage.setTitle("Patient Ledger: " + selectedPatient.getName());
            billingStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            billingStage.setScene(new Scene(root));
            billingStage.show();

        } catch (IOException e) {
            System.err.println("Failed to mount billing subview architecture components:");
            e.printStackTrace();
        }
    }

    private void handleLiveSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            patientTable.setItems(masterPatientList);
            return;
        }
        String lower = keyword.toLowerCase().trim();
        ObservableList<Patient> matches = FXCollections.observableArrayList();
        for (Patient p : masterPatientList) {
            if (p.getName().toLowerCase().contains(lower) || p.getFayda().toLowerCase().contains(lower)) {
                matches.add(p);
            }
        }
        patientTable.setItems(matches);
    }

    private void openSpecializedView(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();


            // If this is the EHR view, pass the context
            if (fxmlPath.equals("/ehr-view.fxml")) {
                EHRController controller = loader.getController();
                // Pass the actual patient object instead of calling an empty method
                controller.initData(MainController.selectedPatientSessionContext);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));

            // CRITICAL: Explicitly set size and constraints to prevent collapse
            stage.setWidth(1000); // Set to your preferred width
            stage.setHeight(700); // Set to your preferred height
            stage.centerOnScreen(); // Prevents it from appearing in the corner (0,0)

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not render the requested view.");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // 1. Clear session credentials
        DatabaseManager.setCurrentSession("", "");

        // 2. Open the role selection view
        openSpecializedView(event, "/choice-view.fxml", "Clinic Portal System - Select Role");

        // 3. CRITICAL: Close the current window so it doesn't stay open in the background
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
    }
    private void clearInputFormFields() {
        faydaField.clear();
        nameField.clear();

        if (dobDatePicker != null) dobDatePicker.setValue(null);
        if (apptDatePicker != null) apptDatePicker.setValue(null);

        contactField.clear();
        statusField.clear();

        if (genderComboBox != null) genderComboBox.setValue(null);
        if (doctorComboBox != null) doctorComboBox.setValue(null);

        if (paidAmountField != null) paidAmountField.clear();
    }

    private void showAlert(String title, String body) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(body);
        alert.showAndWait();
    }
}
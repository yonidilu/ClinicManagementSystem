package com.clinic.controller;

import com.clinic.model.DatabaseManager;
import com.clinic.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.swing.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import static com.clinic.model.DatabaseManager.generateUniquePatientID;


public class MainController {
    @FXML private TableColumn<Patient, String> nameColumn;
    @FXML private TableColumn<Patient, String> dobColumn;
    @FXML private TableColumn<Patient, String> faydaColumn;
    @FXML private TableColumn<Patient, String> genderColumn;
    @FXML private TableColumn<Patient, String> contactColumn;
    @FXML private TableColumn<Patient, String> statusColumn;
    @FXML private TextField genderField;
    @FXML private TextField contactField;
    @FXML private TextField doctorField;
    @FXML private TextField statusField;
    @FXML private TextField diagnosisField;
    @FXML private TextField treatmentField;
    @FXML private TextField prescriptionField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private TextField apptDateField;
    @FXML private TextField paymentField;
    @FXML private Button registerButton;
    @FXML private TableView<Patient> patientTable;
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private TextField patientNameField;
    @FXML private TextField ailmentField;
    @FXML private TextField faydaField;
    @FXML private TableColumn<Patient, String> regDateColumn;
    @FXML private ComboBox doctorComboBox;
    @FXML private TableColumn<Patient, String> doctorColumn;
    @FXML private TableColumn<Patient, String> lastVisitColumn;
    @FXML private TableColumn<Patient, Double> balanceColumn;
    @FXML private TextField nameField;
    @FXML private TextField dobField;
    @FXML private TextField paidAmountField;

    private boolean isAdmin;
    private final ObservableList<Patient> patientList = FXCollections.observableArrayList();

    public void setAccessLevel(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @FXML
    public void initialize() {


        //Setup Initial ID
        try {
            faydaField.setText(generateUniquePatientID());
            faydaField.setEditable(false);
        } catch (Exception e) {
            System.err.println("Error generating ID: " + e.getMessage());
        }

        //Setup Doctor Dropdown
        ObservableList<String> doctors = FXCollections.observableArrayList(
                "Dr. Smith", "Dr. Adams", "Dr. Bekele", "Dr. Taylor", "Dr. Yonathan", "Dr. Abel"
        );
        if (doctorComboBox != null) {
            doctorComboBox.setItems(doctors);
        }

        //Wire Columns - CLEANED & UNIFIED
        if (patientTable != null && nameColumn != null) {
            nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
            dobColumn.setCellValueFactory(cellData -> cellData.getValue().dobProperty());
            faydaColumn.setCellValueFactory(cellData -> cellData.getValue().faydaProperty());
            genderColumn.setCellValueFactory(cellData -> cellData.getValue().genderProperty());
            contactColumn.setCellValueFactory(cellData -> cellData.getValue().contactProperty());

            regDateColumn.setCellValueFactory(new PropertyValueFactory<>("registeredDate"));
            lastVisitColumn.setCellValueFactory(new PropertyValueFactory<>("lastVisit"));
            balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balanceOwed"));

            doctorColumn.setCellValueFactory(cellData -> cellData.getValue().assignedDoctorProperty());
            statusColumn.setCellValueFactory(cellData -> cellData.getValue().paymentStatusProperty());
            balanceColumn.setCellValueFactory(cellData -> cellData.getValue().balanceOwedProperty().asObject());

        }

        // 4. Load data from Database
        try {
            DatabaseManager.initializeDatabase();
            List<Patient> dbPatients = DatabaseManager.getAllPatients();
            if (dbPatients != null) {
                patientList.setAll(dbPatients);
            }
        } catch (Exception e) {
            System.err.println("Failed to load patients: " + e.getMessage());
        }

        //Setup Search Filter (Live Filtering)
        FilteredList<Patient> filteredData = new FilteredList<>(patientList, p -> true);
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredData.setPredicate(patient -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String filter = newVal.toLowerCase();
                    return patient.getName().toLowerCase().contains(filter) ||
                            patient.getFayda().toLowerCase().contains(filter);
                });
            });
        }
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balanceOwed"));

        //Bind Table to Filtered Data ONLY
        patientTable.setItems(filteredData);
        updateCount();

        //Status Cell Styling
        if (statusColumn != null) {
            statusColumn.setCellFactory(column -> new TableCell<Patient, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        String baseStyle = "-fx-text-fill: white; -fx-background-radius: 5; -fx-alignment: center; -fx-font-weight: bold;";
                        if (item.equalsIgnoreCase("Pending")) {
                            setStyle(baseStyle + "-fx-background-color: #e74c3c;");
                        } else if (item.equalsIgnoreCase("Paid")) {
                            setStyle(baseStyle + "-fx-background-color: #2ecc71;");
                        } else if (item.equalsIgnoreCase("Partial")) {
                            setStyle("-fx-text-fill: black; -fx-background-color: #f1c40f; -fx-background-radius: 5; -fx-alignment: center; -fx-font-weight: bold;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            });
        }

        //Debug Check
        if (!patientList.isEmpty()) {
            System.out.println("DEBUG: First Patient: " + patientList.get(0).getName());
            System.out.println("DEBUG: Reg Date Value: [" + patientList.get(0).getRegisteredDate() + "]");
        }

    }
    @FXML
    private void handleAddPatient(ActionEvent event) {
        try {
            //DATA VALIDATION
            if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                System.err.println("Validation Failed: Name is empty.");
                return;
            }

            //BUILD PATIENT OBJECT
            Patient p = new Patient();
            p.setName(nameField.getText().trim());
            p.setFayda(faydaField.getText().trim());
            p.setDob(dobField.getText());
            p.setGender(genderField.getText());
            p.setContact(contactField.getText());
            p.setDiagnosis(diagnosisField.getText());
            p.setTreatment(treatmentField.getText());
            p.setPrescription(prescriptionField.getText());
            p.setAppointmentDate(apptDateField.getText());

            String doctor = (String) doctorComboBox.getValue();
            p.setAssignedDoctor(doctor == null ? "Unassigned" : doctor);

            // Handle Payment logic
            // Inside handleAddPatient, before DatabaseManager calls:
            try {
                String paidText = paidAmountField.getText();
                if (paidText != null && !paidText.trim().isEmpty()) {
                    double amount = Double.parseDouble(paidText.trim());
                    p.setPaymentAmount(String.valueOf(amount)); // For the 'payment' column
                    p.setBalanceOwed(amount); // For the 'balance_owed' column
                } else {
                    p.setPaymentAmount("0.0");
                    p.setBalanceOwed(0.0);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid number in Paid Amount field");
                p.setBalanceOwed(0.0);
            }

            p.setPaymentStatus(statusField.getText() == null || statusField.getText().isEmpty() ? "Pending" : statusField.getText());

            //DATABASE ACTION
            if (isEditMode) {
                Patient selected = patientTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    p.setRegisteredDate(selected.getRegisteredDate());
                    p.setLastVisit(java.time.LocalDate.now().toString());
                    DatabaseManager.updatePatient(p); // Uses WHERE fayda=?
                }
            } else {
                String today = java.time.LocalDate.now().toString();
                p.setRegisteredDate(today);
                p.setLastVisit(today);
                DatabaseManager.savePatient(p);
            }

            //THE REFRESH
            if (searchField != null) {
                searchField.clear();
            }

            //Reload list from DB
            List<Patient> freshData = DatabaseManager.getAllPatients();
            patientList.setAll(freshData);

            //UI CLEANUP
            clearFields();
            updateCount();
            isEditMode = false;
            registerButton.setText("Add Patient");
            registerButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
            faydaField.setText(generateUniquePatientID());

            System.out.println("Success: Patient List updated. Size: " + patientList.size());

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR in handleAddPatient: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private boolean isEditMode = false;

    @FXML
    private void onRegisterButtonClick() {
        try {
            // Validation
            if (nameField.getText().trim().isEmpty() || faydaField.getText().trim().isEmpty()) {
                showWarning("Validation Error", "Name and Fayda ID are required.");
                return;
            }

            Patient p = new Patient();
            p.setName(nameField.getText());
            p.setFayda(faydaField.getText());
            p.setGender(genderField.getText());
            p.setContact(contactField.getText());
            p.setAssignedDoctor(doctorComboBox.getValue() != null ? (String)doctorComboBox.getValue() : "Unassigned");
            p.setDiagnosis(diagnosisField.getText());
            p.setTreatment(treatmentField.getText());
            p.setPaymentStatus(statusField.getText().isEmpty() ? "Pending" : statusField.getText());

            String today = LocalDate.now().toString();

            if (isEditMode) {
                //Get the patient currently selected in the TableView
                Patient selected = patientTable.getSelectionModel().getSelectedItem();

                if (selected != null) {
                    p.setId(selected.getId());
                    p.setRegisteredDate(selected.getRegisteredDate());

                    if (dobField.getText() == null || dobField.getText().trim().isEmpty()) {
                        p.setDob(selected.getDob());
                    } else {
                        p.setDob(dobField.getText());
                    }
                    p.setRegisteredDate(selected.getRegisteredDate());
                    p.setLastVisit(java.time.LocalDate.now().toString());

                    DatabaseManager.updatePatient(p);
                    p.setLastVisit(java.time.LocalDate.now().toString());

                    DatabaseManager.updatePatient(p);
                    refreshTable();
                    System.out.println("Update Complete: " + p.getName() + " (ID: " + p.getId() + ")");
                }

                // Reset the UI state
                isEditMode = false;
                registerButton.setText("Add Patient");
                registerButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                clearFields();
            }
            clearFields();
            patientTable.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void onShowDetails(ActionEvent event) {
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();

        if (selectedPatient == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Please select a patient first.");
            alert.show();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/details-view.fxml"));
            Parent root = loader.load();

            DetailsController controller = loader.getController();
            controller.setPatientData(selectedPatient);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Patient Details - " + selectedPatient.getName());
            stage.show();
        } catch (IOException e) {
            System.err.println("Could not find details-view.fxml. Check your path!");
            e.printStackTrace();
        }
    }
    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (DatabaseManager.verifyLogin(user, pass)) {
            System.out.println("Access Granted for Admin");
            openDashboard();
        } else {
            errorLabel.setText("Invalid Credentials!");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            System.err.println("Could not find login-view.fxml. Check your path!");
            e.printStackTrace();
        }
    }
    @FXML
    private void onDeleteButtonClick() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            DatabaseManager.deletePatient(selected.getFayda());
            patientList.remove(selected);
            updateCount();
        }
    }
    private void clearFields() {
        nameField.clear();
        dobField.clear();
        genderField.clear();
        contactField.clear();
        diagnosisField.clear();
        treatmentField.clear();
        prescriptionField.clear();
        apptDateField.clear();
        paidAmountField.clear();
        statusField.clear();
        doctorComboBox.setValue(null);

        faydaField.setText(generateUniquePatientID());
    }
    @FXML
    private void onEditButtonClick(ActionEvent event) {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // PERSONAL
            nameField.setText(selected.getName());
            genderField.setText(selected.getGender());
            dobField.setText(selected.getDob());
            contactField.setText(selected.getContact());

            // MEDICAL
            diagnosisField.setText(selected.getDiagnosis());
            treatmentField.setText(selected.getTreatment());
            prescriptionField.setText(selected.getPrescription());

            // ADMIN
            faydaField.setText(selected.getFayda());
            faydaField.setEditable(false); // Don't allow changing Fayda during edit
            apptDateField.setText(selected.getAppointmentDate());
            statusField.setText(selected.getPaymentStatus());
            doctorComboBox.setValue(selected.getAssignedDoctor());

            isEditMode = true;
            registerButton.setText("Update Patient");
        }
    }
    @FXML
    private void onBillingButtonClick() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/billing-view.fxml"));
                Parent root = loader.load();

                BillingController billingController = loader.getController();
                billingController.setPatient(selected);

                //Open the new window
                Stage stage = new Stage();
                stage.setTitle("Billing - " + selected.getName());
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException e) {
                System.err.println("Could not load billing view: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Simple alert
            System.out.println("Please select a patient first!");
        }
    }
    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Clinic Dashboard");
            stage.show();
        } catch (IOException e) {
            System.err.println("FXML Error: " + e.getMessage());
        }
    }
    @FXML
    private void handleViewHistory(ActionEvent event) {
        //Get the patient selected in the table
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();

        if (selectedPatient != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ehr-view.fxml"));
                Parent root = loader.load();

                // 2. Pass the patient to the new controller
                EHRController controller = loader.getController();
                if (controller != null) {
                    controller.loadPatientData(selectedPatient);
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("No Selection");
                    alert.setHeaderText("Warning");
                    alert.setContentText("Please select a patient from the table first!");
                    alert.showAndWait();
                }

                //Open new windw
                Stage stage = new Stage();
                stage.setTitle("Medical History: " + selectedPatient.getName());
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();

            } catch (IOException e) {
                System.err.println("FXML Load Error: Check the path to ehr-view.fxml!");
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select a patient from the table first!");
            alert.showAndWait();
        }
    }

    private String currentUserRole;

    public void setRole(String role) {
        this.currentUserRole = role;
    }

    private void showSelectionWarning() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText("Please select a patient first.");
        alert.showAndWait();
    }

    private boolean isPatientSelected(Patient selected) {
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Please select a patient first.");
            alert.showAndWait();
            return false;
        }
        return true;
    }

    @FXML
    private void handleRemoveSelected(ActionEvent event) {
        //Get the selected patient
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Error","Please select a patient to remove.");
            return;
        }

        //Setup the "Are you sure?" confirrmation
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Patient: " + selected.getName());
        confirm.setContentText("This will permanently remove Fayda ID: " + selected.getFayda());

        java.util.Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                //Delete from SQL using 'fayda' to avoid the "no such column: id" error
                DatabaseManager.deletePatient(selected.getFayda());

                //Handle the UI list. Wrap it in a new list to prevent 'UnsupportedOperationException'
                ObservableList<Patient> items = FXCollections.observableArrayList(patientTable.getItems());
                items.remove(selected);
                patientTable.setItems(items);

                System.out.println("SUCCESS: Removed " + selected.getName() + " from DB and UI.");
            } catch (Exception e) {
                System.err.println("CRITICAL: Removal failed! " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Deletion canceled by user.");
        }
    }

    @FXML
    private void handleEditSelected() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            String currentReg = selected.getRegisteredDate();
            String currentVisit = selected.getLastVisit();
            int originalId = selected.getId();
            selected.setName(nameField.getText());
            selected.setDob(dobField.getText());
            selected.setGender(genderField.getText());
            selected.setContact(contactField.getText());
            selected.setDiagnosis(diagnosisField.getText());
            selected.setTreatment(treatmentField.getText());
            selected.setRegisteredDate(currentReg);
            selected.setLastVisit(currentVisit);
            selected.setId(originalId);

            DatabaseManager.updatePatient(selected);
            refreshTable();

            System.out.println("Update successful for: " + selected.getName());
        } else {
            showWarning("Error","Please select a patient for billing.");
        }
    }

    @FXML
    private void handleBilling(ActionEvent event) {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Error","Please select a patient for billing.");
            return;
        }
    }

    private void showWarning(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Clinic System Warning");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void deletePatientFromDatabase(Patient patient) {
        // This SQL command targets the specific patient by their ID
        String query = "DELETE FROM patients WHERE id = ?";

        try (java.sql.Connection conn = com.clinic.model.DatabaseManager.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, patient.getId());
            pstmt.executeUpdate();

            System.out.println("Patient " + patient.getName() + " deleted from database.");
        } catch (java.sql.SQLException e) {
            System.err.println("Database Error: Could not delete patient.");
            e.printStackTrace();
        }
    }
    private void refreshTable() {
        List<Patient> updatedList = DatabaseManager.getAllPatients();

        //Clear the old list and add the new one
        patientList.clear();
        patientList.addAll(updatedList);

        updateCount();

        System.out.println("UI: Table refreshed with " + updatedList.size() + " patients.");
    }
    public void updateCount() {
        if (countLabel != null && patientList != null) {
            countLabel.setText(String.valueOf(patientList.size()));
        } else {
            System.err.println("DEBUG: Cannot update count - countLabel or patientList is null.");
        }
    }
}


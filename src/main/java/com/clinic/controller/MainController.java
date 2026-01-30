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
// NEW: The correct Image import for JavaFX
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    @FXML private TableColumn<Patient, String> doctorColumn;
    @FXML private TableColumn<Patient, String> lastVisitColumn;
    @FXML private TableColumn<Patient, Double> balanceColumn;
    @FXML private TextField nameField;
    @FXML private TextField dobField;
    @FXML private TextField paidAmountField; // This maps to "Balance Owed"


    private boolean isAdmin;
    private final ObservableList<Patient> patientList = FXCollections.observableArrayList();

    public void setAccessLevel(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @FXML
    public void initialize() {
        // 1. Setup Initial ID
        faydaField.setText(generateUniquePatientID());
        faydaField.setEditable(false);

        // 2. Wire Columns (Ensuring synchronization with Patient.java)
        if (nameColumn != null && faydaColumn != null) {
            // Original Core Columns
            nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
            dobColumn.setCellValueFactory(cellData -> cellData.getValue().dobProperty());
            faydaColumn.setCellValueFactory(cellData -> cellData.getValue().faydaProperty());
            genderColumn.setCellValueFactory(cellData -> cellData.getValue().genderProperty());
            contactColumn.setCellValueFactory(cellData -> cellData.getValue().contactProperty());

            // FIXED: Mapping the dates to match your new Patient.java getters
            regDateColumn.setCellValueFactory(new PropertyValueFactory<>("registeredDate"));
            lastVisitColumn.setCellValueFactory(new PropertyValueFactory<>("lastVisit"));

            // NEW COLUMNS: Doctor and Balance
            doctorColumn.setCellValueFactory(cellData -> cellData.getValue().assignedDoctorProperty());
            balanceColumn.setCellValueFactory(cellData -> cellData.getValue().balanceOwedProperty().asObject());

            // Status Column
            statusColumn.setCellValueFactory(cellData -> cellData.getValue().paymentStatusProperty());
        } else {
            System.err.println("Critical Error: Table columns not found in FXML!");
        }

        // 3. Load data from Database
        DatabaseManager.initializeDatabase();
        patientList.setAll(DatabaseManager.getAllPatients());

        // 4. Setup Search Filter
        FilteredList<Patient> filteredData = new FilteredList<>(patientList, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(patient -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String filter = newVal.toLowerCase();
                return patient.getName().toLowerCase().contains(filter) ||
                        patient.getFayda().toLowerCase().contains(filter);
            });
        });

        // 5. Bind Table to Data
        patientTable.setItems(filteredData);
        updateCount();

        // 6. Status Cell Styling (Colors)
        statusColumn.setCellFactory(column -> new TableCell<Patient, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("Pending")) {
                        setStyle("-fx-text-fill: white; -fx-background-color: #e74c3c; -fx-background-radius: 5; -fx-alignment: center;");
                    } else if (item.equalsIgnoreCase("Paid")) {
                        setStyle("-fx-text-fill: white; -fx-background-color: #2ecc71; -fx-background-radius: 5; -fx-alignment: center;");
                    } else if (item.equalsIgnoreCase("Partial")) {
                        setStyle("-fx-text-fill: black; -fx-background-color: #f1c40f; -fx-background-radius: 5; -fx-alignment: center;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }
    public void updateCount() {
        if (countLabel != null) {
            countLabel.setText(String.valueOf(patientList.size()));
        }
    }
    @FXML
    private void handleAddPatient(ActionEvent event) {
        try {
            // 1. Collect data from your TextFields
            String name = nameField.getText();
            String dob = dobField.getText();
            String gender = genderField.getText();
            String contact = contactField.getText();
            String fayda = faydaField.getText();
            String doctor = doctorField.getText();
            String diagnosis = diagnosisField.getText();
            String treatment = treatmentField.getText();
            String prescription = prescriptionField.getText();
            String apptDate = apptDateField.getText();
            String statusText = statusField.getText(); // Renamed to avoid confusion

            // 2. Handle the Money safely
            double initialBalance = 0.0;
            String paidText = paidAmountField.getText();
            if (paidText != null && !paidText.isEmpty()) {
                initialBalance = Double.parseDouble(paidText);
            }

            // 3. Build/Prepare the Patient object
            Patient p = new Patient();
            p.setName(name);
            p.setDob(dob);
            p.setGender(gender);
            p.setContact(contact);
            p.setFayda(fayda);
            p.setAssignedDoctor(doctor);
            p.setDiagnosis(diagnosis);
            p.setTreatment(treatment);
            p.setPrescription(prescription);
            p.setAppointmentDate(apptDate);
            p.setPaymentAmount(String.valueOf(initialBalance));
            p.setBalanceOwed(initialBalance);

            // FIX: The NullPointerException
            if (statusText == null || statusText.isEmpty()) {
                p.setPaymentStatus("Pending");
            } else {
                p.setPaymentStatus(statusText);
            }

            if (isEditMode) {
                // FIX: The Amnesia Problem
                // We must retrieve the existing dates from the table selection
                Patient selected = patientTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    p.setRegisteredDate(selected.getRegisteredDate());
                    p.setLastVisit(java.time.LocalDate.now().toString()); // Update visit date to today
                }

                DatabaseManager.updatePatient(p);

                int selectedIndex = patientTable.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0) {
                    patientList.set(selectedIndex, p);
                }

                isEditMode = false;
                registerButton.setText("Add Patient");
                registerButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
            } else {
                // Logic for Adding a brand new patient
                String today = java.time.LocalDate.now().toString();
                p.setRegisteredDate(today);
                p.setLastVisit(today);

                DatabaseManager.savePatient(p);
                patientList.add(p);
            }

            clearFields();
            updateCount();
            patientTable.refresh(); // Force the table to show the new data

        } catch (NumberFormatException e) {
            showWarning("Please enter a valid number for the amount.");
        } catch (Exception e) {
            System.err.println("Error in handleAddPatient: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private boolean isEditMode = false;

    @FXML
    private void onRegisterButtonClick() {
        // 1. Get text from the UI fields
        String name = patientNameField.getText();
        String ailment = ailmentField.getText();
        String gender = genderField.getText();
        String contact = contactField.getText();
        String status = statusField.getText();
        String fayda = faydaField.getText();

        // Use the new fields we just declared
        String doctor = doctorField.getText();
        String diagnosis = diagnosisField.getText();
        String treatment = treatmentField.getText();
        String prescription = prescriptionField.getText();
        String apptDate = apptDateField.getText();
        String payment = paymentField.getText();
        String regDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));



        Patient p = new Patient(name, ailment, gender, contact, status, fayda,
                doctor, diagnosis, treatment, prescription, apptDate, payment, regDate);

        if (registerButton.getText().equals("Save Changes")) {
            // This calls the update logic that forces a disk save
            DatabaseManager.updatePatient(p);

            // Refresh the table locally so you see the change immediately
            patientList.removeIf(patient -> patient.getFayda().equals(p.getFayda()));
            patientList.add(p);

            registerButton.setText("Add Patient");
            clearFields();
        }
        else {
            // ... (Your existing save logic)
            DatabaseManager.savePatient(p);
            patientList.add(p);
            clearFields();
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
            // FIXED PATH: Must match src/main/resources/com/clinic/view/
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

        // This calls the verifyLogin we set up in DatabaseManager
        if (DatabaseManager.verifyLogin(user, pass)) {
            System.out.println("Access Granted for Admin778!");
            openDashboard();
        } else {
            errorLabel.setText("Invalid Credentials!");
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // FIXED PATH: Must match src/main/resources/com/clinic/view/
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
            // FIXES Error in image_89453e.png - Passing String, not Property
            DatabaseManager.deletePatient(selected.getFayda());
            patientList.remove(selected);
            updateCount();
        }
    }
    private void clearFields() {
        nameField.clear();
        dobField.clear(); // Changed from ailmentField to match FXML
        genderField.clear();
        contactField.clear();
        doctorField.clear();
        diagnosisField.clear();
        treatmentField.clear();
        prescriptionField.clear();
        apptDateField.clear();
        paidAmountField.clear();
        statusField.clear();

        faydaField.setText(generateUniquePatientID());
    }
    @FXML
    private void onEditButtonClick(ActionEvent event) {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // Personal Details
            nameField.setText(selected.getName());
            genderField.setText(selected.getGender());
            dobField.setText(selected.getDob()); // Ensure this matches FXML fx:id
            contactField.setText(selected.getContact());

            // Medical Records
            doctorField.setText(selected.getAssignedDoctor());
            diagnosisField.setText(selected.getDiagnosis());
            treatmentField.setText(selected.getTreatment());
            prescriptionField.setText(selected.getPrescription());

            // Administrative
            faydaField.setText(selected.getFayda());
            apptDateField.setText(selected.getAppointmentDate());
            paidAmountField.setText(String.valueOf(selected.getBalanceOwed())); // Fix: mapping payment
            statusField.setText(selected.getPaymentStatus());

            // Toggle to Update Mode
            isEditMode = true;
            registerButton.setText("Update Patient");
            registerButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        } else {
            showWarning("Please select a patient from the table first!");
        }
    }
    @FXML
    private void onBillingButtonClick() {
        // 1. Get the patient currently selected in the main table
        Patient selected = patientTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            try {
                // FIXED PATH: Must match src/main/resources/com/clinic/view/
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/billing-view.fxml"));
                Parent root = loader.load();

                // 3. Connect to the BillingController and pass the patient
                BillingController billingController = loader.getController();
                billingController.setPatient(selected);// Uses the method we just fixed!

                // 4. Open the new window
                Stage stage = new Stage();
                stage.setTitle("Billing - " + selected.getName());
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException e) {
                System.err.println("Could not load billing view: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Simple alert if no patient is clicked
            System.out.println("Please select a patient first!");
        }
    }
    private void openDashboard() {
        try {
            // Path should match your project structure
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/clinic/view/main-dashboard.fxml"));
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
    private void handleViewHistory(ActionEvent event) { // Added ActionEvent to match FXML
        // 1. Get the patient selected in the table
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();

        if (selectedPatient != null) {
            try {
                // Updated path logic to be more robust for Maven/Gradle structures
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ehr-view.fxml"));
                Parent root = loader.load();

                // 2. Pass the patient to the new controller
                EHRController controller = loader.getController();
                if (controller != null) {
                    controller.loadPatientData(selectedPatient);
                } else {
                    System.err.println("Error: Could not find EHRController!");
                }

                // 3. Open the new window
                Stage stage = new Stage();
                stage.setTitle("Medical History: " + selectedPatient.getName());
                //stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/history.png")))); // Optional icon
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();

            } catch (IOException e) {
                System.err.println("FXML Load Error: Check the path to ehr-view.fxml!");
                e.printStackTrace();
            }
        } else {
            // Use a real alert instead of just a print statement
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select a patient from the table first!");
            alert.showAndWait();
        }
    }
    // Inside MainController.java

    private String currentUserRole;

    public void setRole(String role) {
        this.currentUserRole = role;

        // If you have a label that shows who is logged in:
        // welcomeLabel.setText("Welcome, " + role);

        // This is also where you'd hide certain buttons if the role is 'DOCTOR'
        System.out.println("MainController: Role set to " + role);
    }
    // Inside MainController.java


    // Reusable Warning Popup
    private void showSelectionWarning() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText("Please select a patient first.");
        alert.showAndWait();
    }


    // 1. First, create this helper to avoid repeating yourself
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

// 2. Apply it to your buttons

    @FXML
    private void handleRemoveSelected(ActionEvent event) {
        // 1. Check if anyone is even selected first
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a patient to remove.");
            return;
        }

        // 2. Create the "Are you sure?" popup
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Patient Record");
        confirm.setContentText("Are you sure you want to permanently delete " + selected.getName() + "?");

        // 3. ONLY delete if the user clicks OK
        java.util.Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deletePatientFromDatabase(selected); // Permanently remove from SQL
            patientTable.getItems().remove(selected); // Remove from the visible list
            System.out.println("User confirmed deletion.");
        } else {
            System.out.println("Deletion canceled by user.");
        }
    }

    @FXML
    private void handleEditSelected() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // 1. BACKUP the non-editable data (The memory bank)
            String currentReg = selected.getRegisteredDate();
            String currentVisit = selected.getLastVisit();
            int originalId = selected.getId();

            // 2. UPDATE the object with new info from your UI text fields
            // (Make sure these field names match your actual FXML IDs)
            selected.setName(nameField.getText());
            selected.setDob(dobField.getText());
            selected.setGender(genderField.getText());// or genderField.getText()
            selected.setContact(contactField.getText());
            selected.setDiagnosis(diagnosisField.getText());
            selected.setTreatment(treatmentField.getText());

            // 3. RESTORE the "Amnesia" fields so they aren't overwritten by null
            selected.setRegisteredDate(currentReg);
            selected.setLastVisit(currentVisit);
            selected.setId(originalId);

            // 4. PERSIST to Database
            DatabaseManager.updatePatient(selected);

            // 5. REFRESH the Table to show changes
            refreshTable();

            System.out.println("Update successful for: " + selected.getName());
        } else {
            // Alert the user if they didn't click a patient first
            System.out.println("Please select a patient from the table first!");
        }
    }

    @FXML
    private void handleBilling(ActionEvent event) {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select a patient for billing.");
            return;
        }
        // Your logic to open the billing window
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Selection Required");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait(); // <--- This makes it pop up!
    }
    private void deletePatientFromDatabase(Patient patient) {
        // This SQL command targets the specific patient by their ID
        String query = "DELETE FROM patients WHERE id = ?";

        try (java.sql.Connection conn = com.clinic.model.DatabaseManager.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, patient.getId()); // Assuming your Patient class has getId()
            pstmt.executeUpdate();

            System.out.println("Patient " + patient.getName() + " deleted from database.");
        } catch (java.sql.SQLException e) {
            System.err.println("Database Error: Could not delete patient.");
            e.printStackTrace();
        }
    }
    private void refreshTable() {
        // 1. Fetch the latest data from the database
        List<Patient> updatedList = DatabaseManager.getAllPatients();

        // 2. Clear the old list and add the new one
        patientList.clear();
        patientList.addAll(updatedList);

        // 3. Update the UI count label (if you have one)
        updateCount();

        System.out.println("UI: Table refreshed with " + updatedList.size() + " patients.");
    }


}


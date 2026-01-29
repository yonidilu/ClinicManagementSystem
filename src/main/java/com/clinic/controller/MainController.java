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
import javafx.stage.Stage;
import java.io.IOException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.clinic.model.DatabaseManager.generateUniquePatientID;

public class MainController {
    @FXML
    private TableColumn<Patient, String> nameColumn;
    @FXML
    private TableColumn<Patient, String> dobColumn;
    @FXML
    private TableColumn<Patient, String> faydaColumn;
    @FXML
    private TableColumn<Patient, String> genderColumn;
    @FXML
    private TableColumn<Patient, String> contactColumn;
    @FXML
    private TableColumn<Patient, String> statusColumn;
    @FXML
    private TextField genderField;
    @FXML
    private TextField contactField;
    @FXML
    private TextField doctorField;
    @FXML
    private TextField statusField;
    @FXML
    private TextField diagnosisField;
    @FXML
    private TextField treatmentField;
    @FXML
    private TextField prescriptionField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private TextField apptDateField;
    @FXML
    private TextField paymentField;
    @FXML
    private Button registerButton; // This will fix the 'registerButton' red text!
    @FXML
    private TableView<Patient> patientTable;
    @FXML
    private TextField searchField;
    @FXML
    private Label countLabel;
    @FXML
    private TextField patientNameField;
    @FXML
    private TextField ailmentField;
    @FXML
    private TextField faydaField;
    @FXML
    private TableColumn<Patient, String> regDateColumn;


    private boolean isAdmin;
    private final ObservableList<Patient> patientList = FXCollections.observableArrayList();

    public void setAccessLevel(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @FXML
    public void initialize() {
        faydaField.setText(generateUniquePatientID());
        faydaField.setEditable(false); // Stop users from changing the "Official" ID!
        // 1. Manually wire columns (Fixes NullPointer at line 52)
        // IMPORTANT: Make sure these @FXML names match your top-level declarations exactly!
        if (nameColumn != null && dobColumn != null && faydaColumn != null) {
            genderColumn.setCellValueFactory(cellData -> cellData.getValue().genderProperty());
            contactColumn.setCellValueFactory(cellData -> cellData.getValue().contactProperty());
            statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
            nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
            dobColumn.setCellValueFactory(cellData -> cellData.getValue().dobProperty());
            faydaColumn.setCellValueFactory(cellData -> cellData.getValue().faydaProperty());
        } else {
            System.err.println("Critical Error: Table columns not found in FXML!");
        }

        // 2. Load data from Database
        DatabaseManager.initializeDatabase();
        patientList.setAll(DatabaseManager.getAllPatients());

        // 3. Setup FilteredList for Search
        FilteredList<Patient> filteredData = new FilteredList<>(patientList, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(patient -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String filter = newVal.toLowerCase();

                // Using standard getters to fix Search logic
                if (patient.getName().toLowerCase().contains(filter)) return true;
                return patient.getFayda().toLowerCase().contains(filter);
            });
        });

        // 4. Bind the Table to the Filtered Data
        patientTable.setItems(filteredData);
        updateCount();
        regDateColumn.setCellValueFactory(new PropertyValueFactory<>("regDate"));


        statusColumn.setCellFactory(column -> new TableCell<Patient, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle(""); // Reset style for empty cells
                } else {
                    setText(item);
                    // Apply conditional colors only to the status text
                    if (item.equalsIgnoreCase("Pending")) {
                        setStyle("-fx-text-fill: white; -fx-background-color: #e74c3c; -fx-background-radius: 5; -fx-alignment: center;");
                    } else if (item.equalsIgnoreCase("Paid")) {
                        setStyle("-fx-text-fill: white; -fx-background-color: #2ecc71; -fx-background-radius: 5; -fx-alignment: center;");
                    } else if (item.equalsIgnoreCase("Partial")) {
                        setStyle("-fx-text-fill: black; -fx-background-color: #f1c40f; -fx-background-radius: 5; -fx-alignment: center;");
                    } else {
                        setStyle(""); // Default style for other statuses
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
            DatabaseManager.updatePatient(p);

            // Better way to update the local list without index errors:
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
        patientNameField.clear();
        genderField.clear();
        ailmentField.clear();
        faydaField.clear();
        contactField.clear();
        statusField.clear();

        // Add these new lines for the fields that weren't clearing:
        doctorField.clear();
        diagnosisField.clear();
        treatmentField.clear();
        prescriptionField.clear();
        apptDateField.clear();
        paymentField.clear();
        faydaField.setText(generateUniquePatientID());
    }
    @FXML
    private void onEditButtonClick() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // These are likely working:
            patientNameField.setText(selected.getName());
            genderField.setText(selected.getGender());
            ailmentField.setText(selected.getDob());
            faydaField.setText(selected.getFayda());
            contactField.setText(selected.getContact());
            statusField.setText(selected.getStatus());

            // ADD THESE LINES to load the "disappearing" info:
            doctorField.setText(selected.getDoctorName());
            diagnosisField.setText(selected.getDiagnosis());
            treatmentField.setText(selected.getTreatment());
            prescriptionField.setText(selected.getPrescription());
            apptDateField.setText(selected.getApptDate());
            paymentField.setText(selected.getPayment());

            // Set the UI to "Update Mode"
            registerButton.setText("Save Changes");
            faydaField.setEditable(false);
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
}


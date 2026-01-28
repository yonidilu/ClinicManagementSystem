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
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController {
    @FXML
    private TableColumn<Patient, String> nameColumn;
    @FXML
    private TableColumn<Patient, String> ailmentColumn;
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

    private boolean isAdmin;
    private final ObservableList<Patient> patientList = FXCollections.observableArrayList();

    public void setAccessLevel(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @FXML
    public void initialize() {
        // 1. Manually wire columns (Fixes NullPointer at line 52)
        // IMPORTANT: Make sure these @FXML names match your top-level declarations exactly!
        if (nameColumn != null && ailmentColumn != null && faydaColumn != null) {
            genderColumn.setCellValueFactory(cellData -> cellData.getValue().genderProperty());
            contactColumn.setCellValueFactory(cellData -> cellData.getValue().contactProperty());
            statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
            nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
            ailmentColumn.setCellValueFactory(cellData -> cellData.getValue().ailmentProperty());
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
    }

    public void updateCount() {
        if (countLabel != null) {
            countLabel.setText(String.valueOf(patientList.size()));
        }
    }

    @FXML
    private void onRegisterButtonClick() {
        try {
            // 1. PULL the values from fields FIRST
            String name = patientNameField.getText();
            String ailment = ailmentField.getText();
            String gender = genderField.getText();
            String contact = contactField.getText();
            String status = statusField.getText();
            String fayda = faydaField.getText();
            String doctor = (doctorField != null) ? doctorField.getText() : "Dr. Shimels";

            // 2. NOW create the patient object
            Patient newPatient = new Patient(name, ailment, gender, contact, status, fayda,
                    doctor, "Cholera", "Pills", "300mg", "1/30/26", "30.000", "1/28/26");

            // 3. Save it
            DatabaseManager.savePatient(newPatient);
            patientList.add(newPatient);
            updateCount();
            clearFields();

        } catch (Exception e) {
            System.out.println("Error saving: " + e.getMessage());
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
        TextField[] allFields = {patientNameField, ailmentField, faydaField, genderField, contactField, statusField, doctorField};
        for (TextField f : allFields) {
            if (f != null) f.clear();
        }
    }
}


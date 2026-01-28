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

public class MainController {

    @FXML
    private TableView<Patient> patientTable;
    @FXML
    private TextField searchField;
    @FXML
    private Label countLabel;

    // These must match the fx:id in your main-view.fxml
    @FXML
    private TextField patientNameField;
    @FXML
    private TextField ailmentField;
    @FXML
    private TextField faydaField;

    private boolean isAdmin;
    private final ObservableList<Patient> patientList = FXCollections.observableArrayList();

    /**
     * FIXES Build Error in LoginController (image_9a1a00.png)
     */
    public void setAccessLevel(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @FXML
    public void initialize() {
        // 1. Load data from Database
        DatabaseManager.initializeDatabase();
        patientList.setAll(DatabaseManager.getAllPatients());

        // 2. Setup FilteredList (FIXES image_cc1f36.jpg imports)
        FilteredList<Patient> filteredData = new FilteredList<>(patientList, p -> true);

        // 3. Search Logic
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(patient -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String filter = newVal.toLowerCase();

                // Matches Name or Fayda ID
                if (patient.getName().toLowerCase().contains(filter)) return true;
                return String.valueOf(patient.getFayda()).contains(filter);
            });
        });

        // 4. Bind Table (FIXES "Double Binding" in image_cbfc7a.png)
        patientTable.setItems(filteredData);
        updateCount();
    }

    public void updateCount() {
        if (countLabel != null) {
            countLabel.setText(String.valueOf(patientList.size()));
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));

            // KEEP IT FULL SCREEN
            stage.setMaximized(true);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRegisterButtonClick() {
        try {
            String name = patientNameField.getText();
            String ailment = ailmentField.getText();
            // Convert the String from the text box into an Integer for the DB
            int faydaId = Integer.parseInt(faydaField.getText());

            DatabaseManager.addPatient(name, ailment, faydaId);

            // Refresh the list and UI
            patientList.setAll(DatabaseManager.getAllPatients());
            updateCount();

            // Clear fields for the next patient
            patientNameField.clear();
            ailmentField.clear();
            faydaField.clear();

        } catch (NumberFormatException e) {
            System.out.println("Error: Fayda ID must be a number!");
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

    @FXML
    private void onShowDetails(ActionEvent event) {
        // 1. Get the patient currently highlighted in the table
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();

        if (selectedPatient == null) {
            // Use an Alert to tell the user to select someone first!
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Please select a patient first.");
            alert.show();
            return;
        }

        try {
            // 2. Load the Details FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/details-view.fxml"));
            Parent root = loader.load();

            // 3. Pass the patient data to the DetailsController
            DetailsController controller = loader.getController();
            controller.setPatientData(selectedPatient);

            // 4. Open the new window
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Patient Details - " + selectedPatient.getName());
            stage.show();

        } catch (IOException e) {
            System.out.println("Could not load details window!");
            e.printStackTrace();
        }
    }
}
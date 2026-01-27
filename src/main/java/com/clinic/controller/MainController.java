package com.clinic.controller;

import com.clinic.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {
    @FXML private TextField patientNameField;
    @FXML private TextField ailmentField;
    @FXML private TextField faydaField;
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, String> nameColumn;
    @FXML private TableColumn<Patient, String> ailmentColumn;
    @FXML private TableColumn<Patient, String> faydaColumn;
    private final ObservableList<Patient> patientList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Link columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ailmentColumn.setCellValueFactory(new PropertyValueFactory<>("ailment"));

        // Setup Search Filter
        FilteredList<Patient> filteredData = new FilteredList<>(patientList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(patient -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return patient.getName().toLowerCase().contains(lowerCaseFilter);
            });
        });
        patientTable.setRowFactory(tv -> new TableRow<Patient>() {
            @Override
            protected void updateItem(Patient item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.getAilment().toLowerCase().contains("urgent")) {
                    setStyle("-fx-background-color: #ff7675;"); // Light red for emergencies
                } else {
                    setStyle("");
                }
            }
        });

        patientTable.setItems(filteredData);
        loadFromFile();
    }

    @FXML
    protected void onRegisterButtonClick() {
        String name = patientNameField.getText();
        String ailment = ailmentField.getText();
        String fayda = faydaField.getText();

        // 1. Check if the clinic is full (Limit: 5)
        if (patientList.size() >= 5) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Clinic Full");
            alert.setHeaderText("Maximum Capacity Reached");
            alert.setContentText("The clinic cannot hold more than 5 patients. Please remove a patient before adding a new one.");
            alert.showAndWait();
            return; // This stops the method right here!
        }

        // 2. If not full, proceed with adding
        // Use .getText().isEmpty() instead of just .isEmpty()
        if (!name.isEmpty() && !ailment.isEmpty() && !fayda.isEmpty()) {
            patientList.add(new Patient(name, 30, ailment, fayda));
            patientNameField.clear();
            ailmentField.clear();
            faydaField.clear();
            updateCount();
            saveToFile(); // Remember to save the change!
        }
    }

    @FXML
    protected void onDeleteButtonClick() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            patientList.remove(selected);
            updateCount();
        }
    }

    private void updateCount() {
        countLabel.setText("Total Patients: " + patientList.size());
    }
    private void saveToFile() {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter("patients.txt"))) {
            for (Patient p : patientList) {
                // Saving as: Name,Age,Ailment
                writer.println(p.getName() + "," + p.getAge() + "," + p.getAilment());
            }
        } catch (java.io.IOException e) {
            System.out.println("Error saving patients: " + e.getMessage());
        }
    }

    private void loadFromFile() {
        java.io.File file = new java.io.File("patients.txt");
        if (!file.exists()) return;

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    patientList.add(new Patient(data[0], Integer.parseInt(data[1]), data[2], data[3]));
                }
            }
            updateCount();
        } catch (java.io.IOException e) {
            System.out.println("Error loading patients: " + e.getMessage());
        }
    }
    @FXML
    protected void onShowDetails() {
        // 1. Check if a patient is actually selected in the table
        Patient selected = patientTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            try {
                // 2. Load the new FXML file for the popup
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/details-view.fxml"));
                Parent root = loader.load();

                // 3. Get the controller of the POPUP window and send the patient data to it
                DetailsController controller = loader.getController();
                controller.setPatientData(selected);

                // 4. Create and show the new window (Stage)
                Stage stage = new Stage();
                stage.setTitle("Medical Record: " + selected.getName());
                stage.setScene(new Scene(root, 350, 300));

                // This makes the main window unclickable until the popup is closed
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();

            } catch (IOException e) {
                System.out.println("Could not open details window: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Show a warning if they didn't click a patient first
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Please select a patient from the list first!");
            alert.show();
        }
    }
    @FXML
    private void handleLogout() {
        // This just closes the current dashboard and re-runs your Main.start() logic!
        System.exit(0);
    }
}
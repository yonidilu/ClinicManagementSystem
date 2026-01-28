package com.clinic.controller;

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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.*;
import java.util.ArrayList;

public class MainController {
    @FXML private TextField patientNameField, ailmentField, faydaField;
    @FXML private TableView<Patient> patientTable;
    @FXML private Label countLabel;
    @FXML private HBox adminControls;

    private final ObservableList<Patient> patientList = FXCollections.observableArrayList();
    private final String DATA_FILE = "patients.dat";

    @FXML
    public void initialize() {
        loadData(); // Restores memory
        if (patientTable != null) { patientTable.setItems(patientList); }
        updateCount();
    }

    // FIXES LOGIN ERROR: "cannot find symbol method setAccessLevel"
    public void setAccessLevel(boolean isAdmin) {
        if (adminControls != null) {
            adminControls.setVisible(isAdmin);
            adminControls.setManaged(isAdmin);
        }
    }

    @FXML
    private void onRegisterButtonClick() {
        if (!patientNameField.getText().isEmpty()) {
            patientList.add(new Patient(patientNameField.getText(), ailmentField.getText(), faydaField.getText()));
            saveData();
            patientNameField.clear(); ailmentField.clear(); faydaField.clear();
            updateCount();
        }
    }

    @FXML
    private void onShowDetails() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/clinic/view/details-view.fxml"));
            Parent root = loader.load();
            DetailsController controller = loader.getController();
            controller.setPatient(selected);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/clinic/view/choice-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 400));
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void onDeleteButtonClick() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            patientList.remove(selected);
            saveData();
            updateCount();
        }
    }

    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(new ArrayList<>(patientList));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            ArrayList<Patient> loaded = (ArrayList<Patient>) ois.readObject();
            patientList.setAll(loaded);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateCount() {
        if (countLabel != null) countLabel.setText("Total Patients: " + patientList.size());
    }
}
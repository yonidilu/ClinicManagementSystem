package com.clinic.controller;

import com.clinic.model.Encounter; // You will need an Encounter model class
import com.clinic.model.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class HistoryController {
    @FXML private TableView<Encounter> historyTable;
    @FXML private TableColumn<Encounter, String> dateCol, diagCol, treatCol, prescCol;

    public void initData(String fayda) {
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        diagCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        treatCol.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        prescCol.setCellValueFactory(new PropertyValueFactory<>("prescription"));

        historyTable.setItems(DatabaseManager.getPatientEncounters(fayda));
    }

    @FXML private void handleClose() {
        ((Stage) historyTable.getScene().getWindow()).close();
    }
}
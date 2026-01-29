package com.clinic.controller;

import com.clinic.model.Patient;
import com.clinic.model.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import com.clinic.model.LabResult; // This connects the Controller to your Model class
import com.clinic.model.DatabaseManager; // Connects to your database logic
import javafx.collections.FXCollections; // Required for TableView items
import java.util.List; // Required for the list of results
import java.util.Optional;

import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.collections.FXCollections;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import javafx.stage.FileChooser;
import java.io.File;
// You might also need these for the PDF logic we added earlier:
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

public class LabController {
    @FXML private TableColumn<LabResult, String> testColumn;
    @FXML private TableColumn<LabResult, String> resultColumn;
    @FXML private TableColumn<LabResult, String> dateColumn;
    @FXML private TableView<LabResult> labTable; // Add the <LabResult> here!
    @FXML private Label patientInfoLabel;


    @FXML private ComboBox<String> testComboBox; // Replaces testInput
    @FXML private TextField resultInput;


    private Patient currentPatient;

    public void setPatient(Patient patient) {
        // Adding common clinical tests to the dropdown
        testComboBox.setItems(FXCollections.observableArrayList(
                "Blood Sugar",
                "Cholesterol",
                "Hemoglobin",
                "Blood Pressure",
                "Urine Analysis",
                "Malaria Test"
        ));

        this.currentPatient = patient;
        patientInfoLabel.setText("Results for: " + patient.getName());

        // Setup table columns
        testColumn.setCellValueFactory(cellData -> cellData.getValue().testNameProperty());
        resultColumn.setCellValueFactory(cellData -> cellData.getValue().resultValueProperty());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().testDateProperty());

        refreshTable();
    }

    private void refreshTable() {
        if (currentPatient != null) {
            // Fetch from DB using the unique fayda ID
            List<LabResult> results = DatabaseManager.getLabResults(currentPatient.getFayda());
            labTable.setItems(FXCollections.observableList(results));
        }
    }

    @FXML
    private void handleAddResult() {
        String selectedTest = testComboBox.getValue(); // Get value from dropdown
        String resultVal = resultInput.getText();

        // Validation: Ensure both are filled
        if (selectedTest == null || resultVal.isEmpty()) {
            System.out.println("Please select a test and enter a result!");
            return;
        }

        if (currentPatient != null) {
            DatabaseManager.addLabResult(
                    currentPatient.getFayda(),
                    selectedTest,
                    resultVal,
                    java.time.LocalDate.now().toString()
            );

            resultInput.clear();
            refreshTable(); // Update the UI immediately
        }
    }
    @FXML
    private void handleDeleteResult() {
        LabResult selected = (LabResult) labTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            System.out.println("No result selected for deletion.");
            return;
        }

        // Create the confirmation alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Removing Lab Result");
        alert.setContentText("Are you sure you want to delete the '" + selected.getTestName() + "' result?");

        // Show the alert and wait for the user to click a button
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Proceed with deletion if OK was clicked
            DatabaseManager.deleteLabResult(currentPatient.getFayda(), selected.getTestName());
            refreshTable();
            System.out.println("Result deleted successfully.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }
    @FXML
    private void handlePrintPDF() {
        if (currentPatient == null) return;

        // 1. Setup the File Selection Window
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Lab Report");
        fileChooser.setInitialFileName("LabReport_" + currentPatient.getName().replace(" ", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        // 2. Show the "Save" dialog
        File file = fileChooser.showSaveDialog(labTable.getScene().getWindow());

        if (file != null) {
            try {
                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Report Content
                document.add(new Paragraph("CLINIC PATIENT LAB REPORT").setBold().setFontSize(18));
                document.add(new Paragraph("Patient: " + currentPatient.getName() + " (ID: " + currentPatient.getFayda() + ")"));
                document.add(new Paragraph("Generated on: " + java.time.LocalDate.now()));
                document.add(new Paragraph("\n"));

                // Table for Results
                Table table = new Table(3);
                table.addCell("Test Name");
                table.addCell("Result");
                table.addCell("Date");

                // This loop now works perfectly thanks to TableView<LabResult>!
                for (LabResult res : labTable.getItems()) {
                    table.addCell(res.getTestName());
                    table.addCell(res.getResultValue());
                    table.addCell(res.getTestDate());
                }

                document.add(table);
                document.close();

                System.out.println("Report saved to: " + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

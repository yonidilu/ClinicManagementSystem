package com.clinic.controller;

import com.clinic.model.Patient;
import com.clinic.model.LabResult;
import com.clinic.model.DatabaseManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.*;
import java.io.FileOutputStream;
import java.time.LocalDate;

public class EHRController {

    // UI Connections from ehr-view.fxml
    @FXML private Label nameLabel;
    @FXML private Label idLabel; // Shows PT-26962
    @FXML private Label faydaLabel;
    @FXML private Label dobLabel;
    @FXML private Label diagnosisLabel;
    @FXML private TextArea treatmentArea;
    @FXML private TextArea prescriptionArea;
    @FXML private TableView<LabResult> labTable;
    @FXML private TableColumn<LabResult, String> testCol;
    @FXML private TableColumn<LabResult, String> resultCol;
    @FXML private TableColumn<LabResult, String> dateCol;

    //PDF Header Font
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font BODY_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

    @FXML
    public void initialize() {
        // Prepare table columns
        testCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTestName()));
        resultCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getResultValue()));
        dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTestDate()));
    }

    public void loadPatientData(Patient patient) {
        if (patient != null) {
            nameLabel.setText(patient.getName());
            idLabel.setText(patient.getFayda());
            faydaLabel.setText(patient.getFayda());
            dobLabel.setText(patient.getDob());
            diagnosisLabel.setText(patient.getDiagnosis());
            treatmentArea.setText(patient.getTreatment());
            prescriptionArea.setText(patient.getPrescription());

            // Load data from DB
            labTable.setItems(DatabaseManager.getPatientLabs(patient.getFayda()));
        }
    }

    @FXML
    private void handleSaveEHR() {
        Patient p = new Patient();
        p.setFayda(faydaLabel.getText());
        p.setDiagnosis(diagnosisLabel.getText());
        p.setTreatment(treatmentArea.getText());
        p.setPrescription(prescriptionArea.getText());
        DatabaseManager.updatePatient(p);
        System.out.println("EHR Saved for: " + p.getFayda());
    }

    @FXML
    public void exportMedicalReport() {
        String patientId = idLabel.getText();
        String path = "Medical_Report_" + patientId + ".pdf";

        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            // Report Content
            document.add(new Paragraph("CLINIC MEDICAL REPORT", HEADER_FONT));
            document.add(new Paragraph("Date: " + LocalDate.now(), BODY_FONT));
            document.add(new Paragraph("---------------------------------------"));
            document.add(new Paragraph("Patient Name: " + nameLabel.getText(), BODY_FONT));
            document.add(new Paragraph("Patient ID: " + patientId, BODY_FONT));

            document.add(new Paragraph("\nTreatment Plan:", HEADER_FONT));
            document.add(new Paragraph(treatmentArea.getText(), BODY_FONT));

            document.add(new Paragraph("\nPrescription:", HEADER_FONT));
            document.add(new Paragraph(prescriptionArea.getText(), BODY_FONT));

            // Lab Results Table
            document.add(new Paragraph("\nLab Test History:", HEADER_FONT));
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.addCell("Test Name"); table.addCell("Result"); table.addCell("Date");

            for (LabResult result : labTable.getItems()) {
                table.addCell(result.getTestName()); // e.g. Blood Sugar
                table.addCell(result.getResultValue()); // e.g. 3
                table.addCell(result.getTestDate()); // e.g. 2026-01-30
            }
            document.add(table);

            document.close();
            showAlert("Success", "Medical Report saved to " + path);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not generate PDF: " + e.getMessage());
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Report Generated");
        alert.setHeaderText(null);
        alert.setContentText("Medical Report for " + nameLabel.getText() + " has been saved!");
        alert.showAndWait();
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
package com.clinic.controller;

import com.clinic.model.BillItem;
import com.clinic.model.DatabaseManager;
import com.clinic.model.LabResult;
import com.clinic.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

public class BillingController {

    @FXML private Label patientNameLabel;
    @FXML private Label patientIDLabel;
    @FXML private Label transactionIDLabel;
    @FXML private Label totalLabel;

    @FXML private TableView<BillItem> billTable;
    @FXML private TableColumn<BillItem, String> itemCol;
    @FXML private TableColumn<BillItem, Double> costCol;

    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField priceInput;

    private Patient currentPatient;
    private ObservableList<BillItem> currentBillItems = FXCollections.observableArrayList();
    private double calculatedTotal = 0.0;

    @FXML
    public void initialize() {
        // FIXED: Wrap structural assignments in safe NULL guards to prevent layout loader crashes
        if (itemCol != null) {
            itemCol.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        } else {
            System.err.println("WARNING: 'itemCol' was not injected properly. Check fx:id in billing-view.fxml");
        }

        if (costCol != null) {
            costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));
        } else {
            System.err.println("WARNING: 'costCol' was not injected properly. Check fx:id in billing-view.fxml");
        }

        if (statusComboBox != null) {
            statusComboBox.setItems(FXCollections.observableArrayList("Paid", "Pending", "Partially Paid"));
        }

        if (MainController.selectedPatientSessionContext != null) {
            this.currentPatient = MainController.selectedPatientSessionContext;
            initializeBillingContext(currentPatient);
        } else {
            if (transactionIDLabel != null) {
                transactionIDLabel.setText("TXN-" + System.currentTimeMillis() / 1000);
            }
        }
    }

    public void loadPatientBillingProfile(String fayda, String patientName) {
        if (fayda == null) return;

        Patient patient = new Patient();
        patient.setFayda(fayda);
        patient.setName(patientName);

        initializeBillingContext(patient);
    }

    public void initializeBillingContext(Patient patient) {
        if (patient == null) return;
        this.currentPatient = patient;

        if (patientNameLabel != null) patientNameLabel.setText(patient.getName());
        if (patientIDLabel != null) patientIDLabel.setText(patient.getFayda());

        if (transactionIDLabel != null) {
            transactionIDLabel.setText("TXN-" + (System.currentTimeMillis() / 10000) + "-" + Math.abs(patient.getFayda().hashCode() % 1000));
        }

        if (statusComboBox != null) {
            statusComboBox.setValue(patient.getPaymentStatus() != null ? patient.getPaymentStatus() : "Pending");
        }

        loadBillingData();
    }

    @FXML
    private void loadBillingData() {
        if (currentPatient == null || billTable == null) return;

        currentBillItems.clear();
        calculatedTotal = 0.0;

        currentBillItems.add(new BillItem("General Physician Consultation Fee", 250.00));
        calculatedTotal += 250.00;

        List<LabResult> results = DatabaseManager.getPatientLabs(currentPatient.getFayda());
        if (results != null) {
            for (LabResult res : results) {
                double testCost = mapTestToPriceProfile(res.getTestName());
                currentBillItems.add(new BillItem("Laboratory Panel: " + res.getTestName(), testCost));
                calculatedTotal += testCost;
            }
        }

        billTable.setItems(currentBillItems);
        if (totalLabel != null) {
            totalLabel.setText(String.format("%.2f", calculatedTotal));
        }
    }

    private double mapTestToPriceProfile(String testName) {
        if (testName == null) return 0.0;
        return switch (testName) {
            case "Complete Blood Count (CBC)" -> 180.00;
            case "Basic Metabolic Panel (BMP)" -> 220.00;
            case "Lipid Panel" -> 300.00;
            case "Thyroid Function Panel (TSH)" -> 350.00;
            case "Hemoglobin A1C (HbA1c)" -> 250.00;
            default -> 150.00;
        };
    }

    @FXML
    private void handleApplyPrice(ActionEvent event) {
        if (billTable == null) return;

        BillItem selectedItem = billTable.getSelectionModel().getSelectedItem();
        String customPriceText = (priceInput != null) ? priceInput.getText().trim() : "";

        if (selectedItem == null) {
            displayAlert("Selection Omission", "Highlight a billing row item to adjust pricing variables.");
            return;
        }

        try {
            double verifiedPrice = Double.parseDouble(customPriceText);
            if (verifiedPrice < 0) throw new NumberFormatException();

            selectedItem.setCost(verifiedPrice);
            recalculateTotal();
            if (priceInput != null) priceInput.clear();

        } catch (NumberFormatException e) {
            displayAlert("Invalid Format", "Please input a positive numerical parameter.");
        }
    }

    @FXML
    private void handleTenPercentDiscount(ActionEvent event) {
        if (billTable == null) return;

        BillItem selectedItem = billTable.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            displayAlert("Selection Omission", "Select an active service item row to apply a 10% deduction.");
            return;
        }

        if (selectedItem.isDiscounted()) {
            displayAlert("Rule Violation", "A financial adjustment markdown has already been committed for this entry.");
            return;
        }

        double currentCost = selectedItem.getCost();
        selectedItem.setCost(currentCost * 0.90);
        selectedItem.setDiscounted(true);

        recalculateTotal();
    }

    private void recalculateTotal() {
        if (billTable == null) return;

        calculatedTotal = 0.0;
        for (BillItem item : billTable.getItems()) {
            calculatedTotal += item.getCost();
        }
        if (totalLabel != null) {
            totalLabel.setText(String.format("%.2f", calculatedTotal));
        }
        billTable.refresh();
    }

    @FXML
    private void handlePayment(ActionEvent event) {
        if (currentPatient == null) return;

        String updatedStatus = (statusComboBox != null) ? statusComboBox.getValue() : "Pending";
        currentPatient.setPaymentStatus(updatedStatus);
        currentPatient.setPaymentAmount(String.valueOf(calculatedTotal));

        double balanceOwedValue = "Paid".equalsIgnoreCase(updatedStatus) ? 0.0 : calculatedTotal;
        currentPatient.setBalanceOwed(balanceOwedValue);

        DatabaseManager.updatePatient(currentPatient);
        updateFinancialLedgerInDatabase(currentPatient.getFayda(), balanceOwedValue);

        displayAlert("Ledger Commit Complete", "Patient account states updated to: " + updatedStatus);
    }

    private void updateFinancialLedgerInDatabase(String fayda, double balance) {
        String query = "UPDATE patients SET balance_owed = ? WHERE fayda = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, balance);
            pstmt.setString(2, fayda);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to write updated balance parameters to database: " + e.getMessage());
        }
    }

    @FXML
    private void handlePrint(ActionEvent event) {
        if (currentPatient == null || billTable == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Financial Receipt");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Documents (*.pdf)", "*.pdf"));
        chooser.setInitialFileName("Receipt_Fayda_" + currentPatient.getFayda() + ".pdf");

        File file = chooser.showSaveDialog(billTable.getScene().getWindow());

        if (file != null) {
            try {
                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);
                Document doc = new Document(pdf);

                doc.add(new Paragraph("CLINIC RECEPTION STATEMENT & RECEIPT").setBold().setFontSize(16));
                doc.add(new Paragraph("========================================================================="));
                doc.add(new Paragraph("Transaction ID: " + (transactionIDLabel != null ? transactionIDLabel.getText() : "N/A")));
                doc.add(new Paragraph("Patient Name: " + currentPatient.getName()));
                doc.add(new Paragraph("National Identity ID (Fayda): " + currentPatient.getFayda()));
                doc.add(new Paragraph("Date: " + LocalDate.now()));
                doc.add(new Paragraph("Settlement Status: " + (statusComboBox != null ? statusComboBox.getValue() : "N/A")));
                doc.add(new Paragraph("\n"));

                Table structuralTable = new Table(2);
                structuralTable.addCell("Service Description Item");
                structuralTable.addCell("Cost (ETB)");

                for (BillItem item : billTable.getItems()) {
                    structuralTable.addCell(item.getServiceName());
                    structuralTable.addCell(String.format("%.2f", item.getCost()));
                }

                doc.add(structuralTable);
                doc.add(new Paragraph("\n-------------------------------------------------------------------------"));
                doc.add(new Paragraph("Total Charge: " + (totalLabel != null ? totalLabel.getText() : "0.00") + " ETB").setBold());
                doc.close();

                displayAlert("Receipt Generated", "Official document tracking sheet exported to: " + file.getName());
            } catch (Exception e) {
                displayAlert("PDF System Exception", "Failed to print data entries down to receipt format map.");
                e.printStackTrace();
            }
        }
    }

    private void displayAlert(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(header);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
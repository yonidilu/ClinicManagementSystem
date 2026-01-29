package com.clinic.controller;

import com.clinic.model.BillItem;
import com.clinic.model.DatabaseManager;
import com.clinic.model.LabResult;
import com.clinic.model.Patient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.util.List;
import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;
import javafx.print.PrinterJob;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class BillingController {

    // These MUST exist in billing-view.fxml as fx:id
    @FXML private Label patientNameLabel;
    @FXML private Label patientIDLabel;
    @FXML private Label transactionIDLabel;
    @FXML private Label totalLabel;
    @FXML private TableView<BillItem> billTable;
    @FXML private TableColumn<BillItem, String> serviceColumn;
    @FXML private TableColumn<BillItem, Double> amountColumn;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Button discountButton;

    private Patient currentPatient;


    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        patientNameLabel.setText(patient.getName());
        patientIDLabel.setText(patient.getFayda()); // This is the ID/Payment Code

        // Generate a unique Transaction Number
        String txID = "TXN-" + System.currentTimeMillis();
        transactionIDLabel.setText(txID);

        loadBillingData();
    }

    @FXML
    public void initialize() {
        // 1. Link Table Columns to BillItem properties
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));

        // 2. Populate the Status Dropdown
        statusComboBox.setItems(FXCollections.observableArrayList("Pending", "Paid", "Partial"));

        // 3. Selection Listener: Auto-fill Price & Manage Discount Button
        billTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Fill the input box with the current cost
                priceInput.setText(String.valueOf(newSelection.getCost()));

                // Toggle the discount button: Unclickable if already discounted
                discountButton.setDisable(newSelection.isDiscounted());
            } else {
                // Clean up if nothing is selected
                priceInput.clear();
                discountButton.setDisable(true);
            }
        });
    }


    private void loadBillingData() {
        if (currentPatient == null || billTable == null) return;

        billTable.getItems().clear();
        double total = 0;

        // Fetch results from DB based on Patient ID (Fayda)
        List<LabResult> results = DatabaseManager.getLabResults(currentPatient.getFayda());

        for (LabResult res : results) {
            double testCost = 50.00; // Standard clinic fee
            billTable.getItems().add(new BillItem(res.getTestName(), testCost));
            total += testCost;
        }

        totalLabel.setText(String.format("$%.2f", total));
    }

    // This goes in BillingController.java
    @FXML
    private void handlePayment() {
        try {
            // 1. Collect data from the UI
            String amount = totalLabel.getText().replace("$", "");
            String status = statusComboBox.getValue();
            String id = currentPatient.getFayda(); // Using 'fayda' to match your DB

            if (status == null) {
                System.out.println("Wait! You didn't select a status.");
                return;
            }

            // 2. The "Handshake" with the Database
            // 2. The "Handshake" with the Database
            // We use 'id' and 'status' because those are the variables you defined at lines 99-100
            DatabaseManager.updateBillingInfo(id, amount, status);

              // This updates the UI object immediately
            currentPatient.setPaymentStatus(status);

            System.out.println("UI DEBUG: Table updated for " + currentPatient.getName());
            // 3. Update the local patient object so the Main Table sees the change
            currentPatient.setPayment(amount);
            currentPatient.setStatus(status);

            System.out.println("Success! Database updated for " + currentPatient.getName());

            // 4. Close the window so the user knows it's done
            Stage stage = (Stage) totalLabel.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            System.err.println("Something went wrong in the Controller: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveReceipt(String txID, String amount, String status) {
        try {
            // Create a receipts folder if it doesn't exist
            File directory = new File("receipts");
            if (!directory.exists()) directory.mkdir();

            File file = new File("receipts/" + txID + ".txt");
            PrintWriter writer = new PrintWriter(file);

            writer.println("==================================");
            writer.println("       CLINIC PAYMENT RECEIPT     ");
            writer.println("==================================");
            writer.println("Transaction ID: " + txID);
            writer.println("Patient Name:   " + currentPatient.getName());
            writer.println("Patient ID:     " + currentPatient.getFayda());
            writer.println("Date:           " + java.time.LocalDate.now());
            writer.println("----------------------------------");
            writer.println("Amount Paid:    $" + amount);
            writer.println("Status:         " + status);
            writer.println("==================================");
            writer.println("   Thank you for your visit!      ");

            writer.close();
            System.out.println("Receipt saved: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error generating receipt: " + e.getMessage());
        }
    }

    private void printReceipt(String txID, String amount) {
        // 1. Create a "Printable" version of your receipt UI
        Text title = new Text("CLINIC RECEIPT\n");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Text details = new Text(String.format(
                "Transaction: %s\nPatient: %s\nAmount: $%s\nDate: %s",
                txID, currentPatient.getName(), amount, java.time.LocalDate.now()
        ));

        TextFlow printArea = new TextFlow(title, details);
        printArea.setPadding(new javafx.geometry.Insets(20));

        // 2. Start the Print Job
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(totalLabel.getScene().getWindow())) {
            boolean success = job.printPage(printArea);
            if (success) {
                job.endJob();
            }
        }
    }
    @FXML
    private void handlePrint() {
        // This stays as a separate, optional action for the receptionist
        javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(totalLabel.getScene().getWindow())) {
            // Print logic here...
            job.endJob();
        }
    }
    @FXML private TextField priceInput; // New FXML field

    @FXML
    private void handleApplyPrice() {
        // 1. Get the selected test from the table
        BillItem selectedItem = billTable.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            try {
                // 2. Parse the user input
                double newPrice = Double.parseDouble(priceInput.getText());

                // 3. Update the item (This works because of the Properties we added!)
                // Note: You might need to add setCost() to your BillItem class
                selectedItem.setCost(newPrice);

                // 4. Recalculate the total label
                updateTotal();

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number for the price.");
            }
        } else {
            System.out.println("Please select a test from the table first.");
        }
    }

    // Helper method to refresh the total whenever a price changes
    private void updateTotal() {
        double total = 0;
        for (BillItem item : billTable.getItems()) {
            total += item.getCost();
        }
        totalLabel.setText(String.format("$%.2f", total));

    }
    @FXML
    private void handleTenPercentDiscount() {
        BillItem selectedItem = billTable.getSelectionModel().getSelectedItem();

        if (selectedItem != null && !selectedItem.isDiscounted()) {
            double discountedPrice = selectedItem.getCost() * 0.90;
            selectedItem.setCost(discountedPrice);

            // Mark as discounted and disable the button
            selectedItem.setDiscounted(true);
            discountButton.setDisable(true);

            priceInput.setText(String.format("%.2f", discountedPrice));
            updateTotal();
        }
    }
}
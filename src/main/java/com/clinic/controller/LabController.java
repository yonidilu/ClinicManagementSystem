package com.clinic.controller;

import com.clinic.model.Patient;
import com.clinic.model.DatabaseManager;
import com.clinic.model.LabResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import javafx.application.Platform;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

public class LabController {

    public VBox techControlsContainer;
    @FXML private Label patientInfoLabel;

    // Lab Results Table Binding Variables - Swapped to a Unified Queue System
    @FXML private TableView<LabResult> labTable;
    @FXML private TableColumn<LabResult, String> testColumn;
    @FXML private TableColumn<LabResult, String> resultColumn;
    @FXML private TableColumn<LabResult, String> dateColumn;

    // NEW COLUMNS: Added to trace the Patient Identity and the Ordering Doctor explicitly
    @FXML private TableColumn<LabResult, String> faydaColumn;
    @FXML private TableColumn<LabResult, String> orderingDoctorColumn;

    // CHANGED: Injected Action Column to render the custom inline Edit buttons
    @FXML private TableColumn<LabResult, Void> actionColumn;

    // Interactive Node Components
    @FXML private ComboBox<String> testComboBox;
    @FXML private TextField resultInput;

    private ObservableList<LabResult> activeLabList = FXCollections.observableArrayList();

    // Context field tracker to distinguish between a clean log commit and an inline row mutation edit
    private LabResult editingRecordContext = null;

    @FXML
    public void initialize() {
        // 1. Map table properties to display a complete, shared list across the clinic
        if (faydaColumn != null) {
            faydaColumn.setCellValueFactory(new PropertyValueFactory<>("patientFayda"));
        }
        testColumn.setCellValueFactory(new PropertyValueFactory<>("testName"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("resultValue"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("testDate"));

        if (orderingDoctorColumn != null) {
            orderingDoctorColumn.setCellValueFactory(new PropertyValueFactory<>("orderingDoctor"));
        }

        // Initialize the custom cell factory layout to populate inline buttons
        setupActionColumnButtons();

        // 2. Load the professional selection styling
        try {
            labTable.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("CSS stylesheet not found: " + e.getMessage());
        }

        // 3. SECURITY: Hide technician controls if user is not a LAB_UNIT
        String role = DatabaseManager.getCurrentUserRole();
        if (!"LAB_UNIT".equals(role)) {
            techControlsContainer.setVisible(false);
            techControlsContainer.setManaged(false); // Collapses the layout
        }

        // 4. Populate available drop-down profiles for execution choices
        if (testComboBox != null) {
            testComboBox.setItems(FXCollections.observableArrayList(
                    "Complete Blood Count (CBC)",
                    "Basic Metabolic Panel (BMP)",
                    "Lipid Panel",
                    "Thyroid Function Panel (TSH)",
                    "Hemoglobin A1C (HbA1c)",
                    "Urinalysis Profile"
            ));
        }

        // 5. Set layout title and draw all outstanding orders instantly
        patientInfoLabel.setText("Clinical Pathology Lab Queue — Universal Access Active Monitoring Node");
        refreshLabTable();
    }

    // NEW: Handles the generation and click hooks of the custom inline Edit buttons
    private void setupActionColumnButtons() {
        if (actionColumn == null) return;

        Callback<TableColumn<LabResult, Void>, TableCell<LabResult, Void>> cellFactory =
                new Callback<TableColumn<LabResult, Void>, TableCell<LabResult, Void>>() {
                    @Override
                    public TableCell<LabResult, Void> call(final TableColumn<LabResult, Void> param) {
                        return new TableCell<LabResult, Void>() {
                            private final Button editBtn = new Button("Edit");

                            {
                                editBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 12px;");
                                editBtn.setOnAction(event -> {
                                    LabResult targetRowData = getTableView().getItems().get(getIndex());
                                    handleLoadRowIntoForm(targetRowData);
                                });
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(editBtn);
                                }
                            }
                        };
                    }
                };

        actionColumn.setCellFactory(cellFactory);
    }

    // NEW: Pulls data properties out of the selected table row directly down into the modification panel fields
    private void handleLoadRowIntoForm(LabResult selectedLab) {
        if (selectedLab == null) return;

        this.editingRecordContext = selectedLab;

        testComboBox.setValue(selectedLab.getTestName());
        resultInput.setText(selectedLab.getResultValue());

        resultInput.requestFocus();
    }

    // NEW: Bound to the FXML Refresh queue action to reload the data collections
    @FXML
    private void handleRefreshQueue(ActionEvent event) {
        refreshLabTable();
    }

    private void refreshLabTable() {
        activeLabList.clear();

        // Fetch every single lab test recorded across the clinic management database
        List<LabResult> allOrders = DatabaseManager.getAllLabResults();

        activeLabList.addAll(allOrders);
        labTable.setItems(activeLabList);
    }

    public void initializePatientContext(Patient patient) {
        if (patient == null) return;

        // Use this patient to filter the lab results
        this.patientInfoLabel.setText("Laboratory Results for: " + patient.getName());

        // Now call your database fetcher using the patient's ID
        loadLabResultsForPatient(patient.getFayda());
    }

    private void loadLabResultsForPatient(String fayda) {
        activeLabList.clear();
        activeLabList.addAll(DatabaseManager.getPatientLabs(fayda));
        labTable.setItems(activeLabList);
    }

    @FXML
    private void handleAddResult(ActionEvent event) {
        String resultText = resultInput.getText().trim();
        String selectedTest = testComboBox.getValue();

        if (selectedTest == null) {
            displayStatusAlert("Input Verification Error", "Please select a diagnostic test type profile option.");
            return;
        }

        if (resultText.isEmpty()) {
            displayStatusAlert("Input Verification Error", "Please type the recorded diagnostic measurements inside the results field box.");
            return;
        }

        boolean success;

        // Trace state context logic: check if we are updating an existing entry or making a new one
        if (editingRecordContext != null) {
            // Context update state strategy path
            success = DatabaseManager.updateLabResultValue(
                    editingRecordContext.getPatientFayda(),
                    editingRecordContext.getTestName(),
                    resultText,
                    LocalDate.now().toString()
            );

            // Clear the tracked mutation pointer after completion
            this.editingRecordContext = null;
        } else {
            // Standard execution path fallback logic if logging entirely from scratch
            LabResult selectedOrder = labTable.getSelectionModel().getSelectedItem();
            if (selectedOrder == null) {
                displayStatusAlert("Selection Error", "Please select a specific patient row from the queue table before running a new commit entry.");
                return;
            }

            success = DatabaseManager.updateLabResultValue(
                    selectedOrder.getPatientFayda(),
                    selectedTest,
                    resultText,
                    LocalDate.now().toString()
            );
        }

        if (success) {
            refreshLabTable();
            resultInput.clear();
            testComboBox.setValue(null);
            displayStatusAlert("Entry Saved", "Analytical laboratory measurements updated on the system queue cleanly.");
        } else {
            displayStatusAlert("Database Mutation Fault", "Failed to preserve testing data fields inside the database layer.");
        }
    }

    @FXML
    private void handlePrint(ActionEvent event) {
        LabResult selectedOrder = labTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            displayStatusAlert("Export Blocked", "Please highlight a specific test order row from the table queue list to build an official PDF chart.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Certified Patient Diagnostics Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Charts (*.pdf)", "*.pdf"));
        fileChooser.setInitialFileName("Lab_Report_Fayda_" + selectedOrder.getPatientFayda() + ".pdf");

        File exportLocation = fileChooser.showSaveDialog(labTable.getScene().getWindow());

        if (exportLocation != null) {
            try {
                PdfWriter pdfWriter = new PdfWriter(exportLocation.getAbsolutePath());
                PdfDocument pdfDocument = new PdfDocument(pdfWriter);
                Document document = new Document(pdfDocument);

                // Build Structured iText Layout Definitions
                document.add(new Paragraph("OFFICIAL CLINIC DIAGNOSTIC LABORATORY CHARTS").setBold().setFontSize(16));
                document.add(new Paragraph("========================================================================="));
                document.add(new Paragraph("National Registration Key (Fayda): " + selectedOrder.getPatientFayda()));
                document.add(new Paragraph("Diagnostic Panel Type: " + selectedOrder.getTestName()));
                document.add(new Paragraph("Authorized Ordering Professional: " + selectedOrder.getOrderingDoctor()));
                document.add(new Paragraph("Report Compiling Date: " + LocalDate.now()));
                document.add(new Paragraph("\n"));

                // Configure a 3-column table matrix structure for grid formatting
                Table analyticalTable = new Table(3);
                analyticalTable.addCell("Diagnostic Test Profile");
                analyticalTable.addCell("Observed Result Metric");
                analyticalTable.addCell("Execution Date");

                analyticalTable.addCell(selectedOrder.getTestName());
                analyticalTable.addCell(selectedOrder.getResultValue());
                analyticalTable.addCell(selectedOrder.getTestDate());

                document.add(analyticalTable);
                document.close();

                displayStatusAlert("Export Complete", "Certified laboratory PDF document saved successfully!");
            } catch (Exception e) {
                System.err.println("Fatal iText engine mapping failure encountered during document print loop.");
                e.printStackTrace();
                displayStatusAlert("PDF Engine Fault", "The system encountered an error writing data cells down to the document framework.");
            }
        }
    }



    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            DatabaseManager.setCurrentSession("", "");
            MainController.selectedPatientSessionContext = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/choice-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Clinic Management System - Welcome Portal");

            // 1. Render the new layout to the screen first
            stage.show();

            // 2. Queue the maximization request to run right after the OS finishes drawing the window
            Platform.runLater(() -> {
                stage.setMaximized(false); // Reset the state toggle first to clear OS memory
                stage.setMaximized(true);  // Force a clean maximization pass
            });

        } catch (IOException e) {
            displayStatusAlert("Navigation Failure", "Could not route back to welcome choice layout.");
            e.printStackTrace();
        }
    }

    private void displayStatusAlert(String title, String bodyText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(bodyText);
        alert.showAndWait();
    }
}
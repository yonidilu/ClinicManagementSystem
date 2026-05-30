package com.clinic.model;

import javafx.beans.property.*;

public class LabResult {
    // These match the columns in your LabController
    private final StringProperty patientFayda;
    private final StringProperty testName;
    private final StringProperty resultValue;
    private final StringProperty testDate;
    private final StringProperty orderingDoctor;

    public LabResult(String patientFayda, String testName, String resultValue, String testDate, String orderingDoctor) {
        this.patientFayda = new SimpleStringProperty(patientFayda);
        this.testName = new SimpleStringProperty(testName);
        this.resultValue = new SimpleStringProperty(resultValue);
        this.testDate = new SimpleStringProperty(testDate);
        this.orderingDoctor = new SimpleStringProperty(orderingDoctor);
    }

    // These getters are what the TableView "PropertyValueFactory" looks for
    public String getPatientFayda() { return patientFayda.get(); }
    public String getTestName() { return testName.get(); }
    public String getResultValue() { return resultValue.get(); }
    public String getTestDate() { return testDate.get(); }
    public String getOrderingDoctor() { return orderingDoctor.get(); }

    // These property methods allow the table to refresh if data changes
    public StringProperty patientFaydaProperty() { return patientFayda; }
    public StringProperty testNameProperty() { return testName; }
    public StringProperty resultValueProperty() { return resultValue; }
    public StringProperty testDateProperty() { return testDate; }
    public StringProperty orderingDoctorProperty() { return orderingDoctor; }
}
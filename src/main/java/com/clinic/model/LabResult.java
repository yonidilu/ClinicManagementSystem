package com.clinic.model;

import javafx.beans.property.*;

public class LabResult {
    private final StringProperty testName;
    private final StringProperty resultValue;
    private final StringProperty testDate;

    public LabResult(String testName, String resultValue, String testDate) {
        this.testName = new SimpleStringProperty(testName);
        this.resultValue = new SimpleStringProperty(resultValue);
        this.testDate = new SimpleStringProperty(testDate);
    }

    // Standard Getters - THIS FIXES THE "CANNOT RESOLVE" ERROR
    public String getTestName() { return testName.get(); }
    public String getResultValue() { return resultValue.get(); }
    public String getTestDate() { return testDate.get(); }

    // Property Getters - For the TableView
    public StringProperty testNameProperty() { return testName; }
    public StringProperty resultValueProperty() { return resultValue; }
    public StringProperty testDateProperty() { return testDate; }
}
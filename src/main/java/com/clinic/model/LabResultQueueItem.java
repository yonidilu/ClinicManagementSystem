package com.clinic.model;

public class LabResultQueueItem {
    private final String fayda;
    private final String name;
    private final String testName;
    private final String result;
    private final String date;
    private final String doctor;

    public LabResultQueueItem(String fayda, String name, String testName, String result, String date, String doctor) {
        this.fayda = fayda;
        this.name = name;
        this.testName = testName;
        this.result = result;
        this.date = date;
        this.doctor = doctor;
    }

    public String getFayda() { return fayda; }
    public String getName() { return name; }
    public String getTestName() { return testName; }
    public String getResult() { return result; }
    public String getDate() { return date; }
    public String getDoctor() { return doctor; }
}
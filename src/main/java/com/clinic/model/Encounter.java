package com.clinic.model;

public class Encounter {
    private String date, diagnosis, treatment, prescription;

    public Encounter(String date, String diagnosis, String treatment, String prescription) {
        this.date = date;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.prescription = prescription;
    }

    // Getters are required for PropertyValueFactory to work
    public String getDate() { return date; }
    public String getDiagnosis() { return diagnosis; }
    public String getTreatment() { return treatment; }
    public String getPrescription() { return prescription; }
}
package com.clinic.model;

import javafx.beans.property.*;
import java.util.ArrayList;
import java.util.List;

public class Patient {
    // Core Registry Fields
    private final StringProperty name;
    private final StringProperty dob;
    private final StringProperty gender;
    private final StringProperty contact;
    private final StringProperty status;
    private final StringProperty fayda;
    private StringProperty paymentAmount = new SimpleStringProperty("");
    private StringProperty paymentStatus = new SimpleStringProperty("Pending");

    // Medical & Billing Fields
    private final StringProperty doctorName;
    private final StringProperty diagnosis;
    private final StringProperty treatment;
    private final StringProperty prescription;
    private final StringProperty appointmentDate;
    private final StringProperty paymentStructure;
    private final StringProperty registeredDate;

    // Lab Results List for EHR Summary
    private List<LabResult> labResults = new ArrayList<>();

    // 1. ADDED: Default Constructor (Fixes the "no arguments" error)
    public Patient() {
        this("", "", "", "", "", "", "", "", "", "", "", "", "");
    }

    // Existing Full Constructor
    public Patient(String name, String dob, String gender, String contact, String status, String fayda,
                   String doctorName, String diagnosis, String treatment, String prescription,
                   String apptDate, String payment, String regDate) {

        this.name = new SimpleStringProperty(name);
        this.dob = new SimpleStringProperty(dob);
        this.gender = new SimpleStringProperty(gender);
        this.contact = new SimpleStringProperty(contact);
        this.status = new SimpleStringProperty(status);
        this.fayda = new SimpleStringProperty(fayda);
        this.doctorName = new SimpleStringProperty(doctorName);
        this.diagnosis = new SimpleStringProperty(diagnosis);
        this.treatment = new SimpleStringProperty(treatment);
        this.prescription = new SimpleStringProperty(prescription);
        this.appointmentDate = new SimpleStringProperty(apptDate);
        this.paymentStructure = new SimpleStringProperty(payment);
        this.registeredDate = new SimpleStringProperty(regDate);
    }

    // --- Standard Getters ---
    public String getName() { return name.get(); }
    public String getDob() { return dob.get(); }
    public String getGender() { return gender.get(); }
    public String getContact() { return contact.get(); }
    public String getStatus() { return status.get(); }
    public String getFayda() { return fayda.get(); }
    public String getDoctorName() { return doctorName.get(); }
    public String getDiagnosis() { return diagnosis.get(); }
    public String getTreatment() { return treatment.get(); }
    public String getPrescription() { return prescription.get(); }
    public String getApptDate() { return appointmentDate.get(); }
    public String getPayment() { return paymentStructure.get(); }
    public String getRegDate() { return registeredDate.get(); }

    // --- 2. ADDED: Setters (Fixes "cannot find symbol method set...") ---
    public void setName(String value) { this.name.set(value); }
    public void setDob(String value) { this.dob.set(value); }
    public void setGender(String value) { this.gender.set(value); }
    public void setContact(String value) { this.contact.set(value); }
    public void setStatus(String value) { this.status.set(value); }
    public void setFayda(String value) { this.fayda.set(value); }
    public void setDoctorName(String value) { this.doctorName.set(value); }
    public void setDiagnosis(String value) { this.diagnosis.set(value); }
    public void setTreatment(String value) { this.treatment.set(value); }
    public void setPrescription(String value) { this.prescription.set(value); }
    public void setApptDate(String value) { this.appointmentDate.set(value); }
    public void setPayment(String value) { this.paymentStructure.set(value); }
    public void setRegDate(String value) { this.registeredDate.set(value); }

    // --- 3. ADDED: Lab Management ---
    public List<LabResult> getLabResults() { return labResults; }
    public void setLabResults(List<LabResult> labs) { this.labResults = labs; }

    // Property Getters for TableView
    public StringProperty nameProperty() { return name; }
    public StringProperty dobProperty() { return dob; }
    public StringProperty genderProperty() { return gender; }
    public StringProperty contactProperty() { return contact; }
    public StringProperty statusProperty() { return status; }
    public StringProperty faydaProperty() { return fayda; }
    public StringProperty paymentStatusProperty() {
        return paymentStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus.get();
    }

    public void setPaymentStatus(String status) {
        this.paymentStatus.set(status);
    }

    public StringProperty paymentAmountProperty() {
        return paymentAmount;
    }

    public String getPaymentAmount() {
        return paymentAmount.get();
    }

    public void setPaymentAmount(String amount) {
        this.paymentAmount.set(amount);
    }

}
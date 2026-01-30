package com.clinic.model;

import javafx.beans.property.*;
import java.util.ArrayList;
import java.util.List;

public class Patient {

    // 1. Core Registry & Medical Fields
    private final StringProperty name;
    private final StringProperty dob;
    private final StringProperty gender;
    private final StringProperty contact;
    private final StringProperty status;
    private final StringProperty fayda;
    private final StringProperty doctorName;
    private final StringProperty diagnosis;
    private final StringProperty treatment;
    private final StringProperty prescription;
    private final StringProperty appointmentDate;
    private final StringProperty paymentStructure;
    private final StringProperty registeredDate; // Fixed: Ensuring this is initialized

    // 2. Administrative & Extra Fields
    private final StringProperty assignedDoctor = new SimpleStringProperty("");
    private final StringProperty lastVisit = new SimpleStringProperty("");
    private final DoubleProperty balanceOwed = new SimpleDoubleProperty(0.0);
    private final StringProperty paymentAmount = new SimpleStringProperty("");
    private final StringProperty paymentStatus = new SimpleStringProperty("Pending");
    private int id;

    // 3. Lab Results
    private List<LabResult> labResults = new ArrayList<>();

    // 4. Constructors
    public Patient() {
        this("", "", "", "", "", "", "", "", "", "", "", "", "");
    }

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

    // --- PROPERTY METHODS (For TableView) ---
    public StringProperty nameProperty() { return name; }
    public StringProperty dobProperty() { return dob; }
    public StringProperty genderProperty() { return gender; }
    public StringProperty contactProperty() { return contact; }
    public StringProperty statusProperty() { return status; }
    public StringProperty faydaProperty() { return fayda; }
    public StringProperty assignedDoctorProperty() { return assignedDoctor; }
    public StringProperty lastVisitProperty() { return lastVisit; }
    public DoubleProperty balanceOwedProperty() { return balanceOwed; }
    public StringProperty paymentStatusProperty() { return paymentStatus; }
    public StringProperty registeredDateProperty() { return registeredDate; }

    // --- GETTERS & SETTERS (Cleaned and Fixed) ---
    public String getName() { return name.get(); }
    public void setName(String value) { this.name.set(value); }

    public String getDob() { return dob.get(); }
    public void setDob(String value) { this.dob.set(value); }

    public String getGender() { return gender.get(); }
    public void setGender(String value) { this.gender.set(value); }

    public String getContact() { return contact.get(); }
    public void setContact(String value) { this.contact.set(value); }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { this.status.set(value); }

    public String getFayda() { return fayda.get(); }
    public void setFayda(String value) { this.fayda.set(value); }

    public String getAssignedDoctor() { return assignedDoctor.get(); }
    public void setAssignedDoctor(String value) { this.assignedDoctor.set(value); }

    public String getLastVisit() { return lastVisit.get(); }
    public void setLastVisit(String value) { this.lastVisit.set(value); }

    // FIXED: Return type 'String' and naming synced
    public String getRegisteredDate() { return registeredDate.get(); }
    public void setRegisteredDate(String value) { this.registeredDate.set(value); }

    // FIXED: Synced with MainController's call
    public String getAppointmentDate() { return appointmentDate.get(); }
    public void setAppointmentDate(String value) { this.appointmentDate.set(value); }

    // FIXED: Standardized naming for payment amount
    public String getPaymentAmount() { return paymentAmount.get(); }
    public void setPaymentAmount(String value) { this.paymentAmount.set(value); }

    public double getBalanceOwed() { return balanceOwed.get(); }
    public void setBalanceOwed(double value) { this.balanceOwed.set(value); }

    public String getDiagnosis() { return diagnosis.get(); }
    public void setDiagnosis(String value) { this.diagnosis.set(value); }

    public String getTreatment() { return treatment.get(); }
    public void setTreatment(String value) { this.treatment.set(value); }

    public String getPrescription() { return prescription.get(); }
    public void setPrescription(String value) { this.prescription.set(value); }

    public String getPaymentStatus() { return paymentStatus.get(); }
    public void setPaymentStatus(String status) { this.paymentStatus.set(status); }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // Lab Management
    public List<LabResult> getLabResults() { return labResults; }
    public void setLabResults(List<LabResult> labs) { this.labResults = labs; }
}
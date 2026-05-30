package com.clinic.model;

import javafx.beans.property.*;
import java.util.ArrayList;
import java.util.List;

public class Patient {

    private int id; // The primary key from the database

    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty dob = new SimpleStringProperty("");
    private final StringProperty gender = new SimpleStringProperty("");
    private final StringProperty contact = new SimpleStringProperty("");
    private final StringProperty fayda = new SimpleStringProperty("");
    private final StringProperty assignedDoctor = new SimpleStringProperty("");
    private final StringProperty diagnosis = new SimpleStringProperty("");
    private final StringProperty treatment = new SimpleStringProperty("");
    private final StringProperty prescription = new SimpleStringProperty("");
    private final StringProperty appointmentDate = new SimpleStringProperty("");
    private final StringProperty paymentStatus = new SimpleStringProperty("Pending");
    private final StringProperty registeredDate = new SimpleStringProperty("");
    private final StringProperty lastVisit = new SimpleStringProperty("");
    private final DoubleProperty balanceOwed = new SimpleDoubleProperty(0.0);
    private final StringProperty paymentAmount = new SimpleStringProperty("");

    private List<LabResult> labResults = new ArrayList<>();

    public Patient() {}

    public Patient(String name, String dob, String gender, String contact, String fayda,
                   String doctor, String diagnosis, String treatment, String prescription,
                   String apptDate, String status, String regDate) {
        setName(name);
        setDob(dob);
        setGender(gender);
        setContact(contact);
        setFayda(fayda);
        setAssignedDoctor(doctor);
        setDiagnosis(diagnosis);
        setTreatment(treatment);
        setPrescription(prescription);
        setAppointmentDate(apptDate);
        setPaymentStatus(status);
        setRegisteredDate(regDate);
    }

    public Patient(String name, String dob, String gender, String contact, String status, String doctorName, String diagnosis, String treatment, String prescription, String apptDate, String fayda) {
    }

    // --- PROPERTY METHODS ---
    public StringProperty nameProperty() { return name; }
    public StringProperty dobProperty() { return dob; }
    public StringProperty genderProperty() { return gender; }
    public StringProperty contactProperty() { return contact; }
    public StringProperty faydaProperty() { return fayda; }
    public StringProperty assignedDoctorProperty() { return assignedDoctor; }
    public StringProperty diagnosisProperty() { return diagnosis; }
    public StringProperty treatmentProperty() { return treatment; }
    public StringProperty prescriptionProperty() { return prescription; }
    public StringProperty appointmentDateProperty() { return appointmentDate; }
    public StringProperty paymentStatusProperty() { return paymentStatus; }
    public StringProperty registeredDateProperty() { return registeredDate; }
    public StringProperty lastVisitProperty() { return lastVisit; }
    public DoubleProperty balanceOwedProperty() { return balanceOwed; }
    public StringProperty paymentAmountProperty() { return paymentAmount; } // ADDED THIS

    // --- GETTERS & SETTERS ---
    public String getName() { return name.get(); }
    public void setName(String value) { this.name.set(value); }

    public String getDob() { return dob.get(); }
    public void setDob(String value) { this.dob.set(value); }

    public String getGender() { return gender.get(); }
    public void setGender(String value) { this.gender.set(value); }

    public String getContact() { return contact.get(); }
    public void setContact(String value) { this.contact.set(value); }

    public String getFayda() { return fayda.get(); }
    public void setFayda(String value) { this.fayda.set(value); }

    public String getAssignedDoctor() { return assignedDoctor.get(); }
    public void setAssignedDoctor(String value) { this.assignedDoctor.set(value); }

    public String getDiagnosis() { return diagnosis.get(); }
    public void setDiagnosis(String value) { this.diagnosis.set(value); }

    public String getTreatment() { return treatment.get(); }
    public void setTreatment(String value) { this.treatment.set(value); }

    public String getPrescription() { return prescription.get(); }
    public void setPrescription(String value) { this.prescription.set(value); }

    public String getAppointmentDate() { return appointmentDate.get(); }
    public void setAppointmentDate(String value) { this.appointmentDate.set(value); }

    public String getPaymentStatus() { return paymentStatus.get(); }
    public void setPaymentStatus(String value) { this.paymentStatus.set(value); }

    public String getRegisteredDate() { return registeredDate.get(); }
    public void setRegisteredDate(String value) { this.registeredDate.set(value); }

    public String getLastVisit() { return lastVisit.get(); }
    public void setLastVisit(String value) { this.lastVisit.set(value); }

    public double getBalanceOwed() { return balanceOwed.get(); }
    public void setBalanceOwed(double value) { this.balanceOwed.set(value); }

    public String getPaymentAmount() { return paymentAmount.get(); }
    public void setPaymentAmount(String value) { this.paymentAmount.set(value); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public List<LabResult> getLabResults() { return labResults; }
    public void setLabResults(List<LabResult> labs) { this.labResults = labs; }


}
package com.clinic.model;

import javafx.beans.property.*;

public class Patient {
    // Core Registry Fields
    private final StringProperty name;
    private final StringProperty dob; // Replaces ailment
    private final StringProperty gender;
    private final StringProperty contact;
    private final StringProperty status;
    private final StringProperty fayda;

    // Medical & Billing Fields
    private final StringProperty doctorName;
    private final StringProperty diagnosis;
    private final StringProperty treatment;
    private final StringProperty prescription;
    private final StringProperty appointmentDate;
    private final StringProperty paymentStructure;
    private final StringProperty registeredDate;

    public Patient(String name, String dob, String gender, String contact, String status, String fayda,
                   String doctorName, String diagnosis, String treatment, String prescription,
                   String apptDate, String payment, String regDate) {

        this.name = new SimpleStringProperty(name);
        this.dob = new SimpleStringProperty(dob); // Replaces ailment
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

    // Standard Getters
    public String getName() { return name.get(); }
    public String getDob() { return dob.get(); } // Updated getter
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

    // Property Getters for TableView
    public StringProperty nameProperty() { return name; }
    public StringProperty dobProperty() { return dob; } // Updated property
    public StringProperty genderProperty() { return gender; }
    public StringProperty contactProperty() { return contact; }
    public StringProperty statusProperty() { return status; }
    public StringProperty faydaProperty() { return fayda; }

    // Setters for updating data
    public void setPayment(String payment) { this.paymentStructure.set(payment); }
    public void setStatus(String status) { this.status.set(status); }
}
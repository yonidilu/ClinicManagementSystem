package com.clinic.model; // This line MUST be here!

import javafx.beans.property.*;

public class Patient {
    // Standard String Getters (Fixes DatabaseManager errors)
    public String getName() { return name.get(); }
    public String getAilment() { return ailment.get(); }
    public String getGender() { return gender.get(); }
    public String getContact() { return contact.get(); }
    public String getStatus() { return status.get(); }
    public String getFayda() { return fayda.get(); }

    // JavaFX Property Getters (Fixes TableView red lines in MainController)
    public StringProperty nameProperty() { return name; }
    public StringProperty ailmentProperty() { return ailment; }
    public StringProperty genderProperty() { return gender; }
    public StringProperty contactProperty() { return contact; }
    public StringProperty statusProperty() { return status; }
    public StringProperty faydaProperty() { return fayda; }

    private final StringProperty name, ailment, gender, contact, status, fayda;
    // New clinical fields
    private final StringProperty doctorName, diagnosis, treatment, prescription,
            appointmentDate, paymentStructure, registeredDate;

    public Patient(String name, String ailment, String gender, String contact, String status, String fayda,
                   String doctorName, String diagnosis, String treatment, String prescription,
                   String apptDate, String payment, String regDate) {
        // Standard registration info
        this.name = new SimpleStringProperty(name);
        this.ailment = new SimpleStringProperty(ailment);
        this.gender = new SimpleStringProperty(gender);
        this.contact = new SimpleStringProperty(contact);
        this.status = new SimpleStringProperty(status);
        this.fayda = new SimpleStringProperty(fayda);

        // Medical Results - THIS FIXES image_7b481c.png
        this.doctorName = new SimpleStringProperty(doctorName);
        this.diagnosis = new SimpleStringProperty(diagnosis);
        this.treatment = new SimpleStringProperty(treatment);
        this.prescription = new SimpleStringProperty(prescription);
        this.appointmentDate = new SimpleStringProperty(apptDate);
        this.paymentStructure = new SimpleStringProperty(payment);
        this.registeredDate = new SimpleStringProperty(regDate);
    }
    public String getDoctorName() { return doctorName.get(); }
    public String getDiagnosis() { return diagnosis.get(); }
    public String getTreatment() { return treatment.get(); }
    public String getPrescription() { return prescription.get(); }
    public String getApptDate() { return appointmentDate.get(); }
    public String getPayment() { return paymentStructure.get(); }
    public String getRegDate() { return registeredDate.get(); }
}
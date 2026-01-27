package com.clinic.model;



public class Patient {
    private String name;
    private int age;
    private String ailment;
    private String faydaID;

    public Patient(String name, int age, String ailment, String fayda) {
        this.name = name;
        this.age = age;
        this.ailment = ailment;
        this.faydaID = fayda;
    }

    // Getters (Required for the Table to "see" the data)
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getAilment() { return ailment; }
    public String getFaydaID(){return faydaID; }
    private String assignedDoctor;
}

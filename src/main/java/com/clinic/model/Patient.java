package com.clinic.model;

public class Patient {
    private String name;
    private int age;
    private String ailment;

    public Patient(String name, int age, String ailment) {
        this.name = name;
        this.age = age;
        this.ailment = ailment;
    }

    // Getters (Required for the Table to "see" the data)
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getAilment() { return ailment; }
    private String assignedDoctor;
}

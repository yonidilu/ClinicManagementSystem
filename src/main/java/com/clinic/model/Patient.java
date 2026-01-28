package com.clinic.model;
import java.io.Serializable;

public class Patient implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String ailment;
    private String fayda;

    public Patient(String name, String ailment, String fayda) {
        this.name = name;
        this.ailment = ailment;
        this.fayda = fayda;
    }

    public String getName() { return name; }
    public String getAilment() { return ailment; }
    public String getFayda() { return fayda; }
}
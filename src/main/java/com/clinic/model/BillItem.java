package com.clinic.model;

import javafx.beans.property.*;

public class BillItem {
    private final StringProperty serviceName;
    private final DoubleProperty cost;

    public BillItem(String serviceName, double cost) {
        this.serviceName = new SimpleStringProperty(serviceName);
        this.cost = new SimpleDoubleProperty(cost);
    }

    // Standard Getters
    public String getServiceName() { return serviceName.get(); }
    public double getCost() { return cost.get(); }

    // JavaFX Property Getters (Required for TableView binding)
    public StringProperty serviceNameProperty() { return serviceName; }
    public DoubleProperty costProperty() { return cost; }

    public void setCost(double cost) { this.cost.set(cost); }
    private boolean discounted = false;

    public boolean isDiscounted() { return discounted; }
    public void setDiscounted(boolean discounted) { this.discounted = discounted; }

}
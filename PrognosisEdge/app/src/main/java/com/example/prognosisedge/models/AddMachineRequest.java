package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;

public class AddMachineRequest {
    @SerializedName("serial_number")
    private String serialNumber;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type;

    @SerializedName("location")
    private String location;

    @SerializedName("status")
    private String status;

    public AddMachineRequest(String serialNumber, String name, String type, String location, String status) {
        this.serialNumber = serialNumber;
        this.name = name;
        this.type = type;
        this.location = location;
        this.status = status;
    }

// Getters and setters
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

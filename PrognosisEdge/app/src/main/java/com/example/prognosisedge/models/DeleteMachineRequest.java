package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;

public class DeleteMachineRequest {
    @SerializedName("serial_number")
    private String serialNumber;

    public DeleteMachineRequest(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}

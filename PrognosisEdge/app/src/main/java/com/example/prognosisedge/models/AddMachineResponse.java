package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AddMachineResponse {
    private boolean success;
    private String message;

    @SerializedName("data")
    private List<Machine> machines; // Optional: only if the backend returns updated machine details

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Machine> getMachines() {
        return machines;
    }

    public void setMachines(List<Machine> machines) {
        this.machines = machines;
    }
}

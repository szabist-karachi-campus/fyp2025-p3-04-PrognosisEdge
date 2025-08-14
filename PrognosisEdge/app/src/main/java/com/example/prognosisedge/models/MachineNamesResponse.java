package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MachineNamesResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<String> machineNames;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getMachineNames() {
        return machineNames;
    }

    public void setMachineNames(List<String> machineNames) {
        this.machineNames = machineNames;
    }
}

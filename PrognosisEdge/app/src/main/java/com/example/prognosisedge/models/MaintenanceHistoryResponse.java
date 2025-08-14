package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MaintenanceHistoryResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<MaintenanceRecord> data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<MaintenanceRecord> getData() {
        return data;
    }
}

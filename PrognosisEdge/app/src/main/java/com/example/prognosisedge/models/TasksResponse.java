package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TasksResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("tasks")
    private List<WorkOrder> tasks;

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<WorkOrder> getTasks() {
        return tasks;
    }
}

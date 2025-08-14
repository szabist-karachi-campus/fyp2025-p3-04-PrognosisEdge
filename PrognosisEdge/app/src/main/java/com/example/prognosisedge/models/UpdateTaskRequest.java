package com.example.prognosisedge.models;

public class UpdateTaskRequest {
    private String status;
    private String updatedDate; // ISO date format
    private String notes;

    public UpdateTaskRequest(String status, String updatedDate, String notes) {
        this.status = status;
        this.updatedDate = updatedDate;
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public String getNotes() {
        return notes;
    }
}
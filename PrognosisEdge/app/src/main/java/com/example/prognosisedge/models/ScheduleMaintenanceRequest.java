package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;

public class ScheduleMaintenanceRequest {
    @SerializedName("title")
    private String title;

    @SerializedName("machine_name")
    private String machineName;

    @SerializedName("assigned_engineer")
    private String assignedEngineer;

    @SerializedName("scheduled_at")
    private String scheduledAt;

    @SerializedName("notes")
    private String notes;

    public ScheduleMaintenanceRequest(String title, String machineName, String assignedEngineer, String scheduledAt, String notes) {
        this.title = title;
        this.machineName = machineName;
        this.assignedEngineer = assignedEngineer;
        this.scheduledAt = scheduledAt;
        this.notes = notes;
    }

    // Getters and setters if required
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getAssignedEngineer() {
        return assignedEngineer;
    }

    public void setAssignedEngineer(String assignedEngineer) {
        this.assignedEngineer = assignedEngineer;
    }

    public String getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(String scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

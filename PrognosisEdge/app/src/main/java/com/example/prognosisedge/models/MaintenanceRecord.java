package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;

public class MaintenanceRecord {
    @SerializedName("machine_name")
    private String machineName;

    @SerializedName("task_id")
    private int taskId;

    @SerializedName("title")
    private String title;

    @SerializedName("status")
    private String status;

    @SerializedName("assigned_engineer")
    private String assignedEngineer;

    @SerializedName("scheduled_at")
    private String scheduledAt;

    @SerializedName("started_at")
    private String startedAt;

    @SerializedName("ended_at")
    private String endedAt;

    @SerializedName("notes")
    private String notes;

    @SerializedName("comments")
    private String comments;

    public String getMachineName() {
        return machineName != null ? machineName : "Unknown";
    }

    public int getTaskId() {
        return taskId;
    }

    public String getTitle() {
        return title != null ? title : "No title";
    }

    public String getStatus() {
        return status != null ? status : "Unknown";
    }

    public String getAssignedEngineer() {
        return assignedEngineer != null ? assignedEngineer : "Unassigned";
    }

    public String getScheduledAt() {
        return scheduledAt != null ? scheduledAt : "N/A";
    }

    public String getStartedAt() {
        return startedAt != null ? startedAt : "Not started";
    }

    public String getEndedAt() {
        return endedAt != null ? endedAt : "Not ended";
    }

    public String getNotes() {
        return notes != null ? notes : "No notes provided";
    }

    public String getComments() {
        return comments != null ? comments : "No comments available";
    }
}

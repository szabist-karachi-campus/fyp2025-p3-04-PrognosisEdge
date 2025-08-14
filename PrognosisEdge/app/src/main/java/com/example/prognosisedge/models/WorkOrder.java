package com.example.prognosisedge.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class WorkOrder implements Parcelable {
    @SerializedName("task_id")
    private int taskId;

    @SerializedName("title")
    private String title;

    @SerializedName("status")
    private String status;

    @SerializedName("assigned_engineer")
    private String assignedEngineer;

    @SerializedName("scheduled_date") // Separate date field
    private String scheduledDate;

    @SerializedName("scheduled_time") // Separate time field
    private String scheduledTime;

    @SerializedName("started_at")
    private String startedAt;

    @SerializedName("ended_at")
    private String endedAt;

    @SerializedName("notes")
    private String notes;

    @SerializedName("comments")
    private String comments;

    // Default constructor
    public WorkOrder() {
    }

    // Parameterized constructor
    public WorkOrder(int taskId, String title, String status, String assignedEngineer,
                     String scheduledDate, String scheduledTime, String startedAt,
                     String endedAt, String notes, String comments) {
        this.taskId = taskId;
        this.title = title != null ? title : "N/A";
        this.status = status != null ? status : "N/A";
        this.assignedEngineer = assignedEngineer != null ? assignedEngineer : "Unassigned";
        this.scheduledDate = scheduledDate;
        this.scheduledTime = scheduledTime;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.notes = notes != null ? notes : "No notes provided";
        this.comments = comments != null ? comments : "No comments available";
    }

    // Parcelable implementation
    protected WorkOrder(Parcel in) {
        taskId = in.readInt();
        title = in.readString();
        status = in.readString();
        assignedEngineer = in.readString();
        scheduledDate = in.readString();
        scheduledTime = in.readString();
        startedAt = in.readString();
        endedAt = in.readString();
        notes = in.readString();
        comments = in.readString();
    }

    public static final Creator<WorkOrder> CREATOR = new Creator<WorkOrder>() {
        @Override
        public WorkOrder createFromParcel(Parcel in) {
            return new WorkOrder(in);
        }

        @Override
        public WorkOrder[] newArray(int size) {
            return new WorkOrder[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(taskId);
        dest.writeString(title);
        dest.writeString(status);
        dest.writeString(assignedEngineer);
        dest.writeString(scheduledDate);
        dest.writeString(scheduledTime);
        dest.writeString(startedAt);
        dest.writeString(endedAt);
        dest.writeString(notes);
        dest.writeString(comments);
    }

    // Getters and setters
    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title != null ? title : "N/A";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status != null ? status : "N/A";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedEngineer() {
        return assignedEngineer != null ? assignedEngineer : "Unassigned";
    }

    public void setAssignedEngineer(String assignedEngineer) {
        this.assignedEngineer = assignedEngineer;
    }

    public String getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(String scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getStartedAt() {
        return startedAt != null ? startedAt : "Not started";
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public String getEndedAt() {
        return endedAt != null ? endedAt : "Not ended";
    }

    public void setEndedAt(String endedAt) {
        this.endedAt = endedAt;
    }

    public String getNotes() {
        return notes != null ? notes : "No notes provided";
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getComments() {
        return comments != null ? comments : "No comments available";
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    // Override toString() for easier debugging/logging
    @Override
    public String toString() {
        return "WorkOrder{" +
                "taskId=" + taskId +
                ", title='" + getTitle() + '\'' +
                ", status='" + getStatus() + '\'' +
                ", assignedEngineer='" + getAssignedEngineer() + '\'' +
                ", scheduledDate='" + getScheduledDate() + '\'' +
                ", scheduledTime='" + getScheduledTime() + '\'' +
                ", startedAt='" + getStartedAt() + '\'' +
                ", endedAt='" + getEndedAt() + '\'' +
                ", notes='" + getNotes() + '\'' +
                ", comments='" + getComments() + '\'' +
                '}';
    }
}

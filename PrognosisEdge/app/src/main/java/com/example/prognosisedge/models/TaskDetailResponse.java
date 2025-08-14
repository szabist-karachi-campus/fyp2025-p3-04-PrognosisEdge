package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;

public class TaskDetailResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private TaskDetail data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public TaskDetail getData() {
        return data;
    }

    public static class TaskDetail {
        @SerializedName("task_id")
        private int taskId;

        @SerializedName("title")
        private String title;

        @SerializedName("status")
        private String status;

        @SerializedName("assigned_engineer")
        private String assignedEngineer;

        @SerializedName("scheduled_date")
        private String scheduledDate;

        @SerializedName("scheduled_time")
        private String scheduledTime;

        @SerializedName("started_at")
        private String startedAt;

        @SerializedName("ended_at")
        private String endedAt;

        @SerializedName("notes")
        private String notes;

        @SerializedName("comments")
        private String comments;

        public int getTaskId() {
            return taskId;
        }

        public String getTitle() {
            return title != null ? title : "N/A";
        }

        public String getStatus() {
            return status != null ? status : "N/A";
        }

        public String getAssignedEngineer() {
            return assignedEngineer != null ? assignedEngineer : "Unassigned";
        }

        public String getScheduledDate() {
            return scheduledDate != null ? scheduledDate : "N/A";
        }

        public String getScheduledTime() {
            return scheduledTime != null ? scheduledTime : "N/A";
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
}

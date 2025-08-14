package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;

public class TaskCountsResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("counts")
    private Map<String, Integer> counts;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Integer> getCounts() {
        return counts;
    }

    public int getTotalCount() {
        return counts != null ? counts.values().stream().mapToInt(Integer::intValue).sum() : 0;
    }

    public int getUpcomingCount() {
        return counts != null ? counts.getOrDefault("upcoming", 0) : 0;
    }

    public int getInProgressCount() {
        return counts != null ? counts.getOrDefault("inProgress", 0) : 0;
    }

    public int getCompletedCount() {
        return counts != null ? counts.getOrDefault("completed", 0) : 0;
    }

    public int getOverdueCount() {
        return counts != null ? counts.getOrDefault("overdue", 0) : 0;
    }

    public int getCancelledCount() {
        return counts != null ? counts.getOrDefault("cancelled", 0) : 0;
    }
}
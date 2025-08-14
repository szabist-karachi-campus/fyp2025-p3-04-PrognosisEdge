package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class WorkOrderReport implements Serializable {
    @SerializedName("report_id")
    private int reportId;

    @SerializedName("title")
    private String title;

    @SerializedName("created_by")
    private String createdBy;

    @SerializedName("date_range_start")
    private String dateRangeStart;

    @SerializedName("date_range_end")
    private String dateRangeEnd;

    @SerializedName("total_work_orders")
    private int totalWorkOrders;

    @SerializedName("completed_work_orders")
    private int completedWorkOrders;

    @SerializedName("in_progress_work_orders")
    private int inProgressWorkOrders;

    @SerializedName("cancelled_work_orders")
    private int cancelledWorkOrders;

    @SerializedName("overdue_work_orders")
    private int overdueWorkOrders;

    @SerializedName("average_completion_time")
    private double averageCompletionTime;

    @SerializedName("created_at")
    private String createdAt;

    // Getters and Setters
    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getDateRangeStart() {
        return dateRangeStart;
    }

    public void setDateRangeStart(String dateRangeStart) {
        this.dateRangeStart = dateRangeStart;
    }

    public String getDateRangeEnd() {
        return dateRangeEnd;
    }

    public void setDateRangeEnd(String dateRangeEnd) {
        this.dateRangeEnd = dateRangeEnd;
    }

    public int getTotalWorkOrders() {
        return totalWorkOrders;
    }

    public void setTotalWorkOrders(int totalWorkOrders) {
        this.totalWorkOrders = totalWorkOrders;
    }

    public int getCompletedWorkOrders() {
        return completedWorkOrders;
    }

    public void setCompletedWorkOrders(int completedWorkOrders) {
        this.completedWorkOrders = completedWorkOrders;
    }

    public int getInProgressWorkOrders() {
        return inProgressWorkOrders;
    }

    public void setInProgressWorkOrders(int inProgressWorkOrders) {
        this.inProgressWorkOrders = inProgressWorkOrders;
    }

    public int getCancelledWorkOrders() {
        return cancelledWorkOrders;
    }

    public void setCancelledWorkOrders(int cancelledWorkOrders) {
        this.cancelledWorkOrders = cancelledWorkOrders;
    }

    public int getOverdueWorkOrders() {
        return overdueWorkOrders;
    }

    public void setOverdueWorkOrders(int overdueWorkOrders) {
        this.overdueWorkOrders = overdueWorkOrders;
    }

    public double getAverageCompletionTime() {
        return averageCompletionTime;
    }

    public void setAverageCompletionTime(double averageCompletionTime) {
        this.averageCompletionTime = averageCompletionTime;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
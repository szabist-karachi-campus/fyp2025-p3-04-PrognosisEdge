package com.example.prognosisedge.models;

public class WorkOrderReportRequest {
    private String start_date;
    private String end_date;
    private String username;

    public WorkOrderReportRequest(String start_date, String end_date, String username) {
        this.start_date = start_date;
        this.end_date = end_date;
        this.username = username;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
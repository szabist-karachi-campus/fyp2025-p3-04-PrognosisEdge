package com.example.prognosisedge.models;

import java.util.List;

public class MachineReportResponse {
    private boolean success;
    private boolean report_stored;
    private String message;
    private List<MachineReport> reports;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public boolean isReport_stored() { return report_stored; }
    public void setReport_stored(boolean report_stored) { this.report_stored = report_stored; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<MachineReport> getReports() { return reports; }
    public void setReports(List<MachineReport> reports) { this.reports = reports; }
}

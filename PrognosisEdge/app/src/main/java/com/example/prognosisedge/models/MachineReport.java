package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MachineReport implements Serializable {
    @SerializedName("report_id")
    private int reportId;

    @SerializedName("serial_number")
    private String serialNumber;

    @SerializedName("date_range_start")
    private String dateRangeStart;

    @SerializedName("date_range_end")
    private String dateRangeEnd;

    @SerializedName("total_readings")
    private int totalReadings;

    @SerializedName("failure_count")
    private int failureCount;

    @SerializedName("no_failure_count")
    private int noFailureCount;

    @SerializedName("detergent_level_low_count")
    private int detergentLow;

    @SerializedName("pressure_drop_count")
    private int pressureDrop;

    @SerializedName("temperature_anomaly_count")
    private int temperatureAnomaly;

    @SerializedName("water_flow_issue_count")
    private int waterFlowIssue;

    @SerializedName("avg_water_flow_rate")
    private double avgFlow;

    @SerializedName("avg_pressure_stability_index")
    private double avgPressureStability;

    @SerializedName("avg_detergent_level")
    private double avgDetergent;

    @SerializedName("avg_hydraulic_pressure")
    private double avgHydraulic;

    @SerializedName("avg_temperature_fluctuation_index")
    private double avgTempFluctuation;

    @SerializedName("avg_hydraulic_oil_temperature")
    private double avgOilTemp;

    @SerializedName("avg_coolant_temperature")
    private double avgCoolant;

    @SerializedName("failure_prediction_rate")
    private double failurePrediction;

    // Getters and Setters
    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
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

    public int getTotalReadings() {
        return totalReadings;
    }

    public void setTotalReadings(int totalReadings) {
        this.totalReadings = totalReadings;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public int getNoFailureCount() {
        return noFailureCount;
    }

    public void setNoFailureCount(int noFailureCount) {
        this.noFailureCount = noFailureCount;
    }

    public int getDetergentLow() {
        return detergentLow;
    }

    public void setDetergentLow(int detergentLow) {
        this.detergentLow = detergentLow;
    }

    public int getPressureDrop() {
        return pressureDrop;
    }

    public void setPressureDrop(int pressureDrop) {
        this.pressureDrop = pressureDrop;
    }

    public int getTemperatureAnomaly() {
        return temperatureAnomaly;
    }

    public void setTemperatureAnomaly(int temperatureAnomaly) {
        this.temperatureAnomaly = temperatureAnomaly;
    }

    public int getWaterFlowIssue() {
        return waterFlowIssue;
    }

    public void setWaterFlowIssue(int waterFlowIssue) {
        this.waterFlowIssue = waterFlowIssue;
    }

    public double getAvgFlow() {
        return avgFlow;
    }

    public void setAvgFlow(double avgFlow) {
        this.avgFlow = avgFlow;
    }

    public double getAvgPressureStability() {
        return avgPressureStability;
    }

    public void setAvgPressureStability(double avgPressureStability) {
        this.avgPressureStability = avgPressureStability;
    }

    public double getAvgDetergent() {
        return avgDetergent;
    }

    public void setAvgDetergent(double avgDetergent) {
        this.avgDetergent = avgDetergent;
    }

    public double getAvgHydraulic() {
        return avgHydraulic;
    }

    public void setAvgHydraulic(double avgHydraulic) {
        this.avgHydraulic = avgHydraulic;
    }

    public double getAvgTempFluctuation() {
        return avgTempFluctuation;
    }

    public void setAvgTempFluctuation(double avgTempFluctuation) {
        this.avgTempFluctuation = avgTempFluctuation;
    }

    public double getAvgOilTemp() {
        return avgOilTemp;
    }

    public void setAvgOilTemp(double avgOilTemp) {
        this.avgOilTemp = avgOilTemp;
    }

    public double getAvgCoolant() {
        return avgCoolant;
    }

    public void setAvgCoolant(double avgCoolant) {
        this.avgCoolant = avgCoolant;
    }

    public double getFailurePrediction() {
        return failurePrediction;
    }

    public void setFailurePrediction(double failurePrediction) {
        this.failurePrediction = failurePrediction;
    }
}

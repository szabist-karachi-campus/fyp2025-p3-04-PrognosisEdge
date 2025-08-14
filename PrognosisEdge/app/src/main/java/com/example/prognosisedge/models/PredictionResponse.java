package com.example.prognosisedge.models;

public class PredictionResponse {
    private String machineId;
    private String machineName;
    private boolean machineFailure;
    private String failureType;
    private double waterFlowRate;
    private double pressureStabilityIndex;
    private double detergentLevel;
    private double hydraulicPressure;
    private double temperatureFluctuationIndex;
    private double hydraulicOilTemperature;
    private double coolantTemperature;

    // Getters
    public String getMachineId() { return machineId; }
    public String getMachineName() { return machineName; }
    public boolean isMachineFailure() { return machineFailure; }
    public String getFailureType() {
        if (failureType == null || failureType.equalsIgnoreCase("null")) {
            return "No Failure";
        }
        return failureType;
    }
    public double getWaterFlowRate() { return waterFlowRate; }
    public double getPressureStabilityIndex() { return pressureStabilityIndex; }
    public double getDetergentLevel() { return detergentLevel; }
    public double getHydraulicPressure() { return hydraulicPressure; }
    public double getTemperatureFluctuationIndex() { return temperatureFluctuationIndex; }
    public double getHydraulicOilTemperature() { return hydraulicOilTemperature; }
    public double getCoolantTemperature() { return coolantTemperature; }

    // Setters
    public void setMachineId(String machineId) { this.machineId = machineId; }
    public void setMachineName(String machineName) { this.machineName = machineName; }
    public void setMachineFailure(boolean machineFailure) { this.machineFailure = machineFailure; }
    public void setFailureType(String failureType) { this.failureType = failureType; }
    public void setWaterFlowRate(double waterFlowRate) { this.waterFlowRate = waterFlowRate; }
    public void setPressureStabilityIndex(double pressureStabilityIndex) { this.pressureStabilityIndex = pressureStabilityIndex; }
    public void setDetergentLevel(double detergentLevel) { this.detergentLevel = detergentLevel; }
    public void setHydraulicPressure(double hydraulicPressure) { this.hydraulicPressure = hydraulicPressure; }
    public void setTemperatureFluctuationIndex(double temperatureFluctuationIndex) { this.temperatureFluctuationIndex = temperatureFluctuationIndex; }
    public void setHydraulicOilTemperature(double hydraulicOilTemperature) { this.hydraulicOilTemperature = hydraulicOilTemperature; }
    public void setCoolantTemperature(double coolantTemperature) { this.coolantTemperature = coolantTemperature; }
}

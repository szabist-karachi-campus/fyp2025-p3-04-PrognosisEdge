package com.example.prognosisedge.models;

public class MachinePredictions {
    private String name;
    private String lastData;
    private String prediction;

    // Constructor
    public MachinePredictions(String name, String lastData, String prediction) {
        this.name = name;
        this.lastData = lastData;
        this.prediction = prediction;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getLastData() {
        return lastData;
    }

    public String getPrediction() {
        return prediction;
    }
}

package com.example.prognosisedge;

import java.io.Serializable;

public class Report implements Serializable {
    private String title;
    private String description;

    public Report(String title, String description) {
        this.title = title;
        this.description = description;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

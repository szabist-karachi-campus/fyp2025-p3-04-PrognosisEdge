// AppNotification.java
package com.example.prognosisedge;

public class AppNotification {
    private String category;
    private String title;
    private String description;

    public AppNotification(String category, String title, String description) {
        this.category = category;
        this.title = title;
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}

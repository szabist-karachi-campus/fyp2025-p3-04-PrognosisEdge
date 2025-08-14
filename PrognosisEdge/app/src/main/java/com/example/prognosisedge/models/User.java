package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("user_id")
    private int userId;  // New field

    private String name;
    private String email;
    private String password;
    private String role;

    @SerializedName("is_active")
    private boolean isActive;

    public User(int userId, String name, String email, String password, String role, boolean isActive) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isActive = isActive;
    }

    public User(String name, String email, String password, String role, boolean isActive) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isActive = isActive;
    }

    // Getters and setters
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        isActive = active;
    }
}

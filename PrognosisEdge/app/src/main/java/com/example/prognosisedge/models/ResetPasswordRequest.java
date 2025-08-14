package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {
    private String username;

    @SerializedName("new_password")  // This fixes the key mismatch
    private String newPassword;

    public ResetPasswordRequest(String username, String newPassword) {
        this.username = username;
        this.newPassword = newPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

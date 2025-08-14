package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;

public class CreateUserResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("user_id")
    private Integer userId;

    public CreateUserResponse() {
    }

    public CreateUserResponse(boolean success, String message, Integer userId) {
        this.success = success;
        this.message = message;
        this.userId = userId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}

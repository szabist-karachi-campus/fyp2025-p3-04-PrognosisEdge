package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GetAllUsersResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<User> data;

    public GetAllUsersResponse() {
    }

    public GetAllUsersResponse(boolean success, String message, List<User> data) {
        this.success = success;
        this.message = message;
        this.data = data;
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

    public List<User> getData() {
        return data;
    }

    public void setData(List<User> data) {
        this.data = data;
    }
}

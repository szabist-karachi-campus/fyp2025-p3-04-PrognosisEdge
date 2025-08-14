package com.example.prognosisedge.models;

import com.google.gson.annotations.SerializedName;

public class GetUserRequest {

    @SerializedName("user_id")
    private int userId;

    public GetUserRequest() {
    }

    public GetUserRequest(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}

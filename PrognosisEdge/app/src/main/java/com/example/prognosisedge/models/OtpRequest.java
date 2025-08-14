package com.example.prognosisedge.models;

public class OtpRequest {
    private String username;
    private String otp;

    // Constructor for sending OTP
    public OtpRequest(String username) {
        this.username = username;
    }

    // Constructor for verifying OTP
    public OtpRequest(String username, String otp) {
        this.username = username;
        this.otp = otp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}

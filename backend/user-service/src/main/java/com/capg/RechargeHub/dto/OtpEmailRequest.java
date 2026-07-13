package com.capg.RechargeHub.dto;

public class OtpEmailRequest {
    private String email;
    private String otp;

    public OtpEmailRequest() {}

    public OtpEmailRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}

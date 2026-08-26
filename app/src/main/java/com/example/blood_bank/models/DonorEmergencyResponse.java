package com.example.blood_bank.models;

public class DonorEmergencyResponse {
    private String responseId;
    private String requestId;
    private String hospitalId;
    private String hospitalName;
    private String donorId;
    private String donorAuthUid;
    private String donorName;
    private String donorPhone;
    private String donorBloodGroup;
    private double donorDistanceKm;
    private String responseStatus; // "NOT_RESPONDED", "AVAILABLE", "NOT_AVAILABLE", "SELECTED", "COMPLETED"
    private String createdAt;
    private long timestamp;

    public DonorEmergencyResponse() {
        this.responseStatus = "NOT_RESPONDED";
        this.timestamp = System.currentTimeMillis();
    }

    public DonorEmergencyResponse(String responseId, String requestId, String hospitalId, String hospitalName, String donorId, String donorAuthUid, String donorName, String donorPhone, String donorBloodGroup, double donorDistanceKm, String responseStatus, String createdAt) {
        this.responseId = responseId;
        this.requestId = requestId;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.donorId = donorId;
        this.donorAuthUid = donorAuthUid;
        this.donorName = donorName;
        this.donorPhone = donorPhone;
        this.donorBloodGroup = donorBloodGroup;
        this.donorDistanceKm = donorDistanceKm;
        this.responseStatus = responseStatus != null ? responseStatus : "AVAILABLE";
        this.createdAt = createdAt;
        this.timestamp = System.currentTimeMillis();
    }

    public String getResponseId() { return responseId; }
    public void setResponseId(String responseId) { this.responseId = responseId; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getDonorAuthUid() { return donorAuthUid; }
    public void setDonorAuthUid(String donorAuthUid) { this.donorAuthUid = donorAuthUid; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getDonorPhone() { return donorPhone; }
    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }

    public String getDonorBloodGroup() { return donorBloodGroup; }
    public void setDonorBloodGroup(String donorBloodGroup) { this.donorBloodGroup = donorBloodGroup; }

    public double getDonorDistanceKm() { return donorDistanceKm; }
    public void setDonorDistanceKm(double donorDistanceKm) { this.donorDistanceKm = donorDistanceKm; }

    public String getResponseStatus() { return responseStatus; }
    public void setResponseStatus(String responseStatus) { this.responseStatus = responseStatus; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

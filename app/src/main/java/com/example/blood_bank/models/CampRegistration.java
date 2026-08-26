package com.example.blood_bank.models;

public class CampRegistration {
    private String registrationId;
    private String campId;
    private String campName;
    private String bloodBankId;
    private String bloodBankName;
    private String donorId;
    private String donorAuthUid;
    private String donorName;
    private String donorPhone;
    private String bloodGroup;
    private String campDate;
    private String status; // "PENDING", "CONFIRMED", "CANCELLED", "COMPLETED"
    private String createdAt;
    private long createdAtTimestamp;

    public CampRegistration() {
        this.status = "PENDING";
        this.createdAtTimestamp = System.currentTimeMillis();
    }

    public CampRegistration(String registrationId, String campId, String campName, String bloodBankId, String bloodBankName, String donorId, String donorAuthUid, String donorName, String donorPhone, String bloodGroup, String campDate, String status, String createdAt) {
        this.registrationId = registrationId;
        this.campId = campId;
        this.campName = campName;
        this.bloodBankId = bloodBankId;
        this.bloodBankName = bloodBankName;
        this.donorId = donorId;
        this.donorAuthUid = donorAuthUid;
        this.donorName = donorName;
        this.donorPhone = donorPhone;
        this.bloodGroup = bloodGroup;
        this.campDate = campDate;
        this.status = status != null ? status : "PENDING";
        this.createdAt = createdAt;
        this.createdAtTimestamp = System.currentTimeMillis();
    }

    public String getRegistrationId() { return registrationId; }
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }

    public String getCampId() { return campId; }
    public void setCampId(String campId) { this.campId = campId; }

    public String getCampName() { return campName; }
    public void setCampName(String campName) { this.campName = campName; }

    public String getBloodBankId() { return bloodBankId; }
    public void setBloodBankId(String bloodBankId) { this.bloodBankId = bloodBankId; }

    public String getBloodBankName() { return bloodBankName; }
    public void setBloodBankName(String bloodBankName) { this.bloodBankName = bloodBankName; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getDonorAuthUid() { return donorAuthUid; }
    public void setDonorAuthUid(String donorAuthUid) { this.donorAuthUid = donorAuthUid; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getDonorPhone() { return donorPhone; }
    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getCampDate() { return campDate; }
    public void setCampDate(String campDate) { this.campDate = campDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public long getCreatedAtTimestamp() { return createdAtTimestamp; }
    public void setCreatedAtTimestamp(long createdAtTimestamp) { this.createdAtTimestamp = createdAtTimestamp; }
}

package com.example.blood_bank.models;

public class DonorAppointment {
    private String appointmentId;
    private String donorId;
    private String donorAuthUid;
    private String donorName;
    private String donorEmail;
    private String donorPhone;
    private String bloodBankId;
    private String bloodBankName;
    private String bloodGroup;
    private String date;
    private String time;
    private String message;
    private String status; // PENDING, CONFIRMED, REJECTED, COMPLETED, CANCELLED
    private String createdAt;
    private long createdAtTimestamp;
    private long updatedAt;

    public DonorAppointment() {
        this.status = "PENDING";
        this.createdAtTimestamp = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public DonorAppointment(String appointmentId, String donorId, String donorAuthUid, String donorName, String donorEmail, String donorPhone, String bloodBankId, String bloodBankName, String bloodGroup, String date, String time, String message, String status, String createdAt) {
        this.appointmentId = appointmentId;
        this.donorId = donorId;
        this.donorAuthUid = donorAuthUid;
        this.donorName = donorName;
        this.donorEmail = donorEmail;
        this.donorPhone = donorPhone;
        this.bloodBankId = bloodBankId;
        this.bloodBankName = bloodBankName;
        this.bloodGroup = bloodGroup;
        this.date = date;
        this.time = time;
        this.message = message;
        this.status = status != null ? status : "PENDING";
        this.createdAt = createdAt;
        this.createdAtTimestamp = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getDonorAuthUid() { return donorAuthUid != null ? donorAuthUid : donorId; }
    public void setDonorAuthUid(String donorAuthUid) { this.donorAuthUid = donorAuthUid; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getDonorEmail() { return donorEmail; }
    public void setDonorEmail(String donorEmail) { this.donorEmail = donorEmail; }

    public String getDonorPhone() { return donorPhone; }
    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }

    public String getBloodBankId() { return bloodBankId; }
    public void setBloodBankId(String bloodBankId) { this.bloodBankId = bloodBankId; }

    public String getBloodBankName() { return bloodBankName; }
    public void setBloodBankName(String bloodBankName) { this.bloodBankName = bloodBankName; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getMessage() { return message != null ? message : ""; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status != null ? status : "PENDING"; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public long getCreatedAtTimestamp() { return createdAtTimestamp; }
    public void setCreatedAtTimestamp(long createdAtTimestamp) { this.createdAtTimestamp = createdAtTimestamp; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}

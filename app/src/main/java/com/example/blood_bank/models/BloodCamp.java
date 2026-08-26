package com.example.blood_bank.models;

public class BloodCamp {
    private String campId;
    private String campName;
    private String organizer;
    private String bloodBankId;
    private String bloodBankName;
    private String location;
    private String address;
    private double latitude;
    private double longitude;
    private String date;
    private String startTime;
    private String endTime;
    private String contact;
    private String description;
    private int availableSlots;
    private int totalSlots;
    private String status; // "ACTIVE", "FULL", "COMPLETED", "CANCELLED"
    private String createdAt;
    private long createdAtTimestamp;
    public double distanceKm = 0.0;

    public BloodCamp() {
        this.status = "ACTIVE";
        this.createdAtTimestamp = System.currentTimeMillis();
    }

    public BloodCamp(String campId, String campName, String organizer, String bloodBankId, String bloodBankName, String location, String address, double latitude, double longitude, String date, String startTime, String endTime, String contact, String description, int availableSlots, int totalSlots, String status, String createdAt) {
        this.campId = campId;
        this.campName = campName;
        this.organizer = organizer;
        this.bloodBankId = bloodBankId;
        this.bloodBankName = bloodBankName;
        this.location = location;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.contact = contact;
        this.description = description;
        this.availableSlots = availableSlots;
        this.totalSlots = totalSlots;
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt;
        this.createdAtTimestamp = System.currentTimeMillis();
    }

    public String getCampId() { return campId; }
    public void setCampId(String campId) { this.campId = campId; }

    public String getCampName() { return campName; }
    public void setCampName(String campName) { this.campName = campName; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public String getBloodBankId() { return bloodBankId; }
    public void setBloodBankId(String bloodBankId) { this.bloodBankId = bloodBankId; }

    public String getBloodBankName() { return bloodBankName; }
    public void setBloodBankName(String bloodBankName) { this.bloodBankName = bloodBankName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(int availableSlots) { this.availableSlots = availableSlots; }

    public int getTotalSlots() { return totalSlots; }
    public void setTotalSlots(int totalSlots) { this.totalSlots = totalSlots; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public long getCreatedAtTimestamp() { return createdAtTimestamp; }
    public void setCreatedAtTimestamp(long createdAtTimestamp) { this.createdAtTimestamp = createdAtTimestamp; }
}

package com.example.blood_bank.models;

public class BloodRequest {
    private String requestId;
    private String hospitalId;
    private String hospitalName;
    private String bloodGroup;
    private String component;
    private int requiredUnits;
    private int quantity;
    private String urgency;
    private String status; // Pending, Accepted, Rejected, Partially Available, Fulfilled, Cancelled, SEARCHING, RESERVED, DISPATCHED
    private int escalationStage; // 1: Blood Bank, 2: Donors, 3: Expanded Network, 4: Admin
    private String qrToken;
    private String otpCode;
    private String createdAt;
    private long createdAtTimestamp;
    private String verifiedAt;
    private String searchStartedAt;
    private String sourceFoundAt;
    private String reservedAt;
    private String handoverStartedAt;
    private String completedAt;
    private String cancelledAt;
    private String assignedSource;
    private String bloodBankId;
    private String bloodBankName;
    private String notes;
    private String locationAddress;
    private double latitude;
    private double longitude;

    // Default constructor for Firestore
    public BloodRequest() {
        this.status = "Pending";
        this.escalationStage = 1;
    }

    public BloodRequest(String requestId, String hospitalName, String bloodGroup, String component, int requiredUnits, String urgency, String status, String createdAt) {
        this.requestId = requestId;
        this.hospitalName = hospitalName;
        this.bloodGroup = bloodGroup;
        this.component = component;
        this.requiredUnits = requiredUnits;
        this.quantity = requiredUnits;
        this.urgency = urgency;
        this.status = status;
        this.createdAt = createdAt;
        this.createdAtTimestamp = System.currentTimeMillis();
        this.escalationStage = 1;
        this.qrToken = "QR-" + requestId;
        this.otpCode = String.valueOf((int) (Math.random() * 9000) + 1000);
        this.verifiedAt = createdAt;
        this.searchStartedAt = createdAt;
        this.sourceFoundAt = createdAt;
        this.reservedAt = createdAt;
        this.handoverStartedAt = createdAt;
        this.completedAt = createdAt;
        this.assignedSource = "City Central Blood Bank";
    }

    public BloodRequest(String requestId, String hospitalId, String hospitalName, String bloodGroup, String component, int quantity, String urgency, String status, String createdAt, String notes, double latitude, double longitude, String locationAddress) {
        this.requestId = requestId;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.bloodGroup = bloodGroup;
        this.component = component != null && !component.isEmpty() ? component : "Whole Blood";
        this.quantity = quantity;
        this.requiredUnits = quantity;
        this.urgency = urgency != null && !urgency.isEmpty() ? urgency : "Normal";
        this.status = status != null && !status.isEmpty() ? status : "Pending";
        this.createdAt = createdAt;
        this.createdAtTimestamp = System.currentTimeMillis();
        this.notes = notes;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationAddress = locationAddress;
        this.escalationStage = 1;
        this.qrToken = "QR-" + requestId;
        this.otpCode = String.valueOf((int) (Math.random() * 9000) + 1000);
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getComponent() { return component != null && !component.isEmpty() ? component : "Whole Blood"; }
    public void setComponent(String component) { this.component = component; }

    public int getRequiredUnits() { return requiredUnits > 0 ? requiredUnits : quantity; }
    public void setRequiredUnits(int requiredUnits) { this.requiredUnits = requiredUnits; this.quantity = requiredUnits; }

    public int getQuantity() { return quantity > 0 ? quantity : requiredUnits; }
    public void setQuantity(int quantity) { this.quantity = quantity; this.requiredUnits = quantity; }

    public String getUrgency() { return urgency != null ? urgency : "Normal"; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public String getStatus() { return status != null ? status : "Pending"; }
    public void setStatus(String status) { this.status = status; }

    public int getEscalationStage() { return escalationStage; }
    public void setEscalationStage(int stage) { this.escalationStage = stage; }

    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public long getCreatedAtTimestamp() { return createdAtTimestamp; }
    public void setCreatedAtTimestamp(long createdAtTimestamp) { this.createdAtTimestamp = createdAtTimestamp; }

    public String getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(String verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getSearchStartedAt() { return searchStartedAt; }
    public void setSearchStartedAt(String searchStartedAt) { this.searchStartedAt = searchStartedAt; }

    public String getSourceFoundAt() { return sourceFoundAt; }
    public void setSourceFoundAt(String sourceFoundAt) { this.sourceFoundAt = sourceFoundAt; }

    public String getReservedAt() { return reservedAt; }
    public void setReservedAt(String reservedAt) { this.reservedAt = reservedAt; }

    public String getHandoverStartedAt() { return handoverStartedAt; }
    public void setHandoverStartedAt(String handoverStartedAt) { this.handoverStartedAt = handoverStartedAt; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    public String getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(String cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getAssignedSource() { return assignedSource != null ? assignedSource : (bloodBankName != null ? bloodBankName : "Unassigned"); }
    public void setAssignedSource(String assignedSource) { this.assignedSource = assignedSource; }

    public String getBloodBankId() { return bloodBankId; }
    public void setBloodBankId(String bloodBankId) { this.bloodBankId = bloodBankId; }

    public String getBloodBankName() { return bloodBankName; }
    public void setBloodBankName(String bloodBankName) { this.bloodBankName = bloodBankName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}

package com.example.blood_bank.models;

public class InventoryItem {
    private String bloodBankId;
    private String bloodBankName;
    private String bloodGroup;
    private String component;
    private int availableUnits;
    private int reservedUnits;
    private int expiryDaysLeft;
    private String recentDemand; // HIGH, MEDIUM, LOW
    private boolean priorityReview;

    public InventoryItem(String bloodBankId, String bloodBankName, String bloodGroup, String component, int availableUnits, int reservedUnits, int expiryDaysLeft, String recentDemand, boolean priorityReview) {
        this.bloodBankId = bloodBankId;
        this.bloodBankName = bloodBankName;
        this.bloodGroup = bloodGroup;
        this.component = component;
        this.availableUnits = availableUnits;
        this.reservedUnits = reservedUnits;
        this.expiryDaysLeft = expiryDaysLeft;
        this.recentDemand = recentDemand;
        this.priorityReview = priorityReview;
    }

    public String getBloodBankId() { return bloodBankId; }
    public String getBloodBankName() { return bloodBankName; }
    public String getBloodGroup() { return bloodGroup; }
    public String getComponent() { return component; }
    public int getAvailableUnits() { return availableUnits; }
    public void setAvailableUnits(int availableUnits) { this.availableUnits = availableUnits; }
    public int getReservedUnits() { return reservedUnits; }
    public int getExpiryDaysLeft() { return expiryDaysLeft; }
    public String getRecentDemand() { return recentDemand; }
    public boolean isPriorityReview() { return priorityReview; }
}

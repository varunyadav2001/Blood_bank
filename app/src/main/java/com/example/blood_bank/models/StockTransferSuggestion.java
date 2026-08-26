package com.example.blood_bank.models;

public class StockTransferSuggestion {
    private String sourceBank;
    private String destBank;
    private String bloodGroup;
    private String component;
    private int sourceAvailableUnits;
    private int destCurrentUnits;
    private String destRecentDemand;
    private int suggestedTransferQty;

    public StockTransferSuggestion(String sourceBank, String destBank, String bloodGroup, String component, int sourceAvailableUnits, int destCurrentUnits, String destRecentDemand, int suggestedTransferQty) {
        this.sourceBank = sourceBank;
        this.destBank = destBank;
        this.bloodGroup = bloodGroup;
        this.component = component;
        this.sourceAvailableUnits = sourceAvailableUnits;
        this.destCurrentUnits = destCurrentUnits;
        this.destRecentDemand = destRecentDemand;
        this.suggestedTransferQty = suggestedTransferQty;
    }

    public String getSourceBank() { return sourceBank; }
    public String getDestBank() { return destBank; }
    public String getBloodGroup() { return bloodGroup; }
    public String getComponent() { return component; }
    public int getSourceAvailableUnits() { return sourceAvailableUnits; }
    public int getDestCurrentUnits() { return destCurrentUnits; }
    public String getDestRecentDemand() { return destRecentDemand; }
    public int getSuggestedTransferQty() { return suggestedTransferQty; }
}

package com.example.blood_bank.models;

public class DemandForecast {
    private String bloodGroup;
    private int currentStock;
    private int recentDemand;
    private String weeklyDemandTrend;
    private String monthlyDemandTrend;
    private int expectedDemand;
    private String riskLevel; // HIGH, MEDIUM, LOW
    private boolean hasEnoughData;

    public DemandForecast(String bloodGroup, int currentStock, int recentDemand, String weeklyDemandTrend, String monthlyDemandTrend, int expectedDemand, String riskLevel, boolean hasEnoughData) {
        this.bloodGroup = bloodGroup;
        this.currentStock = currentStock;
        this.recentDemand = recentDemand;
        this.weeklyDemandTrend = weeklyDemandTrend;
        this.monthlyDemandTrend = monthlyDemandTrend;
        this.expectedDemand = expectedDemand;
        this.riskLevel = riskLevel;
        this.hasEnoughData = hasEnoughData;
    }

    public String getBloodGroup() { return bloodGroup; }
    public int getCurrentStock() { return currentStock; }
    public int getRecentDemand() { return recentDemand; }
    public String getWeeklyDemandTrend() { return weeklyDemandTrend; }
    public String getMonthlyDemandTrend() { return monthlyDemandTrend; }
    public int getExpectedDemand() { return expectedDemand; }
    public String getRiskLevel() { return riskLevel; }
    public boolean isHasEnoughData() { return hasEnoughData; }
}

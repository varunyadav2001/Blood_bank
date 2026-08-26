package com.example.blood_bank.models;

public class DonorProfile {
    private String donorId;
    private String name;
    private String bloodGroup;
    private boolean isAvailable;
    private double distanceKm;
    private int operationalReliabilityScore; // Non-medical Operational Score
    private int responseRate;
    private int acceptedCount;
    private int declinedCount;

    public DonorProfile(String donorId, String name, String bloodGroup, boolean isAvailable, double distanceKm, int score, int responseRate, int acceptedCount, int declinedCount) {
        this.donorId = donorId;
        this.name = name;
        this.bloodGroup = bloodGroup;
        this.isAvailable = isAvailable;
        this.distanceKm = distanceKm;
        this.operationalReliabilityScore = score;
        this.responseRate = responseRate;
        this.acceptedCount = acceptedCount;
        this.declinedCount = declinedCount;
    }

    public String getDonorId() { return donorId; }
    public String getName() { return name; }
    public String getBloodGroup() { return bloodGroup; }
    public boolean isAvailable() { return isAvailable; }
    public double getDistanceKm() { return distanceKm; }
    public int getOperationalReliabilityScore() { return operationalReliabilityScore; }
    public int getResponseRate() { return responseRate; }
    public int getAcceptedCount() { return acceptedCount; }
    public int getDeclinedCount() { return declinedCount; }
}

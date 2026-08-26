package com.example.blood_bank.models;

import java.util.HashMap;
import java.util.Map;

public class BloodBank {
    private String id;
    private String name;
    private String city;
    private String address;
    private String phone;
    private double latitude;
    private double longitude;
    private boolean verified;
    private Map<String, Integer> bloodStock;

    public BloodBank() {
        this.bloodStock = new HashMap<>();
        this.verified = true;
    }

    public BloodBank(String id, String name, String city, String address, String phone, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.verified = true;
        this.bloodStock = new HashMap<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public Map<String, Integer> getBloodStock() { return bloodStock; }
    public void setBloodStock(Map<String, Integer> bloodStock) { this.bloodStock = bloodStock; }
}

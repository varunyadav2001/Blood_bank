package com.example.blood_bank.models;

public class UserProfile {
    private String uid;
    private String email;
    private String mobileNumber;
    private String displayName;
    private String role; // "admin", "blood_bank", "hospital", "donor"
    private boolean isEmailVerified;
    private boolean isBiometricEnabled;
    private String organizationName;
    private String city;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private String locationAddress;

    public UserProfile() {}

    public UserProfile(String uid, String email, String mobileNumber, String displayName, String role, boolean isEmailVerified, boolean isBiometricEnabled, String organizationName, String city) {
        this.uid = uid;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.displayName = displayName;
        this.role = role;
        this.isEmailVerified = isEmailVerified;
        this.isBiometricEnabled = isBiometricEnabled;
        this.organizationName = organizationName;
        this.city = city;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return displayName != null && !displayName.isEmpty() ? displayName : (organizationName != null && !organizationName.isEmpty() ? organizationName : "User"); }
    public void setName(String name) { this.displayName = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPhone() { return mobileNumber; }
    public void setPhone(String phone) { this.mobileNumber = phone; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEmailVerified() { return isEmailVerified; }
    public void setEmailVerified(boolean emailVerified) { isEmailVerified = emailVerified; }

    public boolean isBiometricEnabled() { return isBiometricEnabled; }
    public void setBiometricEnabled(boolean biometricEnabled) { isBiometricEnabled = biometricEnabled; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getLocationAddress() { return locationAddress != null && !locationAddress.isEmpty() ? locationAddress : city; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }

    private String bloodBankId;
    public String getBloodBankId() { return bloodBankId != null && !bloodBankId.isEmpty() ? bloodBankId : uid; }
    public void setBloodBankId(String bloodBankId) { this.bloodBankId = bloodBankId; }

    private String bloodGroup = "O+";
    private String lastDonationDate = "Not donated yet";
    private int totalDonations = 0;
    private String availabilityStatus = "Available";
    private String dob;
    private String gender;

    public boolean isAvailable() { return "Available".equalsIgnoreCase(availabilityStatus); }
    public void setAvailable(boolean available) { this.availabilityStatus = available ? "Available" : "Unavailable"; }

    public String getBloodGroup() { return bloodGroup != null && !bloodGroup.isEmpty() ? bloodGroup : "O+"; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getLastDonationDate() { return lastDonationDate != null && !lastDonationDate.isEmpty() ? lastDonationDate : "Not donated yet"; }
    public void setLastDonationDate(String lastDonationDate) { this.lastDonationDate = lastDonationDate; }

    public int getTotalDonations() { return totalDonations; }
    public void setTotalDonations(int totalDonations) { this.totalDonations = totalDonations; }

    public String getAvailabilityStatus() { return availabilityStatus != null && !availabilityStatus.isEmpty() ? availabilityStatus : "Available"; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}

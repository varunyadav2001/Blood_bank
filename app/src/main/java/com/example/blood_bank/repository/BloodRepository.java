package com.example.blood_bank.repository;

import com.example.blood_bank.models.AuditLog;
import com.example.blood_bank.models.BloodRequest;
import com.example.blood_bank.models.DemandForecast;
import com.example.blood_bank.models.DonorProfile;
import com.example.blood_bank.models.InventoryItem;
import com.example.blood_bank.models.StockTransferSuggestion;
import com.example.blood_bank.models.UserProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BloodRepository {

    private static BloodRepository instance;
    private final List<BloodRequest> requests = new ArrayList<>();
    private final List<InventoryItem> inventoryItems = new ArrayList<>();
    private final List<AuditLog> auditLogs = new ArrayList<>();
    private final List<DemandForecast> demandForecasts = new ArrayList<>();
    private final List<StockTransferSuggestion> transferSuggestions = new ArrayList<>();
    private final Map<String, UserProfile> userDatabase = new HashMap<>();

    private UserProfile currentUser;

    private BloodRepository() {
        seedInitialData();
    }

    public static synchronized BloodRepository getInstance() {
        if (instance == null) {
            instance = new BloodRepository();
        }
        return instance;
    }

    private void seedInitialData() {
        // Standardized Users Database (Single source of truth for Roles)
        UserProfile uAlex = new UserProfile("USR-DNR-01", "alex@donor.org", "+15550192837", "Alex Rivera", "donor", true, true, "Community Donor", "Metro City");
        uAlex.setBloodGroup("O+");
        uAlex.setTotalDonations(3);
        uAlex.setLastDonationDate("15 Jan 2026");
        userDatabase.put("alex@donor.org", uAlex);

        UserProfile uBombayDnr = new UserProfile("USR-DNR-02", "bombayblood@gmail.com", "+15550199999", "Bombay Blood Donor", "donor", true, true, "Bombay Donor Club", "Metro City");
        uBombayDnr.setBloodGroup("Bombay (Oh)");
        uBombayDnr.setTotalDonations(5);
        uBombayDnr.setLastDonationDate("10 Feb 2026");
        userDatabase.put("bombayblood@gmail.com", uBombayDnr);
        userDatabase.put("dr.sarah@cityhospital.org", new UserProfile("USR-HOS-01", "dr.sarah@cityhospital.org", "+15550192838", "St. Jude General Hospital", "hospital", true, false, "St. Jude Hospital", "Metro City"));
        UserProfile uCity = new UserProfile("USR-BB-01", "bank@citycentral.org", "+15550192839", "City Central Blood Bank", "blood_bank", true, false, "City Central Reserve", "Metro City");
        uCity.setBloodBankId("BB-101");
        userDatabase.put("bank@citycentral.org", uCity);

        UserProfile uBombayGmail = new UserProfile("USR-BB-BOMBAY", "bombay@gmail.com", "+15550199991", "Bombay Blood Bank", "blood_bank", true, false, "Bombay Blood Bank", "Mumbai");
        uBombayGmail.setBloodBankId("BB-BOMBAY");
        userDatabase.put("bombay@gmail.com", uBombayGmail);

        UserProfile uBombayOrg = new UserProfile("USR-BB-BOMBAY", "bombay@bloodbank.org", "+15550199991", "Bombay Blood Bank", "blood_bank", true, false, "Bombay Blood Bank", "Mumbai");
        uBombayOrg.setBloodBankId("BB-BOMBAY");
        userDatabase.put("bombay@bloodbank.org", uBombayOrg);

        UserProfile uMsiGmail = new UserProfile("USR-BB-MSI", "msi@gmail.com", "+15550198882", "MSI Blood Bank Sangli", "blood_bank", true, false, "MSI Blood Bank Sangli", "Sangli");
        uMsiGmail.setBloodBankId("BB-MSI");
        userDatabase.put("msi@gmail.com", uMsiGmail);

        UserProfile uMsi = new UserProfile("USR-BB-MSI", "msi@bloodbank.org", "+15550198882", "MSI Blood Bank Sangli", "blood_bank", true, false, "MSI Blood Bank Sangli", "Sangli");
        uMsi.setBloodBankId("BB-MSI");
        userDatabase.put("msi@bloodbank.org", uMsi);

        UserProfile uShashwatGmail = new UserProfile("USR-BB-SHASHWAT", "shashwat@gmail.com", "+91 233 2223302", "Shashwat Blood Bank", "blood_bank", true, false, "Shashwat Blood Bank", "Miraj");
        uShashwatGmail.setBloodBankId("BB-SHASHWAT");
        userDatabase.put("shashwat@gmail.com", uShashwatGmail);

        UserProfile uShashwat = new UserProfile("USR-BB-SHASHWAT", "shashwat@bloodbank.org", "+91 233 2223302", "Shashwat Blood Bank", "blood_bank", true, false, "Shashwat Blood Bank", "Miraj");
        uShashwat.setBloodBankId("BB-SHASHWAT");
        userDatabase.put("shashwat@bloodbank.org", uShashwat);

        UserProfile uCivilGmail = new UserProfile("USR-BB-SANGLI-CIVIL", "sangli@gmail.com", "+91 233 2374503", "Sangli Civil Blood Bank", "blood_bank", true, false, "Sangli Civil Blood Bank", "Sangli");
        uCivilGmail.setBloodBankId("BB-SANGLI-CIVIL");
        userDatabase.put("sangli@gmail.com", uCivilGmail);

        UserProfile uCivil = new UserProfile("USR-BB-SANGLI-CIVIL", "sanglicivil@bloodbank.org", "+91 233 2374503", "Sangli Civil Blood Bank", "blood_bank", true, false, "Sangli Civil Blood Bank", "Sangli");
        uCivil.setBloodBankId("BB-SANGLI-CIVIL");
        userDatabase.put("sanglicivil@bloodbank.org", uCivil);

        userDatabase.put("admin@smartblood.org", new UserProfile("USR-ADM-01", "admin@smartblood.org", "+15550192840", "Chief Admin", "admin", true, true, "SmartBlood Ops", "Metro City"));

        // Initial Requests
        requests.add(new BloodRequest("REQ-2026-904", "St. Jude General Hospital", "O-", "RBC", 4, "CRITICAL", "SEARCHING", "08:30 AM"));
        requests.add(new BloodRequest("REQ-2026-891", "Metro Trauma Center", "B+", "Whole Blood", 2, "HIGH", "RESERVED", "08:15 AM"));
        requests.add(new BloodRequest("REQ-2026-850", "City General Hospital", "O+", "Plasma", 2, "NORMAL", "FULFILLED", "07:45 AM"));

        // Initial Inventory
        inventoryItems.add(new InventoryItem("BB-101", "City Central Blood Bank", "O+", "RBC", 12, 4, 15, "HIGH", false));
        inventoryItems.add(new InventoryItem("BB-101", "City Central Blood Bank", "A-", "FFP", 4, 1, 3, "MEDIUM", true));
        inventoryItems.add(new InventoryItem("BB-101", "City Central Blood Bank", "O-", "Whole Blood", 3, 2, 2, "HIGH", true));
        inventoryItems.add(new InventoryItem("BB-102", "Red Cross Reserve", "O+", "RBC", 35, 5, 20, "LOW", false));

        // Initial Demand Forecasts
        demandForecasts.add(new DemandForecast("O+", 12, 18, "+15%", "+22%", 21, "HIGH", true));
        demandForecasts.add(new DemandForecast("O-", 3, 10, "+30%", "+40%", 12, "HIGH", true));
        demandForecasts.add(new DemandForecast("A+", 42, 35, "+5%", "+8%", 38, "LOW", true));

        // Stock Transfer Suggestion
        transferSuggestions.add(new StockTransferSuggestion(
                "Red Cross Reserve (BB-102)",
                "City Central Blood Bank (BB-101)",
                "O+", "RBC", 35, 2, "HIGH", 10
        ));

        // Audit Logs
        addAuditLog("ADM-001", "ADMIN", "SYSTEM_INIT", "APP", "08:00 AM", "NONE", "READY", "Role-based architecture initialized.");
    }

    // AUTHENTICATION & SINGLE SOURCE OF TRUTH ROLE ROUTING

    public UserProfile getUserProfileByEmail(String email) {
        if (email == null) return null;
        return userDatabase.get(email.trim().toLowerCase());
    }

    public void setCurrentUser(UserProfile user) {
        this.currentUser = user;
    }

    public UserProfile authenticateEmail(String email, String password) {
        if (email == null) return null;
        String cleanEmail = email.trim().toLowerCase();
        UserProfile profile = userDatabase.get(cleanEmail);

        if (profile == null) {
            return null;
        }

        this.currentUser = profile;
        addAuditLog(profile.getUid(), profile.getRole().toUpperCase(), "LOGIN_SUCCESS", profile.getUid(), "11:00 AM", "LOGGED_OUT", "LOGGED_IN", "User authenticated with role: " + profile.getRole());
        return profile;
    }

    public UserProfile authenticateMobile(String mobileNumber) {
        String mockEmail = "mobile_" + mobileNumber.replaceAll("[^0-9]", "") + "@donor.org";
        UserProfile profile = new UserProfile("USR-MOB-" + System.currentTimeMillis(), mockEmail, mobileNumber, "Mobile User (" + mobileNumber + ")", "donor", true, false, "Mobile Donor", "Metro City");
        userDatabase.put(mockEmail, profile);
        this.currentUser = profile;
        addAuditLog(profile.getUid(), "DONOR", "LOGIN_MOBILE_OTP", profile.getUid(), "11:05 AM", "LOGGED_OUT", "LOGGED_IN", "Mobile OTP authenticated.");
        return profile;
    }

    public UserProfile authenticateGoogle() {
        UserProfile profile = new UserProfile("USR-GGL-" + System.currentTimeMillis(), "google.user@gmail.com", "+15559998888", "Google Verified User", "donor", true, false, "Google User", "Metro City");
        this.currentUser = profile;
        addAuditLog(profile.getUid(), "DONOR", "LOGIN_GOOGLE", profile.getUid(), "11:10 AM", "LOGGED_OUT", "LOGGED_IN", "Google authenticated.");
        return profile;
    }

    public UserProfile authenticateBiometric() {
        UserProfile profile = userDatabase.get("alex@donor.org");
        if (profile == null) {
            profile = new UserProfile("USR-DNR-01", "alex@donor.org", "+15550192837", "Alex Rivera", "donor", true, true, "Community Donor", "Metro City");
        }
        this.currentUser = profile;
        addAuditLog(profile.getUid(), profile.getRole().toUpperCase(), "LOGIN_BIOMETRIC", profile.getUid(), "11:12 AM", "LOGGED_OUT", "LOGGED_IN", "Biometric authenticated.");
        return profile;
    }

    // ROLE-SPECIFIC REGISTRATION METHODS

    public void saveUserProfile(UserProfile profile) {
        if (profile != null && profile.getEmail() != null) {
            userDatabase.put(profile.getEmail().trim().toLowerCase(), profile);
        }
    }

    public UserProfile registerDonorWithUid(String uid, String name, String email, String phone, String bloodGroup, String city) {
        UserProfile profile = new UserProfile(uid, email, phone, name, "donor", true, false, "Individual Donor", city);
        profile.setBloodGroup(bloodGroup);
        profile.setLastDonationDate("Not donated yet");
        profile.setTotalDonations(0);
        profile.setAvailabilityStatus("Available");
        saveUserProfile(profile);
        addAuditLog(uid, "DONOR", "REGISTER_DONOR", uid, "11:15 AM", "NONE", "REGISTERED", "Registered as Donor with Blood Group " + bloodGroup);
        return profile;
    }

    public UserProfile registerHospitalWithUid(String uid, String hospitalName, String email, String phone, String licenseNo, String city, double latitude, double longitude, String locationAddress) {
        UserProfile profile = new UserProfile(uid, email, phone, hospitalName, "hospital", true, false, hospitalName, city);
        profile.setLatitude(latitude);
        profile.setLongitude(longitude);
        profile.setLocationAddress(locationAddress);
        saveUserProfile(profile);
        addAuditLog(uid, "HOSPITAL", "REGISTER_HOSPITAL", uid, "11:18 AM", "NONE", "REGISTERED", "Registered Hospital " + hospitalName + " at " + locationAddress);
        return profile;
    }

    public UserProfile registerHospitalWithUid(String uid, String hospitalName, String email, String phone, String licenseNo, String city) {
        return registerHospitalWithUid(uid, hospitalName, email, phone, licenseNo, city, 37.7710, -122.4280, city + ", Maharashtra, India");
    }

    public UserProfile registerBloodBankWithUid(String uid, String bankName, String email, String phone, String licenseNo, String city, double latitude, double longitude, String locationAddress) {
        UserProfile profile = new UserProfile(uid, email, phone, bankName, "blood_bank", true, false, bankName, city);
        profile.setLatitude(latitude);
        profile.setLongitude(longitude);
        profile.setLocationAddress(locationAddress);
        saveUserProfile(profile);
        addAuditLog(uid, "BLOOD_BANK", "REGISTER_BLOOD_BANK", uid, "11:20 AM", "NONE", "REGISTERED", "Registered Blood Bank " + bankName + " at " + locationAddress);
        return profile;
    }

    public UserProfile registerBloodBankWithUid(String uid, String bankName, String email, String phone, String licenseNo, String city) {
        return registerBloodBankWithUid(uid, bankName, email, phone, licenseNo, city, 37.7749, -122.4194, city + ", Maharashtra, India");
    }

    public UserProfile registerDonor(String name, String email, String phone, String bloodGroup, String city) {
        return registerDonorWithUid("USR-DNR-" + System.currentTimeMillis(), name, email, phone, bloodGroup, city);
    }

    public UserProfile registerHospital(String hospitalName, String email, String phone, String licenseNo, String city) {
        return registerHospitalWithUid("USR-HOS-" + System.currentTimeMillis(), hospitalName, email, phone, licenseNo, city);
    }

    public UserProfile registerBloodBank(String bankName, String email, String phone, String licenseNo, String city) {
        return registerBloodBankWithUid("USR-BB-" + System.currentTimeMillis(), bankName, email, phone, licenseNo, city);
    }

    public void signOutUser() {
        if (currentUser != null) {
            addAuditLog(currentUser.getUid(), currentUser.getRole().toUpperCase(), "LOGOUT", currentUser.getUid(), "11:30 AM", "LOGGED_IN", "LOGGED_OUT", "User signed out.");
        }
        this.currentUser = null;
    }

    // PUBLIC EMERGENCY SEARCH (WITHOUT LOGIN)

    public List<InventoryItem> searchPublicEmergencyBlood(String bloodGroup, String component, String city) {
        List<InventoryItem> results = new ArrayList<>();
        for (InventoryItem item : inventoryItems) {
            boolean groupMatch = bloodGroup == null || bloodGroup.isEmpty() || bloodGroup.equalsIgnoreCase("ALL") || item.getBloodGroup().equalsIgnoreCase(bloodGroup);
            boolean componentMatch = component == null || component.isEmpty() || component.equalsIgnoreCase("ALL") || item.getComponent().equalsIgnoreCase(component);
            if (groupMatch && componentMatch && item.getAvailableUnits() > 0) {
                results.add(item);
            }
        }
        return results;
    }

    public UserProfile getCurrentUser() { return currentUser; }
    public List<BloodRequest> getRequests() { return requests; }
    public List<InventoryItem> getInventoryItems() { return inventoryItems; }
    public List<DemandForecast> getDemandForecasts() { return demandForecasts; }
    public List<StockTransferSuggestion> getTransferSuggestions() { return transferSuggestions; }
    public List<AuditLog> getAuditLogs() { return auditLogs; }

    public void addAuditLog(String actorId, String actorRole, String action, String entityId, String timestamp, String prevStatus, String newStatus, String details) {
        String logId = "LOG-" + (auditLogs.size() + 1001);
        auditLogs.add(0, new AuditLog(logId, actorId, actorRole, action, entityId, timestamp, prevStatus, newStatus, details));
    }
}

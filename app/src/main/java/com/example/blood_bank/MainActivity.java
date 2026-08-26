package com.example.blood_bank;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.blood_bank.models.AuditLog;
import com.example.blood_bank.models.BloodBank;
import com.example.blood_bank.models.BloodCamp;
import com.example.blood_bank.models.BloodRequest;
import com.example.blood_bank.models.CampRegistration;
import com.example.blood_bank.models.DonorAppointment;
import com.example.blood_bank.models.DonorEmergencyResponse;
import com.example.blood_bank.models.InventoryItem;
import com.example.blood_bank.models.StockTransferSuggestion;
import com.example.blood_bank.models.UserProfile;
import com.example.blood_bank.repository.BloodRepository;
import com.example.blood_bank.services.AIAssistantEngine;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public enum UserRole {
        DONOR,
        HOSPITAL,
        BLOOD_BANK,
        ADMIN
    }

    private UserRole currentRole = UserRole.DONOR;
    private FrameLayout fragmentContainer;
    private TextView activeRoleBadge;
    private TextView topBarSubtitle;
    private BottomNavigationView bottomNavigationView;

    private boolean isDonorAvailable = true;
    private int onboardingStep = 1;
    private CountDownTimer emergencyTimer;
    private CountDownTimer otpTimer;
    private final BloodRepository repository = BloodRepository.getInstance();
    private FusedLocationProviderClient fusedLocationClient;

    public static final int LOW_STOCK_THRESHOLD = 5;
    private ListenerRegistration hospitalRequestsListener;
    private ListenerRegistration hospitalNotificationsListener;
    private ListenerRegistration globalNotificationBadgeListener;
    private ListenerRegistration inventoryBankListener;
    private ListenerRegistration bloodBankDashboardListener;
    private ListenerRegistration bloodBankRequestsListener;
    private ListenerRegistration bloodBankTransfersListener;
    private ListenerRegistration bloodBankNotifsListener;
    private ListenerRegistration donorAppointmentsListener;
    private ListenerRegistration donorUpcomingAppointmentListener;
    private ListenerRegistration donorEmergencyListener;
    private ListenerRegistration donorEmergencyDispatchListener;
    private ListenerRegistration bloodBankDonorAppointmentsListener;
    private ListenerRegistration campsListListener;
    private ListenerRegistration donorCampRegistrationsListener;
    private ListenerRegistration bankPendingRequestsCountListener;
    private ListenerRegistration bankDonorAppointmentsCountListener;
    private ListenerRegistration bankEmergencyRequestsCountListener;
    private ListenerRegistration bankTransfersCountListener;
    private ListenerRegistration bankUnreadNotifsCountListener;
    private boolean isBankDonorAppointmentsTabSelected = false;
    private ListenerRegistration bloodBanksMapListener;

    private final List<BloodRequest> liveHospitalRequests = new ArrayList<>();
    private String highlightTargetRequestId = null;
    private String highlightTargetTransferId = null;
    private String highlightTargetBloodGroup = null;
    private SmartMapItem selectedBloodBank = null;
    private LatLng currentDonorLatLng = new LatLng(16.8524, 74.5815);

    private String currentBankName = "MSI Blood Bank";
    private double currentBankLat = 16.8580;
    private double currentBankLng = 74.5880;
        private String currentBankPhone = "+91 233 2374501";
    private String currentBankId = "BB-001";
    private String requestsFilterStatus = "ALL";
    private int selectedBankTabIndex = 0;
    private double userLat = 16.8524;
    private double userLng = 74.5815;

    // Map & Location references
    private MapView activeMapView;
    private GoogleMap googleMapInstance;
    private View mapNoticeBannerRef;
    private TextView txtNoticeRef;
    private View btnNoticeActionRef;
    private View mapPermissionOverlayRef;
    private TextView txtPermDescRef;
    private View btnAllowPermRef;
    private View btnOpenSettingsRef;
    private TextView txtBankNameRef;
    private TextView txtBankDistanceRef;
    private TextView txtBankStockRef;
    private TextView txtBankPhoneRef;
    private View btnQuickRequestRef;
    private View btnNavigateRef;
    private View cardSelectedBankRef;
    private Polyline activeRoutePolyline;
    private LatLng userCurrentLocation = new LatLng(16.8524, 74.5815); // Sangli coordinates

    public static class SmartMapItem {
        public String id;
        public String name;
        public String type; // BLOOD_BANK or HOSPITAL
        public double lat;
        public double lng;
        public String address;
        public String phone;
        public String area;
        public double distanceKm = 0.0;
        public boolean verified = true;
        public int totalUnits = 0;
        public Map<String, Integer> stockMap = new HashMap<>();
        public Marker marker;

        public SmartMapItem(String id, String name, String type, double lat, double lng, String address, String phone) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.lat = lat;
            this.lng = lng;
            this.address = address;
            this.phone = phone;
            this.area = address;
        }

        public SmartMapItem(String id, String name, String type, double lat, double lng, String phone, String area, boolean verified, int totalUnits, Map<String, Integer> stockMap) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.lat = lat;
            this.lng = lng;
            this.phone = phone;
            this.address = area;
            this.area = area;
            this.verified = verified;
            this.totalUnits = totalUnits;
            this.stockMap = stockMap != null ? stockMap : new HashMap<>();
        }
    }

    private final List<SmartMapItem> smartMapItemList = new ArrayList<>();

    private void cleanupHospitalListeners() {
        if (hospitalRequestsListener != null) {
            hospitalRequestsListener.remove();
            hospitalRequestsListener = null;
        }
        if (hospitalNotificationsListener != null) {
            hospitalNotificationsListener.remove();
            hospitalNotificationsListener = null;
        }
        if (bloodBanksMapListener != null) {
            bloodBanksMapListener.remove();
            bloodBanksMapListener = null;
        }
    }

    private void cleanupBankListeners() {
        if (inventoryBankListener != null) {
            inventoryBankListener.remove();
            inventoryBankListener = null;
        }
        if (bloodBankDashboardListener != null) {
            bloodBankDashboardListener.remove();
            bloodBankDashboardListener = null;
        }
        if (bloodBankRequestsListener != null) {
            bloodBankRequestsListener.remove();
            bloodBankRequestsListener = null;
        }
        if (bloodBankTransfersListener != null) {
            bloodBankTransfersListener.remove();
            bloodBankTransfersListener = null;
        }
        if (bloodBankNotifsListener != null) {
            bloodBankNotifsListener.remove();
            bloodBankNotifsListener = null;
        }
        if (bloodBankDonorAppointmentsListener != null) {
            bloodBankDonorAppointmentsListener.remove();
            bloodBankDonorAppointmentsListener = null;
        }
        if (bankPendingRequestsCountListener != null) {
            bankPendingRequestsCountListener.remove();
            bankPendingRequestsCountListener = null;
        }
        if (bankDonorAppointmentsCountListener != null) {
            bankDonorAppointmentsCountListener.remove();
            bankDonorAppointmentsCountListener = null;
        }
        if (bankEmergencyRequestsCountListener != null) {
            bankEmergencyRequestsCountListener.remove();
            bankEmergencyRequestsCountListener = null;
        }
        if (bankTransfersCountListener != null) {
            bankTransfersCountListener.remove();
            bankTransfersCountListener = null;
        }
        if (bankUnreadNotifsCountListener != null) {
            bankUnreadNotifsCountListener.remove();
            bankUnreadNotifsCountListener = null;
        }
    }

    private void cleanupDonorListeners() {
        if (donorAppointmentsListener != null) {
            donorAppointmentsListener.remove();
            donorAppointmentsListener = null;
        }
        if (donorUpcomingAppointmentListener != null) {
            donorUpcomingAppointmentListener.remove();
            donorUpcomingAppointmentListener = null;
        }
        if (donorEmergencyListener != null) {
            donorEmergencyListener.remove();
            donorEmergencyListener = null;
        }
        if (donorEmergencyDispatchListener != null) {
            donorEmergencyDispatchListener.remove();
            donorEmergencyDispatchListener = null;
        }
        if (campsListListener != null) {
            campsListListener.remove();
            campsListListener = null;
        }
        if (donorCampRegistrationsListener != null) {
            donorCampRegistrationsListener.remove();
            donorCampRegistrationsListener = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            com.google.android.gms.maps.MapsInitializer.initialize(getApplicationContext());
        } catch (Exception ignored) {}

        View mainRoot = findViewById(R.id.main_root);
        View topAppBar = findViewById(R.id.top_app_bar);
        View bottomNav = findViewById(R.id.bottom_navigation);

        if (mainRoot != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainRoot, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                if (topAppBar != null) {
                    topAppBar.setPadding(
                            topAppBar.getPaddingLeft(),
                            systemBars.top + 8,
                            topAppBar.getPaddingRight(),
                            12
                    );
                }
                if (bottomNav != null) {
                    bottomNav.setPadding(
                            bottomNav.getPaddingLeft(),
                            8,
                            bottomNav.getPaddingRight(),
                            systemBars.bottom + 8
                    );
                }
                return WindowInsetsCompat.CONSUMED;
            });
        }

        fragmentContainer = findViewById(R.id.fragment_container);
        activeRoleBadge = findViewById(R.id.txt_active_role_badge);
        topBarSubtitle = findViewById(R.id.top_bar_subtitle);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        setupTopBarActions();

        // Check active session
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && firebaseUser.getEmail() != null) {
            String uid = firebaseUser.getUid();
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            UserProfile profile = snapshot.toObject(UserProfile.class);
                            if (profile != null) {
                                repository.setCurrentUser(profile);
                                updateRoleUI(profile);
                                loadHomeDashboardForRole();
                                return;
                            }
                        }
                        loadLoginScreen();
                    })
                    .addOnFailureListener(e -> loadLoginScreen());
        } else {
            loadLoginScreen();
        }

        setupBottomNavigationListeners();
        setupGlobalNotificationBadgeListener();
    }

    private void setupGlobalNotificationBadgeListener() {
        if (globalNotificationBadgeListener != null) {
            globalNotificationBadgeListener.remove();
        }
        TextView dot = findViewById(R.id.txt_top_notification_badge);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || dot == null) return;

        globalNotificationBadgeListener = FirebaseFirestore.getInstance().collection("notifications")
                .whereEqualTo("userId", user.getUid())
                .whereEqualTo("read", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (snapshots != null && !snapshots.isEmpty()) {
                        dot.setVisibility(View.VISIBLE); dot.setText(String.valueOf(snapshots.size()));
                    } else {
                        dot.setVisibility(View.GONE);
                    }
                });
    }

    private void updateRoleUI(UserProfile profile) {
        if (profile == null) return;
        if ("HOSPITAL".equalsIgnoreCase(profile.getRole())) {
            currentRole = UserRole.HOSPITAL;
        } else if ("BLOOD_BANK".equalsIgnoreCase(profile.getRole()) || "BANK".equalsIgnoreCase(profile.getRole())) {
            currentRole = UserRole.BLOOD_BANK;
        } else if ("ADMIN".equalsIgnoreCase(profile.getRole())) {
            currentRole = UserRole.ADMIN;
        } else {
            currentRole = UserRole.DONOR;
        }

        if (activeRoleBadge != null) {
            activeRoleBadge.setVisibility(View.VISIBLE);
            activeRoleBadge.setText(currentRole.name().replace("_", " "));
        }
        if (topBarSubtitle != null) {
            topBarSubtitle.setText(profile.getName());
        }
        updateBottomMenuForRole(currentRole);
    }

    private void updateBottomMenuForRole(UserRole role) {
        if (bottomNavigationView == null) return;
        bottomNavigationView.setVisibility(View.VISIBLE);
        bottomNavigationView.getMenu().clear();
        switch (role) {
            case DONOR:
                bottomNavigationView.inflateMenu(R.menu.menu_donor);
                break;
            case HOSPITAL:
                bottomNavigationView.inflateMenu(R.menu.menu_hospital);
                break;
            case BLOOD_BANK:
                bottomNavigationView.inflateMenu(R.menu.menu_bloodbank);
                break;
            case ADMIN:
                bottomNavigationView.inflateMenu(R.menu.menu_admin);
                break;
        }
    }

    private void setupBottomNavigationListeners() {
        if (bottomNavigationView == null) return;
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d("SMARTBLOOD_CLICK", "Bottom navigation tab clicked, itemId: " + itemId);

            if (itemId == R.id.nav_donor_home || itemId == R.id.nav_hospital_home || itemId == R.id.nav_bank_home || itemId == R.id.nav_admin_home) {
                loadHomeDashboardForRole();
                return true;
            } else if (itemId == R.id.nav_donor_emergency) {
                loadView(R.layout.view_emergency_center, this::bindEmergencyCenterView);
                return true;
            } else if (itemId == R.id.nav_donor_camps) {
                loadView(R.layout.view_camps, this::bindCampsView);
                return true;
            } else if (itemId == R.id.nav_hospital_search || itemId == R.id.nav_bank_inventory) {
                loadView(R.layout.view_inventory, this::bindInventoryView);
                return true;
            } else if (itemId == R.id.nav_donor_requests || itemId == R.id.nav_hospital_requests || itemId == R.id.nav_bank_requests || itemId == R.id.nav_admin_requests) {
                loadView(R.layout.view_requests, this::bindRequestsView);
                return true;
            } else if (itemId == R.id.nav_donor_profile || itemId == R.id.nav_hospital_profile || itemId == R.id.nav_bank_profile || itemId == R.id.nav_admin_profile) {
                loadView(R.layout.view_profile, this::bindProfileView);
                return true;
            } else if (itemId == R.id.nav_admin_users || itemId == R.id.nav_admin_analytics) {
                loadView(R.layout.view_admin_dashboard_v2, this::bindAdminDashboardV2);
                return true;
            } else if (itemId == R.id.nav_bank_transfers) {
                loadView(R.layout.view_transfers, this::bindTransfersView);
                return true;
            } else if (itemId == R.id.nav_hospital_tracking) {
                loadView(R.layout.view_map, this::bindMapView);
                return true;
            }
            return false;
        });
    }

    private boolean checkRoleAuthorization(UserRole requiredRole) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            loadLoginScreen();
            return false;
        }

        if (currentRole != requiredRole && requiredRole != null) {
            Toast.makeText(this, "Access Denied: Protected for " + requiredRole.name() + " role only.", Toast.LENGTH_SHORT).show();
            loadHomeDashboardForRole();
            return false;
        }
        return true;
    }

    private void setupTopBarActions() {
        View btnNotification = findViewById(R.id.btn_top_notification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> loadView(R.layout.view_notifications, this::bindNotificationsView));
        }

        View logoIcon = findViewById(R.id.app_logo_icon);
        if (logoIcon != null) {
            logoIcon.setOnClickListener(v -> loadView(R.layout.view_onboarding, this::bindOnboardingView));
        }

        View btnLogout = findViewById(R.id.btn_top_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> handleUserLogout());
        }
    }

    private void handleUserLogout() {
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception ignored) {}
        repository.signOutUser();
        Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
        loadLoginScreen();
    }

    @Override
    public void onBackPressed() {
        if (repository.getCurrentUser() == null) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    private void loadHomeDashboardForRole() {
        UserProfile user = repository.getCurrentUser();
        if (user == null) {
            loadLoginScreen();
            return;
        }

        switch (currentRole) {
            case DONOR:
                loadView(R.layout.view_donor_dashboard_v2, this::bindDonorDashboardV2);
                break;
            case HOSPITAL:
                loadView(R.layout.view_hospital_dashboard_v2, this::bindHospitalDashboardV2);
                break;
            case BLOOD_BANK:
                loadView(R.layout.view_blood_bank_dashboard_v2, this::bindBloodBankDashboardV2);
                break;
            case ADMIN:
                loadView(R.layout.view_admin_dashboard_v2, this::bindAdminDashboardV2);
                break;
        }
    }

    private interface ViewBinder {
        void bind(View view);
    }

    private void loadView(int layoutResId, ViewBinder binder) {
        cleanupHospitalListeners();
        cleanupBankListeners();
        cleanupDonorListeners();
        if (fragmentContainer == null) return;
        fragmentContainer.removeAllViews();
        View view = LayoutInflater.from(this).inflate(layoutResId, fragmentContainer, false);
        fragmentContainer.addView(view);
        if (binder != null) {
            binder.bind(view);
        }
    }

    private void loadLoginScreen() {
        if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.GONE);
        if (activeRoleBadge != null) activeRoleBadge.setVisibility(View.GONE);
        if (topBarSubtitle != null) topBarSubtitle.setText("Smart Blood Bank Platform");
        loadView(R.layout.view_auth_role_select, this::bindRoleSelectView);
    }

    private void bindRoleSelectView(View view) {
        View cardDonor = view.findViewById(R.id.card_portal_donor);
        View cardHospital = view.findViewById(R.id.card_portal_hospital);
        View cardBloodBank = view.findViewById(R.id.card_portal_bloodbank);
        View btnEmergency = view.findViewById(R.id.btn_portal_emergency_search);
        View btnRegisterNow = view.findViewById(R.id.btn_portal_register_now);

        if (cardDonor != null) cardDonor.setOnClickListener(v -> loadDonorLoginScreen(null));
        if (cardHospital != null) cardHospital.setOnClickListener(v -> loadHospitalLoginScreen(null));
        if (cardBloodBank != null) cardBloodBank.setOnClickListener(v -> loadBloodBankLoginScreen(null));
        if (btnEmergency != null) btnEmergency.setOnClickListener(v -> showPublicEmergencySearchDialog());
        if (btnRegisterNow != null) btnRegisterNow.setOnClickListener(v -> showRegisterRoleDialog());
    }

    private void loadDonorLoginScreen(String emailPrefill) {
        if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.GONE);
        if (activeRoleBadge != null) activeRoleBadge.setText("Donor Portal");
        if (topBarSubtitle != null) topBarSubtitle.setText("Blood Donor Authentication");
        loadView(R.layout.view_auth_donor, view -> bindDonorLoginView(view, emailPrefill));
    }

    private void bindDonorLoginView(View view, String emailPrefill) {
        View btnBack = view.findViewById(R.id.btn_donor_login_back);
        EditText inputEmail = view.findViewById(R.id.input_donor_email);
        EditText inputPassword = view.findViewById(R.id.input_donor_password);
        TextView btnSubmit = view.findViewById(R.id.btn_donor_login_submit);
        TextView btnRegisterLink = view.findViewById(R.id.btn_donor_register_link);
        TextView btnForgotPassword = view.findViewById(R.id.btn_donor_forgot_password);
        TextView txtError = view.findViewById(R.id.donor_login_error_text);
        View heroIcon = view.findViewById(R.id.icon_donor_hero);
        View glowPulse = view.findViewById(R.id.glow_donor_pulse);

        if (heroIcon != null) {
            heroIcon.setAlpha(0.5f);
            heroIcon.animate().alpha(1.0f).setDuration(450).start();
        }
        if (glowPulse != null) {
            glowPulse.setAlpha(0.2f);
            glowPulse.animate().alpha(0.85f).setDuration(550).start();
        }

        if (inputEmail != null) {
            inputEmail.setText(emailPrefill != null ? emailPrefill : "omkar@gmail.com");
        }
        if (inputPassword != null) {
            inputPassword.setText("");
        }

        if (btnBack != null) btnBack.setOnClickListener(v -> loadLoginScreen());
        if (btnRegisterLink != null) btnRegisterLink.setOnClickListener(v -> showRegisterDonorDialog());
        if (btnForgotPassword != null) btnForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String emailStr = inputEmail != null ? inputEmail.getText().toString().trim() : "";
                String pwdStr = inputPassword != null ? inputPassword.getText().toString() : "";

                if (TextUtils.isEmpty(emailStr)) {
                    showLoginError(txtError, btnSubmit, "Please enter your registered donor email.");
                    if (inputEmail != null) inputEmail.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(pwdStr)) {
                    showLoginError(txtError, btnSubmit, "Please enter your password.");
                    if (inputPassword != null) inputPassword.requestFocus();
                    return;
                }

                if (txtError != null) txtError.setVisibility(View.GONE);
                btnSubmit.setEnabled(false);
                btnSubmit.setText("Signing in...");

                performRoleSpecificLogin(emailStr, pwdStr, UserRole.DONOR, txtError, btnSubmit);
            });
        }
    }

    private void loadHospitalLoginScreen(String emailPrefill) {
        if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.GONE);
        if (activeRoleBadge != null) activeRoleBadge.setText("Hospital Portal");
        if (topBarSubtitle != null) topBarSubtitle.setText("Healthcare Center Authentication");
        loadView(R.layout.view_auth_hospital, view -> bindHospitalLoginView(view, emailPrefill));
    }

    private void bindHospitalLoginView(View view, String emailPrefill) {
        View btnBack = view.findViewById(R.id.btn_hospital_login_back);
        EditText inputEmail = view.findViewById(R.id.input_hospital_email);
        EditText inputPassword = view.findViewById(R.id.input_hospital_password);
        TextView btnSubmit = view.findViewById(R.id.btn_hospital_login_submit);
        TextView btnRegisterLink = view.findViewById(R.id.btn_hospital_register_link);
        TextView btnForgotPassword = view.findViewById(R.id.btn_hospital_forgot_password);
        TextView txtError = view.findViewById(R.id.hospital_login_error_text);
        View heroIcon = view.findViewById(R.id.icon_hospital_hero);
        View glowPulse = view.findViewById(R.id.glow_hospital_pulse);

        if (heroIcon != null) {
            heroIcon.setAlpha(0.5f);
            heroIcon.animate().alpha(1.0f).setDuration(450).start();
        }
        if (glowPulse != null) {
            glowPulse.setAlpha(0.2f);
            glowPulse.animate().alpha(0.6f).setDuration(550).start();
        }

        if (inputEmail != null) {
            inputEmail.setText(emailPrefill != null ? emailPrefill : "jadhav@gmail.com");
        }
        if (inputPassword != null) {
            inputPassword.setText("");
        }

        if (btnBack != null) btnBack.setOnClickListener(v -> loadLoginScreen());
        if (btnRegisterLink != null) btnRegisterLink.setOnClickListener(v -> showRegisterHospitalDialog());
        if (btnForgotPassword != null) btnForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String emailStr = inputEmail != null ? inputEmail.getText().toString().trim() : "";
                String pwdStr = inputPassword != null ? inputPassword.getText().toString() : "";

                if (TextUtils.isEmpty(emailStr)) {
                    showLoginError(txtError, btnSubmit, "Please enter your official hospital email.");
                    if (inputEmail != null) inputEmail.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(pwdStr)) {
                    showLoginError(txtError, btnSubmit, "Please enter your password.");
                    if (inputPassword != null) inputPassword.requestFocus();
                    return;
                }

                if (txtError != null) txtError.setVisibility(View.GONE);
                btnSubmit.setEnabled(false);
                btnSubmit.setText("Signing in...");

                performRoleSpecificLogin(emailStr, pwdStr, UserRole.HOSPITAL, txtError, btnSubmit);
            });
        }
    }

    private void loadBloodBankLoginScreen(String emailPrefill) {
        if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.GONE);
        if (activeRoleBadge != null) activeRoleBadge.setText("Blood Bank Portal");
        if (topBarSubtitle != null) topBarSubtitle.setText("Reserve Center Authentication");
        loadView(R.layout.view_auth_bloodbank, view -> bindBloodBankLoginView(view, emailPrefill));
    }

    private void bindBloodBankLoginView(View view, String emailPrefill) {
        View btnBack = view.findViewById(R.id.btn_bank_login_back);
        EditText inputEmail = view.findViewById(R.id.input_bank_email);
        EditText inputPassword = view.findViewById(R.id.input_bank_password);
        TextView btnSubmit = view.findViewById(R.id.btn_bank_login_submit);
        TextView btnRegisterLink = view.findViewById(R.id.btn_bank_register_link);
        TextView btnForgotPassword = view.findViewById(R.id.btn_bank_forgot_password);
        TextView txtError = view.findViewById(R.id.bank_login_error_text);
        View heroIcon = view.findViewById(R.id.icon_bank_hero);
        View glowPulse = view.findViewById(R.id.glow_bank_pulse);

        if (heroIcon != null) {
            heroIcon.setAlpha(0.5f);
            heroIcon.animate().alpha(1.0f).setDuration(450).start();
        }
        if (glowPulse != null) {
            glowPulse.setAlpha(0.2f);
            glowPulse.animate().alpha(0.6f).setDuration(550).start();
        }

        if (inputEmail != null) {
            inputEmail.setText(emailPrefill != null ? emailPrefill : "bombay@gmail.com");
        }
        if (inputPassword != null) {
            inputPassword.setText("");
        }

        View chipBombay = view.findViewById(R.id.chip_quick_bank_bombay);
        View chipMsi = view.findViewById(R.id.chip_quick_bank_msi);
        View chipShashwat = view.findViewById(R.id.chip_quick_bank_shashwat);
        View chipSangliCivil = view.findViewById(R.id.chip_quick_bank_sanglicivil);

        if (chipBombay != null) {
            chipBombay.setOnClickListener(v -> {
                if (inputEmail != null) inputEmail.setText("bombay@gmail.com");
                if (inputPassword != null) inputPassword.setText("");
            });
        }
        if (chipMsi != null) {
            chipMsi.setOnClickListener(v -> {
                if (inputEmail != null) inputEmail.setText("msi@bloodbank.org");
                if (inputPassword != null) inputPassword.setText("");
            });
        }
        if (chipShashwat != null) {
            chipShashwat.setOnClickListener(v -> {
                if (inputEmail != null) inputEmail.setText("shashwat@bloodbank.org");
                if (inputPassword != null) inputPassword.setText("");
            });
        }
        if (chipSangliCivil != null) {
            chipSangliCivil.setOnClickListener(v -> {
                if (inputEmail != null) inputEmail.setText("sanglicivil@bloodbank.org");
                if (inputPassword != null) inputPassword.setText("");
            });
        }

        if (btnBack != null) btnBack.setOnClickListener(v -> loadLoginScreen());
        if (btnRegisterLink != null) btnRegisterLink.setOnClickListener(v -> showRegisterBloodBankDialog());
        if (btnForgotPassword != null) btnForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String emailStr = inputEmail != null ? inputEmail.getText().toString().trim() : "";
                String pwdStr = inputPassword != null ? inputPassword.getText().toString() : "";

                if (TextUtils.isEmpty(emailStr)) {
                    showLoginError(txtError, btnSubmit, "Please enter a valid email.");
                    if (inputEmail != null) inputEmail.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(pwdStr)) {
                    showLoginError(txtError, btnSubmit, "Please enter your password.");
                    if (inputPassword != null) inputPassword.requestFocus();
                    return;
                }

                if (txtError != null) txtError.setVisibility(View.GONE);
                btnSubmit.setEnabled(false);
                btnSubmit.setText("Signing in...");

                performRoleSpecificLogin(emailStr, pwdStr, UserRole.BLOOD_BANK, txtError, btnSubmit);
            });
        }
    }

    private void loadAdminLoginScreen(String emailPrefill) {
        if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.GONE);
        if (activeRoleBadge != null) activeRoleBadge.setText("Admin Portal");
        if (topBarSubtitle != null) topBarSubtitle.setText("Platform Governance Authentication");
        loadDonorLoginScreen(emailPrefill != null ? emailPrefill : "admin@smartblood.org");
    }

    private void performRoleSpecificLogin(String email, String password, UserRole expectedRole, TextView txtError, TextView btnSubmit) {
        if (TextUtils.isEmpty(email)) {
            showLoginError(txtError, btnSubmit, "Please enter your email address.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            showLoginError(txtError, btnSubmit, "Please enter your password.");
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        handleSuccessfulAuthLogin(user.getUid(), email, expectedRole, txtError, btnSubmit);
                    } else {
                        showLoginError(txtError, btnSubmit, "Authentication returned null user.");
                    }
                })
                .addOnFailureListener(e -> {
                    showLoginError(txtError, btnSubmit, mapFirebaseAuthError(e, expectedRole));
                });
    }

    private void handleSuccessfulAuthLogin(String uid, String email, UserRole expectedRole, TextView txtError, TextView btnSubmit) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        UserProfile profile = snapshot.toObject(UserProfile.class);
                        if (profile != null) {
                            UserRole actualRole = UserRole.DONOR;
                            if ("HOSPITAL".equalsIgnoreCase(profile.getRole())) actualRole = UserRole.HOSPITAL;
                            else if ("BLOOD_BANK".equalsIgnoreCase(profile.getRole()) || "BANK".equalsIgnoreCase(profile.getRole())) actualRole = UserRole.BLOOD_BANK;
                            else if ("ADMIN".equalsIgnoreCase(profile.getRole())) actualRole = UserRole.ADMIN;

                            if (actualRole != expectedRole) {
                                showLoginError(txtError, btnSubmit, "Account role mismatch: registered as " + actualRole.name() + ".");
                                FirebaseAuth.getInstance().signOut();
                                return;
                            }

                            repository.setCurrentUser(profile);
                            updateRoleUI(profile);
                            loadHomeDashboardForRole();
                            return;
                        }
                    }

                    // Fallback to expected role profile
                    UserProfile fallbackProfile = new UserProfile();
                    fallbackProfile.setUid(uid);
                    fallbackProfile.setEmail(email);
                    fallbackProfile.setRole(expectedRole.name());
                    fallbackProfile.setName(email.split("@")[0]);
                    fallbackProfile.setLatitude(16.8524);
                    fallbackProfile.setLongitude(74.5815);

                    db.collection("users").document(uid).set(fallbackProfile, SetOptions.merge());
                    repository.setCurrentUser(fallbackProfile);
                    updateRoleUI(fallbackProfile);
                    loadHomeDashboardForRole();
                })
                .addOnFailureListener(e -> showLoginError(txtError, btnSubmit, "Failed to load user profile: " + e.getMessage()));
    }

    private void showLoginError(TextView txtError, TextView btnSubmit, String errorMessage) {
        if (txtError != null) {
            txtError.setVisibility(View.VISIBLE);
            txtError.setText(errorMessage);
        }
        if (btnSubmit != null) {
            btnSubmit.setEnabled(true);
            btnSubmit.setText("Login");
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private String mapFirebaseAuthError(Exception exc, UserRole expectedRole) {
        if (exc == null || exc.getMessage() == null) return "Login failed. Please check your credentials.";
        String msg = exc.getMessage().toLowerCase(Locale.US);
        if (msg.contains("user-not-found") || msg.contains("no user record")) {
            return "No account found for this email.";
        }
        if (msg.contains("wrong-password") || msg.contains("invalid-credential")) {
            return "Incorrect password.";
        }
        if (msg.contains("network")) {
            return "Network connection error. Check your internet.";
        }
        return exc.getMessage();
    }

    private Map<String, Integer> defaultStockForBank(String bankName) {
        Map<String, Integer> stock = new HashMap<>();
        stock.put("O+", 14);
        stock.put("O-", 4);
        stock.put("A+", 10);
        stock.put("A-", 3);
        stock.put("B+", 12);
        stock.put("B-", 3);
        stock.put("AB+", 6);
        stock.put("AB-", 2);
        stock.put("Bombay (Oh)", 1);
        return stock;
    }

    private int getNumericStock(Map<String, Integer> stockMap, String bloodGroup) {
        if (stockMap == null || bloodGroup == null) return 0;
        String canonical = getCanonicalBloodGroup(bloodGroup);
        Integer val = stockMap.get(canonical);
        if (val != null && val > 0) return val;
        Integer rawVal = stockMap.get(bloodGroup);
        if (rawVal != null && rawVal > 0) return rawVal;
        for (Map.Entry<String, Integer> entry : stockMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(bloodGroup) || entry.getKey().equalsIgnoreCase(canonical)) {
                return entry.getValue();
            }
        }
        return 0;
    }

    private String getCanonicalBloodGroup(String raw) {
        if (raw == null) return "";
        String s = raw.toUpperCase(Locale.US).trim();
        if (s.equals("O+") || s.equals("O_POS") || s.equals("OPOS") || s.equals("O_POSITIVE") || s.equals("O POSITIVE") || s.equals("O +") || s.equals("O+ STOCK") || s.equals("O_POS_STOCK")) return "O+";
        if (s.equals("O-") || s.equals("O_NEG") || s.equals("ONEG") || s.equals("O_NEGATIVE") || s.equals("O NEGATIVE") || s.equals("O -") || s.equals("O_NEG_EMERGENCY") || s.equals("O- EMERGENCY") || s.equals("O- STOCK")) return "O-";
        if (s.equals("A+") || s.equals("A_POS") || s.equals("APOS") || s.equals("A_POSITIVE") || s.equals("A POSITIVE") || s.equals("A +") || s.equals("A+ STOCK") || s.equals("A_POS_STOCK")) return "A+";
        if (s.equals("A-") || s.equals("A_NEG") || s.equals("ANEG") || s.equals("A_NEGATIVE") || s.equals("A NEGATIVE") || s.equals("A -") || s.equals("A- STOCK")) return "A-";
        if (s.equals("B+") || s.equals("B_POS") || s.equals("BPOS") || s.equals("B_POSITIVE") || s.equals("B POSITIVE") || s.equals("B +") || s.equals("B+ STOCK") || s.equals("B_POS_STOCK")) return "B+";
        if (s.equals("B-") || s.equals("B_NEG") || s.equals("BNEG") || s.equals("B_NEGATIVE") || s.equals("B NEGATIVE") || s.equals("B -") || s.equals("B- STOCK")) return "B-";
        if (s.equals("AB+") || s.equals("AB_POS") || s.equals("ABPOS") || s.equals("AB_POSITIVE") || s.equals("AB POSITIVE") || s.equals("AB +") || s.equals("AB+ STOCK") || s.equals("AB_POS_STOCK")) return "AB+";
        if (s.equals("AB-") || s.equals("AB_NEG") || s.equals("ABNEG") || s.equals("AB_NEGATIVE") || s.equals("AB NEGATIVE") || s.equals("AB -") || s.equals("AB- STOCK")) return "AB-";
        if (s.contains("BOMBAY") || s.equals("OH") || s.equals("HH") || s.equals("BOMBAY (OH)") || s.equals("BOMBAY BLOOD")) return "Bombay (Oh)";
        return s;
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371 * c; // Earth radius in KM
    }

    private double calculateDistanceInKm(double lat1, double lon1, double lat2, double lon2) {
        return calculateHaversineDistance(lat1, lon1, lat2, lon2);
    }

    private boolean isDonorEligibleForEmergency(String donorGroup, String requiredGroup) {
        if (donorGroup == null || requiredGroup == null || requiredGroup.isEmpty() || requiredGroup.equalsIgnoreCase("ALL")) return true;
        String d = donorGroup.trim().toUpperCase(Locale.US);
        String r = requiredGroup.trim().toUpperCase(Locale.US);
        if (d.equals(r)) return true;
        if (d.equals("O-")) return true; // Universal donor
        if (d.equals("O+") && (r.equals("O+") || r.equals("A+") || r.equals("B+") || r.equals("AB+"))) return true;
        if (d.equals("A-") && (r.equals("A-") || r.equals("A+") || r.equals("AB-") || r.equals("AB+"))) return true;
        if (d.equals("A+") && (r.equals("A+") || r.equals("AB+"))) return true;
        if (d.equals("B-") && (r.equals("B-") || r.equals("B+") || r.equals("AB-") || r.equals("AB+"))) return true;
        if (d.equals("B+") && (r.equals("B+") || r.equals("AB+"))) return true;
        if (d.equals("AB-") && (r.equals("AB-") || r.equals("AB+"))) return true;
        if (d.equals("AB+") && r.equals("AB+")) return true;
        if (d.contains("BOMBAY") && r.contains("BOMBAY")) return true;
        return false;
    }

    private void showReserveBloodDialog() {
        showCreateBloodRequestDialog(null);
    }

    private void showRegisterRoleDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_register_role, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        View cardDonor = dialogView.findViewById(R.id.reg_role_donor);
        View cardHospital = dialogView.findViewById(R.id.reg_role_hospital);
        View cardBloodBank = dialogView.findViewById(R.id.reg_role_bloodbank);
        TextView btnClose = dialogView.findViewById(R.id.btn_close_register_dialog);

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        if (cardDonor != null) cardDonor.setOnClickListener(v -> { dialog.dismiss(); showRegisterDonorDialog(); });
        if (cardHospital != null) cardHospital.setOnClickListener(v -> { dialog.dismiss(); showRegisterHospitalDialog(); });
        if (cardBloodBank != null) cardBloodBank.setOnClickListener(v -> { dialog.dismiss(); showRegisterBloodBankDialog(); });

        dialog.show();
    }

    private void showRegisterDonorDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_register_donor, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(dialogView).create();

        EditText inputName = dialogView.findViewById(R.id.reg_donor_name);
        EditText inputEmail = dialogView.findViewById(R.id.reg_donor_email);
        EditText inputPassword = dialogView.findViewById(R.id.reg_donor_password);
        EditText inputPhone = dialogView.findViewById(R.id.reg_donor_mobile);
        AutoCompleteTextView inputBloodGroup = dialogView.findViewById(R.id.reg_donor_group);
        EditText inputDob = dialogView.findViewById(R.id.reg_donor_dob);
        AutoCompleteTextView inputGender = dialogView.findViewById(R.id.reg_donor_gender);
        EditText inputCity = dialogView.findViewById(R.id.reg_donor_city);
        TextView btnSubmit = dialogView.findViewById(R.id.btn_submit_reg_donor);
        TextView btnClose = dialogView.findViewById(R.id.btn_close_reg_donor);
        TextView txtError = dialogView.findViewById(R.id.reg_donor_error_text);

        if (inputBloodGroup != null) {
            String[] bloodGroups = {"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-", "Bombay (Oh)"};
            inputBloodGroup.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bloodGroups));
            inputBloodGroup.setText("O+", false);
            inputBloodGroup.setOnClickListener(v -> inputBloodGroup.showDropDown());
        }

        if (inputGender != null) {
            String[] genders = {"Male", "Female"};
            inputGender.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders));
            inputGender.setText("Male", false);
            inputGender.setOnClickListener(v -> inputGender.showDropDown());
        }

        if (inputDob != null) {
            inputDob.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                    inputDob.setText(String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year));
                }, c.get(Calendar.YEAR) - 20, c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dpd.show();
            });
        }

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String name = inputName != null ? inputName.getText().toString().trim() : "";
                String email = inputEmail != null ? inputEmail.getText().toString().trim() : "";
                String password = inputPassword != null ? inputPassword.getText().toString() : "";
                String phone = inputPhone != null ? inputPhone.getText().toString().trim() : "";
                String bg = inputBloodGroup != null ? inputBloodGroup.getText().toString().trim() : "O+";
                String dob = inputDob != null ? inputDob.getText().toString().trim() : "";
                String gender = inputGender != null ? inputGender.getText().toString().trim() : "Male";
                String city = inputCity != null ? inputCity.getText().toString().trim() : "Sangli";

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    if (txtError != null) {
                        txtError.setVisibility(View.VISIBLE);
                        txtError.setText("Please fill all required fields.");
                    }
                    return;
                }

                btnSubmit.setEnabled(false);
                btnSubmit.setText("Creating Account...");

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser user = authResult.getUser();
                            if (user != null) {
                                UserProfile profile = new UserProfile();
                                profile.setUid(user.getUid());
                                profile.setName(name);
                                profile.setEmail(email);
                                profile.setPhone(phone);
                                profile.setBloodGroup(bg);
                                profile.setDob(dob);
                                profile.setGender(gender);
                                profile.setRole("DONOR");
                                profile.setAvailable(true);
                                profile.setCity(city);
                                profile.setLatitude(16.8524);
                                profile.setLongitude(74.5815);

                                FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                                        .set(profile)
                                        .addOnSuccessListener(aVoid -> {
                                            dialog.dismiss();
                                            repository.setCurrentUser(profile);
                                            updateRoleUI(profile);
                                            loadHomeDashboardForRole();
                                            Toast.makeText(this, "Welcome to SmartBlood!", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("Register as Donor 🩸");
                            if (txtError != null) {
                                txtError.setVisibility(View.VISIBLE);
                                txtError.setText("Registration Failed: " + e.getMessage());
                            }
                        });
            });
        }

        dialog.show();
    }

    private void showRegisterHospitalDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_register_hospital, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(dialogView).create();

        EditText inputName = dialogView.findViewById(R.id.reg_hospital_name);
        EditText inputEmail = dialogView.findViewById(R.id.reg_hospital_email);
        EditText inputPassword = dialogView.findViewById(R.id.reg_hospital_password);
        EditText inputPhone = dialogView.findViewById(R.id.reg_hospital_phone);
        EditText inputCity = dialogView.findViewById(R.id.reg_hospital_city);
        TextView btnLocation = dialogView.findViewById(R.id.btn_reg_hospital_use_location);
        TextView txtLocationStatus = dialogView.findViewById(R.id.txt_reg_hospital_location_status);
        TextView btnSubmit = dialogView.findViewById(R.id.btn_submit_reg_hospital);
        TextView btnClose = dialogView.findViewById(R.id.btn_close_reg_hospital);
        TextView txtError = dialogView.findViewById(R.id.reg_hospital_error_text);

        final double[] capturedLocation = new double[]{0.0, 0.0};

        if (btnLocation != null) {
            btnLocation.setOnClickListener(v -> {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
                    return;
                }
                btnLocation.setText("Capturing...");
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        capturedLocation[0] = location.getLatitude();
                        capturedLocation[1] = location.getLongitude();
                        if (txtLocationStatus != null) {
                            txtLocationStatus.setText(String.format(Locale.US, "📍 Location Captured: %.4f, %.4f", capturedLocation[0], capturedLocation[1]));
                            txtLocationStatus.setBackgroundResource(R.drawable.bg_chip_status_verified);
                        }
                        btnLocation.setText("📍 Location Updated");
                    } else {
                        Toast.makeText(this, "Failed to capture location. Try again.", Toast.LENGTH_SHORT).show();
                        btnLocation.setText("📍 Use Current Location");
                    }
                });
            });
        }

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String name = inputName != null ? inputName.getText().toString().trim() : "";
                String email = inputEmail != null ? inputEmail.getText().toString().trim() : "";
                String password = inputPassword != null ? inputPassword.getText().toString() : "";
                String phone = inputPhone != null ? inputPhone.getText().toString().trim() : "";
                String city = inputCity != null ? inputCity.getText().toString().trim() : "Sangli";

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    if (txtError != null) {
                        txtError.setVisibility(View.VISIBLE);
                        txtError.setText("Please fill all required fields.");
                    }
                    return;
                }

                if (capturedLocation[0] == 0.0) {
                    if (txtError != null) {
                        txtError.setVisibility(View.VISIBLE);
                        txtError.setText("Please capture hospital location before registration.");
                    }
                    return;
                }

                btnSubmit.setEnabled(false);
                btnSubmit.setText("Creating Hospital Account...");

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser user = authResult.getUser();
                            if (user != null) {
                                UserProfile profile = new UserProfile();
                                profile.setUid(user.getUid());
                                profile.setName(name);
                                profile.setEmail(email);
                                profile.setPhone(phone);
                                profile.setRole("HOSPITAL");
                                profile.setCity(city);
                                profile.setLatitude(capturedLocation[0]);
                                profile.setLongitude(capturedLocation[1]);

                                FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                                        .set(profile)
                                        .addOnSuccessListener(aVoid -> {
                                            dialog.dismiss();
                                            repository.setCurrentUser(profile);
                                            updateRoleUI(profile);
                                            loadHomeDashboardForRole();
                                            Toast.makeText(this, "Hospital registered successfully!", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("Register as Hospital 🏥");
                            if (txtError != null) {
                                txtError.setVisibility(View.VISIBLE);
                                txtError.setText("Registration Failed: " + e.getMessage());
                            }
                        });
            });
        }
        dialog.show();
    }

    private void showRegisterBloodBankDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_register_bloodbank, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(dialogView).create();

        EditText inputName = dialogView.findViewById(R.id.reg_bank_name);
        EditText inputEmail = dialogView.findViewById(R.id.reg_bank_email);
        EditText inputPassword = dialogView.findViewById(R.id.reg_bank_password);
        EditText inputPhone = dialogView.findViewById(R.id.reg_bank_phone);
        EditText inputCity = dialogView.findViewById(R.id.reg_bank_city);
        TextView btnLocation = dialogView.findViewById(R.id.btn_reg_bank_use_location);
        TextView txtLocationStatus = dialogView.findViewById(R.id.txt_reg_bank_location_status);
        TextView btnSubmit = dialogView.findViewById(R.id.btn_submit_reg_bank);
        TextView btnClose = dialogView.findViewById(R.id.btn_close_reg_bank);
        TextView txtError = dialogView.findViewById(R.id.reg_bank_error_text);

        final double[] capturedLocation = new double[]{0.0, 0.0};

        if (btnLocation != null) {
            btnLocation.setOnClickListener(v -> {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1002);
                    return;
                }
                btnLocation.setText("Capturing...");
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        capturedLocation[0] = location.getLatitude();
                        capturedLocation[1] = location.getLongitude();
                        if (txtLocationStatus != null) {
                            txtLocationStatus.setText(String.format(Locale.US, "📍 Location Captured: %.4f, %.4f", capturedLocation[0], capturedLocation[1]));
                            txtLocationStatus.setBackgroundResource(R.drawable.bg_chip_status_verified);
                        }
                        btnLocation.setText("📍 Location Updated");
                    } else {
                        Toast.makeText(this, "Failed to capture location. Try again.", Toast.LENGTH_SHORT).show();
                        btnLocation.setText("📍 Use Current Location");
                    }
                });
            });
        }

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String name = inputName != null ? inputName.getText().toString().trim() : "";
                String email = inputEmail != null ? inputEmail.getText().toString().trim() : "";
                String password = inputPassword != null ? inputPassword.getText().toString() : "";
                String phone = inputPhone != null ? inputPhone.getText().toString().trim() : "";
                String city = inputCity != null ? inputCity.getText().toString().trim() : "Sangli";

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    if (txtError != null) {
                        txtError.setVisibility(View.VISIBLE);
                        txtError.setText("Please fill all required fields.");
                    }
                    return;
                }

                if (capturedLocation[0] == 0.0) {
                    if (txtError != null) {
                        txtError.setVisibility(View.VISIBLE);
                        txtError.setText("Please capture blood bank location before registration.");
                    }
                    return;
                }

                btnSubmit.setEnabled(false);
                btnSubmit.setText("Creating Blood Bank Account...");

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser user = authResult.getUser();
                            if (user != null) {
                                UserProfile profile = new UserProfile();
                                profile.setUid(user.getUid());
                                profile.setName(name);
                                profile.setEmail(email);
                                profile.setPhone(phone);
                                profile.setRole("BLOOD_BANK");
                                profile.setCity(city);
                                profile.setLatitude(capturedLocation[0]);
                                profile.setLongitude(capturedLocation[1]);

                                FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                                        .set(profile)
                                        .addOnSuccessListener(aVoid -> {
                                            dialog.dismiss();
                                            repository.setCurrentUser(profile);
                                            updateRoleUI(profile);
                                            loadHomeDashboardForRole();
                                            Toast.makeText(this, "Blood Bank registered successfully!", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("Register as Blood Bank 🏦");
                            if (txtError != null) {
                                txtError.setVisibility(View.VISIBLE);
                                txtError.setText("Registration Failed: " + e.getMessage());
                            }
                        });
            });
        }
        dialog.show();
    }

    private void showForgotPasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this).setView(dialogView).create();

        EditText inputEmail = dialogView.findViewById(R.id.input_reset_email);
        TextView btnSubmit = dialogView.findViewById(R.id.btn_send_reset_link);
        TextView btnClose = dialogView.findViewById(R.id.btn_close_forgot_dialog);
        TextView txtStatus = dialogView.findViewById(R.id.reset_status_message);

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String email = inputEmail != null ? inputEmail.getText().toString().trim() : "";
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnSuccessListener(aVoid -> {
                            if (txtStatus != null) {
                                txtStatus.setVisibility(View.VISIBLE);
                                txtStatus.setText("✔ Password reset email sent to " + email);
                            }
                            Toast.makeText(this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(e -> {
                            if (txtStatus != null) {
                                txtStatus.setVisibility(View.VISIBLE);
                                txtStatus.setText("Error: " + e.getMessage());
                            }
                        });
            });
        }
        dialog.show();
    }

    private void showPublicEmergencySearchDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_public_emergency_search);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText inputGroup = dialog.findViewById(R.id.input_public_blood_group);
        EditText inputComp = dialog.findViewById(R.id.input_public_component);
        EditText inputUnits = dialog.findViewById(R.id.input_public_units);
        EditText inputCity = dialog.findViewById(R.id.input_public_city);
        TextView btnSearch = dialog.findViewById(R.id.btn_execute_public_search);
        TextView btnClose = dialog.findViewById(R.id.btn_close_public_search);
        TextView btnCall = dialog.findViewById(R.id.btn_public_call_1);
        TextView btnDirections = dialog.findViewById(R.id.btn_public_directions_1);
        View resultsContainer = dialog.findViewById(R.id.container_public_results);

        if (btnClose != null) {
            btnClose.setClickable(true);
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        final double bankLat = 16.8580;
        final double bankLng = 74.5880;
        final String bankPhoneNumber = "8483912001";
        final String bankName = "City Central Blood Bank";

        View.OnClickListener callListener = v -> {
            Log.d("SMARTBLOOD_CLICK", "Public Emergency Search: Calling Bank at " + bankPhoneNumber);
            try {
                // Use ACTION_DIAL as preferred for safety and user control
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + bankPhoneNumber));
                startActivity(dialIntent);
            } catch (Exception e) {
                Log.e("SmartBlood", "Error opening dialer: " + e.getMessage());
                Toast.makeText(this, "Dialing " + bankPhoneNumber + "...", Toast.LENGTH_SHORT).show();
            }
        };

        View.OnClickListener directionsListener = v -> {
            Log.d("SMARTBLOOD_CLICK", "Public Emergency Search: Opening Directions for " + bankName);
            try {
                // Open Google Maps navigation using the bank's coordinates
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + bankLat + "," + bankLng);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                
                // Fallback to browser if Maps app is not available
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Uri webMapUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + bankLat + "," + bankLng);
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, webMapUri);
                    startActivity(webIntent);
                }
            } catch (Exception ex) {
                Log.e("SmartBlood", "Error opening maps: " + ex.getMessage());
                try {
                    // Universal geo intent as last resort
                    Uri geoUri = Uri.parse("geo:" + bankLat + "," + bankLng + "?q=" + bankLat + "," + bankLng + "(" + Uri.encode(bankName) + ")");
                    Intent geoIntent = new Intent(Intent.ACTION_VIEW, geoUri);
                    startActivity(geoIntent);
                } catch (Exception e) {
                    Toast.makeText(this, "Opening directions to " + bankName + "...", Toast.LENGTH_SHORT).show();
                }
            }
        };

        if (btnCall != null) {
            btnCall.setClickable(true);
            btnCall.setFocusable(true);
            btnCall.setOnClickListener(callListener);
        }

        if (btnDirections != null) {
            btnDirections.setClickable(true);
            btnDirections.setFocusable(true);
            btnDirections.setOnClickListener(directionsListener);
        }

        if (btnSearch != null) {
            btnSearch.setClickable(true);
            btnSearch.setFocusable(true);
            btnSearch.setOnClickListener(v -> {
                String group = inputGroup != null ? inputGroup.getText().toString().trim() : "O+";
                String units = inputUnits != null ? inputUnits.getText().toString().trim() : "2";
                if (resultsContainer != null) resultsContainer.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Found active reserves for " + group + " (" + units + " Units) at City Central Blood Bank", Toast.LENGTH_SHORT).show();
            });
        }

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
    private void bindMapView(View view) {
        initSmartBankData();

        MapView mapView = view.findViewById(R.id.map_view);
        activeMapView = mapView;
        mapNoticeBannerRef = view.findViewById(R.id.map_notice_banner);
        txtNoticeRef = view.findViewById(R.id.txt_map_notice);
        btnNoticeActionRef = view.findViewById(R.id.btn_notice_action);
        EditText inputSearch = view.findViewById(R.id.input_map_search);

        mapPermissionOverlayRef = view.findViewById(R.id.map_permission_overlay);
        txtPermDescRef = view.findViewById(R.id.txt_permission_desc);
        btnAllowPermRef = view.findViewById(R.id.btn_allow_location_access);
        btnOpenSettingsRef = view.findViewById(R.id.btn_open_app_settings);

        txtBankNameRef = view.findViewById(R.id.txt_selected_bank_name);
        txtBankDistanceRef = view.findViewById(R.id.txt_selected_bank_distance);
        txtBankStockRef = view.findViewById(R.id.txt_selected_bank_stock);
        btnQuickRequestRef = view.findViewById(R.id.btn_map_reserve_stock);
        btnNavigateRef = view.findViewById(R.id.btn_map_navigate);
        cardSelectedBankRef = view.findViewById(R.id.map_bottom_sheet);

        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(map -> {
                googleMapInstance = map;
                setupMapSettings(map);
                renderAllMarkersOnMap(map);
            });
        }

        if (inputSearch != null) {
            inputSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterMapMarkers(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        setupMapFilterChips(view);
    }

    private void setupMapFilterChips(View view) {
        TextView chipAll = view.findViewById(R.id.chip_group_all);
        if (chipAll != null) {
            chipAll.setOnClickListener(v -> {
                for (SmartMapItem item : smartMapItemList) if (item.marker != null) item.marker.setVisible(true);
            });
        }
    }

    private void initSmartBankData() {
        if (!smartMapItemList.isEmpty()) return;
        smartMapItemList.add(new SmartMapItem("BB-001", "MSI Blood Bank", "BLOOD_BANK", 16.8580, 74.5880, "Opp. Civil Hospital, Sangli", "+91 98220 11223"));
        smartMapItemList.add(new SmartMapItem("BB-002", "Bombay Blood Bank", "BLOOD_BANK", 16.8450, 74.6010, "Station Road, Sangli", "+91 98220 44556"));
        smartMapItemList.add(new SmartMapItem("BB-003", "Shashwat Blood Bank", "BLOOD_BANK", 16.8620, 74.5650, "Vishrambag, Sangli", "+91 98220 77889"));
        smartMapItemList.add(new SmartMapItem("BB-004", "Sangli Civil Blood Bank", "BLOOD_BANK", 16.8520, 74.5800, "Civil Hospital Campus, Sangli", "+91 98220 99000"));
    }

    private void setupMapSettings(GoogleMap map) {
        if (map == null) return;
        try {
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userCurrentLocation, 13f));
        } catch (Exception ignored) {}
    }

    private void renderAllMarkersOnMap(GoogleMap map) {
        if (map == null) return;
        map.clear();
        for (SmartMapItem item : smartMapItemList) {
            LatLng pos = new LatLng(item.lat, item.lng);
            Marker m = map.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(item.name)
                    .snippet(item.address));
            item.marker = m;
        }

        map.setOnMarkerClickListener(marker -> {
            for (SmartMapItem item : smartMapItemList) {
                if (item.marker != null && item.marker.getId().equals(marker.getId())) {
                    showSelectedBankOnMap(item, true);
                    return true;
                }
            }
            return false;
        });
    }

    private void filterMapMarkers(String query) {
        if (TextUtils.isEmpty(query)) {
            for (SmartMapItem item : smartMapItemList) {
                if (item.marker != null) item.marker.setVisible(true);
            }
            return;
        }
        String q = query.toLowerCase(Locale.US);
        for (SmartMapItem item : smartMapItemList) {
            boolean matches = item.name.toLowerCase(Locale.US).contains(q) || item.address.toLowerCase(Locale.US).contains(q);
            if (item.marker != null) item.marker.setVisible(matches);
        }
    }

    private void showSelectedBankOnMap(SmartMapItem item, boolean updateCamera) {
        if (item == null) return;
        if (cardSelectedBankRef != null) cardSelectedBankRef.setVisibility(View.VISIBLE);
        if (txtBankNameRef != null) txtBankNameRef.setText(item.name);
        if (txtBankDistanceRef != null) txtBankDistanceRef.setText("Distance: " + String.format(Locale.US, "%.1f km", calculateHaversineDistance(userCurrentLocation.latitude, userCurrentLocation.longitude, item.lat, item.lng)));

        if (btnQuickRequestRef != null) {
            btnQuickRequestRef.setOnClickListener(v -> showCreateBloodRequestDialog(item.id));
        }

        if (btnNavigateRef != null) {
            btnNavigateRef.setOnClickListener(v -> {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + item.lat + "," + item.lng);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(mapIntent);
                } catch (Exception e) {
                    Toast.makeText(this, "Opening map coordinates...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (updateCamera && googleMapInstance != null) {
            googleMapInstance.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(item.lat, item.lng), 15f));
        }
    }
                    private void bindHospitalDashboardV2(View view) {
        View glowCyan = view.findViewById(R.id.bg_glow_hosp_dash_cyan);
        View gridHosp = view.findViewById(R.id.bg_grid_hosp_dash);

        if (glowCyan != null) {
            ObjectAnimator pulseCyan = ObjectAnimator.ofFloat(glowCyan, "alpha", 0.55f, 0.95f, 0.55f);
            pulseCyan.setDuration(3600);
            pulseCyan.setRepeatCount(ValueAnimator.INFINITE);
            pulseCyan.start();
        }
        if (gridHosp != null) {
            ObjectAnimator gridAnim = ObjectAnimator.ofFloat(gridHosp, "translationY", -10f, 10f, -10f);
            gridAnim.setDuration(8000);
            gridAnim.setRepeatCount(ValueAnimator.INFINITE);
            gridAnim.start();
        }

        UserProfile currentUser = repository.getCurrentUser();
        String hospName = currentUser != null && currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "St. Jude General Hospital";
        String hospId = currentUser != null ? currentUser.getUid() : "HOS-8842";
        String hospLocation = currentUser != null && currentUser.getLocationAddress() != null ? currentUser.getLocationAddress() : "Sangli, Maharashtra, India";

        TextView txtName = view.findViewById(R.id.txt_hosp_dash_name);
        TextView txtId = view.findViewById(R.id.txt_hosp_dash_id);
        TextView txtLoc = view.findViewById(R.id.txt_hosp_dash_location);

        if (txtName != null) txtName.setText(hospName);
        if (txtId != null) txtId.setText("✔ Verified Hospital Account  -  ID: " + hospId);
        if (txtLoc != null) txtLoc.setText("📍 " + hospLocation);

        View btnCreateRequest = view.findViewById(R.id.btn_create_request_v2);
        if (btnCreateRequest != null) {
            btnCreateRequest.setOnClickListener(v -> showCreateBloodRequestDialog(null));
        }

        View btnEmergencyAction = view.findViewById(R.id.btn_hosp_emergency_action);
        if (btnEmergencyAction != null) {
            btnEmergencyAction.setOnClickListener(v -> showEmergencyRequestDialog(null, 4, null, userLat, userLng));
        }

        View cardTotalReq = view.findViewById(R.id.card_stat_total_requests);
        if (cardTotalReq != null) {
            cardTotalReq.setOnClickListener(v -> {
                requestsFilterStatus = "ALL";
                loadView(R.layout.view_requests, this::bindRequestsView);
            });
        }

        View cardPendingReq = view.findViewById(R.id.card_stat_pending_requests);
        if (cardPendingReq != null) {
            cardPendingReq.setOnClickListener(v -> {
                requestsFilterStatus = "PENDING";
                loadView(R.layout.view_requests, this::bindRequestsView);
            });
        }

        View cardAcceptedReq = view.findViewById(R.id.card_stat_accepted_requests);
        if (cardAcceptedReq != null) {
            cardAcceptedReq.setOnClickListener(v -> {
                requestsFilterStatus = "IN_TRANSIT";
                loadView(R.layout.view_requests, this::bindRequestsView);
            });
        }

        View cardFulfilledReq = view.findViewById(R.id.card_stat_fulfilled_requests);
        if (cardFulfilledReq != null) {
            cardFulfilledReq.setOnClickListener(v -> {
                requestsFilterStatus = "FULFILLED";
                loadView(R.layout.view_requests, this::bindRequestsView);
            });
        }

        View cardEmergencyReq = view.findViewById(R.id.card_stat_emergency_requests);
        if (cardEmergencyReq != null) {
            cardEmergencyReq.setOnClickListener(v -> {
                requestsFilterStatus = "EMERGENCY";
                loadView(R.layout.view_requests, this::bindRequestsView);
            });
        }

        View cardLowStock = view.findViewById(R.id.card_stat_low_stock);
        if (cardLowStock != null) {
            cardLowStock.setOnClickListener(v -> showLowStockDetailsDialog());
        }

        TextView txtStatTotal = view.findViewById(R.id.txt_stat_total_requests);
        TextView txtStatPending = view.findViewById(R.id.txt_stat_pending_requests);
        TextView txtStatAccepted = view.findViewById(R.id.txt_stat_accepted_requests);
        TextView txtStatFulfilled = view.findViewById(R.id.txt_stat_fulfilled_requests);
        TextView txtStatLowStock = view.findViewById(R.id.txt_stat_low_stock);
        TextView txtStatEmergency = view.findViewById(R.id.txt_stat_emergency_requests);

        View layoutActiveCard = view.findViewById(R.id.layout_active_request_card);
        View layoutEmptyRequests = view.findViewById(R.id.layout_hosp_empty_requests);
        TextView txtActiveReqId = view.findViewById(R.id.txt_active_req_id);
        TextView txtActiveStatusChip = view.findViewById(R.id.txt_active_req_status_chip);
        TextView txtActiveDetails = view.findViewById(R.id.txt_active_req_details);
        TextView txtActiveTime = view.findViewById(R.id.txt_active_req_hospital_time);
        TextView txtTimeline1 = view.findViewById(R.id.txt_timeline_step1);
        TextView txtTimeline2 = view.findViewById(R.id.txt_timeline_step2);
        TextView txtTimeline3 = view.findViewById(R.id.txt_timeline_step3);
        TextView txtTimeline4 = view.findViewById(R.id.txt_timeline_step4);
        View btnTrack = view.findViewById(R.id.btn_track_request_details);
        View btnCancelActive = view.findViewById(R.id.btn_cancel_active_request);

        String currentUid = currentUser != null ? currentUser.getUid() : "HOS-8842";
        try {
            cleanupHospitalListeners();
            hospitalRequestsListener = FirebaseFirestore.getInstance().collection("bloodRequests")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("SmartBlood", "Error listening to hospital requests: " + e.getMessage());
                        return;
                    }

                    liveHospitalRequests.clear();
                    int total = 0;
                    int pending = 0;
                    int accepted = 0;
                    int fulfilled = 0;
                    int emergency = 0;

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            String hId = doc.getString("hospitalId");
                            String hUid = doc.getString("hospitalUid");
                            String hName = doc.getString("hospitalName");

                            boolean matchesHosp = (currentUid != null && (currentUid.equalsIgnoreCase(hId) || currentUid.equalsIgnoreCase(hUid))) ||
                                                  (hName != null && (hName.equalsIgnoreCase(hospName) || hName.toLowerCase(Locale.US).contains(hospName.toLowerCase(Locale.US)) || hospName.toLowerCase(Locale.US).contains(hName.toLowerCase(Locale.US))));

                            if (!matchesHosp) continue;

                            String reqId = doc.getString("requestId") != null ? doc.getString("requestId") : doc.getId();
                            String hosp = doc.getString("hospitalName") != null ? doc.getString("hospitalName") : hospName;
                            String bGroup = doc.getString("bloodGroup") != null ? doc.getString("bloodGroup") : "O+";
                            String comp = doc.getString("component") != null ? doc.getString("component") : "Packed RBC";
                            Long qtyLong = doc.getLong("quantity");
                            if (qtyLong == null) qtyLong = doc.getLong("units");
                            if (qtyLong == null) qtyLong = doc.getLong("requiredUnits");
                            int qty = qtyLong != null ? qtyLong.intValue() : 4;
                            String urgency = doc.getString("urgency") != null ? doc.getString("urgency") : (doc.getString("priority") != null ? doc.getString("priority") : "Normal");
                            String status = doc.getString("status") != null ? doc.getString("status") : "Pending";
                            String createdAt = doc.getString("createdAt") != null ? doc.getString("createdAt") : "Recent";
                            String notes = doc.getString("notes") != null ? doc.getString("notes") : "";

                            BloodRequest req = new BloodRequest(reqId, currentUid, hosp, bGroup, comp, qty, urgency, status, createdAt, notes, userLat, userLng, hospLocation);
                            String assigned = doc.getString("assignedSource");
                            if (assigned != null) req.setAssignedSource(assigned);
                            liveHospitalRequests.add(req);

                            total++;
                            String statusLower = status.toLowerCase(Locale.US);
                            if (statusLower.contains("pending") || statusLower.contains("searching")) {
                                pending++;
                            } else if (statusLower.contains("accept") || statusLower.contains("reserv") || statusLower.contains("transit")) {
                                accepted++;
                            } else if (statusLower.contains("fulfil") || statusLower.contains("complet")) {
                                fulfilled++;
                            }

                            String urgencyLower = urgency.toLowerCase(Locale.US);
                            if (urgencyLower.contains("emerg") || urgencyLower.contains("critic")) {
                                emergency++;
                            }
                        }
                    }

                    if (txtStatTotal != null) txtStatTotal.setText(String.valueOf(total));
                    if (txtStatPending != null) txtStatPending.setText(String.valueOf(pending));
                    if (txtStatAccepted != null) txtStatAccepted.setText(String.valueOf(accepted));
                    if (txtStatFulfilled != null) txtStatFulfilled.setText(String.valueOf(fulfilled));
                    if (txtStatEmergency != null) txtStatEmergency.setText(String.valueOf(emergency));

                    if (!liveHospitalRequests.isEmpty()) {
                        BloodRequest latestReq = liveHospitalRequests.get(0);
                        if (layoutActiveCard != null) layoutActiveCard.setVisibility(View.VISIBLE);
                        if (layoutEmptyRequests != null) layoutEmptyRequests.setVisibility(View.GONE);

                        if (txtActiveReqId != null) txtActiveReqId.setText(latestReq.getRequestId());
                        if (txtActiveDetails != null) {
                            txtActiveDetails.setText(String.format(Locale.US, "%s  -  %d Units  -  %s", latestReq.getBloodGroup(), latestReq.getQuantity(), latestReq.getComponent()));
                        }
                        if (txtActiveTime != null) {
                            String noteStr = latestReq.getNotes() != null && !latestReq.getNotes().isEmpty() ? "  -  " + latestReq.getNotes() : "";
                            txtActiveTime.setText(String.format(Locale.US, "Requested %s%s", latestReq.getCreatedAt(), noteStr));
                        }

                        String status = latestReq.getStatus();
                        String statusUpper = status != null ? status.toUpperCase(Locale.US) : "PENDING";
                        if (txtActiveStatusChip != null) {
                            txtActiveStatusChip.setText(statusUpper);
                            if (statusUpper.contains("FULFIL")) {
                                txtActiveStatusChip.setBackgroundResource(R.drawable.bg_chip_status_available);
                                txtActiveStatusChip.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                            } else if (statusUpper.contains("ACCEPT") || statusUpper.contains("RESERV")) {
                                txtActiveStatusChip.setBackgroundResource(R.drawable.bg_chip_status_reserved);
                                txtActiveStatusChip.setTextColor(ContextCompat.getColor(this, R.color.status_reserved_text));
                            } else if (statusUpper.contains("CANCEL")) {
                                txtActiveStatusChip.setBackgroundResource(R.drawable.bg_chip_status_pending);
                                txtActiveStatusChip.setTextColor(ContextCompat.getColor(this, R.color.text_tertiary));
                            } else {
                                txtActiveStatusChip.setBackgroundResource(R.drawable.bg_chip_status_critical);
                                txtActiveStatusChip.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                            }
                        }

                        if (txtTimeline1 != null) {
                            txtTimeline1.setText("✔ Step 1: Requisition Created & Verified (" + latestReq.getCreatedAt() + ")");
                            txtTimeline1.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                        }
                        if (txtTimeline2 != null) {
                            if (statusUpper.contains("ACCEPT") || statusUpper.contains("FULFIL") || statusUpper.contains("RESERV") || statusUpper.contains("ALLOCAT")) {
                                txtTimeline2.setText("✔ Step 2: Blood Bank Allocated (" + (latestReq.getAssignedSource() != null ? latestReq.getAssignedSource() : "MSI Blood Bank") + ")");
                                txtTimeline2.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                            } else if (statusUpper.contains("CANCEL")) {
                                txtTimeline2.setText("⚪ Step 2: Allocation Cancelled");
                                txtTimeline2.setTextColor(ContextCompat.getColor(this, R.color.text_tertiary));
                            } else {
                                txtTimeline2.setText("⏳ Step 2: Blood Bank Proximity Search & Allocation");
                                txtTimeline2.setTextColor(ContextCompat.getColor(this, R.color.status_low_text));
                            }
                        }
                        if (txtTimeline3 != null) {
                            if (statusUpper.contains("FULFIL")) {
                                txtTimeline3.setText("✔ Step 3: Verified Courier Dispatch & Handover Completed");
                                txtTimeline3.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                            } else if (statusUpper.contains("ACCEPT") || statusUpper.contains("RESERV")) {
                                txtTimeline3.setText("⏳ Step 3: Courier In Transit  -  OTP Handover Ready");
                                txtTimeline3.setTextColor(ContextCompat.getColor(this, R.color.status_reserved_text));
                            } else {
                                txtTimeline3.setText("⚪ Step 3: Verified Courier Dispatch & Handover");
                                txtTimeline3.setTextColor(ContextCompat.getColor(this, R.color.text_tertiary));
                            }
                        }
                        if (txtTimeline4 != null) {
                            if (statusUpper.contains("FULFIL")) {
                                txtTimeline4.setText("✔ Step 4: Transfusion Delivered & Fulfilled");
                                txtTimeline4.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                            } else {
                                txtTimeline4.setText("⚪ Step 4: Transfusion Delivered & Fulfilled");
                                txtTimeline4.setTextColor(ContextCompat.getColor(this, R.color.text_tertiary));
                            }
                        }

                        if (btnTrack != null) {
                            btnTrack.setOnClickListener(v -> loadView(R.layout.view_map, this::bindMapView));
                        }

                        if (btnCancelActive != null) {
                            if (statusUpper.contains("PENDING") || statusUpper.contains("SEARCHING")) {
                                btnCancelActive.setVisibility(View.VISIBLE);
                                btnCancelActive.setOnClickListener(v -> {
                                    new AlertDialog.Builder(this)
                                        .setTitle("Cancel Blood Request")
                                        .setMessage("Are you sure you want to cancel request " + latestReq.getRequestId() + "?")
                                        .setPositiveButton("Yes, Cancel", (diag, which) -> {
                                            String timeStr = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());
                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("status", "Cancelled");
                                            updates.put("cancelledAt", timeStr);
                                            FirebaseFirestore.getInstance().collection("bloodRequests").document(latestReq.getRequestId()).update(updates);
                                            Toast.makeText(this, "Request " + latestReq.getRequestId() + " cancelled.", Toast.LENGTH_SHORT).show();
                                        })
                                        .setNegativeButton("No", null)
                                        .show();
                                });
                            } else {
                                btnCancelActive.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        if (layoutActiveCard != null) layoutActiveCard.setVisibility(View.GONE);
                        if (layoutEmptyRequests != null) layoutEmptyRequests.setVisibility(View.VISIBLE);
                    }
                });
        } catch (Exception e) {
            Log.e("SmartBlood", "Error setting up hospital requests listener: " + e.getMessage());
        }
    }

    private void bindBloodBankDashboardV2(View view) {
        View glowPurple = view.findViewById(R.id.bg_glow_bank_dash_purple);
        View nodesBank = view.findViewById(R.id.bg_nodes_bank_dash);

        if (glowPurple != null) {
            ObjectAnimator pulsePurple = ObjectAnimator.ofFloat(glowPurple, "alpha", 0.50f, 0.95f, 0.50f);
            pulsePurple.setDuration(4000);
            pulsePurple.setRepeatCount(ValueAnimator.INFINITE);
            pulsePurple.start();
        }
        if (nodesBank != null) {
            ObjectAnimator nodesAnim = ObjectAnimator.ofFloat(nodesBank, "translationY", -10f, 10f, -10f);
            nodesAnim.setDuration(7800);
            nodesAnim.setRepeatCount(ValueAnimator.INFINITE);
            nodesAnim.start();
        }

        View btnAi = view.findViewById(R.id.btn_bloodbank_ai);
        if (btnAi != null) btnAi.setOnClickListener(v -> showAIAssistantDialog());

        View btnSwitchFacility = view.findViewById(R.id.btn_bank_switch_facility);
        if (btnSwitchFacility != null) btnSwitchFacility.setOnClickListener(v -> showFacilitySwitcherDialog());

        UserProfile currentUser = repository.getCurrentUser();
        String bankName = currentUser != null && currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentBankName;
        String currentUid = currentUser != null ? currentUser.getUid() : "BB-101";
        String activeBankId = currentUser != null && currentUser.getBloodBankId() != null ? currentUser.getBloodBankId() : currentBankId;
        String city = currentUser != null && currentUser.getCity() != null ? currentUser.getCity() : "Sangli";
        String address = currentUser != null && currentUser.getLocationAddress() != null ? currentUser.getLocationAddress() : (city + ", Maharashtra");

        TextView txtName = view.findViewById(R.id.txt_bank_dash_name);
        TextView txtSubtitle = view.findViewById(R.id.txt_bank_dash_subtitle);
        TextView txtLocation = view.findViewById(R.id.txt_bank_dash_location);
        ProgressBar progress = view.findViewById(R.id.loading_bank_progress);

        if (txtName != null) txtName.setText(bankName);
        if (txtSubtitle != null) txtSubtitle.setText("✔ Verified Reserve Dashboard  -  ID: " + activeBankId);
        if (txtLocation != null) txtLocation.setText("📍 " + address);

        View cardPendingReq = view.findViewById(R.id.card_bank_stat_pending_requests);
        if (cardPendingReq != null) {
            cardPendingReq.setOnClickListener(v -> {
                selectedBankTabIndex = 0;
                loadView(R.layout.view_requests, this::bindRequestsView);
            });
        }

        View cardDonorAppt = view.findViewById(R.id.card_bank_stat_donor_appointments);
        if (cardDonorAppt != null) {
            cardDonorAppt.setOnClickListener(v -> {
                selectedBankTabIndex = 1;
                loadView(R.layout.view_requests, this::bindRequestsView);
            });
        }

        View cardEmergencyReq = view.findViewById(R.id.card_bank_stat_emergency_requests);
        if (cardEmergencyReq != null) {
            cardEmergencyReq.setOnClickListener(v -> {
                requestsFilterStatus = "EMERGENCY";
                loadView(R.layout.view_requests, this::bindRequestsView);
            });
        }

        View cardTransfers = view.findViewById(R.id.card_bank_stat_transfers);
        if (cardTransfers != null) {
            cardTransfers.setOnClickListener(v -> loadView(R.layout.view_transfers, this::bindTransfersView));
        }

        View cardNotifications = view.findViewById(R.id.card_bank_stat_notifications);
        if (cardNotifications != null) {
            cardNotifications.setOnClickListener(v -> loadView(R.layout.view_notifications, this::bindNotificationsView));
        }

        TextView txtStatTotal = view.findViewById(R.id.txt_bank_stat_total_units);
        TextView txtStatAvailable = view.findViewById(R.id.txt_bank_stat_available_units);
        TextView txtStatLowStock = view.findViewById(R.id.txt_bank_stat_low_stock);
        TextView txtStatOutOfStock = view.findViewById(R.id.txt_bank_stat_out_of_stock);

        TextView txtOPosUnits = view.findViewById(R.id.txt_bank_stock_o_pos_units);
        TextView txtOPosStatus = view.findViewById(R.id.txt_bank_stock_o_pos_status);
        TextView txtONegUnits = view.findViewById(R.id.txt_bank_stock_o_neg_units);
        TextView txtONegStatus = view.findViewById(R.id.txt_bank_stock_o_neg_status);
        TextView txtAPosUnits = view.findViewById(R.id.txt_bank_stock_a_pos_units);
        TextView txtAPosStatus = view.findViewById(R.id.txt_bank_stock_a_pos_status);
        TextView txtANegUnits = view.findViewById(R.id.txt_bank_stock_a_neg_units);
        TextView txtANegStatus = view.findViewById(R.id.txt_bank_stock_a_neg_status);
        TextView txtBPosUnits = view.findViewById(R.id.txt_bank_stock_b_pos_units);
        TextView txtBPosStatus = view.findViewById(R.id.txt_bank_stock_b_pos_status);
        TextView txtBNegUnits = view.findViewById(R.id.txt_bank_stock_b_neg_units);
        TextView txtBNegStatus = view.findViewById(R.id.txt_bank_stock_b_neg_status);
        TextView txtABPosUnits = view.findViewById(R.id.txt_bank_stock_ab_pos_units);
        TextView txtABPosStatus = view.findViewById(R.id.txt_bank_stock_ab_pos_status);
        TextView txtABNegUnits = view.findViewById(R.id.txt_bank_stock_ab_neg_units);
        TextView txtABNegStatus = view.findViewById(R.id.txt_bank_stock_ab_neg_status);
        TextView txtBombayUnits = view.findViewById(R.id.txt_bank_stock_bombay_units);
        TextView txtBombayStatus = view.findViewById(R.id.txt_bank_stock_bombay_status);

        View cardOPos = view.findViewById(R.id.card_bank_stock_o_pos);
        View cardONeg = view.findViewById(R.id.card_bank_stock_o_neg);
        View cardAPos = view.findViewById(R.id.card_bank_stock_a_pos);
        View cardANeg = view.findViewById(R.id.card_bank_stock_a_neg);
        View cardBPos = view.findViewById(R.id.card_bank_stock_b_pos);
        View cardBNeg = view.findViewById(R.id.card_bank_stock_b_neg);
        View cardABPos = view.findViewById(R.id.card_bank_stock_ab_pos);
        View cardABNeg = view.findViewById(R.id.card_bank_stock_ab_neg);
        View cardBombay = view.findViewById(R.id.card_bank_stock_bombay);

        TextView txtAlertCrit = view.findViewById(R.id.txt_bank_alert_critical);
        TextView txtAlertLow = view.findViewById(R.id.txt_bank_alert_low);
        TextView txtAlertInfo = view.findViewById(R.id.txt_bank_alert_info);

        TextView txtPendingCount = view.findViewById(R.id.txt_bank_stat_pending_requests);
        TextView txtDonorAptCount = view.findViewById(R.id.txt_bank_stat_donor_appointments);
        TextView txtEmergencyCount = view.findViewById(R.id.txt_bank_stat_emergency_requests);
        TextView txtTransfersCount = view.findViewById(R.id.txt_bank_stat_active_transfers);
        TextView txtUnreadNotifsCount = view.findViewById(R.id.txt_bank_stat_unread_notifs);

        java.util.function.Consumer<Map<String, Integer>> renderStockData = (stockMap) -> {
            if (stockMap == null) return;

            int oPos = getNumericStock(stockMap, "O+");
            int oNeg = getNumericStock(stockMap, "O-");
            int aPos = getNumericStock(stockMap, "A+");
            int aNeg = getNumericStock(stockMap, "A-");
            int bPos = getNumericStock(stockMap, "B+");
            int bNeg = getNumericStock(stockMap, "B-");
            int abPos = getNumericStock(stockMap, "AB+");
            int abNeg = getNumericStock(stockMap, "AB-");
            int bombay = getNumericStock(stockMap, "Bombay (Oh)");

            updateBloodGroupStockCard(txtOPosUnits, txtOPosStatus, oPos);
            updateBloodGroupStockCard(txtONegUnits, txtONegStatus, oNeg);
            updateBloodGroupStockCard(txtAPosUnits, txtAPosStatus, aPos);
            updateBloodGroupStockCard(txtANegUnits, txtANegStatus, aNeg);
            updateBloodGroupStockCard(txtBPosUnits, txtBPosStatus, bPos);
            updateBloodGroupStockCard(txtBNegUnits, txtBNegStatus, bNeg);
            updateBloodGroupStockCard(txtABPosUnits, txtABPosStatus, abPos);
            updateBloodGroupStockCard(txtABNegUnits, txtABNegStatus, abNeg);
            updateBloodGroupStockCard(txtBombayUnits, txtBombayStatus, bombay);

            if (cardOPos != null) cardOPos.setOnClickListener(v -> showUpdateInventoryStockDialog("O+", oPos));
            if (cardONeg != null) cardONeg.setOnClickListener(v -> showUpdateInventoryStockDialog("O-", oNeg));
            if (cardAPos != null) cardAPos.setOnClickListener(v -> showUpdateInventoryStockDialog("A+", aPos));
            if (cardANeg != null) cardANeg.setOnClickListener(v -> showUpdateInventoryStockDialog("A-", aNeg));
            if (cardBPos != null) cardBPos.setOnClickListener(v -> showUpdateInventoryStockDialog("B+", bPos));
            if (cardBNeg != null) cardBNeg.setOnClickListener(v -> showUpdateInventoryStockDialog("B-", bNeg));
            if (cardABPos != null) cardABPos.setOnClickListener(v -> showUpdateInventoryStockDialog("AB+", abPos));
            if (cardABNeg != null) cardABNeg.setOnClickListener(v -> showUpdateInventoryStockDialog("AB-", abNeg));
            if (cardBombay != null) cardBombay.setOnClickListener(v -> showUpdateInventoryStockDialog("Bombay (Oh)", bombay));

            int totalUnits = oPos + oNeg + aPos + aNeg + bPos + bNeg + abPos + abNeg + bombay;
            int[] allStandardStock = new int[]{oPos, oNeg, aPos, aNeg, bPos, bNeg, abPos, abNeg};
            String[] groupNames = new String[]{"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"};

            int lowStockCount = 0;
            int outOfStockCount = 0;
            int availableUnits = totalUnits; 
            List<String> lowGroups = new ArrayList<>();
            List<String> outGroups = new ArrayList<>();

            for (int i = 0; i < allStandardStock.length; i++) {
                int u = allStandardStock[i];
                if (u == 0) {
                    outOfStockCount++;
                    outGroups.add(groupNames[i]);
                } else if (u <= LOW_STOCK_THRESHOLD) {
                    lowStockCount++;
                    lowGroups.add(groupNames[i]);
                }
            }
            if (bombay == 0) { outOfStockCount++; outGroups.add("Bombay (Oh)"); }
            else if (bombay <= LOW_STOCK_THRESHOLD) { lowStockCount++; lowGroups.add("Bombay (Oh)"); }

            if (txtStatTotal != null) txtStatTotal.setText(String.format(Locale.US, "%d Units", totalUnits));
            if (txtStatAvailable != null) txtStatAvailable.setText(String.format(Locale.US, "%d Units", totalUnits));
            if (txtStatLowStock != null) txtStatLowStock.setText(String.format(Locale.US, "%d Groups", lowStockCount));
            if (txtStatOutOfStock != null) txtStatOutOfStock.setText(String.format(Locale.US, "%d Groups", outOfStockCount));

            if (txtAlertCrit != null) {
                if (!outGroups.isEmpty()) {
                    txtAlertCrit.setText("🚨 Out of stock: " + outGroups.get(0) + " (0 units remaining).");
                    txtAlertCrit.setVisibility(View.VISIBLE);
                } else if (!lowGroups.isEmpty()) {
                    txtAlertCrit.setText("🚨 Low stock alert: " + lowGroups.get(0) + " reserve is low.");
                    txtAlertCrit.setVisibility(View.VISIBLE);
                } else {
                    txtAlertCrit.setText("🟢 All blood reserves are at operational thresholds.");
                    txtAlertCrit.setVisibility(View.VISIBLE);
                }
            }
            if (txtAlertLow != null) {
                if (!lowGroups.isEmpty()) {
                    txtAlertLow.setText("⏳ Low reserve groups: " + TextUtils.join(", ", lowGroups));
                    txtAlertLow.setVisibility(View.VISIBLE);
                } else {
                    txtAlertLow.setText("✔ Low stock threshold set to ≤ " + LOW_STOCK_THRESHOLD + " units.");
                }
            }
            if (txtAlertInfo != null) {
                txtAlertInfo.setText("📋 " + bankName + " connected to live SmartBlood network.");
            }
        };

        Map<String, Integer> defaultStock = defaultStockForBank(bankName);
        for (SmartMapItem item : smartMapItemList) {
            if ("BLOOD_BANK".equalsIgnoreCase(item.type) && 
                (item.id.equalsIgnoreCase(activeBankId) || item.id.equalsIgnoreCase(currentUid) || (item.name != null && item.name.equalsIgnoreCase(bankName)))) {
                defaultStock = item.stockMap;
                break;
            }
        }
        renderStockData.accept(defaultStock);

        if (progress != null) progress.setVisibility(View.VISIBLE);
        try {
            cleanupBankListeners();
            
            // Task 1: Counts
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            
            bankPendingRequestsCountListener = db.collection("bloodRequests")
                .whereIn("status", Arrays.asList("Pending", "Searching", "Allocated", "Accepted"))
                .addSnapshotListener((snaps, e) -> {
                    if (snaps != null) {
                        int count = 0;
                        for (DocumentSnapshot d : snaps.getDocuments()) {
                            String tId = d.getString("targetBankId");
                            String bId = d.getString("bloodBankId");
                            if (activeBankId.equals(tId) || activeBankId.equals(bId)) count++;
                        }
                        if (txtPendingCount != null) txtPendingCount.setText(String.valueOf(count));
                    }
                });

            bankDonorAppointmentsCountListener = db.collection("donorAppointments")
                .addSnapshotListener((snaps, e) -> {
                    if (snaps != null && txtDonorAptCount != null) {
                        int count = 0;
                        for (DocumentSnapshot d : snaps.getDocuments()) {
                            String st = d.getString("status");
                            String bId = d.getString("bloodBankId");
                            String bName = d.getString("bloodBankName");
                            boolean isMatch = (activeBankId != null && activeBankId.equalsIgnoreCase(bId)) ||
                                              (currentUid != null && currentUid.equalsIgnoreCase(bId)) ||
                                              (bankName != null && bName != null && (bName.equalsIgnoreCase(bankName) || bName.toLowerCase(Locale.US).contains(bankName.toLowerCase(Locale.US)) || bankName.toLowerCase(Locale.US).contains(bName.toLowerCase(Locale.US))));
                            if (isMatch && st != null && st.equalsIgnoreCase("PENDING")) {
                                count++;
                            }
                        }
                        txtDonorAptCount.setText(String.valueOf(count));
                    }
                });

            bankEmergencyRequestsCountListener = db.collection("bloodRequests")
                .addSnapshotListener((snaps, e) -> {
                    if (snaps != null && txtEmergencyCount != null) {
                        int count = 0;
                        for (DocumentSnapshot d : snaps.getDocuments()) {
                            String urg = d.getString("urgency");
                            String st = d.getString("status");
                            boolean isEmergency = urg != null && (urg.equalsIgnoreCase("Emergency") || urg.equalsIgnoreCase("Critical") || urg.toUpperCase(Locale.US).contains("EMERG") || urg.toUpperCase(Locale.US).contains("CRITIC"));
                            boolean isStatusActive = st != null && (st.equalsIgnoreCase("Pending") || st.equalsIgnoreCase("Searching") || st.equalsIgnoreCase("ACTIVE"));
                            if (isEmergency && isStatusActive) count++;
                        }
                        txtEmergencyCount.setText(String.valueOf(count));
                    }
                });

            bankTransfersCountListener = db.collection("transfers")
                .addSnapshotListener((snaps, e) -> {
                    if (snaps != null) {
                        int count = 0;
                        for (DocumentSnapshot d : snaps.getDocuments()) {
                            String sId = d.getString("sourceBloodBankId");
                            String dId = d.getString("destinationBloodBankId");
                            String st = d.getString("status");
                            if ((activeBankId.equals(sId) || activeBankId.equals(dId)) && "PENDING_APPROVAL".equals(st)) count++;
                        }
                        if (txtTransfersCount != null) txtTransfersCount.setText(String.valueOf(count));
                    }
                });

            bankUnreadNotifsCountListener = db.collection("notifications")
                .whereEqualTo("userId", activeBankId)
                .whereEqualTo("read", false)
                .addSnapshotListener((snaps, e) -> {
                    if (snaps != null && txtUnreadNotifsCount != null) txtUnreadNotifsCount.setText(String.valueOf(snaps.size()));
                });

            bloodBankDashboardListener = db.collection("bloodBanks")
                .addSnapshotListener((snapshots, error) -> {
                    if (progress != null) progress.setVisibility(View.GONE);

                    if (error != null) {
                        Log.e("SmartBlood", "Error listening to blood bank data: " + error.getMessage());
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        return;
                    }

                    DocumentSnapshot targetDoc = null;
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String docId = doc.getId();
                        String uid = doc.getString("userId");
                        String bId = doc.getString("bloodBankId");
                        String n = doc.getString("name");

                        if (docId.equalsIgnoreCase(activeBankId) || 
                            docId.equalsIgnoreCase(currentUid) ||
                            (bId != null && bId.equalsIgnoreCase(activeBankId)) ||
                            (uid != null && uid.equalsIgnoreCase(currentUid)) ||
                            (n != null && n.trim().equalsIgnoreCase(bankName.trim()))) {
                            targetDoc = doc;
                            break;
                        }
                    }

                    if (targetDoc == null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String n = doc.getString("name");
                            if (n != null && (n.toLowerCase(Locale.US).contains(bankName.toLowerCase(Locale.US)) || bankName.toLowerCase(Locale.US).contains(n.toLowerCase(Locale.US)))) {
                                targetDoc = doc;
                                break;
                            }
                        }
                    }

                    if (targetDoc != null) {
                        Map<String, Integer> liveStock = extractStockMapFromDoc(targetDoc);
                        String liveName = targetDoc.getString("name") != null ? targetDoc.getString("name") : bankName;
                        String liveCity = targetDoc.getString("city") != null ? targetDoc.getString("city") : city;
                        String liveAddr = targetDoc.getString("address") != null ? targetDoc.getString("address") : address;

                        if (txtName != null) txtName.setText(liveName);
                        if (txtSubtitle != null) txtSubtitle.setText("✔ Verified Reserve Dashboard  -  ID: " + targetDoc.getId());
                        if (txtLocation != null) txtLocation.setText("📍 " + liveAddr);

                        for (SmartMapItem item : smartMapItemList) {
                            if ("BLOOD_BANK".equalsIgnoreCase(item.type) && 
                                (item.id.equalsIgnoreCase(targetDoc.getId()) || (item.name != null && item.name.equalsIgnoreCase(liveName)))) {
                                item.stockMap = liveStock;
                                item.name = liveName;
                                item.area = liveCity;
                                break;
                            }
                        }

                        renderStockData.accept(liveStock);
                    }
                });
        } catch (Exception ignored) {}
    }

        private void bindDonorDashboardV2(View view) {
        View glowRed = view.findViewById(R.id.bg_glow_donor_dash_red);
        View waveDonor = view.findViewById(R.id.bg_wave_donor_dash);

        if (glowRed != null) {
            ObjectAnimator pulseRed = ObjectAnimator.ofFloat(glowRed, "alpha", 0.50f, 0.95f, 0.50f);
            pulseRed.setDuration(3800);
            pulseRed.setRepeatCount(ValueAnimator.INFINITE);
            pulseRed.start();
        }
        if (waveDonor != null) {
            ObjectAnimator waveAnim = ObjectAnimator.ofFloat(waveDonor, "translationY", -10f, 10f, -10f);
            waveAnim.setDuration(7600);
            waveAnim.setRepeatCount(ValueAnimator.INFINITE);
            waveAnim.start();
        }

        UserProfile currentUser = repository.getCurrentUser();
        final String currentUid = currentUser != null ? currentUser.getUid() : "USR-DNR-01";
        final String donorName = currentUser != null && currentUser.getName() != null ? currentUser.getName() : "Omkar Jadhav";
        final String bloodGroup = currentUser != null && currentUser.getBloodGroup() != null ? currentUser.getBloodGroup() : "O+";

        TextView txtGreeting = view.findViewById(R.id.txt_donor_dash_greeting);
        TextView txtStatusTag = view.findViewById(R.id.txt_donor_dash_status_tag);
        TextView txtHeroGroup = view.findViewById(R.id.txt_donor_hero_blood_group);

        if (txtGreeting != null) txtGreeting.setText("Welcome Back, " + donorName + " 👋");
        if (txtHeroGroup != null) txtHeroGroup.setText(bloodGroup);

        View btnProfileShortcut = view.findViewById(R.id.btn_donor_dash_profile_shortcut);
        if (btnProfileShortcut != null) {
            btnProfileShortcut.setOnClickListener(v -> loadView(R.layout.view_profile, this::bindProfileView));
        }

        TextView btnToggle = view.findViewById(R.id.btn_donor_toggle_availability);
        if (btnToggle != null) {
            btnToggle.setText(isDonorAvailable ? "🟢 Available" : "🔴 Unavailable");
            btnToggle.setOnClickListener(v -> {
                isDonorAvailable = !isDonorAvailable;
                btnToggle.setText(isDonorAvailable ? "🟢 Available" : "🔴 Unavailable");
                if (txtStatusTag != null) {
                    txtStatusTag.setText(isDonorAvailable ? "Active & Ready" : "Currently Unavailable");
                }
                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put("isAvailable", isDonorAvailable);
                FirebaseFirestore.getInstance().collection("users").document(currentUid).set(updateMap, SetOptions.merge());
                repository.addAuditLog(currentUid, "DONOR", "UPDATE_AVAILABILITY", currentUid, new SimpleDateFormat("hh:mm a", Locale.US).format(new Date()), isDonorAvailable ? "UNAVAILABLE" : "AVAILABLE", isDonorAvailable ? "AVAILABLE" : "UNAVAILABLE", "Donor availability updated.");
                Toast.makeText(this, "Availability status updated to " + (isDonorAvailable ? "Available" : "Unavailable"), Toast.LENGTH_SHORT).show();
            });
        }

        // Live user profile sync for blood group and availability
        try {
            FirebaseFirestore.getInstance().collection("users").document(currentUid)
                    .addSnapshotListener((doc, e) -> {
                        if (doc != null && doc.exists()) {
                            String liveGroup = doc.getString("bloodGroup");
                            if (liveGroup != null && !liveGroup.isEmpty() && txtHeroGroup != null) {
                                txtHeroGroup.setText(liveGroup);
                            }
                            Boolean liveAvail = doc.getBoolean("isAvailable");
                            if (liveAvail != null) {
                                isDonorAvailable = liveAvail;
                                if (btnToggle != null) btnToggle.setText(isDonorAvailable ? "🟢 Available" : "🔴 Unavailable");
                                if (txtStatusTag != null) txtStatusTag.setText(isDonorAvailable ? "Active & Ready" : "Currently Unavailable");
                            }
                        }
                    });
        } catch (Exception ignored) {}

        View cardDonateCTA = view.findViewById(R.id.card_donor_donate_blood_cta);
        if (cardDonateCTA != null) {
            cardDonateCTA.setOnClickListener(v -> showBookDonationDialog());
        }

        View cardUpcoming = view.findViewById(R.id.card_donor_upcoming_appointment);
        TextView txtApptBank = view.findViewById(R.id.txt_upcoming_bank_name);
        TextView txtApptBadge = view.findViewById(R.id.txt_upcoming_status_badge);
        TextView txtApptDateTime = view.findViewById(R.id.txt_upcoming_date_time);
        View btnViewAppt = view.findViewById(R.id.btn_donor_view_appointment);

        final DocumentSnapshot[] activeAppointmentDoc = new DocumentSnapshot[]{null};

        try {
            cleanupDonorListeners();
            donorUpcomingAppointmentListener = FirebaseFirestore.getInstance().collection("donorAppointments")
                    .whereEqualTo("donorUid", currentUid)
                    .addSnapshotListener((snapshots, e) -> {
                        if (snapshots != null && !snapshots.isEmpty()) {
                            DocumentSnapshot latestDoc = null;
                            // Prioritize PENDING or CONFIRMED appointments
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                String st = doc.getString("status");
                                if (st != null && (st.equalsIgnoreCase("PENDING") || st.equalsIgnoreCase("CONFIRMED"))) {
                                    latestDoc = doc;
                                    break;
                                }
                            }
                            if (latestDoc == null) {
                                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                    String st = doc.getString("status");
                                    if (st != null && !st.equalsIgnoreCase("CANCELLED") && !st.equalsIgnoreCase("REJECTED")) {
                                        latestDoc = doc;
                                        break;
                                    }
                                }
                            }

                            if (latestDoc != null) {
                                activeAppointmentDoc[0] = latestDoc;
                                String bName = latestDoc.getString("bloodBankName") != null ? latestDoc.getString("bloodBankName") : "Blood Bank";
                                String aDate = latestDoc.getString("appointmentDate") != null ? latestDoc.getString("appointmentDate") : (latestDoc.getString("date") != null ? latestDoc.getString("date") : "Scheduled");
                                String aTime = latestDoc.getString("appointmentTime") != null ? latestDoc.getString("appointmentTime") : (latestDoc.getString("time") != null ? latestDoc.getString("time") : "10:30 AM");
                                String st = latestDoc.getString("status") != null ? latestDoc.getString("status").toUpperCase(Locale.US) : "PENDING";

                                if (txtApptBank != null) txtApptBank.setText("🏥 " + bName);
                                if (txtApptDateTime != null) txtApptDateTime.setText("📅 " + aDate + "  -  " + aTime);
                                if (txtApptBadge != null) {
                                    txtApptBadge.setText(st);
                                    if (st.contains("COMPLET")) {
                                        txtApptBadge.setBackgroundResource(R.drawable.bg_chip_status_available);
                                        txtApptBadge.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                                    } else if (st.contains("CONFIRM")) {
                                        txtApptBadge.setBackgroundResource(R.drawable.bg_chip_status_reserved);
                                        txtApptBadge.setTextColor(ContextCompat.getColor(this, R.color.status_reserved_text));
                                    } else {
                                        txtApptBadge.setBackgroundResource(R.drawable.bg_chip_status_critical);
                                        txtApptBadge.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                                    }
                                }
                            } else {
                                activeAppointmentDoc[0] = null;
                                if (txtApptBank != null) txtApptBank.setText("No upcoming appointment");
                                if (txtApptDateTime != null) txtApptDateTime.setText("Click Donate Blood to schedule your lifesaving donation.");
                                if (txtApptBadge != null) {
                                    txtApptBadge.setText("READY TO BOOK");
                                    txtApptBadge.setBackgroundResource(R.drawable.bg_chip_status_pending);
                                    txtApptBadge.setTextColor(ContextCompat.getColor(this, R.color.status_low_text));
                                }
                            }
                        } else {
                            activeAppointmentDoc[0] = null;
                            if (txtApptBank != null) txtApptBank.setText("No upcoming appointment");
                            if (txtApptDateTime != null) txtApptDateTime.setText("Click Donate Blood to schedule your lifesaving donation.");
                            if (txtApptBadge != null) {
                                txtApptBadge.setText("READY TO BOOK");
                                txtApptBadge.setBackgroundResource(R.drawable.bg_chip_status_pending);
                                txtApptBadge.setTextColor(ContextCompat.getColor(this, R.color.status_low_text));
                            }
                        }
                    });
        } catch (Exception ignored) {}

        View.OnClickListener apptDetailsClick = v -> {
            if (activeAppointmentDoc[0] != null) {
                showAppointmentDetailsDialog(activeAppointmentDoc[0]);
            } else {
                showBookDonationDialog();
            }
        };

        if (btnViewAppt != null) btnViewAppt.setOnClickListener(apptDetailsClick);
        if (cardUpcoming != null) cardUpcoming.setOnClickListener(apptDetailsClick);

        // My Lifesaving Impact real-time calculations from Firebase
        TextView txtStatDonations = view.findViewById(R.id.txt_donor_stat_total_donations);
        TextView txtStatResponses = view.findViewById(R.id.txt_donor_stat_emergency_responses);
        TextView txtStatHelped = view.findViewById(R.id.txt_donor_stat_requests_helped);
        TextView txtStatRate = view.findViewById(R.id.txt_donor_stat_response_rate);

        try {
            // Count completed donations from appointments
            FirebaseFirestore.getInstance().collection("donorAppointments")
                    .whereEqualTo("donorUid", currentUid)
                    .addSnapshotListener((snaps, e) -> {
                        int completedDonations = 0;
                        if (snaps != null) {
                            for (DocumentSnapshot doc : snaps.getDocuments()) {
                                String st = doc.getString("status");
                                if (st != null && (st.equalsIgnoreCase("COMPLETED") || st.equalsIgnoreCase("DONATION_COMPLETED"))) {
                                    completedDonations++;
                                }
                            }
                        }
                        if (txtStatDonations != null) {
                            txtStatDonations.setText(String.format(Locale.US, "%02d", completedDonations));
                        }
                    });

            // Count emergency responses from donorEmergencyResponses
            FirebaseFirestore.getInstance().collection("donorEmergencyResponses")
                    .whereEqualTo("donorUid", currentUid)
                    .addSnapshotListener((snaps, e) -> {
                        int responseCount = 0;
                        int helpedCount = 0;
                        if (snaps != null) {
                            for (DocumentSnapshot doc : snaps.getDocuments()) {
                                responseCount++;
                                String st = doc.getString("responseStatus");
                                if (st == null) st = doc.getString("status");
                                if (st != null && (st.equalsIgnoreCase("AVAILABLE") || st.equalsIgnoreCase("ACCEPTED") || st.equalsIgnoreCase("FULFILLED") || st.equalsIgnoreCase("COMPLETED") || st.equalsIgnoreCase("SELECTED"))) {
                                    helpedCount++;
                                }
                            }
                        }
                        if (txtStatResponses != null) {
                            txtStatResponses.setText(String.format(Locale.US, "%02d", responseCount));
                        }
                        if (txtStatHelped != null) {
                            txtStatHelped.setText(String.format(Locale.US, "%02d", helpedCount));
                        }
                        if (txtStatRate != null) {
                            int rate = responseCount > 0 ? Math.min(100, (helpedCount * 100) / responseCount) : 100;
                            txtStatRate.setText(rate + "%");
                        }
                    });
        } catch (Exception ignored) {}

        View cardEmergencySection = view.findViewById(R.id.card_donor_emergency_section);
        TextView txtEmgTitle = view.findViewById(R.id.txt_donor_emergency_title);
        TextView txtEmgHosp = view.findViewById(R.id.txt_donor_emergency_hospital);
        TextView txtEmgBadge = view.findViewById(R.id.txt_donor_emergency_status_badge);
        TextView txtEmgDist = view.findViewById(R.id.txt_donor_emergency_distance);
        View btnRespond = view.findViewById(R.id.btn_donor_respond_emergency);
        View btnDecline = view.findViewById(R.id.btn_donor_decline_emergency);

        try {
            donorEmergencyListener = FirebaseFirestore.getInstance().collection("bloodRequests")
                    .addSnapshotListener((snapshots, e) -> {
                        if (snapshots != null && !snapshots.isEmpty()) {
                            DocumentSnapshot emgDoc = null;
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                String urg = doc.getString("urgency");
                                String st = doc.getString("status");
                                boolean isEmergency = urg != null && (urg.equalsIgnoreCase("Emergency") || urg.equalsIgnoreCase("Critical") || urg.toUpperCase(Locale.US).contains("EMERG") || urg.toUpperCase(Locale.US).contains("CRITIC"));
                                boolean isStatusActive = st != null && (st.equalsIgnoreCase("Pending") || st.equalsIgnoreCase("Searching") || st.equalsIgnoreCase("ACTIVE"));
                                if (isEmergency && isStatusActive && isDonorEligibleForEmergency(bloodGroup, doc.getString("bloodGroup"))) {
                                    emgDoc = doc;
                                    break;
                                }
                            }

                            if (emgDoc != null) {
                                final DocumentSnapshot activeEmg = emgDoc;
                                final String emgHosp = activeEmg.getString("hospitalName") != null ? activeEmg.getString("hospitalName") : "Regional Trauma Center";
                                final String emgGroup = activeEmg.getString("bloodGroup") != null ? activeEmg.getString("bloodGroup") : "O+";
                                Long emgUnits = activeEmg.getLong("quantity");
                                if (emgUnits == null) emgUnits = activeEmg.getLong("requiredUnits");
                                final int finalEmgUnits = emgUnits != null ? emgUnits.intValue() : 2;
                                final String reqId = activeEmg.getString("requestId") != null ? activeEmg.getString("requestId") : activeEmg.getId();
                                double hLat = activeEmg.getDouble("latitude") != null ? activeEmg.getDouble("latitude") : (activeEmg.getDouble("hospitalLat") != null ? activeEmg.getDouble("hospitalLat") : 16.8524);
                                double hLng = activeEmg.getDouble("longitude") != null ? activeEmg.getDouble("longitude") : (activeEmg.getDouble("hospitalLng") != null ? activeEmg.getDouble("hospitalLng") : 74.5815);
                                double distKm = calculateDistanceInKm(userLat, userLng, hLat, hLng);

                                if (cardEmergencySection != null) cardEmergencySection.setVisibility(View.VISIBLE);
                                if (txtEmgTitle != null) txtEmgTitle.setText(emgGroup + " Blood Urgent Request");
                                if (txtEmgHosp != null) txtEmgHosp.setText(emgHosp + "  -  " + finalEmgUnits + " Units Required");
                                if (txtEmgDist != null) txtEmgDist.setText(String.format(Locale.US, "%.1f km away", distKm));

                                String respDocId = "RESP-" + reqId + "-" + currentUid;
                                FirebaseFirestore.getInstance().collection("donorEmergencyResponses").document(respDocId).get()
                                    .addOnSuccessListener(respDoc -> {
                                        String respSt = respDoc != null && respDoc.exists() ? respDoc.getString("responseStatus") : "";
                                        if ("AVAILABLE".equalsIgnoreCase(respSt)) {
                                            if (txtEmgBadge != null) {
                                                txtEmgBadge.setVisibility(View.VISIBLE);
                                                txtEmgBadge.setText("✓ RESPONDED: AVAILABLE");
                                                txtEmgBadge.setBackgroundResource(R.drawable.bg_chip_status_available);
                                                txtEmgBadge.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                                            }
                                            if (btnRespond != null) btnRespond.setEnabled(false);
                                        } else if ("NOT_AVAILABLE".equalsIgnoreCase(respSt)) {
                                            if (cardEmergencySection != null) cardEmergencySection.setVisibility(View.GONE);
                                        } else {
                                            if (txtEmgBadge != null) txtEmgBadge.setVisibility(View.GONE);
                                            if (btnRespond != null) btnRespond.setEnabled(true);
                                            if (btnDecline != null) btnDecline.setEnabled(true);
                                        }
                                    });

                                if (btnRespond != null) {
                                    btnRespond.setOnClickListener(v -> {
                                        final String timeStr = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());
                                        Map<String, Object> respMap = new HashMap<>();
                                        respMap.put("responseId", respDocId);
                                        respMap.put("requestId", reqId);
                                        respMap.put("hospitalId", activeEmg.getString("hospitalId"));
                                        respMap.put("hospitalName", emgHosp);
                                        respMap.put("donorId", currentUid);
                                        respMap.put("donorAuthUid", currentUid);
                                        respMap.put("donorName", donorName);
                                        respMap.put("donorPhone", currentUser != null ? currentUser.getMobileNumber() : "");
                                        respMap.put("donorBloodGroup", bloodGroup);
                                        respMap.put("donorDistanceKm", distKm);
                                        respMap.put("responseStatus", "AVAILABLE");
                                        respMap.put("status", "AVAILABLE");
                                        respMap.put("createdAt", new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(new Date()));
                                        respMap.put("timestamp", System.currentTimeMillis());

                                        FirebaseFirestore.getInstance().collection("donorEmergencyResponses").document(respDocId).set(respMap);

                                        Map<String, Object> reqUpdates = new HashMap<>();
                                        reqUpdates.put("matchedDonorId", currentUid);
                                        reqUpdates.put("donorName", donorName);
                                        reqUpdates.put("donorBloodGroup", bloodGroup);
                                        reqUpdates.put("donorStatus", "AVAILABLE");
                                        reqUpdates.put("donorDistanceKm", distKm);
                                        reqUpdates.put("donorResponseTime", System.currentTimeMillis());
                                        FirebaseFirestore.getInstance().collection("bloodRequests").document(reqId).update(reqUpdates);

                                        String targetHospId = activeEmg.getString("hospitalId");
                                        if (targetHospId != null) {
                                            String notifId = "NOTIF-HOS-RESP-" + System.currentTimeMillis();
                                            Map<String, Object> notif = new HashMap<>();
                                            notif.put("notificationId", notifId);
                                            notif.put("userId", targetHospId);
                                            notif.put("hospitalId", targetHospId);
                                            notif.put("recipientFacilityId", targetHospId);
                                            notif.put("targetRole", "HOSPITAL");
                                            notif.put("title", "🚨 Emergency Donor Available!");
                                            notif.put("message", "Donor " + donorName + " (" + bloodGroup + ", " + String.format(Locale.US, "%.1f km away", distKm) + ") is Available and ready to donate.");
                                            notif.put("type", "EMERGENCY_DONOR_ACCEPTED");
                                            notif.put("requestId", reqId);
                                            notif.put("relatedId", reqId);
                                            notif.put("timestamp", System.currentTimeMillis());
                                            notif.put("createdAt", System.currentTimeMillis());
                                            notif.put("isRead", false);
                                            notif.put("read", false);
                                            FirebaseFirestore.getInstance().collection("notifications").document(notifId).set(notif);
                                        }

                                        repository.addAuditLog(currentUid, "DONOR", "ACCEPT_EMERGENCY", reqId, timeStr, "SEARCHING", "RESPONDED", "Donor accepted emergency request.");
                                        Toast.makeText(this, "✔ Response submitted! Hospital notified.", Toast.LENGTH_SHORT).show();
                                        loadView(R.layout.view_emergency_center, this::bindEmergencyCenterView);
                                    });
                                }

                                if (btnDecline != null) {
                                    btnDecline.setOnClickListener(v -> {
                                        Map<String, Object> respMap = new HashMap<>();
                                        respMap.put("responseId", respDocId);
                                        respMap.put("requestId", reqId);
                                        respMap.put("hospitalId", activeEmg.getString("hospitalId"));
                                        respMap.put("hospitalName", emgHosp);
                                        respMap.put("donorId", currentUid);
                                        respMap.put("donorAuthUid", currentUid);
                                        respMap.put("donorName", donorName);
                                        respMap.put("donorBloodGroup", bloodGroup);
                                        respMap.put("responseStatus", "NOT_AVAILABLE");
                                        respMap.put("status", "NOT_AVAILABLE");
                                        respMap.put("timestamp", System.currentTimeMillis());

                                        FirebaseFirestore.getInstance().collection("donorEmergencyResponses").document(respDocId).set(respMap);
                                        if (cardEmergencySection != null) cardEmergencySection.setVisibility(View.GONE);
                                        Toast.makeText(this, "Marked as Not Available.", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } else {
                                if (cardEmergencySection != null) cardEmergencySection.setVisibility(View.GONE);
                            }
                        } else {
                            if (cardEmergencySection != null) cardEmergencySection.setVisibility(View.GONE);
                        }
                    });
        } catch (Exception ignored) {}
    }

    private void bindAdminDashboardV2(View view) {
        View btnAi = view.findViewById(R.id.btn_launch_ai_assistant);
        if (btnAi != null) btnAi.setOnClickListener(v -> showAIAssistantDialog());

        View btnAuditLogs = view.findViewById(R.id.btn_view_audit_logs);
        if (btnAuditLogs != null) btnAuditLogs.setOnClickListener(v -> loadView(R.layout.view_audit_logs, this::bindAuditLogsView));

        View btnReviewTransfer = view.findViewById(R.id.btn_review_transfer_suggestion);
        if (btnReviewTransfer != null) btnReviewTransfer.setOnClickListener(v -> showStockTransferDialog());
    }

    private void bindEmergencyCenterView(View view) {
        attachRoleBackgroundAnimators(view);

        UserProfile currentUser = repository.getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : "USR-DNR-01";
        String donorName = currentUser != null && currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Donor";
        String donorGroup = currentUser != null ? currentUser.getBloodGroup() : "O+";

        TextView txtCount = view.findViewById(R.id.txt_emergency_result_count);
        ProgressBar progress = view.findViewById(R.id.loading_emergency_progress);
        LinearLayout container = view.findViewById(R.id.layout_emergency_requests_container);
        View emptyState = view.findViewById(R.id.layout_emergency_empty_state);

        TextView chipAll = view.findViewById(R.id.chip_emergency_radius_all);
        TextView chip1 = view.findViewById(R.id.chip_emergency_radius_1km);
        TextView chip5 = view.findViewById(R.id.chip_emergency_radius_5km);
        TextView chip10 = view.findViewById(R.id.chip_emergency_radius_10km);
        TextView chip15 = view.findViewById(R.id.chip_emergency_radius_15km);
        TextView chip20 = view.findViewById(R.id.chip_emergency_radius_20km);
        TextView chip30 = view.findViewById(R.id.chip_emergency_radius_30km);
        TextView chip50 = view.findViewById(R.id.chip_emergency_radius_50km);
        TextView[] allChips = new TextView[]{chipAll, chip1, chip5, chip10, chip15, chip20, chip30, chip50};
        double[] radiusValues = new double[]{50.0, 1.0, 5.0, 10.0, 15.0, 20.0, 30.0, 50.0};

        if (progress != null) progress.setVisibility(View.VISIBLE);

        final List<DocumentSnapshot> emergencyDocs = new ArrayList<>();

        Runnable renderEmergencies = () -> {
            if (container == null) return;
            container.removeAllViews();

            List<DocumentSnapshot> filteredList = new ArrayList<>();
            for (DocumentSnapshot doc : emergencyDocs) {
                double hLat = doc.getDouble("latitude") != null ? doc.getDouble("latitude") : (doc.getDouble("hospitalLat") != null ? doc.getDouble("hospitalLat") : 16.8524);
                double hLng = doc.getDouble("longitude") != null ? doc.getDouble("longitude") : (doc.getDouble("hospitalLng") != null ? doc.getDouble("hospitalLng") : 74.5815);
                double distKm = calculateDistanceInKm(userLat, userLng, hLat, hLng);

                if (distKm <= donorEmergencyFilterRadius || donorEmergencyFilterRadius >= 50.0) {
                    filteredList.add(doc);
                }
            }

            if (txtCount != null) {
                txtCount.setText(filteredList.size() + " Active Hospital Emergencies within " + (donorEmergencyFilterRadius >= 50.0 ? "50 KM" : String.format(Locale.US, "%.0f KM", donorEmergencyFilterRadius)));
            }

            if (filteredList.isEmpty()) {
                if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                return;
            }

            if (emptyState != null) emptyState.setVisibility(View.GONE);

            for (DocumentSnapshot doc : filteredList) {
                String reqId = doc.getString("requestId") != null ? doc.getString("requestId") : doc.getId();
                String hName = doc.getString("hospitalName") != null ? doc.getString("hospitalName") : "Regional Trauma Center";
                String bGrp = doc.getString("bloodGroup") != null ? doc.getString("bloodGroup") : "O+";
                Long qUnits = doc.getLong("quantity");
                int qty = qUnits != null ? qUnits.intValue() : 2;
                String comp = doc.getString("component") != null ? doc.getString("component") : "Whole Blood";
                String address = doc.getString("locationAddress") != null ? doc.getString("locationAddress") : "Sangli, Maharashtra";
                double hLat = doc.getDouble("latitude") != null ? doc.getDouble("latitude") : (doc.getDouble("hospitalLat") != null ? doc.getDouble("hospitalLat") : 16.8524);
                double hLng = doc.getDouble("longitude") != null ? doc.getDouble("longitude") : (doc.getDouble("hospitalLng") != null ? doc.getDouble("hospitalLng") : 74.5815);
                double distKm = calculateDistanceInKm(userLat, userLng, hLat, hLng);

                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackgroundResource(R.drawable.bg_card_emergency);
                card.setPadding(24, 20, 24, 20);
                LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                cLp.bottomMargin = 14;
                card.setLayoutParams(cLp);

                RelativeLayout topRow = new RelativeLayout(this);
                topRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                TextView txtTitle = new TextView(this);
                txtTitle.setText("🚨 " + bGrp + " EMERGENCY REQUIRED");
                txtTitle.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                txtTitle.setTextSize(15f);
                txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);

                TextView txtDist = new TextView(this);
                txtDist.setText(String.format(Locale.US, "%.1f KM away", distKm));
                txtDist.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                txtDist.setTextSize(11f);
                txtDist.setTypeface(null, android.graphics.Typeface.BOLD);
                txtDist.setBackgroundResource(R.drawable.bg_chip_status_critical);
                txtDist.setPadding(14, 6, 14, 6);
                RelativeLayout.LayoutParams dLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dLp.addRule(RelativeLayout.ALIGN_PARENT_END);
                dLp.addRule(RelativeLayout.CENTER_VERTICAL);
                txtDist.setLayoutParams(dLp);

                topRow.addView(txtTitle);
                topRow.addView(txtDist);
                card.addView(topRow);

                TextView txtHosp = new TextView(this);
                txtHosp.setText("🏥 " + hName + "  -  " + qty + " Units of " + comp);
                txtHosp.setTextColor(ContextCompat.getColor(this, R.color.white));
                txtHosp.setTextSize(14f);
                txtHosp.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                hLp.topMargin = 6;
                txtHosp.setLayoutParams(hLp);
                card.addView(txtHosp);

                TextView txtLoc = new TextView(this);
                txtLoc.setText("📍 " + address);
                txtLoc.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                txtLoc.setTextSize(12f);
                LinearLayout.LayoutParams locLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                locLp.topMargin = 4;
                txtLoc.setLayoutParams(locLp);
                card.addView(txtLoc);

                TextView txtRespBadge = new TextView(this);
                txtRespBadge.setTextSize(11f);
                txtRespBadge.setTypeface(null, android.graphics.Typeface.BOLD);
                txtRespBadge.setPadding(14, 6, 14, 6);
                txtRespBadge.setVisibility(View.GONE);
                LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                bLp.topMargin = 8;
                txtRespBadge.setLayoutParams(bLp);
                card.addView(txtRespBadge);

                // Action Buttons Row
                LinearLayout actRow = new LinearLayout(this);
                actRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                aLp.topMargin = 12;
                actRow.setLayoutParams(aLp);

                int btnHeight = (int) (44 * getResources().getDisplayMetrics().density);

                TextView btnDecline = new TextView(this);
                btnDecline.setText("✕ Not Available");
                btnDecline.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                btnDecline.setTextSize(12f);
                btnDecline.setTypeface(null, android.graphics.Typeface.BOLD);
                btnDecline.setGravity(android.view.Gravity.CENTER);
                btnDecline.setBackgroundResource(R.drawable.bg_search_bar);
                btnDecline.setClickable(true);
                btnDecline.setFocusable(true);
                btnDecline.setPadding(16, 12, 16, 12);
                LinearLayout.LayoutParams decLp = new LinearLayout.LayoutParams(0, btnHeight, 1f);
                decLp.rightMargin = 8;
                btnDecline.setLayoutParams(decLp);

                TextView btnAvailable = new TextView(this);
                btnAvailable.setText("🩸 I'm Available");
                btnAvailable.setTextColor(ContextCompat.getColor(this, R.color.white));
                btnAvailable.setTextSize(12f);
                btnAvailable.setTypeface(null, android.graphics.Typeface.BOLD);
                btnAvailable.setGravity(android.view.Gravity.CENTER);
                btnAvailable.setBackgroundResource(R.drawable.bg_button_donor);
                btnAvailable.setClickable(true);
                btnAvailable.setFocusable(true);
                btnAvailable.setPadding(16, 12, 16, 12);
                LinearLayout.LayoutParams avLp = new LinearLayout.LayoutParams(0, btnHeight, 1f);
                avLp.leftMargin = 8;
                btnAvailable.setLayoutParams(avLp);

                actRow.addView(btnDecline);
                actRow.addView(btnAvailable);
                card.addView(actRow);

                String respDocId = "RESP-" + reqId + "-" + currentUid;
                FirebaseFirestore.getInstance().collection("donorEmergencyResponses").document(respDocId).get()
                    .addOnSuccessListener(respDoc -> {
                        String st = respDoc != null && respDoc.exists() ? respDoc.getString("responseStatus") : "NOT_RESPONDED";
                        if ("AVAILABLE".equalsIgnoreCase(st)) {
                            txtRespBadge.setVisibility(View.VISIBLE);
                            txtRespBadge.setText("✓ YOU RESPONDED: AVAILABLE");
                            txtRespBadge.setBackgroundResource(R.drawable.bg_chip_status_available);
                            txtRespBadge.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                            btnAvailable.setEnabled(false);
                        } else if ("NOT_AVAILABLE".equalsIgnoreCase(st)) {
                            txtRespBadge.setVisibility(View.VISIBLE);
                            txtRespBadge.setText("✕ YOU RESPONDED: NOT AVAILABLE");
                            txtRespBadge.setBackgroundResource(R.drawable.bg_chip_status_critical);
                            txtRespBadge.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                            btnDecline.setEnabled(false);
                        } else if ("SELECTED".equalsIgnoreCase(st)) {
                            txtRespBadge.setVisibility(View.VISIBLE);
                            txtRespBadge.setText("⭐ SELECTED FOR EMERGENCY TRANSFUSION");
                            txtRespBadge.setBackgroundResource(R.drawable.bg_chip_status_verified);
                            txtRespBadge.setTextColor(ContextCompat.getColor(this, R.color.status_verified_text));
                        } else if ("COMPLETED".equalsIgnoreCase(st)) {
                            txtRespBadge.setVisibility(View.VISIBLE);
                            txtRespBadge.setText("✓ EMERGENCY DONATION COMPLETED");
                            txtRespBadge.setBackgroundResource(R.drawable.bg_chip_status_available);
                            txtRespBadge.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                        }
                    });

                btnAvailable.setOnClickListener(v -> {
                    Log.d("SMARTBLOOD_CLICK", "Emergency I'm Available clicked for requestId: " + reqId);
                    Map<String, Object> respMap = new HashMap<>();
                    respMap.put("responseId", respDocId);
                    respMap.put("requestId", reqId);
                    respMap.put("hospitalId", doc.getString("hospitalId"));
                    respMap.put("hospitalName", hName);
                    respMap.put("donorId", currentUid);
                    respMap.put("donorAuthUid", currentUid);
                    respMap.put("donorName", donorName);
                    respMap.put("donorPhone", currentUser != null ? currentUser.getMobileNumber() : "");
                    respMap.put("donorBloodGroup", donorGroup);
                    respMap.put("donorDistanceKm", distKm);
                    respMap.put("responseStatus", "AVAILABLE");
                    respMap.put("createdAt", new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(new Date()));
                    respMap.put("timestamp", System.currentTimeMillis());

                    FirebaseFirestore.getInstance().collection("donorEmergencyResponses").document(respDocId).set(respMap);

                    Map<String, Object> reqUpdates = new HashMap<>();
                    reqUpdates.put("matchedDonorId", currentUid);
                    reqUpdates.put("donorName", donorName);
                    reqUpdates.put("donorBloodGroup", donorGroup);
                    reqUpdates.put("donorStatus", "AVAILABLE");
                    reqUpdates.put("donorDistanceKm", distKm);
                    reqUpdates.put("donorResponseTime", System.currentTimeMillis());
                    FirebaseFirestore.getInstance().collection("bloodRequests").document(reqId).update(reqUpdates);

                    String notifId = "NOTIF-HOS-RESP-" + System.currentTimeMillis();
                    Map<String, Object> hNotif = new HashMap<>();
                    hNotif.put("notificationId", notifId);
                    hNotif.put("hospitalId", doc.getString("hospitalId"));
                    hNotif.put("recipientFacilityId", doc.getString("hospitalId"));
                    hNotif.put("targetRole", "HOSPITAL");
                    hNotif.put("title", "Emergency Donor Responded");
                    hNotif.put("message", "Donor " + donorName + " (" + donorGroup + ", " + String.format(Locale.US, "%.1f km away", distKm) + ") is Available and ready to donate.");
                    hNotif.put("type", "DONOR_RESPONSE");
                    hNotif.put("requestId", reqId);
                    hNotif.put("createdAt", new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(new Date()));
                    hNotif.put("timestamp", System.currentTimeMillis());
                    hNotif.put("isRead", false);
                    hNotif.put("read", false);
                    FirebaseFirestore.getInstance().collection("notifications").document(notifId).set(hNotif);

                    repository.addAuditLog(currentUid, "DONOR", "ACCEPT_EMERGENCY", reqId, "10:22 AM", "SEARCHING", "RESPONDED", "Donor " + donorName + " accepted emergency request from " + hName);
                    Toast.makeText(this, "Response sent to " + hName + "! Blood Bank will coordinate your donation.", Toast.LENGTH_LONG).show();

                    txtRespBadge.setVisibility(View.VISIBLE);
                    txtRespBadge.setText("✓ YOU RESPONDED: AVAILABLE");
                    txtRespBadge.setBackgroundResource(R.drawable.bg_chip_status_available);
                    txtRespBadge.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                    btnAvailable.setEnabled(false);
                });

                btnDecline.setOnClickListener(v -> {
                    Log.d("SMARTBLOOD_CLICK", "Emergency Not Available clicked for requestId: " + reqId);
                    Map<String, Object> respMap = new HashMap<>();
                    respMap.put("responseId", respDocId);
                    respMap.put("requestId", reqId);
                    respMap.put("hospitalId", doc.getString("hospitalId"));
                    respMap.put("hospitalName", hName);
                    respMap.put("donorId", currentUid);
                    respMap.put("donorAuthUid", currentUid);
                    respMap.put("donorName", donorName);
                    respMap.put("donorBloodGroup", donorGroup);
                    respMap.put("responseStatus", "NOT_AVAILABLE");
                    respMap.put("timestamp", System.currentTimeMillis());

                    FirebaseFirestore.getInstance().collection("donorEmergencyResponses").document(respDocId).set(respMap);
                    Toast.makeText(this, "Marked as Not Available for this emergency.", Toast.LENGTH_SHORT).show();

                    txtRespBadge.setVisibility(View.VISIBLE);
                    txtRespBadge.setText("✕ YOU RESPONDED: NOT AVAILABLE");
                    txtRespBadge.setBackgroundResource(R.drawable.bg_chip_status_critical);
                    txtRespBadge.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                    btnDecline.setEnabled(false);
                });

                container.addView(card);
            }
        };

        for (int i = 0; i < allChips.length; i++) {
            final int index = i;
            if (allChips[i] != null) {
                allChips[i].setClickable(true);
                allChips[i].setFocusable(true);
                allChips[i].setOnClickListener(v -> {
                    Log.d("SMARTBLOOD_CLICK", "Emergency radius chip clicked: " + radiusValues[index]);
                    donorEmergencyFilterRadius = radiusValues[index];
                    for (int j = 0; j < allChips.length; j++) {
                        if (allChips[j] != null) {
                            if (j == index) {
                                allChips[j].setBackgroundResource(R.drawable.bg_button_primary);
                                allChips[j].setTextColor(ContextCompat.getColor(this, R.color.white));
                            } else {
                                allChips[j].setBackgroundResource(R.drawable.bg_chip_status_pending);
                                allChips[j].setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                            }
                        }
                    }
                    renderEmergencies.run();
                });
            }
        }

        if (donorEmergencyDispatchListener != null) {
            donorEmergencyDispatchListener.remove();
            donorEmergencyDispatchListener = null;
        }

        donorEmergencyDispatchListener = FirebaseFirestore.getInstance().collection("bloodRequests")
            .addSnapshotListener((snaps, err) -> {
                if (progress != null) progress.setVisibility(View.GONE);
                emergencyDocs.clear();
                if (snaps != null && !snaps.isEmpty()) {
                    for (DocumentSnapshot d : snaps.getDocuments()) {
                        String urg = d.getString("urgency");
                        boolean isEmergency = urg != null && (urg.equalsIgnoreCase("Emergency") || urg.equalsIgnoreCase("Critical") || urg.toUpperCase(Locale.US).contains("EMERG") || urg.toUpperCase(Locale.US).contains("CRITIC"));
                        String st = d.getString("status");
                        boolean isStatusActive = st != null && !st.equalsIgnoreCase("Fulfilled") && !st.equalsIgnoreCase("Cancelled") && !st.equalsIgnoreCase("REJECTED") && !st.equalsIgnoreCase("Completed");
                        if (isEmergency && isStatusActive && isDonorEligibleForEmergency(donorGroup, d.getString("bloodGroup"))) {
                            emergencyDocs.add(d);
                        }
                    }
                }
                renderEmergencies.run();
            });
    }

    private List<BloodCamp> getMasterCampList() {
        List<BloodCamp> list = new ArrayList<>();
        list.add(new BloodCamp(
            "CAMP-2026-001",
            "Sangli Mega Blood Donation Drive",
            "Rotary Club of Sangli",
            "BB-001",
            "MSI Blood Bank Sangli",
            "Sangli",
            "Rotary Community Hall, Shivaji Nagar, Sangli",
            16.8550, 74.5820,
            "26 Aug 2026",
            "09:00 AM", "04:00 PM",
            "+91 233 2374501",
            "Community voluntary blood collection drive in collaboration with MSI Blood Bank. Certificates, donor recognition, and health check-ups provided.",
            35, 50,
            "ACTIVE",
            "25 Aug 2026"
        ));
        list.add(new BloodCamp(
            "CAMP-2026-002",
            "Miraj Lifesaver Blood Drive",
            "Lions Club Miraj",
            "BB-002",
            "Bombay Blood Bank Miraj",
            "Miraj",
            "Lions Seva Kendra, Station Road, Miraj",
            16.8430, 74.6420,
            "27 Aug 2026",
            "10:00 AM", "05:00 PM",
            "+91 233 2223400",
            "Emergency reserve replenishment drive supporting regional trauma centers.",
            25, 40,
            "ACTIVE",
            "25 Aug 2026"
        ));
        list.add(new BloodCamp(
            "CAMP-2026-003",
            "Vishrambag Youth Voluntary Camp",
            "Sangli Youth Association",
            "BB-003",
            "Shashwat Blood Bank",
            "Vishrambag",
            "Vishrambag Town Hall, 100 Feet Road, Sangli",
            16.8480, 74.6010,
            "28 Aug 2026",
            "09:30 AM", "03:30 PM",
            "+91 233 2671200",
            "Youth-led voluntary blood donation drive organized with Shashwat Blood Bank.",
            40, 60,
            "ACTIVE",
            "25 Aug 2026"
        ));
        list.add(new BloodCamp(
            "CAMP-2026-004",
            "Civil Hospital Emergency Reserve Drive",
            "Indian Red Cross Society",
            "BB-004",
            "Sangli Civil Hospital Blood Bank",
            "Sangli",
            "Civil Hospital Auditorium, Civil Hospital Road, Sangli",
            16.8524, 74.5815,
            "29 Aug 2026",
            "08:30 AM", "02:30 PM",
            "+91 233 2374000",
            "Government medical center blood collection camp to supply emergency and maternal wards.",
            50, 75,
            "ACTIVE",
            "25 Aug 2026"
        ));
        return list;
    }

    private final List<BloodCamp> masterCampList = new ArrayList<>();
    private double donorCampsFilterRadius = 50.0;
    private double donorEmergencyFilterRadius = 50.0;
    private int activeCampsTab = 0; // 0: Active Camps, 1: My Registrations

    private void ensureBloodCampsInFirestore() {
        if (masterCampList.isEmpty()) {
            masterCampList.addAll(getMasterCampList());
        }

        FirebaseFirestore.getInstance().collection("bloodCamps").get()
            .addOnSuccessListener(snapshots -> {
                if (snapshots == null || snapshots.isEmpty()) {
                    List<BloodCamp> initialCamps = getMasterCampList();
                    for (BloodCamp c : initialCamps) {
                        Map<String, Object> cMap = new HashMap<>();
                        cMap.put("campId", c.getCampId());
                        cMap.put("campName", c.getCampName());
                        cMap.put("organizer", c.getOrganizer());
                        cMap.put("bloodBankId", c.getBloodBankId());
                        cMap.put("bloodBankName", c.getBloodBankName());
                        cMap.put("location", c.getLocation());
                        cMap.put("address", c.getAddress());
                        cMap.put("latitude", c.getLatitude());
                        cMap.put("longitude", c.getLongitude());
                        cMap.put("date", c.getDate());
                        cMap.put("startTime", c.getStartTime());
                        cMap.put("endTime", c.getEndTime());
                        cMap.put("contact", c.getContact());
                        cMap.put("description", c.getDescription());
                        cMap.put("availableSlots", c.getAvailableSlots());
                        cMap.put("totalSlots", c.getTotalSlots());
                        cMap.put("status", c.getStatus());
                        cMap.put("createdAt", c.getCreatedAt());
                        cMap.put("createdAtTimestamp", c.getCreatedAtTimestamp());

                        FirebaseFirestore.getInstance().collection("bloodCamps").document(c.getCampId()).set(cMap);
                    }
                }
            });
    }

    private void bindCampsView(View view) {
        attachRoleBackgroundAnimators(view);
        ensureBloodCampsInFirestore();

        UserProfile currentUser = repository.getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : "USR-DNR-01";

        TextView tabActive = view.findViewById(R.id.tab_camps_active);
        TextView tabMyRegistrations = view.findViewById(R.id.tab_camps_my_registrations);
        View searchLayout = view.findViewById(R.id.layout_camps_search);
        EditText inputSearch = view.findViewById(R.id.input_search_camps);
        View radiusScroll = view.findViewById(R.id.scroll_camps_radius_chips);
        TextView txtResultCount = view.findViewById(R.id.txt_camps_result_count);
        ProgressBar progress = view.findViewById(R.id.loading_camps_progress);
        LinearLayout container = view.findViewById(R.id.layout_camps_container);
        View emptyState = view.findViewById(R.id.layout_camps_empty_state);
        TextView txtEmptyTitle = view.findViewById(R.id.txt_empty_camps_title);
        TextView txtEmptyDesc = view.findViewById(R.id.txt_empty_camps_desc);
        TextView btnEmptyAction = view.findViewById(R.id.btn_empty_expand_radius);
        View btnMapView = view.findViewById(R.id.btn_camps_map_view);

        if (btnMapView != null) {
            btnMapView.setOnClickListener(v -> loadView(R.layout.view_map, this::bindMapView));
        }

        TextView chipAll = view.findViewById(R.id.chip_camp_radius_all);
        TextView chip1 = view.findViewById(R.id.chip_camp_radius_1km);
        TextView chip5 = view.findViewById(R.id.chip_camp_radius_5km);
        TextView chip10 = view.findViewById(R.id.chip_camp_radius_10km);
        TextView chip15 = view.findViewById(R.id.chip_camp_radius_15km);
        TextView chip20 = view.findViewById(R.id.chip_camp_radius_20km);
        TextView chip30 = view.findViewById(R.id.chip_camp_radius_30km);
        TextView chip50 = view.findViewById(R.id.chip_camp_radius_50km);
        TextView[] allChips = new TextView[]{chipAll, chip1, chip5, chip10, chip15, chip20, chip30, chip50};
        double[] radiusValues = new double[]{50.0, 1.0, 5.0, 10.0, 15.0, 20.0, 30.0, 50.0};

        final List<BloodCamp> liveCamps = new ArrayList<>();
        final List<CampRegistration> myRegistrations = new ArrayList<>();

        Runnable renderCamps = () -> {
            if (container == null) return;
            container.removeAllViews();

            String q = inputSearch != null ? inputSearch.getText().toString().toLowerCase(Locale.US).trim() : "";

            if (activeCampsTab == 0) {
                // Active Camps Tab
                if (searchLayout != null) searchLayout.setVisibility(View.VISIBLE);
                if (radiusScroll != null) radiusScroll.setVisibility(View.VISIBLE);

                List<BloodCamp> filtered = new ArrayList<>();
                for (BloodCamp c : liveCamps) {
                    double dist = calculateDistanceInKm(userLat, userLng, c.getLatitude(), c.getLongitude());
                    c.distanceKm = dist;

                    if (dist <= donorCampsFilterRadius || donorCampsFilterRadius >= 50.0) {
                        if (!q.isEmpty()) {
                            boolean matchName = c.getCampName() != null && c.getCampName().toLowerCase(Locale.US).contains(q);
                            boolean matchOrg = c.getOrganizer() != null && c.getOrganizer().toLowerCase(Locale.US).contains(q);
                            boolean matchLoc = c.getLocation() != null && c.getLocation().toLowerCase(Locale.US).contains(q);
                            boolean matchBank = c.getBloodBankName() != null && c.getBloodBankName().toLowerCase(Locale.US).contains(q);
                            if (!matchName && !matchOrg && !matchLoc && !matchBank) continue;
                        }
                        filtered.add(c);
                    }
                }

                Collections.sort(filtered, (a, b) -> Double.compare(a.distanceKm, b.distanceKm));

                if (txtResultCount != null) {
                    txtResultCount.setText(filtered.size() + " Blood Donation Camps within " + (donorCampsFilterRadius >= 50.0 ? "50 KM" : String.format(Locale.US, "%.0f KM", donorCampsFilterRadius)));
                }

                if (filtered.isEmpty()) {
                    if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                    if (txtEmptyTitle != null) txtEmptyTitle.setText("No Donation Camps in this Radius");
                    if (txtEmptyDesc != null) txtEmptyDesc.setText("Try expanding your distance filter to 30 KM or 50 KM to discover regional blood drives.");
                    if (btnEmptyAction != null) {
                        btnEmptyAction.setText("Show All Camps (50 KM)");
                        btnEmptyAction.setOnClickListener(v -> {
                            donorCampsFilterRadius = 50.0;
                            if (chipAll != null) chipAll.performClick();
                        });
                    }
                    return;
                }

                if (emptyState != null) emptyState.setVisibility(View.GONE);

                for (BloodCamp camp : filtered) {
                    LinearLayout card = new LinearLayout(this);
                    card.setOrientation(LinearLayout.VERTICAL);
                    card.setBackgroundResource(R.drawable.bg_card_donor);
                    card.setPadding(26, 20, 26, 20);
                    LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    cLp.bottomMargin = 14;
                    card.setLayoutParams(cLp);

                    // Top Row: Camp Name & Distance Badge
                    RelativeLayout topRow = new RelativeLayout(this);
                    topRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    TextView txtName = new TextView(this);
                    txtName.setText("🩸 " + camp.getCampName());
                    txtName.setTextColor(ContextCompat.getColor(this, R.color.white));
                    txtName.setTextSize(16f);
                    txtName.setTypeface(null, android.graphics.Typeface.BOLD);

                    TextView txtDist = new TextView(this);
                    txtDist.setText(String.format(Locale.US, "%.1f KM away", camp.distanceKm));
                    txtDist.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                    txtDist.setTextSize(11f);
                    txtDist.setTypeface(null, android.graphics.Typeface.BOLD);
                    txtDist.setBackgroundResource(R.drawable.bg_chip_status_available);
                    txtDist.setPadding(14, 6, 14, 6);
                    RelativeLayout.LayoutParams dLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dLp.addRule(RelativeLayout.ALIGN_PARENT_END);
                    dLp.addRule(RelativeLayout.CENTER_VERTICAL);
                    txtDist.setLayoutParams(dLp);

                    topRow.addView(txtName);
                    topRow.addView(txtDist);
                    card.addView(topRow);

                    // Organizer & Blood Bank
                    TextView txtOrg = new TextView(this);
                    txtOrg.setText("🏛️ " + camp.getOrganizer() + "  -  " + camp.getBloodBankName());
                    txtOrg.setTextColor(0xFF38BDF8);
                    txtOrg.setTextSize(12f);
                    txtOrg.setTypeface(null, android.graphics.Typeface.BOLD);
                    LinearLayout.LayoutParams orgLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    orgLp.topMargin = 6;
                    txtOrg.setLayoutParams(orgLp);
                    card.addView(txtOrg);

                    // Date & Time Row
                    TextView txtTime = new TextView(this);
                    txtTime.setText("📅 " + camp.getDate() + "  -  ⏰ " + camp.getStartTime() + " - " + camp.getEndTime());
                    txtTime.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                    txtTime.setTextSize(12f);
                    LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    tLp.topMargin = 4;
                    txtTime.setLayoutParams(tLp);
                    card.addView(txtTime);

                    // Address
                    TextView txtAddr = new TextView(this);
                    txtAddr.setText("📍 " + camp.getAddress());
                    txtAddr.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                    txtAddr.setTextSize(12f);
                    LinearLayout.LayoutParams adLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    adLp.topMargin = 4;
                    txtAddr.setLayoutParams(adLp);
                    card.addView(txtAddr);

                    // Slots and Status
                    boolean isFull = camp.getAvailableSlots() <= 0;
                    TextView txtSlots = new TextView(this);
                    txtSlots.setText(isFull ? "🔴 FULL (0 Slots Available)" : "🟢 " + camp.getAvailableSlots() + " Available Slots");
                    txtSlots.setTextColor(isFull ? ContextCompat.getColor(this, R.color.status_critical_text) : ContextCompat.getColor(this, R.color.status_available_text));
                    txtSlots.setTextSize(12f);
                    txtSlots.setTypeface(null, android.graphics.Typeface.BOLD);
                    LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    sLp.topMargin = 6;
                    txtSlots.setLayoutParams(sLp);
                    card.addView(txtSlots);

                    // Action Buttons Row
                    LinearLayout btnRow = new LinearLayout(this);
                    btnRow.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams brLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    brLp.topMargin = 12;
                    btnRow.setLayoutParams(brLp);

                    int campBtnHeight = (int) (42 * getResources().getDisplayMetrics().density);

                    TextView btnDetails = new TextView(this);
                    btnDetails.setText("View Details");
                    btnDetails.setTextColor(0xFF38BDF8);
                    btnDetails.setTextSize(12f);
                    btnDetails.setTypeface(null, android.graphics.Typeface.BOLD);
                    btnDetails.setGravity(android.view.Gravity.CENTER);
                    btnDetails.setBackgroundResource(R.drawable.bg_chip_hospital);
                    btnDetails.setClickable(true);
                    btnDetails.setFocusable(true);
                    btnDetails.setPadding(16, 10, 16, 10);
                    LinearLayout.LayoutParams dtLp = new LinearLayout.LayoutParams(0, campBtnHeight, 1f);
                    dtLp.rightMargin = 8;
                    btnDetails.setLayoutParams(dtLp);
                    btnDetails.setOnClickListener(v -> {
                        Log.d("SMARTBLOOD_CLICK", "Camp View Details clicked: " + camp.getCampName());
                        showCampDetailsDialog(camp);
                    });

                    TextView btnRegister = new TextView(this);
                    btnRegister.setText(isFull ? "Camp Full" : "Register Now →");
                    btnRegister.setTextColor(ContextCompat.getColor(this, R.color.white));
                    btnRegister.setTextSize(12f);
                    btnRegister.setTypeface(null, android.graphics.Typeface.BOLD);
                    btnRegister.setGravity(android.view.Gravity.CENTER);
                    btnRegister.setBackgroundResource(isFull ? R.drawable.bg_chip_status_pending : R.drawable.bg_button_donor);
                    btnRegister.setClickable(!isFull);
                    btnRegister.setFocusable(!isFull);
                    btnRegister.setPadding(16, 10, 16, 10);
                    btnRegister.setEnabled(!isFull);
                    LinearLayout.LayoutParams regLp = new LinearLayout.LayoutParams(0, campBtnHeight, 1.2f);
                    regLp.leftMargin = 8;
                    btnRegister.setLayoutParams(regLp);
                    btnRegister.setOnClickListener(v -> {
                        Log.d("SMARTBLOOD_CLICK", "Camp Register Now clicked: " + camp.getCampName());
                        registerForCamp(camp);
                    });

                    btnRow.addView(btnDetails);
                    btnRow.addView(btnRegister);
                    card.addView(btnRow);

                    container.addView(card);
                }
            } else {
                // My Camp Registrations Tab
                if (searchLayout != null) searchLayout.setVisibility(View.GONE);
                if (radiusScroll != null) radiusScroll.setVisibility(View.GONE);

                if (txtResultCount != null) {
                    txtResultCount.setText(myRegistrations.size() + " Registered Blood Donation Camps");
                }

                if (myRegistrations.isEmpty()) {
                    if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                    if (txtEmptyTitle != null) txtEmptyTitle.setText("No Camp Registrations Yet");
                    if (txtEmptyDesc != null) txtEmptyDesc.setText("You have not registered for any upcoming blood drives. Discover active camps and register to save lives!");
                    if (btnEmptyAction != null) {
                        btnEmptyAction.setText("Explore Active Camps");
                        btnEmptyAction.setOnClickListener(v -> {
                            if (tabActive != null) tabActive.performClick();
                        });
                    }
                    return;
                }

                if (emptyState != null) emptyState.setVisibility(View.GONE);

                for (CampRegistration reg : myRegistrations) {
                    LinearLayout card = new LinearLayout(this);
                    card.setOrientation(LinearLayout.VERTICAL);
                    card.setBackgroundResource(R.drawable.bg_card_donor);
                    card.setPadding(26, 20, 26, 20);
                    LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    cLp.bottomMargin = 14;
                    card.setLayoutParams(cLp);

                    RelativeLayout topRow = new RelativeLayout(this);
                    topRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    TextView txtCampName = new TextView(this);
                    txtCampName.setText("🩸 " + reg.getCampName());
                    txtCampName.setTextColor(ContextCompat.getColor(this, R.color.white));
                    txtCampName.setTextSize(15f);
                    txtCampName.setTypeface(null, android.graphics.Typeface.BOLD);

                    String stUpper = reg.getStatus() != null ? reg.getStatus().toUpperCase(Locale.US) : "PENDING";
                    TextView txtStatus = new TextView(this);
                    txtStatus.setText(stUpper);
                    txtStatus.setTextSize(11f);
                    txtStatus.setTypeface(null, android.graphics.Typeface.BOLD);
                    txtStatus.setPadding(14, 6, 14, 6);

                    if (stUpper.equals("CONFIRMED")) {
                        txtStatus.setText("✓ CONFIRMED");
                        txtStatus.setBackgroundResource(R.drawable.bg_chip_status_verified);
                        txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_verified_text));
                    } else if (stUpper.equals("COMPLETED")) {
                        txtStatus.setText("✓ COMPLETED");
                        txtStatus.setBackgroundResource(R.drawable.bg_chip_status_available);
                        txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                    } else if (stUpper.equals("CANCELLED")) {
                        txtStatus.setText("✕ CANCELLED");
                        txtStatus.setBackgroundResource(R.drawable.bg_chip_status_critical);
                        txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                    } else {
                        txtStatus.setText("⏳ PENDING");
                        txtStatus.setBackgroundResource(R.drawable.bg_chip_status_pending);
                        txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_low_text));
                    }

                    RelativeLayout.LayoutParams sLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    sLp.addRule(RelativeLayout.ALIGN_PARENT_END);
                    sLp.addRule(RelativeLayout.CENTER_VERTICAL);
                    txtStatus.setLayoutParams(sLp);

                    topRow.addView(txtCampName);
                    topRow.addView(txtStatus);
                    card.addView(topRow);

                    TextView txtDetails = new TextView(this);
                    txtDetails.setText("📅 Camp Date: " + reg.getCampDate() + "  -  Associated: " + reg.getBloodBankName());
                    txtDetails.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                    txtDetails.setTextSize(12f);
                    LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dLp.topMargin = 6;
                    txtDetails.setLayoutParams(dLp);
                    card.addView(txtDetails);

                    TextView txtGroup = new TextView(this);
                    txtGroup.setText("Registered Blood Group: " + reg.getBloodGroup() + "  -  Reg ID: " + reg.getRegistrationId());
                    txtGroup.setTextColor(0xFF38BDF8);
                    txtGroup.setTextSize(11f);
                    LinearLayout.LayoutParams gLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    gLp.topMargin = 4;
                    txtGroup.setLayoutParams(gLp);
                    card.addView(txtGroup);

                    if (stUpper.equals("PENDING") || stUpper.equals("CONFIRMED")) {
                        TextView btnCancel = new TextView(this);
                        btnCancel.setText("✕ Cancel Registration");
                        btnCancel.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                        btnCancel.setTextSize(12f);
                        btnCancel.setTypeface(null, android.graphics.Typeface.BOLD);
                        btnCancel.setGravity(android.view.Gravity.CENTER);
                        btnCancel.setBackgroundResource(R.drawable.bg_chip_status_critical);
                        LinearLayout.LayoutParams cnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 76);
                        cnLp.topMargin = 12;
                        btnCancel.setLayoutParams(cnLp);
                        btnCancel.setOnClickListener(v -> {
                            new android.app.AlertDialog.Builder(this)
                                .setTitle("Cancel Camp Registration")
                                .setMessage("Are you sure you want to cancel your registration for " + reg.getCampName() + "?")
                                .setPositiveButton("Cancel Registration", (d, w) -> cancelCampRegistration(reg.getRegistrationId(), reg.getCampId()))
                                .setNegativeButton("Keep Registration", null)
                                .show();
                        });
                        card.addView(btnCancel);
                    }

                    container.addView(card);
                }
            }
        };

        if (tabActive != null && tabMyRegistrations != null) {
            tabActive.setOnClickListener(v -> {
                activeCampsTab = 0;
                tabActive.setBackgroundResource(R.drawable.bg_button_primary);
                tabActive.setTextColor(ContextCompat.getColor(this, R.color.white));
                tabMyRegistrations.setBackgroundResource(0);
                tabMyRegistrations.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                renderCamps.run();
            });

            tabMyRegistrations.setOnClickListener(v -> {
                activeCampsTab = 1;
                tabMyRegistrations.setBackgroundResource(R.drawable.bg_button_primary);
                tabMyRegistrations.setTextColor(ContextCompat.getColor(this, R.color.white));
                tabActive.setBackgroundResource(0);
                tabActive.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                renderCamps.run();
            });
        }

        for (int i = 0; i < allChips.length; i++) {
            final int index = i;
            if (allChips[i] != null) {
                allChips[i].setOnClickListener(v -> {
                    donorCampsFilterRadius = radiusValues[index];
                    for (int j = 0; j < allChips.length; j++) {
                        if (allChips[j] != null) {
                            if (j == index) {
                                allChips[j].setBackgroundResource(R.drawable.bg_button_primary);
                                allChips[j].setTextColor(ContextCompat.getColor(this, R.color.white));
                            } else {
                                allChips[j].setBackgroundResource(R.drawable.bg_chip_status_pending);
                                allChips[j].setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                            }
                        }
                    }
                    renderCamps.run();
                });
            }
        }

        if (inputSearch != null) {
            inputSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) { renderCamps.run(); }
            });
        }

        if (campsListListener != null) {
            campsListListener.remove();
            campsListListener = null;
        }

        campsListListener = FirebaseFirestore.getInstance().collection("bloodCamps")
            .addSnapshotListener((snapshots, e) -> {
                if (progress != null) progress.setVisibility(View.GONE);
                liveCamps.clear();
                if (snapshots != null && !snapshots.isEmpty()) {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String id = doc.getString("campId") != null ? doc.getString("campId") : doc.getId();
                        String name = doc.getString("campName") != null ? doc.getString("campName") : "Blood Camp";
                        String org = doc.getString("organizer") != null ? doc.getString("organizer") : "Volunteer Organization";
                        String bId = doc.getString("bloodBankId") != null ? doc.getString("bloodBankId") : "BB-001";
                        String bName = doc.getString("bloodBankName") != null ? doc.getString("bloodBankName") : "MSI Blood Bank";
                        String loc = doc.getString("location") != null ? doc.getString("location") : "Sangli";
                        String addr = doc.getString("address") != null ? doc.getString("address") : "Sangli, Maharashtra";
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        double fLat = lat != null ? lat : 16.8524;
                        double fLng = lng != null ? lng : 74.5815;
                        String date = doc.getString("date") != null ? doc.getString("date") : "Upcoming";
                        String sTime = doc.getString("startTime") != null ? doc.getString("startTime") : "09:00 AM";
                        String eTime = doc.getString("endTime") != null ? doc.getString("endTime") : "04:00 PM";
                        String contact = doc.getString("contact") != null ? doc.getString("contact") : "+91 233 2374501";
                        String desc = doc.getString("description") != null ? doc.getString("description") : "Voluntary Blood Donation Drive.";
                        Long slots = doc.getLong("availableSlots");
                        int aSlots = slots != null ? slots.intValue() : 25;
                        Long tot = doc.getLong("totalSlots");
                        int tSlots = tot != null ? tot.intValue() : 50;
                        String status = doc.getString("status") != null ? doc.getString("status") : "ACTIVE";
                        String created = doc.getString("createdAt") != null ? doc.getString("createdAt") : "25 Aug 2026";

                        liveCamps.add(new BloodCamp(id, name, org, bId, bName, loc, addr, fLat, fLng, date, sTime, eTime, contact, desc, aSlots, tSlots, status, created));
                    }
                } else {
                    liveCamps.addAll(getMasterCampList());
                }
                renderCamps.run();
            });

        if (donorCampRegistrationsListener != null) {
            donorCampRegistrationsListener.remove();
            donorCampRegistrationsListener = null;
        }

        donorCampRegistrationsListener = FirebaseFirestore.getInstance().collection("campRegistrations")
            .whereEqualTo("donorAuthUid", currentUid)
            .addSnapshotListener((snapshots, e) -> {
                myRegistrations.clear();
                if (snapshots != null && !snapshots.isEmpty()) {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String rId = doc.getString("registrationId") != null ? doc.getString("registrationId") : doc.getId();
                        String cId = doc.getString("campId") != null ? doc.getString("campId") : "";
                        String cName = doc.getString("campName") != null ? doc.getString("campName") : "Donation Camp";
                        String bbId = doc.getString("bloodBankId") != null ? doc.getString("bloodBankId") : "";
                        String bbName = doc.getString("bloodBankName") != null ? doc.getString("bloodBankName") : "Blood Bank";
                        String dId = doc.getString("donorId") != null ? doc.getString("donorId") : currentUid;
                        String dUid = doc.getString("donorAuthUid") != null ? doc.getString("donorAuthUid") : currentUid;
                        String dName = doc.getString("donorName") != null ? doc.getString("donorName") : "Donor";
                        String dPhone = doc.getString("donorPhone") != null ? doc.getString("donorPhone") : "";
                        String bGroup = doc.getString("bloodGroup") != null ? doc.getString("bloodGroup") : "O+";
                        String cDate = doc.getString("campDate") != null ? doc.getString("campDate") : "";
                        String status = doc.getString("status") != null ? doc.getString("status") : "PENDING";
                        String created = doc.getString("createdAt") != null ? doc.getString("createdAt") : "";

                        myRegistrations.add(new CampRegistration(rId, cId, cName, bbId, bbName, dId, dUid, dName, dPhone, bGroup, cDate, status, created));
                    }
                }
                if (activeCampsTab == 1) {
                    renderCamps.run();
                }
            });
    }

    private void showCampDetailsDialog(BloodCamp camp) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_camp_details);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        UserProfile currentUser = repository.getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : "USR-DNR-01";

        TextView txtName = dialog.findViewById(R.id.txt_camp_details_name);
        TextView txtOrganizer = dialog.findViewById(R.id.txt_camp_details_organizer);
        TextView txtStatus = dialog.findViewById(R.id.txt_camp_details_status);
        TextView txtDist = dialog.findViewById(R.id.txt_camp_details_distance);
        TextView txtSlots = dialog.findViewById(R.id.txt_camp_details_slots);
        TextView txtBank = dialog.findViewById(R.id.txt_camp_details_bank_name);
        TextView txtDate = dialog.findViewById(R.id.txt_camp_details_date);
        TextView txtTime = dialog.findViewById(R.id.txt_camp_details_time);
        TextView txtAddr = dialog.findViewById(R.id.txt_camp_details_address);
        TextView txtContact = dialog.findViewById(R.id.txt_camp_details_contact);
        TextView txtDesc = dialog.findViewById(R.id.txt_camp_details_description);
        TextView txtNotice = dialog.findViewById(R.id.txt_camp_details_notice);
        View btnClose = dialog.findViewById(R.id.btn_close_camp_details);
        View btnNavigate = dialog.findViewById(R.id.btn_camp_navigate_map);
        View btnRegister = dialog.findViewById(R.id.btn_camp_register_action);

        double dist = calculateDistanceInKm(userLat, userLng, camp.getLatitude(), camp.getLongitude());

        if (txtName != null) txtName.setText(camp.getCampName());
        if (txtOrganizer != null) txtOrganizer.setText("Organized by " + camp.getOrganizer());
        if (txtDist != null) txtDist.setText(String.format(Locale.US, "%.1f KM away", dist));
        if (txtBank != null) txtBank.setText(camp.getBloodBankName());
        if (txtDate != null) txtDate.setText("📅 " + camp.getDate());
        if (txtTime != null) txtTime.setText("⏰ " + camp.getStartTime() + " - " + camp.getEndTime());
        if (txtAddr != null) txtAddr.setText("📍 " + camp.getAddress());
        if (txtContact != null) txtContact.setText("📞 Contact: " + camp.getContact());
        if (txtDesc != null) txtDesc.setText(camp.getDescription());

        boolean isFull = camp.getAvailableSlots() <= 0;
        if (txtSlots != null) {
            txtSlots.setText(isFull ? "🔴 FULL" : camp.getAvailableSlots() + " Slots Available");
        }
        if (txtStatus != null) {
            txtStatus.setText(isFull ? "FULL" : "ACTIVE");
            txtStatus.setBackgroundResource(isFull ? R.drawable.bg_chip_status_critical : R.drawable.bg_chip_status_available);
            txtStatus.setTextColor(ContextCompat.getColor(this, isFull ? R.color.status_critical_text : R.color.status_available_text));
        }

        // Check if already registered
        FirebaseFirestore.getInstance().collection("campRegistrations")
            .whereEqualTo("donorAuthUid", currentUid)
            .whereEqualTo("campId", camp.getCampId())
            .get()
            .addOnSuccessListener(snaps -> {
                if (snaps != null && !snaps.isEmpty()) {
                    boolean hasActive = false;
                    for (DocumentSnapshot d : snaps.getDocuments()) {
                        String st = d.getString("status");
                        if (st != null && !st.equalsIgnoreCase("CANCELLED")) {
                            hasActive = true;
                            break;
                        }
                    }
                    if (hasActive) {
                        if (txtNotice != null) {
                            txtNotice.setText("You are already registered for this camp.");
                            txtNotice.setVisibility(View.VISIBLE);
                        }
                        if (btnRegister != null) {
                            btnRegister.setEnabled(false);
                            btnRegister.setBackgroundResource(R.drawable.bg_chip_status_pending);
                        }
                    }
                }
            });

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        if (btnNavigate != null) {
            btnNavigate.setOnClickListener(v -> {
                dialog.dismiss();
                loadView(R.layout.view_map, this::bindMapView);
            });
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                dialog.dismiss();
                registerForCamp(camp);
            });
        }

        dialog.show();
    }

    private void registerForCamp(BloodCamp camp) {
        if (camp.getAvailableSlots() <= 0) {
            Toast.makeText(this, "This donation camp is FULL.", Toast.LENGTH_SHORT).show();
            return;
        }

        UserProfile currentUser = repository.getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : "USR-DNR-01";
        String donorName = currentUser != null && currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Donor";
        String donorPhone = currentUser != null ? currentUser.getMobileNumber() : "";
        String donorGroup = currentUser != null ? currentUser.getBloodGroup() : "O+";

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Duplicate Check
        db.collection("campRegistrations")
            .whereEqualTo("donorAuthUid", currentUid)
            .whereEqualTo("campId", camp.getCampId())
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                    boolean hasActive = false;
                    for (DocumentSnapshot d : task.getResult().getDocuments()) {
                        String st = d.getString("status");
                        if (st != null && !st.equalsIgnoreCase("CANCELLED")) {
                            hasActive = true;
                            break;
                        }
                    }
                    if (hasActive) {
                        Toast.makeText(this, "You are already registered for this camp.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                String regId = "REG-" + camp.getCampId() + "-" + System.currentTimeMillis();
                String formattedCreated = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(new Date());

                Map<String, Object> regMap = new HashMap<>();
                regMap.put("registrationId", regId);
                regMap.put("campId", camp.getCampId());
                regMap.put("campName", camp.getCampName());
                regMap.put("bloodBankId", camp.getBloodBankId());
                regMap.put("bloodBankName", camp.getBloodBankName());
                regMap.put("donorId", currentUid);
                regMap.put("donorAuthUid", currentUid);
                regMap.put("donorName", donorName);
                regMap.put("donorPhone", donorPhone);
                regMap.put("bloodGroup", donorGroup);
                regMap.put("campDate", camp.getDate());
                regMap.put("status", "PENDING");
                regMap.put("createdAt", formattedCreated);
                regMap.put("createdAtTimestamp", System.currentTimeMillis());

                db.collection("campRegistrations").document(regId).set(regMap)
                    .addOnSuccessListener(aVoid -> {
                        // Decrement available slots in the camp document
                        db.collection("bloodCamps").document(camp.getCampId())
                            .update("availableSlots", FieldValue.increment(-1));

                        // Notify Donor
                        String notifDonorId = "NOTIF-REG-" + System.currentTimeMillis();
                        Map<String, Object> dNotif = new HashMap<>();
                        dNotif.put("notificationId", notifDonorId);
                        dNotif.put("donorId", currentUid);
                        dNotif.put("donorAuthUid", currentUid);
                        dNotif.put("targetUserId", currentUid);
                        dNotif.put("targetRole", "DONOR");
                        dNotif.put("title", "Camp Registration Submitted");
                        dNotif.put("message", "Your registration for " + camp.getCampName() + " on " + camp.getDate() + " is submitted.");
                        dNotif.put("type", "CAMP_REGISTRATION");
                        dNotif.put("createdAt", formattedCreated);
                        dNotif.put("timestamp", System.currentTimeMillis());
                        dNotif.put("isRead", false);
                        dNotif.put("read", false);
                        db.collection("notifications").document(notifDonorId).set(dNotif);

                        // Notify Blood Bank
                        String notifBankId = "NOTIF-BB-CAMP-" + System.currentTimeMillis();
                        Map<String, Object> bbNotif = new HashMap<>();
                        bbNotif.put("notificationId", notifBankId);
                        bbNotif.put("recipientBloodBankId", camp.getBloodBankId());
                        bbNotif.put("bloodBankId", camp.getBloodBankId());
                        bbNotif.put("targetRole", "BLOOD_BANK");
                        bbNotif.put("title", "New Camp Registration");
                        bbNotif.put("message", "Donor " + donorName + " (" + donorGroup + ") registered for " + camp.getCampName() + ".");
                        bbNotif.put("type", "CAMP_REGISTRATION");
                        bbNotif.put("createdAt", formattedCreated);
                        bbNotif.put("timestamp", System.currentTimeMillis());
                        bbNotif.put("isRead", false);
                        bbNotif.put("read", false);
                        db.collection("notifications").document(notifBankId).set(bbNotif);

                        repository.addAuditLog(currentUid, "DONOR", "CAMP_REGISTER", regId, "10:30 AM", "NEW", "PENDING", "Donor registered for " + camp.getCampName());
                        Toast.makeText(this, "Successfully registered for " + camp.getCampName() + "!", Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            });
    }

    private void cancelCampRegistration(String regId, String campId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        UserProfile currentUser = repository.getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : "USR-DNR-01";

        db.collection("campRegistrations").document(regId)
            .update("status", "CANCELLED")
            .addOnSuccessListener(aVoid -> {
                // Restore slot if camp document exists
                if (campId != null && !campId.isEmpty()) {
                    db.collection("bloodCamps").document(campId)
                        .update("availableSlots", FieldValue.increment(1));
                }

                String notifId = "NOTIF-REG-CAN-" + System.currentTimeMillis();
                Map<String, Object> dNotif = new HashMap<>();
                dNotif.put("notificationId", notifId);
                dNotif.put("donorId", currentUid);
                dNotif.put("donorAuthUid", currentUid);
                dNotif.put("targetUserId", currentUid);
                dNotif.put("targetRole", "DONOR");
                dNotif.put("title", "Camp Registration Cancelled");
                dNotif.put("message", "Your registration has been cancelled.");
                dNotif.put("type", "CAMP_REGISTRATION_CANCELLED");
                dNotif.put("createdAt", new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(new Date()));
                dNotif.put("timestamp", System.currentTimeMillis());
                dNotif.put("isRead", false);
                dNotif.put("read", false);
                db.collection("notifications").document(notifId).set(dNotif);

                Toast.makeText(this, "Registration cancelled.", Toast.LENGTH_SHORT).show();
            });
    }

    private void bindInventoryView(View view) {
        attachRoleBackgroundAnimators(view);

        EditText inputSearch = view.findViewById(R.id.input_search_inventory);
        ProgressBar progress = view.findViewById(R.id.loading_inventory_progress);
        LinearLayout container = view.findViewById(R.id.layout_inventory_container);
        View emptyState = view.findViewById(R.id.layout_inventory_empty);
        TextView txtCount = view.findViewById(R.id.txt_inventory_count);

        TextView chipAll = view.findViewById(R.id.chip_filter_all);
        TextView chipOPos = view.findViewById(R.id.chip_filter_available);
        TextView chipONeg = view.findViewById(R.id.chip_filter_low);
        TextView chipAPos = view.findViewById(R.id.chip_filter_critical);
        TextView chipBPos = view.findViewById(R.id.chip_filter_expiring);
        TextView chipANeg = view.findViewById(R.id.chip_filter_a_neg);
        TextView chipBNeg = view.findViewById(R.id.chip_filter_b_neg);
        TextView chipABPos = view.findViewById(R.id.chip_filter_ab_pos);
        TextView chipABNeg = view.findViewById(R.id.chip_filter_ab_neg);
        TextView chipBombay = view.findViewById(R.id.chip_filter_bombay);

        if (currentRole == UserRole.HOSPITAL) {
            final String[] currentFilterGroup = new String[]{""};
            if (txtCount != null) txtCount.setText("Regional Blood Bank Stock Near You");

            Runnable refreshBankList = () -> {
                if (container == null) return;
                container.removeAllViews();

                String query = inputSearch != null ? inputSearch.getText().toString().toLowerCase().trim() : "";
                String targetGroup = currentFilterGroup[0];

                List<SmartMapItem> matches = new ArrayList<>();
                for (SmartMapItem item : smartMapItemList) {
                    if (!"BLOOD_BANK".equalsIgnoreCase(item.type)) continue;
                    boolean textMatch = query.isEmpty() || item.name.toLowerCase().contains(query) || (item.area != null && item.area.toLowerCase().contains(query));
                    boolean groupMatch = targetGroup.isEmpty() || (item.stockMap != null && item.stockMap.containsKey(targetGroup) && item.stockMap.get(targetGroup) > 0);
                    if (textMatch && groupMatch) matches.add(item);
                }

                if (matches.isEmpty()) {
                    if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                } else {
                    if (emptyState != null) emptyState.setVisibility(View.GONE);
                    for (SmartMapItem bank : matches) {
                        LinearLayout card = new LinearLayout(this);
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setBackgroundResource(R.drawable.bg_card_hospital);
                        card.setPadding(32, 28, 32, 28);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 0, 0, 24);
                        card.setLayoutParams(lp);

                        RelativeLayout topRow = new RelativeLayout(this);
                        topRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        TextView txtName = new TextView(this);
                        txtName.setText("🩸 " + bank.name);
                        txtName.setTextColor(ContextCompat.getColor(this, R.color.white));
                        txtName.setTextSize(16f);
                        txtName.setTypeface(null, android.graphics.Typeface.BOLD);

                        TextView txtDist = new TextView(this);
                        txtDist.setText(String.format(Locale.US, "%.1f KM", bank.distanceKm));
                        txtDist.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                        txtDist.setTextSize(12f);
                        txtDist.setTypeface(null, android.graphics.Typeface.BOLD);
                        txtDist.setBackgroundResource(R.drawable.bg_chip_status_available);
                        txtDist.setPadding(16, 6, 16, 6);
                        RelativeLayout.LayoutParams distLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        distLp.addRule(RelativeLayout.ALIGN_PARENT_END);
                        distLp.addRule(RelativeLayout.CENTER_VERTICAL);
                        txtDist.setLayoutParams(distLp);

                        topRow.addView(txtName);
                        topRow.addView(txtDist);
                        card.addView(topRow);

                        TextView txtArea = new TextView(this);
                        txtArea.setText("📍 " + bank.area + "  -  Verified Reserve Facility");
                        txtArea.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                        txtArea.setTextSize(12f);
                        LinearLayout.LayoutParams areaLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        areaLp.topMargin = 6;
                        txtArea.setLayoutParams(areaLp);
                        card.addView(txtArea);

                        if (bank.stockMap != null && !bank.stockMap.isEmpty()) {
                            StringBuilder sb = new StringBuilder("Available Stock: ");
                            int count = 0;
                            for (Map.Entry<String, Integer> entry : bank.stockMap.entrySet()) {
                                if (count > 0) sb.append("  -  ");
                                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("u");
                                count++;
                            }
                            TextView txtStock = new TextView(this);
                            txtStock.setText(sb.toString());
                            txtStock.setTextColor(0xFF38BDF8);
                            txtStock.setTextSize(12f);
                            txtStock.setTypeface(null, android.graphics.Typeface.BOLD);
                            LinearLayout.LayoutParams stockLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            stockLp.topMargin = 10;
                            txtStock.setLayoutParams(stockLp);
                            card.addView(txtStock);
                        }

                        LinearLayout actionsRow = new LinearLayout(this);
                        actionsRow.setOrientation(LinearLayout.HORIZONTAL);
                        LinearLayout.LayoutParams actLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 84);
                        actLp.topMargin = 20;
                        actionsRow.setLayoutParams(actLp);

                        TextView btnReserve = new TextView(this);
                        btnReserve.setText("Reserve Blood");
                        btnReserve.setTextColor(ContextCompat.getColor(this, R.color.white));
                        btnReserve.setTextSize(12f);
                        btnReserve.setTypeface(null, android.graphics.Typeface.BOLD);
                        btnReserve.setGravity(android.view.Gravity.CENTER);
                        btnReserve.setBackgroundResource(R.drawable.bg_button_hospital);
                        LinearLayout.LayoutParams resLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                        resLp.rightMargin = 8;
                        btnReserve.setLayoutParams(resLp);
                        btnReserve.setOnClickListener(v -> {
                            selectedBloodBank = bank;
                            currentBankName = bank.name;
                            currentBankLat = bank.lat;
                            currentBankLng = bank.lng;
                            currentBankPhone = bank.phone;
                            showReserveBloodDialog();
                        });

                        TextView btnNav = new TextView(this);
                        btnNav.setText("🧭 Navigate");
                        btnNav.setTextColor(0xFF38BDF8);
                        btnNav.setTextSize(12f);
                        btnNav.setTypeface(null, android.graphics.Typeface.BOLD);
                        btnNav.setGravity(android.view.Gravity.CENTER);
                        btnNav.setBackgroundResource(R.drawable.bg_chip_hospital);
                        LinearLayout.LayoutParams navLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                        navLp.leftMargin = 8;
                        btnNav.setLayoutParams(navLp);
                        btnNav.setOnClickListener(v -> {
                            selectedBloodBank = bank;
                            currentBankName = bank.name;
                            currentBankLat = bank.lat;
                            currentBankLng = bank.lng;
                            currentBankPhone = bank.phone;
                            loadView(R.layout.view_map, this::bindMapView);
                        });

                        actionsRow.addView(btnReserve);
                        actionsRow.addView(btnNav);
                        card.addView(actionsRow);

                        container.addView(card);
                    }
                }
            };

            refreshBankList.run();
            if (inputSearch != null) inputSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) { refreshBankList.run(); }
            });

            if (chipAll != null) chipAll.setOnClickListener(v -> { currentFilterGroup[0] = ""; refreshBankList.run(); });
            if (chipOPos != null) chipOPos.setOnClickListener(v -> { currentFilterGroup[0] = "O+"; refreshBankList.run(); });
            if (chipONeg != null) chipONeg.setOnClickListener(v -> { currentFilterGroup[0] = "O-"; refreshBankList.run(); });
            if (chipAPos != null) chipAPos.setOnClickListener(v -> { currentFilterGroup[0] = "A+"; refreshBankList.run(); });
            if (chipBPos != null) chipBPos.setOnClickListener(v -> { currentFilterGroup[0] = "B+"; refreshBankList.run(); });
            if (chipANeg != null) chipANeg.setOnClickListener(v -> { currentFilterGroup[0] = "A-"; refreshBankList.run(); });
            if (chipBNeg != null) chipBNeg.setOnClickListener(v -> { currentFilterGroup[0] = "B-"; refreshBankList.run(); });
            if (chipABPos != null) chipABPos.setOnClickListener(v -> { currentFilterGroup[0] = "AB+"; refreshBankList.run(); });
            if (chipABNeg != null) chipABNeg.setOnClickListener(v -> { currentFilterGroup[0] = "AB-"; refreshBankList.run(); });
            if (chipBombay != null) chipBombay.setOnClickListener(v -> { currentFilterGroup[0] = "Bombay (Oh)"; refreshBankList.run(); });
        } else if (currentRole == UserRole.BLOOD_BANK) {
            UserProfile curUser = repository.getCurrentUser();
            String activeBankId = curUser != null && curUser.getBloodBankId() != null ? curUser.getBloodBankId() : currentBankId;
            if (txtCount != null) txtCount.setText("My Blood Bank Inventory");
            if (chipAll != null) chipAll.setText("All Groups");
            if (chipOPos != null) chipOPos.setText("O+");
            if (chipONeg != null) chipONeg.setText("O-");
            if (chipAPos != null) chipAPos.setText("A+");
            if (chipBPos != null) chipBPos.setText("B+");
            
            final String[] currentFilterGroup = new String[]{""};

            if (progress != null) progress.setVisibility(View.VISIBLE);
            
            inventoryBankListener = FirebaseFirestore.getInstance().collection("bloodBanks").document(activeBankId)
                .addSnapshotListener((snapshot, e) -> {
                    if (progress != null) progress.setVisibility(View.GONE);
                    if (container == null) return;
                    container.removeAllViews();

                    if (snapshot == null || !snapshot.exists()) {
                        if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                        return;
                    }

                    if (emptyState != null) emptyState.setVisibility(View.GONE);
                    Map<String, Integer> stockMap = extractStockMapFromDoc(snapshot);
                    String[] allGroups = new String[]{"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-", "Bombay (Oh)"};
                    
                    String query = inputSearch != null ? inputSearch.getText().toString().toLowerCase().trim() : "";
                    String filter = currentFilterGroup[0];

                    for (String group : allGroups) {
                        if (!filter.isEmpty() && !group.equals(filter)) continue;
                        if (!query.isEmpty() && !group.toLowerCase().contains(query)) continue;

                        int units = getNumericStock(stockMap, group);

                        LinearLayout card = new LinearLayout(this);
                        card.setOrientation(LinearLayout.HORIZONTAL);
                        card.setBackgroundResource(R.drawable.bg_card_bloodbank);
                        card.setPadding(32, 28, 32, 28);
                        card.setGravity(android.view.Gravity.CENTER_VERTICAL);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 0, 0, 20);
                        card.setLayoutParams(lp);

                        LinearLayout infoCol = new LinearLayout(this);
                        infoCol.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                        infoCol.setLayoutParams(infoLp);

                        TextView txtGrp = new TextView(this);
                        txtGrp.setText("🩸 " + group);
                        txtGrp.setTextColor(ContextCompat.getColor(this, R.color.white));
                        txtGrp.setTextSize(16f);
                        txtGrp.setTypeface(null, android.graphics.Typeface.BOLD);
                        infoCol.addView(txtGrp);

                        TextView txtUnits = new TextView(this);
                        txtUnits.setText(units + " Units Available");
                        txtUnits.setTextColor(0xFF38BDF8);
                        txtUnits.setTextSize(14f);
                        infoCol.addView(txtUnits);

                        TextView txtSt = new TextView(this);
                        txtSt.setTextSize(10f);
                        txtSt.setTypeface(null, android.graphics.Typeface.BOLD);
                        txtSt.setPadding(12, 4, 12, 4);
                        if (units == 0) {
                            txtSt.setText("OUT OF STOCK");
                            txtSt.setBackgroundResource(R.drawable.bg_chip_status_critical);
                            txtSt.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                        } else if (units <= LOW_STOCK_THRESHOLD) {
                            txtSt.setText("LOW STOCK");
                            txtSt.setBackgroundResource(R.drawable.bg_chip_status_low);
                            txtSt.setTextColor(ContextCompat.getColor(this, R.color.status_low_text));
                        } else {
                            txtSt.setText("GOOD STOCK");
                            txtSt.setBackgroundResource(R.drawable.bg_chip_status_available);
                            txtSt.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                        }
                        LinearLayout.LayoutParams stLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        stLp.topMargin = 6;
                        txtSt.setLayoutParams(stLp);
                        infoCol.addView(txtSt);

                        card.addView(infoCol);

                        TextView btnManage = new TextView(this);
                        btnManage.setText("MANAGE");
                        btnManage.setTextColor(ContextCompat.getColor(this, R.color.white));
                        btnManage.setBackgroundResource(R.drawable.bg_button_hospital);
                        btnManage.setPadding(24, 12, 24, 12);
                        btnManage.setTextSize(12f);
                        btnManage.setTypeface(null, android.graphics.Typeface.BOLD);
                        btnManage.setOnClickListener(v -> showUpdateInventoryStockDialog(group, units));
                        card.addView(btnManage);

                        container.addView(card);
                    }
                });

            Runnable localRefresh = () -> {
                FirebaseFirestore.getInstance().collection("bloodBanks").document(activeBankId).get()
                    .addOnSuccessListener(snap -> { });
            };

            if (inputSearch != null) inputSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) { localRefresh.run(); }
            });

            if (chipAll != null) chipAll.setOnClickListener(v -> { currentFilterGroup[0] = ""; localRefresh.run(); });
            if (chipOPos != null) chipOPos.setOnClickListener(v -> { currentFilterGroup[0] = "O+"; localRefresh.run(); });
            if (chipONeg != null) chipONeg.setOnClickListener(v -> { currentFilterGroup[0] = "O-"; localRefresh.run(); });
            if (chipAPos != null) chipAPos.setOnClickListener(v -> { currentFilterGroup[0] = "A+"; localRefresh.run(); });
            if (chipBPos != null) chipBPos.setOnClickListener(v -> { currentFilterGroup[0] = "B+"; localRefresh.run(); });
            if (chipANeg != null) chipANeg.setOnClickListener(v -> { currentFilterGroup[0] = "A-"; localRefresh.run(); });
            if (chipBNeg != null) chipBNeg.setOnClickListener(v -> { currentFilterGroup[0] = "B-"; localRefresh.run(); });
            if (chipABPos != null) chipABPos.setOnClickListener(v -> { currentFilterGroup[0] = "AB+"; localRefresh.run(); });
            if (chipABNeg != null) chipABNeg.setOnClickListener(v -> { currentFilterGroup[0] = "AB-"; localRefresh.run(); });
            if (chipBombay != null) chipBombay.setOnClickListener(v -> { currentFilterGroup[0] = "Bombay (Oh)"; localRefresh.run(); });
        } else {
            TextView chipAllH = view.findViewById(R.id.chip_filter_all);
            if (chipAllH != null) chipAllH.setOnClickListener(v -> Toast.makeText(this, "Showing all inventory items", Toast.LENGTH_SHORT).show());
        }
    }

    
    private void bindRequestsView(View view) {
        attachRoleBackgroundAnimators(view);

        ProgressBar progress = view.findViewById(R.id.loading_requests_progress);
        LinearLayout container = view.findViewById(R.id.layout_requests_container);
        View emptyState = view.findViewById(R.id.layout_requests_empty_state);
        TextView txtPageTitle = view.findViewById(R.id.txt_requests_page_title);
        TextView txtSubtitle = view.findViewById(R.id.txt_requests_subtitle);
        TextView btnCreate = view.findViewById(R.id.btn_requests_page_create);
        View btnEmptyCreate = view.findViewById(R.id.btn_empty_create_request);
        LinearLayout bankTabsLayout = view.findViewById(R.id.layout_bank_request_tabs);
        TextView tabBankHosp = view.findViewById(R.id.tab_bank_hospital_requests);
        TextView tabBankDonors = view.findViewById(R.id.tab_bank_donor_appointments);
        TextView tabBankCamps = view.findViewById(R.id.tab_bank_camp_registrations);

        UserProfile currentUser = repository.getCurrentUser();
        final String currentUid = currentUser != null ? currentUser.getUid() : "USR-01";
        final String activeBankId = currentUser != null && currentUser.getBloodBankId() != null ? currentUser.getBloodBankId() : currentBankId;
        final String bankName = currentUser != null && currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentBankName;

        if (currentRole == UserRole.HOSPITAL) {
            if (bankTabsLayout != null) bankTabsLayout.setVisibility(View.GONE);
            if (txtPageTitle != null) txtPageTitle.setText("Blood Requests");
            if (txtSubtitle != null) txtSubtitle.setText("Live hospital requisitions and fulfillment status");
            if (btnCreate != null) {
                btnCreate.setVisibility(View.VISIBLE);
                btnCreate.setText("+ New Request");
                btnCreate.setOnClickListener(v -> showCreateBloodRequestDialog(null));
            }
            if (btnEmptyCreate != null) {
                btnEmptyCreate.setOnClickListener(v -> showCreateBloodRequestDialog(null));
            }

            if (progress != null) progress.setVisibility(View.VISIBLE);
            try {
                hospitalRequestsListener = FirebaseFirestore.getInstance().collection("bloodRequests")
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (progress != null) progress.setVisibility(View.GONE);
                        if (container == null) return;
                        container.removeAllViews();

                        if (e != null || queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                            return;
                        }

                        boolean hasHospitalRequests = false;
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            String hId = doc.getString("hospitalId");
                            String hUid = doc.getString("hospitalUid");
                            String hName = doc.getString("hospitalName");
                            String myHospName = currentUser != null && currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Hospital";

                            boolean matchesHosp = (currentUid != null && (currentUid.equalsIgnoreCase(hId) || currentUid.equalsIgnoreCase(hUid))) ||
                                                  (hName != null && (hName.equalsIgnoreCase(myHospName) || hName.toLowerCase(Locale.US).contains(myHospName.toLowerCase(Locale.US)) || myHospName.toLowerCase(Locale.US).contains(hName.toLowerCase(Locale.US))));

                            if (!matchesHosp) continue;

                            hasHospitalRequests = true;
                            String reqId = doc.getString("requestId") != null ? doc.getString("requestId") : doc.getId();
                            String bGroup = doc.getString("bloodGroup") != null ? doc.getString("bloodGroup") : "O+";
                            String comp = doc.getString("component") != null ? doc.getString("component") : "Packed RBC";
                            Long qtyLong = doc.getLong("quantity");
                            if (qtyLong == null) qtyLong = doc.getLong("units");
                            if (qtyLong == null) qtyLong = doc.getLong("requiredUnits");
                            int qty = qtyLong != null ? qtyLong.intValue() : 4;
                            String urgency = doc.getString("urgency") != null ? doc.getString("urgency") : (doc.getString("priority") != null ? doc.getString("priority") : "Normal");
                            String status = doc.getString("status") != null ? doc.getString("status") : "Pending";
                            String createdAt = doc.getString("createdAt") != null ? doc.getString("createdAt") : "Today";
                            String notes = doc.getString("notes") != null ? doc.getString("notes") : "";
                            String assigned = doc.getString("assignedSource") != null ? doc.getString("assignedSource") : (doc.getString("targetBankName") != null ? doc.getString("targetBankName") : "");

                            if (!requestsFilterStatus.equals("ALL")) {
                                if (requestsFilterStatus.equals("PENDING") && !status.equalsIgnoreCase("Pending") && !status.equalsIgnoreCase("Searching")) continue;
                                if (requestsFilterStatus.equals("IN_TRANSIT") && !status.equalsIgnoreCase("In Transit") && !status.equalsIgnoreCase("Allocated") && !status.equalsIgnoreCase("Accepted")) continue;
                                if (requestsFilterStatus.equals("FULFILLED") && !status.equalsIgnoreCase("Fulfilled")) continue;
                                if (requestsFilterStatus.equals("EMERGENCY") && !urgency.equalsIgnoreCase("Emergency") && !urgency.equalsIgnoreCase("Critical")) continue;
                            }

                            BloodRequest req = new BloodRequest(reqId, currentUid, bankName, bGroup, comp, qty, urgency, status, createdAt, notes, userLat, userLng, "");
                            req.setAssignedSource(assigned);

                            LinearLayout card = new LinearLayout(this);
                            card.setOrientation(LinearLayout.VERTICAL);
                            card.setBackgroundResource(R.drawable.bg_card_hospital);
                            card.setPadding(32, 28, 32, 28);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(0, 0, 0, 24);
                            card.setLayoutParams(lp);

                            RelativeLayout topRow = new RelativeLayout(this);
                            topRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                            TextView txtReqId = new TextView(this);
                            txtReqId.setText(reqId);
                            txtReqId.setTextColor(ContextCompat.getColor(this, R.color.text_tertiary));
                            txtReqId.setTextSize(12f);
                            txtReqId.setTypeface(null, android.graphics.Typeface.BOLD);

                            TextView txtStatus = new TextView(this);
                            String statusUpper = status.toUpperCase(Locale.US);
                            txtStatus.setText(statusUpper);
                            txtStatus.setTextSize(11f);
                            txtStatus.setTypeface(null, android.graphics.Typeface.BOLD);
                            txtStatus.setPadding(20, 8, 20, 8);

                            if (statusUpper.contains("FULFIL")) {
                                txtStatus.setBackgroundResource(R.drawable.bg_chip_status_available);
                                txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                            } else if (statusUpper.contains("ACCEPT") || statusUpper.contains("RESERV") || statusUpper.contains("ALLOCAT") || statusUpper.contains("TRANSIT")) {
                                txtStatus.setBackgroundResource(R.drawable.bg_chip_status_reserved);
                                txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_reserved_text));
                            } else if (statusUpper.contains("CANCEL")) {
                                txtStatus.setBackgroundResource(R.drawable.bg_chip_status_pending);
                                txtStatus.setTextColor(ContextCompat.getColor(this, R.color.text_tertiary));
                            } else {
                                txtStatus.setBackgroundResource(R.drawable.bg_chip_status_critical);
                                txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                            }

                            RelativeLayout.LayoutParams statusLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            statusLp.addRule(RelativeLayout.ALIGN_PARENT_END);
                            statusLp.addRule(RelativeLayout.CENTER_VERTICAL);
                            txtStatus.setLayoutParams(statusLp);

                            topRow.addView(txtReqId);
                            topRow.addView(txtStatus);
                            card.addView(topRow);

                            LinearLayout detailsRow = new LinearLayout(this);
                            detailsRow.setOrientation(LinearLayout.HORIZONTAL);
                            detailsRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
                            LinearLayout.LayoutParams dtLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dtLp.topMargin = 12;
                            detailsRow.setLayoutParams(dtLp);

                            TextView txtBlood = new TextView(this);
                            txtBlood.setText(String.format(Locale.US, "%s - %d Units %s", bGroup, qty, comp));
                            txtBlood.setTextColor(ContextCompat.getColor(this, R.color.white));
                            txtBlood.setTextSize(17f);
                            txtBlood.setTypeface(null, android.graphics.Typeface.BOLD);

                            TextView txtUrgency = new TextView(this);
                            txtUrgency.setText(urgency.toUpperCase(Locale.US));
                            txtUrgency.setTextSize(10f);
                            txtUrgency.setTypeface(null, android.graphics.Typeface.BOLD);
                            txtUrgency.setPadding(14, 4, 14, 4);

                            if (urgency.equalsIgnoreCase("Emergency") || urgency.equalsIgnoreCase("Critical")) {
                                txtUrgency.setBackgroundResource(R.drawable.bg_chip_status_critical);
                                txtUrgency.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                            } else {
                                txtUrgency.setBackgroundResource(R.drawable.bg_chip_status_available);
                                txtUrgency.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                            }
                            LinearLayout.LayoutParams urgLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            urgLp.leftMargin = 16;
                            txtUrgency.setLayoutParams(urgLp);

                            detailsRow.addView(txtBlood);
                            detailsRow.addView(txtUrgency);
                            card.addView(detailsRow);

                            TextView txtInfo = new TextView(this);
                            String assignedInfo = !assigned.isEmpty() ? " - " + assigned : "";
                            String noteInfo = !notes.isEmpty() ? " (" + notes + ")" : "";
                            txtInfo.setText("Created " + createdAt + assignedInfo + noteInfo);
                            txtInfo.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                            txtInfo.setTextSize(12f);
                            LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            infoLp.topMargin = 6;
                            txtInfo.setLayoutParams(infoLp);
                            card.addView(txtInfo);

                            // Donor Response Info if Matched
                            String matchedDonorName = doc.getString("donorName");
                            String donorStatus = doc.getString("donorStatus");
                            if (matchedDonorName != null && !matchedDonorName.isEmpty()) {
                                TextView txtDonorResp = new TextView(this);
                                txtDonorResp.setText("🩸 Matched Donor: " + matchedDonorName + " (" + (doc.getString("donorBloodGroup") != null ? doc.getString("donorBloodGroup") : bGroup) + ") - " + (donorStatus != null ? donorStatus : "AVAILABLE"));
                                txtDonorResp.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                                txtDonorResp.setTextSize(12f);
                                txtDonorResp.setTypeface(null, android.graphics.Typeface.BOLD);
                                LinearLayout.LayoutParams dRespLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                dRespLp.topMargin = 8;
                                txtDonorResp.setLayoutParams(dRespLp);
                                card.addView(txtDonorResp);
                            }

                            if (statusUpper.contains("PENDING") || statusUpper.contains("SEARCHING")) {
                                TextView btnCancel = new TextView(this);
                                btnCancel.setText("Cancel Request");
                                btnCancel.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                                btnCancel.setTextSize(12f);
                                btnCancel.setTypeface(null, android.graphics.Typeface.BOLD);
                                btnCancel.setGravity(android.view.Gravity.CENTER);
                                btnCancel.setBackgroundResource(R.drawable.bg_card_emergency);
                                LinearLayout.LayoutParams canLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(42 * getResources().getDisplayMetrics().density));
                                canLp.topMargin = 16;
                                btnCancel.setLayoutParams(canLp);
                                btnCancel.setOnClickListener(v -> {
                                    new AlertDialog.Builder(this)
                                        .setTitle("Cancel Blood Request")
                                        .setMessage("Are you sure you want to cancel " + reqId + "?")
                                        .setPositiveButton("Yes, Cancel", (d, w) -> {
                                            String timeStr = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());
                                            Map<String, Object> upd = new HashMap<>();
                                            upd.put("status", "Cancelled");
                                            upd.put("cancelledAt", timeStr);
                                            FirebaseFirestore.getInstance().collection("bloodRequests").document(reqId).update(upd);
                                            Toast.makeText(this, "Request " + reqId + " cancelled.", Toast.LENGTH_SHORT).show();
                                        })
                                        .setNegativeButton("No", null)
                                        .show();
                                });
                                card.addView(btnCancel);
                            } else if (statusUpper.contains("ACCEPT") || statusUpper.contains("RESERV") || statusUpper.contains("ALLOCAT") || statusUpper.contains("TRANSIT")) {
                                LinearLayout actRow = new LinearLayout(this);
                                actRow.setOrientation(LinearLayout.HORIZONTAL);
                                LinearLayout.LayoutParams actLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(42 * getResources().getDisplayMetrics().density));
                                actLp.topMargin = 16;
                                actRow.setLayoutParams(actLp);

                                TextView btnTrackCourier = new TextView(this);
                                btnTrackCourier.setText("Track Route");
                                btnTrackCourier.setTextColor(0xFF38BDF8);
                                btnTrackCourier.setTextSize(12f);
                                btnTrackCourier.setTypeface(null, android.graphics.Typeface.BOLD);
                                btnTrackCourier.setGravity(android.view.Gravity.CENTER);
                                btnTrackCourier.setBackgroundResource(R.drawable.bg_chip_hospital);
                                LinearLayout.LayoutParams trkLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                                trkLp.rightMargin = 8;
                                btnTrackCourier.setLayoutParams(trkLp);
                                btnTrackCourier.setOnClickListener(v -> loadView(R.layout.view_map, this::bindMapView));

                                TextView btnVerifyQr = new TextView(this);
                                btnVerifyQr.setText("📲 QR / OTP Handover");
                                btnVerifyQr.setTextColor(ContextCompat.getColor(this, R.color.white));
                                btnVerifyQr.setTextSize(12f);
                                btnVerifyQr.setTypeface(null, android.graphics.Typeface.BOLD);
                                btnVerifyQr.setGravity(android.view.Gravity.CENTER);
                                btnVerifyQr.setBackgroundResource(R.drawable.bg_button_hospital);
                                LinearLayout.LayoutParams qrLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                                qrLp.leftMargin = 8;
                                btnVerifyQr.setLayoutParams(qrLp);
                                btnVerifyQr.setOnClickListener(v -> showQRVerificationDialog(req));

                                actRow.addView(btnTrackCourier);
                                actRow.addView(btnVerifyQr);
                                card.addView(actRow);
                            }

                            container.addView(card);
                        }

                        if (!hasHospitalRequests) {
                            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                        } else {
                            if (emptyState != null) emptyState.setVisibility(View.GONE);
                        }
                    });
            } catch (Exception e) {
                if (progress != null) progress.setVisibility(View.GONE);
                if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
            }
        } else if (currentRole == UserRole.BLOOD_BANK) {
            if (bankTabsLayout != null) bankTabsLayout.setVisibility(View.VISIBLE);
            if (txtPageTitle != null) txtPageTitle.setText("Requisitions & Appointments");
            if (txtSubtitle != null) txtSubtitle.setText("Manage incoming requisitions, donor appointments, and camps");
            if (btnCreate != null) btnCreate.setVisibility(View.GONE);

            Runnable updateBankTabUI = () -> {
                if (tabBankHosp != null) {
                    tabBankHosp.setBackgroundResource(selectedBankTabIndex == 0 ? R.drawable.bg_button_primary : 0);
                    tabBankHosp.setTextColor(ContextCompat.getColor(this, selectedBankTabIndex == 0 ? R.color.white : R.color.text_secondary));
                }
                if (tabBankDonors != null) {
                    tabBankDonors.setBackgroundResource(selectedBankTabIndex == 1 ? R.drawable.bg_button_primary : 0);
                    tabBankDonors.setTextColor(ContextCompat.getColor(this, selectedBankTabIndex == 1 ? R.color.white : R.color.text_secondary));
                }
                if (tabBankCamps != null) {
                    tabBankCamps.setBackgroundResource(selectedBankTabIndex == 2 ? R.drawable.bg_button_primary : 0);
                    tabBankCamps.setTextColor(ContextCompat.getColor(this, selectedBankTabIndex == 2 ? R.color.white : R.color.text_secondary));
                }

                if (progress != null) progress.setVisibility(View.VISIBLE);
                if (container != null) container.removeAllViews();
                if (emptyState != null) emptyState.setVisibility(View.GONE);

                // Common Empty State helper
                java.util.function.BiConsumer<String, String> showEmpty = (title, desc) -> {
                    if (emptyState != null) {
                        emptyState.setVisibility(View.VISIBLE);
                        TextView txtTitle = emptyState.findViewById(R.id.txt_empty_requests_title);
                        TextView txtDesc = emptyState.findViewById(R.id.txt_empty_requests_desc);
                        View btnEmpty = emptyState.findViewById(R.id.btn_empty_create_request);
                        if (txtTitle != null) txtTitle.setText(title);
                        if (txtDesc != null) txtDesc.setText(desc);
                        if (btnEmpty != null) btnEmpty.setVisibility(View.GONE);
                    }
                };

                if (selectedBankTabIndex == 0) {
                    // Hospital Requisitions
                    FirebaseFirestore.getInstance().collection("bloodRequests")
                            .addSnapshotListener((snapshots, e) -> {
                                if (progress != null) progress.setVisibility(View.GONE);
                                if (container == null) return;
                                container.removeAllViews();

                                if (e != null || snapshots == null || snapshots.isEmpty()) {
                                    showEmpty.accept("No hospital requests yet.", "There are currently no active blood requisitions from hospitals assigned to your facility.");
                                    return;
                                }

                                boolean hasMatches = false;
                                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                    String tId = doc.getString("targetBankId");
                                    String bId = doc.getString("bloodBankId");
                                    String tName = doc.getString("targetBankName");
                                    String bName = doc.getString("bloodBankName");
                                    String urg = doc.getString("urgency");
                                    boolean isEmergency = urg != null && (urg.equalsIgnoreCase("EMERGENCY") || urg.equalsIgnoreCase("Critical") || urg.toUpperCase(Locale.US).contains("EMERG"));

                                    boolean matchesBank = false;
                                    if (activeBankId != null && (activeBankId.equalsIgnoreCase(tId) || activeBankId.equalsIgnoreCase(bId) || activeBankId.equalsIgnoreCase(currentUid))) matchesBank = true;
                                    if (bankName != null && ((tName != null && tName.equalsIgnoreCase(bankName)) || (bName != null && bName.equalsIgnoreCase(bankName)) || (tName != null && bankName.toLowerCase(Locale.US).contains(tName.toLowerCase(Locale.US))))) matchesBank = true;
                                    if (tId == null && bId == null && tName == null && bName == null) matchesBank = true;
                                    if (isEmergency) matchesBank = true;

                                    if (!matchesBank) continue;

                                    hasMatches = true;
                                    final String reqId = doc.getString("requestId") != null ? doc.getString("requestId") : doc.getId();
                                    final String hospId = doc.getString("hospitalId") != null ? doc.getString("hospitalId") : "HOS-01";
                                    final String hospName = doc.getString("hospitalName") != null ? doc.getString("hospitalName") : "Hospital";
                                    final String bGroup = doc.getString("bloodGroup") != null ? doc.getString("bloodGroup") : "O+";
                                    final String comp = doc.getString("component") != null ? doc.getString("component") : "Packed RBC";
                                    Long qtyLong = doc.getLong("quantity");
                                    if (qtyLong == null) qtyLong = doc.getLong("units");
                                    if (qtyLong == null) qtyLong = doc.getLong("requiredUnits");
                                    final int qty = qtyLong != null ? qtyLong.intValue() : 1;
                                    final String urgency = doc.getString("urgency") != null ? doc.getString("urgency") : (doc.getString("priority") != null ? doc.getString("priority") : "Normal");
                                    final String status = doc.getString("status") != null ? doc.getString("status") : "Pending";
                                    final String createdAt = doc.getString("createdAt") != null ? doc.getString("createdAt") : "Today";

                                    LinearLayout card = new LinearLayout(this);
                                    card.setOrientation(LinearLayout.VERTICAL);
                                    card.setBackgroundResource(R.drawable.bg_card_premium);
                                    card.setPadding(30, 24, 30, 24);
                                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    lp.bottomMargin = 20;
                                    card.setLayoutParams(lp);

                                    TextView txtTitle = new TextView(this);
                                    txtTitle.setText("🏥 " + hospName + " - " + reqId);
                                    txtTitle.setTextColor(ContextCompat.getColor(this, R.color.white));
                                    txtTitle.setTextSize(15f);
                                    txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                                    card.addView(txtTitle);

                                    TextView txtDetails = new TextView(this);
                                    txtDetails.setText(String.format(Locale.US, "🩸 %s - %d Units - %s - %s", bGroup, qty, comp, urgency));
                                    txtDetails.setTextColor(0xFF38BDF8);
                                    txtDetails.setTextSize(13f);
                                    card.addView(txtDetails);

                                    TextView txtStatus = new TextView(this);
                                    txtStatus.setText("Status: " + status + " - " + createdAt);
                                    txtStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                                    txtStatus.setTextSize(12f);
                                    card.addView(txtStatus);

                                    // Donor Response Info if Matched
                                    String matchedDonorName = doc.getString("donorName");
                                    String donorStatus = doc.getString("donorStatus");
                                    if (matchedDonorName != null && !matchedDonorName.isEmpty()) {
                                        TextView txtDonorResp = new TextView(this);
                                        txtDonorResp.setText("🩸 Matched Donor: " + matchedDonorName + " (" + (doc.getString("donorBloodGroup") != null ? doc.getString("donorBloodGroup") : bGroup) + ") - " + (donorStatus != null ? donorStatus : "AVAILABLE"));
                                        txtDonorResp.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                                        txtDonorResp.setTextSize(12f);
                                        txtDonorResp.setTypeface(null, android.graphics.Typeface.BOLD);
                                        LinearLayout.LayoutParams dRespLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        dRespLp.topMargin = 8;
                                        txtDonorResp.setLayoutParams(dRespLp);
                                        card.addView(txtDonorResp);
                                    }

                                    LinearLayout actionsRow = new LinearLayout(this);
                                    actionsRow.setOrientation(LinearLayout.HORIZONTAL);
                                    LinearLayout.LayoutParams actLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(42 * getResources().getDisplayMetrics().density));
                                    actLp.topMargin = 14;
                                    actionsRow.setLayoutParams(actLp);

                                    if (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Searching")) {
                                        TextView btnAllocate = new TextView(this);
                                        btnAllocate.setText("✔ Allocate & Reserve");
                                        btnAllocate.setTextColor(ContextCompat.getColor(this, R.color.white));
                                        btnAllocate.setBackgroundResource(R.drawable.bg_button_hospital);
                                        btnAllocate.setGravity(android.view.Gravity.CENTER);
                                        btnAllocate.setClickable(true);
                                        btnAllocate.setFocusable(true);
                                        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                                        aLp.rightMargin = 8;
                                        btnAllocate.setLayoutParams(aLp);
                                        btnAllocate.setOnClickListener(v -> performBloodAllocation(reqId, hospId, hospName, bGroup, qty, activeBankId, bankName));

                                        TextView btnReject = new TextView(this);
                                        btnReject.setText("✖ Reject");
                                        btnReject.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                                        btnReject.setBackgroundResource(R.drawable.bg_card_emergency);
                                        btnReject.setGravity(android.view.Gravity.CENTER);
                                        btnReject.setClickable(true);
                                        btnReject.setFocusable(true);
                                        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                                        rLp.leftMargin = 8;
                                        btnReject.setLayoutParams(rLp);
                                        btnReject.setOnClickListener(v -> {
                                            FirebaseFirestore.getInstance().collection("bloodRequests").document(reqId).update("status", "Rejected");
                                            Toast.makeText(this, "Request " + reqId + " marked as Rejected.", Toast.LENGTH_SHORT).show();
                                        });

                                        actionsRow.addView(btnAllocate);
                                        actionsRow.addView(btnReject);
                                        card.addView(actionsRow);
                                    } else if (status.equalsIgnoreCase("Allocated") || status.equalsIgnoreCase("Accepted")) {
                                        TextView btnDispatch = new TextView(this);
                                        btnDispatch.setText("🚀 Dispatch Courier");
                                        btnDispatch.setTextColor(ContextCompat.getColor(this, R.color.white));
                                        btnDispatch.setBackgroundResource(R.drawable.bg_button_primary);
                                        btnDispatch.setGravity(android.view.Gravity.CENTER);
                                        btnDispatch.setClickable(true);
                                        btnDispatch.setFocusable(true);
                                        LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                        btnDispatch.setLayoutParams(dLp);
                                        btnDispatch.setOnClickListener(v -> {
                                            FirebaseFirestore.getInstance().collection("bloodRequests").document(reqId).update("status", "In Transit");
                                            Toast.makeText(this, "Requisition " + reqId + " dispatched.", Toast.LENGTH_SHORT).show();
                                        });
                                        actionsRow.addView(btnDispatch);
                                        card.addView(actionsRow);
                                    }

                                    container.addView(card);
                                }
                                if (!hasMatches) {
                                    showEmpty.accept("No hospital requests yet.", "There are currently no active blood requisitions from hospitals assigned to your facility.");
                                } else {
                                    if (emptyState != null) emptyState.setVisibility(View.GONE);
                                }
                            });
                } else if (selectedBankTabIndex == 1) {
                    // Donor Appointments
                    FirebaseFirestore.getInstance().collection("donorAppointments")
                            .addSnapshotListener((snapshots, e) -> {
                                if (progress != null) progress.setVisibility(View.GONE);
                                if (container == null) return;
                                container.removeAllViews();

                                if (e != null || snapshots == null || snapshots.isEmpty()) {
                                    showEmpty.accept("No donor appointments yet.", "You don't have any upcoming blood donation appointments scheduled at your facility.");
                                    return;
                                }

                                List<DocumentSnapshot> bankAppts = new ArrayList<>();
                                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                    String bId = doc.getString("bloodBankId");
                                    String bName = doc.getString("bloodBankName");
                                    boolean isMatch = (activeBankId != null && activeBankId.equalsIgnoreCase(bId)) ||
                                                      (currentUid != null && currentUid.equalsIgnoreCase(bId)) ||
                                                      (bankName != null && bName != null && (bName.equalsIgnoreCase(bankName) || bName.toLowerCase(Locale.US).contains(bankName.toLowerCase(Locale.US)) || bankName.toLowerCase(Locale.US).contains(bName.toLowerCase(Locale.US))));
                                    if (isMatch) {
                                        bankAppts.add(doc);
                                    }
                                }

                                if (bankAppts.isEmpty()) {
                                    showEmpty.accept("No donor appointments yet.", "You don't have any upcoming blood donation appointments scheduled at your facility.");
                                    return;
                                }

                                if (emptyState != null) emptyState.setVisibility(View.GONE);

                                for (DocumentSnapshot doc : bankAppts) {
                                    final String aptId = doc.getId();
                                    final String donorUid = doc.getString("donorUid") != null ? doc.getString("donorUid") : "USR-01";
                                    final String donorName = doc.getString("donorName") != null ? doc.getString("donorName") : "Donor";
                                    final String donorPhone = doc.getString("donorPhone") != null ? doc.getString("donorPhone") : "+91 98000 00000";
                                    final String donorGroup = doc.getString("donorBloodGroup") != null ? doc.getString("donorBloodGroup") : (doc.getString("bloodGroup") != null ? doc.getString("bloodGroup") : "O+");
                                    final String aptDate = doc.getString("appointmentDate") != null ? doc.getString("appointmentDate") : (doc.getString("date") != null ? doc.getString("date") : "Today");
                                    final String aptTime = doc.getString("appointmentTime") != null ? doc.getString("appointmentTime") : (doc.getString("time") != null ? doc.getString("time") : "10:30 AM");
                                    final String status = doc.getString("status") != null ? doc.getString("status") : "PENDING";
                                    final String message = doc.getString("message") != null ? doc.getString("message") : (doc.getString("notes") != null ? doc.getString("notes") : "");

                                    LinearLayout card = new LinearLayout(this);
                                    card.setOrientation(LinearLayout.VERTICAL);
                                    card.setBackgroundResource(R.drawable.bg_card_premium);
                                    card.setPadding(30, 24, 30, 24);
                                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    lp.bottomMargin = 20;
                                    card.setLayoutParams(lp);

                                    TextView txtDonorTitle = new TextView(this);
                                    txtDonorTitle.setText("👤 " + donorName + " (" + donorGroup + ") - " + donorPhone);
                                    txtDonorTitle.setTextColor(ContextCompat.getColor(this, R.color.white));
                                    txtDonorTitle.setTextSize(15f);
                                    txtDonorTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                                    card.addView(txtDonorTitle);

                                    TextView txtSchedule = new TextView(this);
                                    txtSchedule.setText("📅 " + aptDate + " - " + aptTime + " - Status: " + status.toUpperCase(Locale.US));
                                    txtSchedule.setTextColor(0xFF38BDF8);
                                    txtSchedule.setTextSize(13f);
                                    card.addView(txtSchedule);

                                    if (!TextUtils.isEmpty(message)) {
                                        TextView txtMsg = new TextView(this);
                                        txtMsg.setText("💬 Notes: " + message);
                                        txtMsg.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                                        txtMsg.setTextSize(12f);
                                        card.addView(txtMsg);
                                    }

                                    if (status.equalsIgnoreCase("PENDING")) {
                                        LinearLayout actRow = new LinearLayout(this);
                                        actRow.setOrientation(LinearLayout.HORIZONTAL);
                                        LinearLayout.LayoutParams actLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 80);
                                        actLp.topMargin = 14;
                                        actRow.setLayoutParams(actLp);

                                        TextView btnAccept = new TextView(this);
                                        btnAccept.setText("✔ Accept Appointment");
                                        btnAccept.setTextColor(ContextCompat.getColor(this, R.color.white));
                                        btnAccept.setBackgroundResource(R.drawable.bg_button_hospital);
                                        btnAccept.setGravity(android.view.Gravity.CENTER);
                                        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                                        aLp.rightMargin = 8;
                                        btnAccept.setLayoutParams(aLp);
                                        btnAccept.setOnClickListener(v -> {
                                            FirebaseFirestore.getInstance().collection("donorAppointments").document(aptId).update("status", "CONFIRMED");

                                            String notifId = "NOTIF-DNR-CONF-" + System.currentTimeMillis();
                                            Map<String, Object> notif = new HashMap<>();
                                            notif.put("notificationId", notifId);
                                            notif.put("userId", donorUid);
                                            notif.put("donorId", donorUid);
                                            notif.put("recipientFacilityId", donorUid);
                                            notif.put("targetRole", "DONOR");
                                            notif.put("title", "Appointment Confirmed 🩸");
                                            notif.put("message", "Your blood donation appointment at " + bankName + " on " + aptDate + " at " + aptTime + " has been CONFIRMED.");
                                            notif.put("type", "APPOINTMENT_CONFIRMED");
                                            notif.put("relatedId", aptId);
                                            notif.put("timestamp", System.currentTimeMillis());
                                            notif.put("createdAt", System.currentTimeMillis());
                                            notif.put("isRead", false);
                                            notif.put("read", false);
                                            FirebaseFirestore.getInstance().collection("notifications").document(notifId).set(notif);

                                            Toast.makeText(this, "Appointment confirmed! Donor notified.", Toast.LENGTH_SHORT).show();
                                        });

                                        TextView btnReject = new TextView(this);
                                        btnReject.setText("✖ Reject");
                                        btnReject.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                                        btnReject.setBackgroundResource(R.drawable.bg_card_emergency);
                                        btnReject.setGravity(android.view.Gravity.CENTER);
                                        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                                        rLp.leftMargin = 8;
                                        btnReject.setLayoutParams(rLp);
                                        btnReject.setOnClickListener(v -> {
                                            FirebaseFirestore.getInstance().collection("donorAppointments").document(aptId).update("status", "REJECTED");
                                            Toast.makeText(this, "Appointment " + aptId + " rejected.", Toast.LENGTH_SHORT).show();
                                        });

                                        actRow.addView(btnAccept);
                                        actRow.addView(btnReject);
                                        card.addView(actRow);
                                    } else if (status.equalsIgnoreCase("CONFIRMED")) {
                                        TextView btnComplete = new TextView(this);
                                        btnComplete.setText("🩸 Complete Blood Donation (+1 Unit Stock)");
                                        btnComplete.setTextColor(ContextCompat.getColor(this, R.color.white));
                                        btnComplete.setBackgroundResource(R.drawable.bg_button_primary);
                                        btnComplete.setGravity(android.view.Gravity.CENTER);
                                        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 80);
                                        cLp.topMargin = 14;
                                        btnComplete.setLayoutParams(cLp);
                                        btnComplete.setOnClickListener(v -> {
                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                            DocumentReference aptRef = db.collection("donorAppointments").document(aptId);
                                            DocumentReference bankRef = db.collection("bloodBanks").document(activeBankId);
                                            DocumentReference donorUserRef = db.collection("users").document(donorUid);

                                            db.runTransaction(transaction -> {
                                                DocumentSnapshot bankSnap = transaction.get(bankRef);
                                                Map<String, Integer> stockMap = extractStockMapFromDoc(bankSnap);
                                                String canGroup = getCanonicalBloodGroup(donorGroup);
                                                int curStock = getNumericStock(stockMap, canGroup);
                                                stockMap.put(canGroup, curStock + 1);

                                                int total = 0;
                                                for (int s : stockMap.values()) total += s;

                                                transaction.update(aptRef, "status", "COMPLETED");
                                                transaction.update(bankRef, "bloodStock", stockMap);
                                                transaction.update(bankRef, "totalUnits", total);
                                                transaction.update(bankRef, "updatedAt", System.currentTimeMillis());

                                                transaction.update(donorUserRef, "totalDonations", FieldValue.increment(1));
                                                transaction.update(donorUserRef, "lastDonationDate", aptDate);
                                                return null;
                                            }).addOnSuccessListener(avoid -> {
                                                String notifId = "NOTIF-DNR-COMP-" + System.currentTimeMillis();
                                                Map<String, Object> notif = new HashMap<>();
                                                notif.put("notificationId", notifId);
                                                notif.put("userId", donorUid);
                                                notif.put("donorId", donorUid);
                                                notif.put("recipientFacilityId", donorUid);
                                                notif.put("targetRole", "DONOR");
                                                notif.put("title", "🎉 Donation Completed! Thank You!");
                                                notif.put("message", "Thank you for donating blood at " + bankName + ". Your contribution has been recorded in your Lifesaving Impact!");
                                                notif.put("type", "DONATION_COMPLETED");
                                                notif.put("relatedId", aptId);
                                                notif.put("timestamp", System.currentTimeMillis());
                                                notif.put("createdAt", System.currentTimeMillis());
                                                notif.put("isRead", false);
                                                notif.put("read", false);
                                                FirebaseFirestore.getInstance().collection("notifications").document(notifId).set(notif);

                                                Toast.makeText(this, "✔ Donation Completed! " + donorGroup + " stock increased by 1 unit.", Toast.LENGTH_LONG).show();
                                            }).addOnFailureListener(err -> {
                                                // Fallback update if bloodBank document ID doesn't exist
                                                aptRef.update("status", "COMPLETED");
                                                donorUserRef.update("totalDonations", FieldValue.increment(1), "lastDonationDate", aptDate);
                                                Toast.makeText(this, "✔ Donation Completed!", Toast.LENGTH_SHORT).show();
                                            });
                                        });
                                        card.addView(btnComplete);
                                    }

                                    container.addView(card);
                                }
                            });
                } else {
                    // Donation Camps Associated with this Blood Bank
                    FirebaseFirestore.getInstance().collection("bloodCamps")
                            .whereEqualTo("bloodBankId", activeBankId)
                            .addSnapshotListener((snapshots, e) -> {
                                if (progress != null) progress.setVisibility(View.GONE);
                                if (container == null) return;
                                container.removeAllViews();

                                if (e != null || snapshots == null || snapshots.isEmpty()) {
                                    showEmpty.accept("No donation camps yet.", "You haven't organized or registered any blood donation camps yet.");
                                    return;
                                }

                                if (emptyState != null) emptyState.setVisibility(View.GONE);

                                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                    final String campId = doc.getId();
                                    String name = doc.getString("campName") != null ? doc.getString("campName") : "Blood Drive";
                                    String organizer = doc.getString("organizer") != null ? doc.getString("organizer") : "Facility Drive";
                                    String date = doc.getString("date") != null ? doc.getString("date") : "Upcoming";
                                    String time = (doc.getString("startTime") != null ? doc.getString("startTime") : "09:00 AM") + " - " + (doc.getString("endTime") != null ? doc.getString("endTime") : "04:00 PM");
                                    String location = doc.getString("address") != null ? doc.getString("address") : "Sangli";
                                    Long totalSlots = doc.getLong("totalSlots");
                                    Long availSlots = doc.getLong("availableSlots");
                                    int tSlots = totalSlots != null ? totalSlots.intValue() : 50;
                                    int aSlots = availSlots != null ? availSlots.intValue() : 50;
                                    int regCount = tSlots - aSlots;
                                    String status = doc.getString("status") != null ? doc.getString("status") : "ACTIVE";

                                    LinearLayout card = new LinearLayout(this);
                                    card.setOrientation(LinearLayout.VERTICAL);
                                    card.setBackgroundResource(R.drawable.bg_card_premium);
                                    card.setPadding(30, 24, 30, 24);
                                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    lp.bottomMargin = 20;
                                    card.setLayoutParams(lp);

                                    TextView txtTitle = new TextView(this);
                                    txtTitle.setText("⛺ " + name);
                                    txtTitle.setTextColor(ContextCompat.getColor(this, R.color.white));
                                    txtTitle.setTextSize(15f);
                                    txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                                    card.addView(txtTitle);

                                    TextView txtOrg = new TextView(this);
                                    txtOrg.setText("🏛 Organized by: " + organizer + "  -  Status: " + status);
                                    txtOrg.setTextColor(0xFF38BDF8);
                                    txtOrg.setTextSize(13f);
                                    card.addView(txtOrg);

                                    TextView txtDate = new TextView(this);
                                    txtDate.setText("📅 " + date + "  |  ⏰ " + time);
                                    txtDate.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                                    txtDate.setTextSize(12f);
                                    card.addView(txtDate);

                                    TextView txtLoc = new TextView(this);
                                    txtLoc.setText("📍 " + location);
                                    txtLoc.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                                    txtLoc.setTextSize(12f);
                                    card.addView(txtLoc);

                                    TextView txtSlots = new TextView(this);
                                    txtSlots.setText(String.format(Locale.US, "👥 Registrations: %d  |  🟢 Available: %d / %d", regCount, aSlots, tSlots));
                                    txtSlots.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                                    txtSlots.setTextSize(12f);
                                    txtSlots.setTypeface(null, android.graphics.Typeface.BOLD);
                                    LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    sLp.topMargin = 8;
                                    txtSlots.setLayoutParams(sLp);
                                    card.addView(txtSlots);

                                    container.addView(card);
                                }
                            });
                }
            };

            if (tabBankHosp != null) tabBankHosp.setOnClickListener(v -> { selectedBankTabIndex = 0; updateBankTabUI.run(); });
            if (tabBankDonors != null) tabBankDonors.setOnClickListener(v -> { selectedBankTabIndex = 1; updateBankTabUI.run(); });
            if (tabBankCamps != null) tabBankCamps.setOnClickListener(v -> { selectedBankTabIndex = 2; updateBankTabUI.run(); });

            updateBankTabUI.run();
        } else {
            // DONOR Role
            if (bankTabsLayout != null) bankTabsLayout.setVisibility(View.GONE);
            if (txtPageTitle != null) txtPageTitle.setText("My Appointments & Requests");
            if (txtSubtitle != null) txtSubtitle.setText("Your scheduled blood donation appointments and requisitions");
            if (btnCreate != null) {
                btnCreate.setVisibility(View.VISIBLE);
                btnCreate.setText("+ Book Donation");
                btnCreate.setOnClickListener(v -> showBookDonationDialog());
            }

            if (progress != null) progress.setVisibility(View.VISIBLE);
            FirebaseFirestore.getInstance().collection("donorAppointments")
                    .whereEqualTo("donorUid", currentUid)
                    .addSnapshotListener((snapshots, e) -> {
                        if (progress != null) progress.setVisibility(View.GONE);
                        if (container == null) return;
                        container.removeAllViews();

                        if (e != null || snapshots == null || snapshots.isEmpty()) {
                            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                            return;
                        }

                        if (emptyState != null) emptyState.setVisibility(View.GONE);

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            final String aptId = doc.getId();
                            String bName = doc.getString("bloodBankName") != null ? doc.getString("bloodBankName") : "Blood Bank";
                            String aDate = doc.getString("appointmentDate") != null ? doc.getString("appointmentDate") : "Scheduled";
                            String aTime = doc.getString("appointmentTime") != null ? doc.getString("appointmentTime") : "10:30 AM";
                            final String status = doc.getString("status") != null ? doc.getString("status") : "PENDING";
                            String msg = doc.getString("message") != null ? doc.getString("message") : "";

                            LinearLayout card = new LinearLayout(this);
                            card.setOrientation(LinearLayout.VERTICAL);
                            card.setBackgroundResource(R.drawable.bg_card_donor);
                            card.setPadding(30, 24, 30, 24);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            lp.bottomMargin = 20;
                            card.setLayoutParams(lp);

                            TextView txtBank = new TextView(this);
                            txtBank.setText("🏥 " + bName);
                            txtBank.setTextColor(ContextCompat.getColor(this, R.color.white));
                            txtBank.setTextSize(15f);
                            txtBank.setTypeface(null, android.graphics.Typeface.BOLD);
                            card.addView(txtBank);

                            TextView txtSchedule = new TextView(this);
                            txtSchedule.setText("📅 " + aDate + " at " + aTime + " - Status: " + status.toUpperCase(Locale.US));
                            txtSchedule.setTextColor(0xFF38BDF8);
                            txtSchedule.setTextSize(13f);
                            card.addView(txtSchedule);

                            if (!msg.isEmpty()) {
                                TextView txtNotes = new TextView(this);
                                txtNotes.setText("Notes: " + msg);
                                txtNotes.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                                txtNotes.setTextSize(12f);
                                card.addView(txtNotes);
                            }

                            if (status.equalsIgnoreCase("PENDING") || status.equalsIgnoreCase("CONFIRMED")) {
                                TextView btnCancel = new TextView(this);
                                btnCancel.setText("Cancel Appointment");
                                btnCancel.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                                btnCancel.setBackgroundResource(R.drawable.bg_card_emergency);
                                btnCancel.setGravity(android.view.Gravity.CENTER);
                                LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 76);
                                cLp.topMargin = 14;
                                btnCancel.setLayoutParams(cLp);
                                btnCancel.setOnClickListener(v -> {
                                    new AlertDialog.Builder(this)
                                        .setTitle("Cancel Appointment")
                                        .setMessage("Are you sure you want to cancel this appointment?")
                                        .setPositiveButton("Yes, Cancel", (d, w) -> {
                                            FirebaseFirestore.getInstance().collection("donorAppointments").document(aptId).update("status", "CANCELLED");
                                            Toast.makeText(this, "Appointment cancelled.", Toast.LENGTH_SHORT).show();
                                        })
                                        .setNegativeButton("No", null)
                                        .show();
                                });
                                card.addView(btnCancel);
                            }

                            card.setOnClickListener(v -> showAppointmentDetailsDialog(doc));
                            container.addView(card);
                        }
                    });
        }
    }

    private void performBloodAllocation(String reqId, String hospId, String hospName, String bGroup, int qty, String bankUid, String bankName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("bloodBanks").get().addOnSuccessListener(bankSnapshots -> {
            DocumentSnapshot targetBankDoc = null;
            if (bankSnapshots != null) {
                for (DocumentSnapshot doc : bankSnapshots) {
                    String docId = doc.getId();
                    String userId = doc.getString("userId");
                    String name = doc.getString("name");
                    if (docId.equalsIgnoreCase(bankUid) || (userId != null && userId.equalsIgnoreCase(bankUid)) || (name != null && name.equalsIgnoreCase(bankName))) {
                        targetBankDoc = doc;
                        break;
                    }
                }
                if (targetBankDoc == null && !bankSnapshots.isEmpty()) {
                    targetBankDoc = bankSnapshots.getDocuments().get(0);
                }
            }

            if (targetBankDoc == null) {
                Toast.makeText(this, "Blood bank facility record not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            DocumentReference bankDocRef = targetBankDoc.getReference();
            DocumentReference reqDocRef = db.collection("bloodRequests").document(reqId);
            String finalDocId = targetBankDoc.getId();

            db.runTransaction(transaction -> {
                DocumentSnapshot currentBankSnap = transaction.get(bankDocRef);
                DocumentSnapshot currentReqSnap = transaction.get(reqDocRef);

                if (!currentReqSnap.exists()) {
                    throw new FirebaseFirestoreException("Requisition not found in system", FirebaseFirestoreException.Code.NOT_FOUND);
                }

                String currentReqStatus = currentReqSnap.getString("status");
                if (currentReqStatus != null && currentReqStatus.toUpperCase(Locale.US).contains("ALLOCAT")) {
                    throw new FirebaseFirestoreException("ALREADY_ALLOCATED", FirebaseFirestoreException.Code.ABORTED);
                }

                Map<String, Integer> stockMap = extractStockMapFromDoc(currentBankSnap);
                String canonicalGroup = getCanonicalBloodGroup(bGroup);
                int availableUnits = getNumericStock(stockMap, canonicalGroup);

                if (availableUnits < qty) {
                    throw new FirebaseFirestoreException("INSUFFICIENT_STOCK:" + availableUnits, FirebaseFirestoreException.Code.ABORTED);
                }

                int newStock = availableUnits - qty;
                stockMap.put(canonicalGroup, newStock);

                // Update Blood Bank Stock in Firestore
                transaction.update(bankDocRef, "bloodStock", stockMap);
                transaction.update(bankDocRef, "updatedAt", System.currentTimeMillis());

                // Update Request Status in Firestore
                String timeStr = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());
                transaction.update(reqDocRef, "status", "ALLOCATED");
                transaction.update(reqDocRef, "allocatedQuantity", qty);
                transaction.update(reqDocRef, "allocatedAt", timeStr);
                transaction.update(reqDocRef, "allocatedTimestamp", System.currentTimeMillis());
                transaction.update(reqDocRef, "allocatedBy", bankUid);
                transaction.update(reqDocRef, "bloodBankId", bankUid);
                transaction.update(reqDocRef, "bloodBankName", bankName);
                transaction.update(reqDocRef, "assignedSource", bankName);
                transaction.update(reqDocRef, "escalationStage", 2);
                transaction.update(reqDocRef, "updatedAt", System.currentTimeMillis());

                return newStock;
            }).addOnSuccessListener(newStock -> {
                String timeStr = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());

                // Update in-memory cache
                String canonicalGroup = getCanonicalBloodGroup(bGroup);
                for (SmartMapItem item : smartMapItemList) {
                    if (item.id.equalsIgnoreCase(finalDocId) || item.id.equalsIgnoreCase(bankUid) || (item.name != null && item.name.equalsIgnoreCase(bankName))) {
                        if (item.stockMap == null) item.stockMap = new HashMap<>();
                        item.stockMap.put(canonicalGroup, (Integer) newStock);
                        break;
                    }
                }

                // Notification for Hospital
                Map<String, Object> notif = new HashMap<>();
                notif.put("hospitalId", hospId);
                notif.put("title", "Blood Units Allocated & Reserved");
                notif.put("message", bankName + " has allocated & reserved " + qty + " units of " + bGroup + " for requisition " + reqId + ".");
                notif.put("type", "ALLOCATED");
                notif.put("timestamp", timeStr);
                notif.put("createdAt", System.currentTimeMillis());
                notif.put("isRead", false);
                db.collection("notifications").add(notif);

                repository.addAuditLog(bankUid, "BLOOD_BANK", "ALLOCATE_STOCK", reqId, timeStr, "VERIFIED", "ALLOCATED", "Allocated " + qty + " units " + bGroup + " from " + bankName + " for " + hospName + ". Remaining " + bGroup + " stock: " + newStock + "u");
                Toast.makeText(this, "✓ " + qty + " units of " + bGroup + " allocated for " + reqId + "!\nRemaining stock: " + newStock + " Units", Toast.LENGTH_LONG).show();
            }).addOnFailureListener(err -> {
                String msg = err.getMessage() != null ? err.getMessage() : "";
                if (msg.contains("ALREADY_ALLOCATED")) {
                    Toast.makeText(this, "Blood already allocated for this request.", Toast.LENGTH_SHORT).show();
                } else if (msg.contains("INSUFFICIENT_STOCK")) {
                    String avail = msg.substring(msg.indexOf(":") + 1).trim();
                    Toast.makeText(this, "⚠️ Insufficient " + bGroup + " stock (" + avail + " units available, " + qty + " requested).", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Allocation failed: " + msg, Toast.LENGTH_SHORT).show();
                }
            });
        }).addOnFailureListener(err -> {
            Toast.makeText(this, "Error accessing blood bank stock: " + err.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void performBloodDispatch(String reqId, String hospId, String hospName, String bGroup, int qty, String bankUid, String bankName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reqRef = db.collection("bloodRequests").document(reqId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snap = transaction.get(reqRef);
            if (!snap.exists()) {
                throw new FirebaseFirestoreException("Request not found", FirebaseFirestoreException.Code.NOT_FOUND);
            }
            String curStatus = snap.getString("status");
            if (curStatus != null && (curStatus.toUpperCase(Locale.US).contains("TRANSIT") || curStatus.toUpperCase(Locale.US).contains("DISPATCH"))) {
                throw new FirebaseFirestoreException("ALREADY_DISPATCHED", FirebaseFirestoreException.Code.ABORTED);
            }
            if (curStatus != null && (curStatus.toUpperCase(Locale.US).contains("FULFIL") || curStatus.toUpperCase(Locale.US).contains("COMPLET"))) {
                throw new FirebaseFirestoreException("ALREADY_FULFILLED", FirebaseFirestoreException.Code.ABORTED);
            }

            String timeStr = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());
            transaction.update(reqRef, "status", "IN_TRANSIT");
            transaction.update(reqRef, "dispatchedAt", timeStr);
            transaction.update(reqRef, "dispatchedTimestamp", System.currentTimeMillis());
            transaction.update(reqRef, "dispatchedBy", bankUid);
            transaction.update(reqRef, "bloodBankId", bankUid);
            transaction.update(reqRef, "bloodBankName", bankName);
            transaction.update(reqRef, "assignedSource", bankName);
            transaction.update(reqRef, "escalationStage", 3);
            transaction.update(reqRef, "updatedAt", System.currentTimeMillis());
            return timeStr;
        }).addOnSuccessListener(timeStr -> {
            // Notification for Hospital
            Map<String, Object> notif = new HashMap<>();
            notif.put("hospitalId", hospId);
            notif.put("title", "Blood Courier Dispatched");
            notif.put("message", "Your " + bGroup + " (" + qty + " Units) requisition " + reqId + " has been dispatched by " + bankName + " and is in transit.");
            notif.put("type", "DISPATCHED");
            notif.put("timestamp", (String) timeStr);
            notif.put("createdAt", System.currentTimeMillis());
            notif.put("isRead", false);
            db.collection("notifications").add(notif);

            repository.addAuditLog(bankUid, "BLOOD_BANK", "DISPATCH_REQUEST", reqId, (String) timeStr, "ALLOCATED", "IN_TRANSIT", "Dispatched " + qty + " units " + bGroup + " to " + hospName);
            Toast.makeText(this, "✓ Requisition " + reqId + " Dispatched & In Transit!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(err -> {
            String msg = err.getMessage() != null ? err.getMessage() : "";
            if (msg.contains("ALREADY_DISPATCHED")) {
                Toast.makeText(this, "Requisition is already in transit.", Toast.LENGTH_SHORT).show();
            } else if (msg.contains("ALREADY_FULFILLED")) {
                Toast.makeText(this, "Requisition is already fulfilled.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Dispatch failed: " + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performHospitalDeliveryConfirmation(String reqId, String hospId, String hospName, String bGroup, int qty, String assignedBank) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reqRef = db.collection("bloodRequests").document(reqId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snap = transaction.get(reqRef);
            if (!snap.exists()) {
                throw new FirebaseFirestoreException("Request not found", FirebaseFirestoreException.Code.NOT_FOUND);
            }
            String curStatus = snap.getString("status");
            if (curStatus != null && (curStatus.toUpperCase(Locale.US).contains("FULFIL") || curStatus.toUpperCase(Locale.US).contains("COMPLET"))) {
                throw new FirebaseFirestoreException("ALREADY_FULFILLED", FirebaseFirestoreException.Code.ABORTED);
            }

            String timeStr = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());
            transaction.update(reqRef, "status", "FULFILLED");
            transaction.update(reqRef, "fulfilledAt", timeStr);
            transaction.update(reqRef, "fulfilledTimestamp", System.currentTimeMillis());
            transaction.update(reqRef, "fulfilledBy", hospId);
            transaction.update(reqRef, "escalationStage", 4);
            transaction.update(reqRef, "updatedAt", System.currentTimeMillis());
            return timeStr;
        }).addOnSuccessListener(timeStr -> {
            // Notification for Hospital
            Map<String, Object> notif = new HashMap<>();
            notif.put("hospitalId", hospId);
            notif.put("title", "Requisition Completed & Fulfilled");
            notif.put("message", "Requisition " + reqId + " (" + qty + " Units of " + bGroup + ") from " + assignedBank + " has been successfully delivered and confirmed.");
            notif.put("type", "FULFILLED");
            notif.put("timestamp", (String) timeStr);
            notif.put("createdAt", System.currentTimeMillis());
            notif.put("isRead", false);
            db.collection("notifications").add(notif);

            repository.addAuditLog(hospId, "HOSPITAL", "CONFIRM_DELIVERY", reqId, (String) timeStr, "IN_TRANSIT", "FULFILLED", "Confirmed delivery of " + qty + " units " + bGroup + " from " + assignedBank);
            Toast.makeText(this, "✓ Requisition " + reqId + " Confirmed & Fulfilled!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(err -> {
            String msg = err.getMessage() != null ? err.getMessage() : "";
            if (msg.contains("ALREADY_FULFILLED")) {
                Toast.makeText(this, "Request already fulfilled.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Confirmation failed: " + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String currentTransfersFilter = "ALL";

    private void bindTransfersView(View view) {
        attachRoleBackgroundAnimators(view);

        UserProfile currentUser = repository.getCurrentUser();
        String bankName = currentUser != null && currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Blood Bank";
        String bankUid = currentUser != null ? currentUser.getUid() : "BB-101";
        String currentBankId = currentUser != null && currentUser.getBloodBankId() != null ? currentUser.getBloodBankId() : bankUid;

        TextView btnInitiate = view.findViewById(R.id.btn_initiate_transfer);
        if (btnInitiate != null) {
            btnInitiate.setOnClickListener(v -> showInitiateStockTransferDialog(null, null, 0));
        }

        LinearLayout recommendationsContainer = view.findViewById(R.id.layout_recommendations_container);
        TextView txtRecsEmpty = view.findViewById(R.id.txt_recommendations_empty);

        // Generate dynamic AI Redistribution Recommendations
        generateTransferRecommendations(bankUid, bankName, recommendationsContainer, txtRecsEmpty);

        TextView chipAll = view.findViewById(R.id.chip_transfers_all);
        TextView chipPending = view.findViewById(R.id.chip_transfers_pending);
        TextView chipCompleted = view.findViewById(R.id.chip_transfers_completed);

        ProgressBar progress = view.findViewById(R.id.loading_transfers_progress);
        LinearLayout container = view.findViewById(R.id.layout_transfers_container);
        View emptyState = view.findViewById(R.id.layout_transfers_empty_state);

        final List<DocumentSnapshot> allTransferDocs = new ArrayList<>();

        Runnable updateChips = () -> {
            if (chipAll != null) {
                chipAll.setBackgroundResource(currentTransfersFilter.equals("ALL") ? R.drawable.bg_button_hospital : R.drawable.bg_chip_hospital);
                chipAll.setTextColor(ContextCompat.getColor(this, currentTransfersFilter.equals("ALL") ? R.color.white : R.color.text_secondary));
            }
            if (chipPending != null) {
                chipPending.setBackgroundResource(currentTransfersFilter.equals("PENDING") ? R.drawable.bg_button_hospital : R.drawable.bg_chip_hospital);
                chipPending.setTextColor(ContextCompat.getColor(this, currentTransfersFilter.equals("PENDING") ? R.color.white : R.color.text_secondary));
            }
            if (chipCompleted != null) {
                chipCompleted.setBackgroundResource(currentTransfersFilter.equals("COMPLETED") ? R.drawable.bg_button_hospital : R.drawable.bg_chip_hospital);
                chipCompleted.setTextColor(ContextCompat.getColor(this, currentTransfersFilter.equals("COMPLETED") ? R.color.white : R.color.text_secondary));
            }
        };

        Runnable renderTransfers = () -> {
            if (container == null) return;
            container.removeAllViews();

            List<DocumentSnapshot> validTransfers = new ArrayList<>();
            for (DocumentSnapshot doc : allTransferDocs) {
                String status = doc.getString("status");
                String statusUpper = status != null ? status.toUpperCase(Locale.US) : "PENDING_APPROVAL";
                String srcId = doc.getString("sourceBloodBankId") != null ? doc.getString("sourceBloodBankId") : "";
                String destId = doc.getString("destinationBloodBankId") != null ? doc.getString("destinationBloodBankId") : "";
                String srcName = doc.getString("sourceBloodBankName") != null ? doc.getString("sourceBloodBankName") : "";
                String destName = doc.getString("destinationBloodBankName") != null ? doc.getString("destinationBloodBankName") : "";

                boolean isDestinationForMe = (destId.equalsIgnoreCase(currentBankId) || destId.equalsIgnoreCase(bankUid) || destName.equalsIgnoreCase(bankName));
                boolean isSourceForMe = (srcId.equalsIgnoreCase(currentBankId) || srcId.equalsIgnoreCase(bankUid) || srcName.equalsIgnoreCase(bankName));

                if (!isDestinationForMe && !isSourceForMe) {
                    String bNameLower = bankName.toLowerCase(Locale.US);
                    String sNameLower = srcName.toLowerCase(Locale.US);
                    String dNameLower = destName.toLowerCase(Locale.US);
                    String sIdLower = srcId.toLowerCase(Locale.US);
                    String dIdLower = destId.toLowerCase(Locale.US);

                    if (bNameLower.contains("msi")) {
                        if (sNameLower.contains("msi") || sIdLower.contains("msi") || sIdLower.contains("bb-msi")) isSourceForMe = true;
                        if (dNameLower.contains("msi") || dIdLower.contains("msi") || dIdLower.contains("bb-msi")) isDestinationForMe = true;
                    } else if (bNameLower.contains("bombay")) {
                        if (sNameLower.contains("bombay") || sIdLower.contains("bombay") || sIdLower.contains("bb-bombay")) isSourceForMe = true;
                        if (dNameLower.contains("bombay") || dIdLower.contains("bombay") || dIdLower.contains("bb-bombay")) isDestinationForMe = true;
                    } else if (bNameLower.contains("shashwat")) {
                        if (sNameLower.contains("shashwat") || sIdLower.contains("shashwat") || sIdLower.contains("bb-shashwat")) isSourceForMe = true;
                        if (dNameLower.contains("shashwat") || dIdLower.contains("shashwat") || dIdLower.contains("bb-shashwat")) isDestinationForMe = true;
                    } else if (bNameLower.contains("civil")) {
                        if (sNameLower.contains("civil") || sIdLower.contains("civil") || sIdLower.contains("bb-sangli-civil")) isSourceForMe = true;
                        if (dNameLower.contains("civil") || dIdLower.contains("civil") || dIdLower.contains("bb-sangli-civil")) isDestinationForMe = true;
                    }
                }

                if (!isDestinationForMe && !isSourceForMe) {
                    continue;
                }

                if (currentTransfersFilter.equals("PENDING")) {
                    if (!isDestinationForMe || !statusUpper.contains("PENDING")) {
                        continue;
                    }
                } else if (currentTransfersFilter.equals("COMPLETED")) {
                    if (!statusUpper.contains("COMPLET") && !statusUpper.contains("APPROV")) {
                        continue;
                    }
                }

                validTransfers.add(doc);
            }

            if (validTransfers.isEmpty()) {
                if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                return;
            }

            if (emptyState != null) emptyState.setVisibility(View.GONE);

            Collections.sort(validTransfers, (d1, d2) -> {
                boolean dest1 = (d1.getString("destinationBloodBankId") != null && (d1.getString("destinationBloodBankId").equalsIgnoreCase(currentBankId) || d1.getString("destinationBloodBankId").equalsIgnoreCase(bankUid))) ||
                                (d1.getString("destinationBloodBankName") != null && d1.getString("destinationBloodBankName").equalsIgnoreCase(bankName));
                boolean dest2 = (d2.getString("destinationBloodBankId") != null && (d2.getString("destinationBloodBankId").equalsIgnoreCase(currentBankId) || d2.getString("destinationBloodBankId").equalsIgnoreCase(bankUid))) ||
                                (d2.getString("destinationBloodBankName") != null && d2.getString("destinationBloodBankName").equalsIgnoreCase(bankName));

                String s1 = d1.getString("status");
                String s2 = d2.getString("status");
                boolean p1 = s1 != null && s1.toUpperCase(Locale.US).contains("PENDING");
                boolean p2 = s2 != null && s2.toUpperCase(Locale.US).contains("PENDING");

                if (dest1 && p1 && !(dest2 && p2)) return -1;
                if (!(dest1 && p1) && (dest2 && p2)) return 1;

                Long t1 = d1.getLong("createdAtTimestamp");
                Long t2 = d2.getLong("createdAtTimestamp");
                if (t1 == null) t1 = 0L;
                if (t2 == null) t2 = 0L;
                return Long.compare(t2, t1);
            });

            for (DocumentSnapshot doc : validTransfers) {
                String trfId = doc.getString("transferId") != null ? doc.getString("transferId") : doc.getId();
                String srcId = doc.getString("sourceBloodBankId") != null ? doc.getString("sourceBloodBankId") : "";
                String srcName = doc.getString("sourceBloodBankName") != null ? doc.getString("sourceBloodBankName") : "Source Reserve";
                String destId = doc.getString("destinationBloodBankId") != null ? doc.getString("destinationBloodBankId") : "";
                String destName = doc.getString("destinationBloodBankName") != null ? doc.getString("destinationBloodBankName") : "Destination Reserve";
                String bGroup = doc.getString("bloodGroup") != null ? doc.getString("bloodGroup") : "O+";
                String comp = doc.getString("component") != null ? doc.getString("component") : "Packed RBC";
                Long qtyLong = doc.getLong("quantity");
                int qty = qtyLong != null ? qtyLong.intValue() : 2;
                String status = doc.getString("status") != null ? doc.getString("status") : "PENDING_APPROVAL";
                String createdAt = doc.getString("createdAt") != null ? doc.getString("createdAt") : "Today";
                String notes = doc.getString("notes") != null ? doc.getString("notes") : "";

                boolean isDestinationForMe = (destId.equalsIgnoreCase(currentBankId) || destId.equalsIgnoreCase(bankUid) || destName.equalsIgnoreCase(bankName));
                boolean isSourceForMe = (srcId.equalsIgnoreCase(currentBankId) || srcId.equalsIgnoreCase(bankUid) || srcName.equalsIgnoreCase(bankName));

                if (!isDestinationForMe && !isSourceForMe) {
                    String bNameLower = bankName.toLowerCase(Locale.US);
                    String sNameLower = srcName.toLowerCase(Locale.US);
                    String dNameLower = destName.toLowerCase(Locale.US);
                    if (bNameLower.contains("msi")) {
                        if (sNameLower.contains("msi")) isSourceForMe = true;
                        if (dNameLower.contains("msi")) isDestinationForMe = true;
                    } else if (bNameLower.contains("bombay")) {
                        if (sNameLower.contains("bombay")) isSourceForMe = true;
                        if (dNameLower.contains("bombay")) isDestinationForMe = true;
                    } else if (bNameLower.contains("shashwat")) {
                        if (sNameLower.contains("shashwat")) isSourceForMe = true;
                        if (dNameLower.contains("shashwat")) isDestinationForMe = true;
                    } else if (bNameLower.contains("civil")) {
                        if (sNameLower.contains("civil")) isSourceForMe = true;
                        if (dNameLower.contains("civil")) isDestinationForMe = true;
                    }
                }

                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackgroundResource(R.drawable.bg_card_hospital);
                card.setPadding(32, 28, 32, 28);
                LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                cardLp.setMargins(0, 0, 0, 24);
                card.setLayoutParams(cardLp);

                // Top Row: Source ➔ Destination + Status Badge
                RelativeLayout topRow = new RelativeLayout(this);
                topRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                TextView txtRoute = new TextView(this);
                txtRoute.setText("🏥 " + srcName + " → " + destName);
                txtRoute.setTextColor(ContextCompat.getColor(this, R.color.white));
                txtRoute.setTextSize(14f);
                txtRoute.setTypeface(null, android.graphics.Typeface.BOLD);
                RelativeLayout.LayoutParams routeLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                routeLp.addRule(RelativeLayout.ALIGN_PARENT_START);
                routeLp.addRule(RelativeLayout.CENTER_VERTICAL);
                txtRoute.setLayoutParams(routeLp);

                TextView txtStatus = new TextView(this);
                String statusUpper = status.toUpperCase(Locale.US);
                txtStatus.setTextSize(11f);
                txtStatus.setTypeface(null, android.graphics.Typeface.BOLD);
                txtStatus.setPadding(18, 6, 18, 6);

                if (statusUpper.contains("PENDING")) {
                    txtStatus.setText("PENDING APPROVAL");
                    txtStatus.setBackgroundResource(R.drawable.bg_chip_status_pending);
                    txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_low_text));
                } else if (statusUpper.contains("APPROV") || statusUpper.contains("COMPLET")) {
                    txtStatus.setText("COMPLETED");
                    txtStatus.setBackgroundResource(R.drawable.bg_chip_status_available);
                    txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                } else if (statusUpper.contains("DECLIN") || statusUpper.contains("REJECT")) {
                    txtStatus.setText("DECLINED");
                    txtStatus.setBackgroundResource(R.drawable.bg_chip_status_critical);
                    txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                } else {
                    txtStatus.setText("CANCELLED");
                    txtStatus.setBackgroundResource(R.drawable.bg_chip_status_pending);
                    txtStatus.setTextColor(ContextCompat.getColor(this, R.color.text_tertiary));
                }

                RelativeLayout.LayoutParams stLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                stLp.addRule(RelativeLayout.ALIGN_PARENT_END);
                stLp.addRule(RelativeLayout.CENTER_VERTICAL);
                txtStatus.setLayoutParams(stLp);

                topRow.addView(txtRoute);
                topRow.addView(txtStatus);
                card.addView(topRow);

                // Units & Blood Group Row
                TextView txtDetails = new TextView(this);
                txtDetails.setText(String.format(Locale.US, "🩸 %s  -  %d Units  -  %s", bGroup, qty, comp));
                txtDetails.setTextColor(0xFF38BDF8);
                txtDetails.setTextSize(16f);
                txtDetails.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams dtLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dtLp.topMargin = 10;
                txtDetails.setLayoutParams(dtLp);
                card.addView(txtDetails);

                // Time, ID & Role context
                TextView txtInfo = new TextView(this);
                String roleContext = isDestinationForMe ? " (Incoming Transfer to You)" : (isSourceForMe ? " (Outgoing from You)" : "");
                String noteStr = !notes.isEmpty() ? "  -  " + notes : "";
                txtInfo.setText("Transfer ID: " + trfId + "  -  " + createdAt + roleContext + noteStr);
                txtInfo.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                txtInfo.setTextSize(12f);
                LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                infoLp.topMargin = 6;
                txtInfo.setLayoutParams(infoLp);
                card.addView(txtInfo);

                // Action Buttons
                if (statusUpper.contains("PENDING")) {
                    if (isDestinationForMe) {
                        LinearLayout actRow = new LinearLayout(this);
                        actRow.setOrientation(LinearLayout.HORIZONTAL);
                        LinearLayout.LayoutParams actLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 84);
                        actLp.topMargin = 16;
                        actRow.setLayoutParams(actLp);

                        TextView btnApprove = new TextView(this);
                        btnApprove.setText("✓ Approve Transfer");
                        btnApprove.setTextColor(ContextCompat.getColor(this, R.color.white));
                        btnApprove.setTextSize(12f);
                        btnApprove.setTypeface(null, android.graphics.Typeface.BOLD);
                        btnApprove.setGravity(android.view.Gravity.CENTER);
                        btnApprove.setBackgroundResource(R.drawable.bg_button_hospital);
                        LinearLayout.LayoutParams appLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.2f);
                        appLp.rightMargin = 8;
                        btnApprove.setLayoutParams(appLp);
                        btnApprove.setOnClickListener(v -> {
                            new android.app.AlertDialog.Builder(this)
                                .setTitle("Approve Stock Transfer")
                                .setMessage("Approve receipt of " + qty + " units of " + bGroup + " from " + srcName + "?\n\nStock will be safely deducted from " + srcName + " and added to your inventory.")
                                .setPositiveButton("Approve Transfer", (d, w) -> {
                                    performApproveStockTransfer(trfId, srcId, srcName, destId, destName, bGroup, qty);
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                        });

                        TextView btnDecline = new TextView(this);
                        btnDecline.setText("✕ Decline");
                        btnDecline.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                        btnDecline.setTextSize(12f);
                        btnDecline.setTypeface(null, android.graphics.Typeface.BOLD);
                        btnDecline.setGravity(android.view.Gravity.CENTER);
                        btnDecline.setBackgroundResource(R.drawable.bg_card_emergency);
                        LinearLayout.LayoutParams decLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.8f);
                        decLp.leftMargin = 8;
                        btnDecline.setLayoutParams(decLp);
                        btnDecline.setOnClickListener(v -> {
                            new android.app.AlertDialog.Builder(this)
                                .setTitle("Decline Stock Transfer")
                                .setMessage("Decline stock transfer request " + trfId + "?")
                                .setPositiveButton("Decline", (d, w) -> {
                                    performDeclineStockTransfer(trfId, bankUid);
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                        });

                        actRow.addView(btnApprove);
                        actRow.addView(btnDecline);
                        card.addView(actRow);
                    } else if (isSourceForMe) {
                        TextView btnCancel = new TextView(this);
                        btnCancel.setText("Cancel Transfer Request");
                        btnCancel.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                        btnCancel.setTextSize(12f);
                        btnCancel.setTypeface(null, android.graphics.Typeface.BOLD);
                        btnCancel.setGravity(android.view.Gravity.CENTER);
                        btnCancel.setBackgroundResource(R.drawable.bg_card_emergency);
                        LinearLayout.LayoutParams canLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 76);
                        canLp.topMargin = 16;
                        btnCancel.setLayoutParams(canLp);
                        btnCancel.setOnClickListener(v -> {
                            new android.app.AlertDialog.Builder(this)
                                .setTitle("Cancel Transfer Request")
                                .setMessage("Cancel pending stock transfer request " + trfId + "?")
                                .setPositiveButton("Yes, Cancel", (d, w) -> {
                                    performCancelStockTransfer(trfId);
                                })
                                .setNegativeButton("No", null)
                                .show();
                        });
                        card.addView(btnCancel);
                    }
                } else if (statusUpper.contains("APPROV") || statusUpper.contains("COMPLET")) {
                    TextView btnDone = new TextView(this);
                    btnDone.setText("✓ Stock Transferred & Synchronized");
                    btnDone.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                    btnDone.setTextSize(12f);
                    btnDone.setTypeface(null, android.graphics.Typeface.BOLD);
                    btnDone.setGravity(android.view.Gravity.CENTER);
                    btnDone.setBackgroundResource(R.drawable.bg_chip_status_available);
                    LinearLayout.LayoutParams doneLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 76);
                    doneLp.topMargin = 16;
                    btnDone.setLayoutParams(doneLp);
                    card.addView(btnDone);
                } else if (statusUpper.contains("DECLIN") || statusUpper.contains("REJECT")) {
                    TextView btnDeclined = new TextView(this);
                    btnDeclined.setText("✕ Transfer Declined");
                    btnDeclined.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                    btnDeclined.setTextSize(12f);
                    btnDeclined.setTypeface(null, android.graphics.Typeface.BOLD);
                    btnDeclined.setGravity(android.view.Gravity.CENTER);
                    btnDeclined.setBackgroundResource(R.drawable.bg_chip_status_critical);
                    LinearLayout.LayoutParams decInfoLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 76);
                    decInfoLp.topMargin = 16;
                    btnDeclined.setLayoutParams(decInfoLp);
                    card.addView(btnDeclined);
                }

                container.addView(card);
            }
        };

        if (chipAll != null) {
            chipAll.setOnClickListener(v -> {
                currentTransfersFilter = "ALL";
                updateChips.run();
                renderTransfers.run();
            });
        }
        if (chipPending != null) {
            chipPending.setOnClickListener(v -> {
                currentTransfersFilter = "PENDING";
                updateChips.run();
                renderTransfers.run();
            });
        }
        if (chipCompleted != null) {
            chipCompleted.setOnClickListener(v -> {
                currentTransfersFilter = "COMPLETED";
                updateChips.run();
                renderTransfers.run();
            });
        }

        if (progress != null) progress.setVisibility(View.VISIBLE);

        try {
            if (bloodBankTransfersListener != null) {
                bloodBankTransfersListener.remove();
                bloodBankTransfersListener = null;
            }

            bloodBankTransfersListener = FirebaseFirestore.getInstance().collection("transfers")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (progress != null) progress.setVisibility(View.GONE);
                    allTransferDocs.clear();

                    if (e != null || queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                        return;
                    }

                    allTransferDocs.addAll(queryDocumentSnapshots.getDocuments());
                    renderTransfers.run();
                });
        } catch (Exception e) {
            if (progress != null) progress.setVisibility(View.GONE);
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
        }
    }

    private void generateTransferRecommendations(String myBankUid, String myBankName, LinearLayout container, TextView emptyText) {
        if (container == null) return;
        container.removeAllViews();

        UserProfile curUser = repository.getCurrentUser();
        String currentBankId = curUser != null ? curUser.getBloodBankId() : myBankUid;

        FirebaseFirestore.getInstance().collection("bloodBanks").get().addOnSuccessListener(bankSnapshots -> {
            if (bankSnapshots == null || bankSnapshots.isEmpty()) {
                if (emptyText != null) emptyText.setVisibility(View.VISIBLE);
                return;
            }

            DocumentSnapshot myBankDoc = null;
            List<DocumentSnapshot> otherBankDocs = new ArrayList<>();

            for (DocumentSnapshot doc : bankSnapshots) {
                String docId = doc.getId();
                String userId = doc.getString("userId");
                String name = doc.getString("name");
                String docBbId = doc.getString("bloodBankId");
                if (docId.equalsIgnoreCase(currentBankId) || docId.equalsIgnoreCase(myBankUid) || 
                    (userId != null && userId.equalsIgnoreCase(myBankUid)) || 
                    (docBbId != null && docBbId.equalsIgnoreCase(currentBankId)) || 
                    (name != null && name.equalsIgnoreCase(myBankName))) {
                    myBankDoc = doc;
                } else {
                    otherBankDocs.add(doc);
                }
            }

            if (myBankDoc == null) {
                String mLower = myBankName.toLowerCase(Locale.US);
                for (DocumentSnapshot doc : bankSnapshots) {
                    String docId = doc.getId().toLowerCase(Locale.US);
                    String name = doc.getString("name") != null ? doc.getString("name").toLowerCase(Locale.US) : "";
                    if ((mLower.contains("msi") && (docId.contains("msi") || name.contains("msi"))) ||
                        (mLower.contains("bombay") && (docId.contains("bombay") || name.contains("bombay"))) ||
                        (mLower.contains("shashwat") && (docId.contains("shashwat") || name.contains("shashwat"))) ||
                        (mLower.contains("civil") && (docId.contains("civil") || name.contains("civil")))) {
                        myBankDoc = doc;
                        otherBankDocs.remove(doc);
                        break;
                    }
                }
            }

            if (myBankDoc == null || otherBankDocs.isEmpty()) {
                if (emptyText != null) emptyText.setVisibility(View.VISIBLE);
                return;
            }

            Map<String, Integer> myStock = extractStockMapFromDoc(myBankDoc);
            String[] bloodGroups = new String[]{"O+", "A+", "B+", "AB+", "O-", "A-", "B-", "AB-"};
            String myFinalName = myBankDoc.getString("name") != null ? myBankDoc.getString("name") : myBankName;

            int recommendationsCount = 0;

            for (String group : bloodGroups) {
                int sourceUnits = getNumericStock(myStock, group);
                // If source has ample stock (> 6 units)
                if (sourceUnits > 6) {
                    for (DocumentSnapshot targetDoc : otherBankDocs) {
                        Map<String, Integer> targetStock = extractStockMapFromDoc(targetDoc);
                        int targetUnits = getNumericStock(targetStock, group);

                        // If target has low stock (<= 4 units)
                        if (targetUnits <= 4) {
                            int suggestedTransfer = Math.min(sourceUnits - 5, Math.min(10, 8 - targetUnits));
                            if (suggestedTransfer < 2) suggestedTransfer = 2;

                            String targetName = targetDoc.getString("name") != null ? targetDoc.getString("name") : "Regional Blood Centre";
                            String targetCity = targetDoc.getString("city") != null ? targetDoc.getString("city") : "";

                            recommendationsCount++;

                            LinearLayout recCard = new LinearLayout(this);
                            recCard.setOrientation(LinearLayout.VERTICAL);
                            recCard.setBackgroundResource(R.drawable.bg_search_bar);
                            recCard.setPadding(28, 24, 28, 24);
                            LinearLayout.LayoutParams recLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            recLp.setMargins(0, 0, 0, 16);
                            recCard.setLayoutParams(recLp);

                            TextView txtRecTitle = new TextView(this);
                            txtRecTitle.setText(String.format(Locale.US, "⚡ Suggested %s Transfer: %d Units", group, suggestedTransfer));
                            txtRecTitle.setTextColor(0xFF38BDF8);
                            txtRecTitle.setTextSize(14f);
                            txtRecTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                            recCard.addView(txtRecTitle);

                            TextView txtRecDesc = new TextView(this);
                            txtRecDesc.setText(String.format(Locale.US, "Source (%s): %d units available\nDestination (%s%s): %d units (Critical low stock)", 
                                    myFinalName, sourceUnits, targetName, !targetCity.isEmpty() ? ", " + targetCity : "", targetUnits));
                            txtRecDesc.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                            txtRecDesc.setTextSize(12f);
                            LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            descLp.topMargin = 6;
                            txtRecDesc.setLayoutParams(descLp);
                            recCard.addView(txtRecDesc);

                            TextView btnQuickTransfer = new TextView(this);
                            btnQuickTransfer.setText("⚡ Create Transfer Request (" + suggestedTransfer + " Units " + group + ")");
                            btnQuickTransfer.setTextColor(ContextCompat.getColor(this, R.color.white));
                            btnQuickTransfer.setTextSize(12f);
                            btnQuickTransfer.setTypeface(null, android.graphics.Typeface.BOLD);
                            btnQuickTransfer.setGravity(android.view.Gravity.CENTER);
                            btnQuickTransfer.setBackgroundResource(R.drawable.bg_button_hospital);
                            LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 76);
                            btnLp.topMargin = 14;
                            btnQuickTransfer.setLayoutParams(btnLp);

                            final int finalSuggested = suggestedTransfer;
                            final DocumentSnapshot finalTargetDoc = targetDoc;
                            btnQuickTransfer.setOnClickListener(v -> {
                                showInitiateStockTransferDialog(finalTargetDoc, group, finalSuggested);
                            });
                            recCard.addView(btnQuickTransfer);

                            container.addView(recCard);
                            if (recommendationsCount >= 3) break;
                        }
                    }
                }
                if (recommendationsCount >= 3) break;
            }

            if (recommendationsCount == 0) {
                if (emptyText != null) emptyText.setVisibility(View.VISIBLE);
            } else {
                if (emptyText != null) emptyText.setVisibility(View.GONE);
            }
        });
    }

    private void showInitiateStockTransferDialog(DocumentSnapshot prefillDestBank, String prefillGroup, int prefillUnits) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_initiate_transfer);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        UserProfile currentUser = repository.getCurrentUser();
        String myBankName = currentUser != null && currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Bombay Blood Bank";
        String myBankUid = currentUser != null ? currentUser.getUid() : "BB-101";
        String myBankId = currentUser != null ? currentUser.getBloodBankId() : myBankUid;

        TextView txtSource = dialog.findViewById(R.id.txt_transfer_source_bank);
        if (txtSource != null) txtSource.setText(myBankName + " (Source)");

        AutoCompleteTextView actvDest = dialog.findViewById(R.id.input_transfer_destination_bank);
        AutoCompleteTextView actvGroup = dialog.findViewById(R.id.input_transfer_blood_group);
        EditText inputQty = dialog.findViewById(R.id.input_transfer_quantity);
        TextView txtSourceAvail = dialog.findViewById(R.id.txt_transfer_source_available);
        AutoCompleteTextView actvComp = dialog.findViewById(R.id.input_transfer_component);
        EditText inputNotes = dialog.findViewById(R.id.input_transfer_notes);
        TextView txtError = dialog.findViewById(R.id.txt_transfer_error);
        View btnSubmit = dialog.findViewById(R.id.btn_submit_stock_transfer);
        View btnClose = dialog.findViewById(R.id.btn_close_initiate_transfer);

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        String[] groups = new String[]{"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-", "Bombay (Oh)"};
        if (actvGroup != null) {
            actvGroup.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, groups));
            actvGroup.setOnClickListener(v -> actvGroup.showDropDown());
            if (prefillGroup != null && !prefillGroup.isEmpty()) {
                actvGroup.setText(prefillGroup, false);
            } else {
                actvGroup.setText("AB+", false);
            }
        }

        String[] comps = new String[]{"Whole Blood", "Packed RBC", "Fresh Frozen Plasma (FFP)", "Platelets", "Cryoprecipitate"};
        if (actvComp != null) {
            actvComp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, comps));
            actvComp.setOnClickListener(v -> actvComp.showDropDown());
            actvComp.setText("Packed RBC", false);
        }

        if (inputQty != null) {
            inputQty.setText(prefillUnits > 0 ? String.valueOf(prefillUnits) : "2");
        }

        // Load Destination Blood Banks from Firestore
        List<DocumentSnapshot> destinationBankDocs = new ArrayList<>();
        List<String> destinationBankLabels = new ArrayList<>();
        final DocumentSnapshot[] myDocHolder = new DocumentSnapshot[1];

        FirebaseFirestore.getInstance().collection("bloodBanks").get().addOnSuccessListener(bankSnapshots -> {
            if (bankSnapshots != null) {
                DocumentSnapshot myDoc = null;
                for (DocumentSnapshot doc : bankSnapshots) {
                    String docId = doc.getId();
                    String userId = doc.getString("userId");
                    String name = doc.getString("name");
                    String docBbId = doc.getString("bloodBankId");
                    if (docId.equalsIgnoreCase(myBankId) || docId.equalsIgnoreCase(myBankUid) || 
                        (userId != null && userId.equalsIgnoreCase(myBankUid)) || 
                        (docBbId != null && docBbId.equalsIgnoreCase(myBankId)) || 
                        (name != null && name.equalsIgnoreCase(myBankName))) {
                        myDoc = doc;
                    } else {
                        destinationBankDocs.add(doc);
                        String c = doc.getString("city") != null ? ", " + doc.getString("city") : "";
                        destinationBankLabels.add(name + c);
                    }
                }

                if (myDoc == null) {
                    String mLower = myBankName.toLowerCase(Locale.US);
                    for (DocumentSnapshot doc : bankSnapshots) {
                        String docId = doc.getId().toLowerCase(Locale.US);
                        String name = doc.getString("name") != null ? doc.getString("name").toLowerCase(Locale.US) : "";
                        if ((mLower.contains("msi") && (docId.contains("msi") || name.contains("msi"))) ||
                            (mLower.contains("bombay") && (docId.contains("bombay") || name.contains("bombay"))) ||
                            (mLower.contains("shashwat") && (docId.contains("shashwat") || name.contains("shashwat"))) ||
                            (mLower.contains("civil") && (docId.contains("civil") || name.contains("civil")))) {
                            myDoc = doc;
                            destinationBankDocs.remove(doc);
                            break;
                        }
                    }
                }

                for (SmartMapItem item : smartMapItemList) {
                    if ("BLOOD_BANK".equalsIgnoreCase(item.type) && 
                        !item.id.equalsIgnoreCase(myBankId) && 
                        !item.id.equalsIgnoreCase(myBankUid) && 
                        (item.name != null && !item.name.equalsIgnoreCase(myBankName))) {
                        boolean already = false;
                        for (String l : destinationBankLabels) {
                            if (l.contains(item.name)) {
                                already = true;
                                break;
                            }
                        }
                        if (!already) {
                            String c = item.area != null && !item.area.isEmpty() ? ", " + item.area : "";
                            destinationBankLabels.add(item.name + c);
                        }
                    }
                }

                myDocHolder[0] = myDoc;

                if (actvDest != null) {
                    actvDest.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, destinationBankLabels));
                    actvDest.setOnClickListener(v -> actvDest.showDropDown());
                    if (prefillDestBank != null) {
                        String name = prefillDestBank.getString("name") != null ? prefillDestBank.getString("name") : "";
                        String c = prefillDestBank.getString("city") != null ? ", " + prefillDestBank.getString("city") : "";
                        actvDest.setText(name + c, false);
                    } else if (!destinationBankLabels.isEmpty()) {
                        actvDest.setText(destinationBankLabels.get(0), false);
                    }
                }

                // Update Source Available Stock Indicator
                final DocumentSnapshot finalMyDoc = myDoc;
                Runnable updateAvailableDisplay = () -> {
                    String rawGroup = actvGroup != null ? actvGroup.getText().toString().trim() : "AB+";
                    String selectedGroup = getCanonicalBloodGroup(rawGroup);
                    if (selectedGroup.isEmpty()) selectedGroup = rawGroup;
                    int avail = 0;
                    if (finalMyDoc != null) {
                        Map<String, Integer> stockMap = extractStockMapFromDoc(finalMyDoc);
                        avail = getNumericStock(stockMap, selectedGroup);
                    } else {
                        for (SmartMapItem item : smartMapItemList) {
                            if ("BLOOD_BANK".equalsIgnoreCase(item.type) && 
                                (item.id.equalsIgnoreCase(myBankUid) || item.id.equalsIgnoreCase(myBankId) || (item.name != null && item.name.equalsIgnoreCase(myBankName)))) {
                                if (item.stockMap != null) {
                                    avail = getNumericStock(item.stockMap, selectedGroup);
                                }
                                break;
                            }
                        }
                    }
                    if (txtSourceAvail != null) {
                        txtSourceAvail.setText(String.format(Locale.US, "Available Source Stock: %d Units (%s)", avail, rawGroup));
                        if (avail <= LOW_STOCK_THRESHOLD) {
                            txtSourceAvail.setTextColor(ContextCompat.getColor(this, R.color.status_low_text));
                        } else {
                            txtSourceAvail.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
                        }
                    }
                };

                updateAvailableDisplay.run();
                if (actvGroup != null) {
                    actvGroup.setOnItemClickListener((p, v, pos, id) -> updateAvailableDisplay.run());
                }
            }
        });

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String destLabel = actvDest != null ? actvDest.getText().toString().trim() : "";
                String rawGroup = actvGroup != null ? actvGroup.getText().toString().trim() : "";
                String group = rawGroup;
                if (rawGroup.contains("Bombay") || rawGroup.equalsIgnoreCase("oh") || rawGroup.equalsIgnoreCase("hh")) {
                    group = "Bombay (Oh)";
                }
                String qtyStr = inputQty != null ? inputQty.getText().toString().trim() : "";
                String comp = actvComp != null ? actvComp.getText().toString().trim() : "Packed RBC";
                String notes = inputNotes != null ? inputNotes.getText().toString().trim() : "";

                if (destLabel.isEmpty()) {
                    if (txtError != null) {
                        txtError.setText("Please select a destination blood bank.");
                        txtError.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                DocumentSnapshot targetDestDoc = null;
                for (int i = 0; i < destinationBankDocs.size(); i++) {
                    if (i < destinationBankLabels.size() && destinationBankLabels.get(i).equalsIgnoreCase(destLabel)) {
                        targetDestDoc = destinationBankDocs.get(i);
                        break;
                    }
                    if (destinationBankDocs.get(i).getString("name") != null && destinationBankDocs.get(i).getString("name").equalsIgnoreCase(destLabel)) {
                        targetDestDoc = destinationBankDocs.get(i);
                        break;
                    }
                }

                if (targetDestDoc == null) {
                    for (DocumentSnapshot d : destinationBankDocs) {
                        String n = d.getString("name");
                        if (n != null && destLabel.toLowerCase(Locale.US).contains(n.toLowerCase(Locale.US))) {
                            targetDestDoc = d;
                            break;
                        }
                    }
                }

                String destBankId;
                String destBankName;

                if (targetDestDoc != null) {
                    destBankId = targetDestDoc.getString("bloodBankId") != null ? targetDestDoc.getString("bloodBankId") : targetDestDoc.getId();
                    destBankName = targetDestDoc.getString("name") != null ? targetDestDoc.getString("name") : "Regional Blood Bank";
                } else {
                    SmartMapItem fallbackItem = null;
                    for (SmartMapItem item : smartMapItemList) {
                        if ("BLOOD_BANK".equalsIgnoreCase(item.type) && 
                            (destLabel.toLowerCase(Locale.US).contains(item.name.toLowerCase(Locale.US)) || item.name.toLowerCase(Locale.US).contains(destLabel.toLowerCase(Locale.US)))) {
                            fallbackItem = item;
                            break;
                        }
                    }
                    if (fallbackItem != null) {
                        destBankId = fallbackItem.id;
                        destBankName = fallbackItem.name;
                    } else {
                        if (txtError != null) {
                            txtError.setText("Invalid destination blood bank selected.");
                            txtError.setVisibility(View.VISIBLE);
                        }
                        return;
                    }
                }

                if (group.isEmpty()) {
                    if (txtError != null) {
                        txtError.setText("Please select a blood group.");
                        txtError.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                int qty = 0;
                try {
                    qty = Integer.parseInt(qtyStr);
                } catch (Exception ignored) {}

                if (qty <= 0 || qty > 100) {
                    if (txtError != null) {
                        txtError.setText("Please enter a valid quantity of units (1 - 100).");
                        txtError.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                // Check Source Stock in Real Firebase Document
                int availableStock = 0;
                DocumentSnapshot myDoc = myDocHolder[0];
                String canonicalGroup = getCanonicalBloodGroup(group);
                if (canonicalGroup.isEmpty()) canonicalGroup = group;

                if (myDoc != null) {
                    availableStock = getNumericStock(extractStockMapFromDoc(myDoc), canonicalGroup);
                } else {
                    for (SmartMapItem item : smartMapItemList) {
                        if ("BLOOD_BANK".equalsIgnoreCase(item.type) && 
                            (item.id.equalsIgnoreCase(myBankUid) || item.id.equalsIgnoreCase(myBankId) || (item.name != null && item.name.equalsIgnoreCase(myBankName)))) {
                            if (item.stockMap != null) {
                                availableStock = getNumericStock(item.stockMap, canonicalGroup);
                            }
                            break;
                        }
                    }
                }

                if (availableStock < qty) {
                    String errMessage = "Insufficient " + canonicalGroup + " stock.";
                    Toast.makeText(this, errMessage, Toast.LENGTH_LONG).show();
                    if (txtError != null) {
                        txtError.setText(errMessage);
                        txtError.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                if (txtError != null) txtError.setVisibility(View.GONE);

                String transferId = "TRF-2026-" + (new Random().nextInt(90000) + 10000);
                String timeStr = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());

                String finalSrcId = (myDoc != null && myDoc.getString("bloodBankId") != null) ? myDoc.getString("bloodBankId") : (myDoc != null ? myDoc.getId() : myBankId);
                String finalSrcName = myDoc != null && myDoc.getString("name") != null ? myDoc.getString("name") : myBankName;
                String createdBy = currentUser != null ? currentUser.getUid() : myBankUid;

                Map<String, Object> trfMap = new HashMap<>();
                trfMap.put("transferId", transferId);
                trfMap.put("sourceBloodBankId", finalSrcId);
                trfMap.put("sourceBloodBankName", finalSrcName);
                trfMap.put("destinationBloodBankId", destBankId);
                trfMap.put("destinationBloodBankName", destBankName);
                trfMap.put("bloodGroup", group);
                trfMap.put("component", comp);
                trfMap.put("quantity", qty);
                trfMap.put("status", "PENDING_APPROVAL");
                trfMap.put("notes", notes);
                trfMap.put("createdAt", timeStr);
                trfMap.put("createdAtTimestamp", System.currentTimeMillis());
                trfMap.put("createdBy", createdBy);
                trfMap.put("updatedAt", System.currentTimeMillis());

                FirebaseFirestore.getInstance().collection("transfers").document(transferId).set(trfMap);

                // Notification for Destination Blood Bank
                Map<String, Object> notif = new HashMap<>();
                notif.put("hospitalId", destBankId);
                notif.put("title", "🚚 Stock Transfer Request Received");
                notif.put("message", myBankName + " requested a stock transfer of " + qty + " units of " + group + " (" + comp + "). Status: Pending Approval.");
                notif.put("type", "TRANSFER");
                notif.put("timestamp", timeStr);
                notif.put("createdAt", System.currentTimeMillis());
                notif.put("isRead", false);
                FirebaseFirestore.getInstance().collection("notifications").add(notif);

                repository.addAuditLog(myBankUid, "BLOOD_BANK", "CREATE_TRANSFER", transferId, timeStr, "NONE", "PENDING_APPROVAL", "Created stock transfer request for " + qty + " units " + group + " to " + destBankName + ". Pending Destination Approval.");

                dialog.dismiss();
                Toast.makeText(this, "✓ Transfer Request " + transferId + " Created Successfully!\nStatus: PENDING APPROVAL (Awaiting " + destBankName + ")", Toast.LENGTH_LONG).show();
            });
        }

        dialog.show();
    }

    private void performApproveStockTransfer(String transferId, String sourceBankId, String sourceBankName, String destBankId, String destBankName, String bGroup, int qty) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference trfRef = db.collection("transfers").document(transferId);
        UserProfile currentUser = repository.getCurrentUser();
        String currentAuthUid = currentUser != null ? currentUser.getUid() : destBankId;
        String currentBankId = currentUser != null && currentUser.getBloodBankId() != null ? currentUser.getBloodBankId() : destBankId;

        db.collection("bloodBanks").get().addOnSuccessListener(bankSnapshots -> {
            DocumentSnapshot srcDoc = null;
            DocumentSnapshot destDoc = null;

            if (bankSnapshots != null) {
                for (DocumentSnapshot doc : bankSnapshots) {
                    String docId = doc.getId();
                    String userId = doc.getString("userId");
                    String name = doc.getString("name");
                    String docBbId = doc.getString("bloodBankId");
                    if (docId.equalsIgnoreCase(sourceBankId) || (userId != null && userId.equalsIgnoreCase(sourceBankId)) || (docBbId != null && docBbId.equalsIgnoreCase(sourceBankId)) || (name != null && name.equalsIgnoreCase(sourceBankName))) {
                        srcDoc = doc;
                    }
                    if (docId.equalsIgnoreCase(destBankId) || (userId != null && userId.equalsIgnoreCase(destBankId)) || (docBbId != null && docBbId.equalsIgnoreCase(destBankId)) || (name != null && name.equalsIgnoreCase(destBankName)) ||
                        docId.equalsIgnoreCase(currentBankId) || (docBbId != null && docBbId.equalsIgnoreCase(currentBankId))) {
                        destDoc = doc;
                    }
                }

                if (srcDoc == null) {
                    String sLower = sourceBankName.toLowerCase(Locale.US);
                    for (DocumentSnapshot doc : bankSnapshots) {
                        String docId = doc.getId().toLowerCase(Locale.US);
                        String name = doc.getString("name") != null ? doc.getString("name").toLowerCase(Locale.US) : "";
                        if ((sLower.contains("msi") && (docId.contains("msi") || name.contains("msi"))) ||
                            (sLower.contains("bombay") && (docId.contains("bombay") || name.contains("bombay"))) ||
                            (sLower.contains("shashwat") && (docId.contains("shashwat") || name.contains("shashwat"))) ||
                            (sLower.contains("civil") && (docId.contains("civil") || name.contains("civil")))) {
                            srcDoc = doc;
                            break;
                        }
                    }
                }

                if (destDoc == null) {
                    String dLower = destBankName.toLowerCase(Locale.US);
                    for (DocumentSnapshot doc : bankSnapshots) {
                        String docId = doc.getId().toLowerCase(Locale.US);
                        String name = doc.getString("name") != null ? doc.getString("name").toLowerCase(Locale.US) : "";
                        if ((dLower.contains("msi") && (docId.contains("msi") || name.contains("msi"))) ||
                            (dLower.contains("bombay") && (docId.contains("bombay") || name.contains("bombay"))) ||
                            (dLower.contains("shashwat") && (docId.contains("shashwat") || name.contains("shashwat"))) ||
                            (dLower.contains("civil") && (docId.contains("civil") || name.contains("civil")))) {
                            destDoc = doc;
                            break;
                        }
                    }
                }
            }

            DocumentReference srcRef = srcDoc != null ? srcDoc.getReference() : db.collection("bloodBanks").document(sourceBankId);
            DocumentReference destRef = destDoc != null ? destDoc.getReference() : db.collection("bloodBanks").document(destBankId);
            String finalSrcId = srcDoc != null ? srcDoc.getId() : sourceBankId;
            String finalDestId = destDoc != null ? destDoc.getId() : destBankId;

            db.runTransaction(transaction -> {
                DocumentSnapshot curTrf = transaction.get(trfRef);
                DocumentSnapshot curSrc = transaction.get(srcRef);
                DocumentSnapshot curDest = transaction.get(destRef);

                if (!curTrf.exists()) {
                    throw new FirebaseFirestoreException("Transfer record not found", FirebaseFirestoreException.Code.NOT_FOUND);
                }

                String curStatus = curTrf.getString("status");
                if (curStatus != null && (curStatus.equalsIgnoreCase("COMPLETED") || curStatus.equalsIgnoreCase("APPROVED"))) {
                    throw new FirebaseFirestoreException("ALREADY_APPROVED", FirebaseFirestoreException.Code.ABORTED);
                }
                if (curStatus != null && !curStatus.toUpperCase(Locale.US).contains("PENDING")) {
                    throw new FirebaseFirestoreException("ALREADY_PROCESSED:" + curStatus, FirebaseFirestoreException.Code.ABORTED);
                }

                if (!curSrc.exists()) {
                    throw new FirebaseFirestoreException("Source blood bank not found", FirebaseFirestoreException.Code.NOT_FOUND);
                }
                if (!curDest.exists()) {
                    throw new FirebaseFirestoreException("Destination blood bank not found", FirebaseFirestoreException.Code.NOT_FOUND);
                }

                Map<String, Integer> srcStock = extractStockMapFromDoc(curSrc);
                Map<String, Integer> destStock = extractStockMapFromDoc(curDest);

                String canonicalGroup = getCanonicalBloodGroup(bGroup);
                if (canonicalGroup.isEmpty()) canonicalGroup = bGroup;
                int srcAvail = getNumericStock(srcStock, canonicalGroup);
                int destAvail = getNumericStock(destStock, canonicalGroup);

                if (srcAvail < qty) {
                    throw new FirebaseFirestoreException("INSUFFICIENT_SOURCE_STOCK:" + srcAvail, FirebaseFirestoreException.Code.ABORTED);
                }

                int newSrcStock = srcAvail - qty;
                int newDestStock = destAvail + qty;

                srcStock.put(canonicalGroup, newSrcStock);
                destStock.put(canonicalGroup, newDestStock);

                int totalSrcUnits = 0;
                for (int s : srcStock.values()) totalSrcUnits += s;
                int totalDestUnits = 0;
                for (int s : destStock.values()) totalDestUnits += s;

                // 1. Update Source Bank Stock in Firestore
                transaction.update(srcRef, "bloodStock", srcStock);
                transaction.update(srcRef, "totalUnits", totalSrcUnits);
                transaction.update(srcRef, "updatedAt", System.currentTimeMillis());

                // 2. Update Destination Bank Stock in Firestore
                transaction.update(destRef, "bloodStock", destStock);
                transaction.update(destRef, "totalUnits", totalDestUnits);
                transaction.update(destRef, "updatedAt", System.currentTimeMillis());

                // 3. Update Transfer Document in Firestore
                String timeStr = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());
                transaction.update(trfRef, "status", "COMPLETED");
                transaction.update(trfRef, "approvedAt", timeStr);
                transaction.update(trfRef, "approvedTimestamp", System.currentTimeMillis());
                transaction.update(trfRef, "approvedBy", currentAuthUid);
                transaction.update(trfRef, "completedAt", timeStr);
                transaction.update(trfRef, "completedTimestamp", System.currentTimeMillis());
                transaction.update(trfRef, "updatedAt", System.currentTimeMillis());

                return new Pair<>(srcStock, destStock);
            }).addOnSuccessListener(result -> {
                Map<String, Integer> updatedSrcStock = result.first;
                Map<String, Integer> updatedDestStock = result.second;
                String timeStr = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());
                String canonicalGroup = getCanonicalBloodGroup(bGroup);
                if (canonicalGroup.isEmpty()) canonicalGroup = bGroup;
                int srcRemaining = getNumericStock(updatedSrcStock, canonicalGroup);
                int destTotal = getNumericStock(updatedDestStock, canonicalGroup);

                // Update in-memory caches
                for (SmartMapItem item : smartMapItemList) {
                    if (item.id.equalsIgnoreCase(finalSrcId) || item.id.equalsIgnoreCase(sourceBankId) || (item.name != null && item.name.equalsIgnoreCase(sourceBankName))) {
                        item.stockMap = updatedSrcStock;
                        item.totalUnits = 0;
                        for (int v : updatedSrcStock.values()) item.totalUnits += v;
                    }
                    if (item.id.equalsIgnoreCase(finalDestId) || item.id.equalsIgnoreCase(destBankId) || (item.name != null && item.name.equalsIgnoreCase(destBankName))) {
                        item.stockMap = updatedDestStock;
                        item.totalUnits = 0;
                        for (int v : updatedDestStock.values()) item.totalUnits += v;
                    }
                }
                if (selectedBloodBank != null) {
                    if (selectedBloodBank.id.equalsIgnoreCase(finalSrcId) || selectedBloodBank.id.equalsIgnoreCase(sourceBankId) || (selectedBloodBank.name != null && selectedBloodBank.name.equalsIgnoreCase(sourceBankName))) {
                        selectedBloodBank.stockMap = updatedSrcStock;
                        selectedBloodBank.totalUnits = 0;
                        for (int v : updatedSrcStock.values()) selectedBloodBank.totalUnits += v;
                    } else if (selectedBloodBank.id.equalsIgnoreCase(finalDestId) || selectedBloodBank.id.equalsIgnoreCase(destBankId) || (selectedBloodBank.name != null && selectedBloodBank.name.equalsIgnoreCase(destBankName))) {
                        selectedBloodBank.stockMap = updatedDestStock;
                        selectedBloodBank.totalUnits = 0;
                        for (int v : updatedDestStock.values()) selectedBloodBank.totalUnits += v;
                    }
                }

                // Notification for Source Bank
                Map<String, Object> notifSrc = new HashMap<>();
                notifSrc.put("hospitalId", sourceBankId);
                notifSrc.put("title", "✓ Stock Transfer Approved & Completed");
                notifSrc.put("message", destBankName + " approved the transfer of " + qty + " units of " + bGroup + ". Remaining " + bGroup + " stock: " + srcRemaining + "u.");
                notifSrc.put("type", "TRANSFER_COMPLETED");
                notifSrc.put("timestamp", timeStr);
                notifSrc.put("createdAt", System.currentTimeMillis());
                notifSrc.put("isRead", false);
                db.collection("notifications").add(notifSrc);

                // Notification for Destination Bank
                Map<String, Object> notifDest = new HashMap<>();
                notifDest.put("hospitalId", destBankId);
                notifDest.put("title", "✓ Stock Transfer Received");
                notifDest.put("message", "Received " + qty + " units of " + bGroup + " from " + sourceBankName + ". New " + bGroup + " stock: " + destTotal + "u.");
                notifDest.put("type", "TRANSFER_COMPLETED");
                notifDest.put("timestamp", timeStr);
                notifDest.put("createdAt", System.currentTimeMillis());
                notifDest.put("isRead", false);
                db.collection("notifications").add(notifDest);

                repository.addAuditLog(destBankId, "BLOOD_BANK", "APPROVE_TRANSFER", transferId, timeStr, "PENDING_APPROVAL", "COMPLETED", "Approved transfer of " + qty + " units " + bGroup + " from " + sourceBankName + " to " + destBankName);
                Toast.makeText(this, "✓ Stock Transfer Approved & Completed!\n" + sourceBankName + " " + bGroup + ": " + srcRemaining + " Units | " + destBankName + " " + bGroup + ": " + destTotal + " Units", Toast.LENGTH_LONG).show();
            }).addOnFailureListener(err -> {
                String msg = err.getMessage() != null ? err.getMessage() : "";
                if (msg.contains("ALREADY_APPROVED")) {
                    Toast.makeText(this, "Transfer already completed.", Toast.LENGTH_SHORT).show();
                } else if (msg.contains("ALREADY_PROCESSED")) {
                    Toast.makeText(this, "Transfer has already been processed.", Toast.LENGTH_SHORT).show();
                } else if (msg.contains("INSUFFICIENT_SOURCE_STOCK")) {
                    String canonicalGroup = getCanonicalBloodGroup(bGroup);
                    if (canonicalGroup.isEmpty()) canonicalGroup = bGroup;
                    Toast.makeText(this, "Insufficient " + canonicalGroup + " stock.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Approval failed: " + msg, Toast.LENGTH_SHORT).show();
                }
            });
        }).addOnFailureListener(err -> {
            Toast.makeText(this, "Database error: " + err.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
private void bindNotificationsView(View view) {
        ProgressBar progress = view.findViewById(R.id.loading_notifications_progress);
        LinearLayout container = view.findViewById(R.id.layout_notifications_container);
        View emptyState = view.findViewById(R.id.layout_notifications_empty);
        View btnMarkRead = view.findViewById(R.id.btn_mark_all_read);

        if (progress != null) progress.setVisibility(View.VISIBLE);

        UserProfile currentUser = repository.getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : "HOS-8842";

        try {
            hospitalNotificationsListener = FirebaseFirestore.getInstance().collection("notifications")
                .whereEqualTo("hospitalId", currentUid)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (progress != null) progress.setVisibility(View.GONE);
                    if (container == null) return;
                    container.removeAllViews();

                    if (e != null || queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                        return;
                    }

                    if (emptyState != null) emptyState.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("title") != null ? doc.getString("title") : "Notification";
                        String msg = doc.getString("message") != null ? doc.getString("message") : "";
                        String time = doc.getString("timestamp") != null ? doc.getString("timestamp") : "Recently";
                        String type = doc.getString("type") != null ? doc.getString("type") : "GENERAL";
                        Boolean isRead = doc.getBoolean("isRead");
                        boolean read = isRead != null && isRead;

                        LinearLayout card = new LinearLayout(this);
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setBackgroundResource(type.contains("EMERG") ? R.drawable.bg_card_emergency : R.drawable.bg_card_premium);
                        card.setPadding(32, 24, 32, 24);
                        if (read) card.setAlpha(0.65f);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 0, 0, 16);
                        card.setLayoutParams(lp);

                        TextView txtTitle = new TextView(this);
                        txtTitle.setText(title);
                        txtTitle.setTextColor(type.contains("EMERG") ? ContextCompat.getColor(this, R.color.status_critical_text) : ContextCompat.getColor(this, R.color.white));
                        txtTitle.setTextSize(15f);
                        txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                        card.addView(txtTitle);

                        TextView txtMsg = new TextView(this);
                        txtMsg.setText(msg);
                        txtMsg.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                        txtMsg.setTextSize(13f);
                        LinearLayout.LayoutParams msgLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        msgLp.topMargin = 4;
                        txtMsg.setLayoutParams(msgLp);
                        card.addView(txtMsg);

                        TextView txtTime = new TextView(this);
                        txtTime.setText("⏱ " + time);
                        txtTime.setTextColor(ContextCompat.getColor(this, R.color.text_tertiary));
                        txtTime.setTextSize(11f);
                        LinearLayout.LayoutParams timeLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        timeLp.topMargin = 8;
                        txtTime.setLayoutParams(timeLp);
                        card.addView(txtTime);

                        container.addView(card);
                    }
                });
        } catch (Exception e) {
            if (progress != null) progress.setVisibility(View.GONE);
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
        }

        if (btnMarkRead != null) {
            btnMarkRead.setOnClickListener(v -> {
                FirebaseFirestore.getInstance().collection("notifications")
                    .whereEqualTo("hospitalId", currentUid)
                    .get()
                    .addOnSuccessListener(snapshots -> {
                        if (snapshots != null) {
                            for (QueryDocumentSnapshot doc : snapshots) {
                                doc.getReference().update("isRead", true);
                            }
                        }
                        Toast.makeText(this, "All notifications marked as read.", Toast.LENGTH_SHORT).show();
                    });
            });
        }
    }

    
private void bindProfileView(View view) {
        attachRoleBackgroundAnimators(view);

        UserProfile currentUser = repository.getCurrentUser();
        String name = currentUser != null && currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "St. Jude General Hospital";
        String email = currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : "hospital@smartblood.org";
        String phone = currentUser != null && currentUser.getMobileNumber() != null ? currentUser.getMobileNumber() : "+91 233 2374500";
        String address = currentUser != null && currentUser.getLocationAddress() != null ? currentUser.getLocationAddress() : "Sangli, Maharashtra, India";
        String uid = currentUser != null ? currentUser.getUid() : "HOS-8842";

        TextView txtName = view.findViewById(R.id.profile_user_name);
        TextView txtRole = view.findViewById(R.id.profile_role_tag);
        TextView txtBadge = view.findViewById(R.id.profile_blood_group_badge);
        TextView txtEmail = view.findViewById(R.id.txt_profile_email);
        TextView txtPhone = view.findViewById(R.id.txt_profile_phone);
        TextView txtAddress = view.findViewById(R.id.txt_profile_address);
        TextView txtCoords = view.findViewById(R.id.txt_profile_coords);
        View btnEdit = view.findViewById(R.id.btn_profile_edit);

        if (txtName != null) txtName.setText(name);
        if (txtRole != null) txtRole.setText("Verified Healthcare Center");
        if (txtBadge != null) txtBadge.setText("Facility ID: " + uid);
        if (txtEmail != null) txtEmail.setText(email);
        if (txtPhone != null) txtPhone.setText(phone);
        if (txtAddress != null) txtAddress.setText(address);
        if (txtCoords != null) txtCoords.setText(String.format(Locale.US, "GPS: %.4f° N, %.4f° E", userLat, userLng));

        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> showEditHospitalProfileDialog());
        }

        SwitchCompat switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        if (switchDarkMode != null) {
            int currentMode = AppCompatDelegate.getDefaultNightMode();
            switchDarkMode.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                AppCompatDelegate.setDefaultNightMode(
                        isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
            });
        }

        View btnLogout = view.findViewById(R.id.btn_profile_logout);
        if (btnLogout != null) btnLogout.setOnClickListener(v -> handleUserLogout());
    }

    
    private void showStockTransferDialog() {
        showInitiateStockTransferDialog(null, null, 0);
    }

    private void updateBloodGroupStockCard(TextView txtUnits, TextView txtStatus, int units) {
        if (txtUnits != null) txtUnits.setText(units + " Units");
        if (txtStatus != null) {
            if (units == 0) {
                txtStatus.setText("OUT OF STOCK");
                txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
            } else if (units <= LOW_STOCK_THRESHOLD) {
                txtStatus.setText("LOW RESERVE");
                txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_low_text));
            } else {
                txtStatus.setText("AVAILABLE");
                txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
            }
        }
    }

    private Map<String, Integer> extractStockMapFromDoc(DocumentSnapshot doc) {
        Map<String, Integer> result = new HashMap<>();
        if (doc == null || !doc.exists()) return result;
        Object rawStock = doc.get("bloodStock");
        if (rawStock instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) rawStock;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String k = String.valueOf(entry.getKey());
                int v = 0;
                if (entry.getValue() instanceof Number) {
                    v = ((Number) entry.getValue()).intValue();
                }
                result.put(getCanonicalBloodGroup(k), v);
            }
        }
        return result;
    }

    private void performDeclineStockTransfer(String transferId, String bankUid) {
        if (transferId == null) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "DECLINED");
        updates.put("declinedAt", System.currentTimeMillis());
        updates.put("declinedBy", bankUid);
        FirebaseFirestore.getInstance().collection("stockTransfers").document(transferId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Stock transfer declined.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to decline: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void performCancelStockTransfer(String transferId) {
        if (transferId == null) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "CANCELLED");
        updates.put("cancelledAt", System.currentTimeMillis());
        FirebaseFirestore.getInstance().collection("stockTransfers").document(transferId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Stock transfer cancelled.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to cancel: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
private void attachRoleBackgroundAnimators(View view) {
        if (view == null) return;
        View glowView = view.findViewById(R.id.bg_glow_bank_dash_purple);
        View overlayView = view.findViewById(R.id.bg_nodes_bank_dash);

        if (glowView != null) {
            android.animation.ObjectAnimator pulseGlow = android.animation.ObjectAnimator.ofFloat(glowView, "alpha", 0.50f, 0.95f, 0.50f);
            pulseGlow.setDuration(4000);
            pulseGlow.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            pulseGlow.start();
        }
        if (overlayView != null) {
            android.animation.ObjectAnimator transOverlay = android.animation.ObjectAnimator.ofFloat(overlayView, "translationY", -10f, 10f, -10f);
            transOverlay.setDuration(7800);
            transOverlay.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            transOverlay.start();
        }
    }

    private void bindOnboardingView(View view) {
        TextView btnNext = view.findViewById(R.id.btn_onboarding_next);
        TextView btnSkip = view.findViewById(R.id.btn_onboarding_skip);
        TextView title = view.findViewById(R.id.onboarding_title);
        TextView desc = view.findViewById(R.id.onboarding_description);

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                onboardingStep++;
                if (onboardingStep == 2) {
                    if (title != null) title.setText("Connect Hospitals & Blood Banks");
                    if (desc != null) desc.setText("Seamless real-time inventory sharing between healthcare centers and emergency dispatchers.");
                } else if (onboardingStep == 3) {
                    if (title != null) title.setText("Smart Emergency Coordination");
                    if (desc != null) desc.setText("AI-driven proximity matching ensuring zero delay in life-saving transfusions.");
                    btnNext.setText("Get Started 🚀");
                } else {
                    onboardingStep = 1;
                    loadHomeDashboardForRole();
                }
            });
        }

        if (btnSkip != null) btnSkip.setOnClickListener(v -> loadHomeDashboardForRole());
    }

    private void bindAuditLogsView(View view) {}


    private void showFacilitySwitcherDialog() {
        String[] facilities = new String[]{
                "MSI Blood Bank Sangli (ID: BB-001)",
                "Bombay Blood Bank (ID: BB-002)",
                "Shashwat Blood Bank (ID: BB-003)",
                "Sangli Civil Hospital Blood Bank (ID: BB-004)"
        };
        final String[] ids = new String[]{"BB-001", "BB-002", "BB-003", "BB-004"};
        final String[] names = new String[]{"MSI Blood Bank", "Bombay Blood Bank", "Shashwat Blood Bank", "Sangli Civil Blood Bank"};
        final String[] phones = new String[]{"+91 233 2374501", "+91 233 2671200", "+91 233 2530900", "+91 233 2441100"};
        final double[] lats = new double[]{16.8580, 16.8510, 16.8640, 16.8480};
        final double[] lngs = new double[]{74.5880, 74.5720, 74.6010, 74.5650};

        new AlertDialog.Builder(this)
                .setTitle("Switch Blood Bank Facility Context")
                .setItems(facilities, (dialog, which) -> {
                    currentBankId = ids[which];
                    currentBankName = names[which];
                    currentBankPhone = phones[which];
                    currentBankLat = lats[which];
                    currentBankLng = lngs[which];

                    UserProfile user = repository.getCurrentUser();
                    if (user != null) {
                        user.setBloodBankId(currentBankId);
                        user.setDisplayName(currentBankName);
                    }

                    Toast.makeText(this, "Switched context to: " + currentBankName, Toast.LENGTH_SHORT).show();
                    loadView(R.layout.view_blood_bank_dashboard_v2, this::bindBloodBankDashboardV2);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showUpdateInventoryStockDialog(String bloodGroup, int currentUnits) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_update_inventory_stock);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        View btnClose = dialog.findViewById(R.id.btn_close_update_stock);
        TextView txtGroup = dialog.findViewById(R.id.txt_dialog_current_group);
        TextView txtCurrent = dialog.findViewById(R.id.txt_dialog_current_units);
        TextView btnModeAdd = dialog.findViewById(R.id.btn_mode_add);
        TextView btnModeDeduct = dialog.findViewById(R.id.btn_mode_deduct);
        TextView btnModeSet = dialog.findViewById(R.id.btn_mode_set);
        TextView txtLabelUnits = dialog.findViewById(R.id.txt_label_units_input);
        EditText inputUnits = dialog.findViewById(R.id.input_stock_dialog_units);
        TextView txtPreview = dialog.findViewById(R.id.txt_dialog_new_preview);
        TextView txtError = dialog.findViewById(R.id.txt_dialog_stock_error);
        TextView btnSave = dialog.findViewById(R.id.btn_save_stock_dialog);

        if (txtGroup != null) txtGroup.setText("Blood Group: " + bloodGroup);
        if (txtCurrent != null) txtCurrent.setText(String.format(Locale.US, "Current Available: %d Units", currentUnits));
        if (txtPreview != null) txtPreview.setText(String.format(Locale.US, "New Resulting Stock: %d Units", currentUnits));

        final int[] mode = new int[]{0}; // 0 = Add, 1 = Deduct, 2 = Set Exact

        Runnable updateModeUI = () -> {
            if (btnModeAdd != null) {
                btnModeAdd.setBackgroundResource(mode[0] == 0 ? R.drawable.bg_button_hospital : R.drawable.bg_chip_status_pending);
                btnModeAdd.setTextColor(ContextCompat.getColor(this, mode[0] == 0 ? R.color.white : R.color.text_secondary));
            }
            if (btnModeDeduct != null) {
                btnModeDeduct.setBackgroundResource(mode[0] == 1 ? R.drawable.bg_button_hospital : R.drawable.bg_chip_status_pending);
                btnModeDeduct.setTextColor(ContextCompat.getColor(this, mode[0] == 1 ? R.color.white : R.color.text_secondary));
            }
            if (btnModeSet != null) {
                btnModeSet.setBackgroundResource(mode[0] == 2 ? R.drawable.bg_button_hospital : R.drawable.bg_chip_status_pending);
                btnModeSet.setTextColor(ContextCompat.getColor(this, mode[0] == 2 ? R.color.white : R.color.text_secondary));
            }
            if (txtLabelUnits != null) {
                txtLabelUnits.setText(mode[0] == 0 ? "Units to Add *" : (mode[0] == 1 ? "Units to Deduct *" : "Exact Units to Set *"));
            }
            if (btnSave != null) {
                btnSave.setText(mode[0] == 0 ? "✔ Add Stock" : (mode[0] == 1 ? "− Deduct Stock" : "= Set Exact Stock"));
            }
        };

        if (btnModeAdd != null) btnModeAdd.setOnClickListener(v -> { mode[0] = 0; updateModeUI.run(); });
        if (btnModeDeduct != null) btnModeDeduct.setOnClickListener(v -> { mode[0] = 1; updateModeUI.run(); });
        if (btnModeSet != null) btnModeSet.setOnClickListener(v -> { mode[0] = 2; updateModeUI.run(); });

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String unitsStr = inputUnits != null ? inputUnits.getText().toString().trim() : "";

                int unitsVal = 0;
                try {
                    unitsVal = Integer.parseInt(unitsStr);
                } catch (Exception ignored) {}

                if (unitsVal < 0 || (unitsVal == 0 && mode[0] != 2)) {
                    if (txtError != null) {
                        txtError.setText("Please enter a valid quantity of units.");
                        txtError.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                int newUnits = currentUnits;
                if (mode[0] == 0) {
                    newUnits = currentUnits + unitsVal;
                } else if (mode[0] == 1) {
                    newUnits = Math.max(0, currentUnits - unitsVal);
                } else {
                    newUnits = unitsVal;
                }

                if (txtError != null) txtError.setVisibility(View.GONE);
                btnSave.setEnabled(false);

                final int unitsToProcess = unitsVal;
                final int processMode = mode[0];
                UserProfile curUser = repository.getCurrentUser();
                String bankUid = curUser != null ? curUser.getUid() : "BB-101";
                String activeBankDocId = curUser != null && curUser.getBloodBankId() != null ? curUser.getBloodBankId() : currentBankId;

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference bankRef = db.collection("bloodBanks").document(activeBankDocId);

                db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(bankRef);
                    if (!snapshot.exists()) {
                        throw new FirebaseFirestoreException("Blood bank facility record not found.", FirebaseFirestoreException.Code.NOT_FOUND);
                    }

                    Map<String, Integer> stockMap = extractStockMapFromDoc(snapshot);
                    String canonicalGroup = getCanonicalBloodGroup(bloodGroup);
                    if (canonicalGroup.isEmpty()) canonicalGroup = bloodGroup;
                    
                    int currentInDb = getNumericStock(stockMap, canonicalGroup);
                    int finalNewUnits;

                    if (processMode == 0) { // Add
                        finalNewUnits = currentInDb + unitsToProcess;
                    } else if (processMode == 1) { // Deduct
                        if (currentInDb < unitsToProcess) {
                            throw new FirebaseFirestoreException("INSUFFICIENT_STOCK:" + currentInDb, FirebaseFirestoreException.Code.ABORTED);
                        }
                        finalNewUnits = currentInDb - unitsToProcess;
                    } else { // Set Exact
                        finalNewUnits = unitsToProcess;
                    }

                    stockMap.put(canonicalGroup, finalNewUnits);
                    
                    // Recalculate total units
                    int total = 0;
                    for (int s : stockMap.values()) total += s;

                    transaction.update(bankRef, "bloodStock", stockMap);
                    transaction.update(bankRef, "totalUnits", total);
                    transaction.update(bankRef, "updatedAt", System.currentTimeMillis());

                    return finalNewUnits;
                }).addOnSuccessListener(finalNewUnits -> {
                    dialog.dismiss();
                    String timeStr = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());
                    repository.addAuditLog(bankUid, "BLOOD_BANK", "UPDATE_STOCK", activeBankDocId, timeStr, String.valueOf(currentUnits), String.valueOf(finalNewUnits), "Updated " + bloodGroup + " stock");
                    Toast.makeText(this, "✔ " + bloodGroup + " stock updated to " + finalNewUnits + " units.", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("INSUFFICIENT_STOCK")) {
                        String avail = msg.substring(msg.indexOf(":") + 1);
                        String err = "Insufficient " + bloodGroup + " stock. Only " + avail + " units available.";
                        if (txtError != null) {
                            txtError.setText(err);
                            txtError.setVisibility(View.VISIBLE);
                        }
                        Toast.makeText(this, err, Toast.LENGTH_LONG).show();
                    } else {
                        if (txtError != null) {
                            txtError.setText("Update failed: " + e.getMessage());
                            txtError.setVisibility(View.VISIBLE);
                        }
                    }
                });
            });
        }

        dialog.show();
    }

    private static class BloodBankOption {
        String id;
        String name;
        String address;
        String phone;
        double lat;
        double lng;

        BloodBankOption(String id, String name, String address, String phone, double lat, double lng) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.phone = phone;
            this.lat = lat;
            this.lng = lng;
        }
    }

    private void showAppointmentDetailsDialog(DocumentSnapshot doc) {
        if (doc == null) return;
        Log.d("SMARTBLOOD_CLICK", "Opening Appointment Details Dialog for doc: " + doc.getId());
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_book_donation);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        View btnClose = dialog.findViewById(R.id.btn_close_book_donation);
        AutoCompleteTextView dropdownBank = dialog.findViewById(R.id.book_bank_dropdown);
        TextView txtBankName = dialog.findViewById(R.id.txt_selected_bank_name);
        TextView txtBankDistance = dialog.findViewById(R.id.txt_selected_bank_distance);
        TextView txtBankLocation = dialog.findViewById(R.id.txt_selected_bank_location);
        TextView txtBankContact = dialog.findViewById(R.id.txt_selected_bank_contact);
        TextView txtDonorGroup = dialog.findViewById(R.id.txt_book_donor_group);
        EditText inputDate = dialog.findViewById(R.id.input_book_date);
        EditText inputTime = dialog.findViewById(R.id.input_book_time);
        EditText inputMessage = dialog.findViewById(R.id.input_book_message);
        TextView btnCancel = dialog.findViewById(R.id.btn_cancel_book_donation);
        TextView btnSubmit = dialog.findViewById(R.id.btn_submit_book_donation);

        // Hide distance radius chips in view details mode
        View chipAll = dialog.findViewById(R.id.chip_radius_all);
        if (chipAll != null && chipAll.getParent() instanceof View) {
            View parent = (View) chipAll.getParent();
            if (parent.getParent() instanceof View) {
                ((View) parent.getParent()).setVisibility(View.GONE);
            } else {
                parent.setVisibility(View.GONE);
            }
        }

        String bName = doc.getString("bloodBankName") != null ? doc.getString("bloodBankName") : "Blood Bank";
        String bAddress = doc.getString("bloodBankAddress") != null ? doc.getString("bloodBankAddress") : "Sangli, Maharashtra";
        String bPhone = doc.getString("bloodBankPhone") != null ? doc.getString("bloodBankPhone") : "+91 233 2374501";
        String bGroup = doc.getString("donorBloodGroup") != null ? doc.getString("donorBloodGroup") : (doc.getString("bloodGroup") != null ? doc.getString("bloodGroup") : "O+");
        String aDate = doc.getString("appointmentDate") != null ? doc.getString("appointmentDate") : (doc.getString("date") != null ? doc.getString("date") : "Scheduled");
        String aTime = doc.getString("appointmentTime") != null ? doc.getString("appointmentTime") : (doc.getString("time") != null ? doc.getString("time") : "10:30 AM");
        String msg = doc.getString("message") != null ? doc.getString("message") : (doc.getString("notes") != null ? doc.getString("notes") : "");
        final String status = doc.getString("status") != null ? doc.getString("status") : "PENDING";
        final String aptId = doc.getId();

        if (txtBankName != null) txtBankName.setText(bName);
        if (txtBankDistance != null) txtBankDistance.setText("STATUS: " + status.toUpperCase(Locale.US));
        if (txtBankLocation != null) txtBankLocation.setText("📍 " + bAddress);
        if (txtBankContact != null) txtBankContact.setText("📞 " + bPhone);
        if (txtDonorGroup != null) txtDonorGroup.setText("🩸 " + bGroup + "  -  " + status.toUpperCase(Locale.US));
        if (dropdownBank != null) { dropdownBank.setText(bName, false); dropdownBank.setEnabled(false); }
        if (inputDate != null) { inputDate.setText(aDate); inputDate.setEnabled(false); inputDate.setOnClickListener(null); }
        if (inputTime != null) { inputTime.setText(aTime); inputTime.setEnabled(false); inputTime.setOnClickListener(null); }
        if (inputMessage != null) { inputMessage.setText(!msg.isEmpty() ? msg : "No additional notes."); inputMessage.setEnabled(false); }

        if (btnSubmit != null) {
            btnSubmit.setText("Close");
            btnSubmit.setClickable(true);
            btnSubmit.setFocusable(true);
            btnSubmit.setBackgroundResource(R.drawable.bg_button_primary);
            btnSubmit.setOnClickListener(v -> {
                Log.d("SMARTBLOOD_CLICK", "Details Dialog Close button clicked");
                dialog.dismiss();
            });
        }

        if (btnCancel != null) {
            if (status.equalsIgnoreCase("PENDING") || status.equalsIgnoreCase("CONFIRMED")) {
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setClickable(true);
                btnCancel.setFocusable(true);
                btnCancel.setText("Cancel Appointment");
                btnCancel.setBackgroundResource(R.drawable.bg_card_emergency);
                btnCancel.setTextColor(ContextCompat.getColor(this, R.color.status_critical_text));
                btnCancel.setOnClickListener(v -> {
                    Log.d("SMARTBLOOD_CLICK", "Details Dialog Cancel Appointment clicked");
                    new AlertDialog.Builder(this)
                            .setTitle("Cancel Appointment")
                            .setMessage("Are you sure you want to cancel appointment " + aptId + "?")
                            .setPositiveButton("Yes, Cancel", (d, w) -> {
                                FirebaseFirestore.getInstance().collection("donorAppointments").document(aptId).update("status", "CANCELLED")
                                        .addOnSuccessListener(aVoid -> {
                                            dialog.dismiss();
                                            Toast.makeText(this, "Appointment cancelled.", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to cancel: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            })
                            .setNegativeButton("Keep Appointment", null)
                            .show();
                });
            } else {
                btnCancel.setVisibility(View.GONE);
            }
        }

        if (btnClose != null) {
            btnClose.setClickable(true);
            btnClose.setOnClickListener(v -> {
                Log.d("SMARTBLOOD_CLICK", "Details Dialog 'X' close clicked");
                dialog.dismiss();
            });
        }

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void showBookDonationDialog() {
        Log.d("SMARTBLOOD_CLICK", "Opening Book Blood Donation Dialog");
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_book_donation);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        View btnClose = dialog.findViewById(R.id.btn_close_book_donation);
        AutoCompleteTextView dropdownBank = dialog.findViewById(R.id.book_bank_dropdown);
        TextView txtBankName = dialog.findViewById(R.id.txt_selected_bank_name);
        TextView txtBankDistance = dialog.findViewById(R.id.txt_selected_bank_distance);
        TextView txtBankLocation = dialog.findViewById(R.id.txt_selected_bank_location);
        TextView txtBankContact = dialog.findViewById(R.id.txt_selected_bank_contact);
        TextView txtDonorGroup = dialog.findViewById(R.id.txt_book_donor_group);
        EditText inputDate = dialog.findViewById(R.id.input_book_date);
        EditText inputTime = dialog.findViewById(R.id.input_book_time);
        EditText inputMessage = dialog.findViewById(R.id.input_book_message);
        TextView txtError = dialog.findViewById(R.id.txt_book_error);
        TextView btnCancel = dialog.findViewById(R.id.btn_cancel_book_donation);
        TextView btnSubmit = dialog.findViewById(R.id.btn_submit_book_donation);

        // Radius Chips
        TextView chipAll = dialog.findViewById(R.id.chip_radius_all);
        TextView chip1 = dialog.findViewById(R.id.chip_radius_1km);
        TextView chip5 = dialog.findViewById(R.id.chip_radius_5km);
        TextView chip10 = dialog.findViewById(R.id.chip_radius_10km);
        TextView chip15 = dialog.findViewById(R.id.chip_radius_15km);
        TextView chip20 = dialog.findViewById(R.id.chip_radius_20km);
        TextView chip30 = dialog.findViewById(R.id.chip_radius_30km);
        TextView chip50 = dialog.findViewById(R.id.chip_radius_50km);
        TextView[] radiusChips = new TextView[]{chipAll, chip1, chip5, chip10, chip15, chip20, chip30, chip50};
        double[] radiusValues = new double[]{50.0, 1.0, 5.0, 10.0, 15.0, 20.0, 30.0, 50.0};
        final double[] currentRadiusFilter = new double[]{50.0};

        UserProfile curUser = repository.getCurrentUser();
        final String userGroup = curUser != null && curUser.getBloodGroup() != null ? curUser.getBloodGroup() : "O+";
        if (txtDonorGroup != null) txtDonorGroup.setText("🩸 " + userGroup);

        final List<BloodBankOption> masterBankList = new ArrayList<>();
        final List<BloodBankOption> filteredBankList = new ArrayList<>();
        final BloodBankOption[] selectedBankRef = new BloodBankOption[]{null};

        // Seed default regional blood banks
        masterBankList.add(new BloodBankOption("BB-001", "MSI Blood Bank Sangli", "Sangli Center, Maharashtra", "+91 233 2374501", 16.8580, 74.5880));
        masterBankList.add(new BloodBankOption("BB-002", "Bombay Blood Bank", "Station Road, Miraj, Maharashtra", "+91 233 2223301", 16.8510, 74.5720));
        masterBankList.add(new BloodBankOption("BB-003", "Shashwat Blood Bank", "Kupwad Road, Sangli, Maharashtra", "+91 233 2223302", 16.8640, 74.6010));
        masterBankList.add(new BloodBankOption("BB-004", "Sangli Civil Blood Bank", "Civil Hospital Complex, Sangli, Maharashtra", "+91 233 2374503", 16.8480, 74.5650));
        masterBankList.add(new BloodBankOption("BB-005", "Aachary Shree Tulsi Blood Bank", "Market Yard, Sangli, Maharashtra", "+91 233 2321456", 16.8550, 74.5800));

        // Merge from smartMapItemList if available
        for (SmartMapItem item : smartMapItemList) {
            if ("BLOOD_BANK".equalsIgnoreCase(item.type) && item.name != null && !item.name.isEmpty()) {
                boolean exists = false;
                for (BloodBankOption opt : masterBankList) {
                    if (opt.name.equalsIgnoreCase(item.name)) { exists = true; break; }
                }
                if (!exists) {
                    masterBankList.add(new BloodBankOption(item.id != null ? item.id : "BB-" + (masterBankList.size() + 1), item.name, item.address != null ? item.address : "Sangli", item.phone != null ? item.phone : "+91 233 2220000", item.lat, item.lng));
                }
            }
        }

        Runnable updateBankCard = () -> {
            if (selectedBankRef[0] != null) {
                BloodBankOption bank = selectedBankRef[0];
                if (txtBankName != null) txtBankName.setText(bank.name);
                double dist = calculateDistanceInKm(userLat, userLng, bank.lat, bank.lng);
                if (txtBankDistance != null) txtBankDistance.setText(String.format(Locale.US, "%.1f KM away", dist));
                if (txtBankLocation != null) txtBankLocation.setText("📍 " + bank.address);
                if (txtBankContact != null) txtBankContact.setText("📞 " + bank.phone);
            }
        };

        Runnable updateDropdownUI = () -> {
            filteredBankList.clear();
            for (BloodBankOption opt : masterBankList) {
                double dist = calculateDistanceInKm(userLat, userLng, opt.lat, opt.lng);
                if (dist <= currentRadiusFilter[0] || currentRadiusFilter[0] >= 50.0) {
                    filteredBankList.add(opt);
                }
            }
            if (filteredBankList.isEmpty()) {
                filteredBankList.addAll(masterBankList);
            }

            List<String> names = new ArrayList<>();
            for (BloodBankOption opt : filteredBankList) {
                double dist = calculateDistanceInKm(userLat, userLng, opt.lat, opt.lng);
                names.add(opt.name + " (" + String.format(Locale.US, "%.1f km", dist) + ")");
            }

            if (dropdownBank != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
                dropdownBank.setAdapter(adapter);

                // If current selection is in filtered list, keep it; else choose first
                boolean found = false;
                if (selectedBankRef[0] != null) {
                    for (BloodBankOption opt : filteredBankList) {
                        if (opt.id.equals(selectedBankRef[0].id) || opt.name.equalsIgnoreCase(selectedBankRef[0].name)) {
                            selectedBankRef[0] = opt;
                            found = true;
                            break;
                        }
                    }
                }
                if (!found && !filteredBankList.isEmpty()) {
                    selectedBankRef[0] = filteredBankList.get(0);
                }

                if (selectedBankRef[0] != null) {
                    double dist = calculateDistanceInKm(userLat, userLng, selectedBankRef[0].lat, selectedBankRef[0].lng);
                    dropdownBank.setText(selectedBankRef[0].name + " (" + String.format(Locale.US, "%.1f km", dist) + ")", false);
                }
            }
            updateBankCard.run();
        };

        updateDropdownUI.run();

        // Dynamically fetch and merge all blood banks from Firestore
        FirebaseFirestore.getInstance().collection("bloodBanks").get().addOnSuccessListener(snaps -> {
            if (snaps != null && !snaps.isEmpty()) {
                for (DocumentSnapshot doc : snaps.getDocuments()) {
                    String bName = doc.getString("name");
                    if (bName != null && !bName.isEmpty()) {
                        boolean exists = false;
                        for (BloodBankOption opt : masterBankList) {
                            if (opt.name.equalsIgnoreCase(bName) || opt.id.equalsIgnoreCase(doc.getId())) {
                                exists = true;
                                if (doc.getString("address") != null) opt.address = doc.getString("address");
                                if (doc.getString("phone") != null) opt.phone = doc.getString("phone");
                                if (doc.getDouble("latitude") != null) opt.lat = doc.getDouble("latitude");
                                if (doc.getDouble("longitude") != null) opt.lng = doc.getDouble("longitude");
                                break;
                            }
                        }
                        if (!exists) {
                            String addr = doc.getString("address") != null ? doc.getString("address") : (doc.getString("city") != null ? doc.getString("city") : "Sangli");
                            String ph = doc.getString("phone") != null ? doc.getString("phone") : (doc.getString("contact") != null ? doc.getString("contact") : "+91 233 2220000");
                            Double lat = doc.getDouble("latitude") != null ? doc.getDouble("latitude") : 16.8580;
                            Double lng = doc.getDouble("longitude") != null ? doc.getDouble("longitude") : 74.5880;
                            masterBankList.add(new BloodBankOption(doc.getId(), bName, addr, ph, lat, lng));
                        }
                    }
                }
                updateDropdownUI.run();
            }
        });

        // Radius Chips Click Listeners
        for (int i = 0; i < radiusChips.length; i++) {
            final int index = i;
            if (radiusChips[i] != null) {
                radiusChips[i].setOnClickListener(v -> {
                    currentRadiusFilter[0] = radiusValues[index];
                    for (int j = 0; j < radiusChips.length; j++) {
                        if (radiusChips[j] != null) {
                            if (j == index) {
                                radiusChips[j].setBackgroundResource(R.drawable.bg_button_primary);
                                radiusChips[j].setTextColor(ContextCompat.getColor(this, R.color.white));
                            } else {
                                radiusChips[j].setBackgroundResource(R.drawable.bg_chip_status_pending);
                                radiusChips[j].setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                            }
                        }
                    }
                    updateDropdownUI.run();
                });
            }
        }

        if (dropdownBank != null) {
            View.OnClickListener dropClick = v -> dropdownBank.showDropDown();
            dropdownBank.setOnClickListener(dropClick);
            View layoutBank = dialog.findViewById(R.id.layout_book_bank_dropdown);
            if (layoutBank instanceof com.google.android.material.textfield.TextInputLayout) {
                ((com.google.android.material.textfield.TextInputLayout) layoutBank).setEndIconOnClickListener(dropClick);
            }
            dropdownBank.setOnItemClickListener((parent, view1, position, id) -> {
                if (position >= 0 && position < filteredBankList.size()) {
                    selectedBankRef[0] = filteredBankList.get(position);
                    updateBankCard.run();
                    if (txtError != null) txtError.setVisibility(View.GONE);
                }
            });
        }

        // Date Picker
        if (inputDate != null) {
            View.OnClickListener dateClick = v -> {
                Calendar c = Calendar.getInstance();
                DatePickerDialog dp = new DatePickerDialog(this, (view1, year, month, dayOfMonth) -> {
                    inputDate.setText(String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year));
                    if (txtError != null) txtError.setVisibility(View.GONE);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dp.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                dp.show();
            };
            inputDate.setOnClickListener(dateClick);
            if (inputDate.getParent() instanceof View) {
                View parent = (View) inputDate.getParent();
                parent.setOnClickListener(dateClick);
                if (parent.getParent() instanceof com.google.android.material.textfield.TextInputLayout) {
                    com.google.android.material.textfield.TextInputLayout til = (com.google.android.material.textfield.TextInputLayout) parent.getParent();
                    til.setOnClickListener(dateClick);
                    til.setEndIconOnClickListener(dateClick);
                }
            }
            inputDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(new Date()));
        }

        // Time Picker
        if (inputTime != null) {
            View.OnClickListener timeClick = v -> {
                Calendar c = Calendar.getInstance();
                int curHour = c.get(Calendar.HOUR_OF_DAY);
                int curMin = c.get(Calendar.MINUTE);
                TimePickerDialog tp = new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                    inputTime.setText(String.format(Locale.US, "%02d:%02d %s", hourOfDay % 12 == 0 ? 12 : hourOfDay % 12, minute, hourOfDay >= 12 ? "PM" : "AM"));
                    if (txtError != null) txtError.setVisibility(View.GONE);
                }, curHour, curMin, false);
                tp.show();
            };
            inputTime.setOnClickListener(timeClick);
            if (inputTime.getParent() instanceof View) {
                View parent = (View) inputTime.getParent();
                parent.setOnClickListener(timeClick);
                if (parent.getParent() instanceof com.google.android.material.textfield.TextInputLayout) {
                    com.google.android.material.textfield.TextInputLayout til = (com.google.android.material.textfield.TextInputLayout) parent.getParent();
                    til.setOnClickListener(timeClick);
                    til.setEndIconOnClickListener(timeClick);
                }
            }
            inputTime.setText("10:30 AM");
        }

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String dateStr = inputDate != null ? inputDate.getText().toString().trim() : "";
                String timeStr = inputTime != null ? inputTime.getText().toString().trim() : "";
                String msgStr = inputMessage != null ? inputMessage.getText().toString().trim() : "";

                if (selectedBankRef[0] == null) {
                    if (txtError != null) {
                        txtError.setText("Please select a Blood Bank from the list.");
                        txtError.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                if (TextUtils.isEmpty(dateStr) || TextUtils.isEmpty(timeStr)) {
                    if (txtError != null) {
                        txtError.setText("Please select valid date and time for your appointment.");
                        txtError.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                if (txtError != null) txtError.setVisibility(View.GONE);
                btnSubmit.setEnabled(false);
                btnSubmit.setText("Booking Appointment...");

                final BloodBankOption chosenBank = selectedBankRef[0];
                final String donorUid = curUser != null ? curUser.getUid() : "USR-DNR-01";
                final String donorName = curUser != null && curUser.getName() != null ? curUser.getName() : "Donor";
                final String donorPhone = curUser != null && curUser.getPhone() != null ? curUser.getPhone() : "+91 98000 00000";
                final String aptId = "APT-2026-" + (System.currentTimeMillis() % 100000);

                // Duplicate Check: Check if active appointment exists on same date with same bank
                FirebaseFirestore.getInstance().collection("donorAppointments")
                        .whereEqualTo("donorUid", donorUid)
                        .get()
                        .addOnSuccessListener(querySnaps -> {
                            boolean hasDuplicate = false;
                            if (querySnaps != null) {
                                for (DocumentSnapshot d : querySnaps.getDocuments()) {
                                    String st = d.getString("status");
                                    String dDate = d.getString("appointmentDate");
                                    String bId = d.getString("bloodBankId");
                                    if (st != null && !st.equalsIgnoreCase("CANCELLED") && !st.equalsIgnoreCase("REJECTED") &&
                                            dateStr.equals(dDate) && chosenBank.id.equals(bId)) {
                                        hasDuplicate = true;
                                        break;
                                    }
                                }
                            }

                            if (hasDuplicate) {
                                btnSubmit.setEnabled(true);
                                btnSubmit.setText("Book Donation 🩸");
                                if (txtError != null) {
                                    txtError.setText("You already have an appointment scheduled with " + chosenBank.name + " on " + dateStr + ".");
                                    txtError.setVisibility(View.VISIBLE);
                                }
                                return;
                            }

                            Map<String, Object> aptMap = new HashMap<>();
                            aptMap.put("id", aptId);
                            aptMap.put("appointmentId", aptId);
                            aptMap.put("donorUid", donorUid);
                            aptMap.put("donorId", donorUid);
                            aptMap.put("donorAuthUid", donorUid);
                            aptMap.put("donorName", donorName);
                            aptMap.put("donorPhone", donorPhone);
                            aptMap.put("donorEmail", curUser != null ? curUser.getEmail() : "");
                            aptMap.put("donorBloodGroup", userGroup);
                            aptMap.put("bloodGroup", userGroup);
                            aptMap.put("bloodBankId", chosenBank.id);
                            aptMap.put("bloodBankName", chosenBank.name);
                            aptMap.put("bloodBankAddress", chosenBank.address);
                            aptMap.put("bloodBankPhone", chosenBank.phone);
                            aptMap.put("appointmentDate", dateStr);
                            aptMap.put("date", dateStr);
                            aptMap.put("appointmentTime", timeStr);
                            aptMap.put("time", timeStr);
                            aptMap.put("message", msgStr);
                            aptMap.put("notes", msgStr);
                            aptMap.put("status", "PENDING");
                            aptMap.put("createdAt", new SimpleDateFormat("hh:mm a", Locale.US).format(new Date()));
                            aptMap.put("createdAtTimestamp", System.currentTimeMillis());
                            aptMap.put("updatedAt", System.currentTimeMillis());

                            FirebaseFirestore.getInstance().collection("donorAppointments").document(aptId).set(aptMap)
                                    .addOnSuccessListener(aVoid -> {
                                        // Send notification to the Blood Bank
                                        String notifId = "NOTIF-BB-APT-" + System.currentTimeMillis();
                                        Map<String, Object> notif = new HashMap<>();
                                        notif.put("notificationId", notifId);
                                        notif.put("userId", chosenBank.id);
                                        notif.put("bloodBankId", chosenBank.id);
                                        notif.put("recipientFacilityId", chosenBank.id);
                                        notif.put("targetRole", "BLOOD_BANK");
                                        notif.put("title", "🩸 New Donor Appointment Scheduled");
                                        notif.put("message", donorName + " (" + userGroup + ") scheduled a blood donation appointment for " + dateStr + " at " + timeStr + ".");
                                        notif.put("type", "APPOINTMENT_CREATED");
                                        notif.put("relatedId", aptId);
                                        notif.put("timestamp", System.currentTimeMillis());
                                        notif.put("createdAt", System.currentTimeMillis());
                                        notif.put("isRead", false);
                                        notif.put("read", false);
                                        FirebaseFirestore.getInstance().collection("notifications").document(notifId).set(notif);

                                        dialog.dismiss();
                                        repository.addAuditLog(donorUid, "DONOR", "BOOK_APPOINTMENT", aptId, new SimpleDateFormat("hh:mm a", Locale.US).format(new Date()), "NONE", "PENDING", "Booked donation appointment at " + chosenBank.name);
                                        Toast.makeText(this, "✔ Blood donation scheduled with " + chosenBank.name + "!", Toast.LENGTH_LONG).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        btnSubmit.setEnabled(true);
                                        btnSubmit.setText("Book Donation 🩸");
                                        if (txtError != null) {
                                            txtError.setText("Failed to book appointment: " + e.getMessage());
                                            txtError.setVisibility(View.VISIBLE);
                                        }
                                        Toast.makeText(this, "Booking failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("Book Donation 🩸");
                            if (txtError != null) {
                                txtError.setText("Network error: " + e.getMessage());
                                txtError.setVisibility(View.VISIBLE);
                            }
                        });
            });
        }

        dialog.show();
    }

    private void showCreateBloodRequestDialog(String prefillBankOrGroup) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_create_blood_request);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        View btnClose = dialog.findViewById(R.id.btn_close_create_request_dialog);
        AutoCompleteTextView actvBank = dialog.findViewById(R.id.input_request_blood_bank);
        AutoCompleteTextView actvGroup = dialog.findViewById(R.id.input_request_blood_group);
        EditText inputUnits = dialog.findViewById(R.id.input_request_units);
        AutoCompleteTextView actvComp = dialog.findViewById(R.id.input_request_component);
        AutoCompleteTextView actvUrgency = dialog.findViewById(R.id.input_request_urgency);
        EditText inputPatient = dialog.findViewById(R.id.input_request_patient_ref);
        EditText inputNotes = dialog.findViewById(R.id.input_request_notes);
        TextView txtError = dialog.findViewById(R.id.txt_create_request_error);
        View btnSubmit = dialog.findViewById(R.id.btn_submit_blood_request);

        final List<String> bankNames = new ArrayList<>();
        final List<String> bankIds = new ArrayList<>();

        bankNames.add("MSI Blood Bank Sangli");
        bankIds.add("BB-001");
        bankNames.add("Bombay Blood Bank");
        bankIds.add("BB-002");
        bankNames.add("Shashwat Blood Bank");
        bankIds.add("BB-003");
        bankNames.add("Sangli Civil Blood Bank");
        bankIds.add("BB-004");

        ArrayAdapter<String> bankAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bankNames);
        if (actvBank != null) {
            actvBank.setAdapter(bankAdapter);
            actvBank.setOnClickListener(v -> actvBank.showDropDown());
            if (prefillBankOrGroup != null && prefillBankOrGroup.startsWith("BB-")) {
                for (int i = 0; i < bankIds.size(); i++) {
                    if (bankIds.get(i).equalsIgnoreCase(prefillBankOrGroup)) {
                        actvBank.setText(bankNames.get(i), false);
                        break;
                    }
                }
            } else {
                actvBank.setText(bankNames.get(0), false);
            }
        }

        // Fetch registered blood banks from Firestore to dynamically enrich list
        FirebaseFirestore.getInstance().collection("bloodBanks").get().addOnSuccessListener(snaps -> {
            if (snaps != null && !snaps.isEmpty()) {
                for (DocumentSnapshot d : snaps.getDocuments()) {
                    String n = d.getString("name");
                    String bId = d.getString("bloodBankId") != null ? d.getString("bloodBankId") : d.getId();
                    if (n != null && !bankNames.contains(n)) {
                        bankNames.add(n);
                        bankIds.add(bId);
                    }
                }
                bankAdapter.notifyDataSetChanged();
            }
        });

        String[] groups = new String[]{"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-", "Bombay (Oh)"};
        if (actvGroup != null) {
            actvGroup.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, groups));
            actvGroup.setOnClickListener(v -> actvGroup.showDropDown());
            if (prefillBankOrGroup != null && !prefillBankOrGroup.startsWith("BB-")) {
                actvGroup.setText(prefillBankOrGroup, false);
            } else {
                actvGroup.setText("O+", false);
            }
        }

        String[] comps = new String[]{"Packed RBC", "Whole Blood", "Platelets", "FFP", "Cryoprecipitate"};
        if (actvComp != null) {
            actvComp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, comps));
            actvComp.setOnClickListener(v -> actvComp.showDropDown());
            actvComp.setText("Packed RBC", false);
        }

        String[] urgencies = new String[]{"Normal", "High", "Critical Emergency"};
        if (actvUrgency != null) {
            actvUrgency.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, urgencies));
            actvUrgency.setOnClickListener(v -> actvUrgency.showDropDown());
            actvUrgency.setText("Normal", false);
        }

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String chosenBank = actvBank != null ? actvBank.getText().toString().trim() : "";
                String group = actvGroup != null ? actvGroup.getText().toString().trim() : "";
                String unitsStr = inputUnits != null ? inputUnits.getText().toString().trim() : "";
                String comp = actvComp != null ? actvComp.getText().toString().trim() : "Packed RBC";
                String urgency = actvUrgency != null ? actvUrgency.getText().toString().trim() : "Normal";
                String patientRef = inputPatient != null ? inputPatient.getText().toString().trim() : "Patient Requisition";
                String notes = inputNotes != null ? inputNotes.getText().toString().trim() : "";

                int units = 0;
                try {
                    units = Integer.parseInt(unitsStr);
                } catch (Exception ignored) {}

                if (TextUtils.isEmpty(group) || units <= 0) {
                    if (txtError != null) {
                        txtError.setText("Please enter valid blood group and quantity.");
                        txtError.setVisibility(View.VISIBLE);
                    }
                    return;
                }

                if (txtError != null) txtError.setVisibility(View.GONE);
                btnSubmit.setEnabled(false);

                UserProfile curUser = repository.getCurrentUser();
                final String hospUid = curUser != null ? curUser.getUid() : "HOS-8842";
                final String hospName = curUser != null && curUser.getDisplayName() != null ? curUser.getDisplayName() : "St. Jude General Hospital";
                final String reqId = "REQ-2026-" + (System.currentTimeMillis() % 100000);
                final String timeStr = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());

                String computedTargetBankId = "BB-001";
                for (int i = 0; i < bankNames.size(); i++) {
                    if (bankNames.get(i).equalsIgnoreCase(chosenBank)) {
                        computedTargetBankId = bankIds.get(i);
                        break;
                    }
                }
                final String targetBankId = computedTargetBankId;
                final int finalUnits = units;
                final String finalGroup = group;
                final String finalComp = comp;
                final String finalUrgency = urgency;
                final String finalChosenBank = chosenBank;

                Map<String, Object> reqMap = new HashMap<>();
                reqMap.put("requestId", reqId);
                reqMap.put("id", reqId);
                reqMap.put("hospitalId", hospUid);
                reqMap.put("hospitalUid", hospUid);
                reqMap.put("hospitalName", hospName);
                reqMap.put("bloodGroup", finalGroup);
                reqMap.put("quantity", finalUnits);
                reqMap.put("units", finalUnits);
                reqMap.put("requiredUnits", finalUnits);
                reqMap.put("component", finalComp);
                reqMap.put("urgency", finalUrgency);
                reqMap.put("priority", finalUrgency);
                reqMap.put("patientRef", patientRef);
                reqMap.put("notes", notes);
                reqMap.put("status", "Pending");
                reqMap.put("targetBankId", targetBankId);
                reqMap.put("bloodBankId", targetBankId);
                reqMap.put("targetBankName", finalChosenBank);
                reqMap.put("bloodBankName", finalChosenBank);
                reqMap.put("location", hospName + ", Sangli");
                reqMap.put("locationAddress", hospName + ", Sangli");
                reqMap.put("createdAt", timeStr);
                reqMap.put("createdAtTimestamp", System.currentTimeMillis());
                reqMap.put("updatedAt", System.currentTimeMillis());
                reqMap.put("latitude", userLat);
                reqMap.put("longitude", userLng);

                FirebaseFirestore.getInstance().collection("bloodRequests").document(reqId).set(reqMap)
                        .addOnSuccessListener(aVoid -> {
                            Map<String, Object> notif = new HashMap<>();
                            notif.put("userId", targetBankId);
                            notif.put("bloodBankId", targetBankId);
                            notif.put("recipientFacilityId", targetBankId);
                            notif.put("targetRole", "BLOOD_BANK");
                            notif.put("title", "🏥 New Requisition from " + hospName);
                            notif.put("message", hospName + " requested " + finalUnits + " units of " + finalGroup + " (" + finalComp + ") with urgency: " + finalUrgency + ".");
                            notif.put("type", "REQUEST_CREATED");
                            notif.put("relatedId", reqId);
                            notif.put("timestamp", System.currentTimeMillis());
                            notif.put("createdAt", System.currentTimeMillis());
                            notif.put("isRead", false);
                            notif.put("read", false);
                            FirebaseFirestore.getInstance().collection("notifications").add(notif);

                            dialog.dismiss();
                            repository.addAuditLog(hospUid, "HOSPITAL", "CREATE_REQUEST", reqId, timeStr, "NONE", "PENDING", "Created blood requisition for " + finalUnits + " units of " + finalGroup);
                            Toast.makeText(this, "✔ Requisition " + reqId + " submitted to " + finalChosenBank, Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            if (txtError != null) {
                                txtError.setText("Failed to submit request: " + e.getMessage());
                                txtError.setVisibility(View.VISIBLE);
                            }
                            btnSubmit.setEnabled(true);
                        });
            });
        }

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void showEmergencyRequestDialog(String prefillGroup, Integer prefillUnits, String prefillHospital, Double lat, Double lng) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_emergency_request);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        View btnClose = dialog.findViewById(R.id.btn_close_emergency_dialog);
        AutoCompleteTextView actvGroup = dialog.findViewById(R.id.input_emergency_blood_group);
        EditText inputUnits = dialog.findViewById(R.id.input_emergency_units);
        EditText inputPatient = dialog.findViewById(R.id.input_emergency_patient_ref);
        View btnSubmit = dialog.findViewById(R.id.btn_submit_emergency_request);

        String[] groups = new String[]{"O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+", "Bombay (Oh)"};
        if (actvGroup != null) {
            actvGroup.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, groups));
            actvGroup.setOnClickListener(v -> actvGroup.showDropDown());
            if (prefillGroup != null) {
                actvGroup.setText(prefillGroup, false);
            } else {
                actvGroup.setText("O-", false);
            }
        }

        if (inputUnits != null) {
            inputUnits.setText(prefillUnits != null ? String.valueOf(prefillUnits) : "4");
        }

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String group = actvGroup != null ? actvGroup.getText().toString().trim() : "O-";
                String unitsStr = inputUnits != null ? inputUnits.getText().toString().trim() : "4";
                String patient = inputPatient != null ? inputPatient.getText().toString().trim() : "Trauma Patient";

                int units = 4;
                try {
                    units = Integer.parseInt(unitsStr);
                } catch (Exception ignored) {}

                UserProfile curUser = repository.getCurrentUser();
                final String hospUid = curUser != null ? curUser.getUid() : "HOS-8842";
                final String hospName = curUser != null && curUser.getDisplayName() != null ? curUser.getDisplayName() : (prefillHospital != null ? prefillHospital : "St. Jude General Hospital");
                final String reqId = "EMG-2026-" + (System.currentTimeMillis() % 100000);
                final String timeStr = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());
                final int finalUnits = units;
                final String finalGroup = group;

                Map<String, Object> emgMap = new HashMap<>();
                emgMap.put("requestId", reqId);
                emgMap.put("id", reqId);
                emgMap.put("hospitalId", hospUid);
                emgMap.put("hospitalUid", hospUid);
                emgMap.put("hospitalName", hospName);
                emgMap.put("bloodGroup", finalGroup);
                emgMap.put("quantity", finalUnits);
                emgMap.put("units", finalUnits);
                emgMap.put("requiredUnits", finalUnits);
                emgMap.put("component", "Packed RBC");
                emgMap.put("urgency", "EMERGENCY");
                emgMap.put("priority", "EMERGENCY");
                emgMap.put("patientRef", patient);
                emgMap.put("notes", "🚨 HIGH PRIORITY EMERGENCY - IMMEDIATE BROADCAST");
                emgMap.put("status", "Pending");
                emgMap.put("location", hospName + ", Sangli");
                emgMap.put("locationAddress", hospName + ", Sangli");
                emgMap.put("createdAt", timeStr);
                emgMap.put("createdAtTimestamp", System.currentTimeMillis());
                emgMap.put("updatedAt", System.currentTimeMillis());
                emgMap.put("latitude", lat != null ? lat : userLat);
                emgMap.put("longitude", lng != null ? lng : userLng);

                FirebaseFirestore.getInstance().collection("bloodRequests").document(reqId).set(emgMap)
                        .addOnSuccessListener(aVoid -> {
                            dialog.dismiss();
                            repository.addAuditLog(hospUid, "HOSPITAL", "EMERGENCY_BROADCAST", reqId, timeStr, "NONE", "EMERGENCY_ACTIVE", "Broadcasted emergency for " + finalUnits + " units of " + finalGroup);
                            Toast.makeText(this, "🚨 Priority Emergency Request Broadcasted to Regional Donors & Blood Banks!", Toast.LENGTH_LONG).show();
                        });
            });
        }

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void showLowStockDetailsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_low_stock_details);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        View btnClose = dialog.findViewById(R.id.btn_close_low_stock_dialog);
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        View btnRequest = dialog.findViewById(R.id.btn_low_stock_request_action);
        if (btnRequest != null) {
            btnRequest.setOnClickListener(v -> {
                dialog.dismiss();
                showCreateBloodRequestDialog("O-");
            });
        }

        dialog.show();
    }

    private void showAIAssistantDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_ai_assistant);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        View btnClose = dialog.findViewById(R.id.btn_close_ai_dialog);
        EditText inputQuery = dialog.findViewById(R.id.input_ai_query);
        View btnSend = dialog.findViewById(R.id.btn_send_ai_query);
        TextView responseText = dialog.findViewById(R.id.ai_response_text);
        View chipCritical = dialog.findViewById(R.id.chip_ai_critical);
        View chipExpiring = dialog.findViewById(R.id.chip_ai_expiring);
        View chipRequests = dialog.findViewById(R.id.chip_ai_requests);

        if (btnSend != null) {
            btnSend.setOnClickListener(v -> {
                String q = inputQuery != null ? inputQuery.getText().toString().trim() : "";
                if (TextUtils.isEmpty(q)) return;
                String resp = AIAssistantEngine.generateResponse(q);
                if (responseText != null) responseText.setText(resp);
                if (inputQuery != null) inputQuery.setText("");
            });
        }

        if (chipCritical != null) {
            chipCritical.setOnClickListener(v -> {
                String resp = AIAssistantEngine.generateResponse("critical groups");
                if (responseText != null) responseText.setText(resp);
            });
        }

        if (chipExpiring != null) {
            chipExpiring.setOnClickListener(v -> {
                String resp = AIAssistantEngine.generateResponse("expiring units");
                if (responseText != null) responseText.setText(resp);
            });
        }

        if (chipRequests != null) {
            chipRequests.setOnClickListener(v -> {
                String resp = AIAssistantEngine.generateResponse("pending requests");
                if (responseText != null) responseText.setText(resp);
            });
        }

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showQRVerificationDialog(BloodRequest req) {
        if (req == null) return;
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_qr_verification);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView txtToken = dialog.findViewById(R.id.txt_qr_token);
        View btnClose = dialog.findViewById(R.id.btn_close_qr_dialog);
        View btnConfirm = dialog.findViewById(R.id.btn_confirm_handover);

        if (txtToken != null) txtToken.setText("QR-" + req.getRequestId());
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                String timeStr = new SimpleDateFormat("hh:mm a", Locale.US).format(new Date());
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", "Fulfilled");
                updates.put("fulfilledAt", timeStr);
                FirebaseFirestore.getInstance().collection("bloodRequests").document(req.getRequestId()).update(updates);

                UserProfile curUser = repository.getCurrentUser();
                String uid = curUser != null ? curUser.getUid() : "BB-101";
                repository.addAuditLog(uid, "BLOOD_BANK", "FULFILL_HANDOVER", req.getRequestId(), timeStr, "IN_TRANSIT", "FULFILLED", "Handover verified via OTP & QR.");

                dialog.dismiss();
                Toast.makeText(this, "✔ Handover Verified! Request marked as Fulfilled.", Toast.LENGTH_SHORT).show();
            });
        }

        dialog.show();
    }

    private void showEditHospitalProfileDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_hospital_profile);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        UserProfile curUser = repository.getCurrentUser();
        EditText editName = dialog.findViewById(R.id.edit_hospital_name);
        EditText editPhone = dialog.findViewById(R.id.edit_hospital_phone);
        EditText editCity = dialog.findViewById(R.id.edit_hospital_city);
        EditText editAddr = dialog.findViewById(R.id.edit_hospital_address);
        View btnClose = dialog.findViewById(R.id.btn_close_edit_profile);
        View btnSave = dialog.findViewById(R.id.btn_save_hospital_profile);

        if (editName != null && curUser != null) editName.setText(curUser.getDisplayName() != null ? curUser.getDisplayName() : curUser.getName());
        if (editPhone != null && curUser != null) editPhone.setText(curUser.getPhone());
        if (editCity != null && curUser != null) editCity.setText(curUser.getCity());
        if (editAddr != null && curUser != null) editAddr.setText(curUser.getLocationAddress());

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String nameStr = editName != null ? editName.getText().toString().trim() : "";
                String phoneStr = editPhone != null ? editPhone.getText().toString().trim() : "";
                String cityStr = editCity != null ? editCity.getText().toString().trim() : "";
                String addrStr = editAddr != null ? editAddr.getText().toString().trim() : "";

                if (TextUtils.isEmpty(nameStr)) {
                    Toast.makeText(this, "Please enter hospital name", Toast.LENGTH_SHORT).show();
                    return;
                }

                String uid = curUser != null ? curUser.getUid() : "HOS-8842";
                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put("name", nameStr);
                updateMap.put("displayName", nameStr);
                updateMap.put("phone", phoneStr);
                updateMap.put("city", cityStr);
                updateMap.put("locationAddress", addrStr);

                FirebaseFirestore.getInstance().collection("users").document(uid).set(updateMap, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            if (curUser != null) {
                                curUser.setName(nameStr);
                                curUser.setDisplayName(nameStr);
                                curUser.setPhone(phoneStr);
                                curUser.setCity(cityStr);
                                curUser.setLocationAddress(addrStr);
                            }
                            dialog.dismiss();
                            Toast.makeText(this, "✔ Profile updated successfully.", Toast.LENGTH_SHORT).show();
                            loadView(R.layout.view_profile, this::bindProfileView);
                        });
            });
        }

        dialog.show();
    }

    private void showEditDonorProfileDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_donor_profile);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        UserProfile curUser = repository.getCurrentUser();
        EditText editEmail = dialog.findViewById(R.id.edit_donor_email);
        EditText editName = dialog.findViewById(R.id.edit_donor_name);
        EditText editPhone = dialog.findViewById(R.id.edit_donor_phone);
        AutoCompleteTextView editGroup = dialog.findViewById(R.id.edit_donor_blood_group);
        EditText editCity = dialog.findViewById(R.id.edit_donor_city);
        EditText editAddr = dialog.findViewById(R.id.edit_donor_address);
        View btnClose = dialog.findViewById(R.id.btn_close_edit_donor_profile);
        View btnCancel = dialog.findViewById(R.id.btn_cancel_edit_donor_profile);
        View btnSave = dialog.findViewById(R.id.btn_save_donor_profile);

        if (editEmail != null && curUser != null) editEmail.setText(curUser.getEmail());
        if (editName != null && curUser != null) editName.setText(curUser.getName());
        if (editPhone != null && curUser != null) editPhone.setText(curUser.getPhone());
        if (editCity != null && curUser != null) editCity.setText(curUser.getCity());
        if (editAddr != null && curUser != null) editAddr.setText(curUser.getLocationAddress());

        String[] groups = new String[]{"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-", "Bombay (Oh)"};
        if (editGroup != null) {
            editGroup.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, groups));
            editGroup.setOnClickListener(v -> editGroup.showDropDown());
            if (curUser != null && curUser.getBloodGroup() != null) {
                editGroup.setText(curUser.getBloodGroup(), false);
            }
        }

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String nameStr = editName != null ? editName.getText().toString().trim() : "";
                String phoneStr = editPhone != null ? editPhone.getText().toString().trim() : "";
                String groupStr = editGroup != null ? editGroup.getText().toString().trim() : "O+";
                String cityStr = editCity != null ? editCity.getText().toString().trim() : "";
                String addrStr = editAddr != null ? editAddr.getText().toString().trim() : "";

                if (TextUtils.isEmpty(nameStr)) {
                    Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }

                String uid = curUser != null ? curUser.getUid() : "USR-DNR-01";
                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put("name", nameStr);
                updateMap.put("phone", phoneStr);
                updateMap.put("bloodGroup", groupStr);
                updateMap.put("city", cityStr);
                updateMap.put("locationAddress", addrStr);

                FirebaseFirestore.getInstance().collection("users").document(uid).set(updateMap, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            if (curUser != null) {
                                curUser.setName(nameStr);
                                curUser.setPhone(phoneStr);
                                curUser.setBloodGroup(groupStr);
                                curUser.setCity(cityStr);
                                curUser.setLocationAddress(addrStr);
                            }
                            dialog.dismiss();
                            Toast.makeText(this, "✔ Donor profile updated successfully.", Toast.LENGTH_SHORT).show();
                            loadView(R.layout.view_profile, this::bindProfileView);
                        });
            });
        }

        dialog.show();
    }
}

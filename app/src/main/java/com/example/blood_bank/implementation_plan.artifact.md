# Implementation Plan - SmartBlood Fixes

This plan addresses the two required fixes for the SmartBlood app: functional Call/Directions buttons in Public Emergency Search and ensuring passwords are not prefilled in login screens.

## Proposed Changes

### 1. Public Emergency Search - Call & Directions

#### [MODIFY] [MainActivity.java](file:///E:/Blood_bank/app/src/main/java/com/example/blood_bank/MainActivity.java)
- Ensure the "Call Bank" button (`btn_public_call_1`) uses `Intent.ACTION_DIAL` with phone number `8483912001`.
- Ensure the "Directions" button (`btn_public_directions_1`) opens Google Maps or a fallback browser with the coordinates `16.8580, 74.5880` (associated with the existing bank data).
- *Note: These are mostly implemented in the current version of the file, but I will verify and reinforce the implementation to ensure they are "REAL and functional" as per requirements.*

### 2. Blood Bank Login - Password Security

#### [MODIFY] [MainActivity.java](file:///E:/Blood_bank/app/src/main/java/com/example/blood_bank/MainActivity.java)
- In `bindBloodBankLoginView`, ensure `inputPassword.setText("")` is called correctly.
- In `bindDonorLoginView` and `bindHospitalLoginView`, change the hardcoded password prefilling (e.g., `omkar123`, `jadhav123`) to an empty string `""` to satisfy the requirement for "ALL login screens" in the context of the Blood Bank portal app.
- Update the "Quick Select" chips for Blood Banks to also set the password field to empty.

## Verification Plan

### Automated Tests
- Run `.\gradlew.bat assembleDebug --console=plain` to ensure the project builds correctly.

### Manual Verification
- Deploy to device/emulator.
- **Task 1**:
    - Open "Public Emergency Search".
    - Tap "Search Blood Reserves".
    - Tap "CALL BANK" -> Verify dialer opens with `8483912001`.
    - Tap "DIRECTIONS" -> Verify Google Maps (or fallback) opens at the bank's location.
- **Task 2**:
    - Open Blood Bank Login (and other roles).
    - Verify Email is prefilled (e.g., `bombay@gmail.com`).
    - Verify Password field is EMPTY.
    - Manually type correct password and verify login works.
    - Tap Quick Select chips and verify password remains/becomes empty.

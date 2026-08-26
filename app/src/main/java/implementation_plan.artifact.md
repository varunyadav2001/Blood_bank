# Implementation Plan - Set Premium App Icon (Choice 3: Community Care)

This plan outlines the steps to replace the current app icon with a premium "Community Care" icon, featuring a red blood drop, community figures, and a heartbeat concept.

## Proposed Changes

### 1. Update Launcher Icon Resources

#### [MODIFY] [ic_launcher_foreground.xml](file:///E:/Blood_bank/app/src/main/res/drawable/ic_launcher_foreground.xml)
- Redesign the foreground to include:
    - A stylized red blood drop.
    - A white heartbeat (ECG) pulse line.
    - Stylized community/people figures at the base.
- Ensure proper centering and scaling for adaptive icon support.

#### [MODIFY] [ic_launcher_background.xml](file:///E:/Blood_bank/app/src/main/res/drawable/ic_launcher_background.xml)
- Change the background to a premium midnight blue/dark theme (`#0A0F1D`) to match the app's aesthetic.

#### [MODIFY] [colors.xml](file:///E:/Blood_bank/app/src/main/res/values/colors.xml) (if needed)
- Ensure all necessary premium colors are defined.

### 2. Verify Manifest and Adaptive Icon XMLs

#### [MODIFY] [ic_launcher.xml](file:///E:/Blood_bank/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml)
#### [MODIFY] [ic_launcher_round.xml](file:///E:/Blood_bank/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml)
- Ensure they point to the updated foreground and background.

### 3. Build and Install

- Run `.\gradlew.bat assembleDebug --console=plain`.
- Run `.\gradlew.bat installDebug`.

## Verification Plan

### Manual Verification
- Verify the new "Community Care" icon appears on the device:
    - Home screen (Adaptive shape support).
    - App drawer.
    - Recent apps.
    - Settings -> Apps.

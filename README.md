# 🩸 SmartBlood • Smart Blood Donation & Emergency Management System

> A modern Android-based digital platform connecting **Donors, Hospitals, and Blood Banks** for blood donation, emergency blood requests, inventory management, and real-time coordination.

SmartBlood is developed using **Java, XML, Android, and Firebase** to simplify blood donation and emergency blood management through one unified ecosystem.

---

## 🚀 Quick Links & Live Demo

| Option | Link |
|---|---|
| 🌐 Official Landing Page | [Open SmartBlood Website](https://varunyadav2001.github.io/Blood_bank/) |
| 🚀 Live Demo | [Open SmartBlood Live Demo](https://varunyadav2001.github.io/Blood_bank/demo.html) |
| 📥 Download APK | [Download Latest APK](https://github.com/varunyadav2001/Blood_bank/releases/latest/download/app-debug.apk) |
| 📦 GitHub Repository | [View Source Code](https://github.com/varunyadav2001/Blood_bank) |

### 🎯 Demo Flow

**Landing Page → LIVE DEMO → SmartBlood Dashboard**

The Live Demo provides a browser-based demonstration of the SmartBlood management interface.

---

# 🌟 Key Highlights

- 🩸 **Smart Blood Inventory Management**
- 👤 **Donor Registration & Management**
- 🏥 **Hospital Blood Requests**
- 🚨 **Emergency Blood Request Management**
- 📅 **Blood Donation Appointment Scheduling**
- 🔄 **Real-Time Blood Stock Monitoring**
- 📊 **Blood Availability Dashboard**
- 🔐 **Firebase Authentication**
- ☁️ **Cloud Firestore Integration**
- 🔔 **Emergency Coordination**
- 📱 **Native Android Application**
- 🌐 **GitHub Pages Live Demo**
- 📥 **Downloadable Android APK**

---

# 👥 User Roles

SmartBlood provides dedicated workflows for three major users.

## 👤 1. Donor

Donors can:

- Create an account
- Maintain personal profile
- Add blood group information
- View donation history
- Schedule donation appointments
- Respond to emergency blood requirements
- Track donation-related information

---

## 🏥 2. Hospital

Hospitals can:

- Register with the system
- Create blood requests
- Submit emergency requirements
- Specify required blood group
- Track request status
- Coordinate with blood banks
- Monitor request fulfillment

---

## 🩸 3. Blood Bank

Blood banks can:

- Manage blood inventory
- Monitor available blood units
- View hospital requests
- Approve or process requests
- Manage blood stock
- Coordinate blood transfers
- Monitor emergency requirements
- Maintain real-time inventory information

---

# ⚡ Core Features

## 🔐 Authentication & Security

- Firebase Authentication
- Secure user login
- Role-based access
- Protected application workflows
- User-specific dashboards
- Firebase security rules

---

## 👤 Donor Management

- Donor registration
- Blood group profile
- Donation history
- Donor availability
- Emergency donor coordination
- Appointment management

---

## 🚨 Emergency Blood Requests

Hospitals can create urgent blood requirements.

A request can contain:

- Blood group
- Required units
- Hospital information
- Emergency priority
- Required date
- Request status

This helps blood banks and donors respond quickly to critical requirements.

---

## 🩸 Blood Inventory Management

Blood banks can monitor:

- Blood groups
- Available units
- Stock status
- Hospital requests
- Blood transfers
- Emergency requirements

The dashboard provides a centralized view of blood availability.

---

## 📅 Donation Appointment

Donors can schedule blood donation appointments.

The system can maintain:

- Appointment date
- Appointment status
- Donor information
- Donation history

---

## 🔄 Real-Time Data Synchronization

SmartBlood uses Firebase Cloud Firestore to provide real-time synchronization between application users and management dashboards.

Changes in:

- Blood inventory
- Blood requests
- Appointments
- User information
- Request status

can be synchronized with the backend.

---

# 📊 Live Dashboard

The SmartBlood Live Demo includes a centralized dashboard displaying important system information such as:

- 🩸 Blood Units Available
- 👤 Registered Donors
- 🏥 Partner Hospitals
- 🚨 Active Blood Requests
- 👤 Donor Management
- 🏥 Hospital Requests
- 🩸 Blood Bank Management
- 📅 Appointment Management
- 📊 Reports & Statistics

---

# 🏗️ System Workflow

```text
                    ┌──────────────────┐
                    │     SmartBlood   │
                    │      System      │
                    └────────┬─────────┘
                             │
             ┌───────────────┼───────────────┐
             │               │               │
             ▼               ▼               ▼
        ┌─────────┐     ┌─────────┐     ┌────────────┐
        │  Donor  │     │ Hospital│     │ Blood Bank │
        └────┬────┘     └────┬────┘     └─────┬──────┘
             │               │                 │
             │               │                 │
             ▼               ▼                 ▼
       Donation          Blood Request     Inventory
       Appointment      Emergency Request  Management
             │               │                 │
             └───────────────┼─────────────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │     Firebase     │
                    │ Authentication   │
                    │  + Firestore     │
                    └──────────────────┘

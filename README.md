# CityFix — CN6008 Coursework

An Android application that lets citizens report urban faults (potholes, broken streetlights, flooding, vandalism) and track their resolution in real time. Admins manage and update report statuses through a dedicated dashboard.

---

## Features

### Citizens
- **Register / Login** via email & password (Firebase Auth)
- **Interactive Map** — browse all reports as markers; pan the map to target a precise location before submitting
- **Submit a Report** — choose category, write title & description; location is taken from the map centre crosshair
- **Feed** — scrollable list of all reports with search and category filter chips
- **Upvote** reports to signal urgency
- **Comments** — real-time comment thread on each report
- **Status History** — full timeline of every status change on a report
- **My Reports** — profile tab shows only your own submissions
- **View on Map** — tap Map on any feed card to fly the map camera to that report's location
- **Settings Panel** — sign out, access admin dashboard (if admin)

### Admins
- **Admin Dashboard** — full list of all reports
- **Status Management** — change report status (Open → In Progress → Resolved)
- **Stats Bar** — live counts of Open / In Progress / Resolved reports
- **Status History** — every change is logged with admin name and timestamp

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java (Android SDK) |
| UI | Material Design 3, BottomSheetDialogFragment |
| Auth | Firebase Authentication (Email/Password) |
| Database | Cloud Firestore (real-time listeners) |
| Maps | Google Maps SDK for Android |
| Architecture | MVVM + LiveData + Repository pattern |

---

## Project Structure

```
app/src/main/java/com/cityfix/
├── activities/
│   ├── AuthActivity.java           — Login & Register screens
│   ├── MainActivity.java           — Bottom nav host, location permission
│   ├── ReportDetailActivity.java   — Upvotes, comments, status history
│   └── AdminDashboardActivity.java — Admin report management + stats
├── fragments/
│   ├── MapFragment.java            — Google Map with report markers + crosshair
│   ├── FeedFragment.java           — Report list, search, filter chips
│   ├── ProfileFragment.java        — User info, my reports, stats
│   ├── SubmitReportFragment.java   — Bottom sheet report form
│   └── SettingsFragment.java       — Sign out, admin access
├── adapters/
│   ├── ReportAdapter.java          — Feed & admin list cards
│   └── CommentAdapter.java         — Comment thread
├── models/
│   ├── FaultReport.java
│   ├── User.java
│   ├── Comment.java
│   └── StatusUpdate.java
├── repositories/
│   ├── ReportRepository.java       — All Firestore report operations
│   └── UserRepository.java         — User profile operations
└── utils/
    └── Constants.java
```

---

## Setup

### 1. Firebase Project
1. Create a project at [console.firebase.google.com](https://console.firebase.google.com)
2. Enable **Authentication** → Email/Password provider
3. Enable **Firestore Database** (production or test mode)
4. Download `google-services.json` → place in `app/`

### 2. Google Maps API Key
1. Go to [Google Cloud Console](https://console.cloud.google.com) → your Firebase project
2. Enable **Maps SDK for Android**
3. Create an API key (unrestricted, or restrict to Maps SDK for Android)
4. Ensure **billing is enabled** on the project (Maps requires it even on free tier)
5. Paste the key into `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="google_maps_key">YOUR_KEY_HERE</string>
   ```

### 3. Emulator Requirements
Google Maps does **not** work on plain AOSP emulators. Use a system image tagged **Google APIs** or **Google Play Store** (Device Manager → create device → select Google Play system image).

### 4. Admin Access
To grant admin rights to a user:
1. Open Firestore → `users` collection → find the user document
2. Set the `role` field to `admin`
3. The user will see **Admin Dashboard** in their Settings panel

### 5. Open in Android Studio
Open the project root in Android Studio — Gradle will sync automatically.

---

## Firestore Data Model

```
users/{uid}
  name, email, role ("citizen" | "admin"), reportsCount

reports/{reportId}
  title, description, category, status, latitude, longitude
  address, userId, userName, upvotes, timestamp

  statusHistory/{updateId}
    status, changedBy, timestamp, note

  comments/{commentId}
    text, userName, userId, timestamp
```

---

## Sprint Progress

- [x] Sprint 1 — Firebase Auth, project scaffold, bottom navigation
- [x] Sprint 2 — Google Maps integration, submit report flow, Firestore persistence
- [x] Sprint 3 — Feed, search & filter, report detail, admin dashboard, status management
- [x] Sprint 4 — Upvotes, comments, status history, my reports, view on map, settings panel, UI polish

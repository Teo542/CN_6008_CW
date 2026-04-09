# CityFix — CN6008 Coursework

An Android application that lets citizens report urban faults (potholes, broken streetlights, flooding, vandalism) and track their resolution in real time. Admins manage and update report statuses through a dedicated web admin portal with live analytics.

---

## Features

### Citizens (Android App)
- **Register / Login** via email & password (Firebase Auth)
- **Interactive Map** — colour-coded markers (red/orange/green by status); pan the map to target a precise location before submitting
- **Submit a Report** — choose category, write title & description; location taken from map centre crosshair; attach a photo with front or back camera
- **Feed** — scrollable list of all reports with live search and category filter chips
- **Upvote** reports to signal urgency — one vote per user, enforced server-side
- **Comments** — real-time comment thread on each report
- **Status History** — full timeline of every status change
- **My Reports** — profile tab shows only your own submissions
- **View on Map** — tap Map on any feed card to fly the camera to that report's location
- **Settings Panel** — change display name, avatar colour, sign out

### Admins (Web Portal)
- **Secure Login** — only accounts with `role: "admin"` in Firestore can access
- **Overview Dashboard** — 6 KPI cards, 7-day trend bar chart, status doughnut chart, category breakdown bars, top upvoted reports, most active citizens, today's activity summary
- **Reports Table** — search, filter by status and category, inline status change, upvote count
- **Report Detail** — full info, one-click status buttons, post admin comments, status history timeline, open location in Google Maps
- **Real-time sync** — every change on the portal instantly reflects in the Android app

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java (Android SDK 35) |
| UI | Material Design 3, BottomSheetDialogFragment |
| Auth | Firebase Authentication (Email/Password) |
| Database | Cloud Firestore (real-time listeners) |
| Maps | Google Maps SDK for Android |
| Architecture | MVVM + LiveData + Repository pattern |
| Admin Portal | Vanilla HTML/CSS/JS + Firebase Web SDK v10 + Chart.js |

---

## Project Structure

```
app/src/main/java/com/cityfix/
├── activities/
│   ├── AuthActivity.java           — Login & Register screens
│   ├── MainActivity.java           — Bottom nav host, location permission
│   └── ReportDetailActivity.java   — Upvotes, comments, status history, photo
├── fragments/
│   ├── MapFragment.java            — Google Map with markers + centre crosshair
│   ├── FeedFragment.java           — Report list, search, filter chips
│   ├── ProfileFragment.java        — User info, my reports, report count
│   ├── SubmitReportFragment.java   — Bottom sheet form + camera + location
│   └── SettingsFragment.java       — Display name, avatar colour, sign out
├── adapters/
│   ├── ReportAdapter.java          — Feed list cards with View on Map
│   └── CommentAdapter.java         — Comment thread
├── models/
│   ├── FaultReport.java
│   ├── User.java
│   ├── Comment.java
│   └── StatusUpdate.java
├── repositories/
│   ├── ReportRepository.java       — All Firestore report operations + listener cleanup
│   └── UserRepository.java         — User profile operations
├── viewmodels/
│   └── MapViewModel.java           — Shared report LiveData for Map + Feed
└── utils/
    ├── Constants.java
    └── LocationHelper.java

admin/                              — Web admin portal
├── index.html                      — Login (admin role enforced)
├── dashboard.html                  — Analytics overview + reports table
├── report.html                     — Report detail + status + comments
├── firebase.js                     — Firebase initialisation
└── style.css                       — Dark theme styles

firestore.rules                     — Server-side security rules
```

---

## Setup

### 1. Firebase Project
1. Create a project at [console.firebase.google.com](https://console.firebase.google.com)
2. Enable **Authentication** → Email/Password provider
3. Enable **Firestore Database**
4. Download `google-services.json` → place in `app/`
5. Deploy security rules: **Firestore → Rules** → paste contents of `firestore.rules` → Publish

### 2. Google Maps API Key
1. Go to [Google Cloud Console](https://console.cloud.google.com) → your Firebase project
2. Enable **Maps SDK for Android**
3. Create an API key and restrict it to **Maps SDK for Android**
4. Add an Android app restriction for package `com.cityfix` and the app signing SHA-1
5. Ensure **billing is enabled** (Maps requires it even on free tier)
6. Paste the key into `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="maps_api_key">YOUR_KEY_HERE</string>
   ```

### 3. Emulator Requirements
Google Maps does **not** work on plain AOSP emulators. Use a system image tagged **Google Play Store** (Device Manager → create device → select Google Play system image).

### 4. Admin Access
1. Open Firestore → `users` collection → find the user document
2. Set the `role` field to `"admin"`
3. The user can now log into the **Web Admin Portal**

### 5. Web Admin Portal
Run a local server from the `admin/` folder:
```bash
cd admin
python -m http.server 8080
```
Open `http://localhost:8080` — log in with an account that has `role: "admin"` in Firestore.

### 6. Open in Android Studio
Open the project root in Android Studio — Gradle will sync automatically.

---

## Firestore Data Model

```
users/{uid}
  displayName, email, role ("citizen" | "admin"),
  reportsSubmitted, avatarColor, joinedAt

reports/{reportId}
  title, description, category, status, latitude, longitude
  address, userId, userName, upvotes, upvoterIds[], imageUrl, timestamp

  comments/{commentId}
    text, userName, userId, timestamp

  statusHistory/{updateId}
    fromStatus, toStatus, changedBy, timestamp
```

---

## Security
- Firestore rules enforce server-side role checks — citizens cannot change report status or write status history
- Upvote deduplication enforced via Firestore transaction and `upvoterIds` array
- Admin portal verifies `role: "admin"` before granting access
- `allowBackup=false` prevents auth token extraction via ADB
- Input validation on title (100 chars), description (500 chars), and comments (500 chars)

---

## Future Improvements

The following features were identified as meaningful extensions beyond the current prototype scope. They are documented here as a roadmap for a production-ready version of CityFix.

### Push Notifications (Firebase Cloud Messaging)
Citizens would receive a push notification when the status of their submitted report changes — e.g. *"Your pothole report on Ermou Street has been resolved."* This closes the feedback loop between citizen and municipality and is a standard feature in civic engagement platforms. Implementation would require an FCM server key, a Cloud Function trigger on Firestore status changes, and storing per-user FCM tokens.

### Map Marker Clustering
When zoomed out, nearby report markers would group into numbered cluster bubbles (e.g. a circle showing "14" for 14 nearby reports). This prevents visual clutter on dense city maps and is standard in professional mapping applications. Android's `maps-utils` library provides this out of the box.

### Heatmap Overlay
A toggleable heatmap layer on the map would visualise problem density across the city — darker/redder areas indicate more unresolved reports. This gives city managers an at-a-glance spatial overview without reading individual markers and is a common feature in urban management dashboards.

### Offline Support
Firestore's built-in offline persistence (one line of configuration) would allow citizens to browse reports and submit new ones without an internet connection. Changes would queue locally and sync automatically when connectivity is restored — critical for field use in areas with poor signal.

### Export to CSV / PDF
Admin users could download all reports (or a filtered subset) as a spreadsheet or PDF report. This is essential for formal reporting to city councils, insurance claims, and budget planning. A simple JavaScript CSV export is trivial to add; a PDF summary would require a library such as `jsPDF`.

### Average Resolution Time Analytics
The admin dashboard would display the average time between a report being opened and marked resolved, broken down by category (e.g. *"Potholes resolved in avg 4.2 days"*). This is a key performance indicator for public works departments and would significantly enhance the analytics section.

### Citizen Reputation / Gamification
A points system rewarding active citizens (reports submitted, upvotes received, comments) with badges or a leaderboard. This increases engagement and report quality, as seen in platforms like FixMyStreet and SeeClickFix.

### OAuth / Social Login
Adding Google or Apple sign-in alongside email/password would lower the registration barrier and increase adoption. Firebase Auth supports both providers with minimal additional code.

### Duplicate Report Detection
When submitting a report, the app could query Firestore for open reports within a 50-metre radius of the same category and warn the user: *"A similar report already exists nearby."* This reduces duplicate entries and improves data quality for administrators.

### Cloud Functions for Server-Side Logic
Moving critical operations (status change notifications, counter reconciliation, duplicate detection) to Firebase Cloud Functions would eliminate the remaining client-side trust assumptions and make the application fully production-safe.

---

## Sprint Progress

- [x] Sprint 1 — Firebase Auth, project scaffold, bottom navigation
- [x] Sprint 2 — Google Maps integration, submit report flow, Firestore persistence
- [x] Sprint 3 — Feed, search & filter, report detail, status management
- [x] Sprint 4 — Upvotes, comments, status history, my reports, view on map, settings, UI polish
- [x] Sprint 5 — Web admin portal, camera (front/back), real-time map sync, profile updates
- [x] Sprint 6 — Analytics dashboard, Firestore security rules, security audit fixes

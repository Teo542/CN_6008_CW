# CityFix - CN6008 Coursework

An Android application that lets citizens report urban faults (potholes, broken streetlights, flooding, vandalism) and track their resolution in real time. Admins manage and update report statuses through a dedicated web admin portal with live analytics.

## README Scope

This README is the technical guide for running and reviewing the repository. It covers features, setup, project structure, build verification, and manual checks. The Word report complements it by focusing on design rationale, methodology, critical analysis, and evaluation rather than repeating the full setup guide.

---

## Features

### Citizens (Android App)
- **Register / Login** via email & password (Firebase Auth)
- **Interactive Map** - colour-coded markers (red/orange/green by status); pan the map to target a precise location before submitting
- **Submit a Report** - choose category, write title & description; location taken from map centre crosshair; attach a photo with front or back camera
- **Feed** - scrollable list of all reports with live search and category filter chips
- **Upvote** reports to signal urgency - one vote per user, enforced server-side
- **Comments** - real-time comment thread on each report
- **Status History** - full timeline of every status change
- **My Reports** - profile tab shows only your own submissions
- **View on Map** - tap Map on any feed card to fly the camera to that report's location
- **Settings Panel** - change display name, avatar colour, sign out

### Admins (Web Portal)
- **Secure Login** - only accounts with `role: "admin"` in Firestore can access
- **Overview Dashboard** - 6 KPI cards, 7-day trend bar chart, status doughnut chart, category breakdown bars, top upvoted reports, most active citizens, today's activity summary
- **Reports Table** - search, filter by status and category, inline status change, upvote count
- **Report Detail** - full info, one-click status buttons, post admin comments, status history timeline, open location in Google Maps
- **Real-time sync** - every change on the portal instantly reflects in the Android app

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
|-- activities/
|   |-- AuthActivity.java           - Login and register screens
|   |-- MainActivity.java           - Bottom nav host, location permission
|   `-- ReportDetailActivity.java   - Upvotes, comments, status history, photo
|-- fragments/
|   |-- MapFragment.java            - Google Map with markers and centre crosshair
|   |-- FeedFragment.java           - Report list, search, filter chips
|   |-- ProfileFragment.java        - User info, my reports, report count
|   |-- SubmitReportFragment.java   - Bottom sheet form, camera, location
|   `-- SettingsFragment.java       - Display name, avatar colour, sign out
|-- adapters/
|   |-- ReportAdapter.java          - Feed list cards with View on Map
|   `-- CommentAdapter.java         - Comment thread
|-- models/
|   |-- FaultReport.java
|   |-- User.java
|   |-- Comment.java
|   `-- StatusUpdate.java
|-- repositories/
|   |-- ReportRepository.java       - Firestore report operations and listener cleanup
|   `-- UserRepository.java         - User profile operations
|-- viewmodels/
|   `-- MapViewModel.java           - Shared report LiveData for Map and Feed
`-- utils/
    |-- Constants.java
    `-- LocationHelper.java

admin/                              - Web admin portal
|-- index.html                      - Login with admin role enforced
|-- dashboard.html                  - Analytics overview and reports table
|-- report.html                     - Report detail, status, comments
|-- firebase.js                     - Firebase initialisation
`-- style.css                       - Dark theme styles

firestore.rules                     - Server-side security rules
```

---

## Setup

### 1. Firebase Project
1. Create a project at [console.firebase.google.com](https://console.firebase.google.com)
2. Enable **Authentication** -> Email/Password provider
3. Enable **Firestore Database**
4. Download `google-services.json` -> place in `app/`
5. Deploy security rules: **Firestore -> Rules** -> paste contents of `firestore.rules` -> Publish

#### What `google-services.json` Does

`google-services.json` is the Android Firebase client configuration file for this exact app. It tells the app which Firebase project to connect to and includes values such as:

- Firebase project identifiers
- Android app identifier/package mapping
- API configuration used by the Firebase Android SDK

Without this file, the Android app will not connect to the intended Firebase project, so authentication, Firestore reads/writes, and other Firebase-backed features will fail.

Important clarification:

- This file is required for the Android app to run against the configured Firebase backend.
- It is not the same thing as a privileged Firebase Admin SDK service-account key.
- It should still be handled carefully and only shared where needed for marking/demo/build verification.

### 2. Google Maps API Key
1. Go to [Google Cloud Console](https://console.cloud.google.com) -> your Firebase project
2. Enable **Maps SDK for Android**
3. Create an API key and restrict it to **Maps SDK for Android**
4. Add an Android app restriction for package `com.cityfix` and the app signing SHA-1
5. Ensure **billing is enabled** (Maps requires it even on free tier)
6. Paste the key into `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="maps_api_key">YOUR_KEY_HERE</string>
   ```

### 3. Emulator Requirements
Google Maps does **not** work on plain AOSP emulators. Use a system image tagged **Google Play Store** (Device Manager -> create device -> select Google Play system image).

### 4. Admin Access
1. Open Firestore -> `users` collection -> find the user document
2. Set the `role` field to `"admin"`
3. The user can now log into the **Web Admin Portal**

### 5. Web Admin Portal
Run a local server from the `admin/` folder:
```bash
cd admin
python -m http.server 8080
```
Open `http://localhost:8080` - log in with an account that has `role: "admin"` in Firestore.

### 6. Open in Android Studio
Open the project root in Android Studio - Gradle will sync automatically.

### 7. Submission / Marker Setup Note

For coursework submission, the marker needs enough configuration to build and run the Android app as reviewed in this repository.

Recommended approach:

- Include `app/google-services.json` in the Moodle/source-code zip if your course rules allow it.
- Keep `google-services.json` out of public repositories unless you intentionally want reviewers to use the same Firebase project.
- Do **not** include privileged backend credentials such as Firebase Admin SDK service-account JSON files, `.env` secrets, or Google Cloud private keys.

If `google-services.json` is not included in the final submission package, the marker will need to supply their own Firebase project configuration before the Android app can run successfully.

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
- Firestore rules enforce server-side role checks - citizens cannot change report status or write status history
- Upvote deduplication enforced via Firestore transaction and `upvoterIds` array
- Admin portal verifies `role: "admin"` before granting access
- `allowBackup=false` prevents auth token extraction via ADB
- Input validation on title (100 chars), description (500 chars), and comments (500 chars)

---

## Testing / Verification

This prototype is verified through a combination of automated unit tests, Android build checks, admin portal smoke testing, and manual emulator testing. The automated tests focus on deterministic application logic that can be checked without Firebase, Google Maps, or an emulator.

### Unit Tests
Run the local JVM tests from the project root:
```bash
./gradlew :app:testDebugUnitTest
```
On Windows:
```bash
.\gradlew.bat :app:testDebugUnitTest
```

Current unit-test coverage:
- `ValidationUtilsTest` checks report title, description, comment, and password boundaries.
- `StatusFormatterTest` checks safe display text for `open`, `in_progress`, `resolved`, null, and unknown statuses.

These tests were added because validation and status formatting are reused by multiple screens. Moving that logic into `ValidationUtils` and `StatusFormatter` prevents each Activity or Fragment from silently applying different rules. The tests specifically protect boundary cases such as 100-character titles, 501-character descriptions/comments, 5-character passwords, and unknown status values.

### Android Build
Run the debug build from the project root:
```bash
./gradlew :app:assembleDebug
```
On Windows:
```bash
.\gradlew.bat :app:assembleDebug
```

### Admin Portal Smoke Test
Run a local static server from the `admin/` folder:
```bash
cd admin
python -m http.server 8080
```
Then open `http://localhost:8080` and verify:
- The admin login page loads
- Invalid credentials show an error message
- Password reset requires an email address
- Direct access to `dashboard.html` redirects unauthenticated users back to login
- Authenticated admin users can view dashboard metrics, filter reports, and update report status

### Manual App Verification
The Android app should be checked on an emulator or device with Google Play services:
- Register and log in with Firebase Authentication
- Submit a report with category, title, description, map location, and optional photo
- Confirm the report appears on the map and feed in real time
- Open report details, add a comment, and upvote once per user
- Confirm profile and "My Reports" screens reflect the user's submitted reports

---

## Future Improvements

The following features were identified as meaningful extensions beyond the current prototype scope. They are documented here as a roadmap for a production-ready version of CityFix.

### Push Notifications (Firebase Cloud Messaging)
Citizens would receive a push notification when the status of their submitted report changes - e.g. *"Your pothole report on Ermou Street has been resolved."* This closes the feedback loop between citizen and municipality and is a standard feature in civic engagement platforms. Implementation would require an FCM server key, a Cloud Function trigger on Firestore status changes, and storing per-user FCM tokens.

### Map Marker Clustering
When zoomed out, nearby report markers would group into numbered cluster bubbles (e.g. a circle showing "14" for 14 nearby reports). This prevents visual clutter on dense city maps and is standard in professional mapping applications. Android's `maps-utils` library provides this out of the box.

### Heatmap Overlay
A toggleable heatmap layer on the map would visualise problem density across the city - darker/redder areas indicate more unresolved reports. This gives city managers an at-a-glance spatial overview without reading individual markers and is a common feature in urban management dashboards.

### Offline Support
Firestore's built-in offline persistence (one line of configuration) would allow citizens to browse reports and submit new ones without an internet connection. Changes would queue locally and sync automatically when connectivity is restored - critical for field use in areas with poor signal.

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

- [x] Sprint 1 - Firebase Auth, project scaffold, bottom navigation
- [x] Sprint 2 - Google Maps integration, submit report flow, Firestore persistence
- [x] Sprint 3 - Feed, search & filter, report detail, status management
- [x] Sprint 4 - Upvotes, comments, status history, my reports, view on map, settings, UI polish
- [x] Sprint 5 - Web admin portal, camera (front/back), real-time map sync, profile updates
- [x] Sprint 6 - Analytics dashboard, Firestore security rules, security audit fixes

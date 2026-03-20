# CityFix — CN6008 Solo Coursework

A Smart City Android application where citizens report urban faults (potholes, broken streetlights, flooding) and track their resolution in real time.

## Tech Stack
- **Android Studio** (Java)
- **Firebase Auth** — user authentication
- **Cloud Firestore** — real-time database
- **Firebase Storage** — photo uploads
- **Google Maps SDK** — geolocation & map display
- **Material Design 3** — UI components

## Setup

### 1. Firebase Project
1. Create a project at console.firebase.google.com
2. Enable **Authentication** (Email/Password)
3. Enable **Firestore Database**
4. Enable **Storage**
5. Download `google-services.json` → place in `app/`

### 2. Google Maps API Key
1. Go to Google Cloud Console
2. Enable **Maps SDK for Android**
3. Create an API key
4. Replace `YOUR_MAPS_API_KEY_HERE` in `app/src/main/res/values/strings.xml`

### 3. Open in Android Studio
Open this folder in Android Studio — it will sync Gradle automatically.

## Project Structure
```
app/src/main/java/com/cityfix/
├── activities/     AuthActivity, MainActivity, ReportDetailActivity, AdminDashboardActivity
├── fragments/      MapFragment, FeedFragment, ProfileFragment
├── models/         FaultReport, User, Comment, StatusUpdate
├── repositories/   ReportRepository, UserRepository
├── services/       FCMService
└── utils/          Constants
```

## Sprint Progress
- [x] Sprint 1 — Foundation & Auth
- [ ] Sprint 2 — Core Reporting Flow (GPS, photo, map markers)
- [ ] Sprint 3 — Feed, Detail, Admin dashboard
- [ ] Sprint 4 — Polish, UML, Academic report

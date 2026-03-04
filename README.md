A minimal, privacy-focused Android application to manually save your favorite places (cafes, viewpoints, hidden spots, etc.) with custom titles, personal notes, photos, and exact GPS coordinates.

**No background tracking, no cloud sync, no accounts.** Everything is 100% intentional, on-demand, and stored locally on your device.

---

## 🌟 Core Features

### 1. 📍 Save Current Spot (Manual & On-Demand)
- Tap the **"Save This Spot"** button on the home screen.
- Auto-fetches your device's current GPS coordinates ONE time upon opening.
- Displays a smooth loading indicator while GPS locks.
- **Form details**:
  - **Title / Name**: Required text input (e.g., *"Blue Door Cafe"*).
  - **Description / Note**: Multiline text input for personal thoughts (e.g., *"Best iced latte, quiet corner seat"*).
  - **Category / Tag**: Dropdown selection (e.g., *Food, Nature, Photo Spot, Landmark, Nightlife, Other*).
  - **Photo**: Attach from phone gallery OR capture directly with camera (stored locally).
  - **Date & Time**: Auto-filled timestamp.
  - **Coordinates**: Auto-filled from GPS and fully editable in case you want to tweak latitude/longitude manually.
- Confirmation snackbar after saving.

### 2. 📋 My Places List
- Scrollable list of all your saved places.
- Cards show: Thumbnail photo, Title, Date, Note preview, and Latitude/Longitude coordinates.
- **Swipe actions**: Swipe left to delete, swipe right to edit.
- Search bar to filter saved places by **name or description text**.

### 3. 🔍 Detail View
- Displays full-resolution photo, title, date, category tag, description, address, and coordinates.
- **"Open in Google Maps"**: Launches the official Google Maps app (or web browser fallback) with exact coordinates for navigation (`geo:` / web intent).
- **"Share"**: Opens the Android system share sheet with a text snippet: *"Check out [Title] at [lat], [long]"*.
- **Edit & Delete**: Full edit capabilities and permanent delete with confirmation modal.
- **In-App Map View**: Single-spot map preview.

### 4. 🗺️ Map Tab View
- Home screen features two main tabs: **List** and **Map**.
- The **Map** tab displays pins for ALL your saved spots across the world simultaneously.
- Tapping a pin reveals place details and allows quick navigation to the Detail View.

### 5. 🌙 Dark Mode Support
- Complete dark theme support using standard `MaterialComponents.DayNight`.

---

## 🛠️ Tech Stack & Open-Source Libraries

- **Language**: [Kotlin](https://kotlinlang.org/)
- **Minimum SDK**: API Level 21 (Android 5.0 Lollipop+)
- **Architecture**: MVVM (Model-View-ViewModel) with Android Jetpack
  - **Room Database**: Local SQLite abstraction for place data & schema migration.
  - **ViewModel & LiveData**: Lifecycle-aware data handling.
  - **View Binding & Data Binding**: Clean UI component binding.
  - **Navigation Component**: Single-activity architecture with Navigation graph.
  - **ViewPager2 & TabLayout**: Tabbed layout switching (List & Map views).
- **Google Play Services Location & Maps**: `play-services-location` for single-shot location fetch and `play-services-maps` for interactive maps.
- **Dexter**: Runtime permissions manager (Location, Camera, Storage).
- **CircleImageView**: Rounded thumbnail image views.

---

## 🚀 How to Build & Run

### Prerequisites
1. Android Studio 4.0+ (or IntelliJ IDEA with Android plugin).
2. Android SDK 21 or higher.

### 🔑 Google Maps API Key Setup
1. Obtain a **Maps SDK for Android** API Key from the [Google Cloud Console](https://console.cloud.google.com/).
2. Open `app/src/main/res/values/strings.xml`.
3. Replace `YOUR_GOOGLE_MAPS_API_KEY` in the `google_maps_key` string resource:

```xml
<string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">YOUR_ACTUAL_API_KEY_HERE</string>
```

### 🔨 Building via Command Line
```bash
# Clone or navigate to directory
cd my-places-android-master

# Build debug APK
./gradlew assembleDebug

# Install on connected Android device/emulator
./gradlew installDebug
```

---

## 🔒 Privacy & Permissions

This application respects user privacy by design:
- **Location**: Requested only "When In Use" upon tapping "Save This Spot" or "Add Current Location". No background location permission or background tracking is ever requested.
- **Camera / Storage**: Requested on-demand only when attaching photos.
- **Data Storage**: Photos are saved to app-private internal storage (`Context.MODE_PRIVATE`). Database is local Room SQLite. No network requests are made except loading Google Maps tiles.

---

## 📄 License
Copyright 2026. Licensed under the Apache License, Version 2.0.

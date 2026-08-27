# Hashmi Motors — Spare Parts Manager

**v1.2.1** — gold/ink atelier UI, compile fix for the scan tab, live camera / QR, billing with customer on the bill, backup. No plans or paywalls.

A personal-use Android app for managing a spare parts shop. Built for shop owners and their staff (in this case: you and your father) to run the counter, manage inventory, and create GST-compliant bills.

## ✨ Features

- 📦 **Catalog Management** — Add 100s-1000s of parts, search by name/OEM/brand
- 🚗 **Vehicle Compatibility Search** — "What fits my car?" 3-step wizard
- 💰 **Billing & Invoicing** — GST-compliant Bill of Supply, PDF/WhatsApp/SMS share
- 📊 **Inventory Tracking** — Add stock, low-stock alerts, movement history
- 👥 **Multi-device Sync** — Two Google accounts share the same data
- 🎨 **Polished UI** — Animated backgrounds, bounce buttons, confetti, sound effects
- 📥 **7 SKU Import Methods** — Manual, CSV, Google Sheets, OCR, AI, barcode, voice
- 🌐 **Web Enrichment** — On-demand part suggestions from public catalogs (optional)

## 🏗️ Tech Stack

- **Kotlin** + **Jetpack Compose** — Modern Android UI
- **Room** — Local encrypted database (works offline)
- **Hilt** — Dependency injection
- **Firebase** (optional) — Multi-device sync
- **ML Kit** — OCR & barcode scanning
- **Material 3** — Design system

## 📊 Current Build Status

| Layer | Status | Files | Lines |
|---|---|---|---|
| Project setup (Gradle, manifest) | ✅ Complete | 8 | ~250 |
| Theme & resources | ✅ Complete | 6 | ~400 |
| Domain models | ✅ Complete | 1 | ~280 |
| Data layer (Room) | ✅ Complete | 5 | ~700 |
| Repositories | ✅ Complete | 7 | ~500 |
| ViewModels | 🟡 Partial | 6 | ~400 |
| UI Screens | 🟡 Partial | 7 | ~800 |
| Navigation | 🟡 Basic | 1 | ~150 |
| GitHub Actions CI | ✅ Complete | 1 | ~80 |

**What's working in v1.1:**
- Live CameraX preview for QR / barcode (ML Kit)
- Part photo capture and OCR label scan
- Search, voice search, CSV import, sample catalog
- Billing with scan-to-add, customer name on bills, invoice QR
- Fitment wizard that lists and links compatible parts
- Inventory, stock in, reports, customers, PDF / WhatsApp share

## 🚀 Quick Start

### Option 1: Download pre-built APK (when available)

Once the GitHub Actions workflow runs successfully:
1. Go to your repo → Actions tab
2. Click the latest green build
3. Scroll to "Artifacts" → Download `hashmi-motors-debug-apk`
4. Transfer to your phone
5. Install (you may need to enable "Install from unknown sources")

### Option 2: Build locally in Android Studio

1. Install [Android Studio](https://developer.android.com/studio) (Hedgehog or later)
2. Open this project
3. Let Gradle sync (may take 5-10 minutes first time)
4. Connect your phone with USB debugging enabled
5. Click Run

## 🔧 Setup Firebase (Optional - for sync)

If you want multi-device sync between your phone and your father's phone:

1. Go to https://console.firebase.google.com
2. Create a new project: "Hashmi Motors"
3. Add an Android app with package name: `com.hashmimotors.app`
4. Download `google-services.json` to `app/google-services.json`
5. Enable Authentication → Google Sign-In
6. Create Firestore database (start in test mode)
7. Edit `app/build.gradle.kts`:
   - Uncomment Firebase dependencies
   - Uncomment `id("com.google.gms.google-services")` plugin
8. Add to `gradle.properties`:
   ```
   firebase.enabled=true
   ```

## 📋 Requirements

- **Android 8.0+** (API 26) — covers ~98% of devices
- **2GB RAM minimum** for smooth animations
- **100MB storage** for app + database
- **Internet** only for sync (not required for daily use)

## 📂 Project Structure

```
Hashmi-moters-/
├── .github/workflows/         # GitHub Actions CI/CD
├── app/
│   ├── build.gradle.kts       # App dependencies
│   ├── src/main/
│   │   ├── java/com/hashmimotors/app/
│   │   │   ├── data/
│   │   │   │   ├── local/     # Room DB
│   │   │   │   └── repository/# Business logic
│   │   │   ├── domain/model/  # Data classes
│   │   │   ├── di/            # Hilt modules
│   │   │   └── ui/            # Compose UI
│   │   │       ├── theme/
│   │   │       ├── components/
│   │   │       ├── dashboard/
│   │   │       ├── catalog/
│   │   │       ├── billing/
│   │   │       ├── inventory/
│   │   │       ├── shop/
│   │   │       ├── settings/
│   │   │       ├── splash/
│   │   │       └── onboarding/
│   │   ├── res/               # Resources
│   │   └── AndroidManifest.xml
│   └── google-services.json   # (you add this)
├── docs/
│   └── APP_PLAN.md            # Full app specification
├── gradle/wrapper/
├── gradlew
├── build.gradle.kts
└── settings.gradle.kts
```

## 🐛 Known Issues / Roadmap

This is an active build. The first build may have errors that we'll fix together.

## 📄 License

Personal use only. Not for distribution.

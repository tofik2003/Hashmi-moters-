# Hashmi Motors — Spare Parts Manager

A personal-use Android app for managing a spare parts shop. Built for shop owners and their staff (in this case: you and your father) to run the counter, manage inventory, and create GST-compliant bills.

## ✨ What actually works today

- 📦 **Catalog** — add parts, search by name / SKU / OEM / brand, filter by category
- 💰 **Billing** — cart with per-line and bill-level discounts, GST-composition Bill of Supply
- 🧾 **Invoice sharing** — PDF generation, WhatsApp deep link, SMS share sheet
- 📊 **Inventory** — stock in, manual adjustments, low-stock list, movement history
- 📈 **Reports** — today, this month, and inventory valuation
- 🚗 **Vehicle fitment** — 3-step make → model → variant wizard, 50+ Indian models pre-seeded
- 👥 **Customers** — contact list with running purchase totals
- 🎨 **Polish** — animated gradient/particle background, bounce buttons, splash, sound + haptics on bill save
- ⚙️ **Settings** — theme mode, accent colour, background style, sounds, animations

## 🚧 Not implemented yet (do not expect these)

The plan in [`docs/APP_PLAN.md`](docs/APP_PLAN.md) describes a larger app. These parts are **not built**:

- Multi-device sync / Firebase (dependencies are present but commented out; no sync code exists)
- Photo capture, barcode scanning, OCR shelf photos, voice input, CSV/Sheets import
- PIN lock and biometric unlock
- Web enrichment (part suggestions from public catalogs)
- Regular-scheme GST tax invoices — **only the composition scheme (Bill of Supply) is supported**

## 🏗️ Tech Stack

| Layer | Tech |
|---|---|
| Language / UI | Kotlin 1.9.22 + Jetpack Compose (BOM 2024.02.00) + Material 3 |
| Local storage | Room 2.6.1 (SQLite), KSP |
| DI | Hilt 2.50 |
| PDF | `android.graphics.pdf.PdfDocument` (framework, no extra dependency) |
| Build | Gradle 8.5, AGP 8.2.2, JDK 17, `compileSdk` 34 / `minSdk` 26 |

> **The database is not encrypted.** An earlier revision of this README claimed
> "Room — Local encrypted database". There is no SQLCipher dependency and no
> encryption is configured; `hashmi_motors.db` is a plain SQLite file in app-private
> storage. Treat the device itself as the security boundary.

## 🚀 Build & test

### CI (recommended)

Every push builds and tests automatically — see [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml):

1. `./gradlew testDebugUnitTest` — unit tests (billing math, invoice numbering, Room converters)
2. `./gradlew assembleDebug` — produces the APK

Then: repo → **Actions** → latest green run → **Artifacts** → `HashmiMotors-debug`.
Transfer to the phone and install (enable "Install from unknown sources").

### Local build

```bash
cp local.properties.example local.properties   # then set sdk.dir to your Android SDK
./gradlew testDebugUnitTest                    # run the tests
./gradlew assembleDebug                        # -> app/build/outputs/apk/debug/app-debug.apk
```

Or open the project in Android Studio (Hedgehog or later) and press Run.

## 🧪 Tests

Unit tests live in `app/src/test/` and cover the code where a mistake costs money
or corrupts a bill:

| Test | Guards against |
|---|---|
| `BillingUiStateTest` | Line discounts being subtracted twice from the grand total |
| `InvoiceMathTest` | Discount clamping, negative totals |
| `FinancialYearTest` | Invoice numbers using the calendar year instead of the 1 Apr – 31 Mar financial year |
| `ConvertersTest` | Room TypeConverters losing or corrupting stored invoice lines |

## 🔧 Setup Firebase (optional — sync is not implemented)

Adding `google-services.json` alone does **not** give you sync; no synchronisation
code has been written. If you later build it:

1. Create a project at https://console.firebase.google.com named "Hashmi Motors"
2. Add an Android app with package name `com.hashmimotors.app`
3. Put `google-services.json` in `app/`
4. Uncomment the Firebase dependencies in `app/build.gradle.kts`
5. Add `firebase.enabled=true` to `gradle.properties`

## 📋 Requirements

- **Android 8.0+** (API 26)
- **JDK 17** to build
- Internet only for the optional sync (not needed for daily use)

## 📂 Project Structure

```
Hashmi-moters-/
├── .github/workflows/build-apk.yml   # CI: unit tests + debug APK
├── app/src/
│   ├── main/java/com/hashmimotors/app/
│   │   ├── data/local/               # Room entities, DAOs, converters, database
│   │   ├── data/repository/          # Repositories (domain <-> Room mapping)
│   │   ├── di/                       # Hilt modules
│   │   ├── domain/model/             # Pure domain models
│   │   ├── domain/money/             # Pure invoice math + financial year (unit tested)
│   │   └── ui/                       # Compose screens, ViewModels, components
│   ├── main/res/                     # Resources
│   ├── main/AndroidManifest.xml
│   └── test/java/                    # JVM unit tests
├── docs/
│   ├── APP_PLAN.md                   # Full product specification (aspirational)
│   └── AUDIT_REPORT.md               # Verified defect list and what was fixed
└── gradle/wrapper/                   # Wrapper jar is committed, so ./gradlew works
```

## 📄 License

Personal use only. Not for distribution.

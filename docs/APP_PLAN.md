# Hashmi Motors — Final Build Spec (v4)

> **Personal-use Android app** for you + father.
> **Status:** Approved for build.
> **Build route:** DIY with AI assistance (arena.ai).
> **Stack:** Native Android (Kotlin) + Jetpack Compose + Room + Firebase.

---

## 0. Decisions Locked In (v4)

| # | Decision | Choice |
|---|---|---|
| 1 | Sharing with father | **Two Google accounts**, both linked, each with own PIN |
| 2 | Builder | **You + arena.ai** (DIY) |
| 3 | Invoice sharing | **PDF + WhatsApp + SMS** (no printer) |
| 4 | Language | **English only** |
| 5 | Build order | **MVP-first** |
| 6 | SKU import (manual methods) | **All 7** — manual, CSV, Google Sheets, photo OCR, AI from old bills, barcode, voice |
| 7 | **SKU enrichment via web** | **On-demand scraping suggestions** (you search → app suggests → you confirm). Optional, never automatic. |
| 8 | GST | **Composition scheme** → "Bill of Supply" |
| 9 | Printer | **None** in v1 |
| 10 | Inventory size | 500–2,000 parts (medium shop) |
| 11 | **App polish** | **Fun & animated** — bouncy buttons, confetti, splash animation, sound effects, gradient backgrounds |
| 12 | **GitHub Actions** | **Debug APK** auto-built on every push to `arena/01a04216-hashmi-moters` branch |

---

## What's New in v4

Three additions since v3:

1. **On-demand web enrichment** — when you search for a part, the app can suggest OEM numbers, compatible vehicles, and reference prices from public parts catalogs. You confirm before anything is saved.
2. **Fun & animated UI** — full polish package: splash animation, smooth transitions, micro-interactions, sound effects (toggleable), confetti on bill save, animated background.
3. **GitHub Actions CI/CD** — every code push auto-builds a debug APK you can download and install on your phone in 1 click.

---

## 1. App Identity

| Item | Value |
|---|---|
| App name | **Hashmi Motors** |
| Package id | `com.hashmimotors.app` |
| Version | 1.0.0 (versionCode 1) |
| Min Android | Android 8.0 (API 26) — covers ~98% of devices |
| Target Android | Android 14 (API 34) |
| Play Store | Will publish later (separate workflow) |
| Repo | `tofik2003/Hashmi-moters-` on branch `arena/01a04216-hashmi-moters` |

---

## 2. Tech Stack (Final v4)

| Layer | Tech | Notes |
|---|---|---|
| Language | Kotlin 1.9+ | Official Android |
| UI | Jetpack Compose + Material 3 | Big-button, animated |
| **Animations** | **Compose Animation APIs** + Lottie | Bouncy buttons, confetti, splash |
| **Backgrounds** | **Compose Canvas** (gradient + particles) + Lottie | Subtle moving background |
| **Sounds** | **SoundPool + ExoPlayer** (small) | Tap, save, error, success sounds |
| Local DB | Room (SQLite) | Encrypted with SQLCipher |
| Cloud sync | Firebase Firestore | Free tier, real-time |
| Auth | Firebase Auth (Google Sign-In) | Each user has own account |
| Photos | Local file storage (paths in DB) | No cloud cost |
| PDF | Android PdfDocument | Free |
| Share | Android Sharesheet | One API for WhatsApp, SMS, email |
| OCR (shelf photos) | ML Kit Text Recognition | Free, on-device |
| Barcode scan | ML Kit + CameraX | Free, on-device |
| Voice input | Android SpeechRecognizer | Free, on-device |
| AI from old bills | Gemini API (free tier) | Parse paper bills into parts list |
| **Web enrichment** | **Cheerio (Node.js) + Firebase Cloud Functions** | On-demand scraping suggestions |
| **CI/CD** | **GitHub Actions** | Debug APK on every push |

---

## 3. App Architecture (v4)

```
┌─────────────────────────────────────────────────────┐
│              Android App (Kotlin)                    │
│                                                      │
│  ┌──────────────────────────────────────────────┐   │
│  │  UI Layer (Jetpack Compose + Animations)     │   │
│  │  - Screens, ViewModels, Navigation          │   │
│  │  - Lottie animations, SoundPool              │   │
│  └────────────────┬─────────────────────────────┘   │
│                   │                                  │
│  ┌────────────────▼─────────────────────────────┐   │
│  │  Domain Layer (UseCases)                     │   │
│  └────────────────┬─────────────────────────────┘   │
│                   │                                  │
│  ┌────────────────▼─────────────────────────────┐   │
│  │  Data Layer                                   │   │
│  │  ┌────────┐  ┌─────────┐  ┌────────────────┐  │   │
│  │  │ Room   │  │Firebase │  │Web Enrichment  │  │   │
│  │  │(encrypted)│Firestore│  │API (suggestions)│  │   │
│  │  └────────┘  └─────────┘  └────────────────┘  │   │
│  └──────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────┘
                     │ HTTPS (when online)
                     ▼
         ┌──────────────────────────────────────┐
         │   Firebase Cloud Functions (Node)    │
         │   - /searchEnrichment?q=brake+pad   │
         │   - Scrapes public parts catalogs    │
         │   - Returns suggestions (never saves)│
         │   - 24h cache to avoid rate limits   │
         └──────────────────────────────────────┘
```

---

## 4. Features (Detailed)

### F1. Authentication & Onboarding (unchanged from v3)

### F2. Dashboard (Enhanced with animations)

**New v4 elements:**
- **Animated logo** on app open (Lottie: car silhouette drives in, settles into logo)
- **Live counter** animates from 0 → today's sales (1.5s count-up)
- **Quick action cards** lift slightly on press (spring animation)
- **Low-stock alert** slides in from top with a soft warning sound (if enabled)
- **Background:** subtle moving gradient (deep blue → indigo) with slow-moving particles

### F3. Catalog (with Web Enrichment)

**F3.1 — Add Part (Manual)** — unchanged

**F3.2 — Bulk Import (7 methods)** — unchanged

**F3.3 — NEW: Web Enrichment (on-demand)**

When you search for a part and the app finds 0 or few local results, it shows a banner:

```
┌─────────────────────────────────────┐
│  🌐 No local results.               │
│  Search online for suggestions?     │
│  [Search Online] [No Thanks]        │
└─────────────────────────────────────┘
```

**If you tap Search Online:**
1. App sends query to our backend (`/searchEnrichment?q=brake+pad+swift`)
2. Backend scrapes 2-3 free public parts catalogs (e.g. boodmo.com, partsouq.com)
3. Returns up to 5 suggestions, each with:
   - Part name
   - OEM numbers found
   - Compatible vehicles mentioned
   - Reference price (if shown on site)
   - Source URL
4. App shows suggestions in a "🌐 From the web" section:

```
┌────────────────────────────────────────┐
│  Brake Pad - Maruti Swift [From Web]   │
│  OEM: 55810-63J10, 55810M68K10         │
│  Fits: Swift 2014-2023, Dzire           │
│  Price: ₹450-₹850 (varies)            │
│  Source: boodmo.com                    │
│  [Add to My Catalog] [Open in Browser] │
└────────────────────────────────────────┘
```

5. **You tap "Add to My Catalog"** → form pre-fills with the scraped data → you review, edit, save. The app never auto-saves scraped data.

**Backend scraping details:**
- Runs in Firebase Cloud Functions (Node.js)
- Uses Cheerio for HTML parsing
- 2-3 second timeout per source
- 24h cache per query (so same search doesn't re-scrape)
- Returns max 5 results per source
- Graceful failure: if a source is down, others still work
- If ALL sources fail: "Couldn't reach online catalogs right now. Try again later or add manually."

**Legal safety (built in):**
- Backend sets a proper User-Agent (we're not pretending to be a browser)
- Respects robots.txt (skips pages that disallow scraping)
- Doesn't bypass any anti-bot measures
- Caches aggressively to minimize requests
- We only store what YOU explicitly add to your catalog — the scraped suggestions are NOT stored long-term
- Source URL shown on every result so you can verify

**Suggested sites** (we'll start with 2-3, add more if you want):
1. **boodmo.com** — India-focused, has OEM numbers
2. **partsouq.com** — has good cross-brand coverage
3. **autodoc.co.in** — price comparison

**If scraping ever gets blocked:** the "Search Online" button just shows an error and you fall back to manual entry. App never breaks.

**F3.4 — Search & Filter** — unchanged
**F3.5 — Part Detail** — unchanged

### F4. Vehicle Fitment Search (unchanged from v3)

### F5. Billing & Invoicing (with animations)

**New v4 elements:**
- **Bill save** → full-screen confetti animation (1.5s) + success sound ("cha-ching" or similar) + haptic feedback
- **PDF generation** → progress indicator with smooth animation
- **WhatsApp share** → button has a "bounce" on press
- **Total amount** → counts up from 0 to final total (1s) when bill preview opens
- **Field focus** → subtle glow on focused field

### F6. Inventory (unchanged from v3)

### F7. Customers (unchanged from v3)

### F8. Reports (v1.1, unchanged from v3)

### F9. Settings (enhanced)

**New v4 additions:**
- **Sound effects:** toggle on/off, volume slider
- **Animations:** toggle on/off (for slower phones), animation speed (normal / reduced)
- **Theme:** Light / Dark / Auto (follows system)
- **Background style:** Gradient / Particles / Solid (your choice)
- **Accent color:** Pick from 4 options (default: indigo)

### F10. NEW: Onboarding Tutorial (4 screens)

First-time launch shows a 4-screen swipe tutorial with Lottie animations:
1. **Welcome** — animated car logo → "Welcome to Hashmi Motors"
2. **Search & Add Parts** — animation of typing & a part appearing → "Find any part in seconds"
3. **Create Bills** — animation of bill being created → "GST-compliant bills, share in 1 tap"
4. **Always in Sync** — animation of two phones syncing → "You and your father, always on the same page"

Skippable on every screen. Shown only once per device.

---

## 5. Polish & Animation Spec (Detailed)

### 5.1 Splash Screen
- **Duration:** 1.5 seconds
- **Animation:** Lottie file showing a stylized car silhouette driving in from the left, transforming into the "Hashmi Motors" logo
- **Background:** Dark blue gradient with subtle moving particles
- **Sound:** Soft startup chime (toggleable)
- **Implementation:** Android 12+ SplashScreen API + Lottie for the logo animation

### 5.2 Background

Two variants (user chooses in settings):

**Variant A — Gradient + Particles (default)**
- Vertical gradient: deep indigo (#1A237E) → black (#000000) → deep purple (#4A148C)
- Slowly moving particles (~30 small white dots) drifting upward
- Particle movement is subtle, doesn't distract
- Implementation: Compose Canvas with rememberInfiniteTransition

**Variant B — Solid Color**
- Just the dark indigo background
- For users who want zero distraction

### 5.3 Micro-interactions (every interactive element)

| Element | Animation | Duration |
|---|---|---|
| Button press | Scale 1.0 → 0.95 → 1.0 (spring) | 150ms |
| Button release | Subtle bounce | 200ms |
| List item add | Slide in from right + fade | 300ms |
| List item remove | Slide out to left + fade | 250ms |
| Card tap | Elevation 2dp → 8dp | 200ms |
| Field focus | Border color + 2dp glow | 200ms |
| Tab switch | Underline slides | 250ms |
| Modal open | Scale 0.9 → 1.0 + fade | 300ms |
| Page transition | Slide + fade (horizontal) | 350ms |
| Counter (numbers) | Count up animation | 1000ms |

### 5.4 Special Animations (key moments)

| Moment | Animation | Sound |
|---|---|---|
| Bill saved | Confetti (200-300 particles) + scale pulse on total | "Success" chime |
| PDF generated | Progress bar smooth + check mark draw | None |
| Low-stock alert slides in | Slide from top + bounce | Soft "ping" |
| Error happens | Shake (3 oscillations) | Error "buzz" |
| Empty state | Subtle floating animation on icon | None |
| Login success | Logo "lights up" | Success chime |

### 5.5 Sound Effects

| Event | Sound | Source |
|---|---|---|
| App open | Soft startup chime | Free from freesound.org (CC0) |
| Bill save | "Cha-ching" / success chime | Free from freesound.org |
| Error | Subtle error buzz | Free from freesound.org |
| Low stock alert | Soft "ping" | Free from freesound.org |
| Tap on primary button | Subtle click (very short) | Free from freesound.org |

**Settings:**
- All sounds toggleable (master switch)
- Volume slider (0-100%)
- "Silent mode" preset (all off, for shop use)
- "Full experience" preset (all on, for demo/fun)

**Implementation:** SoundPool for short sounds (clicks, pings), ExoPlayer only if needed for longer sounds.

### 5.6 Confetti

- **Library:** `konfetti` for Compose (free, MIT licensed, ~50KB)
- **When:** After saving a bill
- **Duration:** 1.5s
- **Colors:** App accent colors (default: indigo, pink, gold)
- **Particles:** 200-300

### 5.7 Lottie Animations

| Where | What | Source |
|---|---|---|
| Splash | Car driving in | LottieFiles (free) |
| Tutorial screen 1 | Car silhouette | LottieFiles (free) |
| Tutorial screen 2 | Typing & part appearing | LottieFiles (free) |
| Tutorial screen 3 | Bill being created | LottieFiles (free) |
| Tutorial screen 4 | Two phones syncing | LottieFiles (free) |
| Empty states (no parts, no bills) | Floating box icon | LottieFiles (free) |
| Loading | Spinner / progress | Built-in Compose |

I'll source these from LottieFiles (all free, CC0 or MIT). If I can't find a good fit, I'll create simple ones using After Effects export or Compose-built alternatives.

---

## 6. Data Model (v4 — same as v3, with these additions)

```kotlin
// New: app settings (theme, sounds, animations)
data class AppSettings(
  val id: String = "default",
  val themeMode: ThemeMode,             // LIGHT | DARK | AUTO
  val backgroundStyle: BackgroundStyle, // GRADIENT_PARTICLES | SOLID
  val accentColor: AccentColor,         // INDIGO | BLUE | GREEN | ORANGE
  val soundsEnabled: Boolean = true,
  val soundVolume: Int = 80,            // 0-100
  val animationsEnabled: Boolean = true,
  val animationSpeed: AnimationSpeed,   // NORMAL | REDUCED
  val tutorialShown: Boolean = false
)

enum class ThemeMode { LIGHT, DARK, AUTO }
enum class BackgroundStyle { GRADIENT_PARTICLES, SOLID }
enum class AccentColor { INDIGO, BLUE, GREEN, ORANGE }
enum class AnimationSpeed { NORMAL, REDUCED }

// New: web enrichment cache (24h, only suggestions, never auto-saved)
data class WebEnrichmentCache(
  val query: String,
  val results: List<WebEnrichmentResult>,
  val fetchedAt: Long,
  val expiresAt: Long                    // fetchedAt + 24h
)

data class WebEnrichmentResult(
  val partName: String,
  val oemNumbers: List<String>,
  val compatibleVehicles: List<String>,
  val priceRange: String?,               // e.g. "₹450-₹850"
  val sourceUrl: String,
  val sourceName: String                 // "Boodmo", "Partsouq"
)
```

All other data classes from v3 remain unchanged.

---

## 7. Screen List (v4 — 17 Screens)

| # | Screen | New v4 elements |
|---|---|---|
| 1 | Splash | Lottie animation + startup sound |
| 2 | Google Sign-In | — |
| 3 | PIN Setup | — |
| 4 | **Onboarding Tutorial (4 swipe screens)** | **NEW** — Lottie animations |
| 5 | Shop Setup (one-time) | — |
| 6 | Dashboard | Animated background, live counter, bounce buttons |
| 7 | Search Parts | **Web enrichment banner** when no local results |
| 8 | Part Detail | — |
| 9 | Add Part (manual) | Field focus glow |
| 10 | Add Parts Hub | — |
| 11 | **Web Enrichment Results** | **NEW** — shows online suggestions with "Add" button |
| 12 | Fitment Wizard | — |
| 13 | New Bill | Line item animations |
| 14 | Invoice Preview | **Confetti + count-up total + success sound** |
| 15 | Invoice History | — |
| 16 | Add Stock | — |
| 17 | Inventory | — |
| 18 | Settings | **Theme, sounds, animations, background, accent color toggles** |

---

## 8. Build Order (v4 — 4 Phases, 4-5 weeks)

### Phase 1 — Foundation + MVP (Week 1-2)
- [ ] Android project setup with Compose
- [ ] Firebase project + Google Sign-In
- [ ] Room database with encryption
- [ ] PIN + biometric
- [ ] Shop setup screen
- [ ] **GitHub Actions workflow** (debug APK on every push)
- [ ] **Animated splash screen** (Lottie car animation)
- [ ] **Animated background** (gradient + particles)
- [ ] **Sound effects system** (toggleable)
- [ ] Add Part (manual)
- [ ] Search Parts
- [ ] Part Detail
- [ ] New Bill screen
- [ ] Save to Room
- [ ] Invoice Preview (Bill of Supply)
- [ ] PDF generation
- [ ] **Confetti on bill save**
- [ ] **Count-up total animation**
- [ ] Share via Android sharesheet
- **Milestone:** You can add a part, create a bill, see confetti, share PDF. Build artifact on every push.

### Phase 2 — Sync + Onboarding (Week 2.5)
- [ ] Firestore integration
- [ ] Two-account linking
- [ ] Real-time sync
- [ ] **4-screen onboarding tutorial** with Lottie animations
- [ ] Settings screen (with new theme/sound/animation toggles)
- **Milestone:** Both phones see same data, tutorial plays once.

### Phase 3 — Bulk Import + Web Enrichment (Week 3-4)
- [ ] CSV / Excel import
- [ ] Google Sheets sync
- [ ] Photo OCR (ML Kit)
- [ ] AI extract from old bills (Gemini API)
- [ ] Barcode scan
- [ ] Voice input
- [ ] **Web Enrichment backend** (Firebase Cloud Functions + Cheerio)
- [ ] **Web Enrichment UI** (search → suggestion → confirm)
- [ ] Add Stock screen
- [ ] Stock adjustment
- [ ] Low-stock dashboard alert
- **Milestone:** You can import parts in any format, including on-demand web suggestions.

### Phase 4 — Fitment + Polish + Launch (Week 4-5)
- [ ] Pre-seed vehicle data (top 20 brands, 200 models)
- [ ] Fitment wizard 3-step
- [ ] Fitment result screen
- [ ] Invoice history with filter
- [ ] Customer list + detail
- [ ] Daily sales report
- [ ] Backup/export
- [ ] **Final polish pass** (all animations, all sounds, all micro-interactions)
- [ ] **Performance audit** (animations on slow phones)
- [ ] **Battery usage check**
- [ ] Bug bash + UI polish
- [ ] **Internal testing** (you + father use for 1 week)
- [ ] **GitHub Actions: add release AAB build** (for future Play Store)
- **Milestone:** v1.0.0 ready for production. Debug APK + signed AAB both built automatically.

---

## 9. GitHub Actions Setup (Detailed)

### 9.1 Workflow File

I'll create `.github/workflows/build-debug-apk.yml`:

```yaml
name: Build Debug APK

on:
  push:
    branches: [ arena/01a04216-hashmi-moters ]
  pull_request:
    branches: [ arena/01a04216-hashmi-moters ]
  workflow_dispatch:  # Manual trigger from GitHub UI

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Gradle packages
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: ${{ runner.os }}-gradle-

      - name: Make gradlew executable
        run: chmod +x gradlew

      - name: Build Debug APK
        run: ./gradlew assembleDebug

      - name: Upload APK as artifact
        uses: actions/upload-artifact@v4
        with:
            name: hashmi-motors-debug-${{ github.sha }}
            path: app/build/outputs/apk/debug/app-debug.apk
            retention-days: 30

      - name: Sign APK with debug keystore
        run: |
          # Auto-create debug keystore on first run, reuse thereafter
          if [ ! -f ~/.android/debug.keystore ]; then
            mkdir -p ~/.android
            keytool -genkey -v -keystore ~/.android/debug.keystore \
              -storepass android -alias androiddebugkey \
              -keypass android -keyalg RSA -keysize 2048 \
              -validity 10000 -dname "CN=Hashmi Motors Debug,O=Android,C=US"
          fi

      - name: Build release APK (unsigned, for local install)
        run: ./gradlew assembleRelease
        continue-on-error: true

  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run unit tests
        run: ./gradlew testDebugUnitTest
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: app/build/reports/tests/
```

### 9.2 How you'll use it

1. **Every time I (or you) push code** → GitHub Action runs automatically
2. **Build takes ~5-8 minutes** for first build, ~2-3 minutes for incremental
3. **APK is uploaded as an artifact** — click the green ✅ check on your commit
4. **Download APK** → transfer to your phone → install
5. **Install on father's phone** the same way (different Google account login)

### 9.3 What you'll see in GitHub

```
✅ Build Debug APK #42
   ✓ Checkout code (3s)
   ✓ Set up JDK 17 (5s)
   ✓ Cache Gradle packages (1s)
   ✓ Build Debug APK (3m 12s)
   ✓ Upload APK as artifact (2s)

   📦 hashmi-motors-debug-abc1234
      → app-debug.apk (18 MB)
```

### 9.4 Future (when you want to publish to Play Store)

When you're ready, I'll add a second workflow `release.yml` that:
- Uses a real keystore (stored in GitHub Secrets — never in code)
- Builds a signed AAB
- Optionally auto-uploads to Play Store internal testing track

This is for v1.1 — not needed for v1.0.0 internal testing.

---

## 10. Project File Structure (v4)

```
Hashmi-moters-/
├── .github/
│   └── workflows/
│       ├── build-debug-apk.yml      # NEW: GitHub Actions
│       └── (release.yml in future)
├── app/
│   ├── build.gradle.kts             # Compose, Lottie, Konfetti, ML Kit
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/hashmimotors/app/
│   │   │   ├── MainActivity.kt
│   │   │   ├── App.kt
│   │   │   ├── ui/
│   │   │   │   ├── theme/           # Material 3 + accent colors
│   │   │   │   ├── animations/      # Confetti, count-up, etc.
│   │   │   │   ├── components/      # Animated buttons, cards
│   │   │   │   ├── auth/            # Login, PIN, Onboarding
│   │   │   │   ├── dashboard/
│   │   │   │   ├── catalog/
│   │   │   │   ├── enrichment/      # NEW: Web suggestions
│   │   │   │   ├── fitment/
│   │   │   │   ├── billing/
│   │   │   │   ├── inventory/
│   │   │   │   ├── customers/
│   │   │   │   ├── import/
│   │   │   │   └── settings/
│   │   │   ├── domain/
│   │   │   ├── data/
│   │   │   │   ├── local/           # Room
│   │   │   │   ├── remote/          # Firebase
│   │   │   │   ├── enrichment/      # NEW: Web API client
│   │   │   │   └── util/            # PDF, OCR, barcode, AI, sound
│   │   │   └── di/
│   │   ├── assets/                  # NEW: Lottie JSONs, sound files
│   │   │   ├── lottie/
│   │   │   │   ├── splash.json
│   │   │   │   ├── tutorial_1.json
│   │   │   │   ├── ...
│   │   │   └── sounds/
│   │   │       ├── success.ogg
│   │   │       ├── error.ogg
│   │   │       ├── click.ogg
│   │   │       └── ...
│   │   └── res/
├── functions/                       # NEW: Firebase Cloud Functions
│   ├── package.json
│   ├── src/
│   │   └── enrichment.ts            # Cheerio-based scraper
│   ├── tsconfig.json
│   └── .gitignore
├── firestore.rules
├── data/
│   └── seed/
│       └── vehicles.json            # Top-20 Indian car brands
├── docs/
│   ├── APP_PLAN.md                  # This file
│   ├── USER_GUIDE.md
│   ├── CSV_TEMPLATE.md
│   └── GITHUB_ACTIONS_GUIDE.md      # NEW: How to download & install APK
└── README.md
```

---

## 11. Setup Steps Before We Start Coding (v4)

I need you to do 6 things (each 5-15 minutes):

### Step 1: Create a Google Cloud / Firebase project
- Go to https://console.firebase.google.com
- Sign in with your Google account
- Click "Add project" → name "Hashmi Motors" → create
- Once created, click the Android icon to add an Android app
- **Package name:** `com.hashmimotors.app`
- Download `google-services.json` → save it (I'll need to add it to the repo as a secret or you'll need to add it to `app/`)

### Step 2: Enable Authentication
- Firebase Console → Authentication → Sign-in method → enable Google
- Add your email as a test user

### Step 3: Get a Gemini API key
- Go to https://aistudio.google.com/app/apikey
- Create API key (free tier)

### Step 4: Initialize Firebase Cloud Functions (for web enrichment)
- Install Firebase CLI: `npm install -g firebase-tools`
- Login: `firebase login`
- In the repo: `firebase init functions` → select your project → TypeScript
- This creates the `functions/` folder

### Step 5: Provide shop details
- Shop name, full address, GSTIN, phone, state

### Step 6: Provide father's Google email
- Just his Gmail address

### Step 7 (Optional but recommended): GitHub repo access
- You already have the repo at `tofik2003/Hashmi-moters-`
- I'll need write access to push code (you have this since we created the branch)
- For GitHub Actions to work, the workflow files just need to be in `.github/workflows/` — no extra setup needed

---

## 12. When you're ready

Once you complete the 7 steps above, reply with **"Let's start coding"** and:

1. I'll set up the Android project structure
2. I'll create the GitHub Actions workflow (builds APK on every push)
3. We'll build the MVP in order (Phase 1) — about 10-14 small steps
4. After MVP, we add sync (Phase 2)
5. Then bulk import + web enrichment (Phase 3)
6. Then fitment + final polish (Phase 4)
7. You + father test for a week
8. We publish to Play Store (Phase 5 — optional, later)

**Estimated total time for v1.0.0: 4-5 weeks of part-time work (1-2 hours/day).**

---

## 13. Open Items I'll Handle Without Asking

- Default categories (Engine, Brakes, Suspension, Electrical, Body, Filters, Oils, Belts, Hoses, Accessories, Tools)
- Top-20 Indian car brand list for fitment
- Bill of Supply template (matches Indian GST rules for Composition dealers)
- Default app icon (Hashmi Motors branding)
- Sample data for testing (50 demo parts you can delete later)
- **Lottie animations** (sourced from LottieFiles, all free/CC0/MIT)
- **Sound effects** (sourced from freesound.org, all free/CC0)
- **Color palette** (4 accent color options, default indigo)
- **Gradle dependencies** (Compose, Lottie, Konfetti, ML Kit, Firebase, Cheerio)
- **ProGuard rules** (for release build, when we get there)
- **Network security config** (HTTPS only)
- **App icon set** (adaptive icon for Android 8+)
- **Permission requests** (camera for OCR/barcode, microphone for voice — minimal, only when needed)

---

## 14. Updated Cost & Timeline (v4)

| Item | Cost |
|---|---|
| Android app development (DIY) | **₹0** (your time + arena.ai) |
| Firebase (free tier) | **₹0** (2 users fits free tier) |
| Firebase Cloud Functions (free tier) | **₹0** (125K invocations/month free — we'll use ~1-5K) |
| Gemini API (free tier) | **₹0** (1500 requests/day free) |
| GitHub Actions (free tier) | **₹0** (2000 minutes/month free — we'll use ~300) |
| GitHub repo | **₹0** (already have it) |
| Play Store developer account | **₹2,100** (one-time, when publishing) |
| **Total** | **₹2,100 only** |

**Timeline: 4-5 weeks part-time for v1.0.0**

---

## 15. Honest Risks (v4 additions)

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Scraping sites block our backend | Medium | Low (feature, not core) | Aggressive caching (24h), graceful failure, manual fallback always works |
| Scraper breaks when site redesigns | Medium | Low | Easy to fix or disable — feature degrades, app still works |
| Lottie animations add APK size | Low | Low (1-2 MB total) | Source small files (<100 KB each), use compressed JSON |
| Sound effects annoy in shop | Medium | Low | Master toggle in settings, "Silent mode" preset for shop use |
| Animations drain battery on old phones | Medium | Low | "Reduced animation" mode in settings, performance audit before launch |
| GitHub Actions builds slow on first run | Low | Low | Cache Gradle dependencies, ~5 min first build, ~2 min after |
| Web enrichment suggestions are wrong | Medium | Medium | You always review and confirm before saving — never auto-saved |
| Confetti on every bill save feels gimmicky | Low | Low | Toggle in settings, only on bill save (not every action) |

---

*End of v4 spec. Reply with shop details + father email + "Let's start" to begin Phase 1.*

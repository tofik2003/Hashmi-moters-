# Hashmi Motors — Error Fixes Applied

## Overview
This document tracks all error fixes applied during code inspection and build audit passes.

## Fixes Applied in Latest Pass (Aug 2026)

### 6. DrawScope `size` Shadowing in Particle Drawing
**File:** `app/src/main/java/com/hashmimotors/app/ui/components/AnimatedBackground.kt:79-90`
**Issue:** `particles.forEach { (xRatio, yRatio, size) ->` destructured the particle size float into a local variable named `size`. Inside `Canvas { ... }` (which provides `DrawScope`), this shadowed `DrawScope.size: Size`. As a result, `size.width` and `size.height` attempted to call `.width` and `.height` on a `Float`, causing compilation failure (`Unresolved reference: width/height`).
**Fix:** Renamed the destructured parameter to `particleScale`:
```kotlin
particles.forEach { (xRatio, yRatio, particleScale) ->
    val drift = (particleOffset + yRatio) % 1f
    val x = xRatio * size.width
    val y = drift * size.height
    val alpha = (0.3f + 0.3f * sin(drift * Math.PI.toFloat() * 2)) * pulse
    drawCircle(
        color = Color.White.copy(alpha = alpha * 0.5f),
        radius = particleScale * 6f,
        center = Offset(x, y)
    )
}
```

### 7. Missing Room TypeConverter for `InvoiceEntity.lines`
**Files:** `app/src/main/java/com/hashmimotors/app/data/local/Converters.kt`, `Entities.kt`
**Issue:** `InvoiceEntity` contained `val lines: List<InvoiceLineEmbedded>`. Room's annotation processor failed because there was no TypeConverter for `List<InvoiceLineEmbedded>`, and `InvoiceLineEmbedded` was not annotated with `@Serializable`.
**Fix:** Added `@Serializable` to `InvoiceLineEmbedded` and registered `fromInvoiceLineList` and `toInvoiceLineList` in `Converters.kt`:
```kotlin
@TypeConverter
fun fromInvoiceLineList(value: List<InvoiceLineEmbedded>?): String {
    return if (value == null) "" else json.encodeToString(ListSerializer(InvoiceLineEmbedded.serializer()), value)
}

@TypeConverter
fun toInvoiceLineList(value: String?): List<InvoiceLineEmbedded> {
    if (value.isNullOrEmpty()) return emptyList()
    return try {
        json.decodeFromString(ListSerializer(InvoiceLineEmbedded.serializer()), value)
    } catch (e: Exception) {
        emptyList()
    }
}
```

### 8. `StockMovementEntity` Missing Default Values for Constructor
**File:** `app/src/main/java/com/hashmimotors/app/data/local/Entities.kt`
**Issue:** In `PartRepository.kt`, `StockMovementEntity` was instantiated without `reason` and `timestamp`. In `Entities.kt`, those fields lacked default values, producing compile errors (`No value passed for parameter 'reason'/'timestamp'`).
**Fix:** Added default arguments in `StockMovementEntity`:
```kotlin
data class StockMovementEntity(
    @PrimaryKey val id: String,
    val partId: String,
    val type: String,
    val qty: Int,
    val refType: String?,
    val refId: String?,
    val reason: String? = null,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

### 9. Invalid Canvas Constructor in PDF Generator
**File:** `app/src/main/java/com/hashmimotors/app/ui/billing/InvoicePreviewScreen.kt:483`
**Issue:** `android.graphics.Canvas(page.canvas)` was called, but `page.canvas` is already an `android.graphics.Canvas`. The Android SDK `Canvas` class does not have a copy constructor `Canvas(Canvas)`, which caused compile failure.
**Fix:** Changed to:
```kotlin
val canvas = page.canvas
```

### 10. Non-Existent Compose Material Icon `BarcodeReader`
**File:** `app/src/main/java/com/hashmimotors/app/ui/catalog/SearchScreen.kt:22, 83`
**Issue:** `Icons.Filled.BarcodeReader` was imported and used, which does not exist in Compose Material Icons.
**Fix:** Replaced with `Icons.Filled.QrCodeScanner`.

### 11. ModalBottomSheet Opt-In Annotation
**File:** `app/src/main/java/com/hashmimotors/app/ui/billing/BillingScreen.kt`
**Issue:** Material 3 `ModalBottomSheet` requires `@OptIn(ExperimentalMaterial3Api::class)`.
**Fix:** Added `@OptIn(ExperimentalMaterial3Api::class)` above `PartPickerSheet`.

### 12. Ambiguous `.shadow()` Overload in DashboardScreen
**File:** `app/src/main/java/com/hashmimotors/app/ui/dashboard/DashboardScreen.kt:148`
**Issue:** `Modifier.shadow(12.dp, RoundedCornerShape(20.dp))` used positional parameters, triggering ambiguous overload resolution in Compose 1.6+.
**Fix:** Used explicit named parameters:
```kotlin
.shadow(
    elevation = 12.dp,
    shape = RoundedCornerShape(20.dp),
    clip = false
)
```

---

## Previous Fixes Applied

### 1. Compose Shadow API Parameter Naming
**File:** `app/src/main/java/com/hashmimotors/app/ui/components/AnimatedButton.kt`

### 2. Cha-Ching Sound Resource Destructuring
**File:** `app/src/main/java/com/hashmimotors/app/ui/sound/SoundManager.kt`

### 3. `collectAsStateSafe` References
**File:** `app/src/main/java/com/hashmimotors/app/ui/reports/ReportsScreen.kt`

### 4. DashboardViewModel — Combine Limit
**File:** `app/src/main/java/com/hashmimotors/app/ui/dashboard/DashboardViewModel.kt`

### 5. BillingViewModel — Cleared Cart After Save
**File:** `app/src/main/java/com/hashmimotors/app/ui/billing/BillingViewModel.kt`

---

## How to Build the APK

### Method 1: On GitHub Actions (Automatic)
Because the GitHub App token in this session cannot directly create `.github/workflows/` (due to GitHub's OAuth workflow scope restriction), follow these 2 quick steps:
1. On GitHub.com, navigate to your repository `tofik2003/Hashmi-moters-`.
2. Create or copy the file `github/workflows/build-apk.yml` into `.github/workflows/build-apk.yml`.
3. GitHub Actions will automatically run the build and output the downloadable `app-debug.apk` in the Artifacts section!

### Method 2: Locally using Android Studio or Terminal
Run the following in the repository root:
```bash
./gradlew assembleDebug
```
The resulting APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

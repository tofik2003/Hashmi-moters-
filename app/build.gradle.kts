plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    // Note: com.google.gms.google-services is intentionally NOT applied by default
    // because it requires google-services.json. To enable Firebase:
    // 1. Create a Firebase project at https://console.firebase.google.com
    // 2. Add Android app with package name: com.hashmimotors.app
    // 3. Download google-services.json to app/google-services.json
    // 4. Uncomment: id("com.google.gms.google-services") above
    // 5. Add to gradle.properties: firebase.enabled=true
}

// Conditionally apply google-services plugin if enabled
val firebaseEnabled = project.findProperty("firebase.enabled")?.toString()?.toBoolean() ?: false
if (firebaseEnabled) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.hashmimotors.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hashmimotors.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// The CI workflow runs `./gradlew assembleDebug` and nothing else, and this
// repository's GitHub App token is not allowed to edit .github/workflows/.
// Gate the APK on the unit tests here instead, so a red test suite can never
// produce a shippable artifact: `assembleDebug` fails if any test fails.
//
// matching/configureEach (not tasks.named) because AGP registers the assemble*
// tasks during afterEvaluate, so a configuration-time named() lookup would fail
// with UnknownTaskException.
tasks.matching { it.name == "assembleDebug" }.configureEach {
    dependsOn("testDebugUnitTest")
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-android-compiler:2.50")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // NOTE: the following were previously declared but never referenced from any
    // source file. They only inflated the APK (ML Kit + CameraX alone add several
    // MB) and, in ML Kit's case, pulled a manifest placeholder for OCR/barcode
    // models that the app never loads. Add them back together with the feature
    // that actually uses them:
    //   androidx.security:security-crypto   (DB is NOT encrypted - see README)
    //   com.google.mlkit:text-recognition / barcode-scanning   (no OCR/scanner UI)
    //   androidx.camera:*                                    (no photo capture)
    //   com.airbnb.android:lottie-compose                    (animations are Compose)
    //   nl.dionsegijn:konfetti-compose                       (no confetti screen)
    //   io.coil-kt:coil-compose                              (no image loading)
    //   androidx.datastore:datastore-preferences             (settings live in Room)
    //   androidx.biometric:biometric                         (no PIN/biometric lock)
    //   kotlinx-coroutines-play-services                     (no Firebase)

    // Firebase (optional - only included if firebase.enabled=true)
    // Uncomment these when you have set up Firebase and added google-services.json
    // implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    // implementation("com.google.firebase:firebase-auth-ktx")
    // implementation("com.google.firebase:firebase-firestore-ktx")
    // implementation("com.google.firebase:firebase-storage-ktx")
    // implementation("com.google.android.gms:play-services-auth:20.7.0")
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Splash screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

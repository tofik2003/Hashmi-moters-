# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# Keep Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp

# Keep Room
-keep class androidx.room.** { *; }

# Keep Kotlin Serialization
-keepclassmembers class **$$serializer { *; }
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

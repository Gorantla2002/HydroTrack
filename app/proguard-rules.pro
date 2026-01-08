# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep data models
-keep class uk.ac.tees.mad.hydrotrackplus.data.model.** { *; }

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exception

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Android default proguard rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.Database <methods>;
}
-keepclassmembers class * {
    @androidx.room.Entity <methods>;
}
-keepclassmembers class * {
    @androidx.room.Dao <methods>;
}

# Keep Room database classes
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.Database { *; }

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep LiveData
-keep class androidx.lifecycle.LiveData { *; }

# Keep Kotlin
-keep class kotlin.Metadata { *; }
-keep class kotlin.** { *; }
-dontwarn kotlin.**

# Keep ZXing
-keep class com.journeyapps.barcodescanner.** { *; }
-keep class com.google.zxing.** { *; }

# Keep Material Components
-keep class com.google.android.material.** { *; }

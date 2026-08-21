# AkashBoard ProGuard Rules

# Keep IME service
-keep class com.akashboard.AkashBoardIME { *; }

# Keep JNI bridge
-keep class com.akashboard.engine.PredictorBridge { *; }

# Keep Room entities
-keep class com.akashboard.data.ClipboardItem { *; }

# Keep Room DAOs
-keep class com.akashboard.data.ClipboardDao { *; }

# Keep Room Database
-keep class com.akashboard.data.ClipboardDB { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# Keep serialized data classes
-keepattributes *Annotation*
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.akashboard.**$$serializer { *; }
-keepclassmembers class com.akashboard.** {
    *** Companion;
}
-keepclasseswithmembers class com.akashboard.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep settings fragments
-keep class com.akashboard.settings.** { *; }

# General
-keepattributes Signature
-keepattributes Exceptions
-dontwarn javax.annotation.**

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

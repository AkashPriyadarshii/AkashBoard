# AkashBoard ProGuard Rules

# Keep IME service
-keep class com.akashboard.AkashBoardIME { *; }

# Keep JNI bridge
-keep class com.akashboard.engine.PredictorBridge { *; }

# Keep Room entities
-keep class com.akashboard.data.ClipboardItem { *; }

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

# General
-keepattributes Signature
-keepattributes Exceptions
-dontwarn javax.annotation.**

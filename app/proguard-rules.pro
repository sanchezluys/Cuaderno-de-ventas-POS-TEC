# Project ProGuard / R8 Optimization & Obfuscation Rules

# Optimization passes and aggressive optimizations
-optimizationpasses 5
-allowaccessmodification
-repackageclasses 'o'

# Obfuscate source file names and preserve line numbers for de-obfuscation mapping
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room Database persistence rules
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class * { *; }
-keep class com.example.data.model.** { *; }
-keep class com.example.data.local.** { *; }

# Kotlin Coroutines & Reflection rules for safe obfuscation
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Android Compose runtime
-keep class androidx.compose.runtime.** { *; }


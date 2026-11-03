# ─── AdaptiveTrust Mobile ProGuard Rules ───────────────────────────────────────

# ── Retrofit2 / OkHttp ─────────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# ── Gson / JSON Models ─────────────────────────────────────────────────────────
-keep class com.google.gson.** { *; }
-keep class com.adaptiveTrust.mobile.data.models.** { *; }
-keepclassmembers class com.adaptiveTrust.mobile.data.models.** {
    <fields>;
    <init>(...);
}

# ── Room ───────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.**

# ── Hilt / Dagger ──────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.* { *; }
-dontwarn dagger.**

# ── Kotlin Coroutines ──────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ── WorkManager ────────────────────────────────────────────────────────────────
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-dontwarn androidx.work.**

# ── Biometric ──────────────────────────────────────────────────────────────────
-keep class androidx.biometric.** { *; }

# ── MPAndroidChart ─────────────────────────────────────────────────────────────
-keep class com.github.mikephil.charting.** { *; }

# ── OkHttp WebSocket ───────────────────────────────────────────────────────────
-keep class okhttp3.internal.ws.** { *; }

# ── DataStore ──────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }

# ── General Android ────────────────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver

# Rei 零 — ProGuard Rules

# ── Kotlin ──
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Metadata { public <methods>; }
-assumenosideeffects class kotlin.jvm.internal.Intrinsics { static void checkParameterIsNotNull(java.lang.Object, java.lang.String); }

# ── Kotlin Serialization ──
-dontwarn kotlinx.serialization.**
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class kotlinx.serialization.json.JsonObject { *; }
-keep class * implements kotlinx.serialization.KSerializer { <init>(...); }
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    public static final ** Companion;
}

# ── OkHttp / Okio ──
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**

# ── Coil (Image Loading) ──
-dontwarn coil.**
-keep class coil.** { *; }

# ── Room ──
-keep class * extends androidx.room.RoomDatabase { <init>(...); }
-keep @androidx.room.Entity class * { <fields>; }
-keepclassmembers class * {
    @androidx.room.Insert <methods>;
    @androidx.room.Update <methods>;
    @androidx.room.Delete <methods>;
    @androidx.room.Query <methods>;
}

# ── Hilt / Dagger ──
-dontwarn dagger.**
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { <init>(...); }

# ── Compose ──
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# ── Navigation ──
-keepnames class androidx.navigation.fragment.NavHostFragment
-keep class * extends androidx.navigation.Navigator

# ── DataStore ──
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { <init>(...); }

# ── WorkManager ──
-keep class * extends androidx.work.CoroutineWorker { <init>(...); }
-keep class * extends androidx.work.ListenableWorker { <init>(...); }

# ── Glance Widget ──
-dontwarn androidx.glance.**
-keep class androidx.glance.** { *; }

# ── AniList / MAL / Jikan ──
-keep class com.rei.app.data.remote.** { *; }
-keep class com.rei.app.domain.model.** { *; }

# ── Backup serialization ──
-keep class com.rei.app.util.BackupUtil$* { *; }

# ── General Android ──
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.app.Application

# ── Deep Links ──
-keep class com.rei.app.ui.MainActivity { *; }

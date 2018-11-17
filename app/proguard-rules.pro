# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-obfuscationdictionary proguard-dictionary.txt
-packageobfuscationdictionary proguard-dictionary.txt
-classobfuscationdictionary proguard-dictionary.txt

-repackageclasses 'arcanos'

-optimizationpasses 5

# Debugging
-keepattributes LineNumberTable, SourceFile

# Common attributes
-keepattributes Signature, Exceptions, InnerClasses, EnclosingMethod, *Annotation*

# Remove log calls
-assumenosideeffects class android.util.Log {
    public static *** d(...);
}

# Kotlin
-keep class kotlin.** { *; }
-dontnote kotlin.internal.PlatformImplementationsKt
-dontnote kotlin.jvm.internal.Reflection

# Keep models
-keep class de.developercity.arcanosradio.features.streaming.data.models.* { *; }

# Keep custom errors
-keep class * extends java.lang.Exception
-keep class * extends java.lang.Throwable

# Material
-dontnote com.google.android.material.**

# Dagger
-keep class javax.inject.Provider

# Rx
-dontnote io.reactivex.**

# Retrofit
-keep class retrofit2.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.Platform$Java8

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn org.conscrypt.**
-dontnote okhttp3.**
-dontnote okio.**

# Gson
-dontnote sun.misc.Unsafe
-dontnote com.google.gson.**

# Stetho
-dontnote com.facebook.stetho.**

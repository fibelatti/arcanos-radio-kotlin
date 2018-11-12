object Versions {
    const val gradlePluginVersion = "3.2.1"
    const val kotlinVersion = "1.3.0"
    const val dexCountPlugin = "0.8.2"
    const val jacocoVersion = "0.8.1"

    const val minSdkVersion = 21
    const val targetSdkVersion = 28
    const val compileSdkVersion = 28

    const val buildToolsVersion = "28.0.3"

    internal const val appCompatVersion = "1.0.0"
    internal const val materialDesignVersion = "1.0.0"
    internal const val constraintLayoutVersion = "2.0.0-alpha1"

    internal const val daggerVersion = "2.17"

    internal const val rxJavaVersion = "2.2.2"
    internal const val rxAndroidVersion = "2.1.0"

    internal const val retrofitVersion = "2.4.0"
    internal const val httpLoggingInterceptorVersion = "3.10.0"
    internal const val stethoVersion = "1.5.0"

    internal const val gsonVersion = "2.8.5"
    internal const val picassoVersion = "2.71828"

    internal const val jUnitVersion = "4.12"
    internal const val testRunnerVersion = "1.0.1"
    internal const val mockitoVersion = "2.23.0"
    internal const val mockitoAndroidVersion = "2.18.3"
    internal const val junit5pluginVersion = "1.2.0.0"
    internal const val junit5Version = "5.2.0"
}

object Classpaths {
    val gradlePlugin = "com.android.tools.build:gradle:${Versions.gradlePluginVersion}"
    val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}"
    val junit5Plugin = "de.mannodermaus.gradle.plugins:android-junit5:${Versions.junit5pluginVersion}"
    val jacocoPlugin = "org.jacoco:org.jacoco.core:${Versions.jacocoVersion}"
    val dexCountPlugin = "com.getkeepsafe.dexcount:dexcount-gradle-plugin:${Versions.dexCountPlugin}"
}

object KotlinDependencies {
    val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlinVersion}"
}

object SupportLibraryDependencies {
    val supportLibrary = "androidx.appcompat:appcompat:${Versions.appCompatVersion}"
    val materialDesign = "com.google.android.material:material:${Versions.materialDesignVersion}"
    val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayoutVersion}"
    val media = "androidx.media:media:${Versions.appCompatVersion}"
}

object DIDependencies {
    val dagger = "com.google.dagger:dagger:${Versions.daggerVersion}"
    val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.daggerVersion}"
}

object RxDependencies {
    val rxJava = "io.reactivex.rxjava2:rxjava:${Versions.rxJavaVersion}"
    val rxAndroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxAndroidVersion}"
}

object NetworkDependencies {
    val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofitVersion}"
    val retrofitGsonConverter = "com.squareup.retrofit2:converter-gson:${Versions.retrofitVersion}"
    val retrofitAdapter = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofitVersion}"
    val httpLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.httpLoggingInterceptorVersion}"
    val stetho = "com.facebook.stetho:stetho:${Versions.stethoVersion}"
    val stethoOkHttp = "com.facebook.stetho:stetho-okhttp3:${Versions.stethoVersion}"
}

object ThirdPartyDependencies {
    val gson = "com.google.code.gson:gson:${Versions.gsonVersion}"
    val picasso = "com.squareup.picasso:picasso:${Versions.picassoVersion}"
}

object TestDependencies {
    val junit = "junit:junit:${Versions.jUnitVersion}"
    val junit5 = "org.junit.jupiter:junit-jupiter-api:${Versions.junit5Version}"
    val junit5Engine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit5Version}"
    val junit5Params = "org.junit.jupiter:junit-jupiter-params:${Versions.junit5Version}"
    val junitVintage = "org.junit.vintage:junit-vintage-engine:${Versions.junit5Version}"
    val testRunner = "com.android.support.test:runner:${Versions.testRunnerVersion}"
    val kotlinTest = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlinVersion}"
    val mockitoCore = "org.mockito:mockito-core:${Versions.mockitoVersion}"
    val mockitoAndroid = "org.mockito:mockito-android:${Versions.mockitoAndroidVersion}"
    val supportAnnotations = "androidx.annotation:annotation:${Versions.appCompatVersion}"
}

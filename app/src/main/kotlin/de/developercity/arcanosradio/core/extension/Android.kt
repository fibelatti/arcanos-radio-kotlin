package de.developercity.arcanosradio.core.extension

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.ConnectivityManager
import android.os.Build
import androidx.core.content.ContextCompat

// region SDK checks
fun inOreo(body: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) body()
}
// endregion

// region SystemService
inline fun <reified T> Context.getSystemService(): T? =
    ContextCompat.getSystemService(this, T::class.java)

fun ConnectivityManager?.isConnected(): Boolean = this?.activeNetworkInfo?.isConnected.orFalse()

@Suppress("DEPRECATION")
fun ConnectivityManager?.isConnectedToWifi(): Boolean =
    this?.activeNetworkInfo?.run { isConnected && type == ConnectivityManager.TYPE_WIFI }.orFalse()

fun AudioManager?.getMusicVolume(): Int = this?.getStreamVolume(AudioManager.STREAM_MUSIC).orZero()

fun AudioManager?.setMusicVolume(volume: Int) {
    this?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
}

fun AudioManager?.getMusicMaxVolume(): Int = this?.getStreamMaxVolume(AudioManager.STREAM_MUSIC).orZero()
// endregion

fun Context.getServicePendingIntent(requestCode: Int = 0, intent: Intent, flags: Int = 0): PendingIntent =
    PendingIntent.getService(this, requestCode, intent, flags)

fun Context.getActivityPendingIntent(requestCode: Int = 0, intent: Intent, flags: Int = 0): PendingIntent =
    PendingIntent.getActivity(this, requestCode, intent, flags)

package de.developercity.arcanosradio.core.extension

import android.content.Context
import android.media.AudioManager
import android.net.ConnectivityManager
import android.os.Build
import androidx.core.content.ContextCompat

// region SDK checks
fun inOreo(body: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) body()
}
// endregion

inline fun <reified T> Context.getSystemService(): T? =
    ContextCompat.getSystemService(this, T::class.java)

fun ConnectivityManager?.isConnected(): Boolean = this?.activeNetworkInfo?.isConnected.orFalse()

fun AudioManager?.getMusicVolume(): Int = this?.getStreamVolume(AudioManager.STREAM_MUSIC).orZero()

fun AudioManager?.setMusicVolume(volume: Int) {
    this?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
}

fun AudioManager?.getMusicMaxVolume(): Int = this?.getStreamMaxVolume(AudioManager.STREAM_MUSIC).orZero()

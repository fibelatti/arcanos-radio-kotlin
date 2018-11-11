package de.developercity.arcanosradio.core.extension

import android.content.Context
import android.media.AudioManager
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat

inline fun <reified T> Context.getSystemService(): T? =
    ContextCompat.getSystemService(this, T::class.java)

fun ConnectivityManager?.isConnected(): Boolean = this?.activeNetworkInfo?.isConnected.orFalse()

fun AudioManager?.getMusicVolume(): Int = this?.getStreamVolume(AudioManager.STREAM_MUSIC).orZero()

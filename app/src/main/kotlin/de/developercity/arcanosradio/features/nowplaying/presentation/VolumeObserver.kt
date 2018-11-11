package de.developercity.arcanosradio.features.nowplaying.presentation

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import de.developercity.arcanosradio.core.extension.getSystemService
import de.developercity.arcanosradio.core.extension.orZero

class VolumeObserver(context: Context, handler: Handler = Handler()) : ContentObserver(handler) {
    private val audioManager: AudioManager? = context.getSystemService<AudioManager>()

    var onVolumeChanged: ((Int) -> Unit)? = null

    override fun onChange(selfChange: Boolean) {
        onVolumeChanged?.invoke(audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC).orZero())
    }
}

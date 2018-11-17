package de.developercity.arcanosradio.features.streaming.device

import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import de.developercity.arcanosradio.core.extension.orZero
import de.developercity.arcanosradio.features.appstate.domain.usecase.UpdateStreamVolume
import javax.inject.Inject

class VolumeObserver @Inject constructor(
    private val audioManager: AudioManager?,
    private val updateStreamingVolume: UpdateStreamVolume
) : ContentObserver(Handler()) {

    override fun onChange(selfChange: Boolean) {
        if (!selfChange) {
            updateStreamingVolume(audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC).orZero())
        }
    }
}

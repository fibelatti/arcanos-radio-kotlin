package de.developercity.arcanosradio.features.streaming.device

import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import de.developercity.arcanosradio.core.extension.orZero
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamVolume
import javax.inject.Inject

class VolumeObserver @Inject constructor(
    private val audioManager: AudioManager?,
    private val appStateRepository: AppStateRepository
) : ContentObserver(Handler()) {

    override fun onChange(selfChange: Boolean) {
        if (!selfChange) {
            appStateRepository.dispatchAction(
                UpdateStreamVolume(audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC).orZero())
            )
        }
    }
}

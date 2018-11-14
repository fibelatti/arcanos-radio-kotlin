package de.developercity.arcanosradio.features.streaming

import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.AudioManager
import android.media.MediaPlayer
import de.developercity.arcanosradio.core.extension.getMusicVolume
import de.developercity.arcanosradio.core.provider.SchedulerProvider
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamState
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioStreamer @Inject constructor(
    private val audioManager: AudioManager?,
    private val appStateRepository: AppStateRepository,
    private val schedulerProvider: SchedulerProvider
) {

    private val mediaPlayer by lazy {
        MediaPlayer().apply {
            val volume = audioManager.getMusicVolume().toFloat()

            setVolume(volume, volume)
            setAudioAttributes(AudioAttributes.Builder().setContentType(CONTENT_TYPE_MUSIC).build())
            setOnPreparedListener {
                it.start()
                updateAppState(StreamingState.Playing)
            }
        }
    }

    private val disposables = CompositeDisposable()

    fun setup(streamingUrl: String) {
        observeAppState()
        mediaPlayer.run {
            setDataSource(streamingUrl)
            prepareAsync()
        }
        updateAppState(StreamingState.Buffering)
    }

    fun play() {
        mediaPlayer.prepareAsync()
        updateAppState(StreamingState.Buffering)
    }

    fun pause() {
        mediaPlayer.stop()
        updateAppState(StreamingState.Paused)
    }

    fun interrupt() {
        mediaPlayer.stop()
        updateAppState(StreamingState.Interrupted)
    }

    fun release() {
        mediaPlayer.run {
            reset()
            release()
        }
        disposables.clear()
    }

    private fun updateAppState(streamingState: StreamingState) {
        appStateRepository.updateState(UpdateStreamState(streamingState))
    }

    private fun observeAppState() {
        appStateRepository.getAppState()
            .subscribeOn(schedulerProvider.io())
            .subscribe { state ->
                mediaPlayer.setVolume(state.streamVolume)
            }
            .let(disposables::add)
    }

    private fun MediaPlayer.setVolume(volume: Int) {
        setVolume(volume.toFloat(), volume.toFloat())
    }
}

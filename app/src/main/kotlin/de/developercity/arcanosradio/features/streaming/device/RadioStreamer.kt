package de.developercity.arcanosradio.features.streaming.device

import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.MediaPlayer
import de.developercity.arcanosradio.features.appstate.domain.AppState
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.DefaultAppStateObserver
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamState
import de.developercity.arcanosradio.features.streaming.domain.NetworkState
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioStreamer @Inject constructor(
    private val appStateRepository: AppStateRepository
) {

    private val mediaPlayer by lazy {
        MediaPlayer().apply {
            setAudioAttributes(AudioAttributes.Builder().setContentType(CONTENT_TYPE_MUSIC).build())
            setOnPreparedListener {
                if (shouldStopAsync) {
                    it.stop()
                } else {
                    it.start()
                    appStateRepository.dispatchAction(UpdateStreamState(StreamingState.Playing))
                }
            }
        }
    }

    private val disposables = CompositeDisposable()

    private var shouldStopAsync: Boolean = false

    fun setup() {
        appStateRepository.addSideEffect(object : Observer<AppState> by DefaultAppStateObserver {
            override fun onSubscribe(disposable: Disposable) {
                disposables.add(disposable)
            }

            override fun onNext(state: AppState) {
                when (state.streamState) {
                    is StreamingState.NotInitialized,
                    is StreamingState.ShouldStart,
                    is StreamingState.Interrupted -> {
                        if (state.streamingUrl.isNotEmpty() && state.networkState is NetworkState.Connected) {
                            mediaPlayer.tryToPrepareAsync(state.streamingUrl)
                            appStateRepository.dispatchAction(UpdateStreamState(StreamingState.Buffering))
                        }
                    }
                    is StreamingState.Buffering,
                    is StreamingState.Playing -> {
                        if (state.networkState is NetworkState.NotConnected) {
                            mediaPlayer.tryToStop()
                            appStateRepository.dispatchAction(UpdateStreamState(StreamingState.Interrupted))
                        }
                    }
                    is StreamingState.ShouldPause -> {
                        mediaPlayer.tryToStop()
                        appStateRepository.dispatchAction(UpdateStreamState(StreamingState.Paused))
                    }
                    is StreamingState.ShouldTerminate -> {
                        disposables.clear()
                        mediaPlayer.release()
                        appStateRepository.dispatchAction(UpdateStreamState(StreamingState.NotInitialized))
                    }
                }

                if (state.streamState is StreamingState.Playing) mediaPlayer.setVolume(state.streamVolume)
            }
        })
    }

    private fun MediaPlayer.setVolume(volume: Int) {
        setVolume(volume.toFloat(), volume.toFloat())
    }

    private fun MediaPlayer.tryToPrepareAsync(streamingUrl: String) {
        try {
            shouldStopAsync = false
            setDataSource(streamingUrl)
            prepareAsync()
            appStateRepository.dispatchAction(UpdateStreamState(StreamingState.Buffering))
        } catch (ignore: IllegalStateException) {
        }
    }

    private fun MediaPlayer.tryToStop() {
        try {
            pause()
            reset()
        } catch (ignore: IllegalStateException) {
            shouldStopAsync = true
        }
    }
}

package de.developercity.arcanosradio.features.streaming.device

import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.MediaPlayer
import de.developercity.arcanosradio.features.appstate.domain.AppState
import de.developercity.arcanosradio.features.appstate.domain.DefaultAppStateObserver
import de.developercity.arcanosradio.features.appstate.domain.usecase.AddSideEffect
import de.developercity.arcanosradio.features.appstate.domain.usecase.UpdateStreamState
import de.developercity.arcanosradio.features.streaming.domain.NetworkState
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioStreamer @Inject constructor(
    private val addSideEffect: AddSideEffect,
    private val updateStreamState: UpdateStreamState
) {

    private val mediaPlayer by lazy {
        MediaPlayer().apply {
            setAudioAttributes(AudioAttributes.Builder().setContentType(CONTENT_TYPE_MUSIC).build())
            setOnPreparedListener { mp ->
                if (shouldStopAsync) {
                    mp.stop()
                } else {
                    mp.start()
                    updateStreamState(StreamingState.Playing)
                }
            }

            /*
             * Called when the network has changed. The previous connection stream will complete
             * and a new one should be started.
             *
             */
            setOnCompletionListener {
                tryToStop()
                updateStreamState(StreamingState.ShouldStart)
            }
        }
    }

    private val disposables = CompositeDisposable()

    private var shouldStopAsync: Boolean = false

    fun setup() {
        addSideEffect(object : Observer<AppState> by DefaultAppStateObserver {
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
                            updateStreamState(StreamingState.Buffering)
                        }
                    }
                    is StreamingState.Buffering,
                    is StreamingState.Playing -> {
                        if (state.networkState is NetworkState.NotConnected) {
                            mediaPlayer.tryToStop()
                            updateStreamState(StreamingState.Interrupted)
                        }
                    }
                    is StreamingState.ShouldPause -> {
                        mediaPlayer.tryToStop()
                        updateStreamState(StreamingState.Paused)
                    }
                    is StreamingState.ShouldTerminate -> {
                        disposables.clear()
                        mediaPlayer.release()
                        updateStreamState(StreamingState.NotInitialized)
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
            updateStreamState(StreamingState.Buffering)
        } catch (ignore: IllegalStateException) {
        }
    }

    private fun MediaPlayer.tryToStop() {
        try {
            stop()
            reset()
        } catch (ignore: IllegalStateException) {
            shouldStopAsync = true
        }
    }
}

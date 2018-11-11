package de.developercity.arcanosradio.features.streaming

import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import de.developercity.arcanosradio.core.extension.getMusicVolume
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

private const val BUFFER_DELAY = 500L

@Singleton
class RadioStreamer @Inject constructor(private val audioManager: AudioManager?) {

    private val observableState: BehaviorSubject<StreamingState> = BehaviorSubject.create()
    private var streamingUrl: String = ""

    private val mediaPlayer by lazy {
        MediaPlayer().apply {
            val volume = audioManager.getMusicVolume().toFloat()

            setAudioAttributes(AudioAttributes.Builder().setContentType(CONTENT_TYPE_MUSIC).build())
            setOnPreparedListener { it.start() }
            setVolume(volume, volume)
        }
    }

    private val handler = Handler()
    private val bufferCheck = object : Runnable {
        override fun run() {
            if (mediaPlayer.currentPosition > 0) {
                observableState.onNext(StreamingState.Playing)
                handler.removeCallbacks(this)
            } else {
                observableState.onNext(StreamingState.Buffering)
                handler.postDelayed(this, BUFFER_DELAY)
            }
        }
    }

    fun getState() = observableState

    fun setStreamingUrl(url: String) {
        streamingUrl = url
    }

    fun play() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.run {
                reset()
                setDataSource(streamingUrl)
                prepareAsync()
            }
            observableState.onNext(StreamingState.Buffering)
            handler.postDelayed(bufferCheck, BUFFER_DELAY)
        } else {
            observableState.onNext(StreamingState.Playing)
        }
    }

    fun pause() {
        mediaPlayer.pause()
        observableState.onNext(StreamingState.Paused)
    }

    fun interrupt() {
        mediaPlayer.pause()
        observableState.onNext(StreamingState.Interrupted)
    }

    fun release() {
        mediaPlayer.run {
            reset()
            release()
        }
        observableState.onComplete()
        handler.removeCallbacks(bufferCheck)
    }

    fun setVolume(volume: Float) {
        mediaPlayer.setVolume(volume, volume)
    }
}

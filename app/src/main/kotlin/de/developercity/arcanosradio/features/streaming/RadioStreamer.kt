package de.developercity.arcanosradio.features.streaming

import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

private const val BUFFER_DELAY = 500L

@Singleton
class RadioStreamer @Inject constructor(private val audioManager: AudioManager?) {

    private val state: BehaviorSubject<RadioStreamState> = BehaviorSubject.create()
    private var streamingUrl: String = ""

    private val mediaPlayer by lazy {
        MediaPlayer().apply {
            val volume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 0f

            setAudioAttributes(AudioAttributes.Builder().setContentType(CONTENT_TYPE_MUSIC).build())
            setOnPreparedListener { it.start() }
            setVolume(volume, volume)
        }
    }

    private val handler = Handler()
    private val bufferCheck = object : Runnable {
        override fun run() {
            if (mediaPlayer.currentPosition > 0) {
                state.onNext(RadioStreamState.Playing)
                handler.removeCallbacks(this)
            } else {
                state.onNext(RadioStreamState.Buffering)
                handler.postDelayed(this, BUFFER_DELAY)
            }
        }
    }

    fun getState() = state

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
            state.onNext(RadioStreamState.Buffering)
            handler.postDelayed(bufferCheck, BUFFER_DELAY)
        } else {
            state.onNext(RadioStreamState.Playing)
        }
    }

    fun pause() {
        mediaPlayer.pause()
        state.onNext(RadioStreamState.Paused)
    }

    fun release() {
        mediaPlayer.run {
            reset()
            release()
        }
        state.onComplete()
        handler.removeCallbacks(bufferCheck)
    }

    fun setVolume(volume: Float) {
        mediaPlayer.setVolume(volume, volume)
    }
}

sealed class RadioStreamState {
    object Buffering : RadioStreamState()
    object Playing : RadioStreamState()
    object Paused : RadioStreamState()
}

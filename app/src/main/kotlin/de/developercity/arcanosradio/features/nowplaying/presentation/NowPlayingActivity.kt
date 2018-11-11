package de.developercity.arcanosradio.features.nowplaying.presentation

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings.System.CONTENT_URI
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintSet
import de.developercity.arcanosradio.R
import de.developercity.arcanosradio.core.extension.animateChangingTransitions
import de.developercity.arcanosradio.core.extension.getMusicMaxVolume
import de.developercity.arcanosradio.core.extension.getMusicVolume
import de.developercity.arcanosradio.core.extension.getSystemService
import de.developercity.arcanosradio.core.extension.load
import de.developercity.arcanosradio.core.extension.setMusicVolume
import de.developercity.arcanosradio.core.platform.base.BaseActivity
import de.developercity.arcanosradio.core.platform.base.BaseIntentBuilder
import de.developercity.arcanosradio.features.streaming.domain.models.NowPlaying
import kotlinx.android.synthetic.main.activity_now_playing.*
import javax.inject.Inject

class NowPlayingActivity : BaseActivity(), NowPlayingPresenter.View {

    @Inject
    lateinit var nowPlayingPresenter: NowPlayingPresenter

    private val volumeObserver by lazy { VolumeObserver(this@NowPlayingActivity) }
    private val defaultConstraintSet = ConstraintSet()
    private var lyricsVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        setupLayout()

        injector.inject(this)

        nowPlayingPresenter.attachView(this)
        nowPlayingPresenter.setup()
    }

    private fun setupLayout() {
        setContentView(R.layout.activity_now_playing)
        layoutRoot.animateChangingTransitions()
        defaultConstraintSet.clone(layoutRoot)

        buttonPlayControl.startAnimation(AnimationUtils.loadAnimation(this, R.anim.expand))
        buttonLyrics.setOnClickListener { toggleLyrics() }
        setupVolumeControls()
    }

    private fun setupVolumeControls() {
        val audioManager = getSystemService<AudioManager>()

        contentResolver.registerContentObserver(CONTENT_URI, true, volumeObserver)
        volumeObserver.onVolumeChanged = {
            nowPlayingPresenter.setVolume(it.toFloat())
            seekVolume.progress = it
        }

        seekVolume.run {
            max = audioManager.getMusicMaxVolume()
            progress = audioManager.getMusicVolume()

            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    audioManager.setMusicVolume(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun toggleLyrics() {
        val constraintSet = if (lyricsVisible) {
            defaultConstraintSet
        } else {
            ConstraintSet().apply {
                clone(this@NowPlayingActivity, R.layout.activity_now_playing_with_lyrics)
            }
        }

        constraintSet.applyTo(layoutRoot)

        lyricsVisible = !lyricsVisible

        buttonLyrics.setText(if (lyricsVisible) R.string.now_playing_hide_lyrics else R.string.now_playing_show_lyrics)
    }

    override fun onDestroy() {
        super.onDestroy()
        nowPlayingPresenter.detachView()
        volumeObserver.onVolumeChanged = null
        contentResolver.unregisterContentObserver(volumeObserver)
    }

    override fun readyToPlay() {
        setupButtonPlayControl(R.string.now_playing_play, R.drawable.ic_play) {
            nowPlayingPresenter.play()
        }
    }

    override fun buffering() {
        setupButtonPlayControl(R.string.now_playing_buffering, R.drawable.ic_loading) {
            nowPlayingPresenter.pause()
        }
    }

    override fun playing() {
        setupButtonPlayControl(R.string.now_playing_stop, R.drawable.ic_stop) {
            nowPlayingPresenter.pause()
        }
    }

    override fun paused() {
        setupButtonPlayControl(R.string.now_playing_play, R.drawable.ic_play) {
            nowPlayingPresenter.play()
        }
    }

    private inline fun setupButtonPlayControl(
        @StringRes text: Int,
        @DrawableRes icon: Int,
        crossinline clickListener: () -> Unit
    ) {
        buttonPlayControl.run {
            setText(text)
            setIconResource(icon)
            setOnClickListener { clickListener() }
        }
    }

    override fun showSongMetadata(nowPlaying: NowPlaying) {
        with(nowPlaying.song) {
            imageViewAlbumArt.load(albumArt, R.drawable.arcanos)
            textViewSong.text = songName
            textViewArtist.text = artist.artistName
            textViewLyrics.text = lyrics
        }
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, NowPlayingActivity::class.java)
}

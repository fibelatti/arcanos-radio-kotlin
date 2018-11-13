package de.developercity.arcanosradio.features.nowplaying.presentation

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.System.CONTENT_URI
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import de.developercity.arcanosradio.R
import de.developercity.arcanosradio.core.extension.animateChangingTransitions
import de.developercity.arcanosradio.core.extension.getMusicMaxVolume
import de.developercity.arcanosradio.core.extension.getMusicVolume
import de.developercity.arcanosradio.core.extension.getSystemService
import de.developercity.arcanosradio.core.extension.gone
import de.developercity.arcanosradio.core.extension.load
import de.developercity.arcanosradio.core.extension.setMusicVolume
import de.developercity.arcanosradio.core.extension.visible
import de.developercity.arcanosradio.core.platform.base.BaseActivity
import de.developercity.arcanosradio.core.platform.base.BaseIntentBuilder
import de.developercity.arcanosradio.features.streaming.StreamingService
import de.developercity.arcanosradio.features.streaming.domain.models.NowPlaying
import de.developercity.arcanosradio.features.streaming.domain.models.Song
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

    override fun streamerReady() {
        ContextCompat.startForegroundService(this, Intent(this, StreamingService::class.java))
    }

    override fun readyToPlay() {
        setupButtonPlayControl(R.string.now_playing_play, R.drawable.ic_play) {
            nowPlayingPresenter.play()
        }
    }

    override fun buffering() {
        setupButtonPlayControl(R.string.now_playing_buffering, R.drawable.ic_buffering) {
            nowPlayingPresenter.pause()
        }
    }

    override fun playing() {
        setupButtonPlayControl(R.string.now_playing_pause, R.drawable.ic_pause) {
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

    override fun showSongMetadata(nowPlaying: NowPlaying, shareUrl: String) {
        with(nowPlaying.song) {
            imageViewAlbumArt.load(albumArt, R.drawable.arcanos)
            textViewSong.text = name
            textViewArtist.text = artist.name
            textViewLyrics.text = lyrics

            setupOpenInBrowser(artist.url)
            setupShare(nowPlaying.song, shareUrl)
        }
    }

    private fun setupOpenInBrowser(artistUrl: String) {
        if (artistUrl.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(artistUrl)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            buttonOpenInBrowser.visible()
            buttonOpenInBrowser.setOnClickListener { startActivity(intent) }
        } else {
            buttonOpenInBrowser.gone()
        }
    }

    private fun setupShare(song: Song, shareUrl: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                getString(
                    R.string.now_playing_share_message,
                    song.name,
                    song.artist.name,
                    shareUrl
                )
            )
        }

        val chooser = Intent.createChooser(intent, getString(R.string.now_playing_share_title))
            .apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

        buttonShare.setOnClickListener { startActivity(chooser) }
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, NowPlayingActivity::class.java)
}

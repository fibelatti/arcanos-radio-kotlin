package de.developercity.arcanosradio.features.streaming

import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import de.developercity.arcanosradio.R
import de.developercity.arcanosradio.core.platform.base.BaseService
import de.developercity.arcanosradio.core.provider.SchedulerProvider
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateNowPlaying
import de.developercity.arcanosradio.features.streaming.domain.StreamingRepository
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val POLL_INTERVAL = 10L

private const val ACTION_PLAY = "ACTION_PLAY"
private const val ACTION_PAUSE = "ACTION_PAUSE"

class StreamingService : BaseService() {

    @Inject
    lateinit var schedulerProvider: SchedulerProvider
    @Inject
    lateinit var streamingRepository: StreamingRepository
    @Inject
    lateinit var appStateRepository: AppStateRepository
    @Inject
    lateinit var streamingNotificationManager: StreamingNotificationManager
    @Inject
    lateinit var radioStreamer: RadioStreamer

    private val disposables = CompositeDisposable()

    private val defaultAlbumArt by lazy { BitmapFactory.decodeResource(resources, R.drawable.arcanos) }

    // region Intent
    private val playIntent by lazy {
        PendingIntent.getService(
            this,
            1,
            Intent(this, StreamingService::class.java).apply { action = ACTION_PLAY },
            0
        )
    }
    private val pauseIntent by lazy {
        PendingIntent.getService(
            this,
            1,
            Intent(this, StreamingService::class.java).apply { action = ACTION_PAUSE },
            0
        )
    }
    // endregion

    override fun onCreate() {
        super.onCreate()
        injector.inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleIntent(intent)
        startNotification()
        pollMetadata()
        observeAppState()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action?.toUpperCase()) {
            ACTION_PLAY -> radioStreamer.play()
            ACTION_PAUSE -> radioStreamer.pause()
        }
    }

    private fun startNotification() {
        startForeground(
            streamingNotificationManager.getNotificationId(),
            streamingNotificationManager.createNotification(
                song = getString(R.string.now_playing_default_title),
                artist = getString(R.string.now_playing_default_subtitle),
                albumArt = defaultAlbumArt,
                actionIcon = R.drawable.ic_loading,
                actionDescription = R.string.now_playing_buffering,
                actionPendingIntent = pauseIntent,
                cancelPendingIntent = pauseIntent
            )
        )
    }

    private fun pollMetadata() {
        Observable.interval(0, POLL_INTERVAL, TimeUnit.SECONDS, schedulerProvider.io())
            .flatMap {
                streamingRepository.getCurrentSongMetadata()
                    .toObservable()
                    .onErrorResumeNext(Observable.empty())
            }
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.io())
            .subscribe { appStateRepository.updateState(UpdateNowPlaying(it)) }
            .let(disposables::add)
    }

    private fun observeAppState() {
        appStateRepository.getAppState()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.main())
            .subscribe { state ->
                when (state.streamState) {
                    StreamingState.Paused -> {
                        disposables.clear()
                        streamingNotificationManager.showNotification(
                            song = getString(R.string.now_playing_default_title),
                            artist = getString(R.string.now_playing_default_subtitle),
                            albumArt = defaultAlbumArt,
                            actionIcon = R.drawable.ic_play,
                            actionDescription = R.string.now_playing_play,
                            actionPendingIntent = playIntent,
                            cancelPendingIntent = pauseIntent
                        )
                    }
                    else -> state.nowPlaying?.let {
                        val (icon, description, intent) = when (state.streamState) {
                            StreamingState.Playing -> Triple(R.drawable.ic_stop, R.string.now_playing_stop, pauseIntent)
                            StreamingState.Paused -> Triple(R.drawable.ic_play, R.string.now_playing_play, playIntent)
                            else -> Triple(R.drawable.ic_loading, R.string.now_playing_buffering, pauseIntent)
                        }

                        streamingNotificationManager.showNotification(
                            song = getString(R.string.now_playing_default_title),
                            artist = getString(R.string.now_playing_default_subtitle),
                            albumArt = defaultAlbumArt,
                            actionIcon = icon,
                            actionDescription = description,
                            actionPendingIntent = intent,
                            cancelPendingIntent = pauseIntent
                        )
                    }
                }
            }
            .let(disposables::add)
    }
}

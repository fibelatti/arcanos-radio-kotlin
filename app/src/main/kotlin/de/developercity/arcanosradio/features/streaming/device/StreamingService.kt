package de.developercity.arcanosradio.features.streaming.device

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import androidx.media.session.MediaButtonReceiver
import de.developercity.arcanosradio.R
import de.developercity.arcanosradio.core.extension.getActivityPendingIntent
import de.developercity.arcanosradio.core.extension.getServicePendingIntent
import de.developercity.arcanosradio.core.platform.base.BaseIntentBuilder
import de.developercity.arcanosradio.core.platform.base.BaseService
import de.developercity.arcanosradio.core.provider.SchedulerProvider
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateNowPlaying
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamState
import de.developercity.arcanosradio.features.nowplaying.presentation.NowPlayingActivity
import de.developercity.arcanosradio.features.streaming.domain.StreamingRepository
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val POLL_INTERVAL = 10L
private const val MEDIA_SESSION_TAG = "ARCANOS_MEDIA_SESSION_TAG"

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

    private val mediaSession: MediaSessionCompat by lazy { MediaSessionCompat(this, MEDIA_SESSION_TAG) }

    // region Intent
    private val playIntent by lazy {
        getServicePendingIntent(intent = IntentBuilder(this, Action.ACTION_PLAY).build())
    }
    private val pauseIntent by lazy {
        getServicePendingIntent(intent = IntentBuilder(this, Action.ACTION_PAUSE).build())
    }
    // endregion

    private var mediaButtonCallback: () -> Unit = {}

    override fun onCreate() {
        super.onCreate()
        injector.inject(this)

        setMediaSessionCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != Action.ACTION_DELETE.value) startNotification()

        handleIntent(intent)
        pollMetadata()
        observeAppState()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun handleIntent(intent: Intent?) {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        when (intent?.action) {
            Action.ACTION_PLAY.value -> radioStreamer.play()
            Action.ACTION_PAUSE.value -> radioStreamer.pause()
            Action.ACTION_DELETE.value -> tearDown()
        }
    }

    private fun setMediaSessionCallback() {
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                return when (mediaButtonEvent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)?.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY -> {
                        mediaButtonCallback()
                        return true
                    }
                    else -> false
                }
            }
        })
    }

    private fun startNotification() {
        startForeground(
            streamingNotificationManager.getNotificationId(),
            streamingNotificationManager.createNotification(
                song = getString(R.string.now_playing_default_title),
                artist = getString(R.string.now_playing_default_subtitle),
                albumArt = defaultAlbumArt,
                actionIcon = R.drawable.ic_notification_buffering,
                actionDescription = R.string.now_playing_buffering,
                actionPendingIntent = pauseIntent,
                tapIntent = getActivityPendingIntent(intent = NowPlayingActivity.IntentBuilder(this).build()),
                deleteIntent = getServicePendingIntent(intent = IntentBuilder(this, Action.ACTION_DELETE).build()),
                mediaSessionToken = mediaSession.sessionToken
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
                        mediaButtonCallback = { radioStreamer.play() }

                        streamingNotificationManager.showNotification(
                            song = getString(R.string.now_playing_default_title),
                            artist = getString(R.string.now_playing_default_subtitle),
                            albumArt = defaultAlbumArt,
                            actionIcon = R.drawable.ic_notification_play,
                            actionDescription = R.string.now_playing_play,
                            actionPendingIntent = playIntent
                        )
                        stopForeground(false)
                        disposables.clear()
                    }
                    else -> state.nowPlaying?.let {
                        val (icon, description, intent) = when (state.streamState) {
                            StreamingState.Playing -> Triple(R.drawable.ic_notification_pause, R.string.now_playing_pause, pauseIntent)
                            StreamingState.Paused -> Triple(R.drawable.ic_notification_play, R.string.now_playing_play, playIntent)
                            else -> Triple(R.drawable.ic_notification_buffering, R.string.now_playing_buffering, pauseIntent)
                        }

                        mediaButtonCallback = { radioStreamer.pause() }

                        streamingNotificationManager.showNowPlayingNotification(
                            nowPlaying = state.nowPlaying,
                            defaultAlbumArt = defaultAlbumArt,
                            actionIcon = icon,
                            actionDescription = description,
                            actionPendingIntent = intent
                        )
                    }
                }
            }
            .let(disposables::add)
    }

    private fun tearDown() {
        stopSelf()
        appStateRepository.updateState(UpdateStreamState(StreamingState.NotInitialized))
        radioStreamer.release()
        disposables.clear()
    }

    enum class Action(val value: String) {
        ACTION_PLAY("ACTION_PLAY"),
        ACTION_PAUSE("ACTION_PAUSE"),
        ACTION_DELETE("ACTION_DELETE")
    }

    class IntentBuilder(context: Context, action: Action) : BaseIntentBuilder(context, StreamingService::class.java) {
        init {
            intent.action = action.value
        }
    }
}

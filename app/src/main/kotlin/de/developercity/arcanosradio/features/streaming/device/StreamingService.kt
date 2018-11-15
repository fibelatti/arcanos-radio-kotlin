package de.developercity.arcanosradio.features.streaming.device

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import de.developercity.arcanosradio.features.streaming.domain.models.NowPlaying
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
    lateinit var appStateRepository: AppStateRepository
    @Inject
    lateinit var streamingRepository: StreamingRepository
    @Inject
    lateinit var streamingNotificationManager: StreamingNotificationManager
    @Inject
    lateinit var radioStreamer: RadioStreamer

    private val disposables by lazy { CompositeDisposable() }
    private val mediaSession: MediaSessionCompat by lazy { MediaSessionCompat(this, MEDIA_SESSION_TAG) }
    private val defaultAlbumArt by lazy { BitmapFactory.decodeResource(resources, R.drawable.arcanos) }
    private val defaultTitle by lazy { getString(R.string.now_playing_default_title) }
    private val defaultSubTitle by lazy { getString(R.string.now_playing_default_subtitle) }

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

        radioStreamer.setup()

        observeAppState()
        pollMetadata()
        setMediaSessionCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Action.ACTION_PLAY.value -> appStateRepository.dispatchAction(UpdateStreamState(StreamingState.ShouldStart))
            Action.ACTION_PAUSE.value -> appStateRepository.dispatchAction(UpdateStreamState(StreamingState.ShouldPause))
            Action.ACTION_DELETE.value -> appStateRepository.dispatchAction(UpdateStreamState(StreamingState.ShouldTerminate))
            else -> startNotification()
        }

        MediaButtonReceiver.handleIntent(mediaSession, intent)

        return super.onStartCommand(intent, flags, startId)
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
                song = defaultTitle,
                artist = defaultSubTitle,
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
            .subscribe { appStateRepository.dispatchAction(UpdateNowPlaying(it)) }
            .let(disposables::add)
    }

    private fun observeAppState() {
        appStateRepository.getAppState()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.main())
            .subscribe { state ->
                when (state.streamState) {
                    StreamingState.Buffering -> {
                        mediaButtonCallback = { appStateRepository.dispatchAction(UpdateStreamState(StreamingState.ShouldPause)) }
                        showNowPlayingNotification(state.nowPlaying, R.drawable.ic_notification_buffering, R.string.now_playing_buffering, pauseIntent)
                    }
                    StreamingState.Playing -> {
                        mediaButtonCallback = { appStateRepository.dispatchAction(UpdateStreamState(StreamingState.ShouldPause)) }
                        showNowPlayingNotification(state.nowPlaying, R.drawable.ic_notification_pause, R.string.now_playing_pause, pauseIntent)
                    }
                    StreamingState.Paused -> {
                        mediaButtonCallback = { appStateRepository.dispatchAction(UpdateStreamState(StreamingState.ShouldStart)) }
                        showNowPlayingNotification(
                            nowPlaying = null,
                            actionIcon = R.drawable.ic_notification_play,
                            actionDescription = R.string.now_playing_play,
                            actionPendingIntent = playIntent
                        )
                        stopForeground(false)
                    }
                    StreamingState.Interrupted -> {
                        // TODO
                    }
                    StreamingState.ShouldTerminate -> {
                        disposables.dispose()
                        stopSelf()
                    }
                }
            }
            .let(disposables::add)
    }

    private fun showNowPlayingNotification(
        nowPlaying: NowPlaying? = null,
        @DrawableRes actionIcon: Int,
        @StringRes actionDescription: Int,
        actionPendingIntent: PendingIntent
    ) {
        streamingNotificationManager.showNowPlayingNotification(
            song = nowPlaying?.song?.name ?: defaultTitle,
            artist = nowPlaying?.song?.artist?.name ?: defaultSubTitle,
            albumArtUrl = nowPlaying?.song?.albumArt,
            defaultAlbumArt = defaultAlbumArt,
            actionIcon = actionIcon,
            actionDescription = actionDescription,
            actionPendingIntent = actionPendingIntent
        )
    }

    enum class Action(val value: String) {
        ACTION_PLAY("ACTION_PLAY"),
        ACTION_PAUSE("ACTION_PAUSE"),
        ACTION_DELETE("ACTION_DELETE")
    }

    class IntentBuilder(context: Context, action: Action? = null) : BaseIntentBuilder(context, StreamingService::class.java) {
        init {
            action?.let { intent.action = it.value }
        }
    }
}

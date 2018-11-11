package de.developercity.arcanosradio.features.nowplaying.presentation

import android.util.Log
import de.developercity.arcanosradio.core.platform.TAG
import de.developercity.arcanosradio.core.platform.base.BaseContract
import de.developercity.arcanosradio.core.platform.base.BasePresenter
import de.developercity.arcanosradio.core.provider.SchedulerProvider
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateNowPlaying
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamState
import de.developercity.arcanosradio.features.streaming.RadioStreamer
import de.developercity.arcanosradio.features.streaming.domain.StreamingRepository
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import de.developercity.arcanosradio.features.streaming.domain.models.NowPlaying
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val POLL_INTERVAL = 10L

class NowPlayingPresenter @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val appStateRepository: AppStateRepository,
    private val streamingRepository: StreamingRepository,
    private val radioStreamer: RadioStreamer
) : BasePresenter<NowPlayingPresenter.View>(schedulerProvider) {

    interface View : BaseContract.View {
        fun readyToPlay()

        fun buffering()

        fun playing()

        fun paused()

        fun showSongMetadata(nowPlaying: NowPlaying)
    }

    override fun detachView() {
        super.detachView()
        radioStreamer.release()
    }

    fun setup() {
        view?.buffering()
        observeAppState()
        observeStreamerState()
        setupSongMetadataPolling()
    }

    fun play() {
        radioStreamer.play()
    }

    fun pause() {
        radioStreamer.pause()
    }

    fun setVolume(volume: Float) {
        radioStreamer.setVolume(volume)
    }

    private fun observeAppState() {
        appStateRepository.getAppState()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.main())
            .subscribe(
                { appState ->
                    Log.d(TAG, "$TAG - $appState")

                    when (appState.streamState) {
                        StreamingState.NotInitialized -> {
                            radioStreamer.run {
                                setStreamingUrl(appState.streamingUrl)
                                play()
                            }
                        }
                        StreamingState.Interrupted -> radioStreamer.play()
                    }

                    if (!appState.networkAvailable) {
                        radioStreamer.interrupt()
                    }

                    when (appState.streamState) {
                        StreamingState.Buffering -> view?.buffering()
                        StreamingState.Playing -> view?.playing()
                        StreamingState.Paused -> view?.readyToPlay()
                    }

                    appState.nowPlaying?.let { view?.showSongMetadata(it) }
                },
                { view?.handleError(it) }
            )
            .disposeOnDetach()
    }

    private fun observeStreamerState() {
        radioStreamer.getState()
            .subscribeOn(schedulerProvider.io())
            .subscribe { appStateRepository.updateState(UpdateStreamState(it)) }
            .disposeOnDetach()
    }

    private fun setupSongMetadataPolling() {
        Observable.interval(0, POLL_INTERVAL, TimeUnit.SECONDS, schedulerProvider.io())
            .flatMap {
                streamingRepository.getCurrentSongMetadata()
                    .toObservable()
                    .onErrorResumeNext(Observable.empty())
            }
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.io())
            .subscribe { appStateRepository.updateState(UpdateNowPlaying(it)) }
            .disposeOnDetach()
    }
}

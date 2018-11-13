package de.developercity.arcanosradio.features.nowplaying.presentation

import android.util.Log
import de.developercity.arcanosradio.core.platform.TAG
import de.developercity.arcanosradio.core.platform.base.BaseContract
import de.developercity.arcanosradio.core.platform.base.BasePresenter
import de.developercity.arcanosradio.core.provider.SchedulerProvider
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.streaming.RadioStreamer
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import de.developercity.arcanosradio.features.streaming.domain.models.NowPlaying
import javax.inject.Inject

class NowPlayingPresenter @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val appStateRepository: AppStateRepository,
    private val radioStreamer: RadioStreamer
) : BasePresenter<NowPlayingPresenter.View>(schedulerProvider) {

    interface View : BaseContract.View {
        fun streamerReady()

        fun readyToPlay()

        fun buffering()

        fun playing()

        fun paused()

        fun showSongMetadata(nowPlaying: NowPlaying, shareUrl: String)
    }

    override fun detachView() {
        super.detachView()
        radioStreamer.release()
    }

    fun setup() {
        view?.buffering()
        observeAppState()
    }

    fun play() {
        view?.streamerReady()
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
                            view?.streamerReady()
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

                    appState.nowPlaying?.let { view?.showSongMetadata(it, appState.shareUrl) }
                },
                { view?.handleError(it) }
            )
            .disposeOnDetach()
    }
}

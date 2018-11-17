package de.developercity.arcanosradio.features.nowplaying.presentation

import de.developercity.arcanosradio.core.persistence.CurrentInstallSharedPreferences
import de.developercity.arcanosradio.core.platform.base.BaseContract
import de.developercity.arcanosradio.core.platform.base.BasePresenter
import de.developercity.arcanosradio.core.provider.SchedulerProvider
import de.developercity.arcanosradio.features.appstate.domain.usecase.GetAppState
import de.developercity.arcanosradio.features.appstate.domain.usecase.UpdateStreamState
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import de.developercity.arcanosradio.features.streaming.domain.models.NowPlaying
import javax.inject.Inject

class NowPlayingPresenter @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val getAppState: GetAppState,
    private val updateStreamState: UpdateStreamState,
    private val currentInstallSharedPreferences: CurrentInstallSharedPreferences
) : BasePresenter<NowPlayingPresenter.View>(schedulerProvider) {

    interface View : BaseContract.View {
        fun showBuffering()

        fun showPlaying()

        fun showIdle()

        fun showNetworkNotAvailable()

        fun updateSongMetadata(nowPlaying: NowPlaying, shareUrl: String)

        fun updateVolumeSeeker(volume: Int)

        fun showPreferencesManager(streamingOverMobileDataEnabled: Boolean)
    }

    fun setup() {
        getAppState()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.main())
            .subscribe { appState ->
                when (appState.streamState) {
                    StreamingState.Buffering -> {
                        appState.nowPlaying?.let { view?.updateSongMetadata(it, appState.shareUrl) }
                        view?.showBuffering()
                    }
                    StreamingState.Playing -> {
                        appState.nowPlaying?.let { view?.updateSongMetadata(it, appState.shareUrl) }
                        view?.showPlaying()
                    }
                    StreamingState.Paused,
                    StreamingState.NotInitialized -> view?.showIdle()
                    StreamingState.Interrupted -> view?.showNetworkNotAvailable()
                }

                view?.updateVolumeSeeker(appState.streamVolume)
            }
            .disposeOnDetach()
    }

    fun play() {
        updateStreamState(StreamingState.ShouldStart)
    }

    fun pause() {
        updateStreamState(StreamingState.ShouldPause)
    }

    fun getPreferences() {
        view?.showPreferencesManager(
            streamingOverMobileDataEnabled = currentInstallSharedPreferences.getStreamingOverMobileDataEnabled()
        )
    }

    fun setStreamingOverMobileDataEnabled(value: Boolean) {
        currentInstallSharedPreferences.setStreamingOverMobileDataEnabled(value)
    }
}

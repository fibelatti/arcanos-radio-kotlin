package de.developercity.arcanosradio.features.nowplaying.presentation

import de.developercity.arcanosradio.core.persistence.CurrentInstallSharedPreferences
import de.developercity.arcanosradio.core.platform.base.BaseContract
import de.developercity.arcanosradio.core.platform.base.BasePresenter
import de.developercity.arcanosradio.core.provider.SchedulerProvider
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamState
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import de.developercity.arcanosradio.features.streaming.domain.models.NowPlaying
import javax.inject.Inject

class NowPlayingPresenter @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val appStateRepository: AppStateRepository,
    private val currentInstallSharedPreferences: CurrentInstallSharedPreferences
) : BasePresenter<NowPlayingPresenter.View>(schedulerProvider) {

    interface View : BaseContract.View {
        fun buffering()

        fun playing()

        fun idle()

        fun showNetworkNotAvailable()

        fun showSongMetadata(nowPlaying: NowPlaying, shareUrl: String)

        fun updateVolumeSeeker(volume: Int)
    }

    fun setup() {
        appStateRepository.getAppState()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.main())
            .subscribe { appState ->
                when (appState.streamState) {
                    StreamingState.Buffering -> {
                        appState.nowPlaying?.let { view?.showSongMetadata(it, appState.shareUrl) }
                        view?.buffering()
                    }
                    StreamingState.Playing -> {
                        appState.nowPlaying?.let { view?.showSongMetadata(it, appState.shareUrl) }
                        view?.playing()
                    }
                    StreamingState.Paused,
                    StreamingState.NotInitialized -> view?.idle()
                    StreamingState.Interrupted -> view?.showNetworkNotAvailable()
                }

                view?.updateVolumeSeeker(appState.streamVolume)
            }
            .disposeOnDetach()
    }

    fun play() {
        appStateRepository.dispatchAction(UpdateStreamState(StreamingState.ShouldStart))
    }

    fun pause() {
        appStateRepository.dispatchAction(UpdateStreamState(StreamingState.ShouldPause))
    }

    fun setStreamingOverMobileDataEnabled(value: Boolean) {
        currentInstallSharedPreferences.setStreamingOverMobileDataEnabled(value)
    }
}

package de.developercity.arcanosradio.features.nowplaying.presentation

import de.developercity.arcanosradio.core.extension.exhaustive
import de.developercity.arcanosradio.core.platform.base.BaseContract
import de.developercity.arcanosradio.core.platform.base.BasePresenter
import de.developercity.arcanosradio.core.provider.SchedulerProvider
import de.developercity.arcanosradio.features.streaming.RadioStreamState
import de.developercity.arcanosradio.features.streaming.RadioStreamer
import de.developercity.arcanosradio.features.streaming.domain.StreamingRepository
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val POLL_INTERVAL = 10L

class NowPlayingPresenter @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val streamingRepository: StreamingRepository,
    private val radioStreamer: RadioStreamer
) : BasePresenter<NowPlayingPresenter.View>(schedulerProvider) {

    interface View : BaseContract.View {
        fun readyToPlay()

        fun buffering()

        fun playing()

        fun paused()
    }

    fun setup() {
        view?.buffering()
        setupPlayer()
        observeStreamerState()
    }

    fun play() {
        radioStreamer.play()
    }

    private fun setupPlayer() {
        streamingRepository.getConfiguration()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.main())
            .subscribe(
                {
                    radioStreamer.run {
                        setStreamingUrl(it.streamingUrl)
                        play()
                    }
                },
                { view?.handleError(it) }
            )
            .disposeOnDetach()
    }

    private fun observeStreamerState() {
        radioStreamer.getState()
            .subscribe(
                { state ->
                    when (state) {
                        RadioStreamState.Buffering -> view?.buffering()
                        RadioStreamState.Playing -> {
                            pollSongMetadata()
                        }
                        RadioStreamState.Paused -> view?.readyToPlay()
                    }.exhaustive
                },
                { view?.handleError(it) }
            )
            .disposeOnDetach()
    }

    private fun pollSongMetadata() {
        Observable.interval(POLL_INTERVAL, TimeUnit.SECONDS)
            .flatMap { streamingRepository.getCurrentSongMetadata().toObservable() }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.main())
            .subscribe(
                {},
                { view?.handleError(it) }
            )
            .disposeOnDetach()
    }
}

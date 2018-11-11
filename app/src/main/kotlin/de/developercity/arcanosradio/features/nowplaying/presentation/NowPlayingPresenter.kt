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
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
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

    private val shouldPollMetadata: PublishSubject<Boolean> = PublishSubject.create<Boolean>()
        .also { it.onNext(false) }

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

                    shouldPollMetadata.onNext(appState.screenOn)

                    if (appState.screenOn) {
                        when (appState.streamState) {
                            StreamingState.Buffering -> view?.buffering()
                            StreamingState.Playing -> view?.playing()
                            StreamingState.Paused -> view?.readyToPlay()
                        }

                        appState.nowPlaying?.let { view?.showSongMetadata(it) }
                    }
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
            .withLatestFrom(shouldPollMetadata, BiFunction<Long, Boolean, Boolean> { _, shouldPoll -> shouldPoll })
            .flatMap { shouldPoll ->
                return@flatMap if (shouldPoll) {
                    streamingRepository.getCurrentSongMetadata()
                        .toObservable()
                        .onErrorResumeNext(Observable.empty())
                } else {
                    Observable.empty<NowPlaying>()
                }
            }
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.io())
            .subscribe { appStateRepository.updateState(UpdateNowPlaying(it)) }
            .disposeOnDetach()
    }
}

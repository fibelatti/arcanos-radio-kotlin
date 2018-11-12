package de.developercity.arcanosradio.features.streaming

import android.content.Intent
import de.developercity.arcanosradio.core.platform.base.BaseService
import de.developercity.arcanosradio.core.provider.SchedulerProvider
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateNowPlaying
import de.developercity.arcanosradio.features.streaming.domain.StreamingRepository
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val POLL_INTERVAL = 10L

class MetadataPollingService : BaseService() {

    @Inject
    lateinit var schedulerProvider: SchedulerProvider
    @Inject
    lateinit var streamingRepository: StreamingRepository
    @Inject
    lateinit var appStateRepository: AppStateRepository

    override fun onCreate() {
        super.onCreate()
        injector.inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        pollMetadata()
        return START_STICKY
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
            .disposeOnDestroy()
    }
}

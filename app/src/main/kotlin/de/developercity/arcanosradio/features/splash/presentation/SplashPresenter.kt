package de.developercity.arcanosradio.features.splash.presentation

import de.developercity.arcanosradio.core.platform.base.BaseContract
import de.developercity.arcanosradio.core.platform.base.BasePresenter
import de.developercity.arcanosradio.core.provider.SchedulerProvider
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamingConfig
import de.developercity.arcanosradio.features.streaming.domain.StreamingRepository
import javax.inject.Inject

class SplashPresenter @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val streamingRepository: StreamingRepository,
    private val appStateRepository: AppStateRepository
) : BasePresenter<SplashPresenter.View>(schedulerProvider) {

    interface View : BaseContract.View {
        fun ready()
    }

    fun setup() {
        streamingRepository.getConfiguration()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.main())
            .subscribe(
                {
                    appStateRepository.dispatchAction(
                        UpdateStreamingConfig(shareUrl = it.shareUrl, StreamingUrl = it.streamingUrl)
                    )
                    view?.ready()
                },
                { view?.handleError(it) }
            )
            .disposeOnDetach()
    }
}

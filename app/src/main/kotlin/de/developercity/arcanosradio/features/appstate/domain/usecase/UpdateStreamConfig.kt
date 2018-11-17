package de.developercity.arcanosradio.features.appstate.domain.usecase

import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamingConfigAction
import javax.inject.Inject

class UpdateStreamConfig @Inject constructor(
    private val appStateRepository: AppStateRepository
) {

    operator fun invoke(shareUrl: String, streamingUrl: String) {
        appStateRepository.dispatchAction(UpdateStreamingConfigAction(shareUrl, streamingUrl))
    }
}

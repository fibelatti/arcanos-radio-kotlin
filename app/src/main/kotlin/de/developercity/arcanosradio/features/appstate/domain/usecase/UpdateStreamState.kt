package de.developercity.arcanosradio.features.appstate.domain.usecase

import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamStateAction
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import javax.inject.Inject

class UpdateStreamState @Inject constructor(
    private val appStateRepository: AppStateRepository
) {

    operator fun invoke(streamingState: StreamingState) {
        appStateRepository.dispatchAction(UpdateStreamStateAction(streamingState))
    }
}

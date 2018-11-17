package de.developercity.arcanosradio.features.appstate.domain.usecase

import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateNetworkStateAction
import de.developercity.arcanosradio.features.streaming.domain.NetworkType
import javax.inject.Inject

class UpdateNetworkState @Inject constructor(
    private val appStateRepository: AppStateRepository
) {

    operator fun invoke(networkType: NetworkType) {
        appStateRepository.dispatchAction(UpdateNetworkStateAction(networkType))
    }
}

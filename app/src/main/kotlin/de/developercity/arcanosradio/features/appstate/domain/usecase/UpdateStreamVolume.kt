package de.developercity.arcanosradio.features.appstate.domain.usecase

import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamVolumeAction
import javax.inject.Inject

class UpdateStreamVolume @Inject constructor(
    private val appStateRepository: AppStateRepository
) {

    operator fun invoke(volume: Int) {
        appStateRepository.dispatchAction(UpdateStreamVolumeAction(volume))
    }
}

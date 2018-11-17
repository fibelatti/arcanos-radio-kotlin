package de.developercity.arcanosradio.features.appstate.domain.usecase

import de.developercity.arcanosradio.features.appstate.domain.AppState
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import io.reactivex.Observer
import javax.inject.Inject

class AddSideEffect @Inject constructor(
    private val appStateRepository: AppStateRepository
) {

    operator fun invoke(observer: Observer<AppState>) {
        appStateRepository.addSideEffect(observer)
    }
}

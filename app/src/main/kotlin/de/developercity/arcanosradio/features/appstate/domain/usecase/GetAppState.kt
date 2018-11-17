package de.developercity.arcanosradio.features.appstate.domain.usecase

import de.developercity.arcanosradio.features.appstate.domain.AppState
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import io.reactivex.Observable
import javax.inject.Inject

class GetAppState @Inject constructor(
    private val appStateRepository: AppStateRepository
) {

    operator fun invoke(): Observable<AppState> = appStateRepository.getAppState()
}

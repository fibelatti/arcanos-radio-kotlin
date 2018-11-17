package de.developercity.arcanosradio.features.appstate.domain.usecase

import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateNowPlayingAction
import de.developercity.arcanosradio.features.streaming.domain.models.NowPlaying
import javax.inject.Inject

class UpdateNowPlaying @Inject constructor(
    private val appStateRepository: AppStateRepository
) {

    operator fun invoke(nowPlaying: NowPlaying) {
        appStateRepository.dispatchAction(UpdateNowPlayingAction(nowPlaying))
    }
}

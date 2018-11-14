package de.developercity.arcanosradio.features.appstate.data

import android.net.ConnectivityManager
import de.developercity.arcanosradio.core.extension.isConnected
import de.developercity.arcanosradio.core.persistence.CurrentInstallSharedPreferences
import de.developercity.arcanosradio.features.appstate.domain.AppState
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateNetworkAvailable
import de.developercity.arcanosradio.features.appstate.domain.UpdateNowPlaying
import de.developercity.arcanosradio.features.appstate.domain.UpdateStateAction
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamState
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamVolume
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamingConfig
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateDataSource @Inject constructor(
    currentInstallSharedPreferences: CurrentInstallSharedPreferences,
    connectivityManager: ConnectivityManager?
) : AppStateRepository {

    private val observableState: BehaviorSubject<AppState> = BehaviorSubject.create()
    private var state = AppState(
        networkAvailable = connectivityManager.isConnected() ||
            currentInstallSharedPreferences.getStreamingOverMobileEnabled()
    )

    override fun getAppState(): Observable<AppState> = observableState.distinctUntilChanged()

    @Synchronized
    override fun updateState(updateStateAction: UpdateStateAction) {
        state = when (updateStateAction) {
            is UpdateStreamingConfig -> state.copy(
                shareUrl = updateStateAction.shareUrl,
                streamingUrl = updateStateAction.StreamingUrl
            )
            is UpdateStreamState -> state.copy(streamState = updateStateAction.streamState)
            is UpdateStreamVolume -> state.copy(streamVolume = updateStateAction.volume)
            is UpdateNowPlaying -> state.copy(nowPlaying = updateStateAction.nowPlaying)
            is UpdateNetworkAvailable -> state.copy(networkAvailable = updateStateAction.available)
        }
        observableState.onNext(state)
    }
}

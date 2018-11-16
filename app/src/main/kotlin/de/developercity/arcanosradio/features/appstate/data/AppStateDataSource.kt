package de.developercity.arcanosradio.features.appstate.data

import android.media.AudioManager
import android.net.ConnectivityManager
import de.developercity.arcanosradio.core.extension.getMusicVolume
import de.developercity.arcanosradio.core.extension.isConnected
import de.developercity.arcanosradio.core.extension.isConnectedToWifi
import de.developercity.arcanosradio.core.persistence.CurrentInstallSharedPreferences
import de.developercity.arcanosradio.core.provider.SchedulerProvider
import de.developercity.arcanosradio.features.appstate.domain.AppState
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateNetworkState
import de.developercity.arcanosradio.features.appstate.domain.UpdateNowPlaying
import de.developercity.arcanosradio.features.appstate.domain.UpdateStateAction
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamState
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamVolume
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamingConfig
import de.developercity.arcanosradio.features.streaming.domain.NetworkState
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateDataSource @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val currentInstallSharedPreferences: CurrentInstallSharedPreferences,
    private val connectivityManager: ConnectivityManager?,
    audioManager: AudioManager?
) : AppStateRepository {

    private var state = AppState(
        streamVolume = audioManager.getMusicVolume(),
        networkState = getNetworkState()
    )
    private val observableState: BehaviorSubject<AppState> = BehaviorSubject.create()
    private val sideEffects: BehaviorSubject<AppState> = BehaviorSubject.create()
    private val observableSideEffects: Observable<AppState> = sideEffects.distinctUntilChanged()

    override fun getAppState(): Observable<AppState> = observableState.distinctUntilChanged()

    @Synchronized
    override fun dispatchAction(updateStateAction: UpdateStateAction) {
        state = updateState(updateStateAction).also {
            observableState.onNext(it)
            sideEffects.onNext(it)
        }
    }

    override fun addSideEffect(observer: Observer<AppState>) {
        observableSideEffects.subscribeOn(schedulerProvider.io()).subscribe(observer)
    }

    private fun updateState(updateStateAction: UpdateStateAction): AppState = when (updateStateAction) {
        is UpdateStreamingConfig -> state.copy(
            shareUrl = updateStateAction.shareUrl,
            streamingUrl = updateStateAction.StreamingUrl
        )
        is UpdateStreamState -> state.copy(streamState = updateStateAction.streamState)
        is UpdateStreamVolume -> state.copy(streamVolume = updateStateAction.volume)
        is UpdateNowPlaying -> state.copy(nowPlaying = updateStateAction.nowPlaying)
        is UpdateNetworkState -> state.copy(networkState = getNetworkState())
    }

    private fun getNetworkState(): NetworkState {
        return when {
            connectivityManager.isConnectedToWifi() -> NetworkState.Connected
            connectivityManager.isConnected() &&
                currentInstallSharedPreferences.getStreamingOverMobileEnabled() -> NetworkState.Connected
            else -> NetworkState.NotConnected
        }
    }
}

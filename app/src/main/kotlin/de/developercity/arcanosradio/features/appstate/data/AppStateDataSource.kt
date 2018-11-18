package de.developercity.arcanosradio.features.appstate.data

import android.media.AudioManager
import android.net.ConnectivityManager
import de.developercity.arcanosradio.core.extension.getMusicVolume
import de.developercity.arcanosradio.core.extension.isConnected
import de.developercity.arcanosradio.core.extension.isConnectedToWifi
import de.developercity.arcanosradio.core.provider.SchedulerProvider
import de.developercity.arcanosradio.features.appstate.domain.AppState
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateNetworkStateAction
import de.developercity.arcanosradio.features.appstate.domain.UpdateNowPlayingAction
import de.developercity.arcanosradio.features.appstate.domain.UpdateStateAction
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamStateAction
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamVolumeAction
import de.developercity.arcanosradio.features.appstate.domain.UpdateStreamingConfigAction
import de.developercity.arcanosradio.features.preferences.data.CurrentInstallSharedPreferences
import de.developercity.arcanosradio.features.streaming.domain.NetworkState
import de.developercity.arcanosradio.features.streaming.domain.NetworkType
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
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
        streamState = if (getNetworkState() is NetworkState.NotConnected) {
            StreamingState.Interrupted
        } else {
            StreamingState.NotInitialized
        },
        streamVolume = audioManager.getMusicVolume(),
        networkState = getNetworkState()
    )
    private val observableState: BehaviorSubject<AppState> = BehaviorSubject.createDefault(state)
    private val sideEffects: BehaviorSubject<AppState> = BehaviorSubject.createDefault(state)
    private val observableSideEffects: Observable<AppState> = sideEffects.distinctUntilChanged()

    override fun getAppState(): Observable<AppState> = observableState.distinctUntilChanged()

    @Synchronized
    override fun dispatchAction(updateStateAction: UpdateStateAction) {
        state = updateState(updateStateAction)
        observableState.onNext(state)
        sideEffects.onNext(state)
    }

    override fun addSideEffect(observer: Observer<AppState>) {
        observableSideEffects.subscribeOn(schedulerProvider.io()).subscribe(observer)
    }

    private fun updateState(updateStateAction: UpdateStateAction): AppState = when (updateStateAction) {
        is UpdateStreamingConfigAction -> state.copy(
            shareUrl = updateStateAction.shareUrl,
            streamingUrl = updateStateAction.streamingUrl
        )
        is UpdateStreamStateAction -> state.copy(streamState = updateStateAction.streamState)
        is UpdateStreamVolumeAction -> state.copy(streamVolume = updateStateAction.volume)
        is UpdateNowPlayingAction -> state.copy(nowPlaying = updateStateAction.nowPlaying)
        is UpdateNetworkStateAction -> state.copy(networkState = getNetworkState(updateStateAction.networkType))
    }

    private fun getNetworkState(networkType: NetworkType? = null): NetworkState {
        return when {
            networkType is NetworkType.Wifi ||
                (networkType is NetworkType.MobileData && currentInstallSharedPreferences.getStreamingOverMobileDataEnabled()) ||
                connectivityManager.isConnectedToWifi() ||
                (connectivityManager.isConnected() && currentInstallSharedPreferences.getStreamingOverMobileDataEnabled()) -> {
                NetworkState.Connected
            }
            else -> NetworkState.NotConnected
        }
    }
}

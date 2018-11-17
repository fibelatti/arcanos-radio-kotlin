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
import de.developercity.arcanosradio.features.streaming.domain.NetworkType
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject
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
    private val observableState: PublishSubject<AppState> = PublishSubject.create()
    private val sideEffects: PublishSubject<AppState> = PublishSubject.create()
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
        is UpdateStreamingConfig -> state.copy(
            shareUrl = updateStateAction.shareUrl,
            streamingUrl = updateStateAction.StreamingUrl
        )
        is UpdateStreamState -> state.copy(streamState = updateStateAction.streamState)
        is UpdateStreamVolume -> state.copy(streamVolume = updateStateAction.volume)
        is UpdateNowPlaying -> state.copy(nowPlaying = updateStateAction.nowPlaying)
        is UpdateNetworkState -> state.copy(networkState = getNetworkState(updateStateAction.networkType))
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

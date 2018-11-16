package de.developercity.arcanosradio.features.streaming.device

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateNetworkState
import de.developercity.arcanosradio.features.streaming.domain.NetworkState
import javax.inject.Inject

class NetworkObserver @Inject constructor(
    private val connectivityManager: ConnectivityManager?,
    private val appStateRepository: AppStateRepository
) : ConnectivityManager.NetworkCallback() {

    override fun onAvailable(network: Network) {
        appStateRepository.dispatchAction(UpdateNetworkState(NetworkState.Available))
    }

    override fun onLost(network: Network) {
        appStateRepository.dispatchAction(UpdateNetworkState(NetworkState.Lost))
    }

    fun register() {
        connectivityManager?.registerNetworkCallback(NetworkRequest.Builder().build(), this)
    }
}

package de.developercity.arcanosradio.features.streaming.device

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateNetworkState
import de.developercity.arcanosradio.features.streaming.domain.NetworkType
import javax.inject.Inject

@Suppress("DEPRECATION")
class NetworkReceiver @Inject constructor(
    private val appStateRepository: AppStateRepository
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notConnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, true)
        val networkType = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1)

        when {
            notConnected -> {
                appStateRepository.dispatchAction(UpdateNetworkState(NetworkType.NotConnected))
            }
            networkType == ConnectivityManager.TYPE_MOBILE -> {
                appStateRepository.dispatchAction(UpdateNetworkState(NetworkType.MobileData))
            }
            networkType == ConnectivityManager.TYPE_WIFI -> {
                appStateRepository.dispatchAction(UpdateNetworkState(NetworkType.Wifi))
            }
        }
    }
}

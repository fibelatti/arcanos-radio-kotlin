package de.developercity.arcanosradio

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.provider.Settings
import com.facebook.stetho.Stetho
import de.developercity.arcanosradio.core.di.AppComponent
import de.developercity.arcanosradio.core.di.DaggerAppComponent
import de.developercity.arcanosradio.core.extension.getSystemService
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateNetworkAvailable
import de.developercity.arcanosradio.features.streaming.device.VolumeObserver
import javax.inject.Inject

class App : Application() {
    val appComponent: AppComponent by lazy(mode = LazyThreadSafetyMode.NONE) {
        DaggerAppComponent
            .builder()
            .application(this)
            .build()
    }

    @Inject
    lateinit var appStateRepository: AppStateRepository
    @Inject
    lateinit var volumeObserver: VolumeObserver

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)

        registerNetworkCallback()
        registerVolumeCallback()

        if (BuildConfig.DEBUG) Stetho.initializeWithDefaults(this)
    }

    private fun registerNetworkCallback() {
        getSystemService<ConnectivityManager>()?.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    appStateRepository.dispatchAction(UpdateNetworkAvailable(available = true))
                }

                override fun onLost(network: Network) {
                    appStateRepository.dispatchAction(UpdateNetworkAvailable(available = false))
                }
            }
        )
    }

    private fun registerVolumeCallback() {
        contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, volumeObserver)
    }
}

package de.developercity.arcanosradio

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.appcompat.app.AppCompatDelegate
import com.facebook.stetho.Stetho
import de.developercity.arcanosradio.core.di.AppComponent
import de.developercity.arcanosradio.core.di.DaggerAppComponent
import de.developercity.arcanosradio.core.extension.getSystemService
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateNetworkAvailable
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

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        appComponent.inject(this)

        registerNetworkCallback()

        if (BuildConfig.DEBUG) Stetho.initializeWithDefaults(this)
    }

    private fun registerNetworkCallback() {
        getSystemService<ConnectivityManager>()?.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    appStateRepository.updateState(UpdateNetworkAvailable(available = true))
                }

                override fun onLost(network: Network) {
                    appStateRepository.updateState(UpdateNetworkAvailable(available = false))
                }
            }
        )
    }
}

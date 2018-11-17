package de.developercity.arcanosradio

import android.app.Application
import android.content.IntentFilter
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.provider.Settings
import com.facebook.stetho.Stetho
import de.developercity.arcanosradio.core.di.AppComponent
import de.developercity.arcanosradio.core.di.DaggerAppComponent
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.streaming.device.NetworkReceiver
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
    lateinit var networkReceiver: NetworkReceiver
    @Inject
    lateinit var volumeObserver: VolumeObserver

    @Suppress("DEPRECATION")
    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)

        registerReceiver(networkReceiver, IntentFilter(CONNECTIVITY_ACTION))
        contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, volumeObserver)

        if (BuildConfig.DEBUG) Stetho.initializeWithDefaults(this)
    }
}

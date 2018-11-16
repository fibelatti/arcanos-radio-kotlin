package de.developercity.arcanosradio

import android.app.Application
import android.provider.Settings
import com.facebook.stetho.Stetho
import de.developercity.arcanosradio.core.di.AppComponent
import de.developercity.arcanosradio.core.di.DaggerAppComponent
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.streaming.device.NetworkObserver
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
    lateinit var networkObserver: NetworkObserver
    @Inject
    lateinit var volumeObserver: VolumeObserver

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)

        networkObserver.register()
        contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, volumeObserver)

        if (BuildConfig.DEBUG) Stetho.initializeWithDefaults(this)
    }
}

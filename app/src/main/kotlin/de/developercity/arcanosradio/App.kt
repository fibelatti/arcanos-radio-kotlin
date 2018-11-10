package de.developercity.arcanosradio

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import de.developercity.arcanosradio.core.di.AppComponent
import de.developercity.arcanosradio.core.di.DaggerAppComponent

class App : Application() {
    val appComponent: AppComponent by lazy(mode = LazyThreadSafetyMode.NONE) {
        DaggerAppComponent
            .builder()
            .application(this)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        appComponent.inject(this)
    }
}

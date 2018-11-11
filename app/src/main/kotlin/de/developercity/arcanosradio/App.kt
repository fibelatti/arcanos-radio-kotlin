package de.developercity.arcanosradio

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatDelegate
import de.developercity.arcanosradio.core.di.AppComponent
import de.developercity.arcanosradio.core.di.DaggerAppComponent
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository
import de.developercity.arcanosradio.features.appstate.domain.UpdateScreenState
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

        registerScreenStateBroadcastReceiver()
    }

    private fun registerScreenStateBroadcastReceiver() {
        registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    appStateRepository.updateState(
                        UpdateScreenState(screenOn = intent.action == Intent.ACTION_SCREEN_ON)
                    )
                }
            },
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            }
        )
    }
}

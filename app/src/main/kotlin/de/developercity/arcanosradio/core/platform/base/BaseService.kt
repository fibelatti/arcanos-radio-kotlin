package de.developercity.arcanosradio.core.platform.base

import android.app.Service
import android.content.Intent
import android.os.IBinder
import de.developercity.arcanosradio.App

abstract class BaseService : Service() {

    protected val injector get() = (application as App).appComponent

    override fun onBind(intent: Intent?): IBinder? = null
}

package de.developercity.arcanosradio.core.platform.base

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.annotation.CallSuper
import de.developercity.arcanosradio.App
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseService : Service() {

    protected val injector get() = (application as App).appComponent

    private val disposables = CompositeDisposable()

    override fun onBind(intent: Intent?): IBinder? = null

    @CallSuper
    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    protected fun Disposable.disposeOnDestroy() {
        disposables.add(this)
    }
}

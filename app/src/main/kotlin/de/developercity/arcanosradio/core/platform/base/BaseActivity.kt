package de.developercity.arcanosradio.core.platform.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import de.developercity.arcanosradio.App

abstract class BaseActivity : AppCompatActivity() {

    val injector get() = (application as App).appComponent

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        super.onCreate(savedInstanceState)
    }

    fun handleError(error: Throwable) {
        error.printStackTrace()
    }
}

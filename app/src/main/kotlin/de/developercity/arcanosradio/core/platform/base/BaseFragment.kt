package de.developercity.arcanosradio.core.platform.base

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    protected val injector by lazy { (activity as BaseActivity).injector }

    open fun handleError(error: Throwable) {
        error.printStackTrace()
    }
}

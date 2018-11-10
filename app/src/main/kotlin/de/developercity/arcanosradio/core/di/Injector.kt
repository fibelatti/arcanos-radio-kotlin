package de.developercity.arcanosradio.core.di

import de.developercity.arcanosradio.core.platform.base.BaseActivity

interface Injector {
    fun inject(baseActivity: BaseActivity)
}

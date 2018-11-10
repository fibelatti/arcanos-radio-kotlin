package de.developercity.arcanosradio.core.di

import de.developercity.arcanosradio.core.platform.base.BaseActivity
import de.developercity.arcanosradio.features.nowplaying.presentation.NowPlayingActivity

interface Injector {
    fun inject(baseActivity: BaseActivity)

    fun inject(nowPlayingActivity: NowPlayingActivity)
}

package de.developercity.arcanosradio.core.di

import de.developercity.arcanosradio.features.nowplaying.presentation.NowPlayingActivity

interface Injector {

    fun inject(nowPlayingActivity: NowPlayingActivity)
}

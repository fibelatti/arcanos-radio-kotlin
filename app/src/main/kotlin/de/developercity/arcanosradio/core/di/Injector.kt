package de.developercity.arcanosradio.core.di

import de.developercity.arcanosradio.features.nowplaying.presentation.NowPlayingActivity
import de.developercity.arcanosradio.features.splash.presentation.SplashActivity
import de.developercity.arcanosradio.features.streaming.device.StreamingService

interface Injector {
    fun inject(splashActivity: SplashActivity)

    fun inject(nowPlayingActivity: NowPlayingActivity)

    fun inject(streamingService: StreamingService)
}

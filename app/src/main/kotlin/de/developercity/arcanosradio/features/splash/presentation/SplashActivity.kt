package de.developercity.arcanosradio.features.splash.presentation

import android.os.Bundle
import de.developercity.arcanosradio.R
import de.developercity.arcanosradio.core.platform.base.BaseActivity
import de.developercity.arcanosradio.features.nowplaying.presentation.NowPlayingActivity
import javax.inject.Inject

class SplashActivity : BaseActivity(), SplashPresenter.View {

    @Inject
    lateinit var splashPresenter: SplashPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitivity_player)

        injector.inject(this)

        splashPresenter.attachView(this)
        splashPresenter.setup()
    }

    override fun onDestroy() {
        super.onDestroy()
        splashPresenter.detachView()
    }

    override fun ready() {
        startActivity(NowPlayingActivity.IntentBuilder(this).build())
    }
}

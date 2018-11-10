package de.developercity.arcanosradio.features.nowplaying.presentation

import android.os.Bundle
import de.developercity.arcanosradio.R
import de.developercity.arcanosradio.core.platform.base.BaseActivity
import javax.inject.Inject

class NowPlayingActivity : BaseActivity(), NowPlayingPresenter.View {

    @Inject
    lateinit var nowPlayingPresenter: NowPlayingPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitivity_player)

        injector.inject(this)

        nowPlayingPresenter.attachView(this)
        nowPlayingPresenter.setup()
    }

    override fun onDestroy() {
        super.onDestroy()
        nowPlayingPresenter.detachView()
    }

    override fun readyToPlay() {
        // TODO
    }

    override fun buffering() {
        // TODO
    }

    override fun playing() {
        // TODO
    }

    override fun paused() {
        // TODO
    }
}

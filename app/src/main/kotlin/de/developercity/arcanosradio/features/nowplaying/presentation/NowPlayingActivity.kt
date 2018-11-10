package de.developercity.arcanosradio.features.nowplaying.presentation

import android.os.Bundle
import de.developercity.arcanosradio.R
import de.developercity.arcanosradio.core.platform.base.BaseActivity

class NowPlayingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitivity_player)
    }
}

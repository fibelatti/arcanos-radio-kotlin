package de.developercity.arcanosradio.features.splash.presentation

import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import de.developercity.arcanosradio.R
import de.developercity.arcanosradio.core.platform.base.BaseActivity
import de.developercity.arcanosradio.features.nowplaying.presentation.NowPlayingActivity
import kotlinx.android.synthetic.main.activity_splash.*
import javax.inject.Inject

class SplashActivity : BaseActivity(), SplashPresenter.View {

    @Inject
    lateinit var splashPresenter: SplashPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        injector.inject(this)

        splashPresenter.attachView(this)
        splashPresenter.bootstrap()
    }

    override fun onDestroy() {
        super.onDestroy()
        splashPresenter.detachView()
    }

    override fun ready() {
        startActivity(
            NowPlayingActivity.IntentBuilder(this).build(),
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                imageViewAlbumArt,
                ViewCompat.getTransitionName(imageViewAlbumArt).orEmpty()
            ).toBundle()
        )
    }
}

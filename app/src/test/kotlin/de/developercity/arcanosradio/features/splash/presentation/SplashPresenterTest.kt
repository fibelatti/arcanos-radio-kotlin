package de.developercity.arcanosradio.features.splash.presentation

import de.developercity.arcanosradio.BaseTest
import de.developercity.arcanosradio.core.extension.mock
import de.developercity.arcanosradio.features.appstate.domain.usecase.UpdateStreamConfig
import de.developercity.arcanosradio.features.streaming.domain.StreamingRepository
import de.developercity.arcanosradio.features.streaming.domain.models.Seconds
import de.developercity.arcanosradio.features.streaming.domain.models.StreamConfiguration
import io.reactivex.Single
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify

internal class SplashPresenterTest : BaseTest() {

    private val mockStreamingRepository = mock<StreamingRepository>()
    private val mockUpdateStreamConfig = mock<UpdateStreamConfig>()

    private val mockView = mock<SplashPresenter.View>()

    private val splashPresenter = SplashPresenter(
        mockSchedulerProvider,
        mockStreamingRepository,
        mockUpdateStreamConfig
    )

    @BeforeEach
    fun setup() {
        splashPresenter.attachView(mockView)
    }

    @Nested
    internal inner class Bootstrap {
        @Test
        fun `GIVEN getConfiguration fails WHEN bootstrap is called THEN handleError is called`() {
            // GIVEN
            val error = Exception()

            given(mockStreamingRepository.getConfiguration())
                .willReturn(Single.error(error))

            // WHEN
            splashPresenter.bootstrap()

            // THEN
            verify(mockView).handleError(error)
        }

        @Test
        fun `GIVEN getConfiguration succeeds WHEN bootstrap is called THEN handleError is called`() {
            // GIVEN
            val configuration = StreamConfiguration(
                streamingUrl = "streamingUrl",
                shareUrl = "shareUrl",
                pollingInterval = Seconds(1)
            )

            given(mockStreamingRepository.getConfiguration())
                .willReturn(Single.just(configuration))

            // WHEN
            splashPresenter.bootstrap()

            // THEN
            verify(mockUpdateStreamConfig).invoke(shareUrl = "shareUrl", streamingUrl = "streamingUrl")
            verify(mockView).ready()
        }
    }
}

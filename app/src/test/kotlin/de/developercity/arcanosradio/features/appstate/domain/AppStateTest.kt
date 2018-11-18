package de.developercity.arcanosradio.features.appstate.domain

import de.developercity.arcanosradio.BaseTest
import de.developercity.arcanosradio.core.extension.shouldBe
import de.developercity.arcanosradio.features.streaming.domain.NetworkState
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import org.junit.jupiter.api.Test

internal class AppStateTest : BaseTest() {

    @Test
    fun testAppStateInitialValues() {
        // GIVEN
        val expectedInitialState = AppState(
            shareUrl = "",
            streamingUrl = "",
            streamState = StreamingState.NotInitialized,
            streamVolume = 0,
            nowPlaying = null,
            networkState = NetworkState.NotConnected
        )

        // THEN
        AppState() shouldBe expectedInitialState
    }
}

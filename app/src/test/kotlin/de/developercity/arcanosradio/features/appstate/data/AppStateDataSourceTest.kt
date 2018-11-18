package de.developercity.arcanosradio.features.appstate.data

import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import de.developercity.arcanosradio.BaseTest
import de.developercity.arcanosradio.core.extension.assertObservable
import de.developercity.arcanosradio.core.extension.mock
import de.developercity.arcanosradio.features.appstate.domain.AppState
import de.developercity.arcanosradio.features.preferences.data.CurrentInstallSharedPreferences
import de.developercity.arcanosradio.features.streaming.domain.NetworkState
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given

internal class AppStateDataSourceTest : BaseTest() {

    private val mockCurrentInstallSharedPreferences = mock<CurrentInstallSharedPreferences>()
    private val mockConnectivityManager = mock<ConnectivityManager>()
    private val mockNetworkInfo = mock<NetworkInfo>()
    private val mockAudioManager = mock<AudioManager>()

    private lateinit var appStateDataSource: AppStateDataSource

    private val mockVolume = 10

    @Nested
    internal inner class Constructor {

        @Test
        fun `GIVEN getNetworkState returns NotConnected WHEN AppStateDataSource is instantiated THEN first state is created with streamState = Interrupted and networkState = NotConnected`() {
            // GIVEN
            arrangeAppStateDataSource(
                isConnectedToWifi = false,
                isConnected = false
            )

            val expectedState = AppState(
                streamState = StreamingState.Interrupted,
                streamVolume = mockVolume,
                networkState = NetworkState.NotConnected
            )

            // THEN
            appStateDataSource.getAppState()
                .test()
                .assertObservable(expectedState)
        }

        @Test
        fun `GIVEN getNetworkState returns Connected WHEN AppStateDataSource is instantiated THEN first state is created with streamState = NotInitialized and networkState = Connected`() {
            // GIVEN
            arrangeAppStateDataSource(
                isConnectedToWifi = true
            )

            val expectedState = AppState(
                streamState = StreamingState.NotInitialized,
                streamVolume = mockVolume,
                networkState = NetworkState.Connected
            )

            // THEN
            appStateDataSource.getAppState()
                .test()
                .assertObservable(expectedState)
        }
    }

    private fun arrangeAppStateDataSource(
        volume: Int = mockVolume,
        isConnectedToWifi: Boolean = true,
        isConnected: Boolean = true,
        streamingOverMobileDataEnabled: Boolean = true
    ) {
        given(mockAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
            .willReturn(volume)
        given(mockConnectivityManager.activeNetworkInfo)
            .willReturn(mockNetworkInfo)
        given(mockNetworkInfo.isConnected)
            .willReturn(isConnected)
        given(mockNetworkInfo.type)
            .willReturn(if (isConnectedToWifi) ConnectivityManager.TYPE_WIFI else ConnectivityManager.TYPE_MOBILE)
        given(mockCurrentInstallSharedPreferences.getStreamingOverMobileDataEnabled())
            .willReturn(streamingOverMobileDataEnabled)

        appStateDataSource = createAppStateDataSourceInstance()
    }

    private fun createAppStateDataSourceInstance() = AppStateDataSource(
        mockSchedulerProvider,
        mockCurrentInstallSharedPreferences,
        mockConnectivityManager,
        mockAudioManager
    )
}

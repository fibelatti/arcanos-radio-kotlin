package de.developercity.arcanosradio.features.appstate.domain

import de.developercity.arcanosradio.features.streaming.domain.NetworkState
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import de.developercity.arcanosradio.features.streaming.domain.models.NowPlaying

data class AppState(
    val shareUrl: String = "",
    val streamingUrl: String = "",
    val streamState: StreamingState = StreamingState.NotInitialized,
    val streamVolume: Int = 0,
    val nowPlaying: NowPlaying? = null,
    val networkState: NetworkState = NetworkState.NotConnected
)

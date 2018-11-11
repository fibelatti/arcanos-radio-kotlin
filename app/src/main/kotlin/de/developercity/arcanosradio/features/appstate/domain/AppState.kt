package de.developercity.arcanosradio.features.appstate.domain

import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import de.developercity.arcanosradio.features.streaming.domain.models.NowPlaying

data class AppState(
    val shareUrl: String = "",
    val streamingUrl: String = "",
    val streamState: StreamingState = StreamingState.NotInitialized,
    val nowPlaying: NowPlaying? = null,
    val screenOn: Boolean = true,
    val networkAvailable: Boolean = false
)

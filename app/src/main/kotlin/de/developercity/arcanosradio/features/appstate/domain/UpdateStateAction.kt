package de.developercity.arcanosradio.features.appstate.domain

import de.developercity.arcanosradio.features.streaming.domain.NetworkType
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import de.developercity.arcanosradio.features.streaming.domain.models.NowPlaying

sealed class UpdateStateAction

class UpdateStreamingConfigAction(val shareUrl: String, val streamingUrl: String) : UpdateStateAction()

class UpdateStreamStateAction(val streamState: StreamingState) : UpdateStateAction()

class UpdateStreamVolumeAction(val volume: Int) : UpdateStateAction()

class UpdateNowPlayingAction(val nowPlaying: NowPlaying) : UpdateStateAction()

class UpdateNetworkStateAction(val networkType: NetworkType) : UpdateStateAction()

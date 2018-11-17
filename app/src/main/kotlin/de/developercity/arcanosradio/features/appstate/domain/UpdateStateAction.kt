package de.developercity.arcanosradio.features.appstate.domain

import de.developercity.arcanosradio.features.streaming.domain.NetworkType
import de.developercity.arcanosradio.features.streaming.domain.StreamingState
import de.developercity.arcanosradio.features.streaming.domain.models.NowPlaying

sealed class UpdateStateAction

class UpdateStreamingConfig(val shareUrl: String, val StreamingUrl: String) : UpdateStateAction()

class UpdateStreamState(val streamState: StreamingState) : UpdateStateAction()

class UpdateStreamVolume(val volume: Int) : UpdateStateAction()

class UpdateNowPlaying(val nowPlaying: NowPlaying) : UpdateStateAction()

class UpdateNetworkState(val networkType: NetworkType) : UpdateStateAction()

package de.developercity.arcanosradio.features.streaming.domain

sealed class StreamingState {
    object NotInitialized : StreamingState()
    object Buffering : StreamingState()
    object Playing : StreamingState()
    object Paused : StreamingState()
    object Interrupted : StreamingState()
}

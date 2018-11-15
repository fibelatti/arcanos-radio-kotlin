package de.developercity.arcanosradio.features.streaming.domain

sealed class StreamingState {
    object NotInitialized : StreamingState()
    object ShouldStart : StreamingState()
    object Buffering : StreamingState()
    object Playing : StreamingState()
    object ShouldPause : StreamingState()
    object Paused : StreamingState()
    object Interrupted : StreamingState()
    object ShouldTerminate : StreamingState()
}

package de.developercity.arcanosradio.features.streaming.domain.models

data class StreamConfiguration(
    val streamingUrl: String,
    val shareUrl: String,
    val pollingInterval: Seconds
)

inline class Seconds(val value: Int)

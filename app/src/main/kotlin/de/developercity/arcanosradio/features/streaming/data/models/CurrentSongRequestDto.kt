package de.developercity.arcanosradio.features.streaming.data.models

data class CurrentSongRequestDto(
    val include: String? = "song.artist",
    val order: String? = "-createdAt",
    val _method: String? = "GET",
    val limit: String? = "1"
)

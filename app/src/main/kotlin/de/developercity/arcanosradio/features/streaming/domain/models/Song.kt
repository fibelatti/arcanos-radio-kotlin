package de.developercity.arcanosradio.features.streaming.domain.models

data class Song(
    val name: String,
    val artist: Artist,
    val albumArt: String,
    val lyrics: String
)

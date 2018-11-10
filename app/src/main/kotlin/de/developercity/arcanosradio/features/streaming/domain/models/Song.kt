package de.developercity.arcanosradio.features.streaming.domain.models

data class Song(
    val songName: String,
    val artist: Artist,
    val albumArt: String,
    val lyrics: String
)

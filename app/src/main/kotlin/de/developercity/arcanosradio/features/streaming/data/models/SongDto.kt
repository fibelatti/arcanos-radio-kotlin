package de.developercity.arcanosradio.features.streaming.data.models

import de.developercity.arcanosradio.core.functional.Mapper
import de.developercity.arcanosradio.features.streaming.domain.models.Song
import javax.inject.Inject

data class SongDto(
    val songName: String?,
    val artist: ArtistDto,
    val albumArt: ApiResourceDto,
    val lyrics: ApiResourceDto
)

class SongDtoMapper @Inject constructor(
    private val artistDtoMapper: ArtistDtoMapper
) : Mapper<SongDto, Song> {

    override fun map(param: SongDto): Song = with(param) {
        Song(songName.orEmpty(), artistDtoMapper.map(artist), albumArt.url.orEmpty(), lyrics.url.orEmpty())
    }
}

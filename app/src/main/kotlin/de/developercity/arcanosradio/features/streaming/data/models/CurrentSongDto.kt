package de.developercity.arcanosradio.features.streaming.data.models

import de.developercity.arcanosradio.core.functional.Mapper
import de.developercity.arcanosradio.features.streaming.domain.models.NowPlaying
import javax.inject.Inject

data class CurrentSongDto(
    val results: List<CurrentSongResultDto>
)

data class CurrentSongResultDto(
    val title: String,
    val song: SongDto
)

class CurrentSongDtoMapper @Inject constructor(
    private val songDtoMapper: SongDtoMapper
) : Mapper<CurrentSongDto, NowPlaying> {

    override fun map(param: CurrentSongDto): NowPlaying = with(param.results.first()) {
        NowPlaying(title, songDtoMapper.map(song))
    }
}

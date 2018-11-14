package de.developercity.arcanosradio.features.streaming.data.models

import de.developercity.arcanosradio.core.functional.Mapper
import de.developercity.arcanosradio.features.streaming.domain.models.Artist
import javax.inject.Inject

data class ArtistDto(
    val artistName: String?,
    val url: String?
)

class ArtistDtoMapper @Inject constructor() : Mapper<ArtistDto, Artist> {
    override fun map(param: ArtistDto): Artist = with(param) {
        Artist(artistName.orEmpty(), url.orEmpty())
    }
}

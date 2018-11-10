package de.developercity.arcanosradio.features.streaming.data

import de.developercity.arcanosradio.features.streaming.data.models.CurrentSongDto
import de.developercity.arcanosradio.features.streaming.data.models.CurrentSongDtoMapper
import de.developercity.arcanosradio.features.streaming.data.models.CurrentSongRequestDto
import de.developercity.arcanosradio.features.streaming.data.models.StreamConfigurationDto
import de.developercity.arcanosradio.features.streaming.data.models.StreamConfigurationDtoMapper
import de.developercity.arcanosradio.features.streaming.domain.StreamingRepository
import de.developercity.arcanosradio.features.streaming.domain.models.NowPlaying
import de.developercity.arcanosradio.features.streaming.domain.models.StreamConfiguration
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import javax.inject.Inject

class StreamingDataSource @Inject constructor(
    private val streamingApi: StreamingApi,
    private val streamConfigurationDtoMapper: StreamConfigurationDtoMapper,
    private val currentSongDtoMapper: CurrentSongDtoMapper
) : StreamingRepository {
    override fun getConfiguration(): Single<StreamConfiguration> =
        streamingApi.getConfiguration()
            .map(streamConfigurationDtoMapper::map)

    override fun getCurrentSongMetadata(): Single<NowPlaying> =
        streamingApi.getCurrentSongMetadata(CurrentSongRequestDto())
            .map(currentSongDtoMapper::map)
}

interface StreamingApi {
    @GET("/parse/config")
    fun getConfiguration(): Single<StreamConfigurationDto>

    @POST("/parse/classes/Playlist")
    fun getCurrentSongMetadata(@Body currentSongRequestDto: CurrentSongRequestDto): Single<CurrentSongDto>
}

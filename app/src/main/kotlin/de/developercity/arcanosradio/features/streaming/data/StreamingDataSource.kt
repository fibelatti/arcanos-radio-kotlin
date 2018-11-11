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
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.BufferedReader
import java.io.InputStreamReader
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
            .flatMap { nowPlaying ->
                getLyrics(nowPlaying.song.lyrics).map {
                    nowPlaying.copy(song = nowPlaying.song.copy(lyrics = it))
                }
            }

    private fun getLyrics(lyricsUrl: String): Single<String> =
        streamingApi.getSongLyrics(lyricsUrl)
            .map {
                val bufferedReader = BufferedReader(InputStreamReader(it.byteStream()))
                var currentLine = bufferedReader.readLine()
                val stringBuilder = StringBuilder().append("$currentLine\n")

                while (currentLine != null) {
                    stringBuilder.append("$currentLine\n")
                    currentLine = bufferedReader.readLine()
                }
                bufferedReader.close()

                stringBuilder.toString()
            }
}

interface StreamingApi {
    @GET("/parse/config")
    fun getConfiguration(): Single<StreamConfigurationDto>

    @POST("/parse/classes/Playlist")
    fun getCurrentSongMetadata(@Body currentSongRequestDto: CurrentSongRequestDto): Single<CurrentSongDto>

    @Streaming
    @GET
    fun getSongLyrics(@Url lyricsUrl: String): Single<ResponseBody>
}

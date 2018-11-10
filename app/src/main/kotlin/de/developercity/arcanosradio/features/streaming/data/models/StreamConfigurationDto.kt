package de.developercity.arcanosradio.features.streaming.data.models

import de.developercity.arcanosradio.core.functional.Mapper
import de.developercity.arcanosradio.features.streaming.domain.models.Seconds
import de.developercity.arcanosradio.features.streaming.domain.models.StreamConfiguration
import javax.inject.Inject

data class StreamConfigurationDto(
    val params: Params
)

data class Params(
    val androidStreamingUrl: String,
    val androidShareUrl: String,
    val androidPoolingTimeActive: Int
)

class StreamConfigurationDtoMapper @Inject constructor() :
    Mapper<StreamConfigurationDto, StreamConfiguration> {

    override fun map(param: StreamConfigurationDto): StreamConfiguration = with(param.params) {
        StreamConfiguration(
            androidStreamingUrl, androidShareUrl, Seconds(androidPoolingTimeActive)
        )
    }
}

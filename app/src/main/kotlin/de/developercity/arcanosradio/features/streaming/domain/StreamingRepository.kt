package de.developercity.arcanosradio.features.streaming.domain

import de.developercity.arcanosradio.features.streaming.domain.models.NowPlaying
import de.developercity.arcanosradio.features.streaming.domain.models.StreamConfiguration
import io.reactivex.Single

interface StreamingRepository {
    fun getConfiguration(): Single<StreamConfiguration>

    fun getCurrentSongMetadata(): Single<NowPlaying>
}

package de.developercity.arcanosradio.core.di.modules

import dagger.Binds
import dagger.Module
import dagger.Provides
import de.developercity.arcanosradio.features.streaming.data.StreamingApi
import de.developercity.arcanosradio.features.streaming.data.StreamingDataSource
import de.developercity.arcanosradio.features.streaming.domain.StreamingRepository
import retrofit2.Retrofit

@Module(includes = [StreamingModule.Binder::class])
object StreamingModule {

    @Module
    interface Binder {
        @Binds
        fun streamingRepository(streamingDataSource: StreamingDataSource): StreamingRepository
    }

    @Provides
    @JvmStatic
    fun streamingApi(retrofit: Retrofit): StreamingApi = retrofit.create(StreamingApi::class.java)
}

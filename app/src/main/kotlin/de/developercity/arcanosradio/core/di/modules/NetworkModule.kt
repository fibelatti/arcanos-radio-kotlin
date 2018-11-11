package de.developercity.arcanosradio.core.di.modules

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import de.developercity.arcanosradio.BuildConfig
import de.developercity.arcanosradio.core.network.HeadersInterceptor
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private const val MAX_CACHE_SIZE: Long = 10 * 1024 * 1024 /* 10mb */

@Module(includes = [NetworkModule.Binder::class])
object NetworkModule {

    @Module
    interface Binder {
        @Binds
        fun headersInterceptor(headersInterceptor: HeadersInterceptor): Interceptor
    }

    @Provides
    @JvmStatic
    @Singleton
    fun retrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    @Provides
    @JvmStatic
    fun okHttpClient(
        okHttpClientBuilder: OkHttpClient.Builder,
        context: Context
    ): OkHttpClient = okHttpClientBuilder
        .cache(Cache(context.cacheDir, MAX_CACHE_SIZE))
        .apply { if (BuildConfig.DEBUG) addNetworkInterceptor(StethoInterceptor()) }
        .build()

    @Provides
    @JvmStatic
    fun okHttpClientBuilder(
        headersInterceptor: HeadersInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .addInterceptor(headersInterceptor)
            .addInterceptor(loggingInterceptor)

    @Provides
    @JvmStatic
    fun httpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG) level = HttpLoggingInterceptor.Level.BODY
        }
}

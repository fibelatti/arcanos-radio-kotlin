package de.developercity.arcanosradio.core.di.modules

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import de.developercity.arcanosradio.App
import de.developercity.arcanosradio.core.platform.AppResourceProvider
import de.developercity.arcanosradio.core.provider.ResourceProvider
import java.text.Collator
import java.util.Locale

@Module(includes = [
    CoreModule.Binder::class
])
object CoreModule {
    @Module
    interface Binder {
        @Binds
        fun bindContext(app: App): Context

        @Binds
        fun bindResourceProvider(appResourceProvider: AppResourceProvider): ResourceProvider
    }

    @Provides
    @JvmStatic
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @JvmStatic
    fun provideLocaleDefault(): Locale = Locale.getDefault()

    @Provides
    @JvmStatic
    fun provideUSCollator(): Collator = Collator.getInstance(Locale.US)
}

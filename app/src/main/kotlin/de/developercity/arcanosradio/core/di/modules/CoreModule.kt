package de.developercity.arcanosradio.core.di.modules

import android.content.Context
import android.media.AudioManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import de.developercity.arcanosradio.App
import de.developercity.arcanosradio.core.extension.getSystemService
import de.developercity.arcanosradio.core.platform.AppResourceProvider
import de.developercity.arcanosradio.core.provider.AppSchedulerProvider
import de.developercity.arcanosradio.core.provider.ResourceProvider
import de.developercity.arcanosradio.core.provider.SchedulerProvider
import java.text.Collator
import java.util.Locale

@Module(includes = [
    CoreModule.Binder::class
])
object CoreModule {
    @Module
    interface Binder {
        @Binds
        fun context(app: App): Context

        @Binds
        fun resourceProvider(appResourceProvider: AppResourceProvider): ResourceProvider

        @Binds
        fun schedulerProvider(appSchedulerProvider: AppSchedulerProvider): SchedulerProvider
    }

    @Provides
    @JvmStatic
    fun gson(): Gson = GsonBuilder().create()

    @Provides
    @JvmStatic
    fun localeDefault(): Locale = Locale.getDefault()

    @Provides
    @JvmStatic
    fun usCollator(): Collator = Collator.getInstance(Locale.US)

    @Provides
    @JvmStatic
    fun audioManager(context: Context): AudioManager? = context.getSystemService<AudioManager>()
}

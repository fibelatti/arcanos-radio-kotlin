package de.developercity.arcanosradio.core.di

import dagger.BindsInstance
import dagger.Component
import de.developercity.arcanosradio.App
import de.developercity.arcanosradio.core.di.modules.CoreModule
import javax.inject.Singleton

@Component(modules = [
    CoreModule::class
])
@Singleton
interface AppComponent : Injector {

    @Component.Builder
    interface Builder {
        fun build(): AppComponent

        @BindsInstance
        fun application(application: App): Builder
    }

    fun inject(application: App)
}

package de.developercity.arcanosradio.core.di.modules

import dagger.Binds
import dagger.Module
import de.developercity.arcanosradio.features.appstate.data.AppStateDataSource
import de.developercity.arcanosradio.features.appstate.domain.AppStateRepository

@Module
interface AppStateModule {

    @Binds
    fun appStateRepository(appStateDataSource: AppStateDataSource): AppStateRepository
}

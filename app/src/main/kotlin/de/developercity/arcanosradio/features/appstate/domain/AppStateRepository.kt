package de.developercity.arcanosradio.features.appstate.domain

import io.reactivex.Observable

interface AppStateRepository {
    fun getAppState(): Observable<AppState>

    fun updateState(updateStateAction: UpdateStateAction)
}

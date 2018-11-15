package de.developercity.arcanosradio.features.appstate.domain

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

interface AppStateRepository {
    fun getAppState(): Observable<AppState>

    fun dispatchAction(updateStateAction: UpdateStateAction)

    fun addSideEffect(observer: Observer<AppState>)
}

object DefaultAppStateObserver : Observer<AppState> {
    override fun onComplete() {}
    override fun onSubscribe(disposable: Disposable) {}
    override fun onNext(state: AppState) {}
    override fun onError(error: Throwable) {}
}

package de.developercity.arcanosradio.core.extension

import io.reactivex.observers.TestObserver

fun <T> TestObserver<T>.assertObservable(vararg item: T): TestObserver<T> = apply {
    assertSubscribed()
    assertValues(*item)
    assertNoErrors()
}

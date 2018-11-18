package de.developercity.arcanosradio

import de.developercity.arcanosradio.core.provider.SchedulerProvider
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

abstract class BaseTest {

    protected val mockSchedulerProvider: SchedulerProvider by lazy {
        object : SchedulerProvider {
            override fun main(): Scheduler = Schedulers.trampoline()

            override fun io(): Scheduler = Schedulers.trampoline()

            override fun computation(): Scheduler = Schedulers.trampoline()
        }
    }
}

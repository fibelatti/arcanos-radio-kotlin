package de.developercity.arcanosradio.core.platform.base

interface BaseContract {
    interface View {
        fun handleError(error: Throwable)
    }
}

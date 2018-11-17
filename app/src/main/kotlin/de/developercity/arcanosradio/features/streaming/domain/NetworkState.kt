package de.developercity.arcanosradio.features.streaming.domain

sealed class NetworkState {
    object Connected : NetworkState()
    object NotConnected : NetworkState()
}

sealed class NetworkType {
    object NotConnected : NetworkType()
    object MobileData : NetworkType()
    object Wifi : NetworkType()
}

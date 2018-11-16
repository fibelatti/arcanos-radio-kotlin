package de.developercity.arcanosradio.features.streaming.domain

sealed class NetworkState {
    object Available : NetworkState()
    object Lost : NetworkState()
    object Connected : NetworkState()
    object NotConnected : NetworkState()
}

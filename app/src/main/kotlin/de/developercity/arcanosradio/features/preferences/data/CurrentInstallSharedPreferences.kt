package de.developercity.arcanosradio.features.preferences.data

import android.content.SharedPreferences
import de.developercity.arcanosradio.core.extension.get
import de.developercity.arcanosradio.core.extension.put
import javax.inject.Inject

const val KEY_ENABLE_STREAMING_OVER_MOBILE_DATA = "KEY_ENABLE_STREAMING_OVER_MOBILE_DATA"

class CurrentInstallSharedPreferences @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    fun getStreamingOverMobileDataEnabled(): Boolean =
        sharedPreferences.get(KEY_ENABLE_STREAMING_OVER_MOBILE_DATA, false)

    fun setStreamingOverMobileDataEnabled(value: Boolean) {
        sharedPreferences.put(KEY_ENABLE_STREAMING_OVER_MOBILE_DATA, value)
    }
}

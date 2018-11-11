package de.developercity.arcanosradio.core.persistence

import android.content.SharedPreferences
import de.developercity.arcanosradio.core.extension.get
import de.developercity.arcanosradio.core.extension.put
import javax.inject.Inject

const val KEY_ENABLE_STREAMING_OVER_MOBILE_DATA = "KEY_ENABLE_STREAMING_OVER_MOBILE_DATA"

class CurrentInstallSharedPreferences @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    fun getStreamingOverMobileEnabled(): Boolean =
        sharedPreferences.get(KEY_ENABLE_STREAMING_OVER_MOBILE_DATA, false)

    fun setStreamingOverMobileEnabled(value: Boolean) {
        sharedPreferences.put(KEY_ENABLE_STREAMING_OVER_MOBILE_DATA, value)
    }
}

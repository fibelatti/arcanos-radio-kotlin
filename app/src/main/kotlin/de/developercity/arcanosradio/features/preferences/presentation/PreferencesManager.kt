package de.developercity.arcanosradio.features.preferences.presentation

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import de.developercity.arcanosradio.R
import kotlinx.android.synthetic.main.layout_preferences_manager.*

interface PreferencesManager {
    fun showPreferencesManager(
        context: Context,
        streamingOverMobileDataEnabled: Boolean,
        onAllowStreamingOverMobileDataChanged: (Boolean) -> Unit
    )
}

class PreferencesManagerDelegate : PreferencesManager {

    override fun showPreferencesManager(
        context: Context,
        streamingOverMobileDataEnabled: Boolean,
        onAllowStreamingOverMobileDataChanged: (Boolean) -> Unit
    ) {
        BottomSheetDialog(context, R.style.AppTheme_BaseBottomSheetDialog_BottomSheetDialog).apply {
            setContentView(R.layout.layout_preferences_manager)

            checkboxAllowStreamingOverMobileData.isChecked = streamingOverMobileDataEnabled
            checkboxAllowStreamingOverMobileData.setOnCheckedChangeListener { _, isChecked ->
                onAllowStreamingOverMobileDataChanged(isChecked)
            }

            show()
        }
    }
}

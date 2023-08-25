package com.dashlane.ui.activities.debug

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.dashlane.BuildConfig
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.session.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DebugFragment : PreferenceFragmentCompat() {

    @Inject
    internal lateinit var debugCategoryAccountsManager: DebugCategoryAccountsManager

    @Inject
    lateinit var deviceInfoRepository: DeviceInfoRepository

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val activity = requireActivity()
        val screen = preferenceManager.createPreferenceScreen(activity)

        if (BuildConfig.DEBUG) {
            DebugCategoryDevOption(activity, deviceInfoRepository).add(screen)
        }
        sessionManager.session?.let { DebugCategoryCryptography(activity, it).add(screen) }

        debugCategoryAccountsManager.add(screen)
        DebugCategorySync(activity).add(screen)
        RacletteDebugCategory(activity).add(screen)
        preferenceScreen = screen
    }
}
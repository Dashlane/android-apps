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
    internal lateinit var debugCategorySync: DebugCategorySync

    @Inject
    internal lateinit var racletteDebugCategory: RacletteDebugCategory

    @Inject
    internal lateinit var deviceInfoRepository: DeviceInfoRepository

    @Inject
    internal lateinit var debugCategoryCryptography: DebugCategoryCryptography

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val activity = requireActivity()
        val screen = preferenceManager.createPreferenceScreen(activity)

        if (BuildConfig.DEBUG) {
            DebugCategoryDevOption(activity, deviceInfoRepository).add(screen)
        }
        sessionManager.session?.let { debugCategoryCryptography.add(screen) }

        debugCategoryAccountsManager.add(screen)
        debugCategorySync.add(screen)
        racletteDebugCategory.add(screen)
        preferenceScreen = screen
    }
}
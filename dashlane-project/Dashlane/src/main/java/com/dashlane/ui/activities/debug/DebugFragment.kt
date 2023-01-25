package com.dashlane.ui.activities.debug

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.dashlane.BuildConfig
import com.dashlane.dagger.singleton.SingletonProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DebugFragment : PreferenceFragmentCompat() {

    @Inject
    internal lateinit var debugCategoryAccountsManager: DebugCategoryAccountsManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = requireActivity()
        val screen = preferenceManager.createPreferenceScreen(context)

        if (BuildConfig.DEBUG) {
            DebugCategoryDevOption(context).add(screen)
        }
        val session = SingletonProvider.getSessionManager().session
        session?.let { DebugCategoryCryptography(context, it).add(screen) }

        debugCategoryAccountsManager.add(screen)
        DebugCategorySync(context).add(screen)
        RacletteDebugCategory(context).add(screen)
        preferenceScreen = screen
    }
}
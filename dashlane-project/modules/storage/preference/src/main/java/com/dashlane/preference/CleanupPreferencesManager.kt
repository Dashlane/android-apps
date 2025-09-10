package com.dashlane.preference

import com.dashlane.preference.ConstantsPrefs.Companion.PHISHING_MODEL_LAST_CHECK_TIMESTAMP
import com.dashlane.preference.ConstantsPrefs.Companion.PHISHING_WARNING_IGNORED_WEBSITES
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class CleanupPreferencesManager @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    private val globalPreferencesManager: GlobalPreferencesManager,
) {
    @Suppress("DEPRECATION")
    fun cleanup() {
        applicationCoroutineScope.launch {
            val itemsToRemove = listOf(
                
                PHISHING_MODEL_LAST_CHECK_TIMESTAMP,
                
                PHISHING_WARNING_IGNORED_WEBSITES,
            ).filter { globalPreferencesManager.contains(it) }

            if (itemsToRemove.isNotEmpty()) {
                globalPreferencesManager.remove(itemsToRemove)
            }
        }
    }
}
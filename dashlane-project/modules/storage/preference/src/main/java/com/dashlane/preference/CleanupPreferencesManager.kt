package com.dashlane.preference

import com.dashlane.preference.ConstantsPrefs.Companion.DEVICE_COUNTRY
import com.dashlane.preference.ConstantsPrefs.Companion.DEVICE_COUNTRY_REFRESH
import com.dashlane.preference.ConstantsPrefs.Companion.DEVICE_EUROPEAN_UNION_STATUS
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class CleanupPreferencesManager @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    private val globalPreferencesManager: GlobalPreferencesManager,
) {

    fun cleanup() {
        applicationCoroutineScope.launch {
            val itemsToRemove = listOf(
                DEVICE_COUNTRY,
                DEVICE_COUNTRY_REFRESH,
                DEVICE_EUROPEAN_UNION_STATUS,
            ).filter { globalPreferencesManager.contains(it) }

            if (itemsToRemove.isNotEmpty()) {
                globalPreferencesManager.remove(itemsToRemove)
            }
        }
    }
}
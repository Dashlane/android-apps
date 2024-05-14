package com.dashlane.accountstatus.premiumstatus

import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferenceEntry
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.endpoints.premium.PremiumStatus
import javax.inject.Inject

class AccountStatusPreferencesRefresher @Inject constructor(
    private val preferencesManager: UserPreferencesManager
) {

    fun resetPremiumNotificationIfChanged(
        newStatus: PremiumStatus,
        lastSaved: PremiumStatus?
    ) {
        lastSaved ?: return

        val hasPlanFeaturesChanged = newStatus.planFeature != lastSaved.planFeature
        val hasEndDateChanged = newStatus.endDate != lastSaved.endDate
        val hasFamilyStatusChanged = newStatus.familyStatus != lastSaved.familyStatus
        val deletePref = hasPlanFeaturesChanged || hasEndDateChanged || hasFamilyStatusChanged
        if (deletePref) {
            preferencesManager.apply(
                listOf(
                    PreferenceEntry.toRemove(
                        ConstantsPrefs.PREMIUM_RENEWAL_FIRST_NOTIFICATION_DONE
                    ),
                    PreferenceEntry.toRemove(
                        ConstantsPrefs.PREMIUM_RENEWAL_SECOND_NOTIFICATION_DONE
                    ),
                    PreferenceEntry.toRemove(
                        ConstantsPrefs.PREMIUM_RENEWAL_THIRD_NOTIFICATION_DONE
                    ),
                    PreferenceEntry.toRemove(
                        ConstantsPrefs.GRACE_PERIOD_END_NOTIFICATION_DONE
                    )
                )
            )
        }
    }
}